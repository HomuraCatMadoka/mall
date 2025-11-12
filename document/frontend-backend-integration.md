# mall 前后端联调方案

## 1. 场景概述

mall 系统由两条链路组成：
- **管理后台链路**：`mall-admin`（后端） + `mall-admin-web`（管理前端）。主要用于后台运营、商品维护等。默认监听 `http://127.0.0.1:8080`。
- **前台业务链路**：`mall-portal`（后端） + `mall-app-web`（H5/uni-app）。负责 C 端会员与商城业务。默认监听 `http://127.0.0.1:8085`。
- 两条链路共用同一个 MySQL 库（云端 docker 实例，IP `119.29.236.9`，库名 `mall`），通过环境变量 `MALL_DB_URL/MALL_DB_USERNAME/MALL_DB_PASSWORD` 注入。

## 2. 前置条件

| 事项 | 要求 |
| --- | --- |
| 操作系统 | macOS（当前环境），安装 JDK8（/Library/Java/JavaVirtualMachines/zulu-8.jdk）与 JDK23。脚本中已自动切换到 JDK8。 |
| 数据库 | 轻量应用服务器上的 MySQL docker，需提前 `create database mall` 并导入 `document/sql/mall.sql`。导入命令：`mysql -h 119.29.236.9 -P 3306 -u malluser -pmall mall < document/sql/mall.sql`。|
| 端口 | 本机开放 8080（admin）、8085（portal）、8060（HBuilder H5 devServer）。如需手机调试，需在局域网或防火墙中放行。|
| 工具 | Maven、curl、HBuilder X（App 开发版）、uni-app CLI（可选）。|

## 3. 后端服务准备

1. **导入云端数据库数据**（仅首次或数据重置时）：
   - 推荐执行 `scripts/db-bootstrap.sh`（内部即为下列命令）。
   ```bash
   mysql -h 119.29.236.9 -P 3306 -u malluser -pmall -e "create database if not exists mall character set utf8mb4;"
   mysql -h 119.29.236.9 -P 3306 -u malluser -pmall mall < document/sql/mall.sql
   ```
2. **启动 mall-admin / mall-portal**：使用统一脚本 `document/sh/start-dev-cloud.sh`。
   ```bash
   # 启动管理后台后端
   bash document/sh/start-dev-cloud.sh mall-admin

   # 启动前台后端
   bash document/sh/start-dev-cloud.sh mall-portal
   ```
   脚本会自动：
   - 切换到项目根目录，创建 `.m2-local` 作为私有 maven 仓库；
   - 导出云库环境变量，设置 `JAVA_HOME` 为 JDK8；
   - 执行 `mvn -Dmaven.repo.local=.m2-local -pl <module> -am install -DskipTests` 以确保依赖齐全；
   - 进入模块目录执行 `mvn spring-boot:run -Dspring-boot.run.profiles=dev`。
3. **健康检查**：可直接执行 `scripts/health-check.sh`，（会调用 `/auth/login` + `/veg/categories` + `/actuator/health`），等价命令如下：
   ```bash
   # 管理后台登录
   curl -X POST http://127.0.0.1:8080/admin/login \
     -H 'Content-Type: application/json' \
     -d '{"username":"admin","password":"macro123"}'

   # 前台健康检查
   curl http://127.0.0.1:8085/actuator/health
   ```

## 4. 前台（mall-app-web）联调

1. **配置 API 基址**：编辑 `mall-app-web/utils/appConfig.js`，确保：
   ```js
   export const API_BASE_URL = 'http://127.0.0.1:8085';
   ```
   - 若要在手机/模拟器访问，请将 `127.0.0.1` 替换为本机局域网 IP（例如 `http://192.168.1.12:8085`），并在 `manifest.json` 的 `h5.domain` 设置相同域名。
2. **运行 H5 应用**（推荐使用 HBuilder X）：
   - 打开 HBuilder X → `文件 → 打开目录` 选择 `/Users/wangkunyu/develop/mall-app-web`；
   - 菜单 `运行 → 运行到浏览器 → Chrome`；若浏览器未自动打开，可手动访问 `http://localhost:8060`；
   - 在浏览器中切换到移动端调试视图，完成登录（测试账号 `test/123456`）。
