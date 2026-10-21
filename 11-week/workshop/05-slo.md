# CP6 · SLIs, SLOs and error budget

Journey: **Place order** (`POST /api/v1/sagas/place-order` on `opti-workflow`, CP1).
Metrics: `workflow_place_order_requests_total{outcome}` and
`workflow_place_order_duration_seconds` from CP3/CP4.

Inputs from `opti-docs`:
- `04-requirements/non-functional.md` NFR-001: p95 < 300 ms for the critical endpoints
  (`POST /work-orders/{id}/approve`, `GET /patients/{id}/formulas/current`).
- NFR-002: production availability 99.9 %, 44 min/month, and the policy "freeze feature
  deploys if > 50 % of the budget is used in the first half of the month".
- `01-context/scope.md`: one optical store, no multi-branch.

## 1. Traffic estimate for the journey

| Fact | Value | Why |
|---|---|---|
| Opening hours | 9 h/day, Monday to Saturday (~26 days/month) | Normal hours of a single optical store; Sunday is the NFR-002 maintenance window |
| Place-order sagas per day | ~35 | One store, 2 sellers. Each sale starts one saga, and quotations that the patient does not take also start one (the saga opens a `QUOTATION`) |
| Sagas per month | 35 × 26 ≈ **910** | |
| Peak | ~8 per hour (Saturday morning) | Still far below the 50 RPS of NFR-001, so load is not the risk; a participant being down is |

## 2. SLOs

Window for all three: **30 days rolling**. Prometheus is v3 (`opti-infra`), so the bucket
label is normalized as `le="1.0"`.

### SLO 1 · Availability: 99.5 %

- **SLI** = place-order sagas that end `completed` or `compensated` ÷ all place-order sagas.
  `compensated` is a correct answer (no stock, inactive patient). Bad = `running` (the
  seller got `202` and no order because a participant did not answer) and `failed`.

```promql
sum(increase(workflow_place_order_requests_total{outcome=~"completed|compensated"}[30d]))
/
sum(increase(workflow_place_order_requests_total[30d]))
```

### SLO 2 · Latency: 95 % of sagas answer in under 1 s

- **SLI** = sagas answered in ≤ 1 s ÷ all sagas. 1 s because the saga is 3 hops in a row
  (`check-patient`, `reserve-stock`, `open-order`) and NFR-001 gives each critical hop
  300 ms at p95; 3 × 300 ms ≈ 1 s is what the seller feels at the counter.

```promql
sum(increase(workflow_place_order_duration_seconds_bucket{le="1.0"}[30d]))
/
sum(increase(workflow_place_order_duration_seconds_count[30d]))
```

### SLO 3 · No frame left reserved: 99 % of failed sagas compensate correctly

- **SLI** = sagas `compensated` ÷ sagas that needed compensation (`compensated` + `failed`).
  `failed` means `COMPENSATION_FAILED`: a frame stays in `products.stock_reservation` with
  no work order, so the store shows less stock than it really has (ADR-004 risk
  "A compensation is missing or wrong, leaving an item reserved").

```promql
sum(increase(workflow_place_order_requests_total{outcome="compensated"}[30d]))
/
sum(increase(workflow_place_order_requests_total{outcome=~"compensated|failed"}[30d]))
```

## 3. Why 99.5 % and not the 99.9 % of NFR-002

NFR-002 is uptime of the platform. A request-based SLO for one journey with ~910 events a
month cannot be 99.9 %: the budget would be 0.9 requests, so **one** failed sale would
break the SLO of the month. 99.5 % gives room for 4 bad sagas, which is still strict for a
store that sells ~35 per day. We keep 99.9 % as the uptime target of `opti-infra` and
propose to the team to add this journey SLO to NFR-002.

## 4. Error budget (SLO 1)

| | Calculation | Budget |
|---|---|---|
| Requests | 910 × (1 − 0.995) = 4.55 | **4 failed sagas / 30 days** |
| Minutes of full outage | 30 × 24 × 60 = 43,200 min × 0.005 | **216 min / 30 days** |
| Minutes in opening hours only | 26 × 9 × 60 = 14,040 min × 0.005 | 70 min of real selling time |

The 216 min is the calendar number the alerts use (CP7). The 70 min is the honest one:
an outage on a Sunday at 3 a.m. costs no sale, one on Saturday at 10 a.m. does.

Latency budget (SLO 2): 5 % of 910 = **45 sagas** per month may take more than 1 s.

## 5. Error budget policy (team: The Illusionists)

| Budget used | What the team does |
|---|---|
| **50 %** (2 bad sagas, ~108 min) | Warning in the team channel. The Tech Lead reviews the traces of the bad sagas by `trace_id` (CP2). If it happens in the first 15 days, NFR-002 applies: feature deploys to `opti-workflow`, `opti-products-api` and `opti-sales-api` are frozen |
| **75 %** (3 bad sagas, ~162 min) | Freeze for all the place-order participants. Only fixes, each with a test of the failure path (like `SagaServiceTest`). One reliability HU goes to the top of the next sprint |
| **100 %** (4+ bad sagas, 216 min) | Full freeze until the 30-day window recovers. Blameless postmortem (like CP9) is mandatory, its action items enter the sprint before any new feature. The PO is told the SLO was missed |
