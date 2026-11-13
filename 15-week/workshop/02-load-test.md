# 2 · Load test to the SLO (k6)

Script: `load/place-order-load.js` · runner: `load/run-load.sh` (k6 0.54 in Docker, on the
`platform` network, so the traffic enters through `opti-api-gateway` like a browser's).

## 2.1 What we measure and against what

| Flow | Load | SLO / threshold | Source |
|---|---|---|---|
| `POST /api/v1/sagas/place-order` (3 hops: customers → products → sales) | 2 sagas/s for 4 min (≈ 480 sagas) | 95 % < 1 s · ≥ 99.5 % `COMPLETED`/`COMPENSATED` | week 11 `05-slo.md`, SLO 1 and 2 |
| `GET /api/v1/frames`, `GET /api/v1/patients` | 5 → 10 → 20 → 40 req/s, 1 min each | p95 < 300 ms | NFR-001 (`04-requirements/non-functional.md`) |
| Errors | all | `http_req_failed` < 0.5 % (429 excluded, counted in `rate_limited`) | — |

Why these numbers: real traffic is ~35 sagas a day with a peak of ~8 per hour (week 11). 2
sagas/s is **~900× the real peak**, so passing it says the saga is not the bottleneck for one
store. The read ramp crosses the gateway limit on purpose
(`limit_req_zone … rate=30r/s`, `burst=60` per client IP in `nginx/conf.d/10-security.conf.template`):
from one IP, above ~30 req/s the gateway must answer `429`, and the services behind it must not
notice. That is the "knee" by design; the knee of the services themselves is beyond it and would
need several client IPs to reach.

Setup of the run: logs in as `admin` and `seller` (demo users), creates a frame `LOAD-<ts>` with
stock 100 000 so no saga is refused for stock, and uses the seeded active patient
`11111111-…`. Each completed saga creates a real work order in sales: run it in **develop only**.

## 2.2 How to run

```bash
cd ~/OneDrive/Documentos/sistemas-distribuidos-2026-b-g1/15-week/workshop/load
./run-load.sh            # ~4 min; writes load-output.txt and load-summary.json
```

While it runs, the bottleneck is read from the traces/logs: every request has
`X-Correlation-Id: k6-…`, so `docker compose logs workflow | grep k6-` shows the duration of each
saga step (week 11 log contract).

## 2.3 Result

| Metric | Threshold | Measured | Pass |
|---|---|---|---|
| saga_latency p95 | < 1000 ms | _pending run_ | |
| saga_good_outcome | ≥ 99.5 % | _pending run_ | |
| reads p95 | < 300 ms | _pending run_ | |
| http_req_failed | < 0.5 % | _pending run_ | |
| rate_limited (429) | expected > 0 only in the 40 req/s stage | _pending run_ | |

Interpretation and the bottleneck found go here after the run (`load-output.txt`).
