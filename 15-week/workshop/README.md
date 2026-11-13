# Week 15 Workshop — Testing at scale and the MVP 3 release on OptiView

Individual work for week 15 applied to the OptiView team project. It covers the two activities
of the guides:

- Session 1: complete the test suite — integration with real dependencies, contract tests in CI,
  e2e money-path tests, a load test to the SLO and one chaos experiment.
- Session 2: ship MVP 3 — promote and tag `v3.0.0`, verify the operability checklist, demo
  under stress, retrospect.

<!-- CONFIG-START -->
- FULL_NAME: Juan Diego Jiménez Horta
- GITHUB_USER: ItzJunixs
- TEAM: The Illusionists (OptiView)
- DOCS_REPO: https://github.com/code-corhuila/opti-docs
- JOURNEY_UNDER_TEST: place-order saga (opti-workflow → customers / products / sales), as in week 11
- SLO_SOURCE: 11-week/workshop/05-slo.md
- EVIDENCE_DATE: 2026-11-12
<!-- CONFIG-END -->

## Files

| # | File | What it answers | Status |
|---|---|---|---|
| 1 | `01-test-strategy.md` | Our real test pyramid (counted), what runs in CI and the gaps | done |
| 2 | `02-load-test.md` + `load/` | k6 load test of place-order and reads against the SLOs | script done, run pending |
| 3 | `03-chaos.md` + `chaos/` | Kill sales-api during place-order: hypotheses from the code, script | script done, run pending |
| 4 | `04-mvp3-release-checklist.md` | The MVP 3 operability checklist with evidence, promotion path, demo script | done |
| 5 | `05-retro.md` | What made it reliable, what we would change, input for week 16 | done |
| 6 | `HOW-TO-RUN.md` | Order to run the load and chaos tests and where the outputs go | done |

## Main findings

1. The pyramid base is strong (160 Java unit tests, ~98 HTTP-slice tests, 64 contract checks,
   59 portal tests) but the 30 integration tests against real PostgreSQL/Redis are skipped in CI.
2. One e2e journey (`opti-e2e`, Playwright) covers the money path and can inject a failure; it is
   not part of any pipeline.
3. Reading `opti-workflow` before the chaos test: when a participant is down longer than
   3 attempts × 30 s, the saga ends `FAILED` **without compensating**, so the frame reserved in
   `reserve-stock` stays reserved. The experiment is designed to confirm it (H2).
4. MVP 3 is not releasable yet as "production-grade": CI not running, `release/3.0.0` PRs open
   with red checks, `v3.0.0` missing in the 9 changed repos, and the week 13 P0 (#33) still open.

## Submission links

- Fork: https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1
- Workshop: https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/tree/main/15-week/workshop
