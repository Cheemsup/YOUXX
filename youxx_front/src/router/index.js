import { createRouter, createWebHashHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import DashboardUser from '../views/DashboardUser.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    //component: HomeView
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
  },
  {
    path: '/dashboard-user',
    name: 'dashboard-user',
    component: DashboardUser,
    meta: { requiresAuth: true }
  },
  {
    path: '/about',
    name: 'about',
    component: () => import('../views/AboutView.vue')
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole')

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token && !to.query.force) {
    if (userRole === 'ADMIN') {
      next('/dashboard')
    } else {
      next('/dashboard-user')
    }
  } else {
    next()
  }
})

export default router
