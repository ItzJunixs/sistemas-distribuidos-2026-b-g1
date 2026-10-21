<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       11-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 11

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Juan Diego Jiménez Horta
- GITHUB_USER: ItzJunixs
- TEAM: The illusionists
- SPRINT_GOAL: Make the place-order journey of OptiView observable end to end (trace map, log contract, instrumentation with a test and a metric catalog) as delivery 1 of the week 11 workshop.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| W11-CP0 | Workshop setup and CONFIG block | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/commit/9e1ef59fed7490d2a34520ea1aa731cb904e6604 |
| W11-CP1 | Critical journey "Place order" and trace map | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/commit/e1e7fe216f336f0681c10660ec55b8bf8b7343f7 |
| W11-CP2 | Structured log contract | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/commit/3b73dd22972d5c80cb2e412b411caa7746de8804 |
| W11-CP3 | Trace-context middleware and structured logger (push 1) | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/commit/238bc5ded7e00e2a457a3a3fbdc4101674ba04b1 |
| W11-CP3 | Context propagation to opti-products-api, metrics and test (push 2) | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/commit/cb610fb77f651f4d9a2a91b09b395287f130e4d5 |
| W11-CP4 | Golden signals and metric catalog | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/commit/195ec1b6d3a2db62ea7adc23085047719e5a5dd6 |

## 2. My individual contribution
- Picked the place-order journey (ADR-004 saga): `opti-sales-portal` → `opti-api-gateway` → `opti-workflow` → `opti-customers-api` / `opti-products-api` / `opti-sales-api`, with the real endpoints and tables (`products.stock_reservation`, `sales.work_order`) and a Mermaid diagram showing `traceparent` next to the existing `X-Correlation-Id`.
- Wrote the log contract on top of the `LogstashEncoder` config the APIs already have: mandatory fields, saga fields (`saga_id`, `saga_step`, `failure_reason`), a never-log list for patient data and seven sample lines with one `trace_id`.
- Built a small Java module for `opti-workflow` (JDK only): W3C trace-context filter, JSON logger, propagation to `POST /api/v1/frames/{id}/reservations`, two metrics and a test that checks the `trace_id` sent downstream is the one received. The test passes on my laptop (output in `HOW-TO-RUN.md`).
- Checked the metric catalog against the repos: the APIs have no actuator/Micrometer yet, `opti-infra` already runs otel-collector + Prometheus + Grafana, so the catalog plans OTLP export to that collector.

## 3. Blockers and risks
- Delivery 1 was pushed late (2026-10-21, after the 2026-10-20 20:30 deadline): I could not reach my PC during the class. The CP0 commit was created on 2026-10-20 19:21 and pushed on 2026-10-21.
- No Spring Boot in the CP3 module because Maven Central was not reachable from where I built it; moving it into `opti-workflow` means rewriting the filter as a `OncePerRequestFilter`.
- None of the team services send metrics or traces to the collector yet.

## 4. Plan for next week
- Delivery 2 of the workshop (CP6 to CP10): SLOs and error budget, burn-rate alerts, runbook, incident drill and postmortem.
- Propose to the team adding `traceparent` to `CorrelationFilter` and Micrometer OTLP export in one API first.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [ ] Testable acceptance criteria
- [x] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

## 6. Evidence links
- https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/tree/main/11-week/workshop
- https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/blob/main/11-week/workshop/01-journey.md
- https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/blob/main/11-week/workshop/02-log-contract.md
- https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/tree/main/11-week/workshop/03-instrumentation
- https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/blob/main/11-week/workshop/04-metrics.md
- https://github.com/code-corhuila/opti-docs/blob/main/05-architecture/decisions/records/ADR-004-place-order-saga.md
- https://github.com/code-corhuila/opti-infra (observability/otel-collector.yaml, observability/prometheus.yml)
