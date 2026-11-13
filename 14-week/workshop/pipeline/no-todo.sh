#!/usr/bin/env bash
# Fails when production code has TODO / FIXME / XXX markers that are not linked to an issue
# (DoD: "no technical debt introduced without registering it").
#
#   no-todo.sh [dir ...]      default: every */src/main of a Maven repo, or src/ of a portal
#
# Allowed: a marker followed by an issue reference, e.g.  // TODO(#42) ...  or
#          // TODO https://github.com/code-corhuila/opti-docs/issues/42 ...
# Tests are not scanned: a TODO in a test is visible in review and does not ship.
set -euo pipefail

dirs=("$@")
if [[ ${#dirs[@]} -eq 0 ]]; then
  mapfile -t dirs < <(find . -path ./node_modules -prune -o -type d -path '*/src/main' -print)
  [[ ${#dirs[@]} -eq 0 && -d src ]] && dirs=(src)
fi
[[ ${#dirs[@]} -gt 0 ]] || { echo "no source directories found"; exit 0; }

found="$(grep -rnIE '\b(TODO|FIXME|XXX)\b' "${dirs[@]}" \
  --exclude-dir=node_modules --exclude-dir=test --exclude-dir=__tests__ \
  --exclude='*.test.*' --exclude='*.spec.*' \
  | grep -vE '\b(TODO|FIXME|XXX)(\(#[0-9]+\)| +https://github\.com/[^ ]+/issues/[0-9]+)' || true)"

if [[ -n "$found" ]]; then
  echo "Unlinked TODO/FIXME/XXX in production code (link an issue or resolve it):"
  echo "$found"
  exit 1
fi
echo "no unlinked TODO/FIXME/XXX in: ${dirs[*]}"
