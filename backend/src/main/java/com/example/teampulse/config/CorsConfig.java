package com.example.teampulse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * 跨域配置（《前置任务》5.9 / P2-09）。
 *
 * <p>读取 {@code team-pulse.cors.allowed-origins}（逗号分隔），注册
 * {@link CorsConfigurationSource}。{@code SecurityConfig} 中的
 * {@code .cors(Customizer.withDefaults())} 会自动拾取本 Bean，无需改动安全配置。</p>
 *
 * <p>鉴权使用 Bearer 头、不依赖 Cookie，因此 {@code allowCredentials=false}；
 * {@code allowedOrigins} 为具体 origin（非 {@code *}），符合 CORS 规范。</p>
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${team-pulse.cors.allowed-origins}") String allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
