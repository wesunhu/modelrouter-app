#!/bin/bash
# ModelRouter 构建脚本 (macOS / Linux)
# SQLite 单机模式 + 前端打包

set -e
cd "$(dirname "$0")"

echo "========================================"
echo " ModelRouter Build"
echo " SQLite + Frontend"
echo "========================================"
echo ""

# 1. 构建前端
echo "[1/3] Building frontend..."
cd frontend
npm install
npm run build
cd ..
cp -f LEGAL.md LEGAL.en.md LEGAL.ja.md frontend/dist/ 2>/dev/null || true

# 2. 构建后端
echo "[2/3] Building backend..."
cd backend
if command -v mvn &> /dev/null; then
  mvn package -DskipTests -DskipFrontendCopy=false
else
  ./mvnw package -DskipTests -DskipFrontendCopy=false
fi
cd ..

# 3. 复制 jar
echo "[3/3] Copying jar..."
JAR="backend/target/modelrouter-backend-0.1.0-preview.1.jar"
if [ -f "$JAR" ]; then
  cp "$JAR" modelrouter.jar
  echo "Done: modelrouter.jar"
else
  echo "Jar not found. Check backend/target/"
fi

echo ""
echo "========================================"
echo " Build Complete"
echo "========================================"
echo ""
echo "Run: ./start.sh"
echo "Or:  java -jar modelrouter.jar (backend only)"
echo ""
