import { createRouter, createWebHashHistory } from 'vue-router'
import Login from '../views/Login.vue'
import DashboardUser from '../views/DashboardUser.vue'

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
    path: '/dashboard-user',
    name: 'dashboard-user',
    component: DashboardUser,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = sessionStorage.getItem('token')

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token && !to.query.force) {
    // 用户端登录后统一进入用户仪表盘
    next('/dashboard-user')
  } else {
    next()
  }
})

export default router
