#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}" )" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

DB_HOST=${DB_HOST:-119.29.236.9}
DB_PORT=${DB_PORT:-3306}
DB_USER=${DB_USER:-malluser}
DB_PASS=${DB_PASS:-mall}
DB_NAME=${DB_NAME:-mall}
SQL_FILE=${SQL_FILE:-"${PROJECT_ROOT}/document/sql/mall.sql"}

if [[ ! -f "${SQL_FILE}" ]]; then
  echo "[db-bootstrap] SQL file not found: ${SQL_FILE}" >&2
  exit 1
fi

echo "[db-bootstrap] Ensuring database ${DB_NAME} exists on ${DB_HOST}:${DB_PORT}";
mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${DB_PASS}" \
  -e "create database if not exists ${DB_NAME} character set utf8mb4 collate utf8mb4_general_ci;"

echo "[db-bootstrap] Importing schema/data from ${SQL_FILE}";
mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${DB_PASS}" "${DB_NAME}" < "${SQL_FILE}";

echo "[db-bootstrap] Completed."