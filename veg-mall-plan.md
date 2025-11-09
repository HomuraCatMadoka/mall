# 蔬菜商城前后端改造计划

## 1. 场景与需求基线
- 核心目标：仅支持普通用户在线挑选蔬菜并提交订单；无库存、无支付、无配送时间与活动模块。
- 用户身份：唯一角色为注册用户，注册信息只包含用户名与手机号；注册即视为可下单，不再额外校验。
- 用户流程：注册/登录 → 进入分类页（展示商家预设分类快捷入口）→ 浏览或搜索蔬菜 → 加入购物车 → 提交订单；提交后订单即锁定，用户不可修改或取消。
- 商品信息：仅展示规格（500g/份、盒装等）与价格；图片为可选项，可在商家后台新增或日后补充。
- 分类机制：采用后端动态分类（方案B），运营可在后台增删改分类，前端实时获取并缓存；支持关键字搜索，不提供筛选条件。
- 运营规则：
  - 订单生成后由后台人工调整（增删蔬菜、修改规格单价或给特定客户优惠）。
  - 系统不做库存校验与支付流程；订单状态仅需“待处理/已处理/已关闭”。
  - 商品规格信息支持后台修改，前端读取最新配置后展示。

## 2. 前端改造方案（uni-app 项目）
### 2.1 配置与数据层
1. `utils/appConfig.js`
   - 调整 `API_BASE_URL` 指向蔬菜商城后端；新增 `categoryApi`, `productApi`, `orderApi` 分组常量，便于与原 mall 接口隔离。
2. `api/` 层
   - 新增 `api/vegCategory.js`、`api/vegProduct.js`、`api/vegOrder.js`，封装分类、商品、订单请求；保留原有 mall 接口以防回退。
   - `api/request.js`（若存在）中增加错误处理：下单成功后直接跳转订单详情，无支付校验。
3. `store/`
   - `store/modules/user.js`：注册成功后只缓存用户名与手机号，去掉无关字段。
   - `store/modules/cart.js`：去除库存相关逻辑；新增对 `spec`（规格字符串）与 `price` 的直接引用，订单提交 payload 包含 `items[{productId, specId, quantity, price}]`。

### 2.2 页面与组件
1. `pages/public/register.vue` / `login.vue`
   - 表单字段仅保留用户名、手机号、验证码（如需）；注册后直接跳回分类页。
2. `pages/category/index.vue`
   - 接入新分类接口；分类卡片展示名称与可选图标；提供顶部搜索框（可复用 `components/mix-search-bar.vue`）。
   - 点击分类进入 `pages/product/product.vue` 并透传分类 ID。
3. `pages/product/product.vue`
   - 列表项展示商品名称、规格、价格；可选图片占位图。
   - 支持关键字搜索（调用 `/veg/products?keyword=`）；去除品牌筛选、销量过滤等无关功能。
4. `pages/product/detail.vue`
   - 仅保留规格、价格、可选图片区域；隐藏优惠券、服务保障、满减等区块。
5. `pages/cart/index.vue`
   - 去掉库存提示与失效商品逻辑；保留数量增减、备注输入。
   - 结算按钮直接跳 `pages/order/confirm.vue`，不校验库存或支付方式。
6. `pages/order/confirm.vue`
   - 表单仅包含备注（如“少泥”等）；展示订单明细（商品+规格+单价+数量）。
   - 提交时调用 `/veg/orders`，成功后跳至订单列表。
7. `pages/order/list.vue` / `detail.vue`
   - 状态枚举改为“待处理/已处理/已关闭”；移除支付倒计时、物流等模块。
   - 详情页新增“后台处理说明”占位，供后期展示运营备注。
8. `components`
   - 新增 `components/veg-category-card.vue`、`components/veg-product-card.vue`、`components/veg-order-summary.vue` 以实现可复用 UI。
   - 若暂不提供图片，组件内加入默认插画占位。
9. `uni.scss` 与主题
   - 增加与蔬菜相关的配色变量（如 `--color-veg-primary`），统一替换按钮、标签颜色。

### 2.3 路由与文案
- `pages.json`
  - 更新页面标题（如“蔬菜分类”“确认蔬菜订单”），确保新组件已注册。
- `static/` 与 `images/`
  - 准备基础占位图、logo；允许后端上传图后再替换。
