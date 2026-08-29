package com.example.internet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.internet.common.BizException;
import com.example.internet.common.PageResult;
import com.example.internet.common.ResultCode;
import com.example.internet.dto.UserFormDTO;
import com.example.internet.dto.UserPageQuery;
import com.example.internet.entity.SysRole;
import com.example.internet.entity.SysUser;
import com.example.internet.entity.SysUserRole;
import com.example.internet.mapper.SysRoleMapper;
import com.example.internet.mapper.SysUserMapper;
import com.example.internet.mapper.SysUserRoleMapper;
import com.example.internet.security.LoginUser;
import com.example.internet.vo.UserItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户管理：分页查询、详情、增删改、状态与密码管理。
 * 内置 admin 账号与当前登录用户受删除/禁用保护；所有变更操作写入操作日志。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    public static final String BUILTIN_ADMIN = "admin";
    private static final String RESET_PASSWORD = "123456";

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final OperationLogService operationLogService;

    public PageResult<UserItemVO> page(UserPageQuery query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .like(query.getUsername() != null && !query.getUsername().isBlank(),
                        SysUser::getUsername, query.getUsername())
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .eq(query.getDeptId() != null, SysUser::getDeptId, query.getDeptId())
                .orderByDesc(SysUser::getId);
        Page<SysUser> page = userMapper.selectPage(new Page<>(query.getPage(), query.getPageSize()), wrapper);
        Map<Long, List<SysRole>> rolesByUser = loadRolesByUserIds(
                page.getRecords().stream().map(SysUser::getId).toList());
        List<UserItemVO> list = page.getRecords().stream()
                .map(user -> toVO(user, rolesByUser.getOrDefault(user.getId(), Collections.emptyList())))
                .toList();
        return PageResult.of(list, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public UserItemVO getById(Long id) {
        SysUser user = requireUser(id);
        List<SysRole> roles = loadRolesByUserIds(List.of(id)).getOrDefault(id, Collections.emptyList());
        return toVO(user, roles);
    }

    @Transactional
    public UserItemVO create(UserFormDTO form, LoginUser operator) {
        requireUsernameAvailable(form.getUsername(), null);
        List<SysRole> roles = requireRolesExist(form.getRoleIds());
        if (form.getPassword() == null || form.getPassword().isBlank()) {
            throw new BizException("新增用户时密码不能为空");
        }

        SysUser user = new SysUser();
        applyForm(user, form);
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        userMapper.insert(user);
        replaceUserRoles(user.getId(), form.getRoleIds());

        operationLogService.log("用户", "新增用户 " + user.getUsername(), operator.getUsername());
        return toVO(user, roles);
    }

    @Transactional
    public void update(Long id, UserFormDTO form, LoginUser operator) {
        SysUser user = requireUser(id);
        requireUsernameAvailable(form.getUsername(), id);
        requireRolesExist(form.getRoleIds());

        applyForm(user, form);
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(form.getPassword()));
        }
        userMapper.updateById(user);
        replaceUserRoles(id, form.getRoleIds());

        operationLogService.log("用户", "修改用户 " + user.getUsername(), operator.getUsername());
    }

    @Transactional
    public void delete(Long id, LoginUser operator) {
        SysUser user = requireUser(id);
        if (BUILTIN_ADMIN.equals(user.getUsername())) {
            throw new BizException("不能删除内置管理员账号");
        }
        if (Objects.equals(operator.getUserId(), id)) {
            throw new BizException("不能删除当前登录用户");
        }
        userMapper.deleteById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        operationLogService.log("用户", "删除用户 " + user.getUsername(), operator.getUsername());
    }

    @Transactional
    public void updateStatus(Long id, Integer status, LoginUser operator) {
        SysUser user = requireUser(id);
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException("状态取值不合法");
        }
        if (status == 0) {
            if (BUILTIN_ADMIN.equals(user.getUsername())) {
                throw new BizException("不能禁用内置管理员账号");
            }
            if (Objects.equals(operator.getUserId(), id)) {
                throw new BizException("不能禁用当前登录用户");
            }
        }
        user.setStatus(status);
        userMapper.updateById(user);
        operationLogService.log("用户",
                (status == 1 ? "启用" : "禁用") + "用户 " + user.getUsername(), operator.getUsername());
    }

    @Transactional
    public void resetPassword(Long id, LoginUser operator) {
        SysUser user = requireUser(id);
        // 与前端提示一致：重置为演示密码 123456；生产环境建议改为随机临时密码或重置链接
        user.setPassword(passwordEncoder.encode(RESET_PASSWORD));
        userMapper.updateById(user);
        operationLogService.log("用户", "重置用户 " + user.getUsername() + " 的密码", operator.getUsername());
    }

    /* ---------------- 内部方法 ---------------- */

    private SysUser requireUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private void requireUsernameAvailable(String username, Long excludeId) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .ne(excludeId != null, SysUser::getId, excludeId));
        if (count != null && count > 0) {
            throw new BizException("用户名已存在");
        }
    }

    private List<SysRole> requireRolesExist(List<Long> roleIds) {
        List<Long> distinctIds = roleIds.stream().distinct().toList();
        List<SysRole> roles = roleMapper.selectBatchIds(distinctIds);
        if (roles.size() != distinctIds.size()) {
            throw new BizException("所选角色不存在");
        }
        return roles;
    }

    private void applyForm(SysUser user, UserFormDTO form) {
        user.setUsername(form.getUsername());
        user.setNickname(form.getNickname());
        user.setEmail(form.getEmail() == null ? "" : form.getEmail());
        user.setPhone(form.getPhone() == null ? "" : form.getPhone());
        user.setStatus(form.getStatus());
        user.setDeptId(form.getDeptId());
        user.setRemark(form.getRemark() == null ? "" : form.getRemark());
    }

    private void replaceUserRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        for (Long roleId : roleIds.stream().distinct().toList()) {
            SysUserRole relation = new SysUserRole();
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            userRoleMapper.insert(relation);
        }
    }

    private Map<Long, List<SysRole>> loadRolesByUserIds(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<SysUserRole> relations = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds));
        if (relations.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<Long>> roleIdsByUser = relations.stream().collect(
                Collectors.groupingBy(SysUserRole::getUserId,
                        Collectors.mapping(SysUserRole::getRoleId, Collectors.toList())));
        Map<Long, SysRole> roleById = roleMapper.selectBatchIds(
                        roleIdsByUser.values().stream().flatMap(List::stream).distinct().toList())
                .stream().collect(Collectors.toMap(SysRole::getId, Function.identity()));
        return roleIdsByUser.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream()
                        .map(roleById::get)
                        .filter(Objects::nonNull)
                        .toList()));
    }

    private UserItemVO toVO(SysUser user, List<SysRole> roles) {
        return UserItemVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail() == null ? "" : user.getEmail())
                .phone(user.getPhone() == null ? "" : user.getPhone())
                .status(user.getStatus())
                .roleIds(roles.stream().map(SysRole::getId).toList())
                .roleName(roles.stream().map(SysRole::getRoleName).collect(Collectors.joining("、")))
                .deptId(user.getDeptId())
                .deptName(null)
                .remark(user.getRemark() == null ? "" : user.getRemark())
                .createTime(user.getCreateTime())
                .build();
    }
}
