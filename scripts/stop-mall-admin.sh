#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}" )/.." && pwd)"
PID_FILE="${PROJECT_ROOT}/logs/mall-admin.pid"

if [[ -f "${PID_FILE}" ]]; then
  PID=$(cat "${PID_FILE}")
  if kill -0 "${PID}" >/dev/null 2>&1; then
    echo "Stopping mall-admin (PID ${PID})"
    kill "${PID}"
  else
    echo "mall-admin PID file present but process not running"
  fi
  rm -f "${PID_FILE}"
else
  echo "mall-admin PID file not found"
fi