- 全局文案与占位提示统一替换为蔬菜场景，例如“加入菜篮”“我的蔬菜订单”。

## 3. 后端改造方案（mall-portal 模块为例）
### 3.1 数据模型
1. `pms_product` 表
   - 保留商品主键与价格；新增或复用 `spec_id/spec_name`、可编辑 `spec_desc`（如“500g/份”）。
   - 图片字段可留空；若未来上传，前端直接读取 URL。
2. 新增 `pms_veg_category` 表
   - 字段：`id`, `name`, `icon`, `sort`, `parent_id`, `status`。
   - 供前端分类入口实时加载。
3. `oms_order` 与 `oms_order_item`
   - 精简字段，仅保留 `member_id`, `order_sn`, `status`, `total_amount`, `remark`, 时间戳。
   - `oms_order_item` 记录 `product_id`, `product_name`, `spec_desc`, `price`, `quantity`。
   - 允许后台修改 `price`、`spec_desc`，以满足特殊优惠场景。

### 3.2 接口与业务逻辑
1. 分类接口
   - `GET /veg/categories`：返回树形或扁平列表；支持 `parentId` 过滤。
   - 后台维护接口（可放 `mall-admin`）：CRUD + 排序。
2. 商品接口
   - `GET /veg/products?categoryId=&keyword=`：分类/搜索查询。
   - `GET /veg/products/{id}`：详情，返回规格、价格、图片 URL（可空）。
   - `POST /veg/products/{id}/spec`（后台）允许修改规格与价格。
3. 订单接口
   - `POST /veg/orders`：创建订单；入参仅 `memberId`, `items`, `remark`。
   - `GET /veg/orders`：按用户查询订单列表。
   - `GET /veg/orders/{id}`：订单详情。
   - 订单创建后默认状态 `PENDING`，后台可通过 `PUT /veg/orders/{id}` 更新为 `PROCESSED` 或 `CLOSED`，并可附加 `operatorRemark`。
   - 无收货地址字段，默认由线下/后台掌握配送信息。
4. 校验逻辑
   - 仅校验商品是否存在与上架；不做库存/支付校验。
   - 需要支持后台修改订单明细：可通过 `PUT /veg/orders/{id}/items` 接口实现增删/调价。

### 3.3 系统与后台
- `mall-admin` 侧新增蔬菜分类、商品、订单管理页面；表单字段与前端展示同步（规格、价格、可选图片）。
- 在现有安全机制下沿用会员认证；注册接口只需校验手机号唯一。
- 运营需要的“订单调价/增删项”操作需记录操作人及时间，方便审计。

### 3.4 接口示例（供联调参考）
> 以下示例默认前缀为 `/veg`，响应结构统一包裹 `code`, `message`, `data` 三段式，`code=200` 代表成功。

1. **获取分类列表**  `GET /categories`
   - 请求参数：`parentId`（可选，默认返回顶级分类）
   - 响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {"id": 1, "name": "叶菜类", "icon": "", "sort": 1, "parentId": 0},
    {"id": 2, "name": "根茎类", "icon": "", "sort": 2, "parentId": 0}
  ]
}
```

2. **按分类/关键字查询商品**  `GET /products`
   - 请求参数：`categoryId`（可选）、`keyword`（可选）、`pageNum`、`pageSize`
   - 响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 101,
        "name": "上海青",
        "specId": 501,
        "specDesc": "500g/份",
        "price": 6.9,
        "coverImage": "",
        "categoryId": 1,
        "status": 1
      }
    ],
    "pageNum": 1,
    "pageSize": 10,
    "total": 12
  }
}
```

3. **商品详情**  `GET /products/{id}`
   - 响应字段：`id`, `name`, `specId`, `specDesc`, `price`, `images[]`, `status`, `remark`

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 101,
    "name": "上海青",
    "specId": 501,
    "specDesc": "500g/份",
    "price": 6.9,
    "images": [],
    "remark": "当天采摘"
  }
}
```

4. **创建订单**  `POST /orders`
   - 请求体：

```json
{
  "memberId": 3001,
  "remark": "少放泥",
  "items": [
    {"productId": 101, "productName": "上海青", "specDesc": "500g/份", "price": 6.9, "quantity": 2},
    {"productId": 102, "productName": "胡萝卜", "specDesc": "800g/袋", "price": 5.5, "quantity": 1}
  ]
}
```

   - 响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "orderId": 90011,
    "orderSn": "VEG202405180001",
    "status": "PENDING",
    "totalAmount": 19.3
  }
}
```

