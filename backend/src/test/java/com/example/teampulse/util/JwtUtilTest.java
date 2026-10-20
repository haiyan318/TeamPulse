package com.example.teampulse.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * JwtUtil 单元测试（不连库、不加载 Spring 上下文）。
 */
class JwtUtilTest {

    private static final String SECRET =
            "teampulse-jwt-secret-key-please-change-in-production-0123456789";

    private final JwtUtil jwtUtil = new JwtUtil(SECRET, 720);

    @Test
    @DisplayName("round-trip：生成 -> 解析，subject 与 claims 一致")
    void roundTrip_preservesClaims() {
        String token = jwtUtil.generateToken(1L, "admin", List.of("ADMIN", "STUDENT"));

        Claims claims = jwtUtil.parseToken(token);

        assertEquals(1L, jwtUtil.getUserId(claims));
        assertEquals("admin", claims.get("username"));
        assertEquals(List.of("ADMIN", "STUDENT"), claims.get("roles", List.class));
    }

    @Test
    @DisplayName("非法 token -> 抛 JwtException")
    void parse_garbageToken_throws() {
        assertThrows(JwtException.class, () -> jwtUtil.parseToken("not-a-jwt"));
    }

    @Test
    @DisplayName("过期 token -> 抛 ExpiredJwtException")
    void parse_expiredToken_throws() {
        // 负有效期：expiry = now - 1min，必然过期
        JwtUtil shortLived = new JwtUtil(SECRET, -1);
        String token = shortLived.generateToken(1L, "admin", List.of());

        assertThrows(ExpiredJwtException.class, () -> shortLived.parseToken(token));
    }
}
