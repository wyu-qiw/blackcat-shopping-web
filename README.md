# 线上购物平台（Spring Boot 3 + Vue 3）

一套前后端分离的线上购物交易平台，包含普通用户与平台管理员双角色，权限严格隔离。后端采用 Spring Boot 3 + MyBatis-Plus + MySQL 8，前端采用 Vue 3 + Vue Router + Pinia + Element Plus。

## 一、功能清单

### 普通用户（USER）
- 注册、登录、退出登录
- 公开浏览所有已上架商品，查看商品封面图、详细描述、售价、状态标签
- 自主发布出售商品（名称、价格、详情描述、图片上传）
- 购买平台在售商品并生成订单记录
- 个人中心查看：我发布的商品、我的购买订单、个人信息
- 无任何后台管理权限，无法访问管理员页面

### 平台管理员（ADMIN）
- 拥有普通用户全部浏览、购买、发布商品功能
- 商品全局管理：查看全平台商品，执行上架 / 下架
- 手动修改商品状态：可售 / 已预购 / 无库存
- 数据大盘：用户总数、商品总数、在售商品数、已下架商品数、订单数量
- 查看全平台用户列表、订单列表、商品列表
- 专属路由拦截，普通用户无法进入后台页面

## 二、商品状态与购买规则

| 状态 | 前台展示 | 是否可购买 | 说明 |
| --- | --- | --- | --- |
| 可售（ONSALE） | 展示 | 可以 | 用户可浏览并购买 |
| 已预购（RESERVED） | 展示 | 禁止 | 购买成功后自动变为该状态，仅可查看 |
| 无库存（OUT_OF_STOCK） | 展示 | 禁止 | 前台隐藏购买按钮 |
| 下架（listed = false） | 不展示 | 禁止 | 仅后台管理员可见 |

说明：
- 普通用户发布商品后自动上架，初始状态为“可售”。
- 购买成功后生成订单，商品状态自动变为“已预购”。
- 只有管理员可以手动切换三种商品状态和上下架。

## 三、技术栈

| 端 | 技术 |
| --- | --- |
| 后端 | Java 17、Spring Boot 3.2.5、MyBatis-Plus 3.5.7、MySQL 8、JWT、BCrypt |
| 前端 | Vue 3.4、Vue Router 4、Pinia 2、Element Plus 2、Axios、Vite 5 |

## 四、项目目录结构

```text
线上购物平台/
├── backend/                        # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/shop/
│       │   ├── ShopApplication.java
│       │   ├── common/             # 统一返回、异常、JWT、登录上下文
│       │   ├── config/             # 跨域、拦截器注册、密码、初始化管理员
│       │   ├── controller/         # 用户端与管理端 Controller
│       │   ├── dto/                # 请求参数
│       │   ├── entity/             # 用户、商品、订单实体
│       │   ├── enums/              # 角色与状态枚举
│       │   ├── interceptor/        # 登录与权限拦截器
│       │   ├── mapper/             # MyBatis-Plus Mapper
│       │   ├── service/            # 业务层
│       │   └── vo/                 # 返回视图对象
│       └── resources/
│           └── application.yml
├── frontend/                       # Vue3 前端
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── api/                    # axios 封装与接口定义
│       ├── layouts/                # 用户端 / 管理端布局
│       ├── router/                 # 路由与权限守卫
│       ├── stores/                 # Pinia 用户状态
│       ├── utils/                  # 状态展示工具
│       └── views/                  # 所有页面
├── sql/
│   └── shopping_platform.sql       # MySQL 建表脚本
└── docs/
    └── API.md                      # 完整接口文档
```

## 五、环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.x
- Node.js 18+
- 推荐使用 IDEA / VS Code 打开前后端目录

## 六、快速启动

### 1. 初始化数据库

打开 MySQL，执行建表脚本：

```bash
mysql -u root -p < sql/shopping_platform.sql
```

### 2. 启动后端

修改 `backend/src/main/resources/application.yml` 中的数据库账号密码：

```yaml
spring:
  datasource:
    username: root
    password: root
```

然后启动后端：

```bash
cd backend
mvn spring-boot:run
```

首次启动会自动创建默认管理员账号：`admin / admin123`。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

浏览器访问：`http://localhost:5173`

### 4. 默认账号

| 身份 | 账号 | 密码 |
| --- | --- | --- |
| 平台管理员 | admin | admin123 |
| 普通用户 | 前端注册页自行注册 | - |

## 七、运行说明

- 后端接口地址：`http://localhost:8080`
- 前端开发代理：`/api` 与 `/uploads` 自动代理到后端
- 商品图片默认上传到后端运行目录下的 `uploads` 文件夹
- 所有接口统一返回 `{ code, msg, data }` 格式

详细接口文档见 [docs/API.md](docs/API.md)。