5. **查询订单列表**  `GET /orders`
   - 请求参数：`memberId`（必填）、`status`（可选）、`pageNum`、`pageSize`
   - 响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "orderId": 90011,
        "orderSn": "VEG202405180001",
        "status": "PENDING",
        "totalAmount": 19.3,
        "createdTime": "2024-05-18 09:00:21"
      }
    ],
    "pageNum": 1,
    "pageSize": 10,
    "total": 5
  }
}
```

6. **后台调整订单明细**  `PUT /orders/{id}/items`
   - 请求体示例：

```json
{
  "operator": "admin",
  "items": [
    {"productId": 101, "specDesc": "500g/份", "price": 5.9, "quantity": 2},
    {"productId": 105, "specDesc": "1kg/袋", "price": 8.0, "quantity": 1}
  ],
  "operatorRemark": "上海青优惠1元，追加土豆"
}
```

   - 响应示例：`{"code":200,"message":"updated","data":null}`

7. **后台更新商品规格/价格**  `PUT /products/{id}/spec`
   - 请求体示例：`{"specId":501,"specDesc":"300g/盒","price":4.5}`
   - 用于满足“规格可后台修改”要求，修改后前端下次拉取即可展示最新规格。

### 3.5 用户注册/登录接口
> 仍沿用 `mall-portal` 的会员体系，只保留用户名 + 手机号作为前台必填项，密码或验证码策略可根据安全要求选取其一。

1. **用户注册**  `POST /auth/register`
   - 请求体示例：

```json
{
  "username": "zhangsan",
  "mobile": "13800001111",
  "password": "123456",           // 如果采用短信登录，可改为 smsCode
  "smsCode": "879012"             // 可选；当采用短信校验时必填
}
```

   - 响应示例：

```json
{
  "code": 200,
  "message": "registered",
  "data": {
    "memberId": 3001,
    "username": "zhangsan",
    "mobile": "13800001111",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

   - 业务校验：手机号唯一；若已存在则返回 `code!=200`（如 `409`）。

2. **用户登录**  `POST /auth/login`
   - 请求体（账号密码登录）：

```json
{
  "loginType": "password",       // 或 "sms"
  "username": "zhangsan",
  "password": "123456"
}
```

   - 若使用短信验证码：`{"loginType":"sms","mobile":"13800001111","smsCode":"879012"}`。
   - 响应示例沿用注册接口，返回 `token`、`memberId` 供前端缓存。

3. **获取用户信息**  `GET /member/profile`
   - 用于前端在登录成功后拉取用户名、手机号等展示。
   - 响应示例：`{"code":200,"data":{"memberId":3001,"username":"zhangsan","mobile":"13800001111"}}`

> 前端 `pages/public/register.vue`、`login.vue` 直接对接上述接口，成功后写入 `store/modules/user.js`，即可完成最小注册/登录闭环。

## 4. 任务拆解与优先级
| 序号 | 任务 | 输出 | 依赖 |
| --- | --- | --- | --- |
| F1 | 完成 `api` 与 `store` 层重构（新增蔬菜接口、精简用户/购物车状态） | 可调用的新接口封装、文档 | 需求基线 |
| F2 | 分类/搜索/商品列表页面改造 | `pages/category`, `pages/product` 完成并可联调 | F1, B1 |
| F3 | 购物车与订单流程改造 | `pages/cart`, `pages/order` 提交成功并跳转 | F1, B2 |
| F4 | 认证页面与文案适配 | 登录/注册仅保留用户名+手机号 | 无 |
| B1 | 数据表设计与迁移脚本（分类/商品/订单） | SQL 脚本、实体更新 | 需求基线 |
| B2 | 分类/商品/订单 API 开发 | `/veg/*` 接口可联调 | B1 |
| B3 | 后台管理功能与订单调价能力 | 管理端页面、权限配置 | B1, B2 |
| B4 | 回归测试与对接文档 | Postman 集合、接口说明 | B2 |

> 建议先完成 B1→B2→F1→F2/F3 的主链路，确保前端可以连通新的后端接口；随后补充后台管理（B3）与测试交付（B4）。
