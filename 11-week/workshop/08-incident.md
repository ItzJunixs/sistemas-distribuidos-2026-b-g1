# CP9 · Incident drill and blameless postmortem

## 1. Scenario of the drill

The downstream dependency of hop 4 of CP1 (`opti-workflow` → `opti-products-api`,
`POST /api/v1/frames/{frameId}/reservations`, step `reserve-stock`) answers in **3 s for
25 % of the requests during 40 minutes**.

Assumed cause for the drill: on a Saturday the inventory administrator uploads new
frame photos (HU-16). The photo update locks the `products.frame` row and keeps the
lock while the file is written to the `frame-images` volume (~3 s). A reservation for
the same frame does `SELECT ... FROM frame WHERE id = :id FOR UPDATE`
(`JdbcFrameRepository`) and waits for that lock. It does not fail:
`statement_timeout = 5000` in products-api and `HTTP_TIMEOUT=PT5S` in opti-workflow are
both bigger than 3 s.

Traffic: Saturday peak, ~12 sagas/hour (above the usual ~8 because of the weekend), so
**8 sagas** in the 40 minutes, **2 of them slow** (25 %).

## 2. Trace of one slow request (span tree with durations)

`trace_id = 9c4e2a7b1f3d4e8ab2c6d0f9e1a37b54`

- `POST /api/v1/sagas/place-order` — opti-api-gateway — **3,182 ms**
  - `saga place-order` — opti-workflow — 3,161 ms
    - `step check-patient` — 44 ms
      - `GET /api/v1/patients/{patientId}` client → customers-api server — 39 ms → 31 ms
        - `SELECT customers.patient` — 6 ms
    - `step reserve-stock` — **3,047 ms** ← the slow hop
      - `POST /api/v1/frames/{frameId}/reservations` client → products-api server — 3,041 ms → 3,028 ms
        - `SELECT products.frame ... FOR UPDATE` — **2,991 ms** (waiting for the row lock)
        - `INSERT products.stock_reservation` — 9 ms
    - `step open-order` — 58 ms
      - `POST /api/v1/work-orders` client → sales-api server — 54 ms → 47 ms
        - `INSERT sales.work_order` + `sales.work_order_item` — 12 ms

The saga is correct (`COMPLETED`, work order `OT-0217`), just slow: 2,991 of the 3,182 ms
are one lock wait in `products`.

## 3. Four log lines of that trace (CP2 format)

```json
{"timestamp":"2026-10-24T15:12:04.310Z","level":"INFO","service":"workflow-api","version":"1.0.0","environment":"prod","trace_id":"9c4e2a7b1f3d4e8ab2c6d0f9e1a37b54","span_id":"1d8f3a6c0b5e2947","correlation_id":"place-order-91aa","event":"saga.started","message":"place-order saga started","saga_id":"e4b1c7d2-5a9f-4c3e-8b6d-2f0a1e9c7b35","seller_id":"22222222-2222-4222-8222-222222222222","product_type":"FRAME","product_id":"aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa","attempt":1}
{"timestamp":"2026-10-24T15:12:07.379Z","level":"WARN","service":"products-api","version":"1.0.0","environment":"prod","trace_id":"9c4e2a7b1f3d4e8ab2c6d0f9e1a37b54","span_id":"6a2e9d4f1c7b3058","correlation_id":"place-order-91aa","event":"stock.reservation.slow","message":"reservation waited 2991 ms for the frame row lock","product_type":"FRAME","product_id":"aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa","duration_ms":3028}
{"timestamp":"2026-10-24T15:12:07.384Z","level":"INFO","service":"workflow-api","version":"1.0.0","environment":"prod","trace_id":"9c4e2a7b1f3d4e8ab2c6d0f9e1a37b54","span_id":"3b7c0e5a9d2f4186","correlation_id":"place-order-91aa","event":"saga.step.completed","message":"step reserve-stock completed","saga_id":"e4b1c7d2-5a9f-4c3e-8b6d-2f0a1e9c7b35","saga_step":"reserve-stock","duration_ms":3047}
{"timestamp":"2026-10-24T15:12:07.492Z","level":"INFO","service":"workflow-api","version":"1.0.0","environment":"prod","trace_id":"9c4e2a7b1f3d4e8ab2c6d0f9e1a37b54","span_id":"1d8f3a6c0b5e2947","correlation_id":"place-order-91aa","event":"http.request","message":"POST /api/v1/sagas/place-order -> 201","http_method":"POST","http_route":"/api/v1/sagas/place-order","http_status":201,"duration_ms":3182}
```

## 4. What the SLIs show

| SLI (`05-slo.md`) | During the 40 min | 30-day value after the incident | Target |
|---|---|---|---|
| SLO 1 Availability | 8 / 8 = **100 %** (no saga `RUNNING` or `FAILED`, 3 s < 5 s timeout) | unchanged | 99.5 % ✅ |
| SLO 2 Latency ≤ 1 s | 6 / 8 = **75 %** | 2 more slow sagas of the 45 allowed → **4.4 % of the latency budget** | 95 % ⚠️ in the window, ✅ in 30 days |
| SLO 3 Compensation | no compensation needed | unchanged | 99 % ✅ |

