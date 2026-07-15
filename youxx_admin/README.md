# 优鲜选·管理后台前端（youxx_admin）

从原 `youxx_front` 中拆分出的**管理端独立前端项目**，仅承载管理员后台页面，与用户端 `youxx_user` 彻底解耦、独立构建、独立部署。

## 与用户端的区别

- 本项目**不包含任何 AI / 大模型相关功能**（无 `AIChatAssistant`、`aiService`、`promptEngineer`、`llmConfig`、`testLLM`）。
- 登录页**不提供注册入口**，且仅允许 `role === 'ADMIN'` 的账号登录；非管理员登录会被拦截并提示无权限。
- 路由仅保留 `/login` 与 `/dashboard`，登录后统一进入管理仪表盘。

## 技术栈

与用户端保持一致：Vue 3.2 + Vue Router 4 + Vuex 4 + Element Plus 2.13 + Axios 1.16，Vue CLI 5.0 构建。

## 环境要求

- Node.js >= 14.0.0
- npm >= 6.0.0
- 后端服务运行在 `http://localhost:8081`（与用户端共用同一后端，CORS 已放通）

## 快速开始

```bash
# 安装依赖
npm install

# 启动开发服务器 (http://localhost:8079)
npm run serve

# 生产环境构建
npm run build
```

## 端口与代理

- 开发服务器端口：**8079**（区别于用户端 8080）
- API 代理地址：`http://localhost:8081`（在 `vue.config.js` 中配置）

## 项目结构

```
youxx_admin/
├── public/              # 静态资源（index.html、favicon）
├── src/
│   ├── assets/          # 图片、样式资源
│   │   ├── products/    # 商品图片（按分类存放，imageHelper 依赖）
│   │   └── styles/      # 全局样式
│   ├── data/            # 数据层（商品本地缓存 products.js）
│   ├── router/          # 路由配置（仅 login / dashboard，含鉴权守卫）
│   ├── services/        # 服务层（api.js）
│   ├── store/           # Vuex 状态管理（空 store，保持技术栈一致）
│   ├── utils/           # 工具函数（imageHelper.js）
│   └── views/           # 页面视图
│       ├── Login.vue             # 管理员登录页（无注册）
│       ├── Dashboard.vue         # 管理后台主布局
│       └── admin/                # 后台子页面
│           ├── DashboardHome.vue
│           ├── DashboardManageUser.vue
│           ├── DashboardMessages.vue
│           ├── DashboardProduct.vue
│           ├── DashboardOrder.vue
│           └── DashboardSettings.vue
├── vue.config.js        # Vue CLI 配置（端口 8079、代理 8081）
└── package.json
```

## 开发说明

- 开发服务器默认端口：`8079`
- 使用 Hash 路由模式
- JWT 通过请求头 `token` 传递（由 `services/api.js` 请求拦截器从 `localStorage.token` 写入）
