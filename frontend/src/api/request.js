import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import pinia from '@/stores'
import { useUserStore } from '@/stores/user'

/**
 * 统一请求封装（P3-03）
 *
 * 约定：
 * 1. 业务方法 resolve 的已经是 Result.data，调用方不再判断 code；
 * 2. 失败统一 reject，error.message 是可直接展示给用户的中文文案，error.code 是业务码；
 * 3. config.silent = true 可屏蔽自动 toast（登录页用自己的红字提示）。
 *
 * ⚠️ ESM 循环引用红线：本文件与 router / stores/user / api/auth 之间存在环
 *    （request → router → stores/user → api/auth → request），当前能成立依赖两点：
 *    ① 所有交叉引用都写在函数体或拦截器回调里，模块求值期不解引用，不会 TDZ；
 *    ② request.js 顶层**禁止**调用 router.beforeEach / useUserStore() / 读 router.currentRoute。
 *    新增代码请沿用这个约束。
 */

const TOKEN_KEY = 'token'

/** 并发 401 节流：10 个请求同时失效时，只弹一次提示、只跳一次登录页 */
let lastUnauthorizedAt = 0

function handleUnauthorized() {
  const now = Date.now()
  if (now - lastUnauthorizedAt < 1000) return
  lastUnauthorizedAt = now

  // 必须同步清 store：只删 localStorage 会让 store.token 残留，守卫会判「已登录」来回弹
  try {
    useUserStore(pinia).logout()
  } catch {
    localStorage.removeItem(TOKEN_KEY)
  }

  const current = router.currentRoute.value
  if (current.path === '/login') return

  ElMessage.warning({ message: '登录已过期，请重新登录', grouping: true })
  router.replace({ path: '/login', query: { redirect: current.fullPath } })
}

function normalize(error, message, code) {
  error.message = message
  error.code = code
  return Promise.reject(error)
}

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 非 Result 结构（文件流等）原样返回
    if (!res || typeof res.code !== 'number') return res

    if (res.code === 200) return res.data

    // HTTP 200 但业务码非 200（如 1002 密码错误由页面自行处理时走 silent）
    const message = res.message || '请求失败'
    if (!response.config.silent) ElMessage.error(message)
    const err = new Error(message)
    err.code = res.code
    return Promise.reject(err)
  },
  (error) => {
    const { response, config } = error
    const status = response?.status
    const code = response?.data?.code
    // 优先用后端 message，避免把 axios 的 "Request failed with status code 400" 抛给用户
    const message =
      response?.data?.message ||
      (status ? `请求失败（${status}）` : '网络异常，请检查后端服务是否启动')

    if (status === 401) {
      handleUnauthorized()
      return normalize(error, message, code ?? 401)
    }
    if (status === 403) {
      if (!config?.silent) ElMessage.warning('无权限访问')
      return normalize(error, '无权限访问', code ?? 403)
    }
    if (!config?.silent) ElMessage.error(message)
    return normalize(error, message, code)
  }
)

export default service