## 5. Which alert fires first, and when

**None.** `PlaceOrderFastBurn` and `PlaceOrderSlowBurn` (CP7) watch only SLO 1, and SLO 1
stays at 100 %: the requests are slow, not failed. We also have no latency alert.

The first signal is a person: at **minute 12** a seller tells the store administrator that
"the system is frozen" after waiting 3 s twice, and tries the sale again (the
`Idempotency-Key` returns the same saga, no double sale). The administrator writes to the
team; the on-call opens the Grafana panel of p95 of
`workflow_place_order_duration_seconds` (CP4) and sees it in the `5` bucket.

## 6. Which runbook step stops the bleeding

- Runbook **2.c** (latency per hop) points to `products-api` POST at p95 ≈ 3 s.
- Runbook **2.d** (`grep` by `trace_id`) shows `stock.reservation.slow`: lock wait on the frame row.
- Runbook **3, mitigation 1** (`docker compose restart products-api`) at **minute 31** ends
  the open photo transactions and releases the locks. The on-call also asks the
  administrator to stop the photo upload until it is fixed. Latency is back to normal at
  minute 33. The scenario window ends at minute 40.

## 7. Blameless postmortem

**Title:** Slow sales for 33 minutes while frame photos were uploaded
**Severity:** P2 (sales worked, slower; workaround: wait). **Status:** resolved.

### Timeline (Saturday, minute 0 = 10:00 local)

| Minute | What happened |
|---|---|
| 0 | Inventory administrator starts uploading new frame photos from `opti-products-portal` |
| 4 | First slow saga (3.2 s). No alert: SLO 1 not affected |
| 12 | Seller reports "frozen system" to the administrator, who writes in the team chat |
| 15 | On-call acknowledges, opens Grafana: p95 of place-order in the 5 s bucket |
| 21 | Runbook 2.c: `products-api` POST reservations is the slow hop |
| 26 | Runbook 2.d: `trace_id` of a slow saga shows `stock.reservation.slow`, lock on `products.frame` |
| 31 | Mitigation 1: restart of `products-api`; administrator pauses the photo upload |
| 33 | p95 back under 0.5 s; one test sale ends `COMPLETED` in 180 ms |
| 40 | End of the scenario window. Incident closed |

### Impact

- Requests: 8 place-order sagas in the window, **2 slow** (3.2 s and 3.1 s), **0 failed**, 0 double sales.
- Budget: availability budget **0 of 216 min**; latency budget **2 of 45 slow sagas (4.4 %)**.
- 33 minutes of degraded experience at the counter in the busiest morning of the week.

### Root cause

The photo update of a frame (HU-16) keeps the `products.frame` row locked while it writes
the image file, and a reservation of that frame needs `FOR UPDATE` on the same row, so it
waits for the file write.

### Contributing factors

- Our SLO alerts only cover availability; a slow-but-correct saga never alerts.
- `statement_timeout = 5000` and `HTTP_TIMEOUT=PT5S` are both larger than the lock wait,
  so nothing fails fast.
- There is no per-step latency metric, so the dashboard shows the saga is slow but not
  which step until someone reads a trace.
- Catalog maintenance and sales happen at the same time on Saturday mornings.

### Action items

**AI-1 — Latency burn-rate alert**
*As the on-call of The Illusionists, I want an alert on SLO 2 (latency) so that slow sagas
reach us before a seller has to call.*
- Given 25 % of sagas take > 1 s for 1 hour with ≥ 3 sagas, when Prometheus evaluates
  `06-alerts.yml`, then `PlaceOrderLatencyBurn` fires with `severity=ticket`.
- `promtool check rules 06-alerts.yml` passes and the rule has `runbook_url`.

**AI-2 — Do not hold the frame lock while writing the photo**
*As a seller, I want reservations to stay fast while the inventory administrator uploads
photos, so that sales at the counter are not delayed.*
- The image file is written to `frame-images` **before** the transaction; the transaction
  only runs `UPDATE frame SET image_url`.
- Integration test in `opti-products-api`: while a photo update is in progress for frame X,
  `POST /api/v1/frames/X/reservations` answers in < 300 ms (NFR-001 p95).

**AI-3 — Fail fast on lock waits and measure each step**
*As the Tech Lead, I want lock waits to fail fast and each saga step measured, so that a
blocked dependency shows up in seconds and in the right step.*
- `connection-init-sql` of products-api adds `SET lock_timeout = 1000`; a test with the row
  locked gets a refusal in ≤ 1.2 s and the saga ends `RUNNING` for resume instead of waiting.
- `opti-workflow` exposes `workflow_saga_step_duration_seconds{step}`; with `reserve-stock`
  delayed 3 s in a test, its p95 is in the `5` bucket and the other steps are not.
