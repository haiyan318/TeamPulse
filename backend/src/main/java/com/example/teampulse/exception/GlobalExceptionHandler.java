package com.example.teampulse.exception;

import com.example.teampulse.common.ErrorCode;
import com.example.teampulse.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器。
 *
 * <p>统一返回 {@link Result}，并给出真实 HTTP 状态码，映射规则见 {@link #resolveStatus(int)}。
 * 前端 axios 的业务错误一律走 rejection 分支，读 {@code error.response.data.message}。</p>
 *
 * <p>日志约定：4xx 用 warn 且不打堆栈；5xx 用 error 并打堆栈；
 * 响应体不回显原始异常 message，避免泄露 SQL、文件路径、类名等内部信息。</p>
 *
 * <p>边界：Spring Security 过滤器链中抛出的 AuthenticationException / AccessDeniedException
 * 在进入 DispatcherServlet 之前已被 ExceptionTranslationFilter 处理，本类捕获不到；
 * 该类场景由安全配置的 AuthenticationEntryPoint / AccessDeniedHandler 负责。</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 校验注解未写 message 时的兜底文案。 */
    private static final String DEFAULT_FIELD_MESSAGE = "参数不合法";

    // ==================== 1. 业务异常 ====================

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException e) {
        int code = e.getCode();
        HttpStatusCode status = resolveStatus(code);
        String message = StringUtils.hasText(e.getMessage()) ? e.getMessage() : "业务处理失败";

        if (status.is5xxServerError()) {
            // 例如误用 BusinessException(ErrorCode.SUCCESS) 或传了非法码，属于编码错误
            log.error("业务异常(业务码映射为 5xx，疑似编码错误): code={}, message={}", code, message, e);
        } else {
            log.warn("业务异常: code={}, message={}", code, message);
        }
        return fail(code, message);
    }

    // ==================== 2. @Valid @RequestBody / @ModelAttribute 绑定校验 ====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + defaultMessage(error.getDefaultMessage()))
                .collect(Collectors.joining("; "));

        // 只有类级约束（如 @AssertTrue）时没有 fieldError，退回 globalError
        if (!StringUtils.hasText(message)) {
            message = e.getBindingResult().getGlobalErrors().stream()
                    .map(error -> defaultMessage(error.getDefaultMessage()))
                    .collect(Collectors.joining("; "));
        }
        if (!StringUtils.hasText(message)) {
            message = ErrorCode.PARAM_ERROR.getMessage();
        }

        log.warn("参数校验失败: {}", message);
        return fail(ErrorCode.PARAM_ERROR.getCode(), message);
    }

    // ==================== 3. Spring 6.1 内建方法校验（@RequestParam @Min 等） ====================

    /**
     * Spring 6.1 起，Controller 方法参数上的约束注解由 MVC 内建校验处理，
     * 抛的是 {@link HandlerMethodValidationException}，而不是 ConstraintViolationException。
     * 该异常自带 400 与 ProblemDetail body，这里改写成统一 Result。
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Result<Void>> handleHandlerMethodValidation(HandlerMethodValidationException e) {
        List<String> parts = new ArrayList<>();
        for (ParameterValidationResult result : e.getAllValidationResults()) {
            String parameterName = result.getMethodParameter().getParameterName();
            String prefix = StringUtils.hasText(parameterName) ? parameterName : "参数";
            for (MessageSourceResolvable error : result.getResolvableErrors()) {
                parts.add(prefix + ": " + defaultMessage(error.getDefaultMessage()));
            }
        }

        String message = parts.isEmpty() ? ErrorCode.PARAM_ERROR.getMessage() : String.join("; ", parts);
        log.warn("参数校验失败: {}", message);
        return fail(ErrorCode.PARAM_ERROR.getCode(), message);
    }

    // ==================== 4. @Validated / Service 层 AOP 校验兜底 ====================

    /**
     * 触发场景：Controller 类上误加 @Validated 后退化为 AOP 校验、
     * Service 层 @Validated 方法校验、手工调用 Validator。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + defaultMessage(violation.getMessage()))
                .sorted()
                .collect(Collectors.joining("; "));

        if (!StringUtils.hasText(message)) {
            message = ErrorCode.PARAM_ERROR.getMessage();
        }
        log.warn("参数校验失败: {}", message);
        return fail(ErrorCode.PARAM_ERROR.getCode(), message);
    }

    // ==================== 5. 请求体不可读 ====================

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleMessageNotReadable(HttpMessageNotReadableException e) {
        // 详细原因（含类名、JSON 路径）只进日志，不回显
        log.warn("请求体解析失败: {}", e.getMessage());
        return fail(ErrorCode.PARAM_ERROR.getCode(), "请求体格式错误");
    }

    // ==================== 6. 缺少必填查询参数 ====================

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingParameter(MissingServletRequestParameterException e) {
        log.warn("缺少必要参数: {}", e.getParameterName());
        return fail(ErrorCode.PARAM_ERROR.getCode(), "缺少必要参数: " + e.getParameterName());
    }

    // ==================== 7. 参数类型不匹配（如 pageNum=abc） ====================

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        // 只回显参数名，不回显用户输入值
        log.warn("参数类型不正确: name={}", e.getName());
        return fail(ErrorCode.PARAM_ERROR.getCode(), "参数类型不正确: " + e.getName());
    }

    // ==================== 8. 未知 URL / 静态资源未命中 ====================

    /**
     * Spring 6.1 新增：未命中任何 Controller 时由 ResourceHttpRequestHandler 抛出。
     * 不显式处理会被兜底误判成 500。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("资源不存在: {} {}", e.getHttpMethod(), e.getResourcePath());
        return fail(ErrorCode.NOT_FOUND.getCode(), ErrorCode.NOT_FOUND.getMessage());
    }

    // ==================== 9. 无权限（进入 DispatcherServlet 之后的场景） ====================

    /**
     * 注意：URL 级权限校验发生在 Security 过滤器链中，不会进入本类；
     * 这条只覆盖进入 DispatcherServlet 之后的 AccessDeniedException（如方法级安全）。
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleDenied(AccessDeniedException e) {
        log.warn("无权限访问: {}", e.getMessage());
        return fail(ErrorCode.FORBIDDEN.getCode(), "无权限访问");
    }

    // ==================== 10. 兜底 ====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOther(Exception e, HttpServletRequest request) {
        // 未显式列出的 Spring 4xx（如 ResponseStatusException、405、415）沿用原状态码，
        // 避免被一律吞成 500
        if (e instanceof ErrorResponse errorResponse && errorResponse.getStatusCode().is4xxClientError()) {
            int status = errorResponse.getStatusCode().value();
            log.warn("请求处理失败: {} {}, status={}, exception={}",
                    request.getMethod(), request.getRequestURI(), status, e.getClass().getSimpleName());
            return fail(status, "请求无法处理");
        }

        log.error("系统异常: {} {}", request.getMethod(), request.getRequestURI(), e);
        return fail(ErrorCode.SERVER_ERROR.getCode(), "服务端异常，请联系管理员");
    }

    // ==================== 状态码映射与工具方法 ====================

    /**
     * 业务码 → HTTP 状态码。
     *
     * <pre>
     *   400-599  ：与 HTTP 同值（通用号段，也允许业务代码显式指定）
     *   1001     ：409 Conflict（唯一约束冲突）
     *   1101     ：409 Conflict（唯一约束冲突）
     *   1201     ：409 Conflict（当前状态不允许该操作）
     *   1202     ：409 Conflict（当前状态不满足前置条件）
     *   1301     ：404 Not Found（目标资源不存在）
     *   其他 1xxx：400 Bad Request（默认可修正的客户端错误）
     *   其他值    ：500 Internal Server Error（业务码本身配置错误）
     * </pre>
     *
     * <p>1002（密码错误）刻意映射为 400 而非 401：401 专留给令牌缺失/过期/非法，
     * 否则前端在登录页输错密码会触发"清 token + 跳登录"。</p>
     */
    static HttpStatusCode resolveStatus(int code) {
        if (code >= 400 && code <= 599) {
            return HttpStatusCode.valueOf(code);
        }
        return switch (code) {
            case 1001, 1101, 1201, 1202 -> HttpStatus.CONFLICT;
            case 1301 -> HttpStatus.NOT_FOUND;
            default -> (code >= 1000 && code <= 1999)
                    ? HttpStatus.BAD_REQUEST
                    : HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private static ResponseEntity<Result<Void>> fail(int code, String message) {
        return ResponseEntity.status(resolveStatus(code)).body(Result.<Void>fail(code, message));
    }

    private static String defaultMessage(String message) {
        return StringUtils.hasText(message) ? message : DEFAULT_FIELD_MESSAGE;
    }
}
