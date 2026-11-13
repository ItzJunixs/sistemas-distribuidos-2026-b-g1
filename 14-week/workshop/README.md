# Week 14 Workshop — CI/CD, DoD-as-gates and branch protection on OptiView

Individual work for week 14 applied to the OptiView team project. It covers the two "This week"
activities of the guides:

- Session 1: PRs run build + lint + type + tests + coverage + security as blocking gates;
  protect main; automate deployment of the tagged image.
- Session 2: map every DoD item to a blocking gate, branch protection on develop/qa/main,
  path-filtered per-service pipelines + cross-service contract tests + a smoke test, and slice
  the CI/CD stories for MVP 3.

<!-- CONFIG-START -->
- FULL_NAME: Juan Diego Jiménez Horta
- GITHUB_USER: ItzJunixs
- TEAM: The Illusionists (OptiView)
- DOCS_REPO: https://github.com/code-corhuila/opti-docs
- DOD_SOURCE: opti-docs/00-governance/definition-of-done.md
- EVIDENCE_DATE: 2026-11-12 (GitHub API: last 40 workflow runs of each of the 16 public code repos; local clones of 2026-11-11)
<!-- CONFIG-END -->

## Files

| # | File | What it answers | Status |
|---|---|---|---|
| 1 | `01-pipeline-audit.md` | What every pipeline of the 16 code repos runs today, and what happened to MVP 3 | done |
| 2 | `02-dod-gates.md` | Each DoD item → the machine that rejects it (or "self-certified") | done |
| 3 | `03-branch-protection.md` | Rules for develop / qa / main and why the current ones let red PRs in | done |
| 4 | `04-dora.md` | The four DORA metrics measured from our git history and Actions runs | done |
| 5 | `05-cicd-stories.md` | CI/CD stories for MVP 3 with acceptance criteria | done |
| 6 | `pipeline/` | `ci-api.yml`, `ci-portal.yml`, `release-smoke.yml`, `no-todo.sh` + `HOW-TO-RUN.md` | done (actionlint + shellcheck clean) |

## Main findings

1. Every code repo has a CI on PRs to develop/qa/main and a publish to GHCR, and
   develop/qa/main are **protected** in all 16 public repos. The base is good.
2. But the gates are only **build + tests** (Java: `mvn verify`; portals: `vitest` + `tsc` +
   `vite build`; DBs: rebuild from scratch + undo; gateway: `nginx -t` + smoke). There is no lint,
   no coverage floor, no security scan and no system smoke test.
3. **MVP 3 entered `develop` and `qa` with every check red.** Since 2026-11-06 the CI jobs end in
   ~2 s with 0 steps (Actions is not starting them). PRs like opti-customers-api #7 (→ develop)
   and #8 (→ qa) were merged anyway. Branch protection exists, but it does not require the
   status checks, or it is bypassed. This is exactly the "just merge it, the gate is annoying"
   scenario of the guide.
4. The integration tests against a real PostgreSQL exist in the four APIs but **never run in
   CI**: they need `TEST_DATABASE_URL`, which `ci.yml` does not provide.

## Submission links

- Fork: https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1
- Workshop: https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/tree/main/14-week/workshop
