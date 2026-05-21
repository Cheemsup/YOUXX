# YOUXX 前后端分离迁移开发计划

## 一、项目现状分析

### 1.1 前端原型 (`youxx_front`) 功能清单

| 功能模块 | 当前实现方式 | 涉及文件 |
|---------|------------|---------|
| 用户认证（登录/注册） | localStorage 存储用户数据，前端直接校验密码 | `data/users.js`, `views/Login.vue` |
| 用户管理（CRUD） | localStorage 读写用户列表 | `data/users.js`, `views/admin/DashboardManageUser.vue` |
| 商品管理（CRUD/上下架/折扣） | localStorage 读写商品列表，前端 require.context 加载图片 | `data/products.js`, `views/admin/DashboardProduct.vue` |
| 订单管理（创建/状态流转/催单） | localStorage 读写订单列表 | `data/orders.js`, `views/admin/DashboardOrder.vue`, `views/user/UserOrder.vue` |
| 消息系统（用户-商家聊天） | localStorage 读写消息，前端模拟会话 | `data/messages.js`, `views/admin/DashboardMessages.vue`, `views/user/UserMessages.vue` |
| AI 智能导购 | 前端直接调用 Minimax/OpenAI API，含规则引擎回退 | `services/aiService.js`, `services/promptEngineer.js`, `config/llmConfig.js`, `components/AIChatAssistant.vue` |
| 购物车 | localStorage 按用户存储 | `views/DashboardUser.vue` |
| 个人信息/地址管理 | localStorage 按用户存储 | `views/user/UserProfile.vue`, `views/user/UserSettings.vue` |
| 系统设置 | 纯前端，未持久化 | `views/admin/DashboardSettings.vue` |
| 路由守卫/认证 | sessionStorage 存 username/role | `router/index.js` |

### 1.2 后端项目 (`youxx_backend`) 现状

- Spring Boot 4.0.6 骨架项目，仅有 `YouxxApplication.java` 启动类
- 已引入依赖：Spring Web MVC、Spring JDBC、MySQL Connector、Redis、Lombok
- `application.yaml` 仅配置了应用名称，数据库/Redis 等均未配置

### 1.3 核心问题

1. **数据存储全在 localStorage**：无持久化、无并发控制、无安全校验，密码明文存储
2. **API Key 暴露在前端**：Minimax API Key 硬编码在 `llmConfig.js`，任何人可从浏览器获取
3. **业务逻辑在前端**：用户验证、订单创建、权限判断等全在前端，可被绕过
4. **无会话管理**：仅靠 sessionStorage 存 username/role，无 Token 机制
5. **数据无法共享**：每个浏览器独立存储，管理员和用户看到的数据不一致

---

## 二、后端开发计划

### 阶段一：基础架构搭建

#### 2.1 数据库设计

**用户表 `sys_user`**
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | VARCHAR(10) | 用户ID (A001/U001) |
| username | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR(255) | BCrypt 加密密码 |
| phone | VARCHAR(20) | 手机号 |
| email | VARCHAR(100) | 邮箱 |
| role | VARCHAR(20) | 角色：ADMIN/USER |
| status | VARCHAR(10) | 状态：NORMAL/DISABLED |
| avatar | TEXT | 头像 URL/Base64 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

**商品分类表 `product_category`**
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | VARCHAR(20) | 分类ID (drinks/snacks等) |
| name | VARCHAR(50) | 分类名称 |
| icon | VARCHAR(50) | 图标标识 |
| sort_order | INT | 排序 |

**商品表 `product`**
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | VARCHAR(10) | 商品ID (P001) |
| name | VARCHAR(100) | 商品名称 |
| category_id | VARCHAR(20) | 分类ID |
| price | DECIMAL(10,2) | 原价 |
| unit | VARCHAR(20) | 计量单位 |
| stock | INT | 库存 |
| image_url | VARCHAR(500) | 图片路径 |
| description | VARCHAR(500) | 描述 |
| bar_code | VARCHAR(50) | 条码 |
| discount | DECIMAL(3,2) | 折扣(0-1) |
| is_hot | BOOLEAN | 是否热销 |
| tags | VARCHAR(200) | 标签(逗号分隔) |
| status | VARCHAR(20) | 状态：ONSHELF/OFFSHELF |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

