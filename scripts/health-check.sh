#!/usr/bin/env bash
set -euo pipefail

ADMIN_URL=${ADMIN_URL:-http://127.0.0.1:8080}
PORTAL_URL=${PORTAL_URL:-http://127.0.0.1:8085}

if ! command -v curl >/dev/null 2>&1; then
  echo "[health-check] curl is required" >&2
  exit 1
fi

echo "[health-check] Checking admin login endpoint"
ADMIN_BODY_FILE=$(mktemp)
PORTAL_BODY_FILE=$(mktemp)
trap 'rm -f "${ADMIN_BODY_FILE}" "${PORTAL_BODY_FILE}"' EXIT

ADMIN_STATUS=$(curl -s -o "${ADMIN_BODY_FILE}" -w "%{http_code}" -X POST "${ADMIN_URL}/admin/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"macro123"}')
ADMIN_BODY=$(cat "${ADMIN_BODY_FILE}")
echo "[health-check] admin status: ${ADMIN_STATUS}"

if [[ "${ADMIN_STATUS}" != "200" ]]; then
  echo "[health-check] admin login failed: ${ADMIN_BODY}" >&2
  exit 1
fi

echo "[health-check] Checking portal actuator health"
PORTAL_STATUS=$(curl -s -o "${PORTAL_BODY_FILE}" -w "%{http_code}" "${PORTAL_URL}/actuator/health")
PORTAL_BODY=$(cat "${PORTAL_BODY_FILE}")
echo "[health-check] portal status: ${PORTAL_STATUS}"

echo "[health-check] admin response: ${ADMIN_BODY}"
echo "[health-check] portal response: ${PORTAL_BODY}"

if [[ "${PORTAL_STATUS}" != "200" ]]; then
  exit 1
fi
