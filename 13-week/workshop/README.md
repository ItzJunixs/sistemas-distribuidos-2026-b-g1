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
- LIBRARY_BOOK: Tanenbaum, A. S., & Van Steen, M. Sistemas distribuidos: principios y paradigmas (2.ª ed.). Pearson Educación, 2008. Call number: <signatura>. Chapter 9 "Seguridad" (9.1.1, 9.2, 9.3), pp. <__>
- LIBRARY_RECORD_TIME: <hh:mm of the library record>
- STORIES: https://github.com/code-corhuila/opti-docs/issues/33, https://github.com/code-corhuila/opti-docs/issues/34, https://github.com/code-corhuila/opti-docs/issues/35
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

## Submission links

- Fork: https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1
- Workshop: https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/tree/main/13-week/workshop
