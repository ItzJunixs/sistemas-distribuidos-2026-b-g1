# 3 · Branch protection on develop / qa / main

## 3.1 What we can see today

From the public API (`GET /repos/code-corhuila/<repo>/branches`), in all 16 code repos
`develop`, `qa` and `main` have `protected: true`; `release/*`, `qa-*` and `feat/*` do not.
The detail of the rules (`/branches/<b>/protection`) needs admin rights, so I inferred it
from what the platform allowed:

| Observation | What it means |
|---|---|
| PRs merged into develop and qa with the `CI` check in `failure` (opti-customers-api #7, #8; opti-sales-api #18, #19 …) | **"Require status checks to pass" is off**, or the check is not in the required list, or an admin bypassed it |
| All changes reach develop/qa/main through PRs (merge commits `Merge pull request #N`), none as direct pushes in the history | "Require a pull request before merging" is probably on |
| The PR to develop and the PR to qa of MVP 3 were merged ~1.5 h apart, the same night | qa is used as a step, not as a soak period; fine if the gate is the CI and the smoke on qa |

## 3.2 Rules proposed (MVP 3)

| Rule | develop | qa | main |
|---|---|---|---|
| Require a pull request, no direct pushes | ✅ | ✅ | ✅ |
| Required approving reviews | 1 | 1 | 2 (one of them the HU owner's reviewer of record) |
| Dismiss stale approvals when new commits are pushed | ✅ | ✅ | ✅ |
| Required status checks (must be green) | `build-and-test` (API) / `build-and-test` (portal) / `rebuild` (db) + `integration`, `quality`, `security` from `pipeline/` | same | same + `release-smoke` (opti-infra) |
| Require branch to be up to date before merging | ✅ | ✅ | ✅ |
| Require conversation resolution | ✅ | ✅ | ✅ |
| Do not allow bypassing the above (applies to admins) | ✅ | ✅ | ✅ |
| Allow force pushes / deletions | ❌ | ❌ | ❌ |
| Restrict who can push to matching branches | — | release managers | release managers |
| Required signed tags `v*` / tag protection | — | — | ruleset: only release managers create `v*` |

"Do not allow bypassing" is the line that would have stopped the MVP 3 merges. With Actions
broken, nobody could have merged, which is the correct outcome: the team would have fixed
Actions (one person, minutes) instead of shipping code no machine had built.

## 3.3 How to apply it (an org/repo admin runs it once per repo)

```bash
# Requires: gh auth login with admin on code-corhuila. Example for opti-customers-api / develop.
gh api -X PUT repos/code-corhuila/opti-customers-api/branches/develop/protection \
  -H "Accept: application/vnd.github+json" --input - <<'JSON'
{
  "required_status_checks": { "strict": true,
    "checks": [ {"context": "build-and-test"}, {"context": "integration"},
                {"context": "quality"}, {"context": "security"} ] },
  "enforce_admins": true,
  "required_pull_request_reviews": { "required_approving_review_count": 1,
                                     "dismiss_stale_reviews": true },
  "required_conversation_resolution": true,
  "restrictions": null,
  "allow_force_pushes": false,
  "allow_deletions": false
}
JSON
```

Better at org level: one **repository ruleset** targeting `develop`, `qa`, `main` in every
`opti-*` repo, so a new repo is covered on creation (Org → Settings → Rules → Rulesets).

## 3.4 Verification

- Open a PR to develop with a failing test → the merge button stays disabled ("Required
  statuses must pass"), also for an admin.
- `git push origin develop` from a clone → `remote: error: GH006: Protected branch update failed`.
- Re-run `GET /repos/code-corhuila/<repo>/branches/develop/protection` with an admin token and
  attach the JSON to the story CI-01.
