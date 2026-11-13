# Week 12 Workshop — Deployment plan, IaC and rollback on OptiView

Individual work for week 12 (cloud deployment, zero-downtime and the MVP 3 deployment plan),
applied to the OptiView team project. It covers the two "This week" activities of the guides:

- Session 1: deploy the system using the tagged image, with a zero-downtime strategy and
  health checks.
- Session 2: write the MVP 3 deployment plan (artifact promotion, IaC, zero-downtime strategy,
  rehearsed and timed rollback) and slice the deployment stories.

<!-- CONFIG-START -->
- FULL_NAME: Juan Diego Jiménez Horta
- GITHUB_USER: ItzJunixs
- TEAM: The Illusionists (OptiView)
- DOCS_REPO: https://github.com/code-corhuila/opti-docs
- INFRA_REPO: https://github.com/code-corhuila/opti-infra (private)
- SERVICE_USED_FOR_THE_REHEARSAL: opti-customers-api (ms-pacientes), behind opti-api-gateway
- EVIDENCE_DATE: 2026-11-12 (local clones updated 2026-11-11, GitHub API read 2026-11-12)
<!-- CONFIG-END -->

## Files

| # | File | What it answers | Status |
|---|---|---|---|
| 1 | `01-deployment-model.md` | Where OptiView sits on IaaS → serverless, and whether we really "build once, deploy the artifact" | done |
| 2 | `02-deployment-plan.md` | MVP 3 plan: artifact promotion, IaC inventory and drift, chosen strategy | done |
| 3 | `03-rollback-runbook.md` | Exact rollback commands (app and database) and how to verify recovery | done |
| 4 | `04-deploy-stories.md` | Deployment stories for MVP 3 with testable acceptance criteria | done |
| 5 | `deploy/` | `compose.release.yml`, `compose.green.yml`, `blue-green.sh`, `rehearse-rollback.sh`, `publish.yml` | done |
| 6 | `deploy/HOW-TO-RUN.md` | How to run the rehearsal and where its timed output is recorded | rehearsal pending a run with Docker |

## Main findings

1. The team already has real IaC: `opti-infra/compose.yml` composes the 16 `deploy/compose.yml`
   files of the sibling repos, plus Redis and the observability stack, with one `.env.*.example`
   per environment (develop / qa / main). Nothing is set up by hand.
2. We do **not** promote one artifact. Every `deploy/compose.yml` has `build:` and a local tag
   (`opti/<service>:local`), so each environment rebuilds from source. The GHCR images are only
   tagged `:latest` and only on push to `develop`.
3. The last `Publish image` run on `develop` (2026-11-10) **failed in 9 repositories**, so the
   GHCR `:latest` images do not contain the MVP 3 code (payments, PDFs).
4. `docker compose up -d` recreates a container (stop-then-start). Today every deploy has a short
   outage. The gateway is ready for something better: it resolves upstreams on every request
   (`resolver ... valid=10s`) and its routes are rendered from `*_API_URL` variables, which makes
   a blue-green switch with `nginx -s reload` possible without touching the services.
5. The database rollback is already rehearsed by CI: every `-db` repo has `05_rollbacks/U*.sql`
   and `db-ci.yml` runs `verify-rebuild.sh` (migrate, migrate again, undo everything, migrate).

## Submission links

- Fork: https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1
- Workshop: https://github.com/ItzJunixs/sistemas-distribuidos-2026-b-g1/tree/main/12-week/workshop
