package com.example.teampulse.config;

import com.example.teampulse.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器（《前置任务》5.7 / P2-07）。
 *
 * <p>解析 {@code Authorization: Bearer <token>}，校验通过后把
 * {@code userId}（principal）与角色 authorities 写入 {@link SecurityContextHolder}。
 * 不逐请求查库，角色直接取自 token claims；{@code /auth/me} 内部再实时查库取最新信息。</p>
 *
 * <p>无 token / 解析失败 / 过期：清空上下文后放行，由后续安全链的
 * {@code RestAuthenticationEntryPoint} 返回 401 统一返回体。</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final String header;
    private final String prefix;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   @Value("${team-pulse.jwt.header}") String header,
                                   @Value("${team-pulse.jwt.prefix}") String prefix) {
        this.jwtUtil = jwtUtil;
        this.header = header;
        this.prefix = prefix;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(header);
        if (authHeader != null && authHeader.startsWith(prefix)) {
            String token = authHeader.substring(prefix.length()).trim();
            try {
                Claims claims = jwtUtil.parseToken(token);
                Long userId = jwtUtil.getUserId(claims);
                List<String> roles = claims.get("roles", List.class);
                List<SimpleGrantedAuthority> authorities = roles == null
                        ? List.of()
                        : roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // token 非法 / 过期：清空上下文，交给安全链返回 401
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
