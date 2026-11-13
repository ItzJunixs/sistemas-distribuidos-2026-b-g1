# 4 · MVP 3 release — operability checklist (Session 2)

State of OptiView against the guide's checklist on **2026-11-12**, three days before the
`v3.0.0` date written in `opti-docs/CHANGELOG.md` (2026-11-15). Evidence is linked to the week
where it was produced. ✅ done · 🟡 partial · ❌ missing.

| # | Checklist item | State | Evidence / what is missing |
|---|---|---|---|
| 1 | All acceptance criteria met; CI green (lint, types, tests, coverage, security) | ❌ | CI has not executed since 2026-11-06 (0-step jobs); no lint, coverage or security gates exist (week 14 `01`, `02`) |
| 2 | Deploys with zero downtime; a rehearsed rollback works | 🟡 | strategy, scripts and runbook ready (week 12); rehearsal pending (`rehearse-rollback.sh`) |
| 3 | Observability: golden-signal dashboards, a real alert with a runbook, distributed tracing | 🟡 | otel-collector + Prometheus + Grafana in `opti-infra`; SLOs, burn-rate alerts, runbook and drill written (week 11); runbooks for gateway, auth and sales in `opti-docs/09-microservices/services/*/runbook.md`. **The services do not export metrics/traces yet**, so no dashboard has live data |
| 4 | Security: authn/authz per service; no secrets in git; scans pass | 🟡 | JWT RS256 validated in every API; STRIDE for 5 boundaries merged in opti-docs (PRs #51–#60); `.env`/keys git-ignored. **Week 13 P0 #33 is still open**: on `develop`, `GET /api/v1/patients/{id}/formulas` in `PatientController` has no `requireRole` (only the new `/formulas/{formulaId}/pdf` has one), so `PatientFormulasAuthorizationTest` would still be red. No secret/dependency scan in CI |
| 5 | Resilience proven: load test meets the SLO; chaos shows graceful degradation | 🟡 | k6 script and chaos experiment ready (`02`, `03`); results pending. Known from code reading: long participant outage leaves stock reserved (H2) |
| 6 | Cross-service consistency under failure (outbox / idempotency) | 🟡 | idempotency keys on every create + saga with compensation (ADR-004) ✅; ADR-005 "outbox" is a polling job, not an outbox (week 10 finding, still open) |
| 7 | Versioned `v3.0.0`; CHANGELOG + ADRs + runbooks updated | 🟡 | `opti-docs` has CHANGELOG 3.0.0 and tag `v3.0.0`. Code repos: `v3.0.0` exists only in the 8 repos **without** MVP 3 changes (auth-api, auth-db, auth-portal, customers-db, products-db, worker, workflow, infra). The 9 repos **with** MVP 3 changes (gateway, front, customers-api/portal, products-api/portal, sales-api/db/portal) have no `v3.0.0`, and their `release/3.0.0 → main` PRs are open with CI red |

## Promotion path still to do (in order)

1. Fix Actions in the org (CI-00) and re-run CI on the open `release/3.0.0` PRs.
   Add `requireRole` to the three patient GET endpoints (#33) before tagging.
2. Merge `release/3.0.0 → main` only with green checks (CI-01).
3. Tag `v3.0.0` on `main` in the 9 changed repos, and publish the images (`publish.yml` of week 12:
   retag of the `sha-*` image that was tested in qa).
4. `release-smoke.yml` with `ref=v3.0.0` (week 14) → green.
5. Deploy with `compose.release.yml` (`OPTI_VERSION=v3.0.0`); rollback = `OPTI_VERSION=v2.0.0`.

## Demo under stress (script for the presentation)

1. Open the front, drive the `opti-e2e` journey (`npm run demo:headed`).
2. In a second terminal: `load/run-load.sh` → show the k6 live output; reads stay < 300 ms, the
   saga < 1 s, and the gateway answers 429 at 40 req/s instead of letting the services fall.
3. `chaos/kill-sales-api.sh short` → Órdenes shows the controlled error, Pacientes and Inventario
   keep working; when sales-api is back the saga that was waiting completes.
4. Show the rollback: `deploy/blue-green.sh deploy` then `rollback` with the client poller (week 12).
5. Point to: ADR-003/004, the STRIDE PRs, the CHANGELOG, the runbooks and this checklist.
