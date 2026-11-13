# 3 · Rollback runbook

When to use it: after a deploy, the gateway shows a rise of `5xx` on a route, the place-order
SLO alert of week 11 (`PlaceOrderFastBurn`) fires, or the smoke test after the deploy fails.
Rule: **roll back first, investigate later.** Decide in under 2 minutes.

All commands run from `opti-infra` on the host of the environment, with its `.env`.

```bash
cd opti-infra
DC="docker compose --env-file .env -f compose.yml"
```

## Level 1 · Blue-green switch back (seconds) — only if blue is still running

```bash
../sistemas-distribuidos-2026-b-g1/12-week/workshop/deploy/blue-green.sh rollback customers-api
```

What it does: re-renders the gateway route with `CUSTOMERS_API_URL=http://customers-api:8080`
(blue) and runs `nginx -t && nginx -s reload`. No container is restarted.

Verify:

```bash
TOKEN=$(./scripts/dev-token.sh aaaa0001-0000-4000-8000-000000000001 10 ADMIN)   # develop only
curl -s -o /dev/null -w '%{http_code}\n' -H "Authorization: Bearer $TOKEN" http://localhost:8000/api/v1/patients   # 200
$DC exec api-gateway grep -h proxy_pass -B1 /etc/nginx/routes/customers.conf | head -2      # points to customers-api
```

## Level 2 · Redeploy the previous version (1–3 min)

Needs versioned images in GHCR (story DEP-01). Until then, use the git tag and build locally
(fallback below).

```bash
export OPTI_VERSION=v2.0.0                       # the last version that worked
$DC -f ../sistemas-distribuidos-2026-b-g1/12-week/workshop/deploy/compose.release.yml pull customers-api
$DC -f ../sistemas-distribuidos-2026-b-g1/12-week/workshop/deploy/compose.release.yml up -d --no-build customers-api
```

Fallback while images are only `:latest` (this is a rebuild, so it is slower and not the
tested artifact — the reason for DEP-01):

```bash
git -C ../opti-customers-api fetch --tags && git -C ../opti-customers-api checkout v2.0.0
$DC up -d --build customers-api
```

Verify: `docker inspect --format '{{.State.Health.Status}}' $($DC ps -q customers-api)` is
`healthy`, then the same `curl` as level 1.

## Level 3 · Undo a database migration (EMERGENCY, Tech Lead decides)

Only when the new version wrote a migration that the previous version cannot live with.
Data written with the new schema can be lost: take a dump first.

```bash
$DC exec customers-db pg_dump -U customers_owner customers > backup-$(date +%F-%H%M).sql
# Run only the U scripts with a version higher than the one we go back to, highest first:
ls ../opti-customers-db/05_rollbacks/*/*/U*.sql     # e.g. 01_ddl/03_tables/U004__create_optical_formula.sql
$DC exec -T customers-db psql -v ON_ERROR_STOP=1 -U customers_owner customers < ../opti-customers-db/05_rollbacks/<folder>/U0NN__<name>.sql
# Then delete that version's row from customers.flyway_schema_history (the history table lives in
# the domain schema, see flyway.toml) so the next migrate is consistent.
# (Names and users are the defaults of .env.*.example: customers / customers_owner.)
```

Why we trust these scripts: every PR to a `-db` repo runs `scripts/verify-rebuild.sh` in
`db-ci.yml` (migrate from empty → migrate again must apply nothing → run all `U*.sql` in
reverse → migrate again). A `U` script that does not undo its `V` breaks CI.

## After the rollback

1. Post in the team channel: version reverted, time, symptom, `traceId` of a failed request.
2. Open an issue `[incident]` in opti-docs with the timeline (template: week 11 `08-incident.md`).
3. The bad version is fixed forward with a new tag (`v3.0.1`); never re-deploy the same tag.

## Rehearsal record

| Date | Environment | Level | Measured time | Errors seen by the client during switch / rollback | Run by |
|---|---|---|---|---|---|
| _pending_ | develop (laptop) | 1 | see `deploy/rehearsal-output.txt` | see `deploy/rehearsal-output.txt` | ItzJunixs |
