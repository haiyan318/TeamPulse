package com.example.teampulse.common;

import lombok.Getter;

/**
 * 统一错误码。
 *
 * <p>号段划分（与《前置任务》5.3 一致）：</p>
 * <pre>
 *   通用            200 / 400 / 401 / 403 / 404 / 500
 *   A 用户权限      1001 - 1099
 *   B 课程项目小组  1101 - 1199
 *   C 任务管理      1201 - 1299
 *   D 总览统计      1301 - 1399
 * </pre>
 *
 * <p>追加规则（B、C、D 并行修改本文件时必须遵守）：</p>
 * <ol>
 *   <li>优先在本号段末尾追加，编号升序；</li>
 *   <li>首次为本模块新增号段时，在文件末尾新建段（先写段头注释再加常量）；</li>
 *   <li>同一号段内后到者顺延，不抢占他人已用编号；</li>
 *   <li>不要重排既有常量、不要整文件格式化，避免大面积 git 冲突；</li>
 *   <li>提交前先 rebase，并检查号段内是否有重复编号。</li>
 * </ol>
 */
@Getter
public enum ErrorCode {

    // ==================== 通用 200 / 400 / 401 / 403 / 404 / 500 ====================
    SUCCESS(200, "success"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    SERVER_ERROR(500, "服务端错误"),

    // ==================== A 用户权限 1001-1099 ====================
    USERNAME_EXISTS(1001, "用户名已存在"),
    PASSWORD_ERROR(1002, "密码错误"),

    // ==================== B 课程项目小组 1101-1199 ====================
    GROUP_NAME_DUPLICATE(1101, "小组名称重复"),

    // ==================== C 任务管理 1201-1299 ====================
    INVALID_STATUS_TRANSITION(1201, "非法状态流转"),
    PREREQUISITE_NOT_FINISHED(1202, "前置任务未完成"),

    // ==================== D 总览统计 1301-1399 ====================
    PROJECT_NOT_FOUND(1301, "项目不存在");

    /** 业务状态码，与 {@link Result#getCode()} 同口径。 */
    private final int code;

    /** 默认提示文案。 */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
