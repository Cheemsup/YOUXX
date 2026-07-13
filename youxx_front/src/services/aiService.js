import { LLM_CONFIG, isLLMEnabled } from '@/config/llmConfig.js'
import { parseLLMResponse } from '@/services/promptEngineer.js'

/**
 * 通过后端SSE流式接口调用大模型
 * @param {string} userMessage - 用户消息
 * @param {Array} conversationHistory - 对话历史 [{role, content}]
 * @param {Function} onToken - 每收到一个token的回调
 * @returns {Promise<{type, content}>} - 最终响应
 */
export async function getAIStreamResponse(userMessage, conversationHistory = [], onToken = null) {
  const response = await fetch(LLM_CONFIG.streamUrl, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      message: userMessage,
      history: conversationHistory
    })
  })

  if (!response.ok) {
    throw new Error(`请求失败: ${response.status}`)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let fullText = ''
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })

    // 解析SSE事件
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''

    let currentEvent = ''
    for (const line of lines) {
      if (line.startsWith('event:')) {
        currentEvent = line.slice(6).trim()
      } else if (line.startsWith('data:')) {
        const data = line.slice(5).trim()
        if (currentEvent === 'token') {
          fullText += data
          if (onToken) {
            onToken(data, fullText)
          }
        } else if (currentEvent === 'done') {
          // 流结束
        } else if (currentEvent === 'error') {
          throw new Error(data)
        }
      }
    }
  }

  return parseLLMResponse(fullText)
}

/**
 * 通过后端普通接口调用大模型（非流式，备用）
 */
export async function getAIResponse(userMessage, conversationHistory = []) {
  const response = await fetch(LLM_CONFIG.chatUrl, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      message: userMessage,
      history: conversationHistory
    })
  })

  if (!response.ok) {
    throw new Error(`请求失败: ${response.status}`)
  }

  const result = await response.json()
  if (result.code === 1) {
    return parseLLMResponse(result.data)
  } else {
    throw new Error(result.msg || 'AI助手暂时不可用')
  }
}

export { isLLMEnabled }
