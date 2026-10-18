package com.example.teampulse.exception;

import com.example.teampulse.common.ErrorCode;
import com.example.teampulse.common.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GlobalExceptionHandler 独立测试。
 *
 * <p>不加载 Spring 上下文、不连数据库：只用 standalone MockMvc 注册一个内嵌 Controller
 * 与 GlobalExceptionHandler，验证"业务码 → HTTP 状态"映射、统一响应体结构与脱敏。</p>
 *
 * <p>standaloneSetup 默认会创建 OptionalValidatorFactoryBean，这里仍显式传入
 * LocalValidatorFactoryBean，让"JSR-303 校验生效"成为测试的显式前提。</p>
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        this.mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                // MockHttpServletResponse 默认 ISO-8859-1，不设置会导致中文断言乱码
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .build();
    }

    // ==================== 业务异常：三种状态映射 ====================

    @Test
    @DisplayName("1002 密码错误 -> HTTP 400，body 为统一 Result 且 data 键存在")
    void businessError_shouldReturn400() throws Exception {
        mockMvc.perform(get("/test/business/password"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(1002))
                .andExpect(jsonPath("$.message").value("密码错误"))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(content().string(containsString("\"data\":null")))
                .andExpect(jsonPath("$.timestamp").isNumber());
    }

    @Test
    @DisplayName("1001 用户名已存在 -> HTTP 409")
    void duplicateUsername_shouldReturn409() throws Exception {
        mockMvc.perform(get("/test/business/username"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    @DisplayName("1301 项目不存在 -> HTTP 404")
    void projectNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/test/business/project"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1301))
                .andExpect(jsonPath("$.message").value("项目不存在"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    // ==================== 参数校验 ====================

    @Test
    @DisplayName("@Valid @RequestBody 失败 -> 400，message 为 field: msg")
    void invalidBody_shouldReturn400WithFieldMessage() throws Exception {
        mockMvc.perform(post("/test/valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("name: 名称不能为空"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("@RequestParam @Min 失败 -> 400（Spring 6.1 内建方法校验）")
    void requestParamConstraint_shouldReturn400() throws Exception {
        mockMvc.perform(get("/test/page").param("pageNum", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                // 不硬编码参数名前缀：内建校验与 AOP 校验的 path 前缀不同，
                // 但约束文案一致，断言文案可同时兼容两条路径
                .andExpect(jsonPath("$.message").value(containsString("页码必须大于 0")));
    }

    @Test
    @DisplayName("缺少必填查询参数 -> 400")
    void missingRequestParam_shouldReturn400() throws Exception {
        mockMvc.perform(get("/test/missing"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("缺少必要参数: keyword"));
    }

    @Test
    @DisplayName("请求体不是合法 JSON -> 400")
    void malformedJson_shouldReturn400() throws Exception {
        mockMvc.perform(post("/test/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("请求体格式错误"));
    }

    // ==================== 权限与兜底 ====================

    @Test
    @DisplayName("AccessDeniedException -> 403")
    void accessDenied_shouldReturn403() throws Exception {
        mockMvc.perform(get("/test/denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("无权限访问"));
    }

    @Test
    @DisplayName("未知异常 -> 500，且不泄露原始 message")
    void unexpectedException_shouldReturn500WithoutLeakingDetail() throws Exception {
        mockMvc.perform(get("/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("服务端异常，请联系管理员"))
                .andExpect(content().string(not(containsString("internal-secret-marker"))));
    }

    // ==================== 内嵌测试 Controller ====================

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/business/password")
        Result<Void> passwordError() {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        @GetMapping("/business/username")
        Result<Void> usernameExists() {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        @GetMapping("/business/project")
        Result<Void> projectNotFound() {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }

        @PostMapping(value = "/valid", consumes = MediaType.APPLICATION_JSON_VALUE)
        Result<Void> valid(@Valid @RequestBody CreateRequest request) {
            return Result.success();
        }

        @GetMapping("/page")
        Result<Void> page(@RequestParam @Min(value = 1, message = "页码必须大于 0") Integer pageNum) {
            return Result.success();
        }

        @GetMapping("/missing")
        Result<Void> missing(@RequestParam String keyword) {
            return Result.success();
        }

        @PostMapping(value = "/json", consumes = MediaType.APPLICATION_JSON_VALUE)
        Result<Void> json(@RequestBody CreateRequest request) {
            return Result.success();
        }

        @GetMapping("/denied")
        Result<Void> denied() {
            throw new AccessDeniedException("forbidden");
        }

        @GetMapping("/boom")
        Result<Void> boom() {
            throw new IllegalStateException("internal-secret-marker");
        }
    }

    @Data
    static class CreateRequest {

        @NotBlank(message = "名称不能为空")
        private String name;
    }
}
