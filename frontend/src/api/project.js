import request from './request'

/**
 * 项目接口（7.2：GET /projects、GET /projects/{id}、POST /projects、PUT/DELETE /projects/{id}）
 * 单列一个文件的原因：7.2 有 4 条 /projects 接口，塞进 course.js 会让模块职责含混。
 */

export function getProjects(params) {
  return request.get('/projects', { params })
}

export function getProject(id) {
  return request.get(`/projects/${id}`)
}

export function createProject(data) {
  return request.post('/projects', data)
}

export function updateProject(id, data) {
  return request.put(`/projects/${id}`, data)
}

export function deleteProject(id) {
  return request.delete(`/projects/${id}`)
}
