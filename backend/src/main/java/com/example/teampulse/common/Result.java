package com.example.teampulse.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应体。
 *
 * <p>契约：所有接口一律返回该结构，字段固定为 code / message / data / timestamp。
 * 业务代码只允许通过四个静态工厂构造，禁止 {@code new Result<>()}
 * （无参构造会得到 code=null，在全局 non_null 策略下 code 键将直接消失）。</p>
 *
 * <p>前端按 {@code code === 200} 取 data，非 200 弹错误提示。</p>
 *
 * @param <T> 业务数据类型
 */
@Data
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 业务状态码，200 表示成功，其余见 {@link ErrorCode}。 */
    private Integer code;

    /** 提示信息，成功固定为 success。 */
    private String message;

    /**
     * 业务数据。
     *
     * <p>显式声明 {@link JsonInclude.Include#ALWAYS}，覆盖全局
     * {@code spring.jackson.default-property-inclusion=non_null}，
     * 保证 data 为 null 时仍输出 {@code "data": null}，前端解包逻辑稳定。</p>
     */
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private T data;

    /** 服务端时间戳，epoch 毫秒。 */
    private Long timestamp;

    /**
     * 统一构造入口，保证四个字段一次性赋值、timestamp 只在服务端产生。
     */
    private static <T> Result<T> build(int code, String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    /**
     * 成功响应，携带业务数据。
     *
     * @param data 业务数据，可为 null
     */
    public static <T> Result<T> success(T data) {
        return build(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应，无业务数据（如新增、删除、退出登录）。
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 失败响应，使用预定义错误码及其文案。
     */
    public static <T> Result<T> fail(ErrorCode errorCode) {
        return build(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 失败响应，用于参数校验、全局异常兜底等需要动态文案的场景。
     */
    public static <T> Result<T> fail(int code, String message) {
        return build(code, message, null);
    }
}
