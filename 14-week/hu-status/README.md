<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       14-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 14

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Juan Diego Jiménez Horta
- GITHUB_USER: ItzJunixs
- TEAM: The Illusionists
- SPRINT_GOAL: Audit the CI/CD of the 16 OptiView code repos against the Definition of Done, turn every DoD item into a blocking gate where a machine can check it, define branch protection for develop/qa/main, measure DORA and slice the CI/CD stories for MVP 3.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| W14-01 | Pipeline audit of the 16 code repos (workflows + last 40 runs each) | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/blob/main/14-week/workshop/01-pipeline-audit.md |
| W14-02 | Definition of Done mapped to gates (18 items: 3 real, 6 not blocking, 9 self-certified) | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/blob/main/14-week/workshop/02-dod-gates.md |
| W14-03 | Branch protection rules for develop/qa/main + how to apply them | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/blob/main/14-week/workshop/03-branch-protection.md |
| W14-04 | DORA metrics from git history and Actions runs | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/blob/main/14-week/workshop/04-dora.md |
| W14-05 | CI/CD stories CI-00..CI-08 for MVP 3 | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/blob/main/14-week/workshop/05-cicd-stories.md |
| W14-06 | Pipeline kit: ci-api.yml (integration with real Postgres, coverage floor, lint, hexagonal and security gates), ci-portal.yml, release-smoke.yml, no-todo.sh | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/tree/main/14-week/workshop/pipeline |

## 2. My individual contribution
- Read every workflow of the 16 code repos and the last 40 Actions runs of each through the GitHub API: 127 CI successes / 34 failures, and every failure since 2026-11-06 is a job with 0 steps that ends in ~2 s, so CI has not compiled or tested anything in November.
- Showed that MVP 3 was merged into develop and qa with CI red (e.g. opti-customers-api #7 and #8, opti-sales-api #18 and #19) although develop/qa/main are protected: required status checks are not enforced. `release/3.0.0 → main` is still open with the same red check.
- Mapped the 18 items of `opti-docs/00-governance/definition-of-done.md` to gates and found that the DoD asks for Checkstyle/ESLint and a coverage baseline that no repo has, and that the 26 persistence integration tests of the four APIs (`Jdbc*IntegrationTest`) never run in CI because `TEST_DATABASE_URL` is not set (same for the 4 Redis tests of opti-workflow and `TEST_REDIS_PORT`).
- Wrote `ci-api.yml` with a Postgres service migrated from the `-db` repo so those tests run (and fail if skipped), a JaCoCo floor without touching the pom, format check only on changed files, a hexagonal-boundary grep and gitleaks + dependency-check; `release-smoke.yml` starts the whole platform from the siblings and logs in through the gateway. All pass actionlint/shellcheck.
- Ran `no-todo.sh` on the 11 service and portal repos: 0 unlinked TODO/FIXME in production code (exit 0 in all), and a negative test fails as expected. The hexagonal grep also finds 0 framework/I-O imports in the six `*-core` modules, so both gates can be turned on today without breaking anything.
- Measured DORA: 1 release to main in 6 weeks, all 130 PRs into develop merged on only 5 days, ~21 % of them `fix/*`; time to restore not measurable yet.

## 3. Blockers and risks
- GitHub Actions is not starting jobs in the org: until an org owner fixes it (Billing → Actions), no gate can be green and branch protection with required checks would block every merge. CI-00 is the first story.
- I cannot read the exact protection rules (needs admin); the rules in `03` are inferred from what was merged.
- Adding lint to existing code at once would fail every PR; the proposal checks only changed files.

## 4. Plan for next week
- Ask the org owner to fix Actions (CI-00) and to enable required checks with no bypass (CI-01) before `release/3.0.0` is merged to main.
- Propose `ci-api.yml` in opti-customers-api first and set its coverage floor from the first green run.
- Week 15: load and chaos tests on the place-order journey and the MVP 3 operability checklist.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [x] Testable acceptance criteria
- [x] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

## 6. Evidence links
- Workshop folder: https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/tree/main/14-week/workshop
- 0-step CI run: https://github.com/code-corhuila/opti-customers-api/actions/runs/42156463617
- PRs merged with CI red: https://github.com/code-corhuila/opti-customers-api/pull/7, https://github.com/code-corhuila/opti-customers-api/pull/8
- Release PR still open: https://github.com/code-corhuila/opti-customers-api/pull/9
- DoD: `opti-docs/00-governance/definition-of-done.md`
