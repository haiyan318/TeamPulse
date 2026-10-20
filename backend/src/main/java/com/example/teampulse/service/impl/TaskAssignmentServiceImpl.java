package com.example.teampulse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.teampulse.entity.TaskAssignment;
import com.example.teampulse.mapper.TaskAssignmentMapper;
import com.example.teampulse.service.TaskAssignmentService;
import org.springframework.stereotype.Service;

/**
 * 任务分配表 Service 实现。
 */
@Service
public class TaskAssignmentServiceImpl extends ServiceImpl<TaskAssignmentMapper, TaskAssignment>
        implements TaskAssignmentService {
}
