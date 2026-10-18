package com.example.teampulse.exception;

import com.example.teampulse.common.ErrorCode;
import lombok.Getter;

import java.io.Serial;

/**
 * 业务异常。
 *
 * <p>继承 {@link RuntimeException}：Service 方法无需声明 throws，
 * 且 Spring 的 {@code @Transactional} 默认对 RuntimeException 回滚。</p>
 *
 * <p>只携带业务码与提示文案，不携带 HTTP 状态：HTTP 状态由
 * {@link GlobalExceptionHandler#resolveStatus(int)} 统一推导，
 * 避免业务代码依赖 Web 层类型。</p>
 *
 * <p>用法：{@code throw new BusinessException(ErrorCode.PASSWORD_ERROR);}</p>
 */
@Getter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 业务状态码，与 {@code Result.code}、{@link ErrorCode#getCode()} 同口径。 */
    private final int code;

    /**
     * 使用预定义错误码及其默认文案。
     */
    public BusinessException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    /**
     * 使用预定义错误码，并覆盖默认文案。
     * 文案为 null 或空白时回退到错误码自带文案，避免响应 message 为空。
     */
    public BusinessException(ErrorCode errorCode, String message) {
        this(errorCode.getCode(),
                (message == null || message.isBlank()) ? errorCode.getMessage() : message);
    }

    /**
     * 自定义业务码 + 自定义文案，用于 ErrorCode 尚未覆盖的场景。
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
