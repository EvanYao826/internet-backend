package com.example.internet.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录结果视图，与前端 LoginResult 类型对齐
 */
@Getter
@AllArgsConstructor
public class LoginVO {

    private String token;

    private UserVO userInfo;
}
