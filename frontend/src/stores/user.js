import { defineStore } from 'pinia'
import { login as loginApi, getMe } from '@/api/auth'

/** localStorage 中 token 的键名，与 request.js 的请求拦截器共用同一常量值 */
export const TOKEN_KEY = 'token'

/** 并发导航时只发一次 /auth/me（守卫在多次快速切换路由时可能重入） */
let meRequest = null

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    userInfo: null,
    // 角色码：STUDENT / ASSISTANT / ADMIN（后端只会返回这三种）
    roles: []
  }),

  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    // realName / email 可能整个键不存在（后端 non_null 序列化），必须可选链
    displayName: (state) => state.userInfo?.realName || state.userInfo?.username || '未登录',
    hasAnyRole: (state) => (codes) => {
      const need = Array.isArray(codes) ? codes : [codes]
      return need.length === 0 || need.some((code) => state.roles.includes(code))
    }
  },

  actions: {
    /** 登录：持久化 token，并用响应里的 userInfo 直接填充，省掉一次 /auth/me */
    async login(payload) {
      const data = await loginApi(payload)
      this.token = data.token
      localStorage.setItem(TOKEN_KEY, data.token)
      this.userInfo = data.userInfo || null
      this.roles = data.userInfo?.roles || []
      return this.userInfo
    },

    /** 拉取当前用户；刷新页面后由路由守卫调用，用于恢复登录态与角色 */
    async fetchMe() {
      if (meRequest) return meRequest
      meRequest = getMe()
        .then((info) => {
          this.userInfo = info
          this.roles = info?.roles || []
          return info
        })
        .finally(() => {
          meRequest = null
        })
      return meRequest
    },

    /** 退出：后端无 /auth/logout，纯前端清理（拦截器的 401 分支也复用它） */
    logout() {
      this.token = ''
      this.userInfo = null
      this.roles = []
      localStorage.removeItem(TOKEN_KEY)
    }
  }
})
