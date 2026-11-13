# Deploy kit — how to run it

| File | Goes to | Purpose |
|---|---|---|
| `publish.yml` | `.github/workflows/publish.yml` of each executable `opti-*` repo | build once on develop (`sha-*`), promote by retag on qa/main/`v*` |
| `compose.release.yml` | `opti-infra/` (or run from here with `-f`) | hosts pull the CI image for `OPTI_VERSION`; no build on the host |
| `compose.green.yml` | used by `blue-green.sh` | second instance of customers-api with the new image |
| `blue-green.sh` | runs on the Docker host | deploy / rollback / cleanup / status of the gateway switch |
| `rehearse-rollback.sh` | runs on the Docker host | timed rehearsal with a client polling through the gateway |

## Requirements

Docker Desktop, Git Bash, the repos cloned as siblings in `Documents/optiview-repos`, and the
platform started and migrated as in `opti-infra/README.md` (develop `.env`, `SEED_DEMO_DATA=true`):

```bash
cd ~/Documents/optiview-repos/opti-infra
cp env/.env.develop.example .env && ./scripts/dev-keys.sh && ./scripts/up.sh
for d in auth customers products sales; do docker compose --env-file .env -f compose.yml run --rm $d-db-migrate; done
```

## Rehearsal (DEP-04 and DEP-07)

```bash
cd ~/OneDrive/Documentos/sistemas-distribuidos-2026-b-g1/12-week/workshop/deploy
INFRA_DIR=~/Documents/optiview-repos/opti-infra ./rehearse-rollback.sh
```

Output goes to `rehearsal-output.txt` (committed as evidence). What to read in it:

- `rollback (decision -> on blue)` must be far below 300 s.
- `non-200` in `during deploy` and `during rollback` must be `0` for zero downtime.
  A `502` there means nginx kept a connection to the old upstream; a `401` means the token
  expired (rerun).

## Notes and limits

- `publish.yml` promotion finds the image of the commit or of its second parent (merge commit of
  a PR). If a `qa-*` branch carries a commit that never went through `develop` (for example
  `7610564 ... [skip ci]` on `opti-sales-db` `qa`), promotion **fails on purpose**: that commit has
  no tested image. Same for a `v*` tag on a commit created only on `main`.
- The gateway switch is a runtime change. If `api-gateway` is recreated it renders its defaults
  (blue). To make green permanent, the next deploy recreates blue with the new image and switches
  back, or the gateway compose gets `CUSTOMERS_API_URL` from `.env`.
- `opti-workflow` and `opti-worker` call `customers-api` directly; see `02-deployment-plan.md` §3.
