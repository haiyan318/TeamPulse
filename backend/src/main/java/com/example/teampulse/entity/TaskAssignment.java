package com.example.teampulse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务分配表实体。
 */
@Data
@TableName("task_assignment")
public class TaskAssignment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;
    private Long userId;
    private LocalDateTime createTime;
}
