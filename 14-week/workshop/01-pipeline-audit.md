# 1 · Pipeline audit of the 16 code repositories

Source: `.github/workflows/*.yml` of each repo (local clones, 2026-11-11) and the GitHub API
(`/actions/runs`, last 40 runs per repo, read 2026-11-12). `opti-docs` and `opti-infra` are
private, so their runs are not in the API numbers.

## 1.1 What each type of repo runs

| Repo type | Repos | Workflow · trigger | Steps (gates) |
|---|---|---|---|
| Java service | auth-api, customers-api, products-api, sales-api, worker, workflow | `ci.yml` · PR to develop/qa/main | `mvn -B verify` (compile + unit + HTTP/contract checks; persistence tests skipped without `TEST_DATABASE_URL`) |
| Portal / front (React + Vite + TS) | front, auth/customers/products/sales-portal | `ci.yml` · PR | `npm ci` → `npm test` (vitest) → `npm run build` (`tsc --noEmit && vite build`) |
| Database | auth/customers/products/sales-db | `db-ci.yml` · PR | Postgres service + `scripts/verify-rebuild.sh`: migrate from empty, migrate again = 0 changes, run all `U*.sql`, migrate again |
| Gateway | api-gateway | `ci.yml` · PR | `docker build`, `nginx -t`, start it, `tests/smoke.sh` with upstreams down (must answer 503, 401, 404 with the envelope, CORS, correlation id) |
| Infra | opti-infra | `ci.yml` · PR | `docker compose config -q … \|\| true` (never fails) + `shellcheck scripts/*.sh` |
| All executable repos | 16 | `publish.yml` · push to develop | build `deploy/Dockerfile` → push `ghcr.io/code-corhuila/<repo>:latest` |

## 1.2 Results of the last runs

| Workflow | success | failure |
|---|---|---|
| CI (services, portals, gateway) | 127 | 34 |
| db-ci | 37 | 9 |
| Publish image | 29 | 9 |

All the failures after 2026-11-05 have the same shape: the job "runs" for about 2 seconds,
has **0 steps** and no runner name. Examples:

| Repo | Run | Branch | Started → finished | Steps |
|---|---|---|---|---|
| opti-customers-api | [42156463617](https://github.com/code-corhuila/opti-customers-api/actions/runs/42156463617) | feat/pdf-payments-validation | 2026-11-06 03:25:29 → 03:25:31 | 0 |
| opti-auth-db | db-ci on fix/disable-fixed-demo-credentials | — | 2026-11-10 20:50:14 → 20:50:16 | 0 |
| opti-customers-api | [42923849949](https://github.com/code-corhuila/opti-customers-api/actions/runs/42923849949) (Publish) | develop | 2026-11-10 23:29 | 0 |
| opti-customers-api | [42937092937](https://github.com/code-corhuila/opti-customers-api/actions/runs/42937092937) | release/3.0.0 | 2026-11-11 01:11:27 → 01:11:28 | 0 |

So the code was never compiled nor tested by CI in November. That is an Actions problem at the
org level (jobs not being scheduled: typically the Actions minutes or spending limit of the
organization), not a red test. Whoever owns the org settings has to check
*Settings → Billing → Actions*.

## 1.3 What was merged while CI was red

| Repo | PR | Base | Merged | CI of that PR |
|---|---|---|---|---|
| opti-customers-api | [#7](https://github.com/code-corhuila/opti-customers-api/pull/7) feat/pdf-payments-validation | develop | 2026-11-10 23:29 | failure (0 steps) |
| opti-customers-api | [#8](https://github.com/code-corhuila/opti-customers-api/pull/8) qa-mvp3-pagos-pdf | qa | 2026-11-11 01:09 | failure (0 steps) |
| opti-sales-api | [#18](https://github.com/code-corhuila/opti-sales-api/pull/18) feat/pdf-payments-validation | develop | 2026-11-10 23:30 | failure (0 steps) |
| opti-sales-api | [#19](https://github.com/code-corhuila/opti-sales-api/pull/19) qa-mvp3-pagos-pdf | qa | 2026-11-11 01:09 | failure (0 steps) |
| the other 7 MVP 3 repos (api-gateway, front, customers-portal, products-api, products-portal, sales-portal, sales-db) | same pattern (feat → develop, qa-mvp3-pagos-pdf → qa) | | 2026-11-10/11 | failure; in opti-sales-db the qa commit `7610564` carries `[skip ci]`, so no check ran at all |

Still open: `release/3.0.0 → main` ([customers-api #9](https://github.com/code-corhuila/opti-customers-api/pull/9),
[sales-api #20](https://github.com/code-corhuila/opti-sales-api/pull/20), …) with CI red. If
`main` lets them in the same way, `v3.0.0` would ship code that no machine has built.

The point is not who merged: under the MVP 3 deadline the team chose to merge because the gate
was "broken". The guide's fix applies: **fix the cause (Actions), not the gate**, and configure
branch protection so the bypass button does not exist (`03-branch-protection.md`).

## 1.4 Other gaps

| # | Gap | Where |
|---|---|---|
| G1 | No linter in any repo (no Checkstyle/Spotless in the poms, no ESLint in the portals), while the DoD requires "CI linting passes" | `pom.xml`, `package.json` |
| G2 | No coverage measurement (no JaCoCo, vitest without `--coverage`) | idem |
| G3 | No dependency / secret scan | no `security.yml` in any repo (the one proposed in week 13 was not adopted) |
| G4 | The persistence integration tests of the four APIs (`Jdbc*IntegrationTest`, 26 tests) and the Redis store test of opti-workflow (4) never run in CI (`@EnabledIfEnvironmentVariable(TEST_DATABASE_URL / TEST_REDIS_PORT)`) | `*-adapters/src/test/.../persistence/` |
| G5 | No system smoke test: nothing starts the composed platform and calls `/health` through the gateway | — |
| G6 | `opti-infra` compose validation has `\|\| true` | `opti-infra/.github/workflows/ci.yml` |
| G7 | Publish only `:latest`, only develop | `publish.yml` (week 12) |
| G8 | No `paths-ignore` for docs: a README change runs the full Maven build | all `ci.yml` |

What already works and must be kept: the DB rebuild/undo gate (`db-ci.yml`) and the gateway
smoke test are real, blocking checks that prove behaviour, not just compilation. The
`ContractChecks` base class (authentication, error envelope, correlation id, validation at
the border, idempotent creation, bounded listings) runs in every API's `mvn verify`.
