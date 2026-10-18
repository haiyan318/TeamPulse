-- ============================================================
-- TeamPulse 数据库 DDL 脚本 schema.sql
-- 适用: MySQL 8.4.x LTS
-- 说明: 与《数据库顶层设计》v0.2 结构一致，仅修复执行与口径问题
-- 执行: 段0 由 root 执行（库已存在则整段跳过）；段1-段4 用 teampulse 账号执行
-- 警告: 本脚本可重跑，会 DROP 并重建全部表，仅限开发环境。本脚本需搭配 seed.sql
-- 变更: 见文末附录 B
-- ============================================================

-- ============ 段0：建库（仅 root；库已存在可整段跳过）============
CREATE DATABASE IF NOT EXISTS `teampulse`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `teampulse`;

-- 会话时区固定，保证 NOW()/CURRENT_TIMESTAMP 与视图口径一致
-- 永久生效请改 my.ini: [mysqld] default-time-zone = '+08:00'
SET time_zone = '+08:00';

-- ============ 段1：清理（视图先于表 DROP，避免中间态）============
DROP VIEW IF EXISTS `v_project_progress`;

DROP TABLE IF EXISTS `task_status_log`;
DROP TABLE IF EXISTS `task_dependency`;
DROP TABLE IF EXISTS `task_assignment`;
DROP TABLE IF EXISTS `task`;
DROP TABLE IF EXISTS `group_member`;
DROP TABLE IF EXISTS `project_group`;
DROP TABLE IF EXISTS `project`;
DROP TABLE IF EXISTS `course`;
DROP TABLE IF EXISTS `sys_user_role`;
DROP TABLE IF EXISTS `sys_role`;
DROP TABLE IF EXISTS `sys_user`;

