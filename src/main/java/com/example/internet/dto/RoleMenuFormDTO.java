package com.example.internet.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色菜单授权请求体：{ menuIds: [...] }
 */
@Data
public class RoleMenuFormDTO {

    @NotNull(message = "菜单ID列表不能为空")
    private List<Long> menuIds;
}
