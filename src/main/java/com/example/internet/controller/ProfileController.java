package com.example.internet.controller;

import com.example.internet.common.Result;
import com.example.internet.dto.PasswordFormDTO;
import com.example.internet.dto.ProfileFormDTO;
import com.example.internet.security.LoginUser;
import com.example.internet.service.ProfileService;
import com.example.internet.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人中心接口，仅操作当前登录用户
 */
@Tag(name = "个人中心")
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "获取个人信息")
    @GetMapping("/info")
    public Result<UserVO> info(@AuthenticationPrincipal LoginUser principal) {
        return Result.ok(profileService.getProfile(principal.getUserId()));
    }

    @Operation(summary = "修改个人信息")
    @PutMapping("/info")
    public Result<Void> update(@Valid @RequestBody ProfileFormDTO form,
                               @AuthenticationPrincipal LoginUser principal) {
        profileService.updateProfile(principal.getUserId(), form, principal);
        return Result.ok(null, "修改成功");
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordFormDTO form,
                                       @AuthenticationPrincipal LoginUser principal) {
        profileService.changePassword(principal.getUserId(), form, principal);
        return Result.ok(null, "密码修改成功");
    }
}
