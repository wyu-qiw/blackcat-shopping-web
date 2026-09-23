# 黑猫优选 · Black Cat Preferred Online Shopping

一套前后端分离的线上购物交易平台，包含**普通用户（USER）**与**平台管理员（ADMIN）**双角色、严格权限隔离。
后端采用 Spring Boot 3 + MyBatis-Plus + MySQL，前端采用 Vue 3 + Vue Router + Pinia + Element Plus + ECharts。

> 当前版本数据规模（开发库 `shopping_platform`）：10 张数据表、10 个商品分类、5 个品牌、32 件商品、3 个账号。

---

## 一、功能清单

### 普通用户（USER）
- 注册、登录、退出登录（JWT 鉴权，密码 BCrypt 加密）
- 首页浏览、关键字搜索、按商品分类筛选商品
- **商品多图**：发布/编辑商品最多上传 8 张图片
  - 商品详情页以**轮播图**形式展示，图片可点击放大预览
  - 商城列表以**第一张图作为封面**
  - 卖家可在编辑页把任意一张「设为封面」
- **品牌自动识别**：发布商品时按标题/描述关键词自动匹配品牌（也可手动选择/留空）
- 商品评论：查看与发表评论
- **购物车**：加入/移除/勾选/清空，购物车勾选结算（同商品自动去重）
- 下单购买、生成订单；订单支持取消与申请退款
- 个人中心：我的商品（编辑/上下架/删除）、我的订单、个人信息维护、修改密码（带每月限额与变更记录）
- 无任何后台管理权限，无法进入管理员页面

### 平台管理员（ADMIN）
- 拥有普通用户的全部浏览、购物、发布能力
- **数据大盘**：用户总数、商品总数、在售/下架数、订单数，销量榜、销售趋势、分类占比（ECharts 图表）
- **商品管理（三级可展开结构）**：
  - **商品列表**：全部商品展示，格式与商城一致
  - **商品分类**：与商城分类同源动态同步（新增分类自动出现），可按分类筛选
  - **品牌分类**：品牌可再展开，点击品牌查看该品牌下全部商品
- **对每一个商品的完整编辑权**：名称、价格、分类、品牌、图片、描述均可编辑，并可：
  - 上架 / 下架
  - 切换商品状态（可售 / 已预购 / 无库存）
  - **移动商品到其他分类、其他品牌**（分类与品牌为两个独立维度，可同时归属、互不冲突）
  - 删除任意商品
- 用户管理：查看全平台用户
- 订单管理：查看全平台订单，处理退款审批、撤销订单、删除订单
- 专属路由 + 拦截器双重隔离，普通用户无法进入后台

---

## 二、商品与订单状态

### 商品状态（`status`）
| 状态 | 前台展示 | 是否可购买 | 说明 |
| --- | --- | --- | --- |
| ONSALE 可售 | 展示 | 可以 | 用户可浏览购买 |
| RESERVED 已预购 | 展示 | 禁止 | 购买成功后自动变为该状态 |
| OUT_OF_STOCK 无库存 | 展示 | 禁止 | 前台隐藏购买按钮 |
| 下架（listed = false） | 不展示 | 禁止 | 仅后台管理员可见 |

### 订单状态（`order_status` 字典）
| 状态码 | 名称 |
| --- | --- |
| PAID | 已支付 |
| CANCELLED | 已取消 |
| REFUNDING | 退款中 |
| REFUNDED | 已退款 |

---

## 三、商品分类与品牌

- **商品分类（10 个）**：生活用品、运动、数码科技、食品生鲜、服饰鞋包、美妆个护、图书文娱、家居家装、母婴玩具、办公用品。
- **品牌（5 个）**：苹果 Apple、联想 Lenovo、耐克 Nike、晨光 M&G、巴黎欧莱雅 L'Oréal Paris。
- 每个品牌带 `keywords` 关键词，用于发布商品时的自动识别。
- 商品的 `category_id`（分类）与 `brand_id`（品牌）是**两个独立字段**，可同时归属、互不冲突。

---

## 四、技术栈

| 端 | 技术 |
| --- | --- |
| 后端 | Java 17、Spring Boot 3.2.5、MyBatis-Plus 3.5.7、MySQL（8.x 驱动）、JWT、BCrypt、Maven |
| 前端 | Vue 3.4、Vue Router 4、Pinia 2、Element Plus 2、Axios、ECharts 5、Vite 5 |
| AI | DeepSeek（OpenAI 兼容接口，`deepseek-chat`），用于智能助手对话与商品描述生成 |

