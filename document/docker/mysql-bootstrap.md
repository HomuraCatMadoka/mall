# CVM MySQL 快速部署指引

本文档配合 `document/sh/mysql-bootstrap.sh` 使用，帮助你把 MySQL 服务部署到腾讯云自建 CVM 实例上，作为本地开发环境完成联调后的第一步上线准备。

## 使用方式

1. **上传脚本**：将 `document/sh/mysql-bootstrap.sh` 拷贝到 CVM，例如：
   ```bash
   scp document/sh/mysql-bootstrap.sh ubuntu@<cvm-ip>:/tmp/
   ```
2. **自定义变量（可选）**：根据需要修改以下环境变量后再执行脚本。
3. **执行脚本**：
   ```bash
   sudo MYSQL_ROOT_PASSWORD='StrongRoot#2024' \
        MYSQL_USER='malluser' \
        MYSQL_PASSWORD='MallPwd#2024' \
        MYSQL_DATABASE='mall' \
        MYSQL_PORT=3306 \
        MYSQL_DATA_DIR='/data/mysql' \
        bash /tmp/mysql-bootstrap.sh
   ```
4. **验证连通**：脚本会输出容器状态，随后在本地或跳板机使用 `mysql -h <cvm-ip> -P3306 -u malluser -p mall` 验证连接。

## 变量说明

| 变量名 | 默认值 | 作用 |
| --- | --- | --- |
| `MYSQL_VERSION` | `8.0` | 选择 MySQL 镜像版本 |
| `MYSQL_CONTAINER_NAME` | `mall-mysql` | 容器名称，后端可通过 `mall-mysql:3306` 访问 |
| `MYSQL_PORT` | `3306` | 暴露给宿主的端口，可按需改为 3307 等 |
| `MYSQL_ROOT_PASSWORD` | `ChangeMe123!` | root 密码，生产必须改成强口令 |
| `MYSQL_DATABASE` | `mall` | 初始化创建的业务库 |
| `MYSQL_USER` / `MYSQL_PASSWORD` | `malluser` / `MallPwd#2024` | 默认业务账号，可用于本地/线上共用 |
| `MYSQL_DATA_DIR` | `/data/mysql` | 数据持久化目录，包含 data/logs 子目录 |
| `DOCKER_INSTALL_SCRIPT` | `https://get.docker.com` | Docker 官方安装脚本地址，若已装可忽略 |

## 安全与运维提示

- **安全组**：只对可信 IP 开放 3306；若仅供同 VPC 的应用访问，可拒绝公网。
- **备份**：结合 `mysqldump` 或定时快照，将 `/data/mysql` 备份到 COS 或 OSS。
- **监控**：可在服务器上部署 `prom/mysqld-exporter`，或开启慢查询日志定位问题。
- **后续扩展**：同一 docker 网络还可加入 `mall-admin`、`mall-portal` 等容器，保持服务发现统一。

完成以上步骤后，即完成“在云端部署 MySQL 服务”的第 1 步，后续可以继续将后端服务镜像推送并连接到该数据库。
