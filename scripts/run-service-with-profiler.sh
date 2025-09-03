#!/usr/bin/env bash
set -euo pipefail

JAR=$(find advanced-topics/target -name "advanced-topics-*-SNAPSHOT.jar" | head -n 1)
if [[ -z "${JAR}" ]]; then
    echo "Error: build first (mvn -q -DskipTests -pl advanced-topics -am package)" >&2
    exit 1
fi

echo "Running JAR for profiling: ${JAR}"
exec java \
  -Xms2g -Xmx2g \
  -XX:+UseG1GC \
  -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints \
  '-Xlog:gc*,gc+heap=debug,safepoint:file=gc-baseline-%t.log:time,uptime,level,tags' \
  -jar "${JAR}"