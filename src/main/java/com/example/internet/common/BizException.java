package com.example.internet.common;

import lombok.Getter;

/**
 * 业务异常，由 GlobalExceptionHandler 统一转换为 Result 响应
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        this(ResultCode.BAD_REQUEST.getCode(), message);
    }

    public BizException(ResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getMessage());
    }

    public BizException(ResultCode resultCode, String message) {
        this(resultCode.getCode(), message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
}
