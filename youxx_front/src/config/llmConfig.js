// LLM 配置 - 已迁移到后端，前端仅保留接口地址
export const LLM_CONFIG = {
  // 后端 LLM 接口地址
  chatUrl: '/api/llm/chat',
  streamUrl: '/api/llm/chat/stream',

  // 超时时间（毫秒）
  timeout: 120000
}

export function isLLMEnabled() {
  return true
}
