package com.example.teampulse.controller;

import com.example.teampulse.common.Result;
import com.example.teampulse.dto.LoginRequest;
import com.example.teampulse.dto.RegisterRequest;
import com.example.teampulse.service.AuthService;
import com.example.teampulse.vo.LoginVO;
import com.example.teampulse.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口（《前置任务》5.8 / P2-08）。
 *
 * <p>路径为相对 context-path：外部访问 {@code /api/auth/login}。
 * {@code /auth/login}、{@code /auth/register} 已在安全白名单；
 * {@code /auth/me}、{@code /auth/logout} 需认证。</p>
 */
@Tag(name = "认证与用户")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "注册（自动绑定 STUDENT 角色）")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success();
    }

    @Operation(summary = "登录（返回 token 与用户信息）")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @Operation(summary = "当前用户信息（需 Bearer token）")
    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.success(authService.me());
    }

    /**
     * 退出登录。
     *
     * <p><b>语义说明</b>：本系统是 STATELESS JWT，服务端既没有会话也没有 token 黑名单，
     * 因此这里不会让已签发的 token 失效——退出后旧 token 仍可用到
     * {@code team-pulse.jwt.expire-minutes} 到期为止。前端退出以清除本地 token 为准，
     * 该端点用于契约完整与后续接入黑名单/审计。需要认证：匿名调用返回 401。</p>
     */
    @Operation(summary = "退出登录（无状态 JWT：服务端不撤销 token，仅语义占位，需 Bearer token）")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
}
