package com.example.internet.common;

import lombok.Getter;

@Getter
public enum ResultCode {

    OK(200, "操作成功"),
    BAD_REQUEST(400, "参数或业务校验失败"),
    UNAUTHORIZED(401, "未认证或登录已过期"),
    FORBIDDEN(403, "无操作权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
