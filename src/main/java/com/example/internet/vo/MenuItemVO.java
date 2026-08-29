package com.example.internet.vo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 菜单树节点视图，与前端 MenuItem 类型对齐
 */
@Getter
@Builder
public class MenuItemVO {

    private Long id;

    private Long parentId;

    private String menuName;

    /** 类型：1 目录，2 菜单，3 按钮 */
    private Integer menuType;

    private String path;

    private String component;

    private String icon;

    private String permission;

    private Integer sort;

    private Integer status;

    private Integer visible;

    private List<MenuItemVO> children;
}
