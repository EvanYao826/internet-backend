package com.example.internet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.internet.common.BizException;
import com.example.internet.common.ResultCode;
import com.example.internet.dto.LoginDTO;
import com.example.internet.entity.SysUser;
import com.example.internet.mapper.SysUserMapper;
import com.example.internet.security.CustomUserDetailsService;
import com.example.internet.security.JwtUtils;
import com.example.internet.security.LoginAttemptService;
import com.example.internet.security.TokenBlacklist;
import com.example.internet.vo.LoginVO;
import com.example.internet.vo.UserVO;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 认证服务：登录、退出、获取当前用户信息。
 * 包含账号状态校验、登录失败锁定、登录日志与 Token 黑名单。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final OperationLogService operationLogService;
    private final TokenBlacklist tokenBlacklist;

    public LoginVO login(LoginDTO dto) {
        loginAttemptService.checkLocked(dto.getUsername());

        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        // 统一提示，避免暴露用户名是否存在
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            loginAttemptService.recordFailure(dto.getUsername());
            operationLogService.log("用户", "登录失败（用户名或密码错误）", dto.getUsername());
            throw new BizException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            operationLogService.log("用户", "登录失败（账号被禁用）", dto.getUsername());
            throw new BizException("账号已被禁用，请联系管理员");
        }

        loginAttemptService.clear(dto.getUsername());
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        operationLogService.log("用户", "登录成功", user.getUsername());
        return new LoginVO(token, buildUserVO(user));
    }

    /**
     * 退出登录：将当前 Token 加入黑名单直至其自然过期
     */
    public void logout(String token) {
        Claims claims = jwtUtils.parseToken(token);
        tokenBlacklist.revoke(token, claims.getExpiration());
        operationLogService.log("用户", "退出登录");
    }

    public UserVO getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return buildUserVO(user);
    }

    /**
     * 构建前端 UserInfo：角色为编码列表；admin 角色返回通配权限 ["*"]，
     * 其余用户返回其角色关联的真实权限标识。
     */
    public UserVO buildUserVO(SysUser user) {
        List<String> roleCodes = userDetailsService.listRolesByUserId(user.getId()).stream()
                .map(r -> r.getRoleCode())
                .toList();
        boolean isAdmin = roleCodes.contains("admin");
        List<String> permissions = isAdmin
                ? List.of("*")
                : List.copyOf(userDetailsService.loadPermissions(user.getId()));
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar() == null ? "" : user.getAvatar())
                .email(user.getEmail() == null ? "" : user.getEmail())
                .phone(user.getPhone() == null ? "" : user.getPhone())
                .status(user.getStatus())
                .roles(roleCodes)
                .permissions(permissions)
                .createTime(user.getCreateTime())
                .build();
    }

    public void requireUserExists(Long userId) {
        if (userMapper.selectById(userId) == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
    }

    public Date getTokenExpiry(String token) {
        return jwtUtils.parseToken(token).getExpiration();
    }
}
