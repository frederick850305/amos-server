-- ============================================================
-- AMOS 本地 PostgreSQL：数据库 / 用户 / 授权初始化
-- ------------------------------------------------------------
-- 用途：仅在本机首次搭建数据库时执行一次（创建业务库与专用账号）。
--       业务表结构不在这里维护，统一交给 Flyway（db/migration/*）。
--
-- 用法（需以超级用户连接，本机用容器默认的 postgres 账号）：
--   docker compose -f ~/postgres-local/docker-compose.yml exec -T db \
--     psql -U postgres -d postgres -f /dev/stdin < scripts/postgres/00_create_database.sql
-- 或把本文件挂载进容器后执行，例如：
--   docker compose -f ~/postgres-local/docker-compose.yml exec -T db \
--     psql -U postgres -d postgres -c "\i /var/lib/postgresql/00_create_database.sql"
--
-- 说明：
--   - 业务库名 = amos（application.yml 中 postgres profile 连接的就是它）
--   - 应用连接账号 = amos_app，密码见下方（本地开发用，切勿用于生产）
--   - 若库 / 用户已存在则跳过，可重复安全执行
-- ============================================================

-- 1) 业务库
do $$
begin
  if not exists (select 1 from pg_database where datname = 'amos') then
    execute 'create database amos';
  end if;
end
$$;

-- 2) 专用应用账号（避免直接用超级用户 postgres 连应用）
do $$
begin
  if not exists (select 1 from pg_roles where rolname = 'amos_app') then
    execute 'create role amos_app login password ''amos_app''';
  end if;
end
$$;

-- 3) 授权：amo_app 可操作 amos 库及其中的对象
grant connect, temp on database amos to amos_app;
grant all privileges on schema public to amos_app;
grant all privileges on all tables    in schema public to amos_app;
grant all privileges on all sequences  in schema public to amos_app;
-- 后续 Flyway 新建的表/序列也默认归 amos_app（公共模式 owner 已授权）
alter default privileges in schema public
  grant all privileges on tables    to amos_app;
alter default privileges in schema public
  grant all privileges on sequences to amos_app;
