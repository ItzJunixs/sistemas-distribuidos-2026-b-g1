<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       09-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 09

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Juan Diego Jiménez Horta
- GITHUB_USER: ItzJunixs
- TEAM: The illusionists
- SPRINT_GOAL: Apply Week 9's hardening material (config/secrets/feature flags, progressive delivery) as this week's hands-on activity — not just review it: audit OptiView's real config (`optiview-platform`) for the exact mistakes the material warns about, and produce an `.env.example` plus a secrets plan, feature-flag policy, and canary/rollback plan for the actual Corte 2 feature (HU-08 completion), kept here as personal weekly evidence.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| Material review | Session 1 — Configuration, secrets and feature flags | done | https://code-corhuila.github.io/ova-web/2026-B/distribuidos/09-week/01-session/ |
| Material review | Session 2 — Planning: secure config and progressive delivery | done | https://code-corhuila.github.io/ova-web/2026-B/distribuidos/09-week/02-session/ |
| Applied — `.env.example` | Document OptiView's real required config vars (Session 1's startup/secrets guidance) | done | `09-week/hu-status/.env.example` |
| Applied — hardening plan | Secrets plan, feature-flag policy, and canary/rollback plan for HU-08 (Session 2's "This week" deliverable) | done | `09-week/hu-status/secrets-and-release-plan.md` |

## 2. My individual contribution
- Reviewed Session 1 (12-factor config strictly by environment, secrets kept out of git and rotated via a secret store, failing fast on missing/malformed config at startup, and feature flags to decouple deploy from release) and Session 2 (writing a real secrets plan, feature-flag policy, and canary/rollback plan) and completed both self-checks.
- Audited OptiView's actual backend config (`application.yml`, `docker-compose.yml` in `optiview-platform`) against Session 1's "common mistakes" list and found a real instance of the exact problem it warns about: both files hard-code a fallback JWT secret and DB password, so a missing environment variable silently starts the service with a known, committed secret instead of failing fast.
- Wrote `09-week/hu-status/.env.example` documenting every variable the backend actually reads, with no real values, as this week's hands-on exercise.
- Wrote `09-week/hu-status/secrets-and-release-plan.md`: a secrets-ownership/rotation table, a feature-flag naming/removal policy, and a stage-by-stage canary + rollback plan for the real Corte 2 feature (completing HU-08 — lenses, treatments, formula validation), plus 3 sliced hardening stories with testable acceptance criteria.
- Kept this entirely inside my personal weekly evidence: did not change `optiview-platform`'s actual config and did not touch the team's shared `opti-docs` repo — this was a personal exercise, not a team deliverable.

## 3. Blockers and risks
- The actual fail-fast fix (removing the hard-coded secret defaults in `optiview-platform`) is still open — documented as hardening story #1 in `secrets-and-release-plan.md`, not implemented here since it's a real behavior change to a shared repo, not a personal weekly exercise.
- Nobody owns secret rotation yet for OptiView; the plan documents that gap rather than resolving it unilaterally.

## 4. Plan for next week
- If the team agrees, propose `optiview-platform`'s fail-fast fix (remove the hard-coded `APP_JWT_SECRET`/`SPRING_DATASOURCE_PASSWORD` defaults) as its own reviewable PR, per hardening story #1.
- Raise secret-rotation ownership with the team.
- Reference this plan's canary/flag approach when HU-08's lens/treatment work actually starts.

## 5. Compliance self-check
- [ ] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [ ] Testable acceptance criteria
- [ ] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [ ] No secrets; config via environment variables

## 6. Evidence links
- https://code-corhuila.github.io/ova-web/2026-B/distribuidos/09-week/01-session/
- https://code-corhuila.github.io/ova-web/2026-B/distribuidos/09-week/02-session/
- `09-week/hu-status/.env.example`
- `09-week/hu-status/secrets-and-release-plan.md`
