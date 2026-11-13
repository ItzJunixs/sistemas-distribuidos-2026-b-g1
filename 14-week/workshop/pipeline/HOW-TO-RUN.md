# Pipeline kit — where each file goes and how it was checked

| File | Goes to | Story |
|---|---|---|
| `ci-api.yml` | `.github/workflows/ci.yml` of the six Java repos (change `DOMAIN`; worker/workflow without `integration`) | CI-02, CI-03, CI-04, CI-05 |
| `ci-portal.yml` | `.github/workflows/ci.yml` of opti-front and the four portals | CI-03, CI-04, CI-05 |
| `no-todo.sh` | `.github/scripts/no-todo.sh` (executable) in every code repo | DoD "no debt without a ticket" |
| `release-smoke.yml` | `opti-infra/.github/workflows/release-smoke.yml` | CI-07 |
| `../../12-week/workshop/deploy/publish.yml` | `.github/workflows/publish.yml` | DEP-01, DEP-02 |

## Static checks done

```text
$ actionlint            # ci-api.yml, ci-portal.yml, release-smoke.yml, publish.yml
(no output, exit 0)     # actionlint 1.7.12, with shellcheck on the run: blocks
$ shellcheck no-todo.sh
(no output, exit 0)
```

## What each repo needs before adopting it

- Java: nothing in the pom for coverage (JaCoCo is called by coordinates). Set
  `COVERAGE_FLOOR` to the value printed by the first green run, rounded down.
- Java: an `NVD_API_KEY` repository secret for dependency-check (without it the NVD download is
  throttled and the job can take > 15 min). Optional `dependency-check-suppressions.xml`.
- Portals: `eslint` + `typescript-eslint` + an `eslint.config.js`, `@vitest/coverage-v8` and
  `test.coverage.thresholds` in `vitest.config.ts`; a `typecheck` script (`tsc --noEmit`).
- Branch protection updated with the new check names (`03-branch-protection.md`).
- Roll-out order: one repo first (opti-customers-api), then copy. Turn gates on as **blocking**
  from day one; if a gate is wrong, fix the gate in a reviewed PR, never skip it.

## Run `no-todo.sh` locally

```bash
cd ~/Documents/optiview-repos/opti-customers-api
bash ~/OneDrive/Documentos/sistemas-distribuidos-2026-b-g1/14-week/workshop/pipeline/no-todo.sh
```

Result on the current repos is in `../hu-status/README.md` §2 (evidence).
