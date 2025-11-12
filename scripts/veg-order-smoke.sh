#!/usr/bin/env bash
set -euo pipefail

PORTAL_URL=${PORTAL_URL:-http://127.0.0.1:8085}
PORTAL_USER=${PORTAL_USER:-vegsmoke}
PORTAL_PASS=${PORTAL_PASS:-veg123456}
PORTAL_MOBILE=${PORTAL_MOBILE:-13800003333}
ADMIN_URL=${ADMIN_URL:-http://127.0.0.1:8080}
ADMIN_USER=${ADMIN_USER:-admin}
ADMIN_PASS=${ADMIN_PASS:-macro123}

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "[veg-smoke] $1 is required" >&2
    exit 1
  fi
}

require_cmd curl
require_cmd jq

portal_login() {
  curl -s -X POST "${PORTAL_URL}/auth/login" \
    -H 'Content-Type: application/json' \
    -d "{\"username\":\"${PORTAL_USER}\",\"password\":\"${PORTAL_PASS}\"}"
}

portal_register() {
  curl -s -X POST "${PORTAL_URL}/auth/register" \
    -H 'Content-Type: application/json' \
    -d "{\"username\":\"${PORTAL_USER}\",\"password\":\"${PORTAL_PASS}\",\"mobile\":\"${PORTAL_MOBILE}\"}"
}

PORTAL_TOKEN=$(portal_login | jq -r '.data.token')
if [[ -z "${PORTAL_TOKEN}" || "${PORTAL_TOKEN}" == "null" ]]; then
  echo "[veg-smoke] register new user ${PORTAL_USER}"
  portal_register >/dev/null
  PORTAL_TOKEN=$(portal_login | jq -r '.data.token')
fi

if [[ -z "${PORTAL_TOKEN}" || "${PORTAL_TOKEN}" == "null" ]]; then
  echo "[veg-smoke] failed to obtain portal token" >&2
  exit 1
fi

PRODUCT_ID=$(curl -s "${PORTAL_URL}/veg/products" -H "Authorization: Bearer ${PORTAL_TOKEN}" | jq -r '.data.list[0].id')
if [[ -z "${PRODUCT_ID}" || "${PRODUCT_ID}" == "null" ]]; then
  echo "[veg-smoke] no veg product available" >&2
  exit 1
fi

ORDER_PAYLOAD=$(cat <<JSON
{
  "remark": "自动化烟测",
  "items": [
    {"productId": ${PRODUCT_ID}, "quantity": 1}
  ]
}
JSON
)

ORDER_RESP=$(curl -s -X POST "${PORTAL_URL}/veg/orders" \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer ${PORTAL_TOKEN}" \
  -d "${ORDER_PAYLOAD}")
ORDER_ID=$(echo "${ORDER_RESP}" | jq -r '.data.orderId')
ORDER_SN=$(echo "${ORDER_RESP}" | jq -r '.data.orderSn')

if [[ -z "${ORDER_ID}" || "${ORDER_ID}" == "null" ]]; then
  echo "[veg-smoke] submit order failed: ${ORDER_RESP}" >&2
  exit 1
fi

echo "[veg-smoke] order submitted: ${ORDER_SN}"

LIST_RESP=$(curl -s "${PORTAL_URL}/veg/orders" -H "Authorization: Bearer ${PORTAL_TOKEN}")
FOUND=$(echo "${LIST_RESP}" | jq -r --arg sn "${ORDER_SN}" '.data.list[]?.orderSn | select(. == $sn)')
if [[ "${FOUND}" != "${ORDER_SN}" ]]; then
  echo "[veg-smoke] order not found in list"
  exit 1
fi

echo "[veg-smoke] order present in list"

ADMIN_TOKEN=$(curl -s "${ADMIN_URL}/admin/login" -H 'Content-Type: application/json' \
  -d "{\"username\":\"${ADMIN_USER}\",\"password\":\"${ADMIN_PASS}\"}" | jq -r '.data.token')
if [[ -n "${ADMIN_TOKEN}" && "${ADMIN_TOKEN}" != "null" ]]; then
  curl -s -X POST "${ADMIN_URL}/veg/order/updateStatus/${ORDER_ID}" \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${ADMIN_TOKEN}" \
    -d '{"status":"CLOSED","operatorRemark":"auto smoke"}' >/dev/null
fi

echo "[veg-smoke] completed"
