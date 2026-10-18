package com.example.teampulse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.teampulse.entity.SysUser;
import com.example.teampulse.mapper.SysUserMapper;
import com.example.teampulse.service.SysUserService;
import org.springframework.stereotype.Service;

/**
 * 用户表 Service 实现。
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {
}
