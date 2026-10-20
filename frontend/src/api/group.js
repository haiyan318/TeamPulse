import request from './request'

/**
 * 小组接口（7.2：GET /groups、GET /groups/{id}、POST /groups、DELETE /groups/{id}、
 * POST /groups/{id}/members、DELETE /groups/{id}/members、PUT /groups/{id}/leader）
 *
 * 注意：删除成员的 body 必须包在 { data } 里，axios 的 delete 第二个参数是 config，
 * 直接传对象会被当成配置丢弃（这是最容易踩的一个坑）。
 */

export function getGroups(params) {
  return request.get('/groups', { params })
}

export function getGroup(id) {
  return request.get(`/groups/${id}`)
}

export function createGroup(data) {
  return request.post('/groups', data)
}

export function deleteGroup(id) {
  return request.delete(`/groups/${id}`)
}

/** 新增组员，data 示例 { userIds: [] }，具体字段以 7.2 契约为准 */
export function addGroupMembers(id, data) {
  return request.post(`/groups/${id}/members`, data)
}

/** 移除组员，data 走 config.data */
export function removeGroupMembers(id, data) {
  return request.delete(`/groups/${id}/members`, { data })
}

/** 设置组长，data 示例 { leaderId } */
export function updateGroupLeader(id, data) {
  return request.put(`/groups/${id}/leader`, data)
}
