# AMOS 开发环境移交说明

本文面向接手 AMOS 开发的同事，覆盖两个仓库、本地数据库、启动步骤与常见故障。
**所有交付物都在 GitHub 上，克隆即可获得完整可运行环境，无需另行索取代码或SQL 文件。**

## 1. 交付清单

| 交付物 | 位置 | 说明 |
|---|---|---|
| 后端代码 | `https://github.com/frederick850305/amos-server` | Spring Boot 3.2.5，main分支即最新 |
| 前端代码 | `https://github.com/frederick850305/amos` | Vue 3 + Vite，main 分支即最新 |
| 数据库结构与种子 | 后端仓库 `amos-app/src/main/resources/db/migration/V1__init.sql` ~ `V11__counter_measure_point_logs.sql` | **唯一 schema 来源**，Flyway 自动执行，H2/PG 双兼容 |
| 建库脚本 | 后端仓库 `scripts/postgres/00_create_database.sql` | 仅建库/建角色，不含业务表 |
| 本地 PG 容器编排 | 后端仓库 `deploy/postgres/docker-compose.yml`（+ `.env.example`） | PostgreSQL 17，零配置可跑 |
| 测试数据清理 | 后端仓库 `scripts/postgres/cleanup_component_test_data.sql` | 删除 V10 造的 C-TEST 数据 |
| 数据库搭建详解 | 后端仓库 `docs/postgres-setup.md` | Flyway 约定、双跑验证、新增表规范 |

数据库脚本**不建议另外导出 SQL/dump**：克隆后端仓库 + 首次启动，Flyway 会重建出与开发机完全一致的结构与种子数据（开发机当前 `flyway_schema_history` 为 V1~V11 全 success，与仓库文件无漂移）。

## 2. 环境依赖

| 依赖 | 版本 | 备注 |
|---|---|---|
| JDK | **17**（推荐 Temurin 17） | Spring Boot 3.2.5 所需。仓库根 `.java-version` 记录该版本 |
| Maven | 3.8+ | 多模块：`amos-app`、`amos-common` |
| Node.js | 20+（本机实测 24.15.0） |仅前端需要 |
| Docker |任意近期版本 | 跑 PostgreSQL 17；macOS 推荐 OrbStack 或 Docker Desktop |
| PostgreSQL | 由容器提供，无需本机安装 |连接 `localhost:5432`，账号 `postgres/postgres`（**仅本地开发**） |

> macOS 无需手动 `export JAVA_HOME`：`scripts/start.sh` 会读取 `.java-version` 自动指向本机 Temurin。

## 3. 拉起后端

```bash
git clone https://github.com/frederick850305/amos-server.git
cd amos-server

# 1) 起本地 PostgreSQL（首次会拉取 postgres:17 镜像）
docker compose -f deploy/postgres/docker-compose.yml up -d

# 2) 首次创建业务库 amos 与应用角色 amos_app（可重复执行）
docker compose -f deploy/postgres/docker-compose.yml exec -T db \
  psql -U postgres -d postgres -f /dev/stdin < scripts/postgres/00_create_database.sql

# 3) 启动（默认 postgres profile；start.sh 会自动设JAVA_HOME、检测端口占用、探测 PG）
./scripts/start.sh
```

启动日志应出现 `Started AmosServerApplication`，健康检查：

```bash
curl http://localhost:8080/actuator/health     # => {"status":"UP"}
```

**免 PostgreSQL 的快速验证**（内置 H2 内存库，跑同一套迁移，用于演示或 CI）：

```bash
./scripts/start.sh --spring.profiles.active=default --server.port=8081
```

## 4. 拉起前端

```bash
git clone https://github.com/frederick850305/amos.git
cd amos
npm i
npm run dev        # http://localhost:5280
```

- `/api` 由 Vite 代理转发到 `http://localhost:8080`；后端换端口时用环境变量覆盖：
  `VITE_API_TARGET=http://localhost:8081 npm run dev`
- 登录账号：**`admin` / `admin`**（由后端 `SystemSeedRunner` 幂等注入，种子数据仅 Traveller / ER 部门）
- 另有「演示模式」可无后端浏览前端（数据走本地 Mock）

## 5. 端口一览

| 服务 | 端口 | 说明 |
|---|---|---|
| amos-server | 8080 | `start.sh` 会先检测占用并自动释放（SIGTERM → SIGKILL） |
| amos 前端 dev | 5280 | Vite，`strictPort: false` 被占用会自动顺延 |
| PostgreSQL | 5432 | 容器 `pg17-local` |

## 6. 常见故障

| 现象 | 原因 | 处理 |
|---|---|---|
| `Connection to localhost:5432 refused` / `flywayInitializer` BeanCreationException | PG 容器没起或 Docker daemon 没运行 | `open -a OrbStack`（macOS）后 `docker compose -f deploy/postgres/docker-compose.yml up -d` |
| `APPLICATION FAILED TO START` + 端口相关 | 8080 被上一轮实例占用 | 直接重跑 `./scripts/start.sh`（自动释放）；手动：`lsof -i :8080` 后 `kill -9 <PID>` |
| Flyway checksum mismatch | 编辑过已应用的迁移文件 | 不要改已发布迁移，新增 `V12__xxx.sql`；开发库必要时删除 `flyway_schema_history` 中该版本行让其幂等重放 |
| 依赖 `amos-common` 的改动不生效 | 该模块以jar 形式被 `amos-app` 引用 | `mvn -pl amos-common install`，或用 reactor 构建 `mvn -pl amos-app -am test` |
| 前端接口 404/502 | 后端未启动或端口非 8080 | 确认后端健康；必要时设 `VITE_API_TARGET` |

## 7. 开发流程与协作约定

- 两仓均以 `main` 为主分支，**走 GitHub Flow**：功能分支累积 commit → push → 开 PR → squash 合并，**不在本地 merge 进 main**。
- 分支命名：`feature/module-xx-xxx`、`fix/xxx`、`chore/xxx`。
- 后端切片测试用 H2（`@ActiveProfiles("test")`），示例：`CounterSliceTest`、`RegisterSliceTest`、`ComponentSliceTest`。
  ```bash
  mvn -pl amos-app -am test
  ```
- 数据库变更**一律新增 Flyway 迁移**，全部写成 H2/PG 双兼容且幂等（`IF NOT EXISTS` / `WHERE NOT EXISTS`），
  规范详见 `docs/postgres-setup.md` 第 6 节。禁止维护第二套 schema 文件、禁止 `ddl-auto: update`。
- 业务模块的设计文档在 `docs/`：`amos-rewrite-plan.md`（总计划）、`api-contract.md`、`data-model.md`、
  `manual-mapping.md`、`database-rewrite-guide.md`，以及 `docs/module-rewrites/` 下各模块指南。

## 8. 当前进度（截至移交）

已完成模块：System Foundation（登录/作用域）、Registers、Component Types、Components、
Functions、Component Installation、**Counters & Measure Points**。

未完成/降级项（接手可优先处理）：
- 前端 `ComponentsView` 之外的个别业务窗口仍走本地 Mock，未完全后端化
- 校验错误提示未统一为`{code,message,data}` 信封格式（后端已返回，前端 `apiFetch` 兼容两种）
- 无 CI（GitHub Actions），构建与测试目前靠本地执行