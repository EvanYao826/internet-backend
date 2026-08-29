package com.example.internet.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.internet.entity.SysMenu;
import com.example.internet.entity.SysRole;
import com.example.internet.entity.SysRoleMenu;
import com.example.internet.entity.SysUser;
import com.example.internet.entity.SysUserRole;
import com.example.internet.mapper.SysMenuMapper;
import com.example.internet.mapper.SysRoleMapper;
import com.example.internet.mapper.SysRoleMenuMapper;
import com.example.internet.mapper.SysUserMapper;
import com.example.internet.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 从数据库加载用户、角色与权限，供登录认证和 JWT 过滤器使用
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return buildLoginUser(user);
    }

    public LoginUser buildLoginUser(SysUser user) {
        boolean enabled = user.getStatus() != null && user.getStatus() == 1;
        return new LoginUser(user.getId(), user.getUsername(), user.getPassword(), enabled, loadPermissions(user.getId()));
    }

    public List<SysRole> listRolesByUserId(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(
                        new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).toList();
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return roleMapper.selectBatchIds(roleIds);
    }

    /**
     * 汇总用户所有角色的菜单权限标识；拥有 admin 角色时授予全部权限
     */
    public Set<String> loadPermissions(Long userId) {
        List<SysRole> roles = listRolesByUserId(userId);
        if (roles.isEmpty()) {
            return Collections.emptySet();
        }
        boolean isAdmin = roles.stream().anyMatch(r -> "admin".equals(r.getRoleCode()));

        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 1)
                .isNotNull(SysMenu::getPermission);
        if (!isAdmin) {
            List<Long> roleIds = roles.stream().map(SysRole::getId).toList();
            List<Long> menuIds = roleMenuMapper.selectList(
                            new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds))
                    .stream().map(SysRoleMenu::getMenuId).distinct().toList();
            if (menuIds.isEmpty()) {
                return Collections.emptySet();
            }
            wrapper.in(SysMenu::getId, menuIds);
        }
        return menuMapper.selectList(wrapper).stream()
                .map(SysMenu::getPermission)
                .collect(Collectors.toSet());
    }
}
