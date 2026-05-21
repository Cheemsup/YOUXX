<template>
  <div class="ai-chat-container" :class="{ 'is-open': isOpen }">
    <div class="chat-toggle-btn" @click="toggleChat">
      <el-icon v-if="!isOpen"><ChatDotRound /></el-icon>
      <el-icon v-else><Close /></el-icon>
    </div>
    
    <div class="chat-window" v-if="isOpen">
      <div class="chat-header">
        <div class="header-left">
          <div class="ai-avatar">
            <MiniCartoonCharacter type="purple" :disable-mouse="true" />
          </div>
          <div class="ai-info">
            <h4>智能导购助手</h4>
            <span class="status-dot"></span>
            <span class="status-text">在线</span>
          </div>
        </div>
        <el-button text @click="clearChat" size="small">
          <el-icon><Delete /></el-icon>
          清空
        </el-button>
      </div>
      
      <div class="chat-messages" ref="messagesContainer">
        <div v-for="(msg, index) in messages" :key="index" class="message-item" :class="msg.role">
          <div v-if="msg.role === 'assistant'" class="avatar">
            <MiniCartoonCharacter type="purple" :disable-mouse="true" />
          </div>
          <div class="message-content">
            <div class="message-text">{{ msg.content }}</div>
            
            <div v-if="msg.productList" class="product-cards">
              <div v-for="product in msg.productList" :key="product.id" class="mini-product-card" @click="handleProductClick(product)">
                <img v-if="product.image" :src="product.image" :alt="product.name" />
                <div class="mini-info">
                  <div class="mini-name">{{ product.name }}</div>
                  <div class="mini-price">¥{{ (product.price * product.discount).toFixed(2) }}</div>
                </div>
              </div>
            </div>
            
            <div v-if="msg.product" class="product-detail-card">
              <div class="detail-image">
                <img v-if="msg.product.image" :src="msg.product.image" :alt="msg.product.name" />
              </div>
              <div class="detail-info">
                <h5>{{ msg.product.name }}</h5>
                <p class="detail-desc">{{ msg.product.description }}</p>
                <div class="detail-price">
                  <span class="current">¥{{ (msg.product.price * msg.product.discount).toFixed(2) }}</span>
                  <span v-if="msg.product.discount < 1" class="original">¥{{ msg.product.price.toFixed(2) }}</span>
                  <span class="unit">/{{ msg.product.unit }}</span>
                </div>
                <div class="detail-tags">
                  <el-tag v-for="tag in msg.product.tags" :key="tag" size="small" type="info">{{ tag }}</el-tag>
                </div>
                <el-button type="primary" size="small" class="add-cart-btn" @click="handleAddToCart(msg.product)">
                  <el-icon><Plus /></el-icon>
                  加入购物车
                </el-button>
              </div>
            </div>
          </div>
          <div v-if="msg.role === 'user'" class="avatar">
            <MiniCartoonCharacter type="orange" :disable-mouse="true" />
          </div>
        </div>
        
        <div v-if="isTyping" class="message-item assistant">
          <div class="avatar">
            <MiniCartoonCharacter type="purple" :disable-mouse="true" />
          </div>
          <div class="message-content">
            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="quick-questions" v-if="messages.length <= 1">
        <span class="quick-label">试试问：</span>
        <div class="quick-buttons">
          <el-button size="small" @click="sendQuickQuestion('有什么热销商品推荐吗？')">热销推荐</el-button>
          <el-button size="small" @click="sendQuickQuestion('饮料有哪些？')">饮料</el-button>
          <el-button size="small" @click="sendQuickQuestion('可乐多少钱？')">价格查询</el-button>
        </div>
      </div>
      
      <div class="chat-input-area">
        <el-input
          v-model="inputMessage"
          placeholder="输入您的问题..."
          @keyup.enter="sendMessage"
          :disabled="isTyping"
          clearable
        >
          <template #suffix>
            <el-button :icon="Promotion" circle type="primary" @click="sendMessage" :disabled="isTyping || !inputMessage.trim()" />
          </template>
        </el-input>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { ChatDotRound, Close, Robot, User, Plus, Promotion, Delete } from '@element-plus/icons-vue'
import { getAIResponse, askAboutProduct } from '@/services/aiService'
import { ElMessage } from 'element-plus'
import MiniCartoonCharacter from './MiniCartoonCharacter.vue'

const emit = defineEmits(['addToCart', 'productClick'])

const isOpen = ref(false)
const messages = ref([])
const inputMessage = ref('')
const isTyping = ref(false)
const messagesContainer = ref(null)

const initMessage = {
  role: 'assistant',
  content: '您好！我是智能导购助手，有什么可以帮您的吗？您可以问我商品信息、价格查询或者让我为您推荐商品哦~'
}

const toggleChat = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value && messages.value.length === 0) {
    messages.value = [initMessage]
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isTyping.value) return
  
  const userMsg = inputMessage.value.trim()
  messages.value.push({
    role: 'user',
    content: userMsg
  })
  inputMessage.value = ''
  scrollToBottom()
  
  isTyping.value = true
  
  try {
    const conversationHistory = messages.value.slice(0, -1).map(msg => ({
      role: msg.role,
      content: msg.content
    }))
    const aiResponse = await getAIResponse(userMsg, conversationHistory)
    processAIResponse(aiResponse)
  } catch (error) {
    messages.value.push({
      role: 'assistant',
      content: '抱歉，我现在有点忙，稍后再试吧~'
    })
  } finally {
    isTyping.value = false
    scrollToBottom()
  }
}

