# How to run the week 15 tests

Requirements: Docker Desktop, Git Bash, `jq` (`winget install jqlang.jq`), the repos as siblings
in `Documents/optiview-repos`, the platform up and migrated with demo data:

```bash
cd ~/Documents/optiview-repos/opti-infra
./scripts/up.sh
for d in auth customers products sales; do docker compose --env-file .env -f compose.yml run --rm $d-db-migrate; done
```

Run in this order (each one leaves its output file next to its script, commit them as evidence):

```bash
cd ~/OneDrive/Documentos/sistemas-distribuidos-2026-b-g1/15-week/workshop

# 1. Load (~4 min) -> load/load-output.txt, load/load-summary.json
./load/run-load.sh

# 2. Chaos (~6 min: short 20 s + long 150 s outage) -> chaos/chaos-output.txt
INFRA_DIR=~/Documents/optiview-repos/opti-infra ./chaos/kill-sales-api.sh both

# 3. Rollback rehearsal of week 12 -> 12-week/workshop/deploy/rehearsal-output.txt
INFRA_DIR=~/Documents/optiview-repos/opti-infra ../../12-week/workshop/deploy/rehearse-rollback.sh

# 4. (optional, for the demo) e2e journey with failure injection
cd ~/Documents/optiview-repos/opti-e2e && npm run demo:failure
```

After running, fill the "Result" tables of `02-load-test.md`, `03-chaos.md` and the rehearsal
row of `12-week/workshop/03-rollback-runbook.md`, and change the matching rows of the hu-status
files from `doing` to `done`.

If a script fails at the start with `login … -> 401`, the demo passwords were changed
(`opti-auth-db` branch `fix/disable-fixed-demo-credentials`): pass `DEMO_PASSWORD=…`.
