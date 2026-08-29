package com.example.internet.vo;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息视图，与前端 UserInfo 类型对齐；不包含密码等敏感字段
 */
@Getter
@Builder
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String email;

    private String phone;

    private Integer status;

    /** 角色编码列表，如 admin */
    private List<String> roles;

    /** 权限标识列表，admin 角色返回 ["*"] */
    private List<String> permissions;

    private LocalDateTime createTime;
}