const processAIResponse = (aiResponse) => {
  const msg = {
    role: 'assistant',
    content: aiResponse.content
  }
  
  if (aiResponse.type === 'product_list') {
    msg.productList = aiResponse.products
  } else if (aiResponse.type === 'product_detail') {
    msg.product = aiResponse.product
  }
  
  messages.value.push(msg)
}

const sendQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

const clearChat = () => {
  messages.value = [initMessage]
  ElMessage.success('聊天记录已清空')
}

const handleProductClick = (product) => {
  emit('productClick', product)
  const aiResponse = askAboutProduct(product)
  processAIResponse(aiResponse)
  scrollToBottom()
}

const handleAddToCart = (product) => {
  emit('addToCart', product)
}

defineExpose({
  askAboutProduct: (product) => {
    if (!isOpen.value) {
      isOpen.value = true
      messages.value = [initMessage]
    }
    const aiResponse = askAboutProduct(product)
    processAIResponse(aiResponse)
    scrollToBottom()
  }
})
</script>

<style scoped>
.ai-chat-container {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 1000;
}

/* 响应式定位：确保在小屏幕上也能看到 */
@media (max-height: 700px) {
  .ai-chat-container {
    bottom: 15px;
    right: 15px;
  }
}

.chat-toggle-btn {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  color: white;
  font-size: 28px;
}

.chat-toggle-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 25px rgba(102, 126, 234, 0.5);
}

.chat-window {
  position: absolute;
  bottom: 75px;
  right: 0;
  width: 420px;
  max-height: calc(100vh - 150px); /* 最大高度为视口高度减去底部和顶部安全距离 */
  height: 600px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

/* 响应式调整：小屏幕适配 */
@media (max-height: 768px) {
  .chat-window {
    bottom: 70px;
    max-height: calc(100vh - 120px);
    height: calc(100vh - 120px);
  }
}

@media (max-height: 600px) {
  .chat-window {
    bottom: 65px;
    max-height: calc(100vh - 100px);
    height: calc(100vh - 100px);
  }
}

/* 响应式调整：小屏幕宽度适配 */
@media (max-width: 768px) {
  .chat-window {
    width: calc(100vw - 40px);
    right: 20px;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.chat-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: white;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-avatar {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-info h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  background: #4ade80;
  border-radius: 50%;
  margin-right: 4px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.status-text {
  font-size: 12px;
  opacity: 0.9;
}

.chat-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: #f8f9fa;
  min-height: 0; /* 重要：确保 flex 子项能正确滚动 */
}

/* 自定义滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(102, 126, 234, 0.5);
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: rgba(102, 126, 234, 0.7);
}

.message-item {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-item .avatar {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message-content {
  max-width: 75%;
}

.message-item.user .message-content {
  display: flex;
  justify-content: flex-end;
}

.message-text {
  background: white;
  padding: 12px 16px;
  border-radius: 16px;
  border-top-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  line-height: 1.6;
  font-size: 14px;
  white-space: pre-wrap;
}

.message-item.user .message-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-top-left-radius: 16px;
  border-top-right-radius: 4px;
}

.typing-indicator {
  background: white;
  padding: 16px 20px;
  border-radius: 16px;
  border-top-left-radius: 4px;
  display: flex;
  gap: 6px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #999;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

.product-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-top: 12px;
}

.mini-product-card {
  background: white;
  border-radius: 12px;
  padding: 10px;
  display: flex;
  gap: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

.mini-product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.mini-product-card img {
  width: 50px;
  height: 50px;
  object-fit: contain;
  border-radius: 8px;
  background: #f5f5f5;
}

.mini-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.mini-name {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mini-price {
  font-size: 14px;
  font-weight: bold;
  color: #f56c6c;
}

.product-detail-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  margin-top: 12px;
  display: flex;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.detail-image {
  width: 120px;
  height: 120px;
  background: #f5f5f5;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.detail-image img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.detail-info {
  flex: 1;
}

.detail-info h5 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #333;
}

.detail-desc {
  margin: 0 0 10px 0;
  font-size: 13px;
  color: #666;
}

.detail-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;
}

.detail-price .current {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}

.detail-price .original {
  font-size: 13px;
  color: #999;
  text-decoration: line-through;
}

.detail-price .unit {
  font-size: 13px;
  color: #666;
}

.detail-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 12px;
}

.add-cart-btn {
  width: 100%;
}

.quick-questions {
  padding: 12px 16px;
  background: white;
  border-top: 1px solid #eee;
}

.quick-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
  display: block;
}

.quick-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.quick-buttons .el-button {
  border-radius: 20px;
}

.chat-input-area {
  padding: 16px;
  background: white;
  border-top: 1px solid #eee;
}

.chat-input-area :deep(.el-input__wrapper) {
  border-radius: 24px;
  padding: 8px 16px;
}
</style>
