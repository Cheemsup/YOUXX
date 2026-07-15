const USERS_KEY = 'systemUsers'
const USERS_VERSION_KEY = 'systemUsersVersion'
const CURRENT_VERSION = 5

const defaultUsers = [
  { id: 'A001', username: 'admin', password: 'Admin123', phone: '13800138000', email: '', role: '管理员', status: '正常' },
  { id: 'A002', username: 'admin2', password: 'Admin123', phone: '13800138001', email: '', role: '管理员', status: '正常' },
  { id: 'A003', username: 'admin3', password: 'Admin123', phone: '13800138002', email: '', role: '管理员', status: '禁用' },
  { id: 'U001', username: 'user', password: 'User123', phone: '13900139000', email: '', role: '用户', status: '正常' },
  { id: 'U002', username: 'test', password: 'Test123', phone: '13700137000', email: '', role: '用户', status: '禁用' },
  { id: 'U003', username: 'zhangsan', password: 'Zhangsan123', phone: '13600136000', email: '', role: '用户', status: '正常' },
  { id: 'U004', username: 'lisi', password: 'Lisi123', phone: '13500135000', email: '', role: '用户', status: '正常' },
  { id: 'U005', username: 'wangwu', password: 'Wangwu123', phone: '13400134000', email: '', role: '用户', status: '正常' },
  { id: 'U006', username: 'zhaoliu', password: 'Zhaoliu123', phone: '13300133000', email: '', role: '用户', status: '正常' },
  { id: 'U007', username: 'sunqi', password: 'Sunqi123', phone: '13200132000', email: '', role: '用户', status: '禁用' },
  { id: 'U008', username: 'zhouba', password: 'Zhouba123', phone: '13100131000', email: '', role: '用户', status: '正常' },
  { id: 'U009', username: 'wujiu', password: 'Wujiu123', phone: '13000130000', email: '', role: '用户', status: '正常' },
  { id: 'U010', username: 'shiyi', password: 'Shiyi123', phone: '12900129000', email: '', role: '用户', status: '正常' },
  { id: 'U011', username: 'cheems', password: '114514', phone: '12800128000', email: '', role: '用户', status: '正常' },
  { id: 'U012', username: 'zhengshi', password: 'Zhengshi123', phone: '12700127000', email: '', role: '用户', status: '正常' },
  { id: 'U013', username: 'user001', password: 'User001123', phone: '12600126000', email: '', role: '用户', status: '正常' },
  { id: 'U014', username: 'user002', password: 'User002123', phone: '12500125000', email: '', role: '用户', status: '正常' },
  { id: 'U015', username: 'user003', password: 'User003123', phone: '12400124000', email: '', role: '用户', status: '正常' }
]

export const getUsers = () => {
  const savedVersion = localStorage.getItem(USERS_VERSION_KEY)
  const usersStr = localStorage.getItem(USERS_KEY)
  
  if (usersStr && savedVersion === String(CURRENT_VERSION)) {
    return JSON.parse(usersStr)
  }
  
  saveUsers(defaultUsers)
  localStorage.setItem(USERS_VERSION_KEY, String(CURRENT_VERSION))
  return defaultUsers
}

export const saveUsers = (users) => {
  localStorage.setItem(USERS_KEY, JSON.stringify(users))
}

export const getUserByUsername = (username) => {
  const users = getUsers()
  return users.find(u => u.username === username)
}

export const validateUser = (username, password, role) => {
  const user = getUserByUsername(username)
  const roleMap = {
    'admin': '管理员',
    'user': '用户'
  }
  const targetRole = roleMap[role] || role
  if (user && user.password === password && user.role === targetRole && user.status === '正常') {
    return user
  }
  return null
}

export const generateUserId = (role = '用户') => {
  const users = getUsers()
  const isAdmin = role === '管理员'
  const prefix = isAdmin ? 'A' : 'U'
  const maxId = users.reduce((max, user) => {
    if ((isAdmin && user.role === '管理员') || (!isAdmin && user.role === '用户')) {
      const num = parseInt(user.id.substring(1))
      return num > max ? num : max
    }
    return max
  }, 0)
  return `${prefix}${String(maxId + 1).padStart(3, '0')}`
}

export const registerUser = (username, password, phone, email = '') => {
  if (getUserByUsername(username)) {
    return { success: false, message: '用户名已存在' }
  }
  const newUser = {
    id: generateUserId('用户'),
    username,
    password,
    phone,
    email,
    role: '用户',
    status: '正常'
  }
  const users = getUsers()
  users.push(newUser)
  saveUsers(users)
  return { success: true, message: '注册成功', user: newUser }
}

export const addUser = (user) => {
  const users = getUsers()
  if (getUserByUsername(user.username)) {
    return { success: false, message: '用户名已存在' }
  }
  users.push(user)
  saveUsers(users)
  return { success: true, message: '用户添加成功' }
}

export const updateUser = (userId, updatedUser) => {
  const users = getUsers()
  const index = users.findIndex(u => u.id === userId)
  if (index > -1) {
    users[index] = { ...users[index], ...updatedUser }
    saveUsers(users)
    return { success: true, message: '用户更新成功' }
  }
  return { success: false, message: '用户不存在' }
}

export const deleteUser = (userId) => {
  const users = getUsers()
  const filteredUsers = users.filter(u => u.id !== userId)
  if (filteredUsers.length < users.length) {
    saveUsers(filteredUsers)
    return { success: true, message: '用户删除成功' }
  }
  return { success: false, message: '用户不存在' }
}


