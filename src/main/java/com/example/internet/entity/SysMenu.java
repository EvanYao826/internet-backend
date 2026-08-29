package com.example.internet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_menu")
public class SysMenu {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父菜单 ID，顶级为 0 */
    private Long parentId;

    private String menuName;

    /** 类型：1 目录，2 菜单，3 按钮 */
    private Integer menuType;

    private String path;

    private String component;

    private String icon;

    /** 权限标识，如 system:user:view */
    private String permission;

    private Integer sort;

    /** 状态：1 启用，0 停用 */
    private Integer status;

    /** 是否可见：1 可见，0 隐藏 */
    private Integer visible;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
