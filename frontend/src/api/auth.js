import request from './request'

/**
 * 认证接口（《前置任务》7.2 冻结清单）
 * 路径不含 /api 前缀 —— axios 实例的 baseURL 已是 /api。
 *
 * 注意：后端当前没有 POST /auth/logout（7.2 有、实现未落），
 * 退出登录由前端清除 token 完成，故此处不导出 logout，
 * 避免调用到不存在的端点产生 404。
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
