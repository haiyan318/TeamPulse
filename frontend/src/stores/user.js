import { defineStore } from 'pinia'
import { login as loginApi, getMe, logout as logoutApi } from '@/api/auth'

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

    /**
     * 清理本地登录态。
     * 必须是同步方法：request.js 的 401 分支直接调用它，那一刻不能等网络、
     * 也不能返回 Promise，否则守卫会读到残留 token 而在 /login 与首页之间来回弹。
     */
    logout() {
      this.token = ''
      this.userInfo = null
      this.roles = []
      localStorage.removeItem(TOKEN_KEY)
    },

    /**
     * 主动退出：先通知后端（契约占位，不会吊销 token），再无条件清本地。
     * 失败（后端未启动 / token 已过期）一律吞掉——本地清干净才是唯一必要条件。
     * 注意：不能在请求前先清 token，否则这次请求头里就没有 Bearer 了。
     */
    async logoutRemote() {
      try {
        await logoutApi()
      } catch {
        // token 已过期时拦截器已顺手 logout() 过一次，这里重复清理无副作用
      } finally {
        this.logout()
      }
    }
  }
})
