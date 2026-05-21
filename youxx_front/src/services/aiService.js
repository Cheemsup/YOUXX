import { getProducts, categories } from '@/data/products.js'
import { LLM_CONFIG, isLLMEnabled, getAPIType } from '@/config/llmConfig.js'
import { buildSystemPrompt, buildUserPromptWithContext, parseLLMResponse } from '@/services/promptEngineer.js'

const knowledgeBase = {
  get products() { return getProducts() },
  categories,
  
  getProductByName(name) {
    const keyword = name.toLowerCase()
    return getProducts().find(p => 
      p.name.toLowerCase().includes(keyword) ||
      p.tags.some(tag => tag.toLowerCase().includes(keyword))
    )
  },
  
  getProductsByCategory(categoryId) {
    return getProducts().filter(p => p.category === categoryId)
  },
  
  getHotProducts() {
    return getProducts().filter(p => p.isHot)
  },
  
  searchProducts(keyword) {
    const lowerKeyword = keyword.toLowerCase()
    return getProducts().filter(p =>
      p.name.toLowerCase().includes(lowerKeyword) ||
      p.description.toLowerCase().includes(lowerKeyword) ||
      p.tags.some(tag => tag.toLowerCase().includes(lowerKeyword))
    )
  }
}

const responseTemplates = {
  greetings: [
    '你好！我是智能导购助手，有什么可以帮您的吗？',
    '您好！欢迎来到智能商城，我可以帮您查询商品信息、推荐好物哦~',
    '嗨！很高兴为您服务，请问您想了解什么商品呢？'
  ],
  
  productNotFound: [
    '抱歉，我没有找到您提到的商品。您可以试试其他关键词，或者告诉我您想要什么类型的商品？',
    '没有找到相关商品呢，要不换个词试试？或者告诉我您的需求，我来帮您推荐~',
    '不好意思，暂时没有找到这个商品。您可以浏览一下我们的热销商品哦！'
  ],
  
  recommendation: [
    '根据您的需求，我为您推荐以下商品：',
    '我觉得这些商品可能适合您：',
    '为您精选了这些好物：'
  ],
  
  hotProducts: [
    '这是我们当前的热销商品，都很受欢迎呢：',
    '大家都在买这些，推荐给您：',
    '热销榜单来啦：'
  ],
  
  categoryInfo: [
    '这是{category}分类下的所有商品：',
    '我们的{category}有这些商品：',
    '{category}类商品一览：'
  ],
  
  priceQuery: [
    '{product}的价格是¥{price}，当前{discount}哦！',
    '{product}现在卖¥{price}，{discountInfo}',
    '{product}售价¥{price}{discountText}'
  ],
  
  thanks: [
    '不客气！还有其他问题随时问我~',
    '很高兴能帮到您！',
    '随时为您服务！'
  ],
  
  default: [
    '我理解您的问题了，让我帮您看看...',
    '好的，我来为您查询一下~',
    '明白了，这就为您查找相关信息！'
  ]
}

const intentRecognizer = {
  isGreeting(message) {
    const greetings = ['你好', '您好', '嗨', 'hello', 'hi', '早上好', '下午好', '晚上好']
    return greetings.some(g => message.toLowerCase().includes(g))
  },
  
  isThanks(message) {
    const thanks = ['谢谢', '感谢', '多谢', 'thanks', 'thank you']
    return thanks.some(t => message.toLowerCase().includes(t))
  },
  
  isHotProductQuery(message) {
    const keywords = ['热销', '热门', '推荐', '畅销', '爆款', '热卖']
    return keywords.some(k => message.includes(k))
  },
  
  isCategoryQuery(message) {
    for (const cat of categories) {
      if (message.includes(cat.name)) {
        return cat
      }
    }
    return null
  },
  
  isProductQuery(message) {
    for (const product of getProducts()) {
      if (message.includes(product.name)) {
        return product
      }
    }
    return null
  },
  
  isPriceQuery(message) {
    const keywords = ['多少钱', '价格', '贵不贵', '便宜', '售价']
    return keywords.some(k => message.includes(k))
  },
  
  isSearchQuery(message) {
    const keywords = ['找', '搜索', '有没有', '有什么', '看看']
    return keywords.some(k => message.includes(k))
  }
}

