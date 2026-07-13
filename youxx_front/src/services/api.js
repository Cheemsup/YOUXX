import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：附加 Token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.token = token
  }
  return config
})

// 响应拦截器：统一错误处理
request.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      localStorage.removeItem('userRole')
      window.location.hash = '#/login'
    }
    return Promise.reject(error)
  }
)

// ==================== 认证 ====================
export function loginApi(username, password) {
  return request.post('/auth/login', { username, password })
}

export function registerApi(username, password, phone) {
  return request.post('/auth/register', { username, password, phone })
}

export function getInfoApi() {
  return request.get('/auth/info')
}

// ==================== 用户管理 ====================
export function listUsersApi(params) {
  return request.get('/user/list', { params })
}

export function getUserApi(id) {
  return request.get(`/user/${id}`)
}

export function addUserApi(data) {
  return request.post('/user', data)
}

export function updateUserApi(id, data) {
  return request.put(`/user/${id}`, data)
}

export function updateUserStatusApi(id, status) {
  return request.put(`/user/${id}/status`, { status })
}

export function deleteUserApi(id) {
  return request.delete(`/user/${id}`)
}

export function getProfileApi() {
  return request.get('/user/profile')
}

export function updateProfileApi(data) {
  return request.put('/user/profile', data)
}

export function updatePasswordApi(oldPassword, newPassword) {
  return request.put('/user/password', { oldPassword, newPassword })
}

export function listAddressesApi() {
  return request.get('/user/address')
}

export function addAddressApi(data) {
  return request.post('/user/address', data)
}

export function updateAddressApi(id, data) {
  return request.put(`/user/address/${id}`, data)
}

export function deleteAddressApi(id) {
  return request.delete(`/user/address/${id}`)
}

export function setDefaultAddressApi(id) {
  return request.put(`/user/address/${id}/default`)
}

// ==================== 商品管理 ====================
export function listProductsApi(params) {
  return request.get('/product/list', { params })
}

export function getProductApi(id) {
  return request.get(`/product/${id}`)
}

export function listHotProductsApi() {
  return request.get('/product/hot')
}

export function listCategoriesApi() {
  return request.get('/product/category/list')
}

export function addProductApi(data) {
  return request.post('/product', data)
}

export function updateProductApi(id, data) {
  return request.put(`/product/${id}`, data)
}

export function updateProductStatusApi(id, status) {
  return request.put(`/product/${id}/status`, { status })
}

export function updateBatchProductStatusApi(ids, status) {
  return request.put('/product/batch/status', { ids, status })
}

export function updateProductDiscountApi(id, discount) {
  return request.put(`/product/${id}/discount`, { discount })
}

export function deleteProductApi(id) {
  return request.delete(`/product/${id}`)
}

// ==================== 订单管理 ====================
export function listOrdersApi(params) {
  return request.get('/order/list', { params })
}

export function listMyOrdersApi(status) {
  return request.get('/order/my', { params: status ? { status } : {} })
}

export function getOrderApi(id) {
  return request.get(`/order/${id}`)
}

export function createOrderApi(order, items) {
  return request.post('/order', { order, items })
}

export function updateOrderStatusApi(id, status) {
  return request.put(`/order/${id}/status`, { status })
}

export function urgeOrderApi(id) {
  return request.post(`/order/${id}/urgent`)
}

export function deleteOrderApi(id) {
  return request.delete(`/order/${id}`)
}

// ==================== 消息管理 ====================
export function getConversationsApi() {
  return request.get('/message/conversations')
}

export function getConversationMessagesApi(conversationId) {
  return request.get(`/message/conversation/${conversationId}`)
}

export function getUnreadCountApi() {
  return request.get('/message/unread-count')
}

export function sendMessageApi(conversationId, content) {
  return request.post('/message/send', { conversationId, content })
}

export function markConversationReadApi(conversationId) {
  return request.put(`/message/conversation/${conversationId}/read`)
}

export function markAllReadApi() {
  return request.put('/message/read-all')
}

export default request