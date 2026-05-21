// Minimax API 配置
export const LLM_CONFIG = {
  // 是否启用 LLM（需要填写 apiKey 后才生效）
  enabled: true,

  // Minimax API Key（必填）
  apiKey: 'sk-cp-PLFrECVGzujOXyxJ40ZGpIDZ-izAZ0ut68DPTep4-uJEm1xNkfFeN9X4m5XlbdefulJMXPNzfnvUsa6oHIBKrNS5oYl1_czfZEfQ5EsLZgIjjgBkGL0xo0k',

  // Minimax Group ID（必填）
  groupId: '2038083649219338792',

  // API 地址（通常不需要修改）
  apiUrl: '/api/minimax/v1/text/chatcompletion_v2',

  // 模型名称
  // Token Plan 用户请使用：
  //   - MiniMax-M2.7：标准版（推荐）
  // 按量计费用户可选：
  //   - abab6.5s-chat：快速模型
  //   - abab6.5-chat：标准模型
  //   - MiniMax-Text-01：通用大模型
  model: 'MiniMax-M2.7',

  // 温度参数：0-1 之间，值越大回答越随机
  temperature: 0.7,

  // 最大 token 数
  maxTokens: 1000,

  // 超时时间（毫秒）
  timeout: 30000
}

// OpenAI 兼容配置（备用）
export const OPENAI_CONFIG = {
  enabled: true,
  apiKey: '',
  apiUrl: 'https://api.openai.com/v1/chat/completions',
  model: 'gpt-3.5-turbo',
  temperature: 0.7,
  maxTokens: 1000,
  timeout: 30000
}

export function setLLMConfig(config) {
  Object.assign(LLM_CONFIG, config)
}

export function isLLMEnabled() {
  return LLM_CONFIG.enabled && LLM_CONFIG.apiKey
}

// 获取当前使用的 API 类型
export function getAPIType() {
  if (LLM_CONFIG.apiUrl.includes('minimax')) {
    return 'minimax'
  }
  return 'openai'
}
