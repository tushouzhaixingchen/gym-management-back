// common/ResultCode.java
package com.gym.management.common;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    // 业务错误码
    LOGIN_FAILED(1001, "账号或密码错误"),
    USER_NOT_FOUND(1002, "用户不存在"),
    USER_ALREADY_EXISTS(1003, "用户已存在"),
    TOKEN_INVALID(1004, "Token 无效"),
    APPOINTMENT_CONFLICT(1005, "该时段已被预约，请刷新后重试"),
    COACH_NOT_FOUND(1006, "教练不存在"),
    APPOINTMENT_INVALID_DURATION(1007, "预约时长至少为 1 小时"),
    APPOINTMENT_NOT_FOUND(1008, "预约不存在"),
    APPOINTMENT_STATUS_INVALID(1009, "预约状态不可支付"),
    APPOINTMENT_ALREADY_PAID(1010, "该预约已支付"),
    APPOINTMENT_CONFIRM_INVALID(1011, "只能确认待确认状态的预约"),
    APPOINTMENT_COMPLETE_INVALID(1012, "只能完成已确认的预约"),
    APPOINTMENT_NOSHOW_INVALID(1013, "只能标记已确认的预约");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}