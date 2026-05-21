<template>
  <div class="page-content">
    <div class="stats-section">
      <div class="stat-card">
        <div class="stat-icon stat-icon-character" style="background: transparent;">
          <MiniCartoonCharacter type="purple" />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">累计订单</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-character" style="background: transparent;">
          <MiniCartoonCharacter type="black" />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.pending }}</div>
          <div class="stat-label">待收货</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-character" style="background: transparent;">
          <MiniCartoonCharacter type="orange" />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.completed }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-character" style="background: transparent;">
          <MiniCartoonCharacter type="yellow" />
        </div>
        <div class="stat-info">
          <div class="stat-value">¥{{ stats.totalAmount }}</div>
          <div class="stat-label">消费金额</div>
        </div>
      </div>
    </div>

    <div class="filter-section">
      <el-radio-group v-model="activeFilter" size="default">
        <el-radio-button label="all">全部订单</el-radio-button>
        <el-radio-button label="待发货">待发货</el-radio-button>
        <el-radio-button label="已发货">待收货</el-radio-button>
        <el-radio-button label="已完成">已完成</el-radio-button>
      </el-radio-group>
    </div>

    <div v-if="filteredOrders.length === 0" class="empty-section">
      <div class="empty-content">
        <el-empty description="暂无订单">
          <template #image>
            <div class="empty-image">
              <el-icon><ShoppingBag /></el-icon>
            </div>
          </template>
          <el-button type="primary" size="large" @click="$emit('goShopping')">
            <el-icon><Shop /></el-icon>
            去逛逛
          </el-button>
        </el-empty>
      </div>
    </div>

    <div v-else class="orders-list">
      <div v-for="order in filteredOrders" :key="order.id" class="order-card">
        <div class="order-header">
          <div class="order-info">
            <span class="order-id"><el-icon><Document /></el-icon> 订单号：{{ order.id }}</span>
            <span class="order-date"><el-icon><Clock /></el-icon> {{ order.date }}</span>
          </div>
          <el-tag :type="getStatusType(order.status)" size="large" effect="dark">
            {{ order.status }}
          </el-tag>
        </div>
        <div class="order-items">
          <div v-for="item in order.items" :key="item.productId" class="order-item">
            <div class="item-image">
              <img v-if="getProductImage(item.productId)" :src="getProductImage(item.productId)" :alt="item.name" />
              <div v-else class="image-placeholder">
                <el-icon><Goods /></el-icon>
              </div>
            </div>
            <div class="item-detail">
              <span class="item-name">{{ item.name }}</span>
              <span class="item-spec">¥{{ (item.price * item.discount).toFixed(2) }}/{{ item.unit }}</span>
            </div>
            <div class="item-quantity">x{{ item.quantity }}</div>
            <div class="item-price">¥{{ item.subtotal }}</div>
          </div>
        </div>
        <div class="order-footer">
          <div class="order-summary">
            共 <strong>{{ order.itemCount }}</strong> 件商品
            <span class="order-total">合计：<strong>¥{{ order.totalAmount }}</strong></span>
          </div>
          <div class="order-actions">
            <el-button v-if="order.status === '待发货'" type="warning" size="small" @click="handleUrgent(order)">
              <el-icon><Bell /></el-icon>
              催单 {{ order.urgentCount > 0 ? `(${order.urgentCount})` : '' }}
            </el-button>
            <el-button v-if="order.status === '已发货'" type="success" size="small" @click="handleConfirmReceive(order)">
              <el-icon><CircleCheck /></el-icon>
              确认收货
            </el-button>
            <el-button size="small" @click="$emit('viewDetail', order)">
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
            <el-button type="primary" size="small" @click="emitBuyAgain(order)">
              <el-icon><RefreshLeft /></el-icon>
              再次购买
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import {
  Document,
  Van,
  CircleCheck,
  Star,
  Clock,
  Goods,
  View,
  RefreshLeft,
  ShoppingBag,
  Shop,
  Bell
} from '@element-plus/icons-vue'
import { getProducts } from '@/data/products.js'
import { urgentOrder, updateOrderStatus } from '@/data/orders.js'
import { ElMessage } from 'element-plus'
import MiniCartoonCharacter from '@/components/MiniCartoonCharacter.vue'

export default {
  name: 'UserOrder',
  components: {
    Document,
    Van,
    CircleCheck,
    Star,
    Clock,
    Goods,
    View,
    RefreshLeft,
    ShoppingBag,
    Shop,
    Bell,
    MiniCartoonCharacter
  },
  props: {
    orders: {
      type: Array,
      default: () => []
    }
  },
  emits: ['viewDetail', 'buyAgain', 'goShopping', 'orderUpdated'],
  setup(props, { emit }) {
    const activeFilter = ref('all')

    const stats = computed(() => {
      const total = props.orders.length
      const pending = props.orders.filter(o => o.status === '待发货' || o.status === '已发货').length
      const completed = props.orders.filter(o => o.status === '已完成').length
      const totalAmount = props.orders
        .filter(o => o.status === '已完成')
        .reduce((sum, o) => sum + parseFloat(o.totalAmount), 0)
        .toFixed(2)
      return { total, pending, completed, totalAmount }
    })

    const filteredOrders = computed(() => {
      if (activeFilter.value === 'all') {
        return props.orders
      }
      return props.orders.filter(o => o.status === activeFilter.value)
    })

    const getStatusType = (status) => {
      const statusMap = {
        '待发货': 'warning',
        '已发货': 'primary',
        '已完成': 'success',
        '已取消': 'danger'
      }
      return statusMap[status] || 'info'
    }

    const handleConfirmReceive = (order) => {
      const success = updateOrderStatus(order.id, '已完成')
      if (success) {
        order.status = '已完成'
        ElMessage.success('确认收货成功，订单已完成')
        emit('orderUpdated')
      } else {
        ElMessage.error('确认收货失败')
      }
    }

    const getProductImage = (productId) => {
      const product = getProducts().find(p => p.id === productId)
      return product ? product.image : ''
    }

    const getFullOrderItems = (order) => {
      const productsList = getProducts()
      return order.items.map(item => {
        const product = productsList.find(p => p.id === item.productId)
        return {
          ...item,
          image: product ? product.image : '',
          stock: product ? product.stock : 999
        }
      })
    }

    const handleUrgent = (order) => {
      const result = urgentOrder(order.id)
      if (result.success) {
        ElMessage.success(result.message)
        emit('orderUpdated')
      } else {
        ElMessage.error(result.message)
      }
    }

    const emitBuyAgain = (order) => {
      const enhancedOrder = {
        ...order,
        items: getFullOrderItems(order)
      }
      emit('buyAgain', enhancedOrder)
    }

    return {
      activeFilter,
      stats,
      filteredOrders,
      getStatusType,
      getProductImage,
      getFullOrderItems,
      handleUrgent,
      handleConfirmReceive,
      emitBuyAgain
    }
  }
}
</script>

