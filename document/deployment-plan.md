# mall 项目部署方案（腾讯云 + Nginx）

## 1. 总览

- **后端**：`mall-admin`、`mall-portal` 两个 Spring Boot 应用（JDK 8），访问云端 MySQL。
- **前端**：`mall-app-web`（uni-app H5，运行在 HBuilder X 或构建成纯静态文件）；管理端若有 `mall-admin-web` 亦可打包为静态资源。
- **接入层**：Nginx 做反向代理与静态资源托管，统一域名（示例：`api.yourdomain.com`/`app.yourdomain.com`）。
- **自动化脚本**：仓内提供 `scripts/start-mall-*.sh`、`veg-order-smoke.sh`、`probe-*.sh` 等，可整合到部署流程中。

## 2. 服务器与域名准备

| 项目 | 建议配置 |
| --- | --- |
| 云服务器 | 腾讯云 CVM（2C4G 以上，Ubuntu 20.04 / CentOS 8 均可） |
| 域名 | `yourdomain.com` 已备案，可解析至该云服务器公网 IP |
| 防火墙/安全组 | 放通 80、443、8080、8085、22、3306 等需要的端口；部署完成后建议仅开放 80/443/22，对内网使用防火墙访问后端端口。 |

## 3. 环境准备

1. **安装基础依赖**
   ```bash
   # 系统更新
   sudo apt update && sudo apt upgrade -y

   # 安装 JDK 8
   sudo apt install openjdk-8-jdk -y
   java -version

   # 安装 Maven
   sudo apt install maven -y  # 或下载 tar 包手动配置

   # 安装 Node.js（如需 H5 构建）
   curl -fsSL https://deb.nodesource.com/setup_16.x | sudo -E bash -
   sudo apt install -y nodejs

   # 安装 Nginx
   sudo apt install nginx -y
   sudo systemctl enable nginx
   ```

2. **获取代码**
   ```bash
   git clone https://github.com/your_repo/mall.git /opt/mall
   cd /opt/mall
   ```

3. **数据库**
   - 直接复用现在的云 MySQL（119.29.236.9:3306）。若迁移到本机，可安装 MySQL 8，执行 `scripts/db-bootstrap.sh` 导入 `document/sql/mall.sql`。
   - 确保远端数据库对云服务器 IP 放通，或在服务器上设置专属账号。

## 4. 构建与部署

### 4.1 mall-portal / mall-admin

1. **打包**
   ```bash
   cd /opt/mall
   export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
   mvn -pl mall-portal -am package -DskipTests
   mvn -pl mall-admin -am package -DskipTests
   ```
   生成的 Jar 位于 `mall-portal/target/mall-portal-1.0-SNAPSHOT.jar` 和 `mall-admin/target/mall-admin-1.0-SNAPSHOT.jar`。

2. **后台启动脚本**
   - 使用仓内 `scripts/start-mall-portal.sh`、`scripts/start-mall-admin.sh`：
     ```bash
     cd /opt/mall
     bash scripts/start-mall-portal.sh
     bash scripts/start-mall-admin.sh
     ```
   - 脚本会自动设置 `MALL_DB_*` 环境变量并以 `nohup` 方式运行，日志输出到 `logs/mall-portal.log` / `logs/mall-admin.log`。
   - 停止服务：`bash scripts/stop-mall-portal.sh`、`bash scripts/stop-mall-admin.sh`。

3. **守护方式（可选）**：
   - 使用 `systemd` 创建 service：
     ```ini
     [Unit]
     Description=Mall Portal Service
     After=network.target

     [Service]
     WorkingDirectory=/opt/mall
     ExecStart=/opt/mall/scripts/start-mall-portal.sh
     ExecStop=/opt/mall/scripts/stop-mall-portal.sh
     Restart=on-failure
     User=ubuntu

     [Install]
     WantedBy=multi-user.target
     ```
   - `systemctl enable mall-portal && systemctl start mall-portal`

### 4.2 前端 H5（mall-app-web）

1. **若使用 HBuilder X 手动运行**：在本地开发机或云桌面运行 HBuilder X，即可直接访问 `http://云服务器IP:8085`。
2. **若需构建为静态站点**：
   - 使用 CLI（示例：`npm install -g @dcloudio/cli`，或在 HBuilder X -> 发行 -> H5）
   - 构建输出目录（假设为 `dist/build/h5`）传到服务器 `/opt/mall/front`。
   - Nginx 配置静态根目录，详见下文。

### 4.3 Nginx 反向代理

1. **目录结构示例**
   - 后端 Jar：`/opt/mall/...`
   - 前端静态：`/opt/mall/front`（包含 `index.html` 等）
   - SSL 证书（如需 HTTPS）：`/etc/nginx/ssl/yourdomain.crt|key`

