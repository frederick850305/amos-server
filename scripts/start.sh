#!/usr/bin/env bash
# 一键启动 amos-server
#
# 默认 postgres profile，连本地容器化 PostgreSQL (localhost:5432/amos)。
# JAVA_HOME 强制按仓库根 .java-version 指向本机 Temurin（macOS 路径）。
# 说明：Spring Boot 3.2.5 必须用 JDK 17 运行，故不沿用环境里可能错误的 JAVA_HOME
#      （例如 shell 中 export 了 JDK 11 会导致插件 UnsupportedClassVersionError）。
#
# 启动前会做两项预检，避免常见的"启动失败"：
#   1) 端口冲突：若目标端口（默认 8080，可被 --server.port= 覆盖）已被占用，
#      先打印占用进程并 SIGTERM，等待释放；超时仍未释放则 SIGKILL。
#      （绝大多数情况是被上一轮没退出的 amos-server 实例占着 8080。）
#   2) PostgreSQL 可达性：默认 postgres profile 时，探测 localhost:5432，
#      不可达则给出明确提示（先起 OrbStack 容器 pg17-local），但不阻断启动。
#
# 用法:
#   ./scripts/start.sh                                                  # 默认 PG + 8080
#   ./scripts/start.sh --spring.profiles.active=default --server.port=8081  # 切 H2 双跑
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# 强制按 .java-version 设置 JAVA_HOME（Spring Boot 3.2.5 需 JDK 17，忽略环境可能错误的 JAVA_HOME）
JAVA_VER="$(tr -d '[:space:]' < "$REPO_ROOT/.java-version" 2>/dev/null | head -1)"
JAVA_VER="${JAVA_VER:-17}"
export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-${JAVA_VER}.jdk/Contents/Home"

# ---- 解析参数：端口 / profile ----
PORT=8080
PROFILE="postgres"
for arg in "$@"; do
  case "$arg" in
    --server.port=*) PORT="${arg#*=}" ;;
    --spring.profiles.active=*) PROFILE="${arg#*=}" ;;
  esac
done

# lsof 探测端口占用；若系统带 timeout 命令则加超时保护（避免极端情况下命令挂起）
LSOF="lsof -ti"
if command -v timeout >/dev/null 2>&1; then
  LSOF="timeout 10 lsof -ti"
fi

echo ">>> JAVA_HOME=$JAVA_HOME"
echo ">>> 目标端口=$PORT  profile=$PROFILE"

# ---- 1) 端口冲突检测与释放 ----
pids_on_port() {
  $LSOF ":$PORT" 2>/dev/null || true
}

if [ -n "$(pids_on_port)" ]; then
  PIDS="$(pids_on_port | tr '\n' ' ')"
  echo ">>> 检测到端口 $PORT 已被占用，占用进程 PID: $PIDS"
  for pid in $PIDS; do
    echo ">>>   终止进程 $pid (SIGTERM)..."
    kill -TERM "$pid" 2>/dev/null || true
  done
  # 等待最多 8 秒释放
  waited=0
  while [ "$waited" -lt 8 ]; do
    if [ -z "$(pids_on_port)" ]; then
      echo ">>> 端口 $PORT 已释放"
      break
    fi
    sleep 1
    waited=$((waited + 1))
  done
  # 仍未释放则强制 SIGKILL
  if [ -n "$(pids_on_port)" ]; then
    PIDS="$(pids_on_port | tr '\n' ' ')"
    echo ">>> 进程未退出，强制 SIGKILL: $PIDS"
    for pid in $PIDS; do
      kill -9 "$pid" 2>/dev/null || true
    done
    sleep 1
  fi
  if [ -n "$(pids_on_port)" ]; then
    echo ">>> 错误：端口 $PORT 仍被占用，无法启动。请手动排查占用进程：" >&2
    echo ">>>   lsof -i :$PORT" >&2
    exit 1
  fi
else
  echo ">>> 端口 $PORT 未被占用，可直接启动"
fi

# ---- 2) PostgreSQL 可达性预检（仅 postgres profile） ----
if [ "$PROFILE" != "default" ] && [ "$PROFILE" != "h2" ]; then
  if bash -c 'exec 3<>/dev/tcp/localhost/5432' 2>/dev/null; then
    echo ">>> 本地 PostgreSQL (localhost:5432) 可达"
  else
    echo ">>> 警告：本地 PostgreSQL (localhost:5432) 不可达。请先启动 OrbStack 容器 pg17-local："
    echo ">>>   cd ~/postgres-local && docker compose up -d"
    echo ">>> 若数据库未就绪，应用将因无法连接而启动失败。详见 docs/postgres-setup.md"
  fi
fi

cd "$REPO_ROOT/amos-app"
echo ">>> 启动 amos-server (spring-boot:run $*)"
exec mvn spring-boot:run "$@"