-- ============ 段2：模块 A 用户与权限（负责人: A）============
CREATE TABLE `sys_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL                COMMENT '用户名',
    `password`    VARCHAR(255) NOT NULL                COMMENT '密码(BCrypt加密)',
    `email`       VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
    `phone`       VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
    `real_name`   VARCHAR(50)  DEFAULT NULL            COMMENT '真实姓名',
    `student_id`  VARCHAR(20)  DEFAULT NULL            COMMENT '学号',
    `avatar`      VARCHAR(255) DEFAULT NULL            COMMENT '头像URL',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态:1启用,0禁用' CHECK (`status` IN (0,1)),
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP                COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除:0未删,1已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

CREATE TABLE `sys_role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_code`   VARCHAR(30)  NOT NULL                COMMENT '角色编码',
    `role_name`   VARCHAR(30)  NOT NULL                COMMENT '角色名称',
    `description` VARCHAR(200) DEFAULT NULL            COMMENT '描述',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP                COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

CREATE TABLE `sys_user_role` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT   NOT NULL                COMMENT '用户ID',
    `role_id`     BIGINT   NOT NULL                COMMENT '角色ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';

-- ============ 段3：模块 B 课程/项目/小组 + 模块 C 任务（负责人: B、C）============
CREATE TABLE `course` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `course_name` VARCHAR(100) NOT NULL                COMMENT '课程名称',
    `course_code` VARCHAR(30)  DEFAULT NULL            COMMENT '课程代码',
    `semester`    VARCHAR(20)  DEFAULT NULL            COMMENT '学期',
    `teacher_id`  BIGINT       NOT NULL                COMMENT '教师用户ID',
    `description` TEXT         DEFAULT NULL            COMMENT '课程描述',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态:1进行中,0已结课' CHECK (`status` IN (0,1)),
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP                COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_teacher_id` (`teacher_id`),
    KEY `idx_course_code` (`course_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程表';

CREATE TABLE `project` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `project_name` VARCHAR(100) NOT NULL                COMMENT '项目名称',
    `course_id`    BIGINT       NOT NULL                COMMENT '所属课程ID',
    `description`  TEXT         DEFAULT NULL            COMMENT '项目描述',
    `start_date`   DATE         DEFAULT NULL            COMMENT '开始日期',
    `end_date`     DATE         DEFAULT NULL            COMMENT '截止日期',
    `status`       VARCHAR(20)  NOT NULL DEFAULT 'NOT_STARTED' COMMENT '项目状态(仅用4值,见设计文档4.2节)',
    `creator_id`   BIGINT       NOT NULL                COMMENT '创建者ID',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                   ON UPDATE CURRENT_TIMESTAMP                COMMENT '更新时间',
    `deleted`      TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_creator_id` (`creator_id`)
    -- ,CONSTRAINT ck_project_status CHECK (`status` IN ('NOT_STARTED','IN_PROGRESS','REVIEWING','DONE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目表';

CREATE TABLE `project_group` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `group_name`  VARCHAR(100) NOT NULL                COMMENT '小组名称',
    `project_id`  BIGINT       NOT NULL                COMMENT '所属项目ID',
    `leader_id`   BIGINT       DEFAULT NULL            COMMENT '组长用户ID',
    `description` VARCHAR(500) DEFAULT NULL            COMMENT '小组描述',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态:1正常,0已解散' CHECK (`status` IN (0,1)),
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP                COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_leader_id` (`leader_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目小组表';

CREATE TABLE `group_member` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `group_id`      BIGINT      NOT NULL                COMMENT '小组ID',
    `user_id`       BIGINT      NOT NULL                COMMENT '用户ID',
    `role_in_group` VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT '组内角色:LEADER/MEMBER',
    `join_time`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP              COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_user` (`group_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='小组成员表';

CREATE TABLE `task` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_name`         VARCHAR(200) NOT NULL                COMMENT '任务名称',
    `task_code`         VARCHAR(30)  DEFAULT NULL            COMMENT '任务编号',
    `project_id`        BIGINT       NOT NULL                COMMENT '所属项目ID',
    `group_id`          BIGINT       DEFAULT NULL            COMMENT '所属小组ID',
    `description`       TEXT         DEFAULT NULL            COMMENT '任务描述',
    `status`            VARCHAR(20)  NOT NULL DEFAULT 'NOT_STARTED' COMMENT '任务状态',
    `priority`          TINYINT      NOT NULL DEFAULT 2      COMMENT '优先级:1高,2中,3低' CHECK (`priority` IN (1,2,3)),
    `assignee_id`       BIGINT       DEFAULT NULL            COMMENT '主负责人ID(冗余)',
    `plan_start_time`   DATETIME     DEFAULT NULL            COMMENT '计划开始时间',
    `plan_end_time`     DATETIME     DEFAULT NULL            COMMENT '计划截止时间',
    `actual_start_time` DATETIME     DEFAULT NULL            COMMENT '实际开始时间',
    `actual_end_time`   DATETIME     DEFAULT NULL            COMMENT '实际完成时间',
    `creator_id`        BIGINT       NOT NULL                COMMENT '创建者ID',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP                COMMENT '更新时间',
    `deleted`           TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_group_id` (`group_id`),
    KEY `idx_assignee_id` (`assignee_id`),
    KEY `idx_plan_end_time` (`plan_end_time`),
    KEY `idx_project_status` (`project_id`, `status`)
    -- ,CONSTRAINT ck_task_status CHECK (`status` IN
    --   ('NOT_STARTED','IN_PROGRESS','REVIEWING','DONE','PAUSED','CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务表';

CREATE TABLE `task_assignment` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id`     BIGINT   NOT NULL                COMMENT '任务ID',
    `user_id`     BIGINT   NOT NULL                COMMENT '被分配用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_user` (`task_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务分配表';

CREATE TABLE `task_dependency` (
    `id`                   BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id`              BIGINT   NOT NULL                COMMENT '当前任务ID',
    `prerequisite_task_id` BIGINT   NOT NULL                COMMENT '前置任务ID',
    `create_time`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_dep` (`task_id`, `prerequisite_task_id`),
    KEY `idx_prerequisite` (`prerequisite_task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务依赖表';

CREATE TABLE `task_status_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id`     BIGINT       NOT NULL                COMMENT '任务ID',
    `from_status` VARCHAR(20)  NOT NULL                COMMENT '流转前状态(新建可写 NONE)',
    `to_status`   VARCHAR(20)  NOT NULL                COMMENT '流转后状态',
    `operator_id` BIGINT       NOT NULL                COMMENT '操作人ID',
    `remark`      VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_operator_id` (`operator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务状态流转日志表';

-- ============ 段4：模块 D 统计视图（负责人: D）============
-- SQL SECURITY INVOKER：权限按调用者判定，避免 DEFINER 绑定导致换机/共用库失效
CREATE SQL SECURITY INVOKER VIEW `v_project_progress` AS
SELECT
    p.id           AS project_id,
    p.project_name AS project_name,
    p.course_id    AS course_id,
    COUNT(t.id)    AS total_tasks,
    CAST(SUM(CASE WHEN t.status = 'NOT_STARTED' THEN 1 ELSE 0 END) AS SIGNED) AS not_started_count,
    CAST(SUM(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS SIGNED) AS in_progress_count,
    CAST(SUM(CASE WHEN t.status = 'REVIEWING'   THEN 1 ELSE 0 END) AS SIGNED) AS reviewing_count,
    CAST(SUM(CASE WHEN t.status = 'DONE'        THEN 1 ELSE 0 END) AS SIGNED) AS done_count,
    CAST(SUM(CASE WHEN t.status = 'PAUSED'      THEN 1 ELSE 0 END) AS SIGNED) AS paused_count,
    CAST(SUM(CASE WHEN t.status = 'CANCELLED'   THEN 1 ELSE 0 END) AS SIGNED) AS cancelled_count,
    -- 逾期与本周到期互斥，边界统一用 NOW()
    CAST(SUM(CASE WHEN t.status NOT IN ('DONE','CANCELLED')
                   AND t.plan_end_time < NOW() THEN 1 ELSE 0 END) AS SIGNED)  AS overdue_count,
    CAST(SUM(CASE WHEN t.status NOT IN ('DONE','CANCELLED')
                   AND t.plan_end_time >= NOW()
                   AND t.plan_end_time < DATE_ADD(NOW(), INTERVAL 7 DAY)
                                          THEN 1 ELSE 0 END) AS SIGNED)       AS due_this_week_count,
    COALESCE(ROUND(SUM(CASE WHEN t.status = 'DONE' THEN 1 ELSE 0 END) * 100.0
                   / NULLIF(COUNT(t.id), 0), 1), 0)                          AS done_rate
FROM `project` p
LEFT JOIN `task` t ON t.project_id = p.id AND t.deleted = 0
WHERE p.deleted = 0
GROUP BY p.id, p.project_name, p.course_id;

-- 可选：小组维度看板（启用后视图数为 2，需同步改核验口径）
-- CREATE SQL SECURITY INVOKER VIEW `v_group_progress` AS
-- SELECT g.id AS group_id, g.group_name, g.project_id, COUNT(t.id) AS total_tasks,
--        CAST(SUM(CASE WHEN t.status='DONE' THEN 1 ELSE 0 END) AS SIGNED) AS done_count,
--        CAST(SUM(CASE WHEN t.status NOT IN ('DONE','CANCELLED')
--                       AND t.plan_end_time < NOW() THEN 1 ELSE 0 END) AS SIGNED) AS overdue_count
-- FROM `project_group` g
-- LEFT JOIN `task` t ON t.group_id = g.id AND t.deleted = 0
-- WHERE g.deleted = 0 GROUP BY g.id, g.group_name, g.project_id;


-- ============ 附录 A：生成真实 BCrypt 密码 ============
-- 方式一（推荐，与项目同一套算法）：在 IDEA 里跑一个临时类
--   System.out.println(new org.springframework.security.crypto.bcrypt
--       .BCryptPasswordEncoder().encode("123456"));
-- 方式二：Git Bash 执行 htpasswd -bnBC 10 "" 123456 | tr -d ':\n'
--   （得到 $2y$ 前缀，Spring Security 5.7+ 可匹配；不确定就用方式一）
-- 拿到哈希后执行：
--   UPDATE `sys_user` SET `password` = '<新哈希>' WHERE `username` = 'admin';

-- ============ 附录 B：v0.2 → v0.3 变更清单 ============
-- 1 段0 独立建库，主体可交给 teampulse 账号执行，规避 CREATE DATABASE 权限问题
-- 2 DROP 顺序重排：先 DROP VIEW，再按依赖倒序 DROP 表
-- 3 视图改为 SQL SECURITY INVOKER，消除 DEFINER 绑定风险
-- 4 视图口径修复：逾期与本周到期统一用 NOW() 且互斥；SUM 结果 CAST AS SIGNED；新增 done_rate
-- 5 新增 CHECK：sys_user.status / course.status / project_group.status / task.priority
--    project.status、task.status 的 CHECK 以注释形式提供，待确认枚举后启用
-- 6 sys_user.idx_student_id 改为 UNIQUE KEY uk_student_id
-- 7 task 的 idx_status 替换为复合索引 idx_project_status(project_id,status)
-- 8 task_status_log 新增 idx_operator_id
-- 9 种子数据改为子查询锚定，去掉硬编码 (1,3)；补齐 2 课程/2 项目/1 小组/3 任务/状态日志
-- 10 显式 SET time_zone='+08:00'，避免云服务器 UTC 导致逾期判定偏 8 小时
