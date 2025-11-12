#!/usr/bin/env bash
set -euo pipefail

if ! command -v jq >/dev/null 2>&1; then
  echo "[probe-admin] jq is required" >&2
  exit 1
fi

ADMIN_URL=${ADMIN_URL:-http://127.0.0.1:8080}
ADMIN_USER=${ADMIN_USER:-admin}
ADMIN_PASS=${ADMIN_PASS:-macro123}

login() {
  curl -s "${ADMIN_URL}/admin/login" \
    -H 'Content-Type: application/json' \
    -d "{\"username\":\"${ADMIN_USER}\",\"password\":\"${ADMIN_PASS}\"}" | jq -r '.data.token'
}

TOKEN=$(login)
if [[ -z "${TOKEN}" || "${TOKEN}" == "null" ]]; then
  echo "[probe-admin] failed to obtain token" >&2
  exit 1
fi

echo "[probe-admin] calling veg order list"
RESP=$(curl -s "${ADMIN_URL}/veg/order/list?pageNum=1&pageSize=1" -H "Authorization: Bearer ${TOKEN}")
CODE=$(echo "${RESP}" | jq -r '.code')
if [[ "${CODE}" != "200" ]]; then
  echo "[probe-admin] veg order list failed: ${RESP}" >&2
  exit 1
fi

echo "[probe-admin] veg order list ok"
