import request from './request'

/**
 * 任务与看板接口（7.2 冻结清单）
 * 状态取值：NOT_STARTED / IN_PROGRESS / REVIEWING / DONE / PAUSED / CANCELLED
 * 优先级取值：1 高 / 2 中 / 3 低
 */

/** 任务列表，参数示例 { pageNum, pageSize, keyword?, status?, priority? } */
export function getTasks(params) {
  return request.get('/tasks', { params })
}

/** 看板视图数据（按状态分列） */
export function getTaskBoard(params) {
  return request.get('/tasks/board', { params })
}

export function getTask(id) {
  return request.get(`/tasks/${id}`)
}

export function createTask(data) {
  return request.post('/tasks', data)
}

export function updateTask(id, data) {
  return request.put(`/tasks/${id}`, data)
}

export function deleteTask(id) {
  return request.delete(`/tasks/${id}`)
}

/** 调整任务负责人，data 示例 { assigneeIds: [] } */
export function updateTaskAssignees(id, data) {
  return request.put(`/tasks/${id}/assignees`, data)
}

/** 流转任务状态，data 示例 { status, remark? } */
export function updateTaskStatus(id, data) {
  return request.put(`/tasks/${id}/status`, data)
}

/** 维护任务依赖，data 示例 { prerequisiteTaskIds: [] } */
export function updateTaskDependencies(id, data) {
  return request.put(`/tasks/${id}/dependencies`, data)
}
