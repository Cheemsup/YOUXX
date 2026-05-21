<template>
  <div class="dashboard-container">
    <el-container class="layout-container">
      <el-aside width="200px" class="sidebar">
        <div class="logo">
          <h2>用户中心</h2>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="dashboard">
            <el-icon><HomeFilled /></el-icon>
            <span>购物</span>
          </el-menu-item>
          <el-menu-item index="profile">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="orders">
            <el-icon><Document /></el-icon>
            <span>我的订单</span>
          </el-menu-item>
          <el-menu-item index="settings">
            <el-icon><Setting /></el-icon>
            <span>设置</span>
          </el-menu-item>
        </el-menu>
        <div class="sidebar-bottom">
          <el-button 
            type="primary" 
            class="go-shopping-sidebar-btn" 
            @click="activeMenu = 'dashboard'"
          >
            <el-icon><Shop /></el-icon>
            去逛逛
          </el-button>
          <el-button 
            type="success" 
            class="contact-merchant-btn" 
            @click="activeMenu = 'messages'"
          >
            <el-icon><ChatDotRound /></el-icon>
            联系商家
          </el-button>
        </div>
      </el-aside>

      <el-container>
        <el-header class="header">
          <div class="header-left">
            <el-button text @click="toggleSidebar">
              <el-icon><Fold /></el-icon>
            </el-button>
          </div>
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
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <el-main class="main-content">
          <div class="content-wrapper">
            <UserHome
              v-if="activeMenu === 'dashboard'"
              :cart-count="cartCount"
              @open-cart="showCart = true"
              @add-to-cart="addToCart"
            />
            <UserProfile
              v-else-if="activeMenu === 'profile'"
              :user-info="userInfo"
              @update:user-info="userInfo = $event"
              @go-to-orders="activeMenu = 'orders'"
            />
            <UserOrder
              v-else-if="activeMenu === 'orders'"
              :orders="orders"
              @view-detail="viewOrderDetail"
              @go-shopping="activeMenu = 'dashboard'"
              @buy-again="handleBuyAgain"
              @order-updated="refreshOrders"
            />
            <UserSettings
              v-else-if="activeMenu === 'settings'"
              :settings="settings"
              @update:settings="settings = $event"
            />
            <UserMessages
              v-else-if="activeMenu === 'messages'"
            />
          </div>
        </el-main>
      </el-container>
    </el-container>

    <el-drawer
      v-model="showCart"
      title="购物车"
      direction="rtl"
      size="400px"
    >
      <div class="cart-content">
        <div v-if="cartItems.length === 0" class="cart-empty">
          <el-empty description="购物车是空的" />
        </div>
        <div v-else>
            <div class="cart-items">
              <div v-for="item in cartItems" :key="item.id" class="cart-item">
                <div class="cart-item-image" v-if="item.image">
                  <img :src="item.image" :alt="item.name" />
                </div>
                <div class="cart-item-info">
                  <span class="cart-item-name">{{ item.name }}</span>
                  <span class="cart-item-price">¥{{ (item.price * item.discount).toFixed(2) }}</span>
                </div>
                <div class="cart-item-controls">
                  <el-input-number
                    v-model="item.quantity"
                    :min="1"
                    :max="item.stock"
                    size="small"
                    @change="updateCartQuantity(item)"
                  />
                  <el-button type="danger" size="small" @click="removeFromCart(item)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          <div class="cart-footer">
            <div class="cart-total">
              <span>总计：</span>
              <span class="total-price">¥{{ cartTotal.toFixed(2) }}</span>
            </div>
            <el-button type="primary" class="checkout-btn" @click="handleCheckout">
              去结算
            </el-button>
          </div>
        </div>
      </div>
    </el-drawer>

    <el-drawer
      v-model="showOrderDetail"
      title="订单详情"
      direction="rtl"
      size="450px"
    >
      <div v-if="currentOrderDetail" class="order-detail-content">
        <div class="detail-section">
          <div class="detail-row">
            <span class="detail-label">订单号</span>
            <span class="detail-value">{{ currentOrderDetail.id }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">下单时间</span>
            <span class="detail-value">{{ currentOrderDetail.date }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">订单状态</span>
            <el-tag :type="getStatusType(currentOrderDetail.status)">
              {{ currentOrderDetail.status }}
            </el-tag>
          </div>
        </div>

        <div class="detail-section">
          <h4>商品清单</h4>
          <div class="detail-items">
            <div v-for="item in currentOrderDetail.items" :key="item.productId" class="detail-item">
              <div class="detail-item-info">
                <span class="detail-item-name">{{ item.name }}</span>
                <span class="detail-item-spec">¥{{ item.price }}/{{ item.unit }}</span>
              </div>
              <div class="detail-item-right">
                <span class="detail-item-quantity">x{{ item.quantity }}</span>
                <span class="detail-item-subtotal">¥{{ item.subtotal }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="detail-footer">
          <div class="detail-total-row">
            <span>商品数量</span>
            <span>{{ currentOrderDetail.itemCount }} 件</span>
          </div>
          <div class="detail-total-row total-amount">
            <span>订单总额</span>
            <span class="amount-value">¥{{ currentOrderDetail.totalAmount }}</span>
          </div>
        </div>
      </div>
    </el-drawer>

    <AIChatAssistant ref="aiAssistantRef" @add-to-cart="addToCart" />
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  HomeFilled,
  User,
  Document,
  Setting,
  Fold,
  Avatar,
  ArrowDown,
  Delete,
  Shop,
  ChatDotRound
} from '@element-plus/icons-vue'
import { getOrdersByUsername, createOrder } from '@/data/orders.js'
import { getUserByUsername } from '@/data/users.js'
import UserHome from './user/UserHome.vue'
import UserProfile from './user/UserProfile.vue'
import UserOrder from './user/UserOrder.vue'
import UserSettings from './user/UserSettings.vue'
import UserMessages from './user/UserMessages.vue'
import AIChatAssistant from '@/components/AIChatAssistant.vue'

export default {
  name: 'DashboardUser',
  components: {
    HomeFilled,
    User,
    Document,
    Setting,
    Fold,
    Avatar,
    ArrowDown,
    Delete,
    Shop,
    ChatDotRound,
    UserHome,
    UserProfile,
    UserOrder,
    UserSettings,
    UserMessages,
    AIChatAssistant
  },
  setup() {
    const router = useRouter()
    const aiAssistantRef = ref(null)
    const activeMenu = ref('dashboard')
    const currentUser = ref('')
    const isCollapsed = ref(false)
    const showCart = ref(false)
    const cartItems = ref([])
    const showOrderDetail = ref(false)
    const currentOrderDetail = ref(null)

    const userInfo = ref({
      username: '',
      email: '',
      phone: '',
      address: ''
    })

    const orders = ref([])

    const settings = ref({
      notifications: true,
      messageAlert: true,
      language: 'zh'
    })

    const cartCount = computed(() => {
      return cartItems.value.reduce((sum, item) => sum + item.quantity, 0)
    })

    const cartTotal = computed(() => {
      return cartItems.value.reduce((sum, item) => {
        return sum + item.price * item.discount * item.quantity
      }, 0)
    })

    const getUserStorageKey = (key) => {
      const username = sessionStorage.getItem('username') || ''
      return `${key}_${username}`
    }

    onMounted(() => {
      const username = sessionStorage.getItem('username')
      currentUser.value = username || '用户'
      
      const user = getUserByUsername(username)
      if (user) {
        userInfo.value.username = user.username
        userInfo.value.phone = user.phone || ''
        userInfo.value.email = user.email || ''
      } else {
        userInfo.value.username = username || '用户'
      }

      const savedCart = localStorage.getItem(getUserStorageKey('cartItems'))
      if (savedCart) {
        cartItems.value = JSON.parse(savedCart)
      }

      orders.value = getOrdersByUsername(username)
    })

    const handleMenuSelect = (index) => {
      activeMenu.value = index
    }

    const handleCommand = (command) => {
      if (command === 'logout') {
        sessionStorage.removeItem('username')
        sessionStorage.removeItem('userRole')
        router.push('/login')
      } else if (command === 'profile') {
        activeMenu.value = 'profile'
      } else if (command === 'settings') {
        activeMenu.value = 'settings'
      }
    }

    const toggleSidebar = () => {
      isCollapsed.value = !isCollapsed.value
    }

    const getStatusType = (status) => {
      const statusMap = {
        '待发货': 'warning',
        '已发货': 'primary',
        '已完成': 'success',
        '已取消': 'danger'
      }
      return statusMap[status] || 'info'
    }

    const addToCart = (product) => {
      const existingItem = cartItems.value.find(item => item.id === product.id)

      if (existingItem) {
        if (existingItem.quantity < existingItem.stock) {
          existingItem.quantity++
          ElMessage.success(`已增加 ${product.name} 数量`)
        } else {
          ElMessage.warning(` ${product.name} 库存不足`)
        }
      } else {
        cartItems.value.push({
          id: product.id,
          name: product.name,
          price: product.price,
          discount: product.discount,
          unit: product.unit,
          quantity: 1,
          stock: product.stock,
          image: product.image
        })
        ElMessage.success(`已加入购物车：${product.name}`)
      }

      saveCart()
    }

    const updateCartQuantity = () => {
      saveCart()
    }

    const removeFromCart = (item) => {
      const index = cartItems.value.findIndex(i => i.id === item.id)
      if (index > -1) {
        cartItems.value.splice(index, 1)
        ElMessage.info(`已移除 ${item.name}`)
        saveCart()
      }
    }

    const saveCart = () => {
      localStorage.setItem(getUserStorageKey('cartItems'), JSON.stringify(cartItems.value))
    }

    const handleCheckout = () => {
      if (cartItems.value.length === 0) {
        ElMessage.warning('购物车是空的')
        return
      }

      const username = sessionStorage.getItem('username')
      const newOrder = createOrder(cartItems.value, cartTotal.value, username)

      orders.value = getOrdersByUsername(username)

      cartItems.value = []
      localStorage.removeItem(getUserStorageKey('cartItems'))

      showCart.value = false

      ElMessage.success(`订单 ${newOrder.id} 下单成功！`)

      activeMenu.value = 'orders'
    }

    const viewOrderDetail = (order) => {
      currentOrderDetail.value = order
      showOrderDetail.value = true
    }

    const refreshOrders = () => {
      const username = sessionStorage.getItem('username')
      orders.value = getOrdersByUsername(username)
    }

    const handleBuyAgain = (order) => {
      order.items.forEach(item => {
        const existingItem = cartItems.value.find(i => i.id === item.productId)
        if (existingItem) {
          existingItem.quantity += item.quantity
        } else {
          cartItems.value.push({
            id: item.productId,
            name: item.name,
            price: item.price,
            discount: item.discount,
            unit: item.unit,
            quantity: item.quantity,
            stock: item.stock || 999,
            image: item.image
          })
        }
      })
      saveCart()
      activeMenu.value = 'dashboard'
      ElMessage.success('已将商品加入购物车')
    }

    return {
      aiAssistantRef,
      activeMenu,
      currentUser,
      showCart,
      cartItems,
      cartCount,
      cartTotal,
      userInfo,
      orders,
      settings,
      showOrderDetail,
      currentOrderDetail,
      handleMenuSelect,
      handleCommand,
      toggleSidebar,
      getStatusType,
      addToCart,
      updateCartQuantity,
      removeFromCart,
      handleCheckout,
      viewOrderDetail,
      refreshOrders,
      handleBuyAgain
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  height: 100vh;
  width: 100%;
}

.layout-container {
  height: 100%;
}

.sidebar {
  background-color: #42b983;
  color: #fff;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.sidebar-bottom {
  margin-top: auto;
  padding: 16px;
  background-color: #2d8f62;
}

.go-shopping-sidebar-btn {
  width: 100%;
  border-radius: 12px;
  font-weight: 600;
  background: linear-gradient(135deg, #35a070 0%, #2d8f62 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.go-shopping-sidebar-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
  background: linear-gradient(135deg, #2d8f62 0%, #247351 100%);
}

.contact-merchant-btn {
  width: 100%;
  border-radius: 12px;
  font-weight: 600;
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  margin-top: 8px;
  margin-left: 0 !important;
}

.contact-merchant-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
  background: linear-gradient(135deg, #337ecc 0%, #2b6cb0 100%);
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  background-color: #35a070;
  color: #fff;
}

.logo h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.sidebar-menu {
  border-right: none;
  background-color: #42b983;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 200px;
}

.header {
  background-color: #fff;
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
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

.content-wrapper {
  background-color: #fff;
  padding: 24px;
  border-radius: 16px;
  min-height: calc(100vh - 148px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  animation: fadeIn 0.5s ease-out;
}

.cart-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.cart-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cart-items {
  flex: 1;
  overflow-y: auto;
}

.cart-item {
  padding: 15px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  gap: 12px;
  align-items: center;
}

.cart-item-image {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f5f7fa;
}

.cart-item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cart-item-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.cart-item-name {
  color: #303133;
  font-weight: 500;
}

.cart-item-price {
  color: #f56c6c;
  font-weight: 500;
}

.cart-item-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-footer {
  padding: 20px;
  border-top: 1px solid #e0e0e0;
  background-color: #f5f7fa;
}

.cart-total {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: 500;
}

.total-price {
  font-size: 20px;
  color: #f56c6c;
}

.checkout-btn {
  width: 100%;
  background-color: #42b983;
  border-color: #42b983;
}

.checkout-btn:hover {
  background-color: #35a070;
  border-color: #35a070;
}

.order-detail-content {
  padding: 0 20px;
}

.detail-section {
  margin-bottom: 25px;
}

.detail-section h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #e0e0e0;
}

.detail-label {
  color: #909399;
}

.detail-value {
  color: #303133;
  font-weight: 500;
}

.detail-items {
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 10px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px dashed #e0e0e0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-item-info {
  display: flex;
  flex-direction: column;
}

.detail-item-name {
  color: #606266;
  font-weight: 500;
}

.detail-item-spec {
  color: #909399;
  font-size: 12px;
}

.detail-item-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.detail-item-quantity {
  color: #909399;
  font-size: 14px;
}

.detail-item-subtotal {
  color: #f56c6c;
  font-weight: 500;
}

.detail-footer {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
}

.detail-total-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  color: #606266;
}

.total-amount {
  font-size: 18px;
  font-weight: 500;
  margin-top: 10px;
}

.amount-value {
  color: #f56c6c;
  font-size: 20px;
}
</style>