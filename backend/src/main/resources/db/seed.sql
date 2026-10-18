-- ============================================================
-- TeamPulse 数据库 DDL 脚本 seed.sql
-- 适用: MySQL 8.4.x LTS
-- 说明: 与《数据库顶层设计》v0.2 结构一致，仅修复执行与口径问题
-- 执行: 段5 用 teampulse 账号执行
-- 警告: 本脚本需搭配 schema.sql
-- 变更: 见文末附录 B
-- ============================================================

-- ============ 段5：种子数据 ============
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`) VALUES
    ('STUDENT',   '学生',   '默认角色，可加入小组、领取任务'),
    ('ASSISTANT', '助教',   '可创建课程/项目、分配任务、评审'),
    ('ADMIN',     '管理员', '系统全部权限');

-- 注意：下面 password 是占位符，必须替换为真实 BCrypt（见附录 A），否则 admin 无法登录
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `status`) VALUES
    ('admin', '$2a$10$PLACEHOLDER_REPLACE_WITH_REAL_BCRYPT_HASH_0000000000000000',
     '系统管理员', 1);

-- 用子查询绑定角色，不再依赖 id=1 / id=3
INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id FROM `sys_user` u JOIN `sys_role` r
WHERE u.username = 'admin' AND r.role_code = 'ADMIN';

-- 演示数据（按项目名称/用户名为锚点，可重复执行前先跑段1-段4清库）
INSERT INTO `course` (`course_name`, `course_code`, `semester`, `teacher_id`, `description`)
SELECT '软件工程', 'SE2026', '2026-2027-1', u.id, '课程项目实训'
FROM `sys_user` u WHERE u.username = 'admin';

INSERT INTO `course` (`course_name`, `course_code`, `semester`, `teacher_id`, `description`)
SELECT '数据库原理', 'DB2026', '2026-2027-1', u.id, '数据库设计与实现'
FROM `sys_user` u WHERE u.username = 'admin';

INSERT INTO `project` (`project_name`, `course_id`, `description`, `start_date`, `end_date`, `status`, `creator_id`)
SELECT 'TeamPulse 团队协作平台', c.id, '4 人小组课程项目', '2026-10-20', '2026-12-20', 'IN_PROGRESS', u.id
FROM `course` c JOIN `sys_user` u ON u.username = 'admin'
WHERE c.course_code = 'SE2026';

INSERT INTO `project` (`project_name`, `course_id`, `description`, `start_date`, `end_date`, `status`, `creator_id`)
SELECT '图书管理系统', c.id, '数据库课程设计', '2026-10-20', '2026-12-20', 'NOT_STARTED', u.id
FROM `course` c JOIN `sys_user` u ON u.username = 'admin'
WHERE c.course_code = 'DB2026';

INSERT INTO `project_group` (`group_name`, `project_id`, `leader_id`, `description`)
SELECT '第 4 组', p.id, u.id, 'TeamPulse 开发小组'
FROM `project` p JOIN `sys_user` u ON u.username = 'admin'
WHERE p.project_name = 'TeamPulse 团队协作平台';

INSERT INTO `group_member` (`group_id`, `user_id`, `role_in_group`)
SELECT g.id, u.id, 'LEADER'
FROM `project_group` g JOIN `sys_user` u ON u.username = 'admin'
WHERE g.group_name = '第 4 组';

-- 三条任务覆盖三种口径：已逾期 / 本周到期 / 已完成，便于验证视图
INSERT INTO `task` (`task_name`, `task_code`, `project_id`, `group_id`, `status`, `priority`,
                    `assignee_id`, `plan_start_time`, `plan_end_time`, `creator_id`)
SELECT '搭建工程骨架', 'T-001', p.id, g.id, 'DONE', 1, u.id,
       DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), u.id
FROM `project` p JOIN `project_group` g ON g.project_id = p.id
JOIN `sys_user` u ON u.username = 'admin'
WHERE p.project_name = 'TeamPulse 团队协作平台';

INSERT INTO `task` (`task_name`, `task_code`, `project_id`, `group_id`, `status`, `priority`,
                    `assignee_id`, `plan_start_time`, `plan_end_time`, `creator_id`)
SELECT '实现登录接口', 'T-002', p.id, g.id, 'IN_PROGRESS', 1, u.id,
       DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), u.id
FROM `project` p JOIN `project_group` g ON g.project_id = p.id
JOIN `sys_user` u ON u.username = 'admin'
WHERE p.project_name = 'TeamPulse 团队协作平台';

INSERT INTO `task` (`task_name`, `task_code`, `project_id`, `group_id`, `status`, `priority`,
                    `assignee_id`, `plan_start_time`, `plan_end_time`, `creator_id`)
SELECT '任务看板页面', 'T-003', p.id, g.id, 'NOT_STARTED', 2, u.id,
       NOW(), DATE_ADD(NOW(), INTERVAL 3 DAY), u.id
FROM `project` p JOIN `project_group` g ON g.project_id = p.id
JOIN `sys_user` u ON u.username = 'admin'
WHERE p.project_name = 'TeamPulse 团队协作平台';

INSERT INTO `task_assignment` (`task_id`, `user_id`)
SELECT t.id, u.id FROM `task` t JOIN `sys_user` u ON u.username = 'admin'
WHERE t.task_code IN ('T-001', 'T-002', 'T-003');

INSERT INTO `task_status_log` (`task_id`, `from_status`, `to_status`, `operator_id`, `remark`)
SELECT t.id, 'NONE', 'NOT_STARTED', u.id, '创建任务'
FROM `task` t JOIN `sys_user` u ON u.username = 'admin' WHERE t.task_code = 'T-001';

INSERT INTO `task_status_log` (`task_id`, `from_status`, `to_status`, `operator_id`, `remark`)
SELECT t.id, 'IN_PROGRESS', 'DONE', u.id, '骨架可编译'
FROM `task` t JOIN `sys_user` u ON u.username = 'admin' WHERE t.task_code = 'T-001';
