package com.example.internet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.internet.common.BizException;
import com.example.internet.common.ResultCode;
import com.example.internet.dto.MenuFormDTO;
import com.example.internet.entity.SysMenu;
import com.example.internet.entity.SysRoleMenu;
import com.example.internet.mapper.SysMenuMapper;
import com.example.internet.mapper.SysRoleMenuMapper;
import com.example.internet.security.LoginUser;
import com.example.internet.vo.MenuItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 菜单管理：树查询、增删改。
 * 校验父级关系（按钮必须挂在菜单下、按钮不能作为父级、禁止形成环）；
 * 删除采用保护策略：存在子菜单或已授权给角色时拒绝删除；变更操作写入操作日志。
 */
@Service
@RequiredArgsConstructor
public class MenuService {

    public static final int TYPE_DIR = 1;
    public static final int TYPE_MENU = 2;
    public static final int TYPE_BUTTON = 3;

    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final OperationLogService operationLogService;

    public List<MenuItemVO> tree() {
        List<SysMenu> all = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSort)
                .orderByAsc(SysMenu::getId));
        Map<Long, List<MenuItemVO>> byParent = all.stream()
                .map(this::toVO)
                .collect(Collectors.groupingBy(MenuItemVO::getParentId));
        return buildTree(0L, byParent);
    }

    @Transactional
    public MenuItemVO create(MenuFormDTO form, LoginUser operator) {
        validateForm(form, null);
        SysMenu menu = new SysMenu();
        applyForm(menu, form);
        menuMapper.insert(menu);
        operationLogService.log("菜单", "新增菜单 " + menu.getMenuName(), operator.getUsername());
        return toVO(menu);
    }

    @Transactional
    public void update(Long id, MenuFormDTO form, LoginUser operator) {
        SysMenu menu = requireMenu(id);
        validateForm(form, id);
        if (form.getMenuType() == TYPE_BUTTON && hasChildren(id)) {
            throw new BizException("该菜单存在子项，不能修改为按钮");
        }
        applyForm(menu, form);
        menuMapper.updateById(menu);
        operationLogService.log("菜单", "修改菜单 " + menu.getMenuName(), operator.getUsername());
    }

    @Transactional
    public void delete(Long id, LoginUser operator) {
        SysMenu menu = requireMenu(id);
        if (hasChildren(id)) {
            throw new BizException("该菜单存在子菜单，请先删除子菜单");
        }
        Long roleRefCount = roleMenuMapper.selectCount(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getMenuId, id));
        if (roleRefCount != null && roleRefCount > 0) {
            throw new BizException("该菜单已授权给 " + roleRefCount + " 个角色，请先解除授权");
        }
        menuMapper.deleteById(id);
        operationLogService.log("菜单", "删除菜单 " + menu.getMenuName(), operator.getUsername());
    }

    /* ---------------- 内部方法 ---------------- */

    private void validateForm(MenuFormDTO form, Long selfId) {
        if (form.getParentId() != null && form.getParentId() != 0) {
            SysMenu parent = menuMapper.selectById(form.getParentId());
            if (parent == null || Objects.equals(parent.getId(), selfId)) {
                throw new BizException("上级菜单不存在");
            }
            if (parent.getMenuType() == TYPE_BUTTON) {
                throw new BizException("按钮不能作为上级菜单");
            }
            // 更新场景：上级不能是自己当前的子孙节点
            if (selfId != null && isDescendant(form.getParentId(), selfId)) {
                throw new BizException("不能将菜单移动到自身或其子菜单下");
            }
        }
        if (form.getMenuType() == TYPE_BUTTON) {
            if (form.getParentId() == null || form.getParentId() == 0) {
                throw new BizException("按钮必须挂在菜单下");
            }
            SysMenu parent = menuMapper.selectById(form.getParentId());
            if (parent != null && parent.getMenuType() != TYPE_MENU) {
                throw new BizException("按钮的上级必须是菜单");
            }
        } else if (form.getPath() == null || form.getPath().isBlank()) {
            throw new BizException("请输入路由地址");
        }
    }

    /**
     * 判断 candidateId 是否为 rootId 的子孙节点
     */
    private boolean isDescendant(Long candidateId, Long rootId) {
        Long current = candidateId;
        int guard = 0;
        while (current != null && current != 0 && guard++ < 100) {
            if (current.equals(rootId)) {
                return true;
            }
            SysMenu node = menuMapper.selectById(current);
            current = node == null ? null : node.getParentId();
        }
        return false;
    }

    private boolean hasChildren(Long id) {
        Long count = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, id));
        return count != null && count > 0;
    }

    private SysMenu requireMenu(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BizException(ResultCode.NOT_FOUND, "菜单不存在");
        }
        return menu;
    }

    private void applyForm(SysMenu menu, MenuFormDTO form) {
        menu.setParentId(form.getParentId());
        menu.setMenuName(form.getMenuName());
        menu.setMenuType(form.getMenuType());
        menu.setPath(blankToNull(form.getPath()));
        menu.setComponent(blankToNull(form.getComponent()));
        menu.setIcon(blankToNull(form.getIcon()));
        menu.setPermission(blankToNull(form.getPermission()));
        menu.setSort(form.getSort() == null ? 1 : form.getSort());
        menu.setStatus(form.getStatus() == null ? 1 : form.getStatus());
        menu.setVisible(form.getVisible() == null ? 1 : form.getVisible());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private List<MenuItemVO> buildTree(Long parentId, Map<Long, List<MenuItemVO>> byParent) {
        List<MenuItemVO> children = byParent.get(parentId);
        if (children == null) {
            return List.of();
        }
        children.forEach(node -> node.getChildren().addAll(buildTree(node.getId(), byParent)));
        return children;
    }

    private MenuItemVO toVO(SysMenu menu) {
        return MenuItemVO.builder()
                .id(menu.getId())
                .parentId(menu.getParentId())
                .menuName(menu.getMenuName())
                .menuType(menu.getMenuType())
                .path(menu.getPath())
                .component(menu.getComponent())
                .icon(menu.getIcon())
                .permission(menu.getPermission())
                .sort(menu.getSort())
                .status(menu.getStatus())
                .visible(menu.getVisible())
                .children(new java.util.ArrayList<>())
                .build();
    }
}
