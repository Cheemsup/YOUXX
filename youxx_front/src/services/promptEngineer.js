import { getProducts, categories } from '@/data/products.js'

export function buildSystemPrompt() {
  const products = getProducts()
  const categoriesInfo = categories.map(cat => 
    `- ${cat.name} (id: ${cat.id})`
  ).join('\n')

  const productsInfo = products.map(p => {
    const discountInfo = p.discount < 1 
      ? `, 折扣: ${Math.round(p.discount * 10)}折 (原价¥${p.price.toFixed(2)})`
      : ''
    return `- ${p.name} [${p.id}]
    分类: ${categories.find(c => c.id === p.category)?.name || p.category}
    价格: ¥${(p.price * p.discount).toFixed(2)}/${p.unit}${discountInfo}
    库存: ${p.stock}
    描述: ${p.description}
    标签: ${p.tags.join(', ')}
    热销: ${p.isHot ? '是' : '否'}`
  }).join('\n\n')

  const hotProductNames = products.filter(p => p.isHot).map(p => p.name).join('、')

  return `你是一个专业的智能商城导购助手。请根据以下知识库信息回答用户问题。

【商城基本信息】
- 商城名称：智能商城Demo
- 服务时间：全天候在线
- 特色功能：商品查询、价格咨询、智能推荐

【商品分类】
${categoriesInfo}

【商品列表】
${productsInfo}

【热销商品】
${hotProductNames}

【回复要求】
1. 回答要友好、专业、口语化
2. 涉及商品时，引用准确的价格和库存信息
3. 如果用户询问的商品不存在，可以推荐相关或热销商品
4. 回复要简洁明了，不要过度冗长
5. 可以适当使用emoji增加亲和力
6. 如果用户询问价格，务必告知当前的折扣价（如果有折扣）

【输出格式】
你的回答应该是纯文本，自然流畅的对话形式。`
}

export function buildUserPromptWithContext(userMessage, conversationHistory = []) {
  let context = ''
  
  if (conversationHistory.length > 0) {
    context = '【对话历史】\n'
    conversationHistory.forEach(msg => {
      context += `${msg.role === 'user' ? '用户' : '助手'}: ${msg.content}\n`
    })
    context += '\n'
  }

  return `${context}【当前问题】\n用户: ${userMessage}`
}

export function parseLLMResponse(llmText) {
  const products = getProducts()
  let productList = null
  let productDetail = null

  const productMatches = llmText.match(/\[P\d+\]/g)
  if (productMatches && productMatches.length > 0) {
    const matchedProductIds = [...new Set(productMatches.map(m => m.replace(/\[|\]/g, '')))]
    productList = products.filter(p => matchedProductIds.includes(p.id))
  }

  if (productList && productList.length === 1) {
    productDetail = productList[0]
    productList = null
  }

  return {
    type: productDetail ? 'product_detail' : (productList ? 'product_list' : 'text'),
    content: llmText,
    products: productList,
    product: productDetail
  }
}
