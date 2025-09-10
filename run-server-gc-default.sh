#!/usr/bin/env bash
set -euo pipefail

JAR=$(find advanced-topics/target -name "advanced-topics-*-SNAPSHOT.jar" | head -n 1)

if [ -z "${JAR}" ]; then
    echo "Error: Could not find the JAR file in advanced-topics/target." >&2
    echo "Please run 'mvn clean package' first." >&2
    exit 1
fi

echo "Running JAR: ${JAR}"
exec java -Xms2g -Xmx2g -XX:+UseG1GC \
  '-Xlog:gc*,gc+heap=debug:file=gc-baseline-%t.log:time,uptime,level,tags' \
  -jar "${JAR}"