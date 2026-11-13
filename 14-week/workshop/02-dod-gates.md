# 2 · Definition of Done as gates

Source: `opti-docs/00-governance/definition-of-done.md`. For each item: the gate that proves it
today, and the gate proposed for MVP 3. "Self-certified" means no machine can reject it.

Legend: ✅ blocking gate exists · ⚠️ exists but does not block / does not run · ❌ self-certified

## Code

| DoD item | Today | Gate proposed | Where |
|---|---|---|---|
| Implements all acceptance criteria | ❌ reviewer reads the HU | Acceptance tests named after the criterion (e.g. `PatientFormulasAuthorizationTest`) + required review | review rule (`03`) |
| Reviewed and approved by ≥ 1 member | ⚠️ branches protected, but PRs merged with red checks | Required approving review, dismiss stale approvals, no bypass | `03-branch-protection.md` |
| Lint passes (DoD names Checkstyle/Spotless, ESLint + Prettier) | ❌ no linter configured anywhere | Java: `spotless:check` (google-java-format); portals: `eslint . --max-warnings 0` | `pipeline/ci-api.yml`, `pipeline/ci-portal.yml` |
| Domain layer has zero framework imports | ❌ (but true today: 0 files in the six `*-core/src/main` import Spring, JPA, `java.sql` or `java.net.http`) | `grep` gate in CI (cheap); ArchUnit later | `ci-api.yml` step "Hexagonal boundary" |
| No debt without a ticket | ❌ | `no-todo.sh`: fails on `TODO`/`FIXME`/`XXX` in `src/main` unless it has an issue link | `pipeline/no-todo.sh` |

## Tests

| DoD item | Today | Gate proposed | Where |
|---|---|---|---|
| Unit tests for new business logic | ✅ `mvn verify`, `vitest run` | keep | `ci.yml` |
| Coverage does not decrease (baseline "pending") | ❌ not measured | JaCoCo `check` with a floor = today's measured value, rounded down; vitest `--coverage` with thresholds. The floor only goes up (declared ≤ measured) | `ci-api.yml`, `ci-portal.yml` |
| All tests pass in CI | ⚠️ CI has not executed since 2026-11-06 (0-step jobs) | Fix Actions + required checks | `01-pipeline-audit.md` |
| One unit test per domain invariant | ✅ e.g. `FrameServiceTest.salePriceBelowCostIsRejected`, `PatientServiceTest.formulaNeedsAxisWhenThereIsCylinderAndKeepsAxisInRange` | review checklist | — |

## Integration

| DoD item | Today | Gate proposed | Where |
|---|---|---|---|
| Changes do not break other services | ⚠️ `Jdbc*IntegrationTest` (26 tests in the four APIs) exist but are skipped in CI | Postgres service container in CI + `TEST_DATABASE_URL`, migrated with the `-db` repo at the same branch | `ci-api.yml` job `integration` |
| OpenAPI contract updated | ⚠️ `ContractChecks` verifies the common contract (Annex C), not the `openapi/*.yaml` of each service | Validate responses against `openapi/openapi.yaml` (e.g. `swagger-request-validator` in `*HttpTest`) — story CI-06 | — |
| Data model / events docs updated | ❌ | PR template checkbox (human) | — |

## Deployment

| DoD item | Today | Gate proposed | Where |
|---|---|---|---|
| Mergeable to develop | ✅ GitHub | "require branch up to date" | `03` |
| CI/CD green on the branch | ⚠️ not enforced | required status checks | `03` |
| Deployed to qa | ❌ manual | promote by retag (week 12 `publish.yml`) | week 12 |
| Smoke test on qa (health responds) | ⚠️ only the gateway alone | `release-smoke.yml` in opti-infra: compose up at the PR's branch, migrate, `/health` of all, a login and a GET through the gateway | `pipeline/release-smoke.yml` |
| No secrets / known CVEs (week 13) | ❌ | `gitleaks` + OWASP dependency-check (`--failOnCVSS 7`) / `npm audit --audit-level=high` | `ci-api.yml`, `ci-portal.yml` |

## Documentation

| DoD item | Today | Gate proposed |
|---|---|---|
| README / ADR updated when needed | ❌ | PR template + reviewer. Not everything needs a machine: this one stays human, but explicit. |

## Result

Of the 18 DoD items above, **3 have a real blocking gate today** (unit tests, the invariant
tests, mergeability), **6 exist but do not block or do not run**, and **9 are self-certified**.
The DB rebuild/undo check of the `-db` repos is a real gate too, but it is not in the DoD: it
should be added there. With the pipeline of `pipeline/` and the rules of `03`, 14 items become
machine-checked; 4 stay with the reviewer (criteria fully implemented, invariants covered,
data-model/event docs, README/ADR), now as a required review instead of trust.