3. **Token 存储与请求**：`utils/requestUtil.js` 会在请求前自动携带 `Authorization` 头；登录成功后会把 token 存在 `uni.getStorageSync('token')`；若切换端口或 IP，请清除浏览器缓存后重新登录。

## 5. 管理后台前端（可选）

若需要 `mall-admin-web` 配合联调：
1. 在管理前端项目中将环境变量或 `config/dev.env.js` 的 `BASE_API` 指向 `http://127.0.0.1:8080`；
2. 执行 `npm install && npm run dev`；
3. 登录账号同 `mall-admin`（admin/macro123）。

> 新增的蔬菜管理接口位于 `mall-admin`：
> - 分类：`GET/POST /veg/category/**`
> - 商品：`GET/POST /veg/product/**`
> - 订单：`GET/POST /veg/order/**`（支持更新状态、调整明细）。
> 若通过 `mall-admin-web` 联调，可在菜单中接入上述接口实现运营维护；若暂未对接前端，也可用 Postman 直接调用。

## 6. 联调流程建议

1. **数据探活**：
   - 登录后台（admin/macro123）→ 获取 token → 调用 `POST /product/update/publishStatus` 修改商品状态 → 在 MySQL 中查询 `pms_product` 验证；
   - 登录前台（test/123456）→ 获取 token → 调用 `GET /home/content`、`GET /product/detail/{id}`，验证业务数据可读；
   - 若需要自测下单流程，可依次调用 `cart/add`、`order/generateConfirmOrder`、`order/generateOrder`，并在 `oms_order` 中校验。
2. **前端页面联调**：
   - 后台：使用 `mall-admin-web` 或 Postman 等工具完成 CRUD；
   - 前台：在浏览器/真机上进入 mall-app-web H5，登录后检查首页、商品详情、购物车、订单列表等；
   - 测试支付回调时可调用 `POST /order/paySuccess`（模拟）。
3. **日志观察**：
   - mall-admin 日志：`tail -f /tmp/start-mall-admin.log`；
   - mall-portal 日志：`tail -f /tmp/start-mall-portal.log`；
   - 若需要单独查看 Spring Boot 输出，可在脚本中移除 `nohup` 或使用 `mvn spring-boot:run` 直接前台运行。

## 7. 自动化检测（已落实）

> 目的：在手工联调前，用脚本快速确认云端数据库与两个后端服务就绪，避免人为误操作。

1. **后端集成测试**（mall-admin & mall-portal）
   - 直接运行 `scripts/run-integration-tests.sh` 即可完成以下步骤：
   ```bash
   # 确保导出云库环境变量
   export MALL_DB_URL="jdbc:mysql://119.29.236.9:3306/mall?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
   export MALL_DB_USERNAME="malluser"
   export MALL_DB_PASSWORD="mall"
   mvn test -DskipTests=false
   ```
   - 关键用例：`mall-admin` 的 `DataSourceConnectivityTest`（校验 spring.datasource 连接串）与 `mall-portal` 的 `PortalProductDaoTests`（查询促销商品）已经接入，只要测试全绿即可证明后端可连云库。
2. **API 级探活脚本**
   - 可使用 `scripts/probe-admin.sh` 与 `scripts/probe-portal.sh` 完成以下验证：
   ```bash
   # 管理后台登录并更新商品状态
   ADMIN_TOKEN=$(curl -s http://127.0.0.1:8080/admin/login \
     -H 'Content-Type: application/json' \
     -d '{"username":"admin","password":"macro123"}' | jq -r '.data.token')
   curl -s -X POST "http://127.0.0.1:8080/product/update/publishStatus?ids=26&publishStatus=0" \
     -H "Authorization: Bearer ${ADMIN_TOKEN}"

   # 前台登录与首页内容
   PORTAL_TOKEN=$(curl -s -X POST "http://127.0.0.1:8085/sso/login?username=test&password=123456" | jq -r '.data.token')
   curl -s http://127.0.0.1:8085/home/content -H "Authorization: Bearer ${PORTAL_TOKEN}" | jq '.code'
   ```
   - `scripts/probe-portal.sh` 默认尝试 `POST /auth/login`（失败则自动注册一次），随后依次调用 `/veg/categories`、`/veg/products`；若任一返回非 200 会立即终止。
   - `scripts/probe-admin.sh` 在登录 admin 后请求 `/veg/order/list`，确保后台新增的蔬菜订单接口可用。