**订单表 `orders`**
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | VARCHAR(30) | 订单号 |
| user_id | VARCHAR(10) | 用户ID |
| username | VARCHAR(50) | 用户名 |
| total_amount | DECIMAL(10,2) | 总金额 |
| item_count | INT | 商品数量 |
| status | VARCHAR(20) | 状态：PENDING/SHIPPED/COMPLETED/CANCELLED |
| urgent_count | INT | 催单次数 |
| last_urgent_time | DATETIME | 最后催单时间 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

**订单明细表 `order_item`**
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | BIGINT AUTO_INCREMENT | 主键 |
| order_id | VARCHAR(30) | 订单号 |
| product_id | VARCHAR(10) | 商品ID |
| product_name | VARCHAR(100) | 商品名称 |
| price | DECIMAL(10,2) | 单价 |
| discount | DECIMAL(3,2) | 折扣 |
| quantity | INT | 数量 |
| unit | VARCHAR(20) | 单位 |
| subtotal | DECIMAL(10,2) | 小计 |

**消息表 `message`**
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | VARCHAR(30) | 消息ID |
| conversation_id | VARCHAR(50) | 会话ID |
| sender | VARCHAR(20) | 发送者：USER/ADMIN |
| sender_name | VARCHAR(50) | 发送者名称 |
| content | TEXT | 消息内容 |
| is_read | BOOLEAN | 是否已读 |
| create_time | DATETIME | 创建时间 |

**用户地址表 `user_address`**
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | BIGINT AUTO_INCREMENT | 主键 |
| user_id | VARCHAR(10) | 用户ID |
| name | VARCHAR(50) | 收货人 |
| phone | VARCHAR(20) | 手机号 |
| detail | VARCHAR(500) | 详细地址 |
| is_default | BOOLEAN | 是否默认 |
| create_time | DATETIME | 创建时间 |

**系统设置表 `sys_settings`**
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | INT | 主键 |
| setting_key | VARCHAR(100) | 设置键 |
| setting_value | VARCHAR(500) | 设置值 |
| update_time | DATETIME | 更新时间 |

#### 2.2 后端项目结构

```
org.youxx
├── common/                              # ===== 阶段一已完成：基础通用组件 =====
│   ├── config/
│   │   ├── JwtProperties.java           # JWT 配置属性绑定 (youxx.jwt.*)
│   │   ├── RedisConfig.java             # Redis 序列化配置
│   │   └── WebMvcConfig.java            # CORS 跨域 + JWT 拦截器注册
│   ├── exception/
│   │   ├── AuthenticationException.java # 认证异常
│   │   └── GlobalExceptionHandler.java  # 全局异常处理 (@RestControllerAdvice)
│   ├── interceptor/
│   │   └── JwtTokenUserInterceptor.java # JWT Token 认证拦截器
│   ├── result/
│   │   ├── PageResult.java              # 分页结果封装
│   │   └── Result.java                  # 统一响应封装 (code/msg/data)
│   ├── security/
│   │   ├── JwtUtil.java                 # JWT 创建/解析工具
│   │   └── PasswordEncoder.java         # BCrypt 密码加密
│   └── userInfoMaintainer/
│       ├── BaseContext.java             # ThreadLocal 用户上下文
│       └── UserInfo.java                # 当前用户信息 DTO
├── entity/                              # ===== 阶段二：数据库实体 =====
│   ├── User.java
│   ├── Product.java
│   ├── ProductCategory.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── Message.java
│   ├── UserAddress.java
│   └── SysSettings.java
├── mapper/                              # ===== MyBatis Mapper 接口 =====
│   ├── UserMapper.java
│   ├── ProductMapper.java
│   ├── OrderMapper.java
│   ├── MessageMapper.java
│   ├── UserAddressMapper.java
│   └── SysSettingsMapper.java
├── service/                             # ===== 业务逻辑层（接口） =====
│   ├── impl/
│   │   ├── AuthServiceImpl.java          # 认证服务实现
│   │   ├── UserServiceImpl.java          # 用户管理实现
│   │   ├── ProductServiceImpl.java       # 商品管理实现
│   │   ├── OrderServiceImpl.java         # 订单管理实现
│   │   ├── MessageServiceImpl.java       # 消息服务实现
│   │   └── AiServiceImpl.java            # AI 导购服务实现
│   ├── AuthService.java                 # 认证服务接口（登录/注册/JWT）
│   ├── UserService.java                 # 用户管理接口
│   ├── ProductService.java              # 商品管理接口（含图片上传）
│   ├── OrderService.java                # 订单管理接口
│   ├── MessageService.java              # 消息服务接口
│   └── AiService.java                   # AI 导购服务接口
├── controller/                          # ===== REST 控制器 =====
│   ├── AuthController.java              # /api/auth/**
│   ├── UserController.java              # /api/user/**
│   ├── ProductController.java           # /api/product/**
│   ├── OrderController.java             # /api/order/**
│   ├── MessageController.java           # /api/message/**
│   ├── AiController.java                # /api/ai/**
│   └── FileController.java              # /api/file/**
└── YouxxApplication.java
```

