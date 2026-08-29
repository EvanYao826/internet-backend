# Internet Backend 接口文档

> 本文档根据前端 `src/api` 和 `src/api/types.ts` 整理。接口前缀为 `/api`，后端实际路由建议保留对应路径，例如前端调用 `/api/auth/login`。

## 1. 通用约定

### 请求地址

开发环境：`http://127.0.0.1:5173/api`，由 Vite 代理到后端 `http://127.0.0.1:8080`。

生产环境由部署网关或同域反向代理转发 `/api`。

### 请求头

除登录接口外，受保护接口需要携带：

```http
Authorization: Bearer <token>
Content-Type: application/json
```

### 统一响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

| code | 含义 |
|---:|---|
| 200 或 0 | 成功 |
| 400 | 参数或业务校验失败 |
| 401 | 未登录、Token 无效或已过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器异常 |

### 分页结构

```json
{
  "list": [],
  "total": 0,
  "page": 1,
  "pageSize": 10
}
```

分页参数：`page` 从 1 开始，`pageSize` 为每页数量。

## 2. 认证模块

### 2.1 用户登录

```http
POST /api/auth/login
```

请求体：

```json
{
  "username": "admin",
  "password": "123456"
}
```

成功返回：`data` 为 `LoginResult`。

```json
{
  "token": "eyJ...",
  "userInfo": {
    "id": 1,
    "username": "admin",
    "nickname": "超级管理员",
    "avatar": "",
    "email": "admin@example.com",
    "phone": "13800000000",
    "status": 1,
    "roles": ["admin"],
    "permissions": ["*"],
    "createTime": "2024-01-01 09:00:00"
  }
}
```

后端要求：校验账号状态、密码、登录失败次数和必要的登录日志；密码不能明文保存。

### 2.2 退出登录

```http
POST /api/auth/logout
```

成功返回：`data: null`。如使用无状态 JWT，建议将 Token 加入失效列表或通过短期 Token 控制有效期。

### 2.3 获取当前用户信息

```http
GET /api/auth/userInfo
```

成功返回：`data` 为当前用户信息，字段同登录返回的 `userInfo`。

## 3. 用户管理

### 3.1 用户分页列表

```http
GET /api/system/users
```

查询参数：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| page | number | 是 | 页码 |
| pageSize | number | 是 | 每页数量 |
| username | string | 否 | 用户名模糊查询 |
| status | number | 否 | 状态，1 启用，0 禁用 |
| deptId | number | 否 | 部门 ID |

成功返回 `PageResult<UserItem>`，用户字段包括：`id`、`username`、`nickname`、`email`、`phone`、`status`、`roleIds`、`roleName`、`deptId`、`deptName`、`remark`、`createTime`。

### 3.2 用户详情

```http
GET /api/system/users/{id}
```

路径参数：`id`，用户 ID。

成功返回 `UserItem`。不存在时返回 `404`。

### 3.3 新增用户

```http
POST /api/system/users
```

请求体：

```json
{
  "username": "zhangsan",
  "nickname": "张三",
  "email": "zhangsan@example.com",
  "phone": "13800000000",
  "password": "123456",
  "status": 1,
  "roleIds": [2],
  "deptId": 1,
  "remark": ""
}
```

成功返回创建后的 `UserItem`。用户名必须唯一，密码需要加密，角色 ID 必须校验存在。

### 3.4 修改用户

```http
PUT /api/system/users/{id}
```

请求体同新增用户。编辑时 `password` 可选，未传入时不修改密码。成功返回 `data: null`。

### 3.5 删除用户

```http
DELETE /api/system/users/{id}
```

成功返回 `data: null`。建议禁止删除当前登录用户和系统内置管理员。

### 3.6 修改用户状态

```http
PUT /api/system/users/{id}/status
```

请求体：

```json
{ "status": 1 }
```

`status`：`1` 启用，`0` 禁用。成功返回 `data: null`。

### 3.7 重置密码

```http
PUT /api/system/users/{id}/resetPassword
```

无请求体，成功返回 `data: null`。正式实现不建议固定返回或使用固定密码，应生成临时密码或重置链接，并记录操作日志。

## 4. 角色管理

### 4.1 角色分页列表

```http
GET /api/system/roles
```

查询参数：`page`、`pageSize`、`roleName`。成功返回 `PageResult<RoleItem>`。

角色字段：`id`、`roleName`、`roleCode`、`sort`、`status`、`remark`、`createTime`。

### 4.2 获取全部角色

```http
GET /api/system/roles/all
```

