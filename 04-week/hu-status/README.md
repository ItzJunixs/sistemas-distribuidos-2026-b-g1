<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       04-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 04

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Juan Diego Jiménez Horta
- GITHUB_USER: ItzJunixs
- TEAM: The illusionists
- SPRINT_GOAL: Complete the 04-requirements documentation (functional, non-functional, user stories, traceability matrix) in opti-docs, create the first 3 User Stories in GitHub Projects to build the MVP walking skeleton presented in Figma this week, and — together with the rest of the team covering 05-architecture and 06-data — close out the Discovery/Definition documentation phase (00-03) into the next layer (04-06).
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-01 | Register a patient | todo |
| HU-05 | Register a frame in inventory | todo |
| HU-08-MVP | Create a work order (happy path only) | todo |

## 2. My individual contribution
- Filled in `04-requirements/functional.md` (new — 21 functional requirements grouped by service) and `04-requirements/_template-nfr.md` (new — was referenced by the section README but did not exist).
- Filled in `04-requirements/non-functional.md` with OptiView-specific metrics (critical endpoints, Habeas Data compliance, scaling limits).
- Wrote the full 12-story backlog (HU-01 to HU-12, 60 story points) in `04-requirements/user-stories.md`, aligned with the estimates already committed in `00-governance/agile-conventions.md`.
- Filled in `04-requirements/traceability-matrix.md` (FR → HU → Test → Service, NFR → Validation, inverse HU → FR) and flagged a service-naming mismatch between `04-requirements` and `02-domain/optiview/` for the team to reconcile.
- Selected and scoped the 3 HUs for this week's MVP demo (HU-01, HU-05, and a trimmed HU-08-MVP limited to the happy path) and created them as User Stories in GitHub Projects, to match the flow already designed in Figma.

## 3. Blockers and risks
-

## 4. Plan for next week
-

## 5. Compliance self-check
- [ ] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [ ] Testable acceptance criteria
- [ ] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [ ] No secrets; config via environment variables

## 6. Evidence links
- https://github.com/code-corhuila/opti-docs
- https://github.com/code-corhuila/opti-docs/issues?q=is%3Aissue+state%3Aopen
- https://www.figma.com/design/zD1Tfk9xzgg6eXSMvaOLdB/Untitled?node-id=0-1&t=l8VwN1DWiLOPwX6x-1
