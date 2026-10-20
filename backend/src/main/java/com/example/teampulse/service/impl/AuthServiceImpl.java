package com.example.teampulse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.teampulse.common.ErrorCode;
import com.example.teampulse.dto.LoginRequest;
import com.example.teampulse.dto.RegisterRequest;
import com.example.teampulse.entity.SysRole;
import com.example.teampulse.entity.SysUser;
import com.example.teampulse.entity.SysUserRole;
import com.example.teampulse.exception.BusinessException;
import com.example.teampulse.mapper.SysRoleMapper;
import com.example.teampulse.mapper.SysUserMapper;
import com.example.teampulse.mapper.SysUserRoleMapper;
import com.example.teampulse.service.AuthService;
import com.example.teampulse.util.JwtUtil;
import com.example.teampulse.vo.LoginVO;
import com.example.teampulse.vo.UserVO;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 认证业务实现（《前置任务》5.8 / P2-08）。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final String STUDENT_ROLE_CODE = "STUDENT";

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(SysUserMapper userMapper,
                           SysUserRoleMapper userRoleMapper,
                           SysRoleMapper roleMapper,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        SysUser exists = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (exists != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRealName(request.getRealName());
        user.setStatus(1);
        userMapper.insert(user);

        // 注册默认绑定 STUDENT 角色（最小闭环）
        SysRole studentRole = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, STUDENT_ROLE_CODE));
        if (studentRole != null) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(studentRole.getId());
            userRoleMapper.insert(userRole);
        }
    }

    @Override
    public LoginVO login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            String username = authentication.getName();
            SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, username));
            List<String> roleCodes = roleCodesOf(user.getId());
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roleCodes);
            return LoginVO.builder()
                    .token(token)
                    .userInfo(toUserVO(user, roleCodes))
                    .build();
        } catch (AuthenticationException e) {
            // 用户名不存在 / 密码错误 / 禁用统一按密码错误返回，避免用户枚举
            throw new BusinessException(ErrorCode.PASSWORD_ERROR, "用户名或密码错误");
        }
    }

    @Override
    public UserVO me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        List<String> roleCodes = roleCodesOf(user.getId());
        return toUserVO(user, roleCodes);
    }

    @Override
    public void logout() {
        // 无状态（SessionCreationPolicy.STATELESS）+ 无 token 黑名单：
        // 服务端没有可撤销的会话状态，"退出"实际由客户端丢弃 token 完成。
        // 这里刻意保持 no-op：不查库、不改状态、不开事务，保证接口在 DB 抖动时也能
        // 快速返回 200，让前端 finally 里必定清本地 token。
        // 将来若引入 Redis 黑名单，在此把当前 token 的 jti 写入并设置剩余 TTL 即可。
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

    private UserVO toUserVO(SysUser user, List<String> roleCodes) {
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .roles(roleCodes)
                .build();
    }
}
