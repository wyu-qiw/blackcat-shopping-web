# 接口文档

基础地址：`http://localhost:8080/api`

认证方式：除公开接口外，请求头需携带：

```http
Authorization: Bearer <token>
```

## 统一返回格式

所有接口返回：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

| code | 含义 |
| --- | --- |
| 200 | 成功 |
| 400 | 参数或业务错误 |
| 401 | 未登录或登录失效 |
| 403 | 无权限（普通用户访问管理员接口） |
| 404 | 请求的资源或接口不存在 |
| 500 | 系统异常 |

## 一、认证接口

### 1. 用户注册

- 请求方式：`POST /auth/register`
- 权限：公开

请求参数：

```json
{
  "username": "zhangsan",
  "password": "123456",
  "nickname": "张三"
}
```

返回示例：

```json
{
  "code": 200,
  "msg": "注册成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.xxx",
    "user": {
      "id": 2,
      "username": "zhangsan",
      "nickname": "张三",
      "role": "USER",
      "avatar": null,
      "createTime": "2026-09-01 10:00:00"
    }
  }
}
```

### 2. 登录

- 请求方式：`POST /auth/login`
- 权限：公开

请求参数：

```json
{
  "username": "admin",
  "password": "admin123"
}
```

返回 `token` 和 `user`，前端根据 `user.role` 自动跳转用户首页或管理后台。

### 3. 获取当前用户

- 请求方式：`GET /auth/me`
- 权限：登录

返回示例：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "平台管理员",
    "role": "ADMIN",
    "avatar": null,
    "createTime": "2026-09-01 09:00:00"
  }
}
```

## 二、用户端商品接口

### 1. 商品列表（前台首页）

- 请求方式：`GET /products`
- 权限：公开
- 说明：只返回已上架商品，按创建时间倒序

返回 `data` 为商品数组：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "sellerId": 2,
      "title": "二手山地自行车",
      "description": "九成新，适合校园通勤",
      "price": 350.00,
      "coverImage": "/uploads/abc.jpg",
      "status": "ONSALE",
      "listed": true,
      "createTime": "2026-09-01 10:30:00",
      "updateTime": "2026-09-01 10:30:00",
      "sellerName": "张三"
    }
  ]
}
```

### 2. 商品详情

- 请求方式：`GET /products/{id}`
- 权限：公开（已下架商品仅管理员可查看）

路径参数：`id` 商品ID。

### 3. 发布商品

- 请求方式：`POST /products`
- 权限：登录（普通用户与管理员均可）

请求参数：

```json
{
  "title": "二手山地自行车",
  "price": 350.00,
  "description": "九成新，适合校园通勤",
  "coverImage": "/uploads/abc.jpg"
}
```

发布后商品自动上架，`status` 为 `ONSALE`。

### 4. 我发布的商品

- 请求方式：`GET /products/mine`
- 权限：登录

## 三、用户端订单接口

### 1. 购买商品

- 请求方式：`POST /orders`
- 权限：登录

请求参数：

```json
{
  "productId": 1
}
```

业务规则：
- 商品必须已上架且状态为 `ONSALE`
- 购买成功后生成订单，商品状态自动变为 `RESERVED`（已预购）

### 2. 我的购买订单

- 请求方式：`GET /orders/mine`
- 权限：登录

## 四、图片上传

- 请求方式：`POST /files/upload`
- 权限：登录
- 请求参数：`file`（MultipartFile，支持 jpg/jpeg/png/gif/webp）

返回示例：

```json
{
  "code": 200,
  "msg": "上传成功",
  "data": "/uploads/uuid.jpg"
}
```

## 五、管理员接口

所有管理员接口都需要 `ADMIN` 角色，普通用户访问返回：

```json
{
  "code": 403,
  "msg": "无权限访问后台管理接口",
  "data": null
}
```

### 1. 数据大盘

- 请求方式：`GET /admin/stats`
- 权限：管理员

