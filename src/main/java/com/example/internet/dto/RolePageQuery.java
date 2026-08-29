package com.example.internet.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色分页查询参数，与前端角色列表查询对齐
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RolePageQuery extends PageQuery {

    /** 角色名称模糊查询 */
    private String roleName;
}
