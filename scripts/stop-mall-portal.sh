#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}" )/.." && pwd)"
PID_FILE="${PROJECT_ROOT}/logs/mall-portal.pid"

if [[ -f "${PID_FILE}" ]]; then
  PID=$(cat "${PID_FILE}")
  if kill -0 "${PID}" >/dev/null 2>&1; then
    echo "Stopping mall-portal (PID ${PID})"
    kill "${PID}"
  else
    echo "mall-portal PID file present but process not running"
  fi
  rm -f "${PID_FILE}"
else
  echo "mall-portal PID file not found"
fi
