<!--
  消息管理界面组件
  类似微信的聊天界面布局
  左侧为消息列表，右侧为聊天窗口
-->
<template>
  <div class="messages-page">
    <div class="messages-container">
      <!-- 左侧消息列表 -->
      <div class="message-list-panel">
        <!-- 消息列表头部 -->
        <div class="message-list-header">
          <h3>消息</h3>
          <span class="unread-badge" v-if="unreadCount > 0">{{ unreadCount }}</span>
        </div>
        <!-- 消息列表内容 -->
        <div class="message-list">
          <!-- 遍历所有会话 -->
          <div
            v-for="conv in conversations"
            :key="conv.id"
            class="message-list-item"
            :class="{ active: currentConversationId === conv.id }"
            @click="selectConversation(conv)"
          >
            <!-- 用户头像 -->
            <div class="message-avatar">
              <div class="avatar-placeholder">
                <el-icon :size="24"><User /></el-icon>
              </div>
            </div>
            <!-- 消息信息 -->
            <div class="message-info">
              <!-- 消息头部：用户名和时间 -->
              <div class="message-header">
                <span class="message-name">{{ conv.fromName }}</span>
                <span class="message-time">{{ formatTime(conv.lastTime) }}</span>
              </div>
              <!-- 消息内容：最后一条消息和未读数 -->
              <div class="message-content">
                <span class="message-text">{{ conv.lastMessage }}</span>
                <el-badge
                  v-if="conv.unreadCount > 0"
                  :value="conv.unreadCount"
                  :max="99"
                  type="primary"
                />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧聊天框 -->
      <div class="chat-panel" v-if="currentConversation">
        <!-- 聊天头部：用户信息 -->
        <div class="chat-header">
          <div class="chat-user-info">
            <div class="chat-avatar">
              <div class="avatar-placeholder small">
                <el-icon :size="20"><User /></el-icon>
              </div>
            </div>
            <div class="chat-user-detail">
              <span class="chat-user-name">{{ currentConversation.fromName }}</span>
              <span class="chat-user-status">在线</span>
            </div>
          </div>
        </div>

        <!-- 聊天消息区域 -->
        <div class="chat-messages" ref="chatMessagesRef">
          <!-- 遍历当前会话的所有消息 -->
          <div
            v-for="msg in currentMessages"
            :key="msg.id"
            class="chat-message"
            :class="{ 'message-self': msg.from === 'admin' }"
          >
            <!-- 对方头像（非管理员消息） -->
            <div class="message-avatar" v-if="msg.from !== 'admin'">
              <div class="avatar-placeholder small">
                <el-icon :size="20"><User /></el-icon>
              </div>
            </div>
            <!-- 消息气泡 -->
            <div class="message-bubble" :class="{ 'bubble-self': msg.from === 'admin' }">
              {{ msg.content }}
            </div>
            <!-- 管理员头像（管理员消息） -->
            <div class="message-avatar" v-if="msg.from === 'admin'">
              <div class="avatar-placeholder small">
                <el-icon :size="20"><Avatar /></el-icon>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入框区域 -->
        <div class="chat-input">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="输入消息..."
            @keydown.enter.exact.prevent="sendMessage"
          />
          <div class="chat-input-actions">
            <el-button type="primary" @click="sendMessage" :disabled="!inputMessage.trim()">
              发送
            </el-button>
          </div>
        </div>
      </div>

      <!-- 空状态：未选择会话时显示 -->
      <div class="chat-empty" v-else>
        <el-empty description="请选择一个对话开始聊天" />
      </div>
    </div>
  </div>
</template>

<script>
// 导入 Vue 3 组合式 API
import { ref, computed, onMounted, nextTick } from 'vue'
// 导入图标组件
import { User, Avatar } from '@element-plus/icons-vue'
// 导入消息数据管理函数
import {
  getConversations,
  getConversationMessages,
  sendMessage as sendMsg,
  markConversationAsRead
} from '@/data/messages.js'

