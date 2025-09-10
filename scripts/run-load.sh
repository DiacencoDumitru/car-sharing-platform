#!/usr/bin/env bash
set -euo pipefail

RPS=${RPS:-800}
DUR=${DUR:-30s}
URL=${URL:-http://localhost:8080/count}
SRC=${SRC:-./advanced-topics/src/main/java}
EXT=${EXT:-txt,md,java}
WORKERS=${WORKERS:-64}
MODE=${MODE:-pool}

JAR=$(find load-generator/target -name "load-generator-*-SNAPSHOT.jar" | head -n 1)
if [[ -z "${JAR}" ]]; then
    echo "Error: build first (mvn -q -DskipTests -pl load-generator -am package)" >&2
    exit 1
fi

echo "Running Load Generator: ${JAR}"
exec java \
  -Drps="${RPS}" \
  -Dduration="${DUR}" \
  -Durl="${URL}" \
  -Dsource="${SRC}" \
  -Dext="${EXT}" \
  -Dworkers="${WORKERS}" \
  -Dmode="${MODE}" \
  -jar "${JAR}"