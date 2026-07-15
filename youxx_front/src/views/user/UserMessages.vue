<template>
  <div class="user-messages-page">
    <div class="messages-container">
      <div class="chat-panel">
        <div class="chat-header">
          <div class="chat-header-info">
            <el-icon :size="24"><Shop /></el-icon>
            <span class="chat-header-name">联系商家</span>
            <span class="chat-header-status">在线</span>
          </div>
        </div>

        <div class="chat-messages" ref="chatMessagesRef">
          <div v-if="currentMessages.length === 0" class="chat-empty-hint">
            <el-icon :size="48"><ChatDotRound /></el-icon>
            <p>暂无消息，发送一条消息联系商家吧</p>
          </div>
          <div
            v-for="msg in currentMessages"
            :key="msg.id"
            class="chat-message"
            :class="{ 'message-self': msg.from === 'user' }"
          >
            <!-- 头像始终紧贴气泡外侧：商家消息头像在左、用户消息头像在右 -->
            <div class="message-avatar" v-if="msg.from !== 'user'">
              <div class="avatar-placeholder small merchant-avatar">
                <el-icon :size="20"><Shop /></el-icon>
              </div>
            </div>
            <div class="message-bubble" :class="{ 'bubble-self': msg.from === 'user', 'bubble-merchant': msg.from !== 'user' }">
              {{ msg.content }}
            </div>
            <div class="message-avatar" v-if="msg.from === 'user'">
              <div class="avatar-placeholder small user-avatar">
                <el-icon :size="20"><User /></el-icon>
              </div>
            </div>
          </div>
        </div>

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
              <el-icon><Promotion /></el-icon>
              发送
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue'
import { User, ChatDotRound, Shop, Promotion } from '@element-plus/icons-vue'
import {
  getConversationMessagesApi,
  sendMessageApi
} from '@/services/api.js'

export default {
  name: 'UserMessages',
  components: {
    User,
    ChatDotRound,
    Shop,
    Promotion
  },
  setup() {
    const currentMessages = ref([])
    const inputMessage = ref('')
    const chatMessagesRef = ref(null)
    const currentUser = ref('')
    const conversationId = ref('')

    const loadMessages = async () => {
      if (!conversationId.value) return
      try {
        const res = await getConversationMessagesApi(conversationId.value)
        currentMessages.value = (res.data || []).map(m => ({
          id: m.id,
          from: m.sender === 'ADMIN' ? 'admin' : 'user',
          fromName: m.senderName,
          content: m.content,
          time: m.createTime
        }))
        nextTick(() => {
          scrollToBottom()
        })
      } catch (e) {
        console.error('加载消息失败', e)
      }
    }

    const sendMessage = async () => {
      if (!inputMessage.value.trim() || !conversationId.value) return

      try {
        await sendMessageApi(conversationId.value, inputMessage.value.trim())
        inputMessage.value = ''
        await loadMessages()
      } catch (e) {
        console.error('发送消息失败', e)
      }
    }

    const scrollToBottom = () => {
      if (chatMessagesRef.value) {
        chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
      }
    }

    onMounted(() => {
      currentUser.value = localStorage.getItem('username') || '用户'
      conversationId.value = `conv_${currentUser.value}`
      loadMessages()
    })

    return {
      currentMessages,
      inputMessage,
      chatMessagesRef,
      sendMessage
    }
  }
}
</script>

<style scoped>
.user-messages-page {
  height: 100%;
  background: #ffffff;
  border: 6px solid #9dd1df;
  border-radius: 20px;
}

.messages-container {
  display: flex;
  height: calc(100vh - 120px);
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
}

/* ===== 顶部标题栏：渐变 + 在线状态 ===== */
.chat-header {
  padding: 18px 24px;
  background: linear-gradient(135deg, #36d1a8 0%, #42b983 100%);
  border-bottom: none;
  box-shadow: 0 2px 10px rgba(66, 185, 131, 0.25);
}

.chat-header-info {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #fff;
}

.chat-header :deep(.el-icon) {
  color: #fff;
}

.chat-header-name {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  letter-spacing: 0.5px;
}

.chat-header-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-left: 6px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.85);
}

.chat-header-status::before {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #d4ffea;
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.25);
}

/* ===== 消息列表区 ===== */
.chat-messages {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(66, 185, 131, 0.35);
  border-radius: 3px;
}

.chat-empty-hint {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: #c0c4cc;
}

.chat-empty-hint p {
  font-size: 14px;
  color: #909399;
}

/* ===== 单条消息：头像贴气泡外侧，尾巴指向头像 ===== */
.chat-message {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  /* 商家消息：头像在左、气泡在右，整体左对齐 */
  justify-content: flex-start;
}

/* 用户自己的消息：头像在右、气泡紧贴左侧，整体右对齐 */
.chat-message.message-self {
  justify-content: flex-end;
}

.message-avatar {
  flex-shrink: 0;
  padding-top: 2px;
}

.avatar-placeholder {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}

.avatar-placeholder.small {
  width: 38px;
  height: 38px;
}

.merchant-avatar {
  background: linear-gradient(135deg, #304156 0%, #409eff 100%);
}

.user-avatar {
  background: linear-gradient(135deg, #42b983 0%, #35a070 100%);
}

.message-bubble {
  max-width: 62%;
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.55;
  word-break: break-word;
  position: relative;
  animation: bubbleIn 0.2s ease;
}

@keyframes bubbleIn {
  from { opacity: 0; transform: translateY(6px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 商家气泡：白底，尾巴朝左指向左侧头像 */
.bubble-merchant {
  background: #fff;
  color: #303133;
  border-top-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 用户气泡：主题绿，尾巴朝右指向右侧头像 */
.bubble-self {
  background: linear-gradient(135deg, #42b983 0%, #36d1a8 100%);
  color: #fff;
  border-top-right-radius: 4px;
  box-shadow: 0 3px 10px rgba(66, 185, 131, 0.3);
}

/* ===== 输入区 ===== */
.chat-input {
  padding: 16px 24px 20px;
  background: #fff;
  border-top: 1px solid #ebeef5;
}

.chat-input :deep(.el-textarea__inner) {
  border-radius: 12px;
  resize: none;
  font-family: inherit;
}

.chat-input :deep(.el-textarea__inner):focus {
  box-shadow: 0 0 0 2px rgba(66, 185, 131, 0.2);
}

.chat-input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.chat-input-actions .el-button {
  background: linear-gradient(135deg, #42b983 0%, #36d1a8 100%);
  border: none;
  border-radius: 10px;
  padding: 10px 28px;
  font-weight: 500;
  box-shadow: 0 3px 10px rgba(66, 185, 131, 0.3);
}

.chat-input-actions .el-button:hover {
  background: linear-gradient(135deg, #35a070 0%, #2bbf95 100%);
}

.chat-input-actions .el-button.is-disabled {
  background: #c8c9cc;
  box-shadow: none;
}
</style>
