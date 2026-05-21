<template>
  <div class="dashboard-content">
    <div class="welcome-section">
      <div class="welcome-text">
        <h1>欢迎回来，{{ currentUser }}！</h1>
        <p class="welcome-subtitle">祝您今天工作愉快</p>
      </div>
      <div class="welcome-date">
        <el-icon><Calendar /></el-icon>
        <span>{{ currentDate }}</span>
      </div>
    </div>

    <div class="stats-cards">
      <div class="stat-card" style="--card-color: #409eff;">
        <div class="stat-card-content">
          <div class="stat-icon-wrapper">
            <div class="stat-icon">
              <el-icon><User /></el-icon>
            </div>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ users.length }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </div>
        <div class="stat-footer">
          <span class="stat-trend up">
            <el-icon><Top /></el-icon>
            12%
          </span>
          <span class="stat-compare">较上月</span>
        </div>
      </div>

      <div class="stat-card" style="--card-color: #67c23a;">
        <div class="stat-card-content">
          <div class="stat-icon-wrapper">
            <div class="stat-icon">
              <el-icon><Goods /></el-icon>
            </div>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ products.length }}</div>
            <div class="stat-label">产品数量</div>
          </div>
        </div>
        <div class="stat-footer">
          <span class="stat-trend up">
            <el-icon><Top /></el-icon>
            8%
          </span>
          <span class="stat-compare">较上月</span>
        </div>
      </div>

      <div class="stat-card" style="--card-color: #e6a23c;">
        <div class="stat-card-content">
          <div class="stat-icon-wrapper">
            <div class="stat-icon">
              <el-icon><Document /></el-icon>
            </div>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ orders.length }}</div>
            <div class="stat-label">订单总数</div>
          </div>
        </div>
        <div class="stat-footer">
          <span class="stat-trend up">
            <el-icon><Top /></el-icon>
            23%
          </span>
          <span class="stat-compare">较上月</span>
        </div>
      </div>

      <div class="stat-card" style="--card-color: #f56c6c;">
        <div class="stat-card-content">
          <div class="stat-icon-wrapper">
            <div class="stat-icon">
              <el-icon><Money /></el-icon>
            </div>
          </div>
          <div class="stat-info">
            <div class="stat-number">¥{{ totalRevenue }}</div>
            <div class="stat-label">今日收入</div>
          </div>
        </div>
        <div class="stat-footer">
          <span class="stat-trend up">
            <el-icon><Top /></el-icon>
            15%
          </span>
          <span class="stat-compare">较昨日</span>
        </div>
      </div>

      <div class="stat-card" style="--card-color: #9b59b6;">
        <div class="stat-card-content">
          <div class="stat-icon-wrapper">
            <div class="stat-icon">
              <el-icon><Bell /></el-icon>
            </div>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ unreadMessages.length }}</div>
            <div class="stat-label">未读消息</div>
          </div>
        </div>
        <div class="stat-footer">
          <span class="stat-trend down">
            <el-icon><Bottom /></el-icon>
            5%
          </span>
          <span class="stat-compare">较昨日</span>
        </div>
      </div>

      <div class="stat-card" style="--card-color: #3498db;">
        <div class="stat-card-content">
          <div class="stat-icon-wrapper">
            <div class="stat-icon">
              <el-icon><Clock /></el-icon>
            </div>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ pendingOrders.length }}</div>
            <div class="stat-label">待办事件</div>
          </div>
        </div>
        <div class="stat-footer">
          <span class="stat-trend">
            <el-icon><Minus /></el-icon>
            0%
          </span>
          <span class="stat-compare">较昨日</span>
        </div>
      </div>
    </div>

    <div class="info-sections">
      <div class="info-section">
        <div class="section-header">
          <div class="section-title">
            <div class="section-icon" style="background: linear-gradient(135deg, #9b59b6, #8e44ad);">
              <el-icon><Bell /></el-icon>
            </div>
            <h3>未读消息</h3>
          </div>
          <el-badge :value="unreadMessages.length" type="danger" class="section-badge" />
        </div>
        <div class="message-list">
          <div 
            v-for="msg in unreadMessages" 
            :key="msg.id" 
            class="message-item consult"
            @click="$emit('navigate', 'messages')"
            style="cursor: pointer;"
          >
            <div class="message-icon consult">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="message-content">
              <div class="message-header">
                <span class="message-title">{{ msg.fromName }}</span>
                <span class="message-time">{{ formatTime(msg.time) }}</span>
              </div>
              <div class="message-desc">{{ msg.content }}</div>
            </div>
          </div>
          <div v-if="unreadMessages.length === 0" class="empty-tip">
            <el-icon><ChatDotRound /></el-icon>
            <span>暂无未读消息</span>
          </div>
        </div>
      </div>

      <div class="info-section">
        <div class="section-header">
          <div class="section-title">
            <div class="section-icon" style="background: linear-gradient(135deg, #3498db, #2980b9);">
              <el-icon><Clock /></el-icon>
            </div>
            <h3>待办事件</h3>
          </div>
          <el-badge :value="pendingOrders.length" type="warning" class="section-badge" />
        </div>
        <div class="todo-list">
          <div 
            v-for="order in pendingOrders" 
            :key="order.id" 
            class="todo-item"
          >
            <div class="todo-status">
              <el-icon><Box /></el-icon>
            </div>
            <div class="todo-content">
              <div class="todo-header">
                <span class="todo-title">订单 {{ order.id }}</span>
                <el-tag size="small" type="warning">待发货</el-tag>
              </div>
              <div class="todo-info">
                <span><el-icon><User /></el-icon> {{ order.customer }}</span>
                <span><el-icon><Money /></el-icon> ¥{{ order.amount }}</span>
                <span><el-icon><Calendar /></el-icon> {{ order.date }}</span>
              </div>
            </div>
            <el-button size="small" type="primary" round @click="$emit('navigate', 'order')">
              处理
            </el-button>
          </div>
          <div v-if="pendingOrders.length === 0" class="empty-tip">
            <el-icon><CircleCheck /></el-icon>
            <span>暂无待办事项</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed, ref } from 'vue'
