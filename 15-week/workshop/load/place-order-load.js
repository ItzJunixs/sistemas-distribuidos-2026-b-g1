// k6 load test of OptiView through opti-api-gateway (week 15, story TST-03).
//
// Two scenarios at the same time:
//   reads   GET /api/v1/frames and /api/v1/patients, arrival rate stepping 5 -> 10 -> 20 -> 40 req/s
//           (40 is above the gateway limit of 30 r/s per client IP + burst 60: the knee we expect)
//   orders  POST /api/v1/sagas/place-order at 2 sagas/s: check-patient -> reserve-stock -> open-order
//
// Thresholds = the SLOs of week 11 (05-slo.md) and NFR-001:
//   place-order: 95 % under 1 s, availability >= 99.5 % (COMPLETED or COMPENSATED is a good answer)
//   reads:       p95 < 300 ms
// 429 answers of the rate limiter are counted apart (rate_limited) and are not errors: they are
// the gateway protecting the services, by design.
//
// Run: see ../HOW-TO-RUN.md (docker run grafana/k6 on the platform network).
import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

const BASE = __ENV.BASE_URL || 'http://api-gateway:8000';
const PASSWORD = __ENV.DEMO_PASSWORD || 'OptiView2026';
const PATIENT = __ENV.PATIENT_ID || '11111111-1111-4111-8111-111111111111'; // seeded, ACTIVE

const rateLimited = new Counter('rate_limited');
const sagaGood = new Rate('saga_good_outcome');
const sagaLatency = new Trend('saga_latency', true);

http.setResponseCallback(http.expectedStatuses({ min: 200, max: 299 }, 429));

export const options = {
  scenarios: {
    reads: {
      executor: 'ramping-arrival-rate', exec: 'reads', startRate: 5, timeUnit: '1s',
      preAllocatedVUs: 20, maxVUs: 80,
      stages: [
        { target: 5, duration: '1m' }, { target: 10, duration: '1m' },
        { target: 20, duration: '1m' }, { target: 40, duration: '1m' },
      ],
    },
    orders: {
      executor: 'constant-arrival-rate', exec: 'orders', rate: 2, timeUnit: '1s',
      duration: '4m', preAllocatedVUs: 10, maxVUs: 40,
    },
  },
  thresholds: {
    'saga_latency': ['p(95)<1000'],
    'saga_good_outcome': ['rate>=0.995'],
    'http_req_duration{scenario:reads}': ['p(95)<300'],
    'http_req_failed': ['rate<0.005'],
  },
  summaryTrendStats: ['avg', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

function uuid() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16);
  });
}

function login(username) {
  const res = http.post(`${BASE}/api/v1/auth/login`, JSON.stringify({ username, password: PASSWORD }),
    { headers: { 'Content-Type': 'application/json' } });
  if (res.status !== 200) throw new Error(`login ${username} -> ${res.status} ${res.body}`);
  return res.json('accessToken');
}

export function setup() {
  const admin = login('admin');
  const seller = login('seller');
  // A frame only for this run, with enough stock that no saga is refused for stock.
  const sku = `LOAD-${Date.now()}`;
  const res = http.post(`${BASE}/api/v1/frames`, JSON.stringify({
    sku, brand: 'LoadTest', model: 'K6', color: 'Black', material: 'Acetate', gender: 'Unisex',
    costCents: 10000000, salePriceCents: 20000000, stock: 100000, minStock: 0,
    location: 'Load test', supplier: 'k6',
  }), { headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${admin}`, 'Idempotency-Key': uuid() } });
  if (res.status !== 201) throw new Error(`create frame -> ${res.status} ${res.body}`);
  return { admin, seller, frameId: res.json('id'), sku };
}

export function reads(data) {
  const path = Math.random() < 0.5 ? '/api/v1/frames?limit=20' : '/api/v1/patients?limit=20';
  const res = http.get(`${BASE}${path}`, { headers: { Authorization: `Bearer ${data.admin}` }, tags: { name: path.split('?')[0] } });
  if (res.status === 429) { rateLimited.add(1, { scenario: 'reads' }); return; }
  check(res, { 'read 200': (r) => r.status === 200 });
}

export function orders(data) {
  const res = http.post(`${BASE}/api/v1/sagas/place-order`,
    JSON.stringify({ patientId: PATIENT, productType: 'FRAME', productId: data.frameId, quantity: 1 }),
    {
      headers: {
        'Content-Type': 'application/json', Authorization: `Bearer ${data.seller}`,
        'Idempotency-Key': uuid(), 'X-Correlation-Id': `k6-${uuid()}`,
      },
      tags: { name: 'place-order' },
    });
  if (res.status === 429) { rateLimited.add(1, { scenario: 'orders' }); return; }
  sagaLatency.add(res.timings.duration);
  const status = res.status === 201 || res.status === 200 ? res.json('status') : `HTTP ${res.status}`;
  sagaGood.add(status === 'COMPLETED' || status === 'COMPENSATED');
  check(res, { 'saga COMPLETED': () => status === 'COMPLETED' });
}

export function teardown(data) {
  console.log(`frame used by the run: ${data.sku} (${data.frameId}); one work order per completed saga was created in sales`);
}
