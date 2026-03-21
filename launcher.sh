#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

JAR="modelrouter-launcher.jar"
if [[ ! -f "$JAR" ]]; then
  echo "[ERROR] 未找到 $JAR" >&2
  echo "请先运行 ./build-launcher.sh（或已安装 Maven 时在 launcher-java 下执行 mvn package）。" >&2
  exit 1
fi

exec java -jar "$JAR" "$@"
