package com.example.teampulse.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.teampulse.entity.SysRole;
import com.example.teampulse.entity.SysUser;
import com.example.teampulse.entity.SysUserRole;
import com.example.teampulse.mapper.SysRoleMapper;
import com.example.teampulse.mapper.SysUserMapper;
import com.example.teampulse.mapper.SysUserRoleMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 按用户名加载用户与角色（《前置任务》5.8 / P2-08）。
 *
 * <p>注册本 Bean 后，{@code SecurityConfig} 暴露的 {@code AuthenticationManager}
 * 会自动装配为 {@code DaoAuthenticationProvider}（复用 BCryptPasswordEncoder），
 * {@code authenticate()} 不再递归。</p>
 *
 * <p>authorities 格式 {@code ROLE_<role_code>}；禁用用户（status != 1）登录会被
 * {@code DaoAuthenticationProvider} 以 DisabledException 拒绝。</p>
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;

    public UserDetailsServiceImpl(SysUserMapper userMapper,
                                  SysUserRoleMapper userRoleMapper,
                                  SysRoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        List<String> roleCodes = roleCodesOf(user.getId());
        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(roleCodes.stream().map(c -> "ROLE_" + c).toArray(String[]::new))
                .disabled(user.getStatus() == null || user.getStatus() != 1)
                .build();
    }

    private List<String> roleCodesOf(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                        .in(SysRole::getId, roleIds))
                .stream()
                .map(SysRole::getRoleCode)
                .toList();
    }
}