返回示例：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "userCount": 10,
    "productCount": 20,
    "onSaleCount": 15,
    "offShelfCount": 5,
    "orderCount": 8
  }
}
```

### 2. 全平台商品列表

- 请求方式：`GET /admin/products`
- 权限：管理员
- 说明：包含已下架商品

### 3. 修改商品状态

- 请求方式：`PUT /admin/products/{id}/status`
- 权限：管理员

请求参数：

```json
{
  "status": "OUT_OF_STOCK"
}
```

合法状态：`ONSALE`（可售）、`RESERVED`（已预购）、`OUT_OF_STOCK`（无库存）。

### 4. 上架 / 下架商品

- 请求方式：`PUT /admin/products/{id}/listed`
- 权限：管理员

请求参数：

```json
{
  "listed": true
}
```

`true` 上架，`false` 下架。下架后前台首页不再展示。

### 5. 全平台用户列表

- 请求方式：`GET /admin/users`
- 权限：管理员

### 6. 全平台订单列表

- 请求方式：`GET /admin/orders`
- 权限：管理员

## 五、二期新增接口

### 1. 商品分类列表

- 请求方式：`GET /categories`
- 权限：公开

### 2. 商品评论

- 评论列表：`GET /products/{id}/comments`（公开）
- 发表评论：`POST /products/{id}/comments`（需登录）

```json
{ "content": "这个商品很不错！" }
```

### 3. 售卖者管理自己商品

- 编辑商品：`PUT /products/{id}`（仅售卖者或管理员）
- 上架 / 下架：`PUT /products/{id}/listed`（仅售卖者或管理员）

```json
{ "title": "新标题", "price": 199, "description": "描述", "coverImage": "/uploads/xx.png", "categoryId": 1 }
```

### 4. 个人中心（每月最多修改 2 次，信息与密码共用额度）

- 修改个人信息：`PUT /users/profile`（需登录）
- 修改密码：`PUT /users/password`（需登录）
- 本月修改额度：`GET /users/profile/limit`（需登录）

```json
{ "nickname": "新昵称", "avatar": null }
{ "oldPassword": "原密码", "newPassword": "新密码" }
```

额度返回示例：

```json
{ "code": 200, "msg": "操作成功", "data": { "used": 1, "total": 2, "remaining": 1 } }
```

### 5. 商品搜索

- 请求方式：`GET /products?keyword=关键词`
- 权限：公开。按商品名称或描述模糊匹配，仅返回已上架商品。

### 6. 商品删除

- 请求方式：`DELETE /products/{id}`
- 权限：售卖者或管理员。删除商品及其价格记录、评论。

### 7. 订单取消 / 退款

- 买家取消订单：`PUT /orders/{id}/cancel`（已支付 → 已取消，商品恢复可售）
- 买家申请退款：`PUT /orders/{id}/refund`（已支付 → 退款中）
- 管理员处理退款：`PUT /admin/orders/{id}/refund`，参数 `{ "approved": true }`（同意，订单变已退款、商品恢复可售）或 `false`（驳回，订单恢复已支付）

## 六、数据库表

完整建表脚本见 [sql/shopping_platform.sql](../sql/shopping_platform.sql)。

| 表名 | 说明 |
| --- | --- |
| `user` | 用户表，`role` 区分 USER / ADMIN |
| `product` | 商品表，`status` 三种状态 + `listed` 上下架 + `category_id` 分类 |
| `orders` | 订单表，记录购买快照金额与买卖双方 |
| `category` | 商品分类表（生活用品 / 运动 / 数码科技） |
| `product_price` | 商品价格表，记录售价及价格变更历史 |
| `order_status` | 订单状态字典表（已支付 / 已取消 / 退款中 / 已退款） |
| `product_comment` | 商品评论表 |
| `user_change_log` | 用户信息/密码修改记录表（用于每月限额） |
