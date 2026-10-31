<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       13-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 13

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Juan Diego Jiménez Horta
- GITHUB_USER: ItzJunixs
- TEAM: The Illusionists
- SPRINT_GOAL: Threat model the api-gateway → ms-pacientes boundary of OptiView (attack surface, STRIDE PR, security backlog and security gates in CI) in the week 13 workshop, delivery 1, then answer the review, refine the stories, run the negative test and plan the implementation (delivery 2).
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| W13-CP0 | Claim trust boundary api-gateway → ms-pacientes | done | https://github.com/code-corhuila/opti-docs/issues/36 · https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/commit/f98e416 |
| W13-CP1 | Attack surface map and trust boundaries | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/commit/7f90af0 |
| W13-CP2 | STRIDE on api-gateway → ms-pacientes (PR, open for review) | done | https://github.com/code-corhuila/opti-docs/pull/32 |
| W13-CP3 | Security backlog: three stories on the team board | done | https://github.com/code-corhuila/opti-docs/issues/33 · https://github.com/code-corhuila/opti-docs/issues/34 · https://github.com/code-corhuila/opti-docs/issues/35 · https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/commit/28377d7 |
| W13-CP4 | Security gates workflow and negative authorization test | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/commit/74c2d8c · https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/commit/328a6cb |
| W13-CP5 | Peer review of @BackSua STRIDE (opti-api-gateway → opti-products-api); no PR open yet, so the comment is on claim issue #30 | done | https://github.com/code-corhuila/opti-docs/issues/30#issuecomment-6484965822 |
| W13-CP6 | Answer peer review on STRIDE (@BackSua, @julianvargasb), rows S and E split in two steps | done | https://github.com/code-corhuila/opti-docs/commit/a80ae4b (follow-up PR on security/stride-ItzJunixs) |
| W13-CP7 | Refine security stories #33, #34, #35 | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/commit/236f1d5 |
| W13-CP8 | Run negative authorization test (red: 403 expected, 200 actual) and validate workflow | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/commit/75c12a7 |
| W13-CP9 | Implementation plan and close workshop | done | reflection doc |

## 2. My individual contribution
- Mapped the OptiView data flow with five trust boundaries and claimed TB2b (api-gateway → ms-pacientes).
- Wrote the STRIDE table for that boundary in opti-docs (`05-architecture/security/stride-ItzJunixs.md`); top risk is E = 9: `GET /patients/{id}/formulas` has no role or ownership check.
- Opened three `[security]` stories (#33 P0, #34 P1, #35 P1) on the team board.
- Proposed `security.yml` (gitleaks, OWASP dependency-check, negative authz test) and `PatientFormulasAuthorizationTest` for opti-customers-api.

## 3. Blockers and risks
- The negative authorization test is red on purpose until #33 is implemented in opti-customers-api; real run on 2026-10-30 confirmed 200 instead of 403.
- Docs (`07-api/authentication.md`) and code disagree on where the JWT is validated; the team has to decide.

## 4. Plan for next week
- Propose to the team the fix for #33: `requireRole` on the three GET endpoints of `PatientController`, so `PatientFormulasAuthorizationTest` turns green.
- Ask the owner of opti-auth-api for `aud` and `patientId` claims in `Rs256TokenIssuer` (#34, second step of #33).
- Clear client `X-User-*` headers in the gateway (#35).

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [x] Testable acceptance criteria
- [x] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

## 6. Evidence links
- Workshop folder: https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/tree/main/13-week/workshop
- STRIDE PR: https://github.com/code-corhuila/opti-docs/pull/32
- Peer review: https://github.com/code-corhuila/opti-docs/issues/30#issuecomment-6484965822
- Stories: https://github.com/code-corhuila/opti-docs/issues/33, https://github.com/code-corhuila/opti-docs/issues/34, https://github.com/code-corhuila/opti-docs/issues/35
- Reflection: https://docs.google.com/document/d/1NNn4eS46S0TIawrHL2fVm6KZ-PpZ_lW3PbhvTaffXK0/edit?usp=sharing
