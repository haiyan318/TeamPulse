package com.example.teampulse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.teampulse.entity.TaskDependency;
import com.example.teampulse.mapper.TaskDependencyMapper;
import com.example.teampulse.service.TaskDependencyService;
import org.springframework.stereotype.Service;

/**
 * 任务依赖表 Service 实现。
 */
@Service
public class TaskDependencyServiceImpl extends ServiceImpl<TaskDependencyMapper, TaskDependency>
        implements TaskDependencyService {
}
