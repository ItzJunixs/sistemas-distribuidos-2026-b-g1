<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       07-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 07

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Juan Diego Jiménez Horta
- GITHUB_USER: ItzJunixs
- TEAM: The illusionists
- SPRINT_GOAL: Close out my half of `07-api` (REST guidelines, authentication strategy, and the `ms-pacientes` OpenAPI contract) and merge it into `main` via PR #10, splitting the remaining 3 service contracts (`ms-inventario`, `ms-ordenes`, `ms-facturacion`) with a teammate so `07-api` is fully covered between the two of us.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-01 | Register a patient | done | https://github.com/code-corhuila/opti-docs/pull/10 |
| HU-02 | Register an optical prescription | done | https://github.com/code-corhuila/opti-docs/pull/10 |
| HU-03 | Search for a patient | done | https://github.com/code-corhuila/opti-docs/pull/10 |
| HU-04 | Flag an overdue control visit | done | https://github.com/code-corhuila/opti-docs/pull/10 |

## 2. My individual contribution
- Wrote `07-api/guidelines.md` (REST versioning, naming, pagination, standard response codes, and the 400-vs-422 distinction mapped to our actual domain invariants) and `07-api/authentication.md` (target Keycloak+JWT flow, RBAC role table, and the Corte 1 realization gap, tracked as AT-003).
- Translated `contracts/openapi/_shared.yaml` and `api-gateway.yaml` from Spanish to English (ADR-001 compliance) and aligned them with the real service names (`ms-pacientes`, `ms-inventario`, `ms-ordenes`, `ms-facturacion`) instead of the generic course scaffold.
- Wrote `contracts/openapi/ms-pacientes.yaml`, the first full OpenAPI contract for the project, covering HU-01 to HU-04 (FR-001 to FR-005).
- Removed `contracts/openapi/auth-service.yaml` — it modeled a service that doesn't exist in our domain (Keycloak is the identity provider, not a bespoke auth service — see `00-governance/security-policy.md`).
- Split the remaining `07-api` work with a teammate (they took `ms-inventario`, `ms-ordenes`, and `ms-facturacion`) and opened PR #10, which was reviewed and merged into `main` (commit `425e7dc`).

## 3. Blockers and risks
- My teammate's 3 remaining contracts are pushed to a branch but not yet opened as a PR against `main` — until that merges, `07-api` is incomplete (missing `ms-inventario.yaml`, `ms-ordenes.yaml`, `ms-facturacion.yaml`).
- That PR is expected to conflict with my already-merged changes on `_shared.yaml` (both branches edited the `bearerAuth` description) and on `auth-service.yaml` (deleted on my side, untouched on theirs) — needs manual conflict resolution, keeping the English/Keycloak version.

## 4. Plan for next week
- Get the teammate's PR opened against `main`, resolve the `_shared.yaml`/`auth-service.yaml` conflicts together, and get it through the required teacher-approval gate (CODEOWNERS on `main`).
- Once merged, verify `07-api` fully satisfies its own `README.md` checklist (guidelines, authentication, one contract per service, shared schemas) before moving on to the next section.

## 5. Compliance self-check
- [ ] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [ ] Testable acceptance criteria
- [ ] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [ ] No secrets; config via environment variables

## 6. Evidence links
- https://github.com/code-corhuila/opti-docs/pull/10
- https://github.com/code-corhuila/opti-docs/commit/425e7dcb41912188d3bb47c2f0abf108c112c69c
- https://github.com/code-corhuila/opti-docs
