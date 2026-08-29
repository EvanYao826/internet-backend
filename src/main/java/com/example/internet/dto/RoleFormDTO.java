package com.example.internet.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色新增/修改表单，与前端 RoleForm 对齐。
 * 编辑时前端禁用角色编码输入，后端仍做唯一性与内置角色保护。
 */
@Data
public class RoleFormDTO {

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过 50")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{1,29}$", message = "角色编码需以字母开头，2-30 位字母/数字/下划线")
    private String roleCode;

    private Integer sort = 99;

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态取值不合法")
    @Max(value = 1, message = "状态取值不合法")
    private Integer status;

    @Size(max = 255, message = "备注长度不能超过 255")
    private String remark;
}
