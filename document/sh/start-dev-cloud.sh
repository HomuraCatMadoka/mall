#!/usr/bin/env bash
set -euo pipefail

# 允许通过第一个参数指定要启动的模块（默认 mall-admin），SPRING_PROFILE 环境变量可覆盖运行环境。
MODULE="${1:-mall-admin}"
PROFILE="${SPRING_PROFILE:-dev}"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
cd "${PROJECT_ROOT}"

LOCAL_REPO="${PROJECT_ROOT}/.m2-local"
mkdir -p "${LOCAL_REPO}"
MVN_CMD=(mvn -Dmaven.repo.local="${LOCAL_REPO}")

# 统一在这里导出云端数据库环境变量，避免每次手动输入。
export MALL_DB_URL="jdbc:mysql://119.29.236.9:3306/mall?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
export MALL_DB_USERNAME="malluser"
export MALL_DB_PASSWORD="mall"

JAVA_HOME="$(/usr/libexec/java_home -v 1.8)"
export JAVA_HOME
export PATH="${JAVA_HOME}/bin:${PATH}"

TARGET_PORT=""
case "${MODULE}" in
  mall-admin)
    TARGET_PORT=${TARGET_PORT_OVERRIDE:-8080}
    ;;
  mall-portal)
    TARGET_PORT=${TARGET_PORT_OVERRIDE:-8085}
    ;;
  *)
    TARGET_PORT=${TARGET_PORT_OVERRIDE:-}
    ;;
esac

if [[ -n "${TARGET_PORT}" ]]; then
  EXISTING_PIDS=$(lsof -ti TCP:"${TARGET_PORT}" || true)
  if [[ -n "${EXISTING_PIDS}" ]]; then
    echo "[mall] Port ${TARGET_PORT} in use by PID(s): ${EXISTING_PIDS}. Terminating..."
    kill ${EXISTING_PIDS}
    sleep 1
  fi
fi

echo "[mall] 使用云端数据库: ${MALL_DB_URL}"
echo "[mall] Spring Profile: ${PROFILE}"
echo "[mall] Target module: ${MODULE}"
echo "[mall] JAVA_HOME: ${JAVA_HOME}"

echo "[mall] 先安装依赖模块..."
"${MVN_CMD[@]}" -pl "${MODULE}" -am install -DskipTests

pushd "${MODULE}" >/dev/null
"${MVN_CMD[@]}" spring-boot:run -Dspring-boot.run.profiles="${PROFILE}"
popd >/dev/null
