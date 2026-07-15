# 优鲜选·用户端前端（youxx_user）

基于 Vue 3 的智能购物平台项目（用户端），集成 AI 导购助手，承载用户购物、AI 助手、个人中心等全部用户功能。

> 管理后台已拆分至独立项目 [`youxx_admin`](../youxx_admin/)，独立构建、独立部署于端口 8079。本目录由原 `youxx_front` 重命名而来，保留全部历史。

## 项目功能

### 用户端
- **首页展示**：商品分类浏览（饮料、乳制品、生鲜、零食、速食等）
- **AI 智能导购**：基于大语言模型的智能推荐助手，支持商品咨询与个性化推荐
- **个人中心**：个人信息管理、订单查看、消息通知
- **购物车**：商品添加与管理

### 管理后台
- **数据仪表盘**：运营数据概览
- **用户管理**：用户信息维护
- **商品管理**：商品 CRUD 与分类管理
- **订单管理**：订单状态跟踪与处理
- **消息管理**：系统消息推送
- **系统设置**：平台配置管理

## 技术栈

- **框架**: Vue 3.2 + Vue Router 4 + Vuex 4
- **UI 库**: Element Plus 2.13
- **HTTP**: Axios 1.16
- **构建工具**: Vue CLI 5.0
- **AI 集成**: 大语言模型 API 对接

## 环境要求

- Node.js >= 14.0.0
- npm >= 6.0.0
- 后端服务运行在 `http://localhost:8081`

## 快速开始

```bash
# 安装依赖
npm install

# 启动开发服务器 (http://localhost:8080)
npm run serve

# 生产环境构建
npm run build
```

## 项目结构

```
youxx_user/
├── public/              # 静态资源
├── src/
│   ├── assets/          # 图片、样式资源
│   │   ├── products/    # 商品图片（按分类存放）
│   │   └── styles/      # 全局样式
│   ├── components/      # 公共组件
│   │   ├── AIChatAssistant.vue    # AI 聊天助手
│   │   └── BannerCartoonCharacter.vue  # 卡通形象组件
│   ├── config/          # 配置文件（LLM 配置）
│   ├── data/            # 数据层（商品、用户数据管理）
│   ├── router/          # 路由配置（含权限守卫）
│   ├── services/        # 服务层（API、AI 服务）
│   ├── store/           # Vuex 状态管理
│   ├── utils/           # 工具函数
│   └── views/           # 页面视图
│       ├── admin/       # 管理后台页面
│       │   ├── DashboardHome.vue     # 仪表盘
│       │   ├── DashboardManageUser.vue  # 用户管理
│       │   ├── DashboardProduct.vue  # 商品管理
│       │   ├── DashboardOrder.vue    # 订单管理
│       │   ├── DashboardMessages.vue # 消息管理
│       │   └── DashboardSettings.vue # 系统设置
│       └── user/        # 用户端页面
│           ├── UserHome.vue         # 用户首页
│           ├── UserProfile.vue      # 个人信息
│           ├── UserOrder.vue        # 订单管理
│           ├── UserMessages.vue     # 消息中心
│           └── UserSettings.vue     # 设置
├── vue.config.js        # Vue CLI 配置（含代理设置）
└── package.json         # 项目依赖配置
```

## 开发说明

- 开发服务器默认端口：`8080`
- API 代理地址：`http://localhost:8081`
- 使用 Hash 路由模式
- 支持角色权限控制（ADMIN / USER）
