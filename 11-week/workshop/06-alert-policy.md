# CP7 · Alert policy for the place-order SLO

Rules: `06-alerts.yml`. SLO 1 from `05-slo.md`: 99.5 % of place-order sagas end
`completed` or `compensated` over 30 days. Budget = 0.5 % = **216 min** or **~4 bad
sagas** (910 sagas/month).

## 1. What each alert means with our numbers

| | Fast burn (`PlaceOrderFastBurn`) | Slow burn (`PlaceOrderSlowBurn`) |
|---|---|---|
| Burn rate | 14.4 | 6 |
| Error ratio threshold | 14.4 × 0.5 % = **7.2 %** of sagas | 6 × 0.5 % = **3 %** of sagas |
| Long window / confirmation window | 1 h / 5 min | 6 h / 30 min |
| Budget spent before it fires | 14.4 × 1 h ÷ 720 h = **2 %** = 4.3 min of the 216 min | 6 × 6 h ÷ 720 h = **5 %** = 10.8 min of the 216 min |
| If nobody acts, budget is gone in | 720 h ÷ 14.4 = **50 h** (~2 days) | 720 h ÷ 6 = **120 h** (5 days) |
| In sagas, at our traffic | Peak hour ~8 sagas: **1 bad saga** (12.5 %) is already over 7.2 % | 6 h of a Saturday ~40 sagas: **2 bad sagas** (5 %) are over 3 % |
| `severity` | `page` → the person on call that day | `ticket` → issue in the board, handled in working hours |
| `for` | 2 min | 15 min |

**Low-traffic guard.** With ~35 sagas a day the ratios jump a lot: 1 bad saga out of 2 at
8 a.m. is 50 %. That is why both rules also require a minimum number of sagas in the long
window (≥ 3 in 1 h, ≥ 10 in 6 h). Without it, a single timeout before the store opens would
page the team. The bad saga still counts in the budget; it just does not wake anybody.

**Why the short window.** The 5 min (fast) and 30 min (slow) windows make the alert stop
soon after the fix. Without them `PlaceOrderFastBurn` would keep firing for up to 1 h after
`opti-products-api` is back, because the 1 h ratio is still high.

## 2. Where the alerts go

Prometheus in `opti-infra` evaluates the rules (they are loaded with `rule_files` in
`observability/prometheus.yml`). The `runbook_url` of both alerts points to
`07-runbook.md`. `opti-infra` has no Alertmanager yet, so adding it (route
`severity=page` to the team chat, `severity=ticket` to a GitHub issue) is part of the
implementation plan.

## 3. Cause-based alerts we will NOT create

| Alert we do not create | Why not |
|---|---|
| **CPU of `opti-products-api` > 80 %** | High CPU does not mean a seller cannot sell. With one store and ~8 sagas an hour the containers are almost idle; if CPU ever matters, it shows up as latency or timeouts, and those already burn the SLO budget and fire the burn-rate alert. A CPU alert would page for something no user feels. |
| **Any HTTP 5xx in `opti-sales-api` or `opti-customers-api` (`> 0`)** | One 5xx is already absorbed by the saga: `opti-workflow` leaves the saga `RUNNING` and `resumeStuck()` retries it (ADR-004, `SagaService`). Paging on every single 5xx would page for errors the system already recovers from. What matters is how many sagas end badly, which is what the SLO alert measures. |

Other cause signals (Redis memory of `opti-workflow`, HikariCP pending connections,
container restarts) stay on the Grafana dashboard of CP4 as **diagnosis**, not as alerts:
we look at them after the SLO alert fires, following the runbook.

## 4. Validation

- YAML structure parsed without errors (groups → rules, 4 recording rules + 2 alerts).
- `promtool` check, with the same Prometheus image `opti-infra` uses:

```bash
docker run --rm -v "$PWD:/w" --entrypoint promtool prom/prometheus:v3.0.1 check rules /w/06-alerts.yml
```
