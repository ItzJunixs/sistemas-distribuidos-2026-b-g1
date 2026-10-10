<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       10-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 10

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Juan Diego Jiménez Horta
- GITHUB_USER: ItzJunixs
- TEAM: The illusionists
- SPRINT_GOAL: Test the integrated MVP 2 system end to end — including killing individual services mid-flow to verify the rest keeps working and the saga compensates — to confirm the Corte 2 deliverable actually meets the release checklist from this week's material, not just that it's documented as meeting it.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| Verification | End-to-end test of the integrated system through the gateway (place-order flow across auth/customers/products/sales) | done | Manual test session, `opti-infra` (`scripts/up.sh`) |
| Verification | Failure-injection test: stop a service mid-saga, confirm the rest of the system stays up and the saga compensates | done | Manual test session against `opti-workflow` |
| Verification | Cross-check the MVP 2 ADRs (saga/outbox/CQRS) against what the code actually does | done | See findings below |
| Material review | Week 10 Session 1 — Persistence in distributed systems (saga, outbox, CQRS) | done | https://code-corhuila.github.io/ova-web/2026-B/distribuidos/10-week/01-session/ |
| Material review | Week 10 Session 2 — Release: shipping MVP 2 (integrated system) | done | https://code-corhuila.github.io/ova-web/2026-B/distribuidos/10-week/02-session/ |

## 2. My individual contribution
- Read both Week 10 PDFs in full (database-per-service, saga orchestration/choreography, the outbox pattern, CQRS and eventual consistency; and the MVP 2 release checklist — promote, tag `v2.0.0`, prove a failure/compensation path, demo with an injected failure) and completed both self-checks.
- Brought up the real integrated system (`opti-infra`, which composes every sibling repo's `deploy/compose.yml` — auth/customers/products/sales DBs and APIs, the gateway, worker, workflow, and the four portals) and drove the place-order flow end to end through the gateway.
- Did the exact test the Session 2 material calls for instead of only the happy path: **stopped individual services mid-flow** (`docker compose stop` on a domain API while a saga was in progress) and confirmed (a) the rest of the system keeps responding, (b) `opti-workflow`'s `SagaOrchestrator` detects the participant failure and runs the compensating steps in reverse order, and (c) re-running the same request afterward doesn't double-charge or double-reserve, thanks to the idempotency keys already in the participant calls.
- Cross-checked the 4 MVP 2 ADRs (database-per-service, place-order saga, outbox, CQRS) against the actual code across all 17 `opti-*` repos, since the course explicitly weighs "architecture: contracts respected, saga/outbox correct" in the MVP 2 evaluation. Confirmed database-per-service and the saga are genuinely implemented as documented. Found two real mismatches: `ADR-005` describes an outbox table + relay, but there is no outbox table, no message broker, and no relay anywhere in the codebase — what actually runs (`opti-worker`'s `CheckSalesGoals`) is a scheduled polling job with an idempotency key, a different and valid pattern, just not the one the ADR names. `ADR-006` describes CQRS with an eventually-consistent read model, but the "read side" (`JdbcSalesReportRepository`) queries the same `work_order` write table directly — separate read use cases, but not a separate or eventually-consistent read model.
- Also confirmed no `opti-*` code repository carries a `v2.0.0` git tag yet (only `opti-docs` does), so the release checklist's "tag `v2.0.0` on main, the images built from that tag are the release" isn't actually satisfied for the running system yet.

## 3. Blockers and risks
- `ADR-005` and `ADR-006` describe patterns (outbox, CQRS) that don't match the actual implementation — needs the authors (who wrote those ADRs) to either build the real pattern or correct the ADR to describe what's actually running, before this gets reviewed against the course's architecture criteria.
- No code repo is tagged `v2.0.0` — there's no reproducible, versioned build of the real system yet, only the documentation claims the release.

## 4. Plan for next week
- Raise the outbox/CQRS mismatch with the team so it's resolved one way or the other (implement the real pattern, or correct the ADR) before the deliverable is reviewed.
- Push for `v2.0.0` tags on the actual service repos once the above is settled.
- Extend failure-injection testing to the remaining cross-service flows not yet covered this week.

## 5. Compliance self-check
- [ ] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [ ] Testable acceptance criteria
- [ ] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [ ] No secrets; config via environment variables

## 6. Evidence links
- https://code-corhuila.github.io/ova-web/2026-B/distribuidos/10-week/01-session/
- https://code-corhuila.github.io/ova-web/2026-B/distribuidos/10-week/02-session/
- https://github.com/code-corhuila/opti-infra
- https://github.com/code-corhuila/opti-workflow — `workflow-core/.../usecase/SagaOrchestrator.java` (compensation logic verified against the failure-injection test)
- https://github.com/code-corhuila/opti-worker — `worker-core/.../usecase/CheckSalesGoals.java` (the actual mechanism behind the "outbox" claim in ADR-005)
- https://github.com/code-corhuila/opti-docs/blob/main/05-architecture/decisions/records/ADR-005-outbox-eventual-consistency.md
- https://github.com/code-corhuila/opti-docs/blob/main/05-architecture/decisions/records/ADR-006-cqrs-sales-reports.md
