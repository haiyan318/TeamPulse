package com.example.teampulse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.teampulse.entity.Project;
import com.example.teampulse.mapper.ProjectMapper;
import com.example.teampulse.service.ProjectService;
import org.springframework.stereotype.Service;

/**
 * 项目表 Service 实现。
 */
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project>
        implements ProjectService {
}
