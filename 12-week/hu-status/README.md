<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       12-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 12

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Juan Diego Jiménez Horta
- GITHUB_USER: ItzJunixs
- TEAM: The Illusionists
- SPRINT_GOAL: Write the MVP 3 deployment plan of OptiView (artifact promotion, IaC audit, zero-downtime strategy and a timed rollback) on top of what opti-infra already runs, and slice the deployment stories with testable acceptance criteria.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| W12-01 | Deployment model of OptiView and "build once, deploy the artifact" audit | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/blob/main/12-week/workshop/01-deployment-model.md |
| W12-02 | MVP 3 deployment plan: artifact promotion, IaC inventory and drift, zero-downtime strategy | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/blob/main/12-week/workshop/02-deployment-plan.md |
| W12-03 | Rollback runbook (gateway switch, previous tag, database U scripts) | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/blob/main/12-week/workshop/03-rollback-runbook.md |
| W12-04 | Deployment stories DEP-01..DEP-07 with Given/When/Then criteria | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/blob/main/12-week/workshop/04-deploy-stories.md |
| W12-05 | Deploy kit: versioned publish workflow, release override, blue-green script for customers-api | done | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/tree/main/12-week/workshop/deploy |
| W12-06 | Timed rehearsal of blue-green deploy + rollback with a client polling through the gateway | doing | https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/blob/main/12-week/workshop/deploy/HOW-TO-RUN.md |

## 2. My individual contribution
- Placed OptiView on the IaaS → serverless spectrum: one Docker host per environment with Compose as orchestrator, and a gateway already prepared for a PaaS (Render) through `*_API_URL` and `NGINX_RESOLVER`.
- Found that we do not promote one artifact: every `deploy/compose.yml` has `build:` and a `:local` tag, GHCR only gets `:latest`, and the `Publish image` run of 2026-11-10 failed in 9 repos (the job ended in ~1 s with no runner), so GHCR does not have the MVP 3 code.
- Listed the IaC we already have (opti-infra `include:` of 16 compose files, `.env` per environment, Flyway + `U*.sql` rollbacks proved by `db-ci.yml`) and five drift points (rebuild per host, no commit label, `|| true` in opti-infra CI, docs describing Kubernetes and `ms-*`, our own images not pinned).
- Chose blue-green at the gateway for the stateless APIs: `blue-green.sh` starts a green instance, waits for its `HEALTHCHECK`, re-renders the nginx route and reloads without restarting; rollback is the same switch back.
- Wrote a `publish.yml` that builds once (`sha-*` + OCI revision label) and promotes by retag (`imagetools create`) to qa/main/`v*`; it passes actionlint.

## 3. Blockers and risks
- GitHub Actions jobs are not starting in the org since 2026-11-06 (runs end in ~1 s, no steps), so no image can be published until that is fixed.
- I could not run the rehearsal in the environment where I prepared the kit (no Docker there); it has to run on my laptop with the platform up, and the output goes to `deploy/rehearsal-output.txt`.
- `opti-workflow` and `opti-worker` call `customers-api` directly, so the gateway switch only moves user traffic (DEP-04 has the alias step).

## 4. Plan for next week
- Run `rehearse-rollback.sh` and record the times and the non-200 count in the runbook.
- Bring DEP-01 and DEP-07 (P0) to the team board and propose `publish.yml` in one repo first (opti-customers-api).
- Map the Definition of Done to pipeline gates (week 14).

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [x] Testable acceptance criteria
- [ ] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

## 6. Evidence links
- Workshop folder: https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/tree/main/12-week/workshop
- Failed publish run (no runner, ~1 s): https://github.com/code-corhuila/opti-customers-api/actions/runs/42923849949
- Infra composition: `opti-infra/compose.yml`, `opti-infra/env/.env.{develop,qa,main}.example`
- DB rollback proof in CI: `opti-customers-db/scripts/verify-rebuild.sh`, `.github/workflows/db-ci.yml`
