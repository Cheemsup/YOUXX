// LLM 配置 - 已迁移到后端，前端仅保留接口地址
export const LLM_CONFIG = {
  // 统一 Agent 接口（非流式，带 tool）
  agentChatUrl: '/api/llm/agent/chat',

  // 超时时间（毫秒）
  timeout: 120000
}

export function isLLMEnabled() {
  return true
}
