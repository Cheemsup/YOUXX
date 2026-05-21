<!--
  管理员后台主布局组件
  包含侧边栏导航、顶部栏和内容区域
-->
<template>
  <div class="dashboard-container">
    <el-container class="layout-container">
      <!-- 左侧边栏 -->
      <el-aside width="200px" class="sidebar">
        <!-- Logo 区域 -->
        <div class="logo">
          <h2>管理系统</h2>
        </div>
        <!-- 导航菜单 -->
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="dashboard">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="user">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="messages">
            <el-icon><ChatDotRound /></el-icon>
            <span>未读消息</span>
          </el-menu-item>
          <el-menu-item index="product">
            <el-icon><Goods /></el-icon>
            <span>产品管理</span>
          </el-menu-item>
          <el-menu-item index="order">
            <el-icon><Document /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="settings">
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 右侧主体内容 -->
      <el-container>
        <!-- 顶部导航栏 -->
        <el-header class="header">
          <!-- 左侧：折叠按钮 -->
          <div class="header-left">
            <el-button text @click="toggleSidebar">
              <el-icon><Fold /></el-icon>
            </el-button>
          </div>
          <!-- 右侧：用户信息下拉菜单 -->
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-icon><Avatar /></el-icon>
                {{ currentUser }}
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="settings">设置</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 主内容区域 -->
        <el-main class="main-content">
          <div class="content-wrapper">
            <!-- 根据当前选中的菜单动态加载对应组件 -->
            <DashboardHome
              v-if="activeMenu === 'dashboard'"
              :current-user="currentUser"
              :users="users"
              :orders="orders"
              @navigate="handleMenuSelect"
            />
            <DashboardManageUser
              v-else-if="activeMenu === 'user'"
              :users="users"
              :current-user="currentUser"
            />
            <DashboardMessages
              v-else-if="activeMenu === 'messages'"
            />
            <DashboardProduct
              v-else-if="activeMenu === 'product'"
            />
            <DashboardOrder
              v-else-if="activeMenu === 'order'"
              :orders="orders"
              @order-updated="loadData"
            />
            <DashboardSettings
              v-else-if="activeMenu === 'settings'"
            />
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
// 导入 Vue 3 组合式 API
import { ref, onMounted } from 'vue'
// 导入 Vue Router 用于路由跳转
import { useRouter } from 'vue-router'
// 导入 Element Plus 图标组件
import {
  HomeFilled,
  User,
  Goods,
  Document,
  Setting,
  Fold,
  Avatar,
  ArrowDown,
  ChatDotRound
} from '@element-plus/icons-vue'
// 导入数据管理模块
import { getUsers } from '@/data/users.js'
import { getOrders, updateOrderStatus } from '@/data/orders.js'
import { getUnreadCount } from '@/data/messages.js'
// 导入子组件
import DashboardHome from './admin/DashboardHome.vue'
import DashboardManageUser from './admin/DashboardManageUser.vue'
import DashboardMessages from './admin/DashboardMessages.vue'
import DashboardProduct from './admin/DashboardProduct.vue'
import DashboardOrder from './admin/DashboardOrder.vue'
import DashboardSettings from './admin/DashboardSettings.vue'

export default {
  name: 'Dashboard',
  // 注册组件
  components: {
    HomeFilled,
    User,
    Goods,
    Document,
    Setting,
    Fold,
    Avatar,
    ArrowDown,
    ChatDotRound,
    DashboardHome,
    DashboardManageUser,
    DashboardMessages,
    DashboardProduct,
    DashboardOrder,
    DashboardSettings
  },
  setup() {
    // 初始化路由
    const router = useRouter()
    // 当前激活的菜单项
    const activeMenu = ref('dashboard')
    // 当前登录用户名
    const currentUser = ref('')
    // 侧边栏是否折叠
    const isCollapsed = ref(false)
    // 未读消息数量
    const unreadCount = ref(0)

    // 用户列表数据
    const users = ref([])
    // 订单列表数据
    const orders = ref([])

    // 加载数据
    const loadData = () => {
      users.value = getUsers()
      orders.value = getOrders()
      // 更新未读消息数
      unreadCount.value = getUnreadCount()
    }

    // 组件挂载时执行
    onMounted(() => {
      const username = sessionStorage.getItem('username')
      currentUser.value = username || '用户'
      loadData()
    })

    // 处理菜单选择
    const handleMenuSelect = (index) => {
      activeMenu.value = index
    }

    // 处理下拉菜单命令
    const handleCommand = (command) => {
      if (command === 'logout') {
        // 清除登录信息并跳转到登录页
        sessionStorage.removeItem('username')
        sessionStorage.removeItem('userRole')
        router.push('/login')
      } else if (command === 'profile') {
        alert('个人信息功能开发中...')
      } else if (command === 'settings') {
        // 切换到设置页面
        activeMenu.value = 'settings'
      }
    }

    // 切换侧边栏折叠状态
    const toggleSidebar = () => {
      isCollapsed.value = !isCollapsed.value
    }

    // 返回响应式数据和方法供模板使用
    return {
      activeMenu,
      currentUser,
      users,
      orders,
      unreadCount,
      handleMenuSelect,
      handleCommand,
      toggleSidebar
    }
  }
}
</script>

<style scoped>
/* ==================== 主容器样式 ==================== */
.dashboard-container {
  height: 100vh;
  width: 100%;
}

.layout-container {
  height: 100%;
}

/* ==================== 侧边栏样式 ==================== */
.sidebar {
  background-color: #304156;
  color: #fff;
  overflow-x: hidden;
}

/* Logo 区域 */
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  background-color: #2b3a4a;
  color: #fff;
}

.logo h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

/* 侧边栏菜单 */
.sidebar-menu {
  border-right: none;
  background-color: #304156;
}

.sidebar-menu .el-menu-item {
  color: #fff;
}

.sidebar-menu .el-menu-item:hover {
  background-color: #263445;
}

/* 激活的菜单项 */
.sidebar-menu .el-menu-item.is-active {
  background-color: #409eff;
  color: #fff;
}

.sidebar-menu .el-menu-item .el-icon {
  color: #fff;
}

/* 未读消息徽章 */
.message-badge {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 200px;
}

/* ==================== 顶部栏样式 ==================== */
.header {
  background-color: #304156;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 8px;
  font-size: 14px;
  color: #fff;
}

.main-content {
  background: linear-gradient(135deg, #bee7e9 0%, #d0e0f7 50%, #7ebcf7 100%);
  padding: 20px;
  overflow-y: auto;
}

.content-wrapper {
  background: transparent;
  padding: 0;
  border-radius: 0;
  min-height: calc(100vh - 140px);
}
</style>
