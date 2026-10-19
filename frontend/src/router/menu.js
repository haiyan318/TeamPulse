/**
 * 业务菜单 / 子路由的唯一真相（P3-04）
 *
 * 为什么单独一个文件：
 * - router/index.js 要用它生成 MainLayout 的子路由；
 * - MainLayout.vue 要用它渲染侧边栏；
 * 若 MainLayout 去读 router.options.routes，会形成 MainLayout → router → MainLayout 的
 * 渲染期循环。本文件零 import，两边共用一处定义，不会漂移。
 *
 * meta.roles 缺省 = 所有登录用户可见；填写后仅命中角色可见（前端只做入口收敛，
 * 真正的权限校验在后端）。
 */
export const menuRoutes = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/Index.vue'),
    meta: { title: '总览', icon: 'DataBoard' }
  },
  {
    path: '/course',
    name: 'Course',
    component: () => import('@/views/course/Index.vue'),
    meta: { title: '课程管理', icon: 'Reading', roles: ['ASSISTANT', 'ADMIN'] }
  },
  {
    path: '/group',
    name: 'Group',
    component: () => import('@/views/group/Index.vue'),
    meta: { title: '小组管理', icon: 'UserFilled', roles: ['ASSISTANT', 'ADMIN'] }
  },
  {
    path: '/task',
    name: 'Task',
    component: () => import('@/views/task/Index.vue'),
    meta: { title: '任务管理', icon: 'List' }
  }
]
