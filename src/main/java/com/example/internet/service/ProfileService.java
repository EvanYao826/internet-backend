package com.example.internet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.example.internet.common.BizException;
import com.example.internet.dto.PasswordFormDTO;
import com.example.internet.dto.ProfileFormDTO;
import com.example.internet.entity.SysUser;
import com.example.internet.mapper.SysUserMapper;
import com.example.internet.security.LoginUser;
import com.example.internet.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 个人中心：资料查询与修改、密码修改。
 * 邮箱/手机号做格式与非他人占用校验；
 * 修改密码后当前 Token 保持有效（与前端交互一致），全局失效需引入 Token 版本机制。
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final OperationLogService operationLogService;

    public UserVO getProfile(Long userId) {
        return authService.getUserInfo(userId);
    }

    public void updateProfile(Long userId, ProfileFormDTO form, LoginUser operator) {
        SysUser user = requireUser(userId);
        requireFieldAvailable(SysUser::getEmail, form.getEmail(), "邮箱已被其他账号使用", userId);
        requireFieldAvailable(SysUser::getPhone, form.getPhone(), "手机号已被其他账号使用", userId);

        user.setNickname(form.getNickname());
        user.setEmail(form.getEmail() == null ? "" : form.getEmail());
        user.setPhone(form.getPhone() == null ? "" : form.getPhone());
        userMapper.updateById(user);
        operationLogService.log("用户", "修改个人资料", operator.getUsername());
    }

    public void changePassword(Long userId, PasswordFormDTO form, LoginUser operator) {
        SysUser user = requireUser(userId);
        if (!passwordEncoder.matches(form.getOldPassword(), user.getPassword())) {
            throw new BizException("原密码不正确");
        }
        if (form.getNewPassword().equals(form.getOldPassword())) {
            throw new BizException("新密码不能与原密码相同");
        }
        user.setPassword(passwordEncoder.encode(form.getNewPassword()));
        userMapper.updateById(user);
        operationLogService.log("用户", "修改登录密码", operator.getUsername());
    }

    /* ---------------- 内部方法 ---------------- */

    private SysUser requireUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return user;
    }

    private void requireFieldAvailable(SFunction<SysUser, ?> column,
                                       String value, String message, Long selfId) {
        if (value == null || value.isBlank()) {
            return;
        }
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(column, value)
                .ne(SysUser::getId, selfId);
        Long count = userMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BizException(message);
        }
    }
}
