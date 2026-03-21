#!/bin/bash
# ModelRouter 启动脚本 (Linux / macOS)
# 分别启动前端(20119)与后端(20118)，支持参数指定端口

set -e
cd "$(dirname "$0")"

FRONTEND_PORT="${1:-20119}"
BACKEND_PORT="${2:-20118}"

echo "========================================"
echo " ModelRouter"
echo " Frontend: http://localhost:$FRONTEND_PORT"
echo " Backend:  http://localhost:$BACKEND_PORT"
echo "========================================"
echo ""

# 查找 jar
JAR=""
if [ -f "modelrouter.jar" ]; then
  JAR="modelrouter.jar"
elif [ -f "modelrouter-backend.jar" ]; then
  JAR="modelrouter-backend.jar"
else
  JAR=$(ls backend/target/modelrouter-backend-*.jar 2>/dev/null | grep -v SNAPSHOT | head -1)
fi

if [ -z "$JAR" ] || [ ! -f "$JAR" ]; then
  echo "[ERROR] modelrouter.jar not found. Run build-unix.sh first."
  exit 1
fi

# 查找前端 dist
DIST=""
if [ -d "frontend/dist" ] && [ -f "frontend/dist/index.html" ]; then
  DIST="frontend/dist"
elif [ -d "backend/target/classes/static" ] && [ -f "backend/target/classes/static/index.html" ]; then
  DIST="backend/target/classes/static"
fi

if [ -z "$DIST" ]; then
  echo "[ERROR] Frontend dist not found. Build frontend first."
  exit 1
fi

# 确保 data 目录
mkdir -p data

# 启动后端 (后台)
echo "[1/2] Starting backend on port $BACKEND_PORT..."
java -jar "$JAR" --spring.profiles.active=sqlite --server.port="$BACKEND_PORT" --modelrouter.serve-spa=false --spring.web.resources.add-mappings=false &
BACKEND_PID=$!
echo "  Backend PID: $BACKEND_PID"

# 等待后端就绪
sleep 3
if ! kill -0 $BACKEND_PID 2>/dev/null; then
  echo "[ERROR] Backend failed to start"
  exit 1
fi

# 启动前端 (优先 npx serve，其次 jwebserver for Java 18+)
echo "[2/2] Starting frontend on port $FRONTEND_PORT..."
if command -v npx &>/dev/null; then
  npx -y serve -s "$DIST" -l "$FRONTEND_PORT" &
  FRONTEND_PID=$!
elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/jwebserver" ] 2>/dev/null; then
  "$JAVA_HOME/bin/jwebserver" -d "$(cd "$DIST" && pwd)" -p "$FRONTEND_PORT" -b 0.0.0.0 &
  FRONTEND_PID=$!
else
  echo "[ERROR] Node.js (npx serve) or JDK 18+ (jwebserver) required for frontend"
  kill $BACKEND_PID 2>/dev/null
  exit 1
fi
echo "  Frontend PID: $FRONTEND_PID"

echo ""
echo "----------------------------------------"
echo " UI:   http://localhost:$FRONTEND_PORT"
echo " API:  http://localhost:$BACKEND_PORT"
echo "----------------------------------------"
echo "Press Ctrl+C to stop"
echo ""

# 前台等待，Ctrl+C 时清理
trap "kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit 0" INT TERM
wait