用于用户表单的角色下拉选择。成功返回 `RoleItem[]`。

### 4.3 新增角色

```http
POST /api/system/roles
```

请求体：

```json
{
  "roleName": "运营人员",
  "roleCode": "operator",
  "sort": 2,
  "status": 1,
  "remark": "负责日常运营"
}
```

`roleCode` 必须唯一。成功返回 `RoleItem`。

### 4.4 修改角色

```http
PUT /api/system/roles/{id}
```

请求体同新增角色。成功返回 `data: null`。

### 4.5 删除角色

```http
DELETE /api/system/roles/{id}
```

成功返回 `data: null`。删除前需要检查是否仍被用户使用，并保护系统内置角色。

### 4.6 获取角色菜单权限

```http
GET /api/system/roles/{id}/menus
```

成功返回菜单 ID 数组：

```json
[1, 10, 11, 12]
```

### 4.7 分配角色菜单权限

```http
PUT /api/system/roles/{id}/menus
```

请求体：

```json
{ "menuIds": [1, 10, 11, 12] }
```

后端需要校验菜单 ID 是否存在，并以事务方式更新角色菜单关联关系。

## 5. 菜单管理

### 5.1 菜单树

```http
GET /api/system/menus
```

成功返回 `MenuItem[]`，支持 `children` 嵌套。

菜单字段：`id`、`parentId`、`menuName`、`menuType`、`path`、`component`、`icon`、`permission`、`sort`、`status`、`visible`、`children`。

`menuType`：`1` 目录，`2` 菜单，`3` 按钮。

### 5.2 新增菜单

```http
POST /api/system/menus
```

请求体：

```json
{
  "parentId": 10,
  "menuName": "用户管理",
  "menuType": 2,
  "path": "/system/user",
  "component": "system/user/index",
  "icon": "user",
  "permission": "system:user:view",
  "sort": 1,
  "status": 1,
  "visible": 1
}
```

成功返回 `MenuItem`。目录、菜单、按钮应分别校验必填字段和父级关系。

### 5.3 修改菜单

```http
PUT /api/system/menus/{id}
```

请求体同新增菜单。成功返回 `data: null`。

### 5.4 删除菜单

```http
DELETE /api/system/menus/{id}
```

存在子菜单或角色关联时，后端应拒绝删除或明确执行级联策略。成功返回 `data: null`。

## 6. 仪表盘

以下接口均需要登录，成功返回 `code: 200`。

### 6.1 顶部统计

```http
GET /api/dashboard/stats
```

返回字段：`userTotal`、`userGrowth`、`orderTotal`、`orderGrowth`、`revenue`、`revenueGrowth`、`visitTotal`、`visitGrowth`。

### 6.2 访问趋势

```http
GET /api/dashboard/visitTrend
```

返回：

```json
{
  "dates": ["6/1", "6/2"],
  "series": [
    { "name": "访问量", "data": [1200, 1350] },
    { "name": "订单量", "data": [280, 310] }
  ]
}
```

### 6.3 分类统计

```http
GET /api/dashboard/categoryStats
```

返回：`categories: string[]` 和 `values: number[]`，两个数组下标一一对应。

### 6.4 最近动态

```http
GET /api/dashboard/activities
```

返回 `ActivityItem[]`，字段为 `id`、`type`、`content`、`operator`、`createTime`。

## 7. 个人中心

### 7.1 获取个人信息

```http
GET /api/profile/info
```

成功返回当前登录用户的 `UserInfo`。

### 7.2 修改个人信息

```http
PUT /api/profile/info
```

请求体：

```json
{
  "nickname": "新昵称",
  "email": "new@example.com",
  "phone": "13800000000"
}
```

成功返回 `data: null`。邮箱和手机号需要格式及唯一性校验。

### 7.3 修改密码

```http
PUT /api/profile/password
```

请求体：

```json
{
  "oldPassword": "旧密码",
  "newPassword": "新密码"
}
```

成功返回 `data: null`。需要校验旧密码、密码强度，并建议修改成功后使现有 Token 失效。

## 8. 后端实现注意事项

- 前端 Mock 仅用于演示，后端必须重新完成认证、权限和参数校验。
- `system:*`、`profile:*` 等权限应在后端接口层强制校验。
- 所有新增、修改、删除和授权操作建议写入操作日志。
- 列表接口需要限制最大 `pageSize`，避免一次查询过多数据。
- 返回用户信息时不要返回密码、密码哈希或敏感认证字段。
- 错误信息应适合前端展示，但不要暴露 SQL、堆栈和内部配置。
