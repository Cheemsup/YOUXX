<template>
  <div class="user-messages-page">
    <div class="messages-container">
      <div class="chat-panel">
        <div class="chat-header">
          <div class="chat-header-info">
            <el-icon :size="24"><Shop /></el-icon>
            <span class="chat-header-name">联系商家</span>
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
  sendUserMessage,
  getConversationMessages
} from '@/data/messages.js'

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

    const loadMessages = () => {
      if (!currentUser.value) return
      const conversationId = `conv_${currentUser.value}`
      currentMessages.value = getConversationMessages(conversationId)
      nextTick(() => {
        scrollToBottom()
      })
    }

    const sendMessage = () => {
      if (!inputMessage.value.trim() || !currentUser.value) return

      sendUserMessage(currentUser.value, inputMessage.value.trim())
      inputMessage.value = ''
      loadMessages()
    }

    const scrollToBottom = () => {
      if (chatMessagesRef.value) {
        chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
      }
    }

    onMounted(() => {
      currentUser.value = sessionStorage.getItem('username') || '用户'
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
  background: #f5f7fa;
}

.chat-header {
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
}

.chat-header-info {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #42b983;
}

.chat-header-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.chat-messages {
  flex: 1;
  padding: 20px 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
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

.chat-message {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.chat-message.message-self {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.avatar-placeholder {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.avatar-placeholder.small {
  width: 36px;
  height: 36px;
}

.merchant-avatar {
  background: linear-gradient(135deg, #304156 0%, #409eff 100%);
}

.user-avatar {
  background: linear-gradient(135deg, #42b983 0%, #35a070 100%);
}

.message-bubble {
  max-width: 60%;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  word-break: break-word;
}

.bubble-self {
  background: #42b983;
  color: #fff;
  border-top-right-radius: 4px;
}

.bubble-merchant {
  background: #fff;
  color: #303133;
  border-top-left-radius: 4px;
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

.chat-input-actions .el-button {
  background: #42b983;
  border-color: #42b983;
  border-radius: 8px;
  padding: 10px 24px;
}

.chat-input-actions .el-button:hover {
  background: #35a070;
  border-color: #35a070;
}
</style>