#### 2.3 基础设施配置

- **application.yaml**：配置 MySQL、Redis、JWT 密钥、Minimax API Key、文件上传路径
- **CORS 配置**：允许前端 8080 端口跨域访问
- **JWT 认证**：登录后签发 Token，前端请求携带 Authorization Header

### 阶段二：核心 API 开发

#### 2.4 认证模块 (`AuthController`)

| 接口 | 方法 | 说明 |
|-----|------|------|
| `/api/auth/login` | POST | 登录，返回 JWT Token + 用户信息 |
| `/api/auth/register` | POST | 用户注册 |
| `/api/auth/info` | GET | 获取当前用户信息（Token 鉴权） |

**关键变更**：
- 密码使用 BCrypt 加密存储
- 登录返回 JWT Token，前端存储到 localStorage
- Token 中包含 userId、username、role

#### 2.5 用户管理模块 (`UserController`)

| 接口 | 方法 | 说明 |
|-----|------|------|
| `/api/user/list` | GET | 获取用户列表（支持搜索/角色筛选/分页） |
| `/api/user/{id}` | GET | 获取用户详情 |
| `/api/user` | POST | 添加用户（管理员） |
| `/api/user/{id}` | PUT | 更新用户信息 |
| `/api/user/{id}/status` | PUT | 切换用户状态 |
| `/api/user/{id}` | DELETE | 删除用户 |
| `/api/user/profile` | GET | 获取个人信息 |
| `/api/user/profile` | PUT | 更新个人信息 |
| `/api/user/password` | PUT | 修改密码 |
| `/api/user/address` | GET | 获取地址列表 |
| `/api/user/address` | POST | 新增地址 |
| `/api/user/address/{id}` | PUT | 更新地址 |
| `/api/user/address/{id}` | DELETE | 删除地址 |
| `/api/user/address/{id}/default` | PUT | 设为默认地址 |

#### 2.6 商品管理模块 (`ProductController`)

| 接口 | 方法 | 说明 |
|-----|------|------|
| `/api/product/list` | GET | 商品列表（支持搜索/分类/状态/分页） |
| `/api/product/{id}` | GET | 商品详情 |
| `/api/product/hot` | GET | 热销商品 |
| `/api/product/category/list` | GET | 分类列表 |
| `/api/product` | POST | 添加商品（管理员） |
| `/api/product/{id}` | PUT | 更新商品 |
| `/api/product/{id}/status` | PUT | 上架/下架 |
| `/api/product/batch/status` | PUT | 批量上架/下架 |
| `/api/product/{id}/discount` | PUT | 设置折扣 |
| `/api/product/{id}` | DELETE | 删除商品 |
| `/api/product/upload` | POST | 商品图片上传 |

#### 2.7 订单管理模块 (`OrderController`)

| 接口 | 方法 | 说明 |
|-----|------|------|
| `/api/order/list` | GET | 订单列表（管理员，支持搜索/状态/日期/分页） |
| `/api/order/my` | GET | 我的订单（用户） |
| `/api/order/{id}` | GET | 订单详情 |
| `/api/order` | POST | 创建订单（下单） |
| `/api/order/{id}/status` | PUT | 更新订单状态（发货/完成/取消） |
| `/api/order/{id}/urgent` | POST | 催单 |
| `/api/order/{id}` | DELETE | 删除订单 |

**下单逻辑关键**：
- 后端校验库存，扣减库存
- 计算折扣价格
- 生成唯一订单号
- 事务保证订单和库存一致性

#### 2.8 消息模块 (`MessageController`)

| 接口 | 方法 | 说明 |
|-----|------|------|
| `/api/message/conversations` | GET | 会话列表（管理员） |
| `/api/message/conversation/{id}` | GET | 会话消息列表 |
| `/api/message/send` | POST | 发送消息 |
| `/api/message/{id}/read` | PUT | 标记已读 |
| `/api/message/conversation/{id}/read` | PUT | 标记会话全部已读 |
| `/api/message/unread-count` | GET | 未读消息数 |

