package com.example.internet.vo;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 角色视图，与前端 RoleItem 类型对齐
 */
@Getter
@Builder
public class RoleItemVO {

    private Long id;

    private String roleName;

    private String roleCode;

    private Integer sort;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;
}