export default {
  name: 'DashboardMessages',
  // 注册组件
  components: {
    User,
    Avatar
  },
  setup() {
    // 会话列表
    const conversations = ref([])
    // 当前选中的会话 ID
    const currentConversationId = ref('')
    // 当前会话的消息列表
    const currentMessages = ref([])
    // 输入框中的消息
    const inputMessage = ref('')
    // 聊天消息区域的 DOM 引用
    const chatMessagesRef = ref(null)

    // 计算未读消息总数
    const unreadCount = computed(() => {
      return conversations.value.reduce((sum, conv) => sum + conv.unreadCount, 0)
    })

    // 计算当前选中的会话
    const currentConversation = computed(() => {
      return conversations.value.find(conv => conv.id === currentConversationId.value)
    })

    // 加载会话列表
    const loadConversations = () => {
      conversations.value = getConversations()
    }

    // 选择会话
    const selectConversation = (conv) => {
      currentConversationId.value = conv.id
      // 标记该会话为已读
      markConversationAsRead(conv.id)
      // 加载该会话的消息
      currentMessages.value = getConversationMessages(conv.id)
      // 重新加载会话列表（更新未读数）
      loadConversations()
      
      // 滚动到最新消息
      nextTick(() => {
        scrollToBottom()
      })
    }

    // 发送消息
    const sendMessage = () => {
      if (!inputMessage.value.trim() || !currentConversationId.value) return
      
      // 发送消息到数据层
      sendMsg(currentConversationId.value, 'admin', '管理员', inputMessage.value.trim())
      inputMessage.value = ''
      // 重新加载消息列表
      currentMessages.value = getConversationMessages(currentConversationId.value)
      loadConversations()
      
      // 滚动到最新消息
      nextTick(() => {
        scrollToBottom()
      })
    }

    // 滚动到聊天底部
    const scrollToBottom = () => {
      if (chatMessagesRef.value) {
        chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
      }
    }

    // 格式化时间显示
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

    // 组件挂载时加载数据
    onMounted(() => {
      loadConversations()
    })

    return {
      conversations,
      currentConversationId,
      currentMessages,
      inputMessage,
      chatMessagesRef,
      unreadCount,
      currentConversation,
      selectConversation,
      sendMessage,
      formatTime
    }
  }
}
</script>

<style scoped>
/* ==================== 页面容器 ==================== */
.messages-page {
  height: 100%;
  background: #ffffff;
  border:6px solid #9dd1df;
  border-radius: 20px;
  
}

/* 消息主容器：左右分栏布局 */
.messages-container {
  display: flex;
  height: calc(100vh - 120px);
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* ==================== 左侧消息列表面板 ==================== */
.message-list-panel {
  width: 360px;
  flex-shrink: 0;
  border-right: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
}

/* 消息列表头部 */
.message-list-header {
  padding: 20px;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.message-list-header h3 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

/* 未读消息徽章 */
.unread-badge {
  background: #f56c6c;
  color: #fff;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
}

/* 消息列表滚动区域 */
.message-list {
  flex: 1;
  overflow-y: auto;
}

/* 消息列表项 */
.message-list-item {
  display: flex;
  padding: 16px 20px;
  cursor: pointer;
  transition: background 0.2s ease;
  border-bottom: 1px solid #f5f5f5;
}

.message-list-item:hover {
  background: #f5f7fa;
}

/* 激活的消息列表项 */
.message-list-item.active {
  background: #e6f1ff;
  border-left: 6px solid #409eff;
}

/* 头像区域 */
.message-avatar {
  margin-right: 12px;
}

/* 头像占位符（渐变圆形） */
.avatar-placeholder {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

/* 小尺寸头像 */
.avatar-placeholder.small {
  width: 36px;
  height: 36px;
}

/* 消息信息区域 */
.message-info {
  flex: 1;
  min-width: 0;
}

/* 消息头部（用户名和时间） */
.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

/* 用户名 */
.message-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

/* 消息时间 */
.message-time {
  font-size: 12px;
  color: #909399;
}

/* 消息内容区域 */
.message-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 消息文本（最后一句话） */
.message-text {
  font-size: 13px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

/* ==================== 右侧聊天框 ==================== */
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

/* 聊天头部 */
.chat-header {
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
}

/* 聊天用户信息 */
.chat-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-user-detail {
  display: flex;
  flex-direction: column;
}

/* 聊天用户名 */
.chat-user-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 在线状态 */
.chat-user-status {
  font-size: 12px;
  color: #67c23a;
}

/* 聊天消息滚动区域 */
.chat-messages {
  flex: 1;
  padding: 20px 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 单条消息 */
.chat-message {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

/* 管理员自己的消息（右对齐） */
.chat-message.message-self {
  flex-direction: row-reverse;
}

/* 消息气泡 */
.message-bubble {
  max-width: 60%;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  font-size: 14px;
  color: #303133;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

/* 管理员消息气泡（蓝色） */
.message-bubble.bubble-self {
  background: #409eff;
  color: #fff;
}

.chat-input {
  padding: 16px 24px;
  background: #fff;
  border-top: 1px solid #e6e6e6;
}

.chat-input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

/* 空状态 */
.chat-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
</style>