#### 2.9 AI 导购模块 (`AiController`)

| 接口 | 方法 | 说明 |
|-----|------|------|
| `/api/ai/chat` | POST | AI 对话（接收消息+历史，返回AI响应） |

**迁移要点**：
- API Key 存储在后端配置文件，不暴露给前端
- 后端调用 Minimax/OpenAI API
- 商品知识库从数据库查询构建 System Prompt
- 规则引擎回退逻辑迁移到后端
- 前端仅发送用户消息和接收结构化响应

#### 2.10 系统设置模块

| 接口 | 方法 | 说明 |
|-----|------|------|
| `/api/settings` | GET | 获取系统设置 |
| `/api/settings` | PUT | 更新系统设置 |

### 阶段三：数据初始化

- 编写 SQL 脚本，将前端 `data/` 目录中的默认数据初始化到数据库
- 商品图片迁移到后端静态资源目录或对象存储
- 管理员默认密码使用 BCrypt 加密后插入

---

## 三、前端改造计划

### 3.1 新增 HTTP 服务层

创建 `src/services/api.js`（基于 axios），统一封装：
- baseURL 配置
- 请求拦截器：自动附加 JWT Token
- 响应拦截器：统一错误处理、401 跳转登录
- 所有 API 调用函数

### 3.2 各模块前端改造

#### 认证改造
- **删除**：`data/users.js` 中的 `validateUser`、`registerUser` 函数
- **改造**：`Login.vue` 调用 `/api/auth/login`、`/api/auth/register`
- **改造**：`router/index.js` 路由守卫检查 JWT Token 有效性
- **改造**：登录成功后存储 Token 到 localStorage，用户信息从 `/api/auth/info` 获取

#### 用户管理改造
- **删除**：`data/users.js` 中所有 CRUD 函数
- **改造**：`DashboardManageUser.vue` 调用后端 API
- **改造**：`DashboardSettings.vue` 从后端获取管理员信息

#### 商品管理改造
- **删除**：`data/products.js` 中所有 CRUD 函数和 localStorage 逻辑
- **保留**：`categories` 常量可移至后端或前端保留作为枚举
- **改造**：`DashboardProduct.vue` 调用后端 API
- **改造**：`UserHome.vue` 商品列表从后端获取
- **改造**：商品图片路径改为后端返回的 URL

#### 订单管理改造
- **删除**：`data/orders.js` 中所有函数
- **改造**：`DashboardOrder.vue` 调用后端 API
- **改造**：`UserOrder.vue` 调用后端 API
- **改造**：`DashboardUser.vue` 下单逻辑调用后端 API

#### 消息系统改造
- **删除**：`data/messages.js` 中所有函数
- **改造**：`DashboardMessages.vue` 调用后端 API
- **改造**：`UserMessages.vue` 调用后端 API
- **可选**：后续可升级为 WebSocket 实现实时消息

#### AI 导购改造
- **删除**：`config/llmConfig.js`（API Key 不再前端存储）
- **删除**：`services/aiService.js` 中的 `callMinimaxAPI`、`callOpenAIAPI`、`callLLMAPI` 函数
- **删除**：`services/promptEngineer.js`（Prompt 构建移至后端）
- **删除**：`utils/testLLM.js`
- **改造**：`AIChatAssistant.vue` 调用 `/api/ai/chat`
- **保留**：`aiService.js` 中规则引擎的意图识别和响应模板可作为后端回退逻辑参考

#### 购物车改造
- 购物车可继续使用 localStorage 前端存储（属于用户端临时状态）
- 下单时后端校验商品和库存

#### 个人信息/地址改造
- **改造**：`UserProfile.vue` 地址管理调用后端 API
- **改造**：头像上传调用后端文件上传 API
- **改造**：`UserSettings.vue` 修改密码调用后端 API

### 3.3 前端文件清理清单