2. **Nginx 配置**（参考 `document/docker/nginx.conf`）
   - 创建 `/etc/nginx/sites-available/mall.conf`：
     ```nginx
     server {
         listen 80;
         server_name api.yourdomain.com;
         location / {
             proxy_pass http://127.0.0.1:8085;
             proxy_set_header Host $host;
             proxy_set_header X-Real-IP $remote_addr;
             proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
         }
     }

     server {
         listen 80;
         server_name admin-api.yourdomain.com;
         location / {
             proxy_pass http://127.0.0.1:8080;
             proxy_set_header Host $host;
             proxy_set_header X-Real-IP $remote_addr;
             proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
         }
     }

     server {
         listen 80;
         server_name app.yourdomain.com;
         root /opt/mall/front;
         index index.html;
         location / {
             try_files $uri $uri/ /index.html;
         }
     }
     ```
   - 如需 HTTPS，在每个 server 块内添加 `listen 443 ssl;`、证书路径以及 `return 301 https://...` 的 80→443 重定向。
   - 启用并重载：
     ```bash
     sudo ln -s /etc/nginx/sites-available/mall.conf /etc/nginx/sites-enabled/mall.conf
     sudo nginx -t
     sudo systemctl reload nginx
     ```

## 5. 自动化＆监控建议

1. **自检脚本**
   - 部署完毕后执行：
     ```bash
     bash scripts/health-check.sh
     bash scripts/probe-portal.sh
     bash scripts/probe-admin.sh
     bash scripts/veg-order-smoke.sh
     ```
   - 如需持续监控，可使用 crontab 每隔 5 分钟执行 `veg-order-smoke.sh`，失败时发邮件/钉钉通知。

2. **tmux 使用示例**
   - 连接服务器后开启会话：`tmux new -s mall`
   - 在 tmux 中执行 `tail -f logs/mall-portal.log` 或 `bash scripts/start-mall-portal.sh`
   - 脱离会话：`Ctrl+B` → `D`；稍后用 `tmux attach -t mall` 返回。

## 6. 持续迭代建议

- **环境变量管理**：
  - 将数据库配置、端口、证书路径抽成 `.env` 或 `/etc/profile.d/mall.sh`，保障脚本复用。
  - 在 `scripts/integration-pipeline.sh` 中增加 `PORTAL_URL`/`ADMIN_URL` 参数，使其可快速检测线上环境。

- **日志与备份**：
  - 使用 `logrotate` 或将 `logs/*.log` 写入 `/var/log/mall/`，并配置每日轮转。
  - 数据库备份：可在 MySQL 端设置定时 `mysqldump`，将 `veg_order`、`pms_veg_*` 等表每日导出到 COS/OSS。

- **安全**：
  - 开启 HTTPS，证书可用腾讯云 SSL 免费版；在 Nginx 中配置 `add_header Strict-Transport-Security`。
  - 限制管理接口的访问：
    - 通过 Nginx `allow/deny` 限制 IP；
    - 或使用单独的 `admin-api` 域名，仅在内网或 VPN 下访问。

- **CI/CD**：
  - 可在 GitHub Actions / Jenkins 中添加构建任务：`mvn package` → 上传 Jar → SSH 执行 `scripts/stop-*.sh && scripts/start-*.sh`。
  - 在 pipeline 中调用 `scripts/veg-order-smoke.sh` 验证部署是否成功。

- **前端部署变量**：
  - 在 `mall-app-web/utils/appConfig.js` 中使用环境变量（例如 `VUE_APP_API_BASE`）。部署前执行 `npm run build -- --api-base=https://api.yourdomain.com`，确保 H5 对接线上地址。

## 7. 常见问题

| 问题 | 解决方案 |
| --- | --- |
| `Public Key Retrieval is not allowed` | 确保 JDBC URL 加上 `allowPublicKeyRetrieval=true`，或在 MySQL 中切换 `mysql_native_password`。脚本已默认带该参数。|
| Nginx 80 端口被占用 | `sudo lsof -i :80` 找到进程并处理；确认没有其他服务抢占。|
| 前端跨域 | 由于 Nginx 同域代理，浏览器不会产生 CORS；若在开发环境直接访问 `http://ip:8085`，需要在 `mall-portal` 中启用 CORS 或通过 HBuilder Proxy。|
| 8080/8085 被防火墙拦截 | 若通过 Nginx 访问，应关闭外部 8080/8085，对外仅暴露 80/443；内部由 Nginx 转发即可。|

---

> 完成上述步骤后，即可通过 `https://app.yourdomain.com` 访问前端门户，`https://api.yourdomain.com` 和 `https://admin-api.yourdomain.com` 分别反向代理 mall-portal 与 mall-admin，并使用提供的脚本定期执行烟测保障可用性。
