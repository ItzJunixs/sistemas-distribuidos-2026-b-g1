# 5 · Retrospective toward the final presentation

Individual retro of Corte 3 (weeks 11–15), written as input for the team retro and week 16.

## What made the system reliable

- **Hexagonal services with in-memory adapters**: ~220 unit/HTTP tests that run in seconds and
  a shared `ContractChecks` (64 tests) that keeps the error envelope, correlation id,
  idempotency and validation identical in the four APIs.
- **Idempotency keys everywhere + an orchestrated saga** (ADR-004): retries are safe, a refusal
  is compensated, a failure is never silent (`FAILED` is visible).
- **Database per service with reversible migrations** proven by CI on every PR (`verify-rebuild.sh`).
- **One entry point**: the gateway isolates routes, rate-limits and answers 503 with the envelope
  when a service is down, so one failure does not take the front down.
- **Everything as code**: `opti-infra` composes the 16 repos; a new laptop gets the platform with
  four commands.

## What we would do differently

| What happened | Effect | Next time |
|---|---|---|
| MVP 3 merged with CI red because Actions was not running jobs | ~9 repos shipped to qa without a single build or test in CI | Required checks with no bypass; fix the pipeline first (week 14) |
| Work merged in big batches (130 PRs in 5 days; MVP 3 as one branch per repo) | Hard reviews, hard to know which change broke what | One PR per HU, merged when done (DORA, week 14) |
| Integration tests written but skipped in CI | The class of bugs mocks hide was never checked automatically | Postgres/Redis in CI from the day the test is written |
| Images rebuilt per environment, only `:latest` | No exact artifact to promote or roll back to | Build once with `sha-*`, promote by retag (week 12) |
| Docs (`deployment.md`, `environments.md`, DoD) describe Kubernetes, `ms-*`, Checkstyle | Reviewers judge against a system we do not have | Docs updated in the same PR as the change (DoD item made a reviewer check) |
| Observability designed (SLOs, alerts, runbook) but services do not export metrics | The alert of week 11 cannot fire in reality | Micrometer OTLP export in one API first |
| Week 13 P0 (#33) still open at release time | A patient-data read without role check | Security stories as P0 gates of the release checklist |

## What the metrics show

- DORA (week 14): 1 release to `main` in 6 weeks, ~21 % fix PRs, time to restore not measured.
- SLOs (week 11): defined, not yet measured live; the load test of this week gives the first
  real latency numbers for place-order.

## For week 16 (final presentation)

Tell the story as "MVP 1 runs → MVP 2 integrates → MVP 3 operates", and be honest about the
last step: show the gates and plans we built, the evidence of what works (contracts, saga,
rebuild, smoke, gateway isolation) and the three open items with their stories (CI-00/CI-01,
#33, CHAOS-01).
