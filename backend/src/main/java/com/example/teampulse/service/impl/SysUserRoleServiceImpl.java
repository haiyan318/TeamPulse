package com.example.teampulse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.teampulse.entity.SysUserRole;
import com.example.teampulse.mapper.SysUserRoleMapper;
import com.example.teampulse.service.SysUserRoleService;
import org.springframework.stereotype.Service;

/**
 * 用户角色关联表 Service 实现。
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole>
        implements SysUserRoleService {
}
