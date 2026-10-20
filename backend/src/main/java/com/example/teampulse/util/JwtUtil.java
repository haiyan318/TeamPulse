package com.example.teampulse.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT 工具（《前置任务》5.7 / P2-07）。
 *
 * <p>基于 jjwt 0.12.5。密钥与有效期从 {@code team-pulse.jwt.*} 注入；
 * token 载荷：{@code subject=userId}，claims 含 {@code username}、{@code roles}
 * （role_code 列表），算法 HS256。</p>
 *
 * <p>解析异常（过期 / 签名非法 / 结构非法）原样抛出，由
 * {@code JwtAuthenticationFilter} 统一捕获处理。</p>
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expireMinutes;

    public JwtUtil(@Value("${team-pulse.jwt.secret}") String secret,
                   @Value("${team-pulse.jwt.expire-minutes}") long expireMinutes) {
        // HS256 要求密钥 >= 256 bit；配置中的 secret 长度需满足
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMinutes = expireMinutes;
    }

    /**
     * 生成 token。
     *
     * @param userId  用户主键（作为 subject）
     * @param username 用户名（写入 claims）
     * @param roles   角色码列表（写入 claims）
     */
    public String generateToken(Long userId, String username, List<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireMinutes * 60_000L);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * 解析并校验 token，返回 claims。
     *
     * @throws io.jsonwebtoken.JwtException 签名非法 / 过期 / 结构非法时抛出
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 claims 中取用户主键（subject）。
     */
    public Long getUserId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }
}
