#!/usr/bin/env bash
# Blue-green switch of customers-api behind opti-api-gateway (02-deployment-plan.md §3).
#
#   blue-green.sh deploy    # start green, wait healthy, smoke it, switch the gateway to green
#   blue-green.sh rollback  # switch the gateway back to blue (blue was never stopped)
#   blue-green.sh cleanup   # stop and remove green (only once the gateway points to blue)
#   blue-green.sh status    # where the gateway points and the health of blue/green
#
# Env: INFRA_DIR (path to opti-infra, default ~/Documents/optiview-repos/opti-infra),
#      GREEN_IMAGE (default opti/customers-api:local), HEALTH_TIMEOUT (seconds, default 90).
#
# The switch re-renders the gateway routes (its own docker-entrypoint.d/15-render-routes.sh) with
# CUSTOMERS_API_URL pointing to the other colour and runs `nginx -t && nginx -s reload`: the old
# nginx workers finish the requests they hold, the new ones use the new upstream. Nothing restarts.
# It is a runtime switch: if the gateway container is recreated it renders the defaults (blue).
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
INFRA_DIR="${INFRA_DIR:-$HOME/Documents/optiview-repos/opti-infra}"
HEALTH_TIMEOUT="${HEALTH_TIMEOUT:-90}"
BLUE_URL="http://customers-api:8080"
GREEN_URL="http://customers-api-green:8080"

[[ -f "$INFRA_DIR/compose.yml" && -f "$INFRA_DIR/.env" ]] || {
  echo "INFRA_DIR=$INFRA_DIR has no compose.yml or .env (start the platform with scripts/up.sh first)" >&2; exit 1; }

dc() { docker compose --env-file "$INFRA_DIR/.env" -f "$INFRA_DIR/compose.yml" -f "$HERE/compose.green.yml" "$@"; }
log() { printf '%s  %s\n' "$(date +%H:%M:%S.%3N)" "$*"; }

health_of() { # health_of SERVICE -> healthy | starting | unhealthy | absent
  local id; id="$(dc ps -q "$1" 2>/dev/null || true)"
  [[ -n "$id" ]] || { echo absent; return; }
  docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}no-healthcheck{{end}}' "$id"
}

switch_gateway_to() { # switch_gateway_to URL
  dc exec -T api-gateway sh -c \
    "CUSTOMERS_API_URL='$1' /docker-entrypoint.d/15-render-routes.sh && nginx -t -q && nginx -s reload"
}

gateway_target() {
  dc exec -T api-gateway sh -c "grep -ho 'set \$customers_api [^;]*' /etc/nginx/routes/customers.conf | head -1 | awk '{print \$3}'"
}

case "${1:-}" in
  deploy)
    log "starting customers-api-green (${GREEN_IMAGE:-opti/customers-api:local})"
    dc up -d --no-build customers-api-green
    log "waiting for green to be healthy (max ${HEALTH_TIMEOUT}s)"
    for ((i = 0; i < HEALTH_TIMEOUT; i++)); do
      [[ "$(health_of customers-api-green)" == healthy ]] && break
      sleep 1
    done
    if [[ "$(health_of customers-api-green)" != healthy ]]; then
      log "green is $(health_of customers-api-green) after ${HEALTH_TIMEOUT}s: NOT switching, the gateway stays on blue"
      exit 1
    fi
    log "green healthy; smoke from inside the network"
    dc exec -T api-gateway wget -qO- "$GREEN_URL/health" >/dev/null
    log "switching the gateway to green"
    switch_gateway_to "$GREEN_URL"
    log "gateway now points to $(gateway_target)"
    ;;
  rollback)
    log "switching the gateway back to blue"
    switch_gateway_to "$BLUE_URL"
    log "gateway now points to $(gateway_target)"
    ;;
  cleanup)
    [[ "$(gateway_target)" == "$BLUE_URL" ]] || { log "the gateway still points to green: run rollback first"; exit 1; }
    dc rm -s -f customers-api-green
    log "green removed"
    ;;
  status)
    echo "gateway -> $(gateway_target)"
    echo "blue  customers-api        $(health_of customers-api)"
    echo "green customers-api-green  $(health_of customers-api-green)"
    ;;
  *)
    sed -n '2,8p' "$0"; exit 2 ;;
esac
