-- =====================================================================
-- Internet 后台管理系统数据库初始化脚本
-- 适用：MySQL 8.x
-- 演示账号：admin / 123456（BCrypt 加密存储）
-- =====================================================================

CREATE DATABASE IF NOT EXISTS internet_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE internet_admin;

-- ---------------------------- 用户表 ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username    VARCHAR(50)  NOT NULL COMMENT '用户名',
    nickname    VARCHAR(50)  NOT NULL COMMENT '昵称',
    avatar      VARCHAR(255) NULL COMMENT '头像地址',
    email       VARCHAR(100) NULL DEFAULT '' COMMENT '邮箱',
    phone       VARCHAR(20)  NULL DEFAULT '' COMMENT '手机号',
    password    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt）',
    dept_id     BIGINT       NULL COMMENT '部门ID',
    remark      VARCHAR(255) NULL DEFAULT '' COMMENT '备注',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB COMMENT = '用户表';

-- ---------------------------- 角色表 ----------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_name   VARCHAR(50)  NOT NULL COMMENT '角色名称',
    role_code   VARCHAR(50)  NOT NULL COMMENT '角色编码',
    sort        INT          NOT NULL DEFAULT 99 COMMENT '显示顺序',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
    remark      VARCHAR(255) NULL DEFAULT '' COMMENT '备注',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE = InnoDB COMMENT = '角色表';

-- ---------------------------- 菜单表 ----------------------------
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父菜单ID，顶级为0',
    menu_name   VARCHAR(50)  NOT NULL COMMENT '菜单名称',
    menu_type   TINYINT      NOT NULL COMMENT '类型：1目录 2菜单 3按钮',
    path        VARCHAR(200) NULL COMMENT '路由地址',
    component   VARCHAR(200) NULL COMMENT '组件路径',
    icon        VARCHAR(50)  NULL COMMENT '图标',
    permission  VARCHAR(100) NULL COMMENT '权限标识，如 system:user:view',
    sort        INT          NOT NULL DEFAULT 99 COMMENT '显示顺序',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
    visible     TINYINT      NOT NULL DEFAULT 1 COMMENT '是否可见：1可见 0隐藏',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB COMMENT = '菜单与按钮权限表';

-- ---------------------------- 用户角色关联表 ----------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id     BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE = InnoDB COMMENT = '用户与角色关联表';

-- ---------------------------- 角色菜单关联表 ----------------------------
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE = InnoDB COMMENT = '角色与菜单关联表';

-- ---------------------------- 操作日志表 ----------------------------
DROP TABLE IF EXISTS sys_operation_log;
CREATE TABLE sys_operation_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    type        VARCHAR(20)  NOT NULL COMMENT '操作类型：用户/角色/菜单/系统',
    content     VARCHAR(255) NOT NULL COMMENT '操作内容',
    operator    VARCHAR(50)  NOT NULL COMMENT '操作人',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB COMMENT = '操作日志表';

-- =====================================================================
-- 初始化数据
-- =====================================================================

-- 角色
INSERT INTO sys_role (id, role_name, role_code, sort, status, remark, create_time) VALUES
(1, '超级管理员', 'admin',    1, 1, '拥有所有权限', '2024-01-01 09:00:00'),
(2, '运营人员',   'operator', 2, 1, '负责日常运营', '2024-02-12 10:20:00'),
(3, '财务人员',   'finance',  3, 1, '负责财务相关', '2024-03-05 14:30:00'),
(4, '访客',       'guest',    4, 0, '只读权限',     '2024-04-18 16:00:00');

