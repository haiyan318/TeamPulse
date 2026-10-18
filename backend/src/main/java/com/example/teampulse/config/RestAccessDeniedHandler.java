package com.example.teampulse.config;

import com.example.teampulse.common.ErrorCode;
import com.example.teampulse.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 已认证但权限不足时返回 403 + 统一 {@link Result}。
 *
 * <p>与 {@link RestAuthenticationEntryPoint} 对称：同样发生在 Security 过滤器链内，
 * 不经过 {@code GlobalExceptionHandler}。</p>
 *
 * <p>文案差异说明：{@code ErrorCode.FORBIDDEN} 为"无权限"，而
 * {@code GlobalExceptionHandler#handleDenied} 硬编码"无权限访问"。两者均为 403、
 * 语义一致；此处沿用枚举文案，不为统一文案改动既有代码及其测试。</p>
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), Result.fail(ErrorCode.FORBIDDEN));
    }
}
