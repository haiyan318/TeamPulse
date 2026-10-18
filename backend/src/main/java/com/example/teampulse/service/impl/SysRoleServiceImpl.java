package com.example.teampulse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.teampulse.entity.SysRole;
import com.example.teampulse.mapper.SysRoleMapper;
import com.example.teampulse.service.SysRoleService;
import org.springframework.stereotype.Service;

/**
 * 角色表 Service 实现。
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole>
        implements SysRoleService {
}
