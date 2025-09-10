#!/usr/bin/env bash
set -euo pipefail

AP="${AP:-tools/async-profiler}"
DUR="${DUR:-30}"
TITLE="${TITLE:-alloc}"
OUT="${OUT:-profiles/alloc-${TITLE}-$(date +%Y%m%d_%H%M%S).html}"
ALLOC="${ALLOC:-16k}"
LIVE="${LIVE:-false}"

mkdir -p profiles

if [[ -z "${PID:-}" ]]; then
  PID="$(curl -fsS http://localhost:8080/pid | sed -E 's/.*"pid":\s*([0-9]+).*/\1/')"
fi

echo "Profiling allocations: pid=$PID dur=${DUR}s alloc=${ALLOC} live=${LIVE} -> $OUT"

ARGS=(-e alloc --alloc "${ALLOC}" -d "${DUR}" -f "$OUT")
if [[ "${LIVE}" == "true" ]]; then
  ARGS+=(--live)
fi

"${AP}/bin/asprof" "${ARGS[@]}" "$PID"

echo "Done. Open $OUT in a browser."