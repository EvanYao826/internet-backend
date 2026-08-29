package com.example.internet.controller;

import com.example.internet.common.Result;
import com.example.internet.dto.LoginDTO;
import com.example.internet.security.LoginUser;
import com.example.internet.service.AuthService;
import com.example.internet.vo.LoginVO;
import com.example.internet.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto), "登录成功");
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization.startsWith(BEARER_PREFIX)
                ? authorization.substring(BEARER_PREFIX.length())
                : authorization;
        authService.logout(token);
        return Result.ok(null, "已退出");
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/userInfo")
    public Result<UserVO> getUserInfo(@AuthenticationPrincipal LoginUser principal) {
        return Result.ok(authService.getUserInfo(principal.getUserId()));
    }
}
