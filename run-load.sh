#!/usr/bin/env bash
set -euo pipefail

RPS=${RPS:-1000}
DUR=${DUR:-30s}
URL=${URL:-http://localhost:8080/count}
SRC=${SRC:-.}
WORKERS=${WORKERS:-64}
MODE=${MODE:-pool}

JAR=$(find load-generator/target -name "load-generator-*-SNAPSHOT.jar" | head -n 1)

if [ -z "${JAR}" ]; then
    echo "Error: Could not find the JAR file in load-generator/target." >&2
    echo "Please run 'mvn clean package' first." >&2
    exit 1
fi

echo "Running Load Generator: ${JAR}"
exec java \
  -Drps="${RPS}" \
  -Dduration="${DUR}" \
  -Durl="${URL}" \
  -Dsource="${SRC}" \
  -Dworkers="${WORKERS}" \
  -Dmode="${MODE}" \
  -jar "${JAR}"