import { User, Goods, Document, Money, Bell, Clock, Warning, ChatDotRound, InfoFilled, Box, Top, Bottom, Minus, Calendar, CircleCheck } from '@element-plus/icons-vue'
import { getProducts as loadProducts, categories as categoriesData } from '../../data/products'
import { getMessages } from '@/data/messages.js'

export default {
  name: 'DashboardHome',
  components: { User, Goods, Document, Money, Bell, Clock, Warning, ChatDotRound, InfoFilled, Box, Top, Bottom, Minus, Calendar, CircleCheck },
  props: {
    currentUser: { type: String, default: '' },
    users: { type: Array, default: () => [] },
    orders: { type: Array, default: () => [] }
  },
  emits: ['navigate'],
  setup(props) {
    const products = loadProducts()
    const categories = categoriesData

    const getCategoryName = (categoryId) => {
      const category = categories.find(c => c.id === categoryId)
      return category ? category.name : categoryId
    }

    const totalRevenue = computed(() => {
      return props.orders.reduce((sum, o) => sum + (o.amount || 0), 0).toFixed(2)
    })

    const pendingOrders = computed(() => {
      return props.orders.filter(o => o.status === '待发货')
    })

    const currentDate = computed(() => {
      const now = new Date()
      const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
      return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 ${weekDays[now.getDay()]}`
    })

    const unreadMessages = computed(() => {
      const messages = getMessages()
      return messages.filter(m => !m.isRead && m.from !== 'admin')
    })

    const formatTime = (timeStr) => {
      const time = new Date(timeStr)
      const now = new Date()
      const diff = now - time

      if (diff < 1000 * 60) return '刚刚'
      if (diff < 1000 * 60 * 60) return `${Math.floor(diff / (1000 * 60))}分钟前`
      if (diff < 1000 * 60 * 60 * 24) return `${Math.floor(diff / (1000 * 60 * 60))}小时前`

      const month = String(time.getMonth() + 1).padStart(2, '0')
      const day = String(time.getDate()).padStart(2, '0')
      const hours = String(time.getHours()).padStart(2, '0')
      const minutes = String(time.getMinutes()).padStart(2, '0')

      if (diff < 1000 * 60 * 60 * 24 * 7) {
        return `${month}-${day} ${hours}:${minutes}`
      }

      const year = time.getFullYear()
      return `${year}-${month}-${day}`
    }

    return { products, getCategoryName, totalRevenue, pendingOrders, unreadMessages, formatTime, currentDate }
  }
}
</script>

<style scoped>
.dashboard-content {
  padding: 0;
}

.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #7d53a7 100%);
  border-radius: 12px;
  color: #fff;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.welcome-text h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.welcome-subtitle {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.welcome-date {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  opacity: 0.9;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
  .welcome-section {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
}

.stat-card {
  background: #ffffff;
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: var(--card-color);
  border-radius: 12px 0 0 12px;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
}

.stat-card-content {
  display: flex;
  align-items: left;
  margin-bottom: 16px;
}

.stat-icon-wrapper {
  margin-right: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--card-color);
  color: #fff;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
  text-align: left;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
  text-align: left;
}

.stat-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f0f2f5;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 13px;
  font-weight: 500;
}

.stat-trend.up {
  color: #67c23a;
}

.stat-trend.down {
  color: #f56c6c;
}

.stat-compare {
  font-size: 12px;
  color: #c0c4cc;
}

.info-sections {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

@media (max-width: 1000px) {
  .info-sections {
    grid-template-columns: 1fr;
  }
}

.info-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f2f5;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
}

.section-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.message-list,
.todo-list {
  max-height: 320px;
  overflow-y: auto;
}

.message-list::-webkit-scrollbar,
.todo-list::-webkit-scrollbar {
  width: 6px;
}

.message-list::-webkit-scrollbar-thumb,
.todo-list::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.message-list::-webkit-scrollbar-thumb:hover,
.todo-list::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.message-item {
  display: flex;
  align-items: flex-start;
  padding: 14px;
  margin-bottom: 10px;
  background: #fafafa;
  border-radius: 10px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.message-item:hover {
  background: #f0f2f5;
  transform: translateX(4px);
}

.message-item.urge {
  border-left: 3px solid #f56c6c;
}

.message-item.consult {
  border-left: 3px solid #409eff;
}

.message-item.notice {
  border-left: 3px solid #67c23a;
}

.message-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
  font-size: 18px;
}

.message-icon.urge {
  background: linear-gradient(135deg, #f56c6c, #e65555);
  color: #fff;
}

.message-icon.consult {
  background: linear-gradient(135deg, #409eff, #3388e6);
  color: #fff;
}

.message-icon.notice {
  background: linear-gradient(135deg, #67c23a, #55a532);
  color: #fff;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.message-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.message-time {
  font-size: 12px;
  color: #909399;
}

.message-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

.todo-item {
  display: flex;
  align-items: flex-start;
  padding: 14px;
  margin-bottom: 10px;
  background: #fafafa;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.todo-item:hover {
  background: #f0f2f5;
  transform: translateX(4px);
}

.todo-status {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #e6a23c, #d48806);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
  color: #fff;
  font-size: 18px;
}

.todo-content {
  flex: 1;
  min-width: 0;
}

.todo-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.todo-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.todo-info {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.todo-info span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #c0c4cc;
  font-size: 14px;
}

.empty-tip .el-icon {
  font-size: 48px;
  margin-bottom: 12px;
  color: #dcdfe6;
}
</style>
