import { createRouter, createWebHashHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'login',
    component: Login
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: Dashboard,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = sessionStorage.getItem('token')

  // 需要鉴权的路由：无 token 则跳登录页
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token && !to.query.force) {
    // 已登录访问登录页：直接进入管理仪表盘
    next('/dashboard')
  } else {
    next()
  }
})

export default router
