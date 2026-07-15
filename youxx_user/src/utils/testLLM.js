/**
 * LLM API 测试工具
 * 用于验证 Minimax 或其他 LLM API 配置是否正确
 */

import { LLM_CONFIG, isLLMEnabled, getAPIType } from '@/config/llmConfig.js'

/**
 * 测试 LLM API 连接
 * @returns {Promise<{success: boolean, message: string, data?: any}>}
 */
export async function testLLMConnection() {
  console.log('=== LLM API 连接测试 ===')

  // 1. 检查配置
  if (!isLLMEnabled()) {
    return {
      success: false,
      message: 'LLM 未启用或未配置 API Key，请检查 llmConfig.js 配置'
    }
  }

  // 检查 Group ID（Minimax 必需）
  const apiType = getAPIType()
  if (apiType === 'minimax' && !LLM_CONFIG.groupId) {
    return {
      success: false,
      message: '⚠️ Minimax 需要配置 Group ID！请参考 MINIMAX_CONFIG_GUIDE.md 获取'
    }
  }

  console.log(`API 类型: ${apiType}`)
  console.log(`API URL: ${LLM_CONFIG.apiUrl}`)
  console.log(`模型: ${LLM_CONFIG.model}`)
  if (apiType === 'minimax') {
    console.log(`Group ID: ${LLM_CONFIG.groupId ? '已配置' : '未配置'}`)
  }

  try {
    let response

    if (apiType === 'minimax') {
      response = await testMinimaxAPI()
    } else {
      response = await testOpenAIAPI()
    }

    return response
  } catch (error) {
    console.error('测试失败:', error)
    return {
      success: false,
      message: `连接测试失败: ${error.message}`
    }
  }
}

async function testMinimaxAPI() {
  const testMessage = '你好，请简单回复"API连接成功"'

  // Minimax API 需要将 Group ID 作为 URL 参数
  const groupId = LLM_CONFIG.groupId
  let apiUrl = LLM_CONFIG.apiUrl
  if (groupId && !apiUrl.includes('GroupId')) {
    apiUrl = apiUrl + (apiUrl.includes('?') ? '&' : '?') + `GroupId=${groupId}`
  }

  const messages = [
    { role: 'system', content: '你是一个 helpful 的助手', name: 'MM智能助理' },
    { role: 'user', content: testMessage, name: '用户' }
  ]

  console.log('正在发送测试请求到:', apiUrl)

  const response = await fetch(apiUrl, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${LLM_CONFIG.apiKey}`
    },
    body: JSON.stringify({
      model: LLM_CONFIG.model,
      messages: messages,
      temperature: 0.7,
      max_tokens: 100
    })
  })

  if (!response.ok) {
    const errorText = await response.text()
    let errorMsg = `HTTP ${response.status}`
    try {
      const errorData = JSON.parse(errorText)
      errorMsg += `: ${errorData.message || errorData.error || errorText}`
    } catch {
      errorMsg += `: ${errorText}`
    }
    return {
      success: false,
      message: errorMsg
    }
  }

  const data = await response.json()
  console.log('API 响应:', data)

  // 解析 Minimax 响应
  const content = data.choices?.[0]?.message?.content
    || data.base_resp?.data?.choices?.[0]?.message?.content
    || '无内容'

  return {
    success: true,
    message: `连接成功！模型响应: "${content.substring(0, 50)}..."`,
    data: {
      response: content,
      model: data.model || LLM_CONFIG.model,
      usage: data.usage || data.base_resp?.usage
    }
  }
}

async function testOpenAIAPI() {
  const testMessage = '你好，请简单回复"API连接成功"'

  console.log('正在发送测试请求...')

  const response = await fetch(LLM_CONFIG.apiUrl, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${LLM_CONFIG.apiKey}`
    },
    body: JSON.stringify({
      model: LLM_CONFIG.model,
      messages: [
        { role: 'system', content: '你是一个 helpful 的助手' },
        { role: 'user', content: testMessage }
      ],
      temperature: 0.7,
      max_tokens: 100
    })
  })

  if (!response.ok) {
    const errorText = await response.text()
    return {
      success: false,
      message: `HTTP ${response.status}: ${errorText}`
    }
  }

  const data = await response.json()
  console.log('API 响应:', data)

  const content = data.choices?.[0]?.message?.content || '无内容'

  return {
    success: true,
    message: `连接成功！模型响应: "${content.substring(0, 50)}..."`,
    data: {
      response: content,
      model: data.model,
      usage: data.usage
    }
  }
}

/**
 * 在控制台运行测试（用于开发调试）
 */
export function runTestInConsole() {
  testLLMConnection().then(result => {
    if (result.success) {
      console.log('✅', result.message)
      console.log('详细数据:', result.data)
    } else {
      console.error('❌', result.message)
    }
  })
}

export default testLLMConnection
