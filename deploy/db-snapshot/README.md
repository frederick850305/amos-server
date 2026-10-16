# 数据库快照（开发移交用）

本目录存放 AMOS **开发库的一次性全量快照**，用于开发移交时让接手人快速拿到一份
「结构和测试数据都与开发机完全一致」的 PostgreSQL 数据库。

## 文件

| 文件 | 说明 |
|---|---|
| `amos_dev_snapshot_2026-10-16.sql` | `amos` 库全量快照：38 张业务表 + `flyway_schema_history` 的结构与全部数据（共 115 行） |

- 导出时间：2026-10-16
- 来源：PostgreSQL 17（容器 `pg17-local`，库 `amos`）
- 生成：`pg_dump -U postgres -d amos --format=plain --no-owner --no-privileges --clean --if-exists`
  （已剥离 PG17 的 `\restrict` / `\unrestrict` 元命令，便于用任意 SQL 客户端导入）
- 特性：`--clean --if-exists`，可重复导入到已有库（会先 DROP 再重建）

## 快速导入

```bash
# 1) 起 PG
docker compose -f deploy/postgres/docker-compose.yml up -d
# 2) 建空库
docker compose -f deploy/postgres/docker-compose.yml exec -T db \
  psql -U postgres -d postgres -c "create database amos"
# 3) 导入
docker compose -f deploy/postgres/docker-compose.yml exec -T db \
  psql -U postgres -d amos -v ON_ERROR_STOP=1 -f /dev/stdin \
  < deploy/db-snapshot/amos_dev_snapshot_2026-10-16.sql
```

校验（应输出 `39`）：

```bash
docker compose -f deploy/postgres/docker-compose.yml exec -T db \
  psql -U postgres -d amos -tAc \
  "select count(*) from information_schema.tables where table_schema='public'"
```

## 重要：这不是 schema 真相源

业务表结构的**唯一来源**仍是 Flyway 迁移 `amos-app/src/main/resources/db/migration/V*__*.sql`
（H2 / PostgreSQL 双兼容）。本快照仅是某一时点的导出物，**不要基于它改表结构**，
否则会与迁移文件产生漂移。新增/变更表一律新增 `V12__xxx.sql` 迁移。

日常开发其实**不需要本文件**：建空库后直接启动应用，Flyway 会自动重建出
与迁移文件一致的结构与种子数据（更干净、与文档描述相符）。本快照的价值在于
「想立刻看到开发机上的那 115 行联调数据」。

## 已知数据漂移

快照忠实保留了导出时开发库的状态，其中有两处**与迁移文件 V10 初始意图不一致**，
成因是联调期做过安装/拆卸等手工测试：

| 组件 | V10 意图 | 快照实际 |
|---|---|---|
| `C-TEST-01` | `Available`，未安装 | `In Use`，`function_no=FN-ENG-01`，且与 `FN-ENG-01.installed_component_no='C-TEST-10'` 双向不一致 |
| `C-TEST-03` | `Available`，未安装 | `In Use`，`function_no=FN-ENG-02`（与 function 侧自洽） |

此外 `component_function_history`(20)、`component_function_rotation`(7)、
`component_status_log`(10)、`counter_reading_log`(1) 为联调期运行时记录。

**做模块 06「安装/拆卸」等需要「未安装组件」的验收时不要用本快照**，
改为建空库 + 启动应用，让 Flyway 重建干净库；或先执行
`scripts/postgres/cleanup_component_test_data.sql` 清理。

## 兼容性

仅适用于 **PostgreSQL**。H2 请走 Flyway 迁移，不要使用本文件。