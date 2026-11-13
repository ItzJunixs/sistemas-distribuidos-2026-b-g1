#!/usr/bin/env bash
# Timed rehearsal of a blue-green deploy and its rollback, with a client polling through the
# gateway the whole time. Proves (or disproves) zero downtime and the "< 5 min" rollback target.
#
# Needs: the platform up and migrated from opti-infra (develop, SEED_DEMO_DATA=true), curl, Git Bash.
#   INFRA_DIR=~/Documents/optiview-repos/opti-infra ./rehearse-rollback.sh
# Writes the full log to rehearsal-output.txt next to this script.
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
export INFRA_DIR="${INFRA_DIR:-$HOME/Documents/optiview-repos/opti-infra}"
GATEWAY="${GATEWAY_URL:-http://localhost:8000}"
HOLD_SECONDS="${HOLD_SECONDS:-15}"      # time green serves traffic before the "bad release" is rolled back
OUT="$HERE/rehearsal-output.txt"
POLL="$(mktemp)"; STOP="$(mktemp -u)"
ms() { date +%s%3N; }

exec > >(tee "$OUT") 2>&1
echo "# Rehearsal of blue-green deploy + rollback — customers-api — $(date -Iseconds)"
echo "# host: $(uname -sr) · docker: $(docker version --format '{{.Server.Version}}' 2>/dev/null)"

# Admin token of the seeded admin user (develop only: dev-token.sh signs with the local dev key).
TOKEN="$(cd "$INFRA_DIR" && ./scripts/dev-token.sh aaaa0001-0000-4000-8000-000000000001 30 ADMIN)"
code="$(curl -s -o /dev/null -w '%{http_code}' -H "Authorization: Bearer $TOKEN" "$GATEWAY/api/v1/patients")"
[[ "$code" == 200 ]] || { echo "precondition failed: GET /api/v1/patients answered $code (is the platform up and migrated?)"; exit 1; }

# Client: one request every 200 ms, records "epoch_ms status".
(
  while [[ ! -e "$STOP" ]]; do
    printf '%s %s\n' "$(ms)" "$(curl -s -o /dev/null -m 5 -w '%{http_code}' \
      -H "Authorization: Bearer $TOKEN" -H "X-Correlation-Id: rehearsal-$(ms)" "$GATEWAY/api/v1/patients")" >> "$POLL"
    sleep 0.2
  done
) &
POLLER=$!
trap 'touch "$STOP"; wait $POLLER 2>/dev/null || true' EXIT
sleep 3

t0=$(ms); echo; echo "== 1. deploy green";   "$HERE/blue-green.sh" deploy;   t1=$(ms)
echo "   holding ${HOLD_SECONDS}s on green (the 'bad release' we will undo)"; sleep "$HOLD_SECONDS"
t2=$(ms); echo; echo "== 2. rollback to blue"; "$HERE/blue-green.sh" rollback; t3=$(ms)
sleep 3
echo; echo "== 3. cleanup";            "$HERE/blue-green.sh" cleanup
touch "$STOP"; wait $POLLER || true

phase() { # phase FROM TO NAME
  awk -v a="$1" -v b="$2" -v n="$3" '$1>=a && $1<=b {t++; if ($2!="200") {bad++; codes[$2]++}}
    END {printf "%-26s requests=%-4d non-200=%-3d", n, t, bad+0; for (c in codes) printf " [%s x%d]", c, codes[c]; print ""}' "$POLL"
}
echo
echo "== Result"
awk -v a="$t0" -v b="$t1" 'BEGIN {printf "deploy (start green -> switch):   %6.1f s\n", (b-a)/1000}'
awk -v a="$t2" -v b="$t3" 'BEGIN {printf "rollback (decision -> on blue):   %6.1f s   target < 300 s\n", (b-a)/1000}'
phase "$t0" "$t1" "during deploy"
phase "$t1" "$t2" "serving on green"
phase "$t2" "$t3" "during rollback"
phase 0 99999999999999 "whole rehearsal"
"$HERE/blue-green.sh" status
