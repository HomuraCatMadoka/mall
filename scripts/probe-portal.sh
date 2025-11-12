#!/usr/bin/env bash
set -euo pipefail

if ! command -v jq >/dev/null 2>&1; then
  echo "[probe-portal] jq is required" >&2
  exit 1
fi

PORTAL_URL=${PORTAL_URL:-http://127.0.0.1:8085}
PORTAL_USER=${PORTAL_USER:-vegtester}
PORTAL_PASS=${PORTAL_PASS:-veg123456}
PORTAL_MOBILE=${PORTAL_MOBILE:-13800002222}

login() {
  curl -s -X POST "${PORTAL_URL}/auth/login" \
    -H 'Content-Type: application/json' \
    -d "{\"username\":\"${PORTAL_USER}\",\"password\":\"${PORTAL_PASS}\"}"
}

TOKEN=$(login | jq -r '.data.token')
if [[ -z "${TOKEN}" || "${TOKEN}" == "null" ]]; then
  echo "[probe-portal] login failed, try register"
  curl -s -X POST "${PORTAL_URL}/auth/register" \
    -H 'Content-Type: application/json' \
    -d "{\"username\":\"${PORTAL_USER}\",\"password\":\"${PORTAL_PASS}\",\"mobile\":\"${PORTAL_MOBILE}\"}" >/dev/null
  TOKEN=$(login | jq -r '.data.token')
fi

if [[ -z "${TOKEN}" || "${TOKEN}" == "null" ]]; then
  echo "[probe-portal] failed to obtain token" >&2
  exit 1
fi

echo "[probe-portal] fetched portal token"

echo "[probe-portal] checking /veg/categories"
RESP_CATE=$(curl -s "${PORTAL_URL}/veg/categories" -H "Authorization: Bearer ${TOKEN}")
CODE_CATE=$(echo "${RESP_CATE}" | jq -r '.code')
if [[ "${CODE_CATE}" != "200" ]]; then
  echo "[probe-portal] categories response: ${RESP_CATE}" >&2
  exit 1
fi

echo "[probe-portal] checking /veg/products"
RESP_PROD=$(curl -s "${PORTAL_URL}/veg/products" -H "Authorization: Bearer ${TOKEN}")
CODE_PROD=$(echo "${RESP_PROD}" | jq -r '.code')
if [[ "${CODE_PROD}" != "200" ]]; then
  echo "[probe-portal] products response: ${RESP_PROD}" >&2
  exit 1
fi

echo "[probe-portal] veg endpoints ok"
