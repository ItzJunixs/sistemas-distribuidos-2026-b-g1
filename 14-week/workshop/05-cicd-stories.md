# 5 · CI/CD stories for MVP 3

Continue the numbering of week 12 (DEP-*). Priorities: P0 blocks the release.

### CI-00 · GitHub Actions runs jobs again in code-corhuila (P0)

- Given a PR to develop of any `opti-*` repo, when CI starts, then the job has a runner and its
  steps appear in the log (not "0 steps, ~2 s").
- Given the org Actions settings, when checked, then the cause (minutes / spending limit /
  disabled Actions) is written in the issue with the date it was fixed.

### CI-01 · Required checks and no bypass on develop/qa/main (P0)

- Given a PR whose `build-and-test` is red, when anyone (admins included) tries to merge to
  develop, qa or main, then GitHub blocks it with "Required statuses must pass".
- Given a direct `git push origin main`, then it is rejected (`GH006`).
- Evidence: the protection JSON of the three branches attached to the issue.

### CI-02 · Persistence tests run in CI against a real PostgreSQL (P1)

- Given `opti-customers-api`, when CI runs on a PR, then `JdbcRepositoriesIntegrationTest` is
  executed (not skipped) against a `postgres:16.9-alpine` service migrated with
  `opti-customers-db` at the PR's base branch, and the test report shows it.

### CI-03 · Coverage floor that only goes up (P1)

- Given JaCoCo in the pom, when `mvn verify` runs, then the build fails if line coverage of
  `*-core` is below the declared floor; the floor in the pom is ≤ the value measured on develop.
- Given a portal, `vitest run --coverage` fails under its `thresholds`.

### CI-04 · Lint gates (P1)

- Given a Java file not formatted with google-java-format, `mvn spotless:check` fails the PR.
- Given an unused variable in a portal, `eslint . --max-warnings 0` fails the PR.

### CI-05 · Secret and dependency scan block on findings (P1)

- Given a commit with a private key, `gitleaks` fails the PR.
- Given a dependency with a CVE ≥ 7.0, dependency-check (Java) / `npm audit --audit-level=high`
  (portals) fails the PR, unless the CVE is in a reviewed suppression file with an expiry date.

### CI-06 · Each API's responses match its `openapi/openapi.yaml` (P2)

- Given `CustomersHttpTest`, when a response does not match the OpenAPI schema of that operation,
  then the test fails (response validation in the shared `ContractChecks`).

### CI-07 · Platform smoke test before qa/main (P0)

- Given a PR to `main` of `opti-infra` (or a manual dispatch with a version), when
  `release-smoke.yml` runs, then it clones the 16 siblings at that branch/tag, starts the
  platform, migrates the four DBs, and checks: every container `healthy`, `GET /health` of the
  gateway 200, `POST /api/v1/auth/login` with the demo admin 200, `GET /api/v1/patients` with
  that token 200. Any failure fails the workflow and prints the gateway log.

### CI-08 · Docs-only changes do not run the build (P2)

- Given a PR that only changes `**.md`, then `ci.yml` is skipped (`paths-ignore`) and the
  required check is satisfied by a no-op job of the same name, so the PR can still merge.
