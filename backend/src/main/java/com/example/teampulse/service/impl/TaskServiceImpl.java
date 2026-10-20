package com.example.teampulse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.teampulse.entity.Task;
import com.example.teampulse.mapper.TaskMapper;
import com.example.teampulse.service.TaskService;
import org.springframework.stereotype.Service;

/**
 * 任务表 Service 实现。
 */
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task>
        implements TaskService {
}
