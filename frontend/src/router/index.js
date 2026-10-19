import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import pinia from '@/stores'
import { useUserStore } from '@/stores/user'
import MainLayout from '@/layout/MainLayout.vue'
import { menuRoutes } from './menu'

const APP_TITLE = import.meta.env.VITE_APP_TITLE || 'TeamPulse'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册', public: true }
  },
  {
    // 布局页只负责外壳，页面全部作为子路由挂在下面
    path: '/',
    name: 'Root',
    component: MainLayout,
    redirect: '/dashboard',
    children: menuRoutes
  },
  // 兜底：未知路径回首页（也顺带兜住 history 模式下手敲的错路径）
  { path: '/:pathMatch(.*)*', name: 'NotFound', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach(async (to) => {
  document.title = to.meta?.title ? `${to.meta.title} - ${APP_TITLE}` : APP_TITLE

  // 显式传入 pinia 实例：不依赖 main.js 里 app.use(pinia) 的先后顺序
  const userStore = useUserStore(pinia)

  // 1. 未登录：公开页放行，其余带 redirect 去登录
  if (!userStore.token) {
    return to.meta?.public ? true : { path: '/login', query: { redirect: to.fullPath } }
  }

  // 2. 已登录：不再回登录 / 注册页（否则还能打开注册页写库）
  if (['/login', '/register'].includes(to.path)) return { path: '/' }

  // 3. 刷新后 store 是空的（只持久化了 token），补一次 /auth/me 恢复用户与角色
  if (!userStore.userInfo) {
    try {
      await userStore.fetchMe()
    } catch {
      // 401 已被拦截器清 token 并 replace 到登录页，这里跟随即可
      if (!userStore.token) return { path: '/login', query: { redirect: to.fullPath } }
      // 后端未启动等非 401 错误：放行，下次导航会重试，避免白屏
      return true
    }
  }

  // 4. 角色不符：收敛回总览（前端只做入口收敛，真实权限在后端）
  if (to.meta?.roles?.length && !userStore.hasAnyRole(to.meta.roles)) {
    ElMessage.warning('无权限访问')
    return { path: '/dashboard' }
  }

  return true
})

export default router
