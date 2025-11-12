#!/usr/bin/env bash
set -euo pipefail

DB_HOST=${DB_HOST:-119.29.236.9}
DB_PORT=${DB_PORT:-3306}
DB_USER=${DB_USER:-malluser}
DB_PASS=${DB_PASS:-mall}
DB_NAME=${DB_NAME:-mall}

RESULT=$(mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${DB_PASS}" \
  -e "use ${DB_NAME}; select count(*) as product_count from pms_product;" | tail -n +2)

echo "[sql-check] pms_product count: ${RESULT}"
