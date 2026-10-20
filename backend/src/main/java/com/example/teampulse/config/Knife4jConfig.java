package com.example.teampulse.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 3 接口文档配置（《前置任务》5.10 / P2-10）。
 *
 * <p>按模块分 4 组（认证与用户 / 课程项目小组 / 任务与看板 / 总览统计），
 * 与 README「团队与分支」的四模块划分一致。B/C/D 控制器尚未开发，
 * 对应分组暂为空，随控制器落库自动填充；{@code pathsToMatch} 前缀为占位约定，
 * 后续可随实际 URL 调整。</p>
 *
 * <p>访问地址：{@code http://localhost:8080/api/doc.html}（context-path 为 /api）。
 * 相关路径已在 {@code SecurityConfig} 白名单（/doc.html、/v3/api-docs/**、/webjars/**）。</p>
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI teamPulseOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("TeamPulse API")
                .version("1.0.0")
                .description("面向课程小组项目的进度风险预警与贡献分析系统"));
    }

    @Bean
    public GroupedOpenApi authGroup() {
        return GroupedOpenApi.builder()
                .group("01-认证与用户")
                .pathsToMatch("/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi courseGroup() {
        return GroupedOpenApi.builder()
                .group("02-课程项目小组")
                .pathsToMatch("/course/**", "/project/**", "/group/**")
                .build();
    }

    @Bean
    public GroupedOpenApi taskGroup() {
        return GroupedOpenApi.builder()
                .group("03-任务与看板")
                .pathsToMatch("/task/**")
                .build();
    }

    @Bean
    public GroupedOpenApi reportGroup() {
        return GroupedOpenApi.builder()
                .group("04-总览统计")
                .pathsToMatch("/report/**", "/overview/**", "/statistics/**")
                .build();
    }
}
