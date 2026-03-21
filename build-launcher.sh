#!/usr/bin/env bash
# Builds modelrouter-launcher.jar (Java 17, same as backend). Requires JDK 17+ and Maven or backend/mvnw.
set -euo pipefail
cd "$(dirname "$0")"

echo "========================================"
echo " ModelRouter Java launcher build"
echo "========================================"
echo

cd launcher-java
if command -v mvn >/dev/null 2>&1; then
  mvn -q package
else
  ../backend/mvnw -q package
fi

OUT="target/modelrouter-launcher-1.0.1.jar"
if [[ ! -f "$OUT" ]]; then
  echo "ERROR: Output jar not found: launcher-java/${OUT}" >&2
  exit 1
fi

cp -f "$OUT" ../modelrouter-launcher.jar
cd ..
echo "Done: modelrouter-launcher.jar"
echo
echo "Run: java -jar modelrouter-launcher.jar"
echo "  or: ./launcher.sh"
