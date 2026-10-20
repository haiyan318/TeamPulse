import request from './request'

/**
 * 认证接口（《前置任务》7.2 冻结清单）
 * 路径不含 /api 前缀 —— axios 实例的 baseURL 已是 /api。
 *
 * 关于 logout：后端是 STATELESS JWT，`POST /auth/logout` 只作契约占位，
 * **不会让已签发的 token 失效**（仍可用到 expire-minutes 到期）。
 * 真正生效的是前端丢弃 token，见 stores/user.js 的 logout()。
 */

/** 注册；成功时 data 为 null */
export function register(data) {
  return request.post('/auth/register', data)
}

/**
 * 登录，成功返回 { token, userInfo: { id, username, realName?, email?, roles } }
 * silent：失败信息由登录页红字呈现，这里屏蔽拦截器的 toast，避免双重提示
 */
export function login(data) {
  return request.post('/auth/login', data, { silent: true })
}

/** 当前登录用户；token 失效由拦截器统一处理（清 token + 跳登录） */
export function getMe() {
  return request.get('/auth/me')
}

/**
 * 退出登录。服务端无状态，不撤销 token，端点只作契约占位。
 * silent：退出失败不该再弹红字，由 store 吞掉异常后照常清本地。
 */
export function logout() {
  return request.post('/auth/logout', null, { silent: true })
}
