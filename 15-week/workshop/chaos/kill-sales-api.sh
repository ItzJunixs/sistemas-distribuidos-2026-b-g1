#!/usr/bin/env bash
# Chaos experiment on the place-order saga: stop sales-api (the participant of the LAST step,
# open-order) while orders are placed, and observe how OptiView degrades and recovers.
# Design and hypotheses: ../03-chaos.md.
#
#   INFRA_DIR=~/Documents/optiview-repos/opti-infra ./kill-sales-api.sh [short|long|both]
#
#   short  sales-api down 20 s  -> H1: the saga waits (RUNNING) and COMPLETES when it comes back
#   long   sales-api down 150 s -> H2: after SAGA_MAX_ATTEMPTS the saga ends FAILED; check whether
#                                  the frame reserved in reserve-stock is released or left RESERVED
# In both: H3: patients and frames keep answering 200 through the gateway; work orders answer 503.
#
# Writes everything to chaos-output.txt next to this script. Needs the platform up and migrated
# (develop, demo data), curl and jq (Git Bash: winget install jqlang.jq).
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
INFRA_DIR="${INFRA_DIR:-$HOME/Documents/optiview-repos/opti-infra}"
GW="${GATEWAY_URL:-http://localhost:8000}"
PASSWORD="${DEMO_PASSWORD:-OptiView2026}"
PATIENT="${PATIENT_ID:-11111111-1111-4111-8111-111111111111}"
MODE="${1:-both}"
OUT="$HERE/chaos-output.txt"

dc() { docker compose --env-file "$INFRA_DIR/.env" -f "$INFRA_DIR/compose.yml" "$@"; }
log() { printf '%s  %s\n' "$(date +%H:%M:%S)" "$*"; }
uuid() { printf '%04x%04x-%04x-4%03x-%04x-%04x%04x%04x\n' $RANDOM $RANDOM $RANDOM $((RANDOM & 0xfff)) \
          $(( (RANDOM & 0x3fff) | 0x8000 )) $RANDOM $RANDOM $RANDOM; }

exec > >(tee -a "$OUT") 2>&1
echo; echo "# Chaos: stop sales-api during place-order — $(date -Iseconds) — mode=$MODE"

login() { curl -sf -X POST "$GW/api/v1/auth/login" -H 'Content-Type: application/json' \
            -d "{\"username\":\"$1\",\"password\":\"$PASSWORD\"}" | jq -r .accessToken; }
ADMIN="$(login admin)"; SELLER="$(login seller)"

code() { curl -s -o /dev/null -w '%{http_code}' -H "Authorization: Bearer $ADMIN" "$GW$1"; }
probe() { log "probe: patients=$(code /api/v1/patients) frames=$(code /api/v1/frames) work-orders=$(code /api/v1/work-orders)"; }

# A frame only for this experiment (stock 10) so the numbers are easy to read.
FRAME="$(curl -sf -X POST "$GW/api/v1/frames" -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' \
  -H "Idempotency-Key: $(uuid)" -d "{\"sku\":\"CHAOS-$(date +%s)\",\"brand\":\"Chaos\",\"model\":\"Monkey\",\"costCents\":10000000,\"salePriceCents\":20000000,\"stock\":10,\"minStock\":0}" | jq -r .id)"
stock() { curl -sf -H "Authorization: Bearer $ADMIN" "$GW/api/v1/frames/$FRAME" | jq -r .stock; }
reserved() { dc exec -T products-db psql -U products_owner -d products -Atc \
  "select count(*) from products.stock_reservation where frame_id='$FRAME' and status='RESERVED'"; }
log "frame $FRAME created: stock=$(stock) reserved=$(reserved)"

place_order() { curl -s -X POST "$GW/api/v1/sagas/place-order" -H "Authorization: Bearer $SELLER" \
  -H 'Content-Type: application/json' -H "Idempotency-Key: $(uuid)" -H "X-Correlation-Id: chaos-$(date +%s)" \
  -d "{\"patientId\":\"$PATIENT\",\"productType\":\"FRAME\",\"productId\":\"$FRAME\",\"quantity\":1}"; }
saga() { curl -s -H "Authorization: Bearer $SELLER" "$GW/api/v1/sagas/$1" | jq -c '{status,failedStep,failureReason,completedSteps,orderId}'; }

experiment() { # experiment NAME DOWN_SECONDS
  local name="$1" down="$2" id t0 final=""
  echo; log "== experiment $name: sales-api down for ${down}s"
  log "before: stock=$(stock) reserved=$(reserved)"
  dc stop sales-api >/dev/null; t0=$(date +%s); log "sales-api stopped"
  probe
  id="$(place_order | tee /dev/stderr | jq -r .id)"; echo
  log "saga $id right after the request: $(saga "$id")"
  while (( $(date +%s) - t0 < down )); do sleep 10; log "saga: $(saga "$id")"; done
  dc start sales-api >/dev/null; log "sales-api started again"
  for _ in $(seq 1 24); do   # up to 4 min for the resume loop (SAGA_RESUME_EVERY=PT30S)
    final="$(saga "$id")"; [[ "$final" == *'"COMPLETED"'* || "$final" == *'"COMPENSATED"'* || "$final" == *'"FAILED"'* ]] && break
    sleep 10
  done
  log "saga final: $final"
  sleep 15; probe
  log "after: stock=$(stock) reserved=$(reserved)"
}

[[ "$MODE" == short || "$MODE" == both ]] && experiment short 20
[[ "$MODE" == long  || "$MODE" == both ]] && experiment long 150
echo; log "done. Compare with the hypotheses in 03-chaos.md"
