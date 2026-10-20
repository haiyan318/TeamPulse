package com.example.teampulse.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目小组表实体。
 */
@Data
@TableName("project_group")
public class ProjectGroup {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String groupName;
    private Long projectId;
    private Long leaderId;
    private String description;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
