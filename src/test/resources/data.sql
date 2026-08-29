-- H2 测试种子数据（密码均为 123456 的 BCrypt 值）
INSERT INTO sys_role (id, role_name, role_code, sort, status, remark) VALUES
(1, '超级管理员', 'admin',    1, 1, '拥有所有权限'),
(2, '运营人员',   'operator', 2, 1, '负责日常运营'),
(3, '访客',       'guest',    3, 0, '只读权限');

INSERT INTO sys_user (id, username, nickname, email, phone, password, status) VALUES
(1, 'admin',   '超级管理员', 'admin@example.com',   '13800000000', '$2a$10$domzvzmkzvt5wEfKUVmn.ug3v8FaKuqmZbp2lZAgpyxX4.6SjDMCq', 1),
(2, 'user001', '张伟',       'user001@example.com', '13900000001', '$2a$10$domzvzmkzvt5wEfKUVmn.ug3v8FaKuqmZbp2lZAgpyxX4.6SjDMCq', 1),
(3, 'user002', '刘磊',       'user002@example.com', '13900000002', '$2a$10$domzvzmkzvt5wEfKUVmn.ug3v8FaKuqmZbp2lZAgpyxX4.6SjDMCq', 0);

INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, icon, permission, sort, status, visible) VALUES
(1,  0,  '仪表盘',   2, '/dashboard',   'dashboard/index',   'dashboard', 'dashboard:view',   1, 1, 1),
(10, 0,  '系统管理', 1, '/system',      NULL,                'system',    NULL,               2, 1, 1),
(11, 10, '用户管理', 2, '/system/user', 'system/user/index', 'user',      'system:user:view', 1, 1, 1),
(12, 10, '角色管理', 2, '/system/role', 'system/role/index', 'role',      'system:role:view', 2, 1, 1),
(13, 10, '菜单管理', 2, '/system/menu', 'system/menu/index', 'menu',      'system:menu:view', 3, 1, 1),
(20, 0,  '个人中心', 2, '/profile',     'profile/index',     'profile',   NULL,               3, 1, 0),
(111, 11, '用户新增',     3, NULL, NULL, NULL, 'system:user:add',           1, 1, 1),
(112, 11, '用户修改',     3, NULL, NULL, NULL, 'system:user:edit',          2, 1, 1),
(113, 11, '用户删除',     3, NULL, NULL, NULL, 'system:user:delete',        3, 1, 1),
(114, 11, '用户重置密码', 3, NULL, NULL, NULL, 'system:user:resetPassword', 4, 1, 1),
(121, 12, '角色新增',     3, NULL, NULL, NULL, 'system:role:add',           1, 1, 1),
(122, 12, '角色修改',     3, NULL, NULL, NULL, 'system:role:edit',          2, 1, 1),
(123, 12, '角色删除',     3, NULL, NULL, NULL, 'system:role:delete',        3, 1, 1),
(124, 12, '角色授权',     3, NULL, NULL, NULL, 'system:role:assign',        4, 1, 1),
(131, 13, '菜单新增',     3, NULL, NULL, NULL, 'system:menu:add',           1, 1, 1),
(132, 13, '菜单修改',     3, NULL, NULL, NULL, 'system:menu:edit',          2, 1, 1),
(133, 13, '菜单删除',     3, NULL, NULL, NULL, 'system:menu:delete',        3, 1, 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1), (2, 2), (3, 2);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu
UNION ALL
SELECT 2, m.id FROM sys_menu m WHERE m.id IN (1, 10, 11);

INSERT INTO sys_operation_log (type, content, operator) VALUES
('用户', '新增用户 user001 完成', 'admin');
