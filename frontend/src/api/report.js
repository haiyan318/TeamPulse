import request from './request'

/**
 * 统计报表接口（7.2：GET /reports/project-progress、/reports/member-workload、/reports/due-this-week）
 * 数据源为视图 v_project_progress 等，返回结构以契约冻结为准，这里原样透出。
 */

/** 项目进度，参数示例 { projectId? } */
export function getProjectProgress(params) {
  return request.get('/reports/project-progress', { params })
}

/** 成员工作量，参数示例 { projectId?, groupId? } */
export function getMemberWorkload(params) {
  return request.get('/reports/member-workload', { params })
}

/** 本周到期任务 */
export function getDueThisWeek(params) {
  return request.get('/reports/due-this-week', { params })
}