function formatProductInfo(product) {
  const discountText = product.discount < 1 
    ? `（原价¥${product.price.toFixed(2)}，现价${Math.round(product.discount * 10)}折）`
    : ''
  return `📦 ${product.name}\n   💰 ¥${(product.price * product.discount).toFixed(2)}/${product.unit}${discountText}\n   📝 ${product.description}\n   🏷️ ${product.tags.join('、')}`
}

function formatProductList(productList) {
  return productList.map((p, i) => `${i + 1}. ${p.name} - ¥${(p.price * p.discount).toFixed(2)}`).join('\n')
}

async function callMinimaxAPI(userMessage, conversationHistory = []) {
  try {
    const systemPrompt = buildSystemPrompt()
    const userPrompt = buildUserPromptWithContext(userMessage, conversationHistory)

    // Minimax API 需要将 Group ID 作为 URL 参数
    const groupId = LLM_CONFIG.groupId
    let apiUrl = LLM_CONFIG.apiUrl
    if (groupId && !apiUrl.includes('GroupId')) {
      apiUrl = apiUrl + (apiUrl.includes('?') ? '&' : '?') + `GroupId=${groupId}`
    }

    console.log('调用 Minimax API:', apiUrl)

    // 构建 Minimax 格式的消息
    const messages = [
      { role: 'system', content: systemPrompt, name: 'MM智能助理' },
      ...conversationHistory.map(msg => ({
        role: msg.role === 'assistant' ? 'assistant' : 'user',
        content: msg.content,
        name: msg.role === 'assistant' ? 'MM智能助理' : '用户'
      })),
      { role: 'user', content: userPrompt, name: '用户' }
    ]

    const requestBody = {
      model: LLM_CONFIG.model,
      messages: messages
    }

    // 只有设置了 temperature 和 max_tokens 才添加
    if (LLM_CONFIG.temperature !== undefined) {
      requestBody.temperature = LLM_CONFIG.temperature
    }
    if (LLM_CONFIG.maxTokens !== undefined) {
      requestBody.max_tokens = LLM_CONFIG.maxTokens
    }

    console.log('请求体:', JSON.stringify(requestBody, null, 2))

    const response = await fetch(apiUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${LLM_CONFIG.apiKey}`
      },
      body: JSON.stringify(requestBody),
      signal: AbortSignal.timeout(LLM_CONFIG.timeout)
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      console.error('API 错误响应:', errorData)
      throw new Error(`API请求失败: ${response.status} - ${errorData.message || errorData.base_resp?.message || '未知错误'}`)
    }

    const data = await response.json()
    console.log('API 响应:', data)

    // Minimax 返回格式处理
    let llmText = ''
    if (data.choices && data.choices[0]?.message?.content) {
      llmText = data.choices[0].message.content
    } else if (data.base_resp?.data?.choices?.[0]?.message?.content) {
      llmText = data.base_resp.data.choices[0].message.content
    } else if (data.reply) {
      llmText = data.reply
    }

    if (!llmText) {
      console.warn('无法解析 API 响应:', data)
      throw new Error('API 返回数据格式异常')
    }

    return parseLLMResponse(llmText)
  } catch (error) {
    console.error('Minimax API调用失败:', error)
    throw error
  }
}

async function callOpenAIAPI(userMessage, conversationHistory = []) {
  try {
    const systemPrompt = buildSystemPrompt()
    const userPrompt = buildUserPromptWithContext(userMessage, conversationHistory)

    const response = await fetch(LLM_CONFIG.apiUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${LLM_CONFIG.apiKey}`
      },
      body: JSON.stringify({
        model: LLM_CONFIG.model,
        messages: [
          { role: 'system', content: systemPrompt },
          ...conversationHistory,
          { role: 'user', content: userPrompt }
        ],
        temperature: LLM_CONFIG.temperature,
        max_tokens: LLM_CONFIG.maxTokens
      }),
      signal: AbortSignal.timeout(LLM_CONFIG.timeout)
    })

    if (!response.ok) {
      throw new Error(`API请求失败: ${response.status}`)
    }

    const data = await response.json()
    const llmText = data.choices[0]?.message?.content || ''

    return parseLLMResponse(llmText)
  } catch (error) {
    console.error('OpenAI API调用失败:', error)
    throw error
  }
}

