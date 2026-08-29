package com.example.internet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.internet.common.BizException;
import com.example.internet.common.PageResult;
import com.example.internet.common.ResultCode;
import com.example.internet.dto.RoleFormDTO;
import com.example.internet.dto.RolePageQuery;
import com.example.internet.entity.SysMenu;
import com.example.internet.entity.SysRole;
import com.example.internet.entity.SysRoleMenu;
import com.example.internet.entity.SysUserRole;
import com.example.internet.mapper.SysMenuMapper;
import com.example.internet.mapper.SysRoleMapper;
import com.example.internet.mapper.SysRoleMenuMapper;
import com.example.internet.mapper.SysUserRoleMapper;
import com.example.internet.security.LoginUser;
import com.example.internet.vo.RoleItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 角色管理：分页、下拉全量、增删改、角色菜单授权。
 * 内置 admin 角色受删除/改码/停用保护；删除前检查是否仍分配有用户；
 * 授权在事务内全量替换；所有变更操作写入操作日志。
 */
@Service
@RequiredArgsConstructor
public class RoleService {

    public static final String BUILTIN_ROLE_CODE = "admin";

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysMenuMapper menuMapper;
    private final OperationLogService operationLogService;

    public PageResult<RoleItemVO> page(RolePageQuery query) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .like(query.getRoleName() != null && !query.getRoleName().isBlank(),
                        SysRole::getRoleName, query.getRoleName())
                .orderByAsc(SysRole::getSort)
                .orderByAsc(SysRole::getId);
        Page<SysRole> page = roleMapper.selectPage(new Page<>(query.getPage(), query.getPageSize()), wrapper);
        List<RoleItemVO> list = page.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(list, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public List<RoleItemVO> listAll() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                        .orderByAsc(SysRole::getSort)
                        .orderByAsc(SysRole::getId))
                .stream().map(this::toVO).toList();
    }

    @Transactional
    public RoleItemVO create(RoleFormDTO form, LoginUser operator) {
        requireCodeAvailable(form.getRoleCode(), null);
        SysRole role = new SysRole();
        applyForm(role, form);
        roleMapper.insert(role);
        operationLogService.log("角色", "新增角色 " + role.getRoleName(), operator.getUsername());
        return toVO(role);
    }

    @Transactional
    public void update(Long id, RoleFormDTO form, LoginUser operator) {
        SysRole role = requireRole(id);
        boolean codeChanged = !Objects.equals(role.getRoleCode(), form.getRoleCode());
        if (BUILTIN_ROLE_CODE.equals(role.getRoleCode()) && (codeChanged || form.getStatus() == 0)) {
            throw new BizException("内置管理员角色不允许修改编码或停用");
        }
        requireCodeAvailable(form.getRoleCode(), id);

        applyForm(role, form);
        roleMapper.updateById(role);
        operationLogService.log("角色", "修改角色 " + role.getRoleName(), operator.getUsername());
    }

    @Transactional
    public void delete(Long id, LoginUser operator) {
        SysRole role = requireRole(id);
        if (BUILTIN_ROLE_CODE.equals(role.getRoleCode())) {
            throw new BizException("内置管理员角色不允许删除");
        }
        Long userCount = userRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id));
        if (userCount != null && userCount > 0) {
            throw new BizException("该角色已分配给 " + userCount + " 个用户，请先解除关联");
        }
        roleMapper.deleteById(id);
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        operationLogService.log("角色", "删除角色 " + role.getRoleName(), operator.getUsername());
    }

    public List<Long> getMenuIds(Long id) {
        requireRole(id);
        return roleMenuMapper.selectList(
                        new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id))
                .stream().map(SysRoleMenu::getMenuId).distinct().toList();
    }

    @Transactional
    public void assignMenus(Long id, List<Long> menuIds, LoginUser operator) {
        SysRole role = requireRole(id);
        List<Long> distinctIds = menuIds.stream().distinct().toList();
        if (!distinctIds.isEmpty()) {
            List<SysMenu> menus = menuMapper.selectBatchIds(distinctIds);
            if (menus.size() != distinctIds.size()) {
                throw new BizException("所选菜单不存在");
            }
        }
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        for (Long menuId : distinctIds) {
            SysRoleMenu relation = new SysRoleMenu();
            relation.setRoleId(id);
            relation.setMenuId(menuId);
            roleMenuMapper.insert(relation);
        }
        operationLogService.log("角色",
                "角色「" + role.getRoleName() + "」权限已更新（共 " + distinctIds.size() + " 项）",
                operator.getUsername());
    }

    /* ---------------- 内部方法 ---------------- */

    private SysRole requireRole(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BizException(ResultCode.NOT_FOUND, "角色不存在");
        }
        return role;
    }

    private void requireCodeAvailable(String roleCode, Long excludeId) {
        Long count = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode)
                .ne(excludeId != null, SysRole::getId, excludeId));
        if (count != null && count > 0) {
            throw new BizException("角色编码已存在");
        }
    }

    private void applyForm(SysRole role, RoleFormDTO form) {
        role.setRoleName(form.getRoleName());
        role.setRoleCode(form.getRoleCode());
        role.setSort(form.getSort() == null ? 99 : form.getSort());
        role.setStatus(form.getStatus());
        role.setRemark(form.getRemark() == null ? "" : form.getRemark());
    }

    private RoleItemVO toVO(SysRole role) {
        return RoleItemVO.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .roleCode(role.getRoleCode())
                .sort(role.getSort())
                .status(role.getStatus())
                .remark(role.getRemark() == null ? "" : role.getRemark())
                .createTime(role.getCreateTime())
                .build();
    }
}
