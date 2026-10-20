package com.example.teampulse.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 登录出参：token + 用户信息。
 */
@Data
@Builder
public class LoginVO {

    private String token;
    private UserVO userInfo;
}
