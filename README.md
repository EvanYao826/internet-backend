# Internet Backend

后台管理系统后端服务，为 [Vue3 前端](https://gitee.com/sun-lujia/internet)（本地目录 `internet/`）提供认证、用户、角色、菜单、仪表盘与个人中心接口。

## 技术栈

- Java 17 / Spring Boot 3.2.5
- Spring Security + JWT（jjwt 0.12）
- MyBatis-Plus 3.5.7 / MySQL 8
- Knife4j（OpenAPI 文档）
- JUnit 5 + MockMvc（48 个测试用例，H2 内存库，无需本地 MySQL）

## 快速开始

### 1. 启动 MySQL 并初始化数据

方式一（Docker，推荐）：

```bash
docker run -d --name internet-mysql -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=123456 -e MYSQL_DATABASE=internet_admin \
  mysql:8.0 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
# 等待约 30 秒后导入初始化脚本（建表 + 演示数据）
docker exec -i internet-mysql mysql -uroot -p123456 < sql/init.sql
```

方式二：本地 MySQL 8 执行 `sql/init.sql`（会自动创建 `internet_admin` 库）。

### 2. 启动后端

- IDEA：直接运行 `InternetApplication`（默认 dev 环境，连 `127.0.0.1:3306`）
- 或命令行：`mvn spring-boot:run`

连接参数与密钥均可用环境变量覆盖：

| 变量 | 默认值 | 说明 |
|---|---|---|
| `DB_HOST` / `DB_PORT` / `DB_NAME` | 127.0.0.1 / 3306 / internet_admin | 数据库连接 |
| `DB_USERNAME` / `DB_PASSWORD` | root / 123456 | 仅 dev 环境 |
| `JWT_SECRET` | 内置开发密钥 | **生产必须注入 ≥32 字节随机串** |
| `SERVER_PORT` | 8080 | 服务端口 |

### 3. 启动前端并登录

前端 `npm run dev` 后访问 http://localhost:5173（Vite 已配置 `/api` 代理到 8080）。

演示账号：**admin / 123456**；后端未启动时前端登录页也有 Mock 演示登录可用。

### 4. 接口文档

启动后访问 Knife4j 在线文档：http://127.0.0.1:8080/api/doc.html （支持 Bearer Token 调试）

## 运行测试

```bash
mvn test
```

测试基于 H2 内存库（`src/test/resources/schema.sql` + `data.sql`），不需要本地 MySQL。

## Docker Compose 一键部署

```bash
docker compose up -d --build
```

包含 MySQL（首次启动自动执行 `sql/init.sql`）与后端服务（健康检查就绪后启动），后端映射到宿主机 8080。生产环境请通过环境变量注入 `JWT_SECRET` 与数据库密码，并使用 `prod` 配置。

## 主要接口

| 模块 | 路径 |
|---|---|
| 认证 | `POST /auth/login`、`POST /auth/logout`、`GET /auth/userInfo` |
| 用户管理 | `/system/users`（分页/详情/增删改/状态/重置密码） |
| 角色管理 | `/system/roles`（分页/全量/增删改/菜单授权） |
| 菜单管理 | `/system/menus`（树/增删改） |
| 仪表盘 | `/dashboard/stats`、`/visitTrend`、`/categoryStats`、`/activities` |
| 个人中心 | `/profile/info`、`/profile/password` |

接口契约与前端 `src/api/types.ts` 逐字段对齐，详见 `API.md`。

## 验收标准核对（DEVELOPMENT.md §10）

| 验收项 | 状态 |
|---|---|
| 登录成功后能够访问受保护接口 | ✅ |
| 未携带或携带无效 Token 时返回 401 | ✅ 统一 JSON 响应 |
| 无权限用户访问受限接口时返回 403 | ✅ `@PreAuthorize` 后端强校验 |
| 用户、角色、菜单数据持久化到 MySQL | ✅ |
| 前端 Mock 关闭后通过 `/api` 完成主要页面联调 | ✅ 五个页面全部联调通过 |
| 接口文档、初始化 SQL、启动说明完整可用 | ✅ Knife4j + `sql/init.sql` + 本文档 |

## 已知限制与生产建议

- **仪表盘演示指标**：用户总数与最近动态为真实数据；订单/营收/访问量为演示值（无对应业务表），接入点见 `DashboardService` 注释。
- **登录失败锁定与 Token 黑名单为内存态**：单实例可用，集群部署请替换为 Redis。
- **改密码后当前 Token 保持有效**：与前端交互一致；需要全局失效时可引入“用户密码版本号”写入 JWT 校验。
- **重置密码固定为 123456**：与前端提示一致；生产环境建议改为随机临时密码或重置链接。
- 前端权限控制仅用于界面展示，所有权限校验已在后端接口层强制执行。
