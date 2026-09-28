<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       08-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 08

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Juan Diego Jiménez Horta
- GITHUB_USER: ItzJunixs
- TEAM: The illusionists
- SPRINT_GOAL: Finish `07-api` end to end — close the open behavior decisions in `guidelines.md`, write the 3 remaining service contracts (`ms-inventario`, `ms-ordenes`, `ms-facturacion`) after the original PR for them was discarded, and fix a real RBAC gap (no role modeled the patient self-service actor from HU-11) across `authentication.md`, `_shared.yaml`, and `00-governance/security-policy.md`.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-05 | Register a frame in inventory | doing | https://github.com/code-corhuila/opti-docs/tree/docs/close-07-api-open-decisions |
| HU-06 | Configure minimum stock and alert | doing | https://github.com/code-corhuila/opti-docs/tree/docs/close-07-api-open-decisions |
| HU-07 | Approve a work order | doing | https://github.com/code-corhuila/opti-docs/tree/docs/close-07-api-open-decisions |
| HU-08 | Create a work order (quote) | doing | https://github.com/code-corhuila/opti-docs/tree/docs/close-07-api-open-decisions |
| HU-09 | Advance the work order status | doing | https://github.com/code-corhuila/opti-docs/tree/docs/close-07-api-open-decisions |
| HU-10 | Invoice and register payments | doing | https://github.com/code-corhuila/opti-docs/commit/f465c7b (partially merged by a teammate) |
| HU-11 | Patient portal: order status and balance | doing | https://github.com/code-corhuila/opti-docs/tree/docs/close-07-api-open-decisions |

## 2. My individual contribution
- Added a "Closed decisions" section to `07-api/guidelines.md` (DEC-01 to DEC-04): empty-list responses are always `200 []`, out-of-range `page`/`limit` is rejected with 400 instead of silently clamped, every 401/403/404 carries the shared `ErrorResponse` body, and any endpoint with two possible business-rule failures must state and justify its evaluation order.
- Wrote the 3 remaining OpenAPI contracts — `ms-inventario.yaml` (HU-05, HU-06), `ms-ordenes.yaml` (HU-07, HU-08, HU-09), `ms-facturacion.yaml` (HU-10) — after the teammate's original PR for them was discarded, applying DEC-01/DEC-04 consistently and grounding each validation order in how `02-domain/entities-and-rules.md` says the invariant is actually checked (e.g. duplicate SKU before bad price, because the use case queries the repository before constructing the `Frame` aggregate).
- Found and fixed a real RBAC gap: `04-requirements/functional.md` (FR-019) and HU-11 clearly put the *patient* in the actor seat for the patient portal, but no `PACIENTE` role existed anywhere — `00-governance/security-policy.md` had even mis-attributed HU-11 to the `VENDEDOR` role. Added `PACIENTE` to the security policy, `authentication.md`, and `_shared.yaml`'s `Role` enum, with an explicit ownership-scoping rule (a patient token only acts on its own record, unlike staff roles).
- Translated `_template-service.yaml` to English (ADR-001 compliance) and removed a stray Redis example that didn't match `06-data/models.md`'s PostgreSQL-only scope.
- Closed a stale entry in `04-requirements/traceability-matrix.md`'s "Identified gaps": the `ms-*` vs `identity-service` naming mismatch it still listed as unresolved was actually settled weeks ago by ADR-002.

## 3. Blockers and risks
- While this branch was still open, a teammate merged a separate PR (`close-07-api-fixes`, #16) straight to `main` using the **same commit message** as mine, but with different, partial content — it added their own `ms-facturacion.yaml` and two lines of `_shared.yaml`, without the `guidelines.md` DEC-01–DEC-04 section or the `authentication.md` role table update those files reference. `main` right now has files that cite decisions (`DEC-01`, `DEC-04`) and a role (`PACIENTE`) that aren't actually documented anywhere yet on `main` — only on this branch. Needs the team to agree not to push overlapping `07-api` work straight to `main` while a PR is open for the same section.
- This branch needs to merge next to resolve that inconsistency; expect little to no real conflict since the overlapping content is identical, but it still needs a deliberate merge, not two people independently "finishing" the same section.

## 4. Plan for next week
- Get this branch's PR reviewed and approved by `ariel5253` and merged into `main`, then verify `07-api` matches its own `README.md` checklist with no dangling `DEC-`/`PACIENTE` references.
- Propose a one-line addition to `00-governance/branching-policy.md` or `git-conventions.md` about claiming a section before starting parallel work on it, so this doesn't repeat.

## 5. Compliance self-check
- [ ] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [ ] Testable acceptance criteria
- [ ] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [ ] No secrets; config via environment variables

## 6. Evidence links
- https://github.com/code-corhuila/opti-docs/tree/docs/close-07-api-open-decisions
- https://github.com/code-corhuila/opti-docs/commit/f465c7b — teammate's partial merge of the same fix, referenced in Blockers above
- https://github.com/code-corhuila/opti-docs/blob/docs/close-07-api-open-decisions/07-api/guidelines.md
- https://github.com/code-corhuila/opti-docs