<style scoped>
.page-content {
  width: 100%;
  animation: fadeIn 0.5s ease-out;
}

.page-content :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.page-content :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(66, 185, 131, 0.15);
  transform: translateY(-1px);
}

.page-content :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(66, 185, 131, 0.15), 0 4px 12px rgba(66, 185, 131, 0.2);
  transform: translateY(-1px);
}

.page-content :deep(.el-button) {
  border-radius: 10px;
  transition: all 0.3s ease;
}

.page-content :deep(.el-button--primary) {
  background: linear-gradient(135deg, #42b983 0%, #35a070 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(66, 185, 131, 0.3);
}

.page-content :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(66, 185, 131, 0.4);
  background: linear-gradient(135deg, #35a070 0%, #2d8f62 100%);
}

.page-content :deep(.el-radio-button__inner) {
  border-radius: 10px;
  transition: all 0.3s ease;
}

.stats-section {
  animation: fadeIn 0.5s ease-out 0.1s backwards;
}

.filter-section {
  animation: fadeIn 0.5s ease-out 0.2s backwards;
}

.orders-section {
  animation: fadeIn 0.5s ease-out 0.3s backwards;
}


.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 25px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-icon-character {
  box-shadow: none;
  padding: 0;
}

.stat-icon-character .mini-character {
  width: 100%;
  height: 100%;
  transform: scale(1.4);
}

.stat-icon-character .mini-character.standing {
  transform: scale(1.4) translateY(-4px);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.filter-section {
  margin-bottom: 25px;
}



.filter-section :deep(.el-radio-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-section :deep(.el-radio-button__inner) {
  border: none;
  background-color: #f5f7fa;
  color: #606266;
  border-radius: 10px;
  padding: 10px 22px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.filter-section :deep(.el-radio-button__inner:hover) {
  color: #42b983;
  background-color: #f0f9f4;
}

.filter-section :deep(.el-radio-button:first-child .el-radio-button__inner),
.filter-section :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 10px;
}

.filter-section :deep(.el-radio-button__orig-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #42b983 0%, #35a070 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(66, 185, 131, 0.4);
}

.empty-section {
  padding: 80px 0;
}

.empty-content {
  text-align: center;
}

.empty-image {
  width: 160px;
  height: 160px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f5ed 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #42b983;
  font-size: 72px;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-card {
  border: none;
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(135deg, #ffffff 0%, #fafbfc 100%);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fadeIn 0.5s ease-out backwards;
}

.order-card:nth-child(1) { animation-delay: 0.05s; }
.order-card:nth-child(2) { animation-delay: 0.1s; }
.order-card:nth-child(3) { animation-delay: 0.15s; }
.order-card:nth-child(4) { animation-delay: 0.2s; }
.order-card:nth-child(5) { animation-delay: 0.25s; }

.order-card:hover {
  transform: translateY(-6px) scale(1.01);
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.15);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 24px;
  background: linear-gradient(135deg, #f8f9fa 0%, #f0f9f4 100%);
  border-bottom: 1px solid #e8f5ed;
}

.order-info {
  display: flex;
  gap: 24px;
  align-items: center;
}

.order-id,
.order-date {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 14px;
}

.order-id {
  color: #303133;
  font-weight: 500;
}

.order-items {
  padding: 20px 24px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px dashed #e8f5ed;
}

.order-item:last-child {
  border-bottom: none;
}

.item-image {
  width: 72px;
  height: 72px;
  border-radius: 12px;
  overflow: hidden;
  background: #f5f7fa;
  flex-shrink: 0;
  margin-right: 16px;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 28px;
}

.item-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.item-name {
  color: #303133;
  font-weight: 500;
  font-size: 15px;
}

.item-spec {
  color: #909399;
  font-size: 13px;
}

.item-quantity {
  width: 80px;
  text-align: center;
  color: #606266;
  font-size: 15px;
}

.item-price {
  width: 100px;
  text-align: right;
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 24px;
  background: linear-gradient(135deg, #f8f9fa 0%, #f0f9f4 100%);
  border-top: 1px solid #e8f5ed;
}

.order-summary {
  color: #606266;
  font-size: 14px;
}

.order-total {
  margin-left: 20px;
  color: #f56c6c;
  font-size: 15px;
}

.order-total strong {
  font-size: 20px;
}

.order-actions {
  display: flex;
  gap: 10px;
}
</style>
