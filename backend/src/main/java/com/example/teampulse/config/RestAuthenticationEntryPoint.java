package com.example.teampulse.config;

import com.example.teampulse.common.ErrorCode;
import com.example.teampulse.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 未认证（无 Token / Token 非法）时返回 401 + 统一 {@link Result}。
 *
 * <p>Spring Security 过滤器链中抛出的异常在进入 DispatcherServlet 之前已被
 * {@code ExceptionTranslationFilter} 处理，{@code GlobalExceptionHandler} 捕获不到，
 * 因此必须在这里自行写响应体。</p>
 *
 * <p>注意：不要改用 {@code response.sendError(401)}，那会转发到 {@code /error}
 * 并由 {@code BasicErrorController} 输出 {@code {timestamp,status,error,path}}，
 * 破坏统一返回体契约。</p>
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // 写字节流而非 Writer，避免容器默认编码导致中文乱码
        objectMapper.writeValue(response.getOutputStream(), Result.fail(ErrorCode.UNAUTHORIZED));
    }
}
