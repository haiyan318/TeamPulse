package com.example.teampulse.service;

import com.example.teampulse.dto.LoginRequest;
import com.example.teampulse.dto.RegisterRequest;
import com.example.teampulse.vo.LoginVO;
import com.example.teampulse.vo.UserVO;

/**
 * 认证业务（注册 / 登录 / 当前用户 / 退出）。
 */
public interface AuthService {

    void register(RegisterRequest request);

    LoginVO login(LoginRequest request);

    UserVO me();

    /**
     * 退出登录。
     *
     * <p>无状态 JWT 场景下服务端没有会话可销毁，当前实现是 no-op；
     * 保留方法是为了契约完整，以及将来接入 token 黑名单/审计时的统一入口。</p>
     */
    void logout();
}
