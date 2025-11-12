#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}" )/.." && pwd)"
JAR_PATH="${PROJECT_ROOT}/mall-portal/target/mall-portal-1.0-SNAPSHOT.jar"
LOG_DIR="${PROJECT_ROOT}/logs"
LOG_FILE="${LOG_DIR}/mall-portal.log"
PID_FILE="${PROJECT_ROOT}/logs/mall-portal.pid"

mkdir -p "${LOG_DIR}"

export JAVA_HOME=${JAVA_HOME:-$(/usr/libexec/java_home -v 1.8)}
export PATH="$JAVA_HOME/bin:$PATH"
export MALL_DB_URL=${MALL_DB_URL:-"jdbc:mysql://119.29.236.9:3306/mall?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"}
export MALL_DB_USERNAME=${MALL_DB_USERNAME:-malluser}
export MALL_DB_PASSWORD=${MALL_DB_PASSWORD:-mall}

if [[ ! -f "${JAR_PATH}" ]]; then
  echo "[start-mall-portal] jar not found, building..."
  (cd "${PROJECT_ROOT}" && mvn -pl mall-portal -am package -DskipTests)
fi

if [[ -f "${PID_FILE}" && -e /proc/$(cat "${PID_FILE}") ]]; then
  echo "[start-mall-portal] already running (PID $(cat ${PID_FILE}))"
  exit 0
fi

echo "[start-mall-portal] starting mall-portal..."
nohup java -jar "${JAR_PATH}" \
  --spring.profiles.active=dev \
  --spring.datasource.url="${MALL_DB_URL}" \
  --spring.datasource.username="${MALL_DB_USERNAME}" \
  --spring.datasource.password="${MALL_DB_PASSWORD}" > "${LOG_FILE}" 2>&1 &
PID=$!
printf "%s" "${PID}" > "${PID_FILE}"
printf "started mall-portal (PID %s). Logs: %s\n" "${PID}" "${LOG_FILE}"
