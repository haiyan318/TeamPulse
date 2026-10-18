package com.example.teampulse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.teampulse.entity.ProjectGroup;
import com.example.teampulse.mapper.ProjectGroupMapper;
import com.example.teampulse.service.ProjectGroupService;
import org.springframework.stereotype.Service;

/**
 * 项目小组表 Service 实现。
 */
@Service
public class ProjectGroupServiceImpl extends ServiceImpl<ProjectGroupMapper, ProjectGroup>
        implements ProjectGroupService {
}
