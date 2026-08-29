package com.example.internet.controller;

import com.example.internet.common.PageResult;
import com.example.internet.common.Result;
import com.example.internet.dto.RoleFormDTO;
import com.example.internet.dto.RoleMenuFormDTO;
import com.example.internet.dto.RolePageQuery;
import com.example.internet.security.LoginUser;
import com.example.internet.service.RoleService;
import com.example.internet.vo.RoleItemVO;
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
 * 角色管理接口。权限在后端强制校验：view/add/edit/delete/assign。
 * roles/all 供用户表单下拉使用，拥有用户查看权限即可访问。
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/system/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "角色分页列表")
    @PreAuthorize("hasAuthority('system:role:view')")
    @GetMapping
    public Result<PageResult<RoleItemVO>> page(@Valid RolePageQuery query) {
        return Result.ok(roleService.page(query));
    }

    @Operation(summary = "全部角色（下拉选择用）")
    @PreAuthorize("hasAnyAuthority('system:user:view','system:role:view')")
    @GetMapping("/all")
    public Result<List<RoleItemVO>> listAll() {
        return Result.ok(roleService.listAll());
    }

    @Operation(summary = "新增角色")
    @PreAuthorize("hasAuthority('system:role:add')")
    @PostMapping
    public Result<RoleItemVO> create(@Valid @RequestBody RoleFormDTO form,
                                     @AuthenticationPrincipal LoginUser operator) {
        return Result.ok(roleService.create(form, operator), "新增成功");
    }

    @Operation(summary = "修改角色")
    @PreAuthorize("hasAuthority('system:role:edit')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody RoleFormDTO form,
                               @AuthenticationPrincipal LoginUser operator) {
        roleService.update(id, form, operator);
        return Result.ok(null, "修改成功");
    }

    @Operation(summary = "删除角色")
    @PreAuthorize("hasAuthority('system:role:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @AuthenticationPrincipal LoginUser operator) {
        roleService.delete(id, operator);
        return Result.ok(null, "删除成功");
    }

    @Operation(summary = "获取角色已分配菜单ID")
    @PreAuthorize("hasAuthority('system:role:view')")
    @GetMapping("/{id}/menus")
    public Result<List<Long>> getMenuIds(@PathVariable Long id) {
        return Result.ok(roleService.getMenuIds(id));
    }

    @Operation(summary = "分配角色菜单权限")
    @PreAuthorize("hasAuthority('system:role:assign')")
    @PutMapping("/{id}/menus")
    public Result<Void> assignMenus(@PathVariable Long id,
                                    @Valid @RequestBody RoleMenuFormDTO form,
                                    @AuthenticationPrincipal LoginUser operator) {
        roleService.assignMenus(id, form.getMenuIds(), operator);
        return Result.ok(null, "授权成功");
    }
}
