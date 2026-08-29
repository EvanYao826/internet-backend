package com.example.internet.vo;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户管理列表/详情视图，与前端 UserItem 类型对齐；不包含密码字段
 */
@Getter
@Builder
public class UserItemVO {

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String phone;

    private Integer status;

    private List<Long> roleIds;

    /** 角色名称（多个用「、」连接） */
    private String roleName;

    private Long deptId;

    /** 当前系统暂无部门表，固定返回 null，字段为前端可选 */
    private String deptName;

    private String remark;

    private LocalDateTime createTime;
}
