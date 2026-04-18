#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

if [ -n "${JAR_PATH:-}" ]; then
  JAR="${JAR_PATH}"
elif [ "${1:-}" != "" ]; then
  JAR="$1"
else
  JAR=$(find "${REPO_ROOT}/services/booking-service/target" -maxdepth 1 -name "booking-service-*-SNAPSHOT.jar" 2>/dev/null | head -n 1)
fi

if [ -z "${JAR}" ] || [ ! -f "${JAR}" ]; then
  echo "Error: JAR not found. Build a module (e.g. mvn -pl services/booking-service -am package) or pass a path:" >&2
  echo "  JAR_PATH=/path/to/app.jar ${0##*/}" >&2
  echo "  ${0##*/} /path/to/app.jar" >&2
  exit 1
fi

echo "Running JAR: ${JAR}"
exec java -Xms2g -Xmx2g -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=50 \
  -XX:InitiatingHeapOccupancyPercent=30 \
  -XX:G1ReservePercent=20 \
  '-Xlog:gc*,gc+heap=debug:file=gc-shorter-pauses-%t.log:time,uptime,level,tags' \
  -jar "${JAR}"