-- 用户（密码均为 123456 的 BCrypt 值）
INSERT INTO sys_user (id, username, nickname, email, phone, password, dept_id, remark, status, create_time) VALUES
(1,  'admin',   '超级管理员', 'admin@example.com',   '13800000000', '$2a$10$domzvzmkzvt5wEfKUVmn.ug3v8FaKuqmZbp2lZAgpyxX4.6SjDMCq', 1, '内置管理员', 1, '2024-01-01 09:00:00'),
(2,  'user001', '张伟',       'user001@example.com', '13900000001', '$2a$10$domzvzmkzvt5wEfKUVmn.ug3v8FaKuqmZbp2lZAgpyxX4.6SjDMCq', 1, '演示账号',   1, '2024-02-01 10:00:00'),
(3,  'user002', '李娜',       'user002@example.com', '13900000002', '$2a$10$domzvzmkzvt5wEfKUVmn.ug3v8FaKuqmZbp2lZAgpyxX4.6SjDMCq', 2, '',           1, '2024-02-02 10:00:00'),
(4,  'user003', '王敏',       'user003@example.com', '13900000003', '$2a$10$domzvzmkzvt5wEfKUVmn.ug3v8FaKuqmZbp2lZAgpyxX4.6SjDMCq', 3, '',           1, '2024-02-03 10:00:00'),
(5,  'user004', '赵强',       'user004@example.com', '13900000004', '$2a$10$domzvzmkzvt5wEfKUVmn.ug3v8FaKuqmZbp2lZAgpyxX4.6SjDMCq', 1, '演示账号',   0, '2024-02-04 10:00:00'),
(6,  'user005', '刘磊',       'user005@example.com', '13900000005', '$2a$10$domzvzmkzvt5wEfKUVmn.ug3v8FaKuqmZbp2lZAgpyxX4.6SjDMCq', 2, '',           1, '2024-02-05 10:00:00'),
(7,  'user006', '陈洋',       'user006@example.com', '13900000006', '$2a$10$domzvzmkzvt5wEfKUVmn.ug3v8FaKuqmZbp2lZAgpyxX4.6SjDMCq', 3, '',           1, '2024-02-06 10:00:00'),
(8,  'user007', '杨勇',       'user007@example.com', '13900000007', '$2a$10$domzvzmkzvt5wEfKUVmn.ug3v8FaKuqmZbp2lZAgpyxX4.6SjDMCq', 1, '',           1, '2024-02-07 10:00:00'),
(9,  'user008', '黄艳',       'user008@example.com', '13900000008', '$2a$10$domzvzmkzvt5wEfKUVmn.ug3v8FaKuqmZbp2lZAgpyxX4.6SjDMCq', 2, '演示账号',   1, '2024-02-08 10:00:00'),
(10, 'user009', '周杰',       'user009@example.com', '13900000009', '$2a$10$domzvzmkzvt5wEfKUVmn.ug3v8FaKuqmZbp2lZAgpyxX4.6SjDMCq', 3, '',           1, '2024-02-09 10:00:00');

-- 菜单与按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, icon, permission, sort, status, visible) VALUES
(1,  0,  '仪表盘',     2, '/dashboard',    'dashboard/index',    'dashboard', 'dashboard:view',    1, 1, 1),
(10, 0,  '系统管理',   1, '/system',       NULL,                 'system',    NULL,                2, 1, 1),
(11, 10, '用户管理',   2, '/system/user',  'system/user/index',  'user',      'system:user:view',  1, 1, 1),
(12, 10, '角色管理',   2, '/system/role',  'system/role/index',  'role',      'system:role:view',  2, 1, 1),
(13, 10, '菜单管理',   2, '/system/menu',  'system/menu/index',  'menu',      'system:menu:view',  3, 1, 1),
(20, 0,  '个人中心',   2, '/profile',      'profile/index',      'profile',   NULL,                3, 1, 0),
(111, 11, '用户新增',   3, NULL, NULL, NULL, 'system:user:add',           1, 1, 1),
(112, 11, '用户修改',   3, NULL, NULL, NULL, 'system:user:edit',          2, 1, 1),
(113, 11, '用户删除',   3, NULL, NULL, NULL, 'system:user:delete',        3, 1, 1),
(114, 11, '用户重置密码', 3, NULL, NULL, NULL, 'system:user:resetPassword', 4, 1, 1),
(121, 12, '角色新增',   3, NULL, NULL, NULL, 'system:role:add',           1, 1, 1),
(122, 12, '角色修改',   3, NULL, NULL, NULL, 'system:role:edit',          2, 1, 1),
(123, 12, '角色删除',   3, NULL, NULL, NULL, 'system:role:delete',        3, 1, 1),
(124, 12, '角色授权',   3, NULL, NULL, NULL, 'system:role:assign',        4, 1, 1),
(131, 13, '菜单新增',   3, NULL, NULL, NULL, 'system:menu:add',           1, 1, 1),
(132, 13, '菜单修改',   3, NULL, NULL, NULL, 'system:menu:edit',          2, 1, 1),
(133, 13, '菜单删除',   3, NULL, NULL, NULL, 'system:menu:delete',        3, 1, 1);

-- 用户-角色关联（admin 为超级管理员，其余用户轮转分配运营/财务角色）
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2), (3, 2), (4, 3), (5, 2), (6, 3), (7, 2), (8, 3), (9, 2), (10, 3);

-- 角色-菜单关联（admin 拥有全部；运营可看仪表盘和用户管理；财务仅仪表盘）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu
UNION ALL
SELECT 2, m.id FROM sys_menu m WHERE m.id IN (1, 10, 11, 111, 112)
UNION ALL
SELECT 3, m.id FROM sys_menu m WHERE m.id IN (1, 10, 12)
UNION ALL
SELECT 4, 1;

-- 操作日志示例
INSERT INTO sys_operation_log (type, content, operator, create_time) VALUES
('用户', '新增用户 user009 完成',          '管理员',   '2024-06-01 09:12:30'),
('系统', '角色「财务人员」权限已更新',    '管理员',   '2024-06-01 11:03:48'),
('用户', '用户 user004 状态被禁用',       '管理员',   '2024-06-01 15:42:09'),
('系统', '系统数据备份完成',              '系统',     '2024-06-01 23:00:00');
