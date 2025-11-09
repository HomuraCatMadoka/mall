#!/usr/bin/env bash

# 基于 Docker 的 MySQL 首次部署脚本，适用于在腾讯云 CVM 上快速拉起数据库服务
# 运行前请以 root 身份执行：sudo bash mysql-bootstrap.sh

set -euo pipefail

MYSQL_VERSION="${MYSQL_VERSION:-8.0}"
MYSQL_CONTAINER_NAME="${MYSQL_CONTAINER_NAME:-mall-mysql}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-ChangeMe123!}"
MYSQL_DATABASE="${MYSQL_DATABASE:-mall}"
MYSQL_USER="${MYSQL_USER:-malluser}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-MallPwd#2024}"
MYSQL_DATA_DIR="${MYSQL_DATA_DIR:-/data/mysql}"
DOCKER_INSTALL_SCRIPT="${DOCKER_INSTALL_SCRIPT:-https://get.docker.com}"

log() {
  local level="$1"
  shift
  printf '[%s] %s\n' "$level" "$*"
}

require_root() {
  if [[ $EUID -ne 0 ]]; then
    log ERROR "请使用 root 身份或 sudo 执行该脚本"
    exit 1
  fi
}

ensure_command() {
  local cmd="$1"
  local pkg="$2"
  if ! command -v "$cmd" >/dev/null 2>&1; then
    if command -v apt-get >/dev/null 2>&1; then
      log INFO "安装依赖 $pkg (apt-get)"
      apt-get update -y
      apt-get install -y "$pkg"
    elif command -v yum >/dev/null 2>&1; then
      log INFO "安装依赖 $pkg (yum)"
      yum install -y "$pkg"
    else
      log ERROR "未找到可用的包管理器，无法安装 $pkg"
      exit 1
    fi
  fi
}

install_docker() {
  if command -v docker >/dev/null 2>&1; then
    log INFO "检测到 Docker 已安装，跳过安装步骤"
    return
  fi
  ensure_command curl curl
  log INFO "开始安装 Docker (官方一键脚本)"
  curl -fsSL "$DOCKER_INSTALL_SCRIPT" | sh
  systemctl enable docker
  systemctl start docker
  log INFO "Docker 安装完成"
}

prepare_directories() {
  log INFO "创建数据目录：${MYSQL_DATA_DIR}/{data,conf,logs}"
  mkdir -p "${MYSQL_DATA_DIR}/data" "${MYSQL_DATA_DIR}/conf" "${MYSQL_DATA_DIR}/logs"
  chown -R root:root "${MYSQL_DATA_DIR}"
}

pull_image() {
  log INFO "拉取 mysql:${MYSQL_VERSION} 镜像"
  docker pull "mysql:${MYSQL_VERSION}"
}

start_mysql() {
  if docker ps -a --format '{{.Names}}' | grep -q "^${MYSQL_CONTAINER_NAME}$"; then
    log WARN "容器 ${MYSQL_CONTAINER_NAME} 已存在，如需重新创建请先执行 docker rm -f ${MYSQL_CONTAINER_NAME}"
    return
  fi
  log INFO "启动 MySQL 容器 ${MYSQL_CONTAINER_NAME}"
  docker run -d \
    --name "${MYSQL_CONTAINER_NAME}" \
    --restart unless-stopped \
    -p "${MYSQL_PORT}:3306" \
    -v "${MYSQL_DATA_DIR}/data:/var/lib/mysql" \
    -v "${MYSQL_DATA_DIR}/logs:/var/log/mysql" \
    -e "MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}" \
    -e "MYSQL_DATABASE=${MYSQL_DATABASE}" \
    -e "MYSQL_USER=${MYSQL_USER}" \
    -e "MYSQL_PASSWORD=${MYSQL_PASSWORD}" \
    "mysql:${MYSQL_VERSION}" \
    --character-set-server=utf8mb4 \
    --collation-server=utf8mb4_unicode_ci
  log INFO "MySQL 容器已启动，监听端口 ${MYSQL_PORT}"
}

post_checks() {
  log INFO "查看容器状态"
  docker ps --filter "name=${MYSQL_CONTAINER_NAME}"
  cat <<EOF

下一步建议：
1. 使用 mysql 客户端测试远程连接：
   mysql -h <CVM 公网或内网 IP> -P ${MYSQL_PORT} -u ${MYSQL_USER} -p${MYSQL_PASSWORD} ${MYSQL_DATABASE}
2. 如需限制外网访问，请在腾讯云安全组中仅开放 3306 给可信 IP，或改为内网访问。
3. 后续可将后端服务的 DATABASE_URL 指向 ${MYSQL_CONTAINER_NAME}:${MYSQL_PORT} 并复用上述账号。
EOF
}

main() {
  require_root
  install_docker
  prepare_directories
  pull_image
  start_mysql
  post_checks
}

main "$@"
