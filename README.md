# YOUXX

基于 Spring Boot + Vue 3 的前后端分离电商示例项目，包含一个面向消费者的商城前端、一个面向运营的后台前端，以及一套统一的后端服务。后端集成了大语言模型（LLM）导购 Agent，能够根据用户对话意图自动调用"加入购物车"工具。

## 核心特性

- 用户端与管理后台共用同一套后端 API，按 JWT 中的 `role` 字段区分权限
- 用户端：商品浏览、商品详情、购物车、下单、订单查询、收货地址管理、个人资料
- 管理后台：用户管理、商品上下架、订单管理、系统消息、平台设置
- 基于 langchain4j 的 AI 导购 Agent：会话级记忆隔离，可调用后端工具完成加购操作
- JWT 鉴权 + 拦截器统一校验，密码使用 Spring Security Crypto 加密
- 商品图片上传与静态资源映射，文件落盘到仓库根的 `upload_resources/`
- Redis 用于会话上下文与 Agent 对话消息缓存

## 技术栈

| 技术 | 用途 |
| --- | --- |
| Spring Boot 4.0.6 | 后端 Web 框架（Java 17） |
| MyBatis Spring Boot | 数据库访问层 |
| MySQL | 业务数据存储 |
| Spring Data Redis | 会话与对话消息缓存 |
| langchain4j 1.13.0 | LLM Agent 与工具调用 |
| jjwt 0.12.6 | JWT 签发与校验 |
| Vue 3 + Vue Router 4 + Vuex 4 | 前端 SPA 框架 |
| Element Plus | 前端 UI 组件库 |
| Axios | 前端 HTTP 客户端 |

## 项目结构

```
YOUXX/
├── youxx_backend/        后端服务（Spring Boot）
│   ├── src/main/java/org/youxx/
│   │   ├── controller/      HTTP 控制器（auth/user/product/order/message/llm）
│   │   ├── service/         业务接口与实现
│   │   ├── mapper/          MyBatis Mapper 接口
│   │   ├── entity/          数据库实体
│   │   ├── dto/             请求入参对象
│   │   ├── vo/              响应出参对象
│   │   └── common/          通用设施
│   │       ├── llm/           ShopAgent、OrderTools 等 LLM 组件
│   │       ├── security/      JwtUtil、PasswordEncoder
│   │       ├── interceptor/   JWT 拦截器
│   │       ├── config/        Redis/WebMvc/JWT/ChatModel 配置
│   │       └── service/       文件存储等通用服务
│   └── src/main/resources/
│       ├── application.yaml      主配置（占位符 + 环境变量）
│       ├── db/                   建表脚本与初始化数据
│       └── mapper/              MyBatis XML 映射
├── youxx_user/          消费者前端（Vue 3，端口 8080）
│   └── src/
│       ├── views/user/      首页/订单/消息/资料/设置
│       ├── services/        api.js、aiService.js、promptEngineer.js
│       ├── router/          路由
│       └── store/           Vuex 状态
├── youxx_admin/         管理后台前端（Vue 3，端口 8079）
│   └── src/
│       ├── views/admin/    用户/商品/订单/消息/设置/首页
│       ├── services/       api.js
│       └── router/         路由
└── upload_resources/    商品图片等静态资源运行时目录
```

后端各模块之间的关系：

- `controller` 接收请求 → `service` 处理业务 → `mapper` 访问数据库
- `interceptor/JwtTokenUserInterceptor` 在请求线程写入用户上下文，`service` 与 LLM 工具从中取 `userId`
- `common/llm/ShopAgent` 是 langchain4j AiServices 接口，`OrderTools` 提供 Agent 可调用的加购工具
- 用户端与管理端通过同一个 `/api/*` 前缀访问后端，前端 Axios `baseURL` 均为 `/api`

## 快速开始

### 环境要求

- JDK 17
- Maven 3.6+
- Node.js 16+（Vue CLI 5）
- MySQL 8
- Redis

### 后端启动

```bash
cd youxx_backend
# 先准备 dev 配置中的数据库与 Redis 连接信息
mvn spring-boot:run
```

后端默认监听 `8081`，前端通过 `/api` 反向代理到该端口。

### 前端启动

```bash
# 用户端
cd youxx_user
npm install
npm run serve    # http://localhost:8080

# 管理后台
cd youxx_admin
npm install
npm run serve    # http://localhost:8079
```

### 数据库初始化

```bash
mysql -u<user> -p<password> < youxx_backend/src/main/resources/db/youxx_schema.sql
mysql -u<user> -p<password> < youxx_backend/src/main/resources/db/init_data.sql
```

`youxx_schema.sql` 创建 `sys_user`、`product_category`、`product`、`orders`、`order_item`、`message`、`user_address`、`sys_settings` 共 8 张表。

## 配置说明

后端主配置 `application.yaml` 采用环境变量占位符，需要在 `application-dev.yaml` 或运行环境中提供以下值：

| 配置项 | 说明 |
| --- | --- |
| `spring.datasource.*` | MySQL 连接地址、驱动、账号密码 |
| `spring.data.redis.*` | Redis 主机、端口、库号、密码、超时 |
| `CHATMODEL_MODEL` / `CHATMODEL_API_KEY` / `CHATMODEL_BASE_URL` | LLM 模型名、API Key、Base URL（OpenAI 兼容接口） |
| `youxx.resource.base-dir` | 静态资源根目录，dev 环境需写死绝对路径 |
| `youxx.jwt.secret-key` | JWT 签名密钥 |
| `server.port` | 后端端口（默认 `8081`） |

前端代理端口在各自 `vue.config.js` 中定义：用户端 `8080`、管理端 `8079`。

## 使用示例

### 一个典型的 AI 导购加购流程

1. 用户在用户端登录，前端拿到 JWT 并写入 Axios 请求头 `token`
2. 进入 AI 导购对话页，前端通过 `aiService.js` 调用 `POST /api/llm/agent/chat`
3. 后端 `ShopAgent.chat()` 携带会话记忆调用 LLM，模型决定调用 `OrderTools` 的加购工具
4. 工具在请求线程上下文中缓存加购清单，模型返回文本后由后端结构化取出，随响应一并返回前端
5. 用户在前端购物车界面确认后点击"结算"，触发 `POST /api/order` 完成真正下单

> Agent 工具只负责加购，不触碰下单与扣库存；真正的结算始终由用户手动触发。

### 管理后台常用操作

- 商品管理：上下架、批量改状态、改折扣、上传图片（`POST /api/product/upload`）
- 订单管理：分页查询、改订单状态、催单（`POST /api/order/{id}/urgent`）
- 用户管理：查看与维护用户资料

## 开发说明

### 模块划分

- 后端遵循 `controller → service → mapper` 分层，DTO 负责入参、VO 负责出参、entity 对应数据表
- LLM 相关代码集中在 `common/llm/`，配置在 `common/config/LlmConfig.java` 与 `ChatModelProperties.java`
- 通用能力（JWT、拦截器、文件存储、Redis）放在 `common/` 下，与业务代码分离

### 构建与测试

```bash
cd youxx_backend
mvn clean package        # 打包
mvn test                 # 运行测试
```

前端构建：

```bash
cd youxx_user && npm run build     # 产物在 dist/
cd youxx_admin && npm run build
```

### 代码规范

- Java 使用 Lombok 简化实体代码，构建时已配置 Lombok 注解处理器
- 前端依赖 Vue CLI 5，公共 HTTP 封装在 `src/services/api.js`
- 提交前请保证后端 `mvn test` 通过、前端 `npm run build` 通过

## License

仓库当前未包含 LICENSE 文件，暂未声明开源协议。
