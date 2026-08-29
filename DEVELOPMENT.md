# Internet Backend 项目开发文档

## 1. 项目概述

本项目为后台管理系统后端，为现有 Vue 3 前端提供用户认证、用户管理、角色权限、菜单管理、个人中心和仪表盘数据接口。

项目目标：

- 提供稳定、清晰、可维护的 RESTful API。
- 使用 RBAC 模型管理用户、角色、菜单和操作权限。
- 支持本地开发、测试和生产环境配置。
- 与前端统一响应格式、认证方式和错误处理规则。

## 2. 技术栈

- Java 17
- Spring Boot 3.x
- Maven
- MySQL 8.x
- MyBatis-Plus
- Spring Security
- JWT
- Lombok
- Knife4j / OpenAPI
- JUnit 5、Spring Boot Test

## 3. 项目结构

```text
internet-backend/
├─ src/main/java/com/example/internet/
│  ├─ common/              # 通用响应、异常、常量、工具类
│  ├─ config/              # Security、MyBatis-Plus、Knife4j 等配置
│  ├─ controller/          # REST 接口层
│  ├─ service/             # 业务逻辑层
│  ├─ mapper/              # 数据访问层
│  ├─ entity/              # 数据库实体
│  ├─ dto/                 # 请求参数对象
│  ├─ vo/                  # 返回对象
│  └─ security/             # JWT 和认证相关代码
├─ src/main/resources/
│  ├─ mapper/              # MyBatis XML（如有需要）
│  ├─ application.yml
│  ├─ application-dev.yml
│  └─ application-prod.yml
├─ sql/                    # 数据库初始化和升级脚本
├─ docs/                   # 接口和设计补充文档
├─ pom.xml
└─ README.md
```

## 4. 核心模块

### 认证模块

- 用户名密码登录
- JWT Access Token 签发与校验
- 获取当前用户信息
- 退出登录
- 修改密码

### 用户管理

- 用户分页查询、详情、新增、修改和删除
- 启用/禁用用户
- 重置密码
- 用户角色分配

### 角色权限

- 角色分页查询、新增、修改和删除
- 角色状态管理
- 为角色分配菜单和按钮权限

### 菜单管理

- 菜单树查询
- 目录、菜单、按钮的新增、修改和删除
- 菜单可见性、状态、排序和权限标识管理

### 仪表盘和个人中心

- 首页统计数据、访问趋势、分类统计和最近动态
- 当前用户资料查询和修改

## 5. 接口约定

接口前缀：`/api`

统一成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

主要状态：

- `200`：请求成功
- `400`：参数或业务校验失败
- `401`：未认证或 Token 已失效
- `403`：无操作权限
- `404`：资源不存在
- `500`：服务器异常

认证请求使用：

```text
Authorization: Bearer <token>
```

## 6. 数据库初步规划

核心表建议包括：

- `sys_user`：用户
- `sys_role`：角色
- `sys_menu`：菜单和按钮权限
- `sys_user_role`：用户与角色关联
- `sys_role_menu`：角色与菜单关联
- `sys_operation_log`：操作日志

密码必须使用 BCrypt 等单向算法加密保存，不得保存明文密码。用户、角色和菜单删除操作需要考虑关联数据及审计记录。

## 7. 权限设计

采用 RBAC：

```text
用户 -> 角色 -> 菜单/按钮权限
```

权限标识示例：

```text
system:user:view
system:user:add
system:user:edit
system:user:delete
```

权限校验必须在后端完成，前端权限控制仅用于界面展示，不能作为安全边界。

## 8. 开发阶段

### 第一阶段：项目基础

- 创建 Spring Boot 项目和 Maven 配置
- 配置 MySQL、MyBatis-Plus 和多环境文件
- 增加统一响应、异常处理和参数校验
- 创建数据库初始化脚本

### 第二阶段：认证与权限

- 完成登录、JWT、当前用户信息和退出登录
- 完成用户、角色、菜单及关联关系
- 增加 Security 权限拦截

### 第三阶段：前端接口联调

- 完成用户、角色、菜单、个人中心接口
- 完成仪表盘接口
- 按前端类型定义统一字段和分页结构

### 第四阶段：质量与部署

- 增加接口测试和核心业务单元测试
- 增加操作日志和安全配置
- 完善 Knife4j 文档
- 增加 Docker Compose 和生产部署说明

## 9. 本地运行要求

- JDK 17+
- Maven 3.9+
- MySQL 8+
- 数据库：建议使用 `internet_admin`
- 后端默认端口：`8080`
- 前端开发代理：`http://127.0.0.1:8080`

建议使用环境变量或本地配置文件管理数据库密码和 JWT 密钥，不要将真实凭据提交到 Git。

## 10. 验收标准

- 登录成功后能够访问受保护接口。
- 未携带或携带无效 Token 时返回 `401`。
- 无权限用户访问受限接口时返回 `403`。
- 用户、角色、菜单数据能够持久化到 MySQL。
- 前端 Mock 关闭后，可以通过 `/api` 正常完成主要页面联调。
- 接口文档、初始化 SQL 和启动说明完整可用。