3. **SQL 校验**
   - 可直接执行 `scripts/sql-check.sh`：
   ```bash
   mysql -h 119.29.236.9 -P 3306 -u malluser -pmall -e "use mall; select count(*) from pms_product;"
   ```
   - 若出现 `Unknown database 'mall'` 或 count=0，说明云库被清空，需要重新导入 `document/sql/mall.sql`。

## 8. 自动化脚本与流程

| 脚本 | 作用 |
| --- | --- |
| `scripts/db-bootstrap.sh` | 根据当前配置创建 `mall` 数据库并导入 `document/sql/mall.sql`。|
| `scripts/run-integration-tests.sh` | 导出云库环境变量后执行 `mvn test -DskipTests=false`。|
| `scripts/health-check.sh` | 运行文档第 3 节的健康检查命令，校验 admin 登录与 portal /actuator。|
| `scripts/sql-check.sh` | 查询 `pms_product` 数量，确认云库数据存在。|
| `scripts/probe-admin.sh` | 模拟后台登录并访问 `/veg/order/list` 验证蔬菜订单接口。|
| `scripts/probe-portal.sh` | 使用 `/auth/login`（必要时自动注册）后访问 `/veg/categories`、`/veg/products`。|
| `scripts/integration-pipeline.sh` | 编排上述脚本并在关键节点提示人工操作（启动服务、运行 HBuilder X）。|

> 运行 `scripts/integration-pipeline.sh` 将按照“数据库导入 → 提示手动启动 mall-admin/mall-portal → 自动执行测试/探活 → 提示手动运行 mall-app-web”顺序执行。若某一步需要人工配合（例如在 HBuilder X 中启动前端），脚本会暂停并提示完成后按 Enter 继续。

## 9. 常见问题排查

| 问题 | 排查步骤 |
| --- | --- |
| 启动时报 `Public Key Retrieval is not allowed` | 确保 JDBC URL 已含 `allowPublicKeyRetrieval=true`，并在 MySQL 中将用户改为 `mysql_native_password`。|
| `Unknown database 'mall'` | 执行 `create database mall` 后重新导入 `document/sql/mall.sql`。|
| 8080/8085 端口占用 | `lsof -i :8080` / `lsof -i :8085` 找到旧进程 `kill <PID>`，再重启脚本。|
| 前端请求 401 | 确保登录接口成功、token 写入 `uni` 存储；若切换 API 域名需清理缓存。|
| 手机/模拟器无法访问 | 将 `API_BASE_URL` 改成局域网 IP，确认本机防火墙允许外部访问 8080/8085/8060。|

## 10. 后续建议

- 将 `document/sh/start-dev-cloud.sh` 加入 shell alias（例如 `alias mall-admin-dev='bash document/sh/start-dev-cloud.sh mall-admin'`），提高启动效率；
- 新增联调专用 `.env` 或 `.envrc`，便于团队成员共享云端数据库配置；
- 若需 CI 自动化，可在测试前执行 `mysql ... mall < document/sql/mall.sql` 以确保环境一致，再运行 `mvn test -DskipTests=false`。

## 附录：前端调试备忘

- HBuilder X 项目路径默认使用 `/Users/wangkunyu/develop/mall-app-web`。如果将前端迁移到其他目录，可在 `scripts/integration-pipeline.sh` 顶部设置 `FRONTEND_PATH` 环境变量覆盖（示例：`FRONTEND_PATH=/path/to/new/app bash scripts/integration-pipeline.sh`）。
- DevTools 日志：运行到浏览器后，建议打开 Chrome DevTools 的 Console 和 Network：
  - 在 Console 执行 `getApp().$vm.$store.state` 可查看 `user`、`cart` 模块数据，确认 token、菜篮同步情况。
  - Network 面板中筛选 `veg`、`auth` 请求，可直观看到 401/404 等问题。
- Token 清理：若需模拟登录过期，可在控制台执行 `uni.setStorageSync('token', '')` 后重启页面。
