import request from './request'

/**
 * 课程管理接口（7.2：GET/POST /courses、PUT/DELETE /courses/{id}）
 * 路径不含 /api 前缀；入参原样透传，不在此处臆造 DTO。
 * 分页入参固定 pageNum / pageSize（7.3 冻结）。
 */

/** 课程列表，参数示例 { pageNum, pageSize, keyword?, semester? } */
export function getCourses(params) {
  return request.get('/courses', { params })
}

export function createCourse(data) {
  return request.post('/courses', data)
}

export function updateCourse(id, data) {
  return request.put(`/courses/${id}`, data)
}

export function deleteCourse(id) {
  return request.delete(`/courses/${id}`)
}
