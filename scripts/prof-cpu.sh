#!/usr/bin/env bash
set -euo pipefail

AP="${AP:-tools/async-profiler}"
DUR="${DUR:-30}"
TITLE="${TITLE:-cpu}"
OUT="${OUT:-profiles/cpu-${TITLE}-$(date +%Y%m%d_%H%M%S).html}"
mkdir -p profiles

if [[ -z "${PID:-}" ]]; then
  PID="$(curl -fsS http://localhost:8080/pid | sed -E 's/.*"pid":\s*([0-9]+).*/\1/')"
fi

echo "Profiling CPU: pid=${PID} dur=${DUR}s -> ${OUT}"
"${AP}/bin/asprof" -e cpu -d "${DUR}" -f "${OUT}" "${PID}"
echo "Done. Open ${OUT} in a browser."