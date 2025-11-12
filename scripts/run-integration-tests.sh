#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}" )" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${PROJECT_ROOT}"

export MALL_DB_URL=${MALL_DB_URL:-"jdbc:mysql://119.29.236.9:3306/mall?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"}
export MALL_DB_USERNAME=${MALL_DB_USERNAME:-malluser}
export MALL_DB_PASSWORD=${MALL_DB_PASSWORD:-mall}

echo "[integration-tests] Running mvn test -DskipTests=false"
mvn test -DskipTests=false
