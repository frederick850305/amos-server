#!/usr/bin/env bash
# 一键启动 amos-server
#
# 默认 postgres profile，连本地容器化 PostgreSQL (localhost:5432/amos)。
# JAVA_HOME 强制按仓库根 .java-version 指向本机 Temurin（macOS 路径）。
# 说明：Spring Boot 3.2.5 必须用 JDK 17 运行，故不沿用环境里可能错误的 JAVA_HOME
#      （例如 shell 中 export 了 JDK 11 会导致插件 UnsupportedClassVersionError）。
#
# 用法:
#   ./scripts/start.sh                                                  # 默认 PG
#   ./scripts/start.sh --spring.profiles.active=default --server.port=8081  # 切 H2 双跑
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# 强制按 .java-version 设置 JAVA_HOME（Spring Boot 3.2.5 需 JDK 17，忽略环境可能错误的 JAVA_HOME）
JAVA_VER="$(tr -d '[:space:]' < "$REPO_ROOT/.java-version" 2>/dev/null | head -1)"
JAVA_VER="${JAVA_VER:-17}"
export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-${JAVA_VER}.jdk/Contents/Home"

cd "$REPO_ROOT/amos-app"

echo ">>> JAVA_HOME=$JAVA_HOME"
echo ">>> 启动 amos-server (spring-boot:run $*)"
exec mvn spring-boot:run "$@"
