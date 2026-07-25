# System Foundation Rewrite

## 1. Module Goal

建立 AMOS 后端的系统基础能力：安装地点、部门、用户、角色、用户可访问工作范围、登录态、当前 Installation/Department 上下文。

这是所有后续模块的前置基础。AMOS 中很多记录都必须按 Installation 和 Department 隔离或过滤。

## 2. Required Inputs

必须先读取：

- `docs/database-rewrite-guide.md`
- `docs/amos-rewrite-plan.md`
- `docs/api-contract.md`
- `docs/data-model.md`
- `docs/manual-mapping.md`
- 前端 `src/store.js`
- 前端 `src/data/amosData.js`
- 后端 `amos-common/src/main/java/com/neusoft/amos/common/*`
- 后端 `amos-app/src/main/resources/application.yml`
- 后端 `amos-app/src/main/resources/db/migration/V1__init.sql`

## 3. Manual Scope

手册 Chapter 1：

- How to Start AMOS Business Suite，printed page 2
- Installations and Departments，printed page 20
- Switching Departments，printed page 20
- Locking the Application，printed page 19
- Selecting or Changing the View，printed page 18

## 4. Frontend Scope

相关前端能力：

- `store.installation`
- `store.department`
- `setInstallation`
- `setDepartment`
- `scopeByDepartment`
- Switch Department dialog
- Lock Application dialog
- Options 中的用户偏好

## 5. Backend Tasks

新增或完善：

- `system` 包
- Installation entity/repository/service/controller
- Department entity/repository/service/controller
- User entity/repository/service/controller
- Role entity
- UserDepartmentAccess entity
- 登录接口对接真实用户表
- 当前用户可访问 installation/department 查询接口

## 6. Database Tables

建议新增 migration：`V3__system_scope.sql`

表：

- `installation`
- `department`
- `amos_user`
- `role`
- `user_role`
- `user_department_access`
- `user_option`

关键约束：

- `installation.code` unique
- `department(installation_id, code)` unique
- `amos_user.username` unique
- `user_department_access(user_id, installation_id, department_id)` unique

## 7. API

```text
GET  /api/system/installations
GET  /api/system/departments?installation=Traveller
GET  /api/system/me
GET  /api/system/me/scopes
GET  /api/system/me/options
PUT  /api/system/me/options
POST /api/auth/login
```

## 8. Acceptance Criteria

- 前端 Installation/Department 可从后端加载。
- 当前用户只能切换到有权限的范围。
- 后端业务接口后续可通过请求上下文获得当前 scope。
- H2 和 PostgreSQL migration 均成功。
- 登录接口不再只是假数据。

## 9. LLM Prompt

```text
请按照 docs/module-rewrites/01-system-foundation-rewrite.md 开发 System Foundation。
先分析前端 store 和 amosData 中的 installation/department/user/options 结构，再设计 Flyway、实体、API 和测试。
不要改无关模块，不执行任何批量删除操作。
```

