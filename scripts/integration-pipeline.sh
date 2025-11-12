#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}" )" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

run_script() {
  local script_name=$1
  shift || true
  echo "\n=== Running ${script_name} ==="
  "${SCRIPT_DIR}/${script_name}" "$@"
}

prompt_continue() {
  local message="$1"
  read -r -p "${message} 按 Enter 继续..." _
}

printf "mall 前后端联调自动化流程\n"
printf "项目根目录: %s\n" "${PROJECT_ROOT}"

read -r -p "是否执行数据库导入 (db-bootstrap)? [y/N] " DO_DB
if [[ "${DO_DB}" =~ ^[Yy]$ ]]; then
  run_script db-bootstrap.sh
else
  echo "跳过数据库导入"
fi

cat <<'EOF'
[提示] 接下来需要在独立终端中启动后端服务：
  bash document/sh/start-dev-cloud.sh mall-admin
  bash document/sh/start-dev-cloud.sh mall-portal
请确保二者都运行成功并监听 8080 / 8085。
EOF
prompt_continue "启动完成后"

run_script run-integration-tests.sh
run_script health-check.sh
run_script sql-check.sh
run_script probe-admin.sh
run_script probe-portal.sh

cat <<'EOF'
[手动步骤] 前端 H5 联调：
1. 打开 HBuilder X，加载 /Users/wangkunyu/develop/mall-app-web。
2. 确认 utils/appConfig.js 中 API_BASE_URL 指向当前机器 (默认 http://127.0.0.1:8085)。
3. 运行 -> 运行到浏览器 -> Chrome，或手动访问 http://localhost:8060。
4. 使用账号 test/123456 登录并验证首页/下单等流程。
EOF
prompt_continue "完成前端验证后"

echo "所有自动化步骤执行完毕。"
