package com.example.teampulse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.teampulse.entity.TaskStatusLog;
import com.example.teampulse.mapper.TaskStatusLogMapper;
import com.example.teampulse.service.TaskStatusLogService;
import org.springframework.stereotype.Service;

/**
 * 任务状态流转日志表 Service 实现。
 */
@Service
public class TaskStatusLogServiceImpl extends ServiceImpl<TaskStatusLogMapper, TaskStatusLog>
        implements TaskStatusLogService {
}
