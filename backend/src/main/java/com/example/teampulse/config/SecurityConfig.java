package com.example.teampulse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 安全配置：无状态 JWT 过滤器链（《前置任务》5.6 / P2-06 白名单 + 5.7 / P2-07 过滤器接入）。
 *
 * <p>白名单路径是 <b>相对 context-path</b> 的路径：外部访问 {@code /api/auth/login}，
 * 这里必须写 {@code /auth/login}。写成 {@code /api/...} 将永远匹配不上，
 * 症状是登录接口仍被拦截成 401。</p>
 *
 * <p>JWT 过滤器已通过
 * {@code http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)}
 * 接入，先于用户名密码过滤器执行。</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 免登录白名单（不含 context-path 的 /api 前缀）。
     *
     * <p>其中 {@code /doc.html}、{@code /webjars/**}、{@code /v3/api-docs/**}、
     * {@code /swagger-ui/**} 依赖接口文档组件（P2-10 Knife4j），当前尚未引入，
     * 放行后仍返回 404，不影响安全验收。</p>
     */
    private static final String[] WHITE_LIST = {
            "/auth/login",
            "/auth/register",
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/favicon.ico",
            "/error"
    };

    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(RestAuthenticationEntryPoint authenticationEntryPoint,
                          RestAccessDeniedHandler accessDeniedHandler,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 无状态 JWT 场景不需要 CSRF Token
                .csrf(csrf -> csrf.disable())
                // 占位：CORS 响应头要等 CorsConfigurationSource Bean 落地后才真正生效
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 未认证 → 401，已认证但无权限 → 403，均返回统一 Result 结构
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // 浏览器跨域预检不带 Authorization，需单独放行
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(WHITE_LIST).permitAll()
                        .anyRequest().authenticated());

        // 5.7 已接入：JWT 过滤器先于用户名密码过滤器执行
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码编码器，供 5.8 注册/登录使用。
     *
     * <p>声明为 {@link PasswordEncoder} 接口类型，便于后续替换算法而不改调用方。</p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 暴露 {@link AuthenticationManager}，供 5.8 登录接口调用 {@code authenticate(...)}。
     *
     * <p><b>使用约束</b>：5.8 之前项目中还没有 {@code UserDetailsService}，此时该 Bean
     * 无可用认证提供者，表现为自引用懒代理，<b>调用 {@code authenticate()} 会递归</b>。
     * 5.8 注册唯一的 {@code UserDetailsService} 后，本 Bean 自动装配为
     * {@code DaoAuthenticationProvider}（复用上面的 BCryptPasswordEncoder），本类无需改动。</p>
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }
}
