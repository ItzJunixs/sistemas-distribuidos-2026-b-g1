# CP8 · Runbook — `PlaceOrderFastBurn`

| | |
|---|---|
| Alert | `PlaceOrderFastBurn` (`06-alerts.yml`), `severity=page`, `team=the-illusionists` |
| Means | More than 7.2 % of place-order sagas in the last hour (and the last 5 min) ended `RUNNING` or `FAILED`. At this speed the 216-min monthly budget is gone in ~2 days |
| Priority | **P1** of `opti-docs/13-operations/incident-management.md`: sellers cannot close sales (core flow), response in 15 min |
| Services | `opti-workflow` (orchestrator) → `opti-customers-api`, `opti-products-api`, `opti-sales-api`; Redis of the saga state |

## 1. Symptoms

- Sellers in `opti-sales-portal` get the saga back with status `RUNNING` (HTTP `202`) and no
  work order, or the screen keeps "processing" for ~5 s (`HTTP_TIMEOUT=PT5S`).
- The same sale tried twice returns the same saga (the `Idempotency-Key` works), so the
  seller thinks the button is broken.
- Grafana: `workflow_place_order_requests_total{outcome="running"}` grows; p95 of
  `workflow_place_order_duration_seconds` jumps to the `5` bucket.

## 2. Confirm (5 min)

**a. Is it real or a few sagas at low traffic?**

```promql
sum by (outcome) (increase(workflow_place_order_requests_total[1h]))
```

If the hour has fewer than ~5 sagas, look at each one by its `trace_id` before waking
anybody else.

**b. Which step fails?**

```promql
sum by (step, reason) (increase(workflow_saga_step_failures_total[15m]))
```

`reserve-stock` + `PARTICIPANT_UNAVAILABLE` → look at `opti-products-api`.
`open-order` → `opti-sales-api`. `check-patient` → `opti-customers-api`.

**c. Which hop is slow (per service latency, CP4)?**

```promql
histogram_quantile(0.95, sum by (le, service_name) (rate(http_server_requests_seconds_bucket{method="POST"}[5m])))
```

**d. Read one bad saga end to end with its `trace_id`.** Take the `traceparent` of a bad
response (or `saga.step.timeout` lines) and filter the JSON logs of every container:

```bash
docker compose logs --since 30m workflow products-api sales-api customers-api \
  | grep '"trace_id":"7f3c9a1e5b2d48c6a0e4f9b3d1c27e58"'
```

The lines tell which step, which `failure_reason`, how many ms (`duration_ms`) and if the
saga was resumed (`saga.resumed`, `attempt`).

**e. Health of the suspect:** `curl -s http://products-api:8080/health` from the `platform`
network, `docker compose ps`, and `docker compose logs --since 15m products-api | grep '"level":"ERROR"'`.

## 3. First mitigations (lowest risk first)

| # | Mitigation | When | How | Risk |
|---|---|---|---|---|
| 1 | **Restart / scale out the slow participant** | One API answers slow or not at all, no recent deploy | `docker compose restart products-api`; if it is load, `docker compose up -d --scale products-api=2` (same `platform` network, Docker DNS spreads the calls) | Low: stateless API, the stock is in `products` DB |
| 2 | **Roll back the last deploy** | The problem started right after a deploy of that API | Images are only published as `:latest` (`publish.yml`), so: `git checkout <previous tag/commit>` of that repo, `docker compose build products-api && docker compose up -d products-api` | Medium: needs the previous commit to be known; DB migrations must be backward compatible |
| 3 | **Stop new sagas at the gateway (manual sales)** | Nothing above works in 30 min and stock is getting reserved without orders | In `opti-api-gateway` `workflow.conf.template` return `503` for `POST /api/v1/sagas/place-order`, reload nginx; sellers write the sale on paper and register it later | High: no online sales, everything entered twice |

Not available today: **circuit breaker** (`05-architecture/overview.md`: "Circuit Breaker:
No") and **feature flags**. Both are action items in the postmortem (CP9).

After any mitigation, `opti-workflow` resumes the sagas left `RUNNING` by itself every
30 s (`SAGA_RESUME_EVERY=PT30S`), so no manual retry is needed for them.

## 4. Escalation

| Time from page | Role (`incident-management.md`) | Who in our team |
|---|---|---|
| 0 min | **On-Call Engineer**: confirms (step 2), applies mitigation 1 | Team member on duty that day |
| 15 min (P1) | **Incident Commander**: decides mitigation 2 or 3, keeps the timeline | Scrum Master of the sprint |
| 15 min | **Tech Lead**: diagnosis in the code (saga steps, participant) | Owner of the failing repo (`opti-products-api`, `opti-sales-api`, ...) |
| 30 min | **Comms Lead**: tells the store (sellers, administrator) to use manual sales | Team member closest to the store |
| If mitigation 3 is needed | Course instructor (Product Owner / stakeholder, `agile-conventions.md`) | Informed by the IC |

## 5. Verify recovery

1. The error ratio of the last 5 min is back under the threshold and the alert resolves by
   itself (the 5-min window makes it stop soon):
   ```promql
   optiview:place_order_error_ratio:rate5m < (14.4 * 0.005)
   ```
2. No saga is left in progress:
   ```promql
   max(workflow_sagas_in_progress) == 0
   ```
3. A seller makes one real sale from `opti-sales-portal` and it ends `COMPLETED` with a
   work order (`OT-xxxx`) in `sales.work_order`.
4. No frame left reserved without an order: the sagas that ended `FAILED`
   (`COMPENSATION_FAILED`) are listed with `GET /api/v1/sagas/{id}` and their reservations
   released by hand with `POST /api/v1/reservations/{reservationId}/release`.
5. Write down the minutes of budget spent (SLO 1 query of `05-slo.md`) and open the
   postmortem if more than 25 % was used.
