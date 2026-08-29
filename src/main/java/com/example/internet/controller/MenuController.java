package com.example.internet.controller;

import com.example.internet.common.Result;
import com.example.internet.dto.MenuFormDTO;
import com.example.internet.security.LoginUser;
import com.example.internet.service.MenuService;
import com.example.internet.vo.MenuItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单管理接口。树查询对角色管理页（授权弹窗）开放，
 * 增删改需 system:menu:* 权限，后端强制校验。
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/system/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "菜单树")
    @PreAuthorize("hasAnyAuthority('system:menu:view','system:role:view')")
    @GetMapping
    public Result<List<MenuItemVO>> tree() {
        return Result.ok(menuService.tree());
    }

    @Operation(summary = "新增菜单")
    @PreAuthorize("hasAuthority('system:menu:add')")
    @PostMapping
    public Result<MenuItemVO> create(@Valid @RequestBody MenuFormDTO form,
                                     @AuthenticationPrincipal LoginUser operator) {
        return Result.ok(menuService.create(form, operator), "新增成功");
    }

    @Operation(summary = "修改菜单")
    @PreAuthorize("hasAuthority('system:menu:edit')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody MenuFormDTO form,
                               @AuthenticationPrincipal LoginUser operator) {
        menuService.update(id, form, operator);
        return Result.ok(null, "修改成功");
    }

    @Operation(summary = "删除菜单")
    @PreAuthorize("hasAuthority('system:menu:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @AuthenticationPrincipal LoginUser operator) {
        menuService.delete(id, operator);
        return Result.ok(null, "删除成功");
    }
}
