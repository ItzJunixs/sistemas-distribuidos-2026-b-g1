#!/usr/bin/env bash
# Runs place-order-load.js with the k6 Docker image inside the platform network, so it reaches the
# gateway by its service name, and saves the console output and the JSON summary next to this script.
#
#   ./run-load.sh            (platform up and migrated from opti-infra, develop demo data)
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
cd "$HERE"
# Git Bash on Windows: keep /work as a container path and give Docker a Windows path for the mount.
export MSYS_NO_PATHCONV=1
src="$(pwd -W 2>/dev/null || pwd)"

docker run --rm --network platform \
  -e BASE_URL="${BASE_URL:-http://api-gateway:8000}" -e DEMO_PASSWORD="${DEMO_PASSWORD:-OptiView2026}" \
  -v "$src:/work" -w /work grafana/k6:0.54.0 \
  run --summary-export=load-summary.json place-order-load.js 2>&1 | tee load-output.txt
