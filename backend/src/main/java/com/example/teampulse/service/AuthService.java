package com.example.teampulse.service;

import com.example.teampulse.dto.LoginRequest;
import com.example.teampulse.dto.RegisterRequest;
import com.example.teampulse.vo.LoginVO;
import com.example.teampulse.vo.UserVO;

/**
 * 认证业务（注册 / 登录 / 当前用户）。
 */
public interface AuthService {

    void register(RegisterRequest request);

    LoginVO login(LoginRequest request);

    UserVO me();
}
