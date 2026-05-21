// 订单数据及其操作函数
const ORDERS_KEY = 'systemOrders'
const ORDERS_VERSION_KEY = 'systemOrdersVersion'
const CURRENT_VERSION = 3

// 获取不同日期的辅助函数
const getDaysAgo = (days) => {
  const date = new Date()
  date.setDate(date.getDate() - days)
  return date
}

const formatDate = (date = new Date()) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const formatDateTime = (date) => {
  return date.toISOString()
}

// 用户名到用户 ID 的映射（非管理员用户）
const userMapping = {
  'user': 'U001',
  'zhangsan': 'U003',
  'lisi': 'U004',
  'wangwu': 'U005',
  'zhaoliu': 'U006',
  'sunqi': 'U007',
  'zhouba': 'U008',
  'wujiu': 'U009',
  'shiyi': 'U010',
  'cheems': 'U011',
  'zhengshi': 'U012',
  'user001': 'U013',
  'user002': 'U014',
  'user003': 'U015'
}

const defaultOrders = []

const generateOrderId = () => {
  const timestamp = Date.now().toString(36)
  const random = Math.random().toString(36).substr(2, 4)
  return `ORD${timestamp}${random}`.toUpperCase()
}

export const getOrders = () => {
  const savedVersion = localStorage.getItem(ORDERS_VERSION_KEY)
  const ordersStr = localStorage.getItem(ORDERS_KEY)
  
  if (ordersStr && savedVersion === String(CURRENT_VERSION)) {
    return JSON.parse(ordersStr)
  }
  
  saveOrders(defaultOrders)
  localStorage.setItem(ORDERS_VERSION_KEY, String(CURRENT_VERSION))
  return defaultOrders
}

export const urgentOrder = (orderId) => {
  const orders = getOrders()
  const orderIndex = orders.findIndex(o => o.id === orderId)
  if (orderIndex > -1) {
    const order = orders[orderIndex]
    if (order.status === '待发货') {
      order.urgentCount = (order.urgentCount || 0) + 1
      order.lastUrgentTime = new Date().toISOString()
      saveOrders(orders)
      return { success: true, message: '催单成功' }
    }
    return { success: false, message: '该订单状态不支持催单' }
  }
  return { success: false, message: '订单不存在' }
}

export const saveOrders = (orders) => {
  localStorage.setItem(ORDERS_KEY, JSON.stringify(orders))
}

export const getOrdersByUsername = (username) => {
  const orders = getOrders()
  return orders.filter(o => o.username === username)
}

export const createOrder = (cartItems, totalAmount, username) => {
  const orders = getOrders()
  
  // 根据用户名查找用户 ID
  const customerId = userMapping[username] || 'U999'
  
  const newOrder = {
    id: generateOrderId(),
    username: username,
    customerId: customerId,
    customer: username,
    items: cartItems.map(item => ({
      productId: item.id,
      name: item.name,
      price: item.price,
      discount: item.discount,
      quantity: item.quantity,
      unit: item.unit,
      subtotal: (item.price * item.discount * item.quantity).toFixed(2)
    })),
    totalAmount: totalAmount.toFixed(2),
    amount: totalAmount,
    itemCount: cartItems.reduce((sum, item) => sum + item.quantity, 0),
    status: '待发货',
    date: formatDate(),
    createTime: new Date().toISOString(),
    urgentCount: 0,
    lastUrgentTime: null
  }
  
  orders.unshift(newOrder)
  saveOrders(orders)
  
  return newOrder
}

export const updateOrderStatus = (orderId, newStatus) => {
  const orders = getOrders()
  const orderIndex = orders.findIndex(o => o.id === orderId)
  if (orderIndex > -1) {
    orders[orderIndex].status = newStatus
    saveOrders(orders)
    return true
  }
  return false
}

export const deleteOrder = (orderId) => {
  const orders = getOrders()
  const filteredOrders = orders.filter(o => o.id !== orderId)
  saveOrders(filteredOrders)
  return filteredOrders.length < orders.length
}

export const getOrderById = (orderId) => {
  const orders = getOrders()
  return orders.find(o => o.id === orderId)
}
