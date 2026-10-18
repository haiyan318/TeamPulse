package com.example.teampulse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.teampulse.entity.Course;
import com.example.teampulse.mapper.CourseMapper;
import com.example.teampulse.service.CourseService;
import org.springframework.stereotype.Service;

/**
 * 课程表 Service 实现。
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course>
        implements CourseService {
}