---

## 五、项目目录结构

```text
Black-Cat-Preferred-Online-Shopping/
├── backend/                        # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/shop/
│       │   ├── ShopApplication.java
│       │   ├── common/             # 统一返回、业务异常、JWT、登录上下文
│       │   ├── config/             # 跨域、拦截器注册、密码、上传路径、初始化管理员
│       │   ├── controller/         # 用户端 / 管理端 / 认证 / 文件 / AI 等控制器
│       │   ├── dto/                # 请求参数对象
│       │   ├── entity/             # User、Product、Order、Cart、Brand、Category 等实体
│       │   ├── enums/              # 角色 / 商品状态 / 订单状态枚举
│       │   ├── interceptor/        # 登录与权限拦截器
│       │   ├── mapper/             # MyBatis-Plus Mapper
│       │   ├── service/            # 业务逻辑层
│       │   └── vo/                 # 返回视图对象
│       └── resources/application.yml
├── frontend/                       # Vue3 前端
│   ├── package.json
│   ├── vite.config.js
│   ├── public/_redirects           # SPA 路由回退（Cloudflare Pages 等）
│   └── src/
│       ├── api/                    # axios 封装与全部接口定义
│       ├── components/             # ProductCard、MultiImageUpload、ChatAssistant 等
│       ├── layouts/                # 用户端 / 商城 / 管理端布局
│       ├── router/                 # 路由与权限守卫
│       ├── stores/                 # Pinia（user、cart）
│       ├── utils/                  # 状态展示工具
│       └── views/                  # 用户端页面 + admin/ 后台页面
├── sql/                            # 建表与增量脚本 v2 ~ v6
├── docs/API.md                     # 接口文档
├── 项目上下文摘要.md                # 开发交接文档（演进历史 / 约定 / 踩坑）
└── package.json                    # 仓库根构建入口（转发到 frontend/）
```

> 注：`backend/uploads/`（商品图片）、`backups/`（本地备份）、`node_modules`、`target`、`dist`
> 均通过 `.gitignore` 忽略，不纳入版本库。

---

## 六、环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.x（开发库使用 8.x 驱动连接）
- Node.js 18+

---

## 七、快速启动

### 1. 初始化数据库

先执行主建表脚本，再按顺序执行增量脚本（v2 ~ v6）：

```bash
mysql -u root -p < sql/shopping_platform.sql
mysql -u root -p < sql/v2_features.sql
mysql -u root -p < sql/v3_cart.sql
mysql -u root -p < sql/v4_brand.sql
mysql -u root -p < sql/v5_brand_detect.sql
mysql -u root -p < sql/v6_product_images.sql
```

### 2. 启动后端

修改 `backend/src/main/resources/application.yml` 中的数据库账号密码：

```yaml
spring:
  datasource:
    username: root
    password: 你的密码
```

```bash
cd backend
mvn spring-boot:run
```

首次启动会自动创建默认管理员：`admin / admin123`。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

浏览器访问：`http://localhost:5173`。

### 4. 默认账号

| 身份 | 账号 | 密码 |
| --- | --- | --- |
| 平台管理员 | admin | admin123 |
| 普通用户（卖家） | yuqi | 123456 |
| 普通用户（测试买家） | buytest | 123456 |

---

## 八、构建与运行说明

### 前端
- 开发：`npm run dev`（端口 5173）
- 构建：`npm run build`
- 产物目录：`frontend/dist/`

### 后端
- 构建打包：`mvn clean package`
- 产物目录：`backend/target/`（可执行 jar）
- 运行：`mvn spring-boot:run` 或 `java -jar target/*.jar`（端口 8080）

### 其它
- 后端接口地址：`http://localhost:8080`
- 前端开发代理：`/api` 与 `/uploads` 自动代理到后端
- API 基地址可用环境变量 `VITE_API_BASE_URL` 覆盖（默认 `/api`）
- 商品图片上传到后端运行目录下的 `uploads/`，经 `/uploads/**` 对外提供
- 所有接口统一返回 `{ code, msg, data }`；业务异常返回 HTTP 200、错误码在 body.code

详细接口见 [docs/API.md](docs/API.md)，开发演进与约定见 [项目上下文摘要.md](项目上下文摘要.md)。