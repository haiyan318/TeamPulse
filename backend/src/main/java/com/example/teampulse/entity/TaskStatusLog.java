package com.example.teampulse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务状态流转日志表实体。
 */
@Data
@TableName("task_status_log")
public class TaskStatusLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;
    private String fromStatus;
    private String toStatus;
    private Long operatorId;
    private String remark;
    private LocalDateTime createTime;
}
