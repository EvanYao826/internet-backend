package com.example.internet.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 菜单新增/修改表单，与前端 MenuForm 对齐。
 * 类型：1 目录，2 菜单，3 按钮；目录/菜单必须携带路由地址（与前端校验一致）。
 */
@Data
public class MenuFormDTO {

    @NotNull(message = "上级菜单不能为空")
    private Long parentId;

    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过 50")
    private String menuName;

    @NotNull(message = "菜单类型不能为空")
    @Min(value = 1, message = "菜单类型不合法")
    @Max(value = 3, message = "菜单类型不合法")
    private Integer menuType;

    @Size(max = 200, message = "路由地址长度不能超过 200")
    private String path;

    @Size(max = 200, message = "组件路径长度不能超过 200")
    private String component;

    @Size(max = 50, message = "图标长度不能超过 50")
    private String icon;

    @Size(max = 100, message = "权限标识长度不能超过 100")
    private String permission;

    private Integer sort = 1;

    @Min(value = 0, message = "状态取值不合法")
    @Max(value = 1, message = "状态取值不合法")
    private Integer status;

    @Min(value = 0, message = "显示取值不合法")
    @Max(value = 1, message = "显示取值不合法")
    private Integer visible;
}
