import { LLM_CONFIG, isLLMEnabled } from '@/config/llmConfig.js'
import { parseLLMResponse } from '@/services/promptEngineer.js'

/**
 * 调用 Agent 接口（非流式，带 tool）。
 * 用户身份不再放进请求体，改由后端 JWT 拦截器从 token 解析写入 BaseContext，
 * 下单工具据此自行获取，避免用户信息发往大模型。
 * 会话记忆由后端按 sessionId 持久化在 Redis（含完整工具调用对），前端不再回传历史。
 * @param {string} userMessage 当前用户消息
 * @param {string} sessionId 会话 ID（前端生成），同一对话复用
 * @returns {Promise<{type:'text', content, cartItems?}>} cartItems 为加购清单（productId+quantity），仅当 Agent 调用 addToCart 时非空
 */
export async function getAgentResponse(userMessage, sessionId) {
  const token = localStorage.getItem('token')
  const response = await fetch(LLM_CONFIG.agentChatUrl, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { token } : {})
    },
    body: JSON.stringify({
      message: userMessage,
      sessionId
    })
  })

  if (!response.ok) {
    throw new Error(`请求失败: ${response.status}`)
  }

  const result = await response.json()
  if (result.code === 1) {
    const data = result.data || {}
    // Agent 只加购不下单：cartItems 为后端从工具结果解析出的加购清单，前端据此写入购物车
    return {
      type: 'text',
      content: data.content,
      cartItems: Array.isArray(data.cartItems) ? data.cartItems : []
    }
  } else {
    throw new Error(result.msg || 'AI助手暂时不可用')
  }
}

export { isLLMEnabled }