| 操作 | 文件 | 说明 |
|-----|------|------|
| 删除 | `src/data/users.js` | 用户数据全部迁移到后端 |
| 删除 | `src/data/products.js` | 商品数据全部迁移到后端 |
| 删除 | `src/data/orders.js` | 订单数据全部迁移到后端 |
| 删除 | `src/data/messages.js` | 消息数据全部迁移到后端 |
| 删除 | `src/config/llmConfig.js` | API 配置迁移到后端 |
| 删除 | `src/utils/testLLM.js` | LLM 测试工具不再需要 |
| 删除 | `src/services/promptEngineer.js` | Prompt 工程迁移到后端 |
| 重写 | `src/services/aiService.js` | 简化为调用后端 `/api/ai/chat` |
| 新增 | `src/services/api.js` | axios 封装 + 所有 API 调用函数 |
| 修改 | `src/router/index.js` | Token 鉴权路由守卫 |
| 修改 | `src/App.vue` | 移除 localStorage 清理逻辑 |
| 修改 | `vue.config.js` | 代理目标改为后端服务地址 |
| 修改 | `package.json` | 新增 axios 依赖 |

### 3.4 前端 API 调用统一规范

所有组件不再直接操作 localStorage 数据，统一通过 `api.js` 调用后端接口：

```javascript
// src/services/api.js 示例结构
import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：附加 Token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一错误处理
request.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.hash = '#/login'
    }
    return Promise.reject(error)
  }
)

// 导出各模块 API 函数...
```

---

## 四、开发优先级和顺序

### Phase 1：基础 + 认证（最高优先级）
1. 后端：数据库建表 + application.yaml 配置
2. 后端：统一响应封装 + JWT 工具类 + 认证拦截器
3. 后端：AuthController（登录/注册）
4. 前端：安装 axios，创建 api.js，改造 Login.vue

### Phase 2：商品模块
1. 后端：ProductMapper + ProductService + ProductController
2. 后端：文件上传服务（商品图片）
3. 前端：改造 UserHome.vue、DashboardProduct.vue
4. 前端：删除 `data/products.js`

### Phase 3：订单模块
1. 后端：OrderMapper + OrderService + OrderController
2. 前端：改造 DashboardOrder.vue、UserOrder.vue、DashboardUser.vue 下单逻辑
3. 前端：删除 `data/orders.js`

### Phase 4：用户管理模块
1. 后端：UserMapper + UserService + UserController
2. 前端：改造 DashboardManageUser.vue、UserProfile.vue、UserSettings.vue
3. 前端：删除 `data/users.js`

### Phase 5：消息模块
1. 后端：MessageMapper + MessageService + MessageController
2. 前端：改造 DashboardMessages.vue、UserMessages.vue
3. 前端：删除 `data/messages.js`

### Phase 6：AI 导购模块
1. 后端：AiService + AiController（迁移 Prompt 工程 + 规则引擎 + API 调用）
2. 前端：改造 AIChatAssistant.vue
3. 前端：删除 `config/llmConfig.js`、`services/promptEngineer.js`、`utils/testLLM.js`，重写 `services/aiService.js`

### Phase 7：收尾
1. 前端：清理 vue.config.js 代理配置（Minimax 代理移除，改为后端代理）
2. 前端：清理 App.vue 中的 localStorage 清理逻辑
3. 全局测试：前后端联调
4. 数据初始化脚本编写和执行

---

## 五、技术选型确认

| 项目 | 选型 | 说明 |
|-----|------|------|
| 后端框架 | Spring Boot 4.0.6 | 已搭建 |
| ORM | MyBatis / MyBatis-Plus | 建议引入 mybatis-plus-boot-starter |
| 数据库 | MySQL | 已引入 connector |
| 缓存 | Redis | 已引入依赖，可用于 Token 存储/热数据缓存 |
| 认证 | JWT (jjwt) | 需新增依赖 |
| 密码加密 | BCrypt (Spring Security Crypto) | 需新增依赖 |
| HTTP 客户端 | RestTemplate / WebClient | 后端调用 Minimax API |
| 前端 HTTP | axios | 需新增依赖 |
| 文件上传 | MultipartFile | Spring 内置支持 |

---

## 六、风险和注意事项

1. **商品图片迁移**：当前前端使用 `require.context` 加载图片，迁移后需改为 URL 引用，需统一处理图片存储
2. **数据一致性**：迁移期间前端 localStorage 数据与后端数据库数据可能不一致，需明确切换节点
3. **密码加密**：现有明文密码需在初始化脚本中转为 BCrypt
4. **API Key 安全**：Minimax API Key 必须从代码仓库中移除，使用环境变量或配置中心管理
5. **前后端端口**：前端 8080，后端需使用其他端口（如 8081），前端通过代理访问
6. **订单号生成**：后端需实现分布式友好的订单号生成策略
