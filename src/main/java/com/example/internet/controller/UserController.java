package com.example.internet.controller;

import com.example.internet.common.PageResult;
import com.example.internet.common.Result;
import com.example.internet.dto.UserFormDTO;
import com.example.internet.dto.UserPageQuery;
import com.example.internet.security.LoginUser;
import com.example.internet.service.UserService;
import com.example.internet.vo.UserItemVO;
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

import java.util.Map;

/**
 * 用户管理接口。权限在后端强制校验：view/add/edit/delete/resetPassword。
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户分页列表")
    @PreAuthorize("hasAuthority('system:user:view')")
    @GetMapping
    public Result<PageResult<UserItemVO>> page(@Valid UserPageQuery query) {
        return Result.ok(userService.page(query));
    }

    @Operation(summary = "用户详情")
    @PreAuthorize("hasAuthority('system:user:view')")
    @GetMapping("/{id}")
    public Result<UserItemVO> detail(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }

    @Operation(summary = "新增用户")
    @PreAuthorize("hasAuthority('system:user:add')")
    @PostMapping
    public Result<UserItemVO> create(@Valid @RequestBody UserFormDTO form,
                                     @AuthenticationPrincipal LoginUser operator) {
        return Result.ok(userService.create(form, operator), "新增成功");
    }

    @Operation(summary = "修改用户")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody UserFormDTO form,
                               @AuthenticationPrincipal LoginUser operator) {
        userService.update(id, form, operator);
        return Result.ok(null, "修改成功");
    }

    @Operation(summary = "删除用户")
    @PreAuthorize("hasAuthority('system:user:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @AuthenticationPrincipal LoginUser operator) {
        userService.delete(id, operator);
        return Result.ok(null, "删除成功");
    }

    @Operation(summary = "修改用户状态")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestBody Map<String, Integer> body,
                                     @AuthenticationPrincipal LoginUser operator) {
        userService.updateStatus(id, body.get("status"), operator);
        return Result.ok(null, "状态已更新");
    }

    @Operation(summary = "重置用户密码")
    @PreAuthorize("hasAuthority('system:user:resetPassword')")
    @PutMapping("/{id}/resetPassword")
    public Result<Void> resetPassword(@PathVariable Long id,
                                      @AuthenticationPrincipal LoginUser operator) {
        userService.resetPassword(id, operator);
        return Result.ok(null, "密码已重置为 123456");
    }
}
