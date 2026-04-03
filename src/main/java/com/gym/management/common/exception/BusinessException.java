package com.gym.management.common.exception;

import com.gym.management.common.ResultCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;
    private final String message;

    /**
     * 无参构造函数
     */
    public BusinessException() {
        this(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMessage());
    }

    /**
     * 只传消息
     */
    public BusinessException(String message) {
        this(ResultCode.ERROR.getCode(), message);
    }

    /**
     * 传状态码和消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 传枚举（最常用）⭐
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }
}