async function callLLMAPI(userMessage, conversationHistory = []) {
  const apiType = getAPIType()

  if (apiType === 'minimax') {
    return await callMinimaxAPI(userMessage, conversationHistory)
  } else {
    return await callOpenAIAPI(userMessage, conversationHistory)
  }
}

async function getRuleBasedResponse(userMessage) {
  await new Promise(resolve => setTimeout(resolve, 500 + Math.random() * 500))
  
  const message = userMessage.trim()
  
  if (intentRecognizer.isThanks(message)) {
    return {
      type: 'text',
      content: responseTemplates.thanks[Math.floor(Math.random() * responseTemplates.thanks.length)]
    }
  }
  
  if (intentRecognizer.isGreeting(message)) {
    return {
      type: 'text',
      content: responseTemplates.greetings[Math.floor(Math.random() * responseTemplates.greetings.length)]
    }
  }
  
  if (intentRecognizer.isHotProductQuery(message)) {
    const hotProducts = knowledgeBase.getHotProducts()
    return {
      type: 'product_list',
      content: responseTemplates.hotProducts[Math.floor(Math.random() * responseTemplates.hotProducts.length)],
      products: hotProducts
    }
  }
  
  const category = intentRecognizer.isCategoryQuery(message)
  if (category) {
    const categoryProducts = knowledgeBase.getProductsByCategory(category.id)
    return {
      type: 'product_list',
      content: responseTemplates.categoryInfo[Math.floor(Math.random() * responseTemplates.categoryInfo.length)].replace('{category}', category.name),
      products: categoryProducts
    }
  }
  
  const product = intentRecognizer.isProductQuery(message)
  if (product) {
    if (intentRecognizer.isPriceQuery(message)) {
      const discountInfo = product.discount < 1 
        ? `现在有${Math.round(product.discount * 10)}折优惠哦！`
        : '价格很实惠呢~'
      return {
        type: 'product_detail',
        content: `${product.name}的价格是¥${(product.price * product.discount).toFixed(2)}/${product.unit}。${discountInfo}`,
        product: product
      }
    }
    return {
      type: 'product_detail',
      content: '这是您询问的商品信息：',
      product: product
    }
  }
  
  if (intentRecognizer.isSearchQuery(message)) {
    const searchResults = knowledgeBase.searchProducts(message)
    if (searchResults.length > 0) {
      return {
        type: 'product_list',
        content: '为您找到以下商品：',
        products: searchResults
      }
    } else {
      return {
        type: 'text',
        content: responseTemplates.productNotFound[Math.floor(Math.random() * responseTemplates.productNotFound.length)]
      }
    }
  }
  
  const anyProductMatch = knowledgeBase.searchProducts(message)
  if (anyProductMatch.length > 0) {
    return {
      type: 'product_list',
      content: responseTemplates.recommendation[Math.floor(Math.random() * responseTemplates.recommendation.length)],
      products: anyProductMatch.slice(0, 5)
    }
  }
  
  return {
    type: 'text',
    content: '我可以帮您查询商品信息、查看热销商品、按分类浏览商品。试试问我"有什么推荐"、"饮料有哪些"或者"可乐多少钱"吧！'
  }
}

export async function getAIResponse(userMessage, conversationHistory = []) {
  if (isLLMEnabled()) {
    try {
      return await callLLMAPI(userMessage, conversationHistory)
    } catch (error) {
      console.warn('LLM模式失败，回退到规则模式:', error)
      return await getRuleBasedResponse(userMessage)
    }
  }
  
  return await getRuleBasedResponse(userMessage)
}

export function askAboutProduct(product) {
  const responses = [
    `好的，让我为您介绍一下${product.name}！`,
    `没问题，这就为您展示${product.name}的详细信息~`,
    `收到！来看看${product.name}的介绍吧！`
  ]
  
  return {
    type: 'product_detail',
    content: responses[Math.floor(Math.random() * responses.length)],
    product: product
  }
}

export { formatProductInfo, formatProductList, isLLMEnabled }
