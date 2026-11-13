<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       15-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 15

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Juan Diego Jiménez Horta
- GITHUB_USER: ItzJunixs
- TEAM: The Illusionists
- SPRINT_GOAL: Complete the OptiView testing strategy for MVP 3 (integration in CI, load test of place-order against the week 11 SLOs and a chaos experiment on the saga), verify the MVP 3 operability checklist with evidence and write the retro toward the final presentation.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| W15-01 | Test pyramid of OptiView counted from the repos, gaps and stories TST-01..05 | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/blob/main/15-week/workshop/01-test-strategy.md |
| W15-02 | k6 load test of place-order + reads with the SLOs as thresholds | doing | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/blob/main/15-week/workshop/02-load-test.md |
| W15-03 | Chaos experiment: stop sales-api during place-order (hypotheses H1–H3 from the code) | doing | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/blob/main/15-week/workshop/03-chaos.md |
| W15-04 | MVP 3 operability checklist, promotion path and demo-under-stress script | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/blob/main/15-week/workshop/04-mvp3-release-checklist.md |
| W15-05 | Retro toward the final presentation | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/blob/main/15-week/workshop/05-retro.md |

## 2. My individual contribution
- Counted the real test suite: 160 Java unit tests, ~98 HTTP-slice tests, 64 `ContractChecks`, 59 portal tests, 1 Playwright e2e journey, and 30 integration tests against PostgreSQL/Redis that are skipped in CI (they need `TEST_DATABASE_URL` / `TEST_REDIS_PORT`); the week 14 `ci-api.yml` turns them on.
- Wrote `place-order-load.js` (k6): reads ramp 5→40 req/s and 2 sagas/s through the gateway, with thresholds taken from the week 11 SLOs (saga p95 < 1 s, ≥ 99.5 % good outcome) and NFR-001 (reads p95 < 300 ms); 429s of the gateway limiter (30 r/s per IP) are counted apart as the designed knee.
- Read `SagaOrchestrator` and `SagaInstance` before the chaos test: a TECHNICAL failure that reaches `SAGA_MAX_ATTEMPTS` ends the saga `FAILED` without running compensations, and `FrameService.reserve` already lowered the stock, so a long sales-api outage should leak one frame. `kill-sales-api.sh` tests a short (20 s) and a long (150 s) outage and checks stock, `RESERVED` rows and the other routes.
- Checked the MVP 3 release against the checklist: `v3.0.0` exists only in the 8 repos without changes, the 9 changed repos have `release/3.0.0 → main` open with CI red, and `GET /patients/{id}/formulas` still has no role check (week 13 #33).

## 3. Blockers and risks
- Load, chaos and rollback rehearsal need Docker with the full platform up; I prepared and statically checked them (shellcheck, `node --check`) but the runs and their outputs are still to be recorded.
- MVP 3 cannot be tagged as production-grade while Actions is down and #33 is open.
- If H2 is confirmed, every long participant outage leaves stock reserved with no order (CHAOS-01).

## 4. Plan for next week
- Run `load/run-load.sh`, `chaos/kill-sales-api.sh both` and the week 12 rehearsal; fill the result tables.
- Bring CI-00/CI-01, #33 and CHAOS-01 to the team before tagging `v3.0.0`.
- Prepare the week 16 presentation with the demo-under-stress script of `04-mvp3-release-checklist.md`.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [x] Testable acceptance criteria
- [x] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

## 6. Evidence links
- Workshop folder: https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/tree/main/15-week/workshop
- Load test: https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/tree/main/15-week/workshop/load
- Chaos test: https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/tree/main/15-week/workshop/chaos
- Open release PR: https://github.com/code-corhuila/opti-customers-api/pull/9
- e2e journey: `opti-e2e/tests/demo.spec.ts`
