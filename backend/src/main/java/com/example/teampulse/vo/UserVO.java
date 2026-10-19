package com.example.teampulse.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 用户信息出参。
 */
@Data
@Builder
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String email;

    /** 角色码列表，如 ["STUDENT"]。 */
    private List<String> roles;
}
