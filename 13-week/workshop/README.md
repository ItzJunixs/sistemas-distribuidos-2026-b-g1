# Week 13 Workshop — Threat modeling on OptiView

Individual workshop of week 13 (security and threat modeling) applied to the OptiView
team project. Delivery 1 (in class): CP0 to CP5. Delivery 2 (home): CP6 to CP9.

<!-- CONFIG-START -->
- FULL_NAME: Juan Diego Jiménez Horta
- GITHUB_USER: ItzJunixs
- TEAM: The Illusionists (OptiView)
- DOCS_REPO: https://github.com/code-corhuila/opti-docs
- BOUNDARY: TB2 — api-gateway → ms-pacientes (repo opti-customers-api), inbound direction
- CLAIM_ISSUE: https://github.com/code-corhuila/opti-docs/issues/36
- REFLECTION_DOC: https://docs.google.com/document/d/1NNn4eS46S0TIawrHL2fVm6KZ-PpZ_lW3PbhvTaffXK0/edit?usp=sharing
- LIBRARY_BOOK: Tanenbaum, A. S., & Van Steen, M. Sistemas distribuidos: principios y paradigmas (2.ª ed.). Pearson Educación, 2008. ISBN 978-970-26-1280-3. Call number 005.4476 T15d, copy 2 (CORHUILA library, Prado Alto). Chapter 9 "Seguridad": pp. 378-380, 396-397, 414, 428
- LIBRARY_RECORD_TIME: 19:45 (2026-10-28)
- STORIES: https://github.com/code-corhuila/opti-docs/issues/33 (E, risk 9, P0), https://github.com/code-corhuila/opti-docs/issues/34 (S, risk 6, P1), https://github.com/code-corhuila/opti-docs/issues/35 (T, risk 6, P1)
<!-- CONFIG-END -->

## Boundary claimed

`api-gateway` (published on :8080) forwards `/api/v1/patients/**` to `ms-pacientes` (:8081,
inside `optiview-net`). Data crossing it: patient identity and contact PII, optical formulas
(clinical PII), the `Authorization: Bearer` JWT and `X-Correlation-Id`.

## Files

| Checkpoint | File | Status |
|---|---|---|
| CP0 | `README.md` | done |
| CP1 | `01-attack-surface.md` | done |
| CP2 | opti-docs `05-architecture/security/stride-ItzJunixs.md` (PR, open for review) | done |
| CP3 | opti-docs issues #33, #34, #35 | done |
| CP4 | `04-security-ci/` | done |
| CP5 | `../hu-status/README.md` + peer review | done |
| CP6 | opti-docs follow-up PR on `security/stride-ItzJunixs` (review answered) | done |
| CP7 | issues #33, #34, #35 refined (criteria, STRIDE, risk, labels, links) | done |
| CP8 | `04-security-ci/HOW-TO-RUN.md`: real test output (red, 403 expected / 200 actual) + actionlint OK | done |
| CP9 | implementation plan in the reflection doc + final `hu-status` | done |

## Submission links

- Fork: https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1
- Workshop: https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/tree/main/13-week/workshop
