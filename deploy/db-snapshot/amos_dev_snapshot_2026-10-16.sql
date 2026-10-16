-- ============================================================================
-- AMOS 开发库全量快照（PostgreSQL 17）
-- ----------------------------------------------------------------------------
-- 导出时间：2026-10-16
-- 来源库：  postgres@localhost:5432/amos（容器 pg17-local）
-- 内容：    public schema 下 38 张业务表 + flyway_schema_history 的
--           【表结构 + 全部现有数据】，共 115 行数据
--           （含注册表、组件类型、组件、功能位置、计数器、测点、读数日志、
--             功能位置履历等联调期测试数据）
-- 生成命令：pg_dump -U postgres -d amos --format=plain --no-owner \
--             --no-privileges --clean --if-exists
--           并剥离了 PG17 的 \restrict / \unrestrict 元命令，
--           以便用任意 SQL 客户端导入（不限于 psql）。
--
-- 【这是什么】
--   开发环境的一次性快照，用于开发移交时快速获得「带测试数据」的数据库。
--   它【不是】可维护的 schema 真相源 —— 业务表结构的唯一来源仍是
--   amos-app/src/main/resources/db/migration/V*__*.sql（Flyway，H2/PG 双兼容）。
--   日常开发不需要本文件；只有「想直接拿到和开发机一模一样的带数据库」时才用它。
--
-- 【怎么导入】
--   1) 起 PG 容器：
--        docker compose -f deploy/postgres/docker-compose.yml up -d
--   2) 建空库 amos（若已存在可跳过）：
--        docker compose -f deploy/postgres/docker-compose.yml exec -T db \
--          psql -U postgres -d postgres -c "create database amos"
--   3) 导入（推荐 psql，带 ON_ERROR_STOP 才能暴露错误）：
--        docker compose -f deploy/postgres/docker-compose.yml exec -T db \
--          psql -U postgres -d amos -v ON_ERROR_STOP=1 -f /dev/stdin \
--          < deploy/db-snapshot/amos_dev_snapshot_2026-10-16.sql
--      也可先复制进容器再执行：
--        docker cp deploy/db-snapshot/amos_dev_snapshot_2026-10-16.sql pg17-local:/tmp/snap.sql
--        docker exec pg17-local psql -U postgres -d amos -v ON_ERROR_STOP=1 -f /tmp/snap.sql
--   4) 校验，应输出 39（38 业务表 + flyway_schema_history）：
--        docker compose -f deploy/postgres/docker-compose.yml exec -T db \
--          psql -U postgres -d amos -tAc \
--          "select count(*) from information_schema.tables where table_schema='public'"
--      再确认迁移历史完整（应见 1..11 且全为 t）：
--        docker compose -f deploy/postgres/docker-compose.yml exec -T db \
--          psql -U postgres -d amos -c \
--          "select version, description, success from flyway_schema_history order by version"
--
-- 【导入后为何不会重复跑迁移】
--   本快照包含 flyway_schema_history 的 V1~V11 记录，其 checksum 与本仓库迁移文件一致，
--   因此应用启动时 Flyway 判定库已是最新，不会重复执行迁移。
--
-- 【已知数据漂移，导入后请知悉】
--   以下为导出时开发库的真实状态，与迁移文件 V10 的初始意图不一致，
--   原因是联调期做过安装/拆卸等手工测试，并非迁移种子：
--     - C-TEST-01：V10 意图为 Available / 未安装；快照中为 In Use / function_no=FN-ENG-01，
--       且与 maintenance_function.FN-ENG-01.installed_component_no='C-TEST-10' 双向不一致。
--     - C-TEST-03：V10 意图为 Available / 未安装；快照中为 In Use / function_no=FN-ENG-02（自洽）。
--     - component_function_history(20) / component_function_rotation(7) /
--       component_status_log(10) / counter_reading_log(1) 含联调期运行时记录。
--   因此本快照适合「直接跑起来看数据 / 演示」；
--   若要做模块 06 安装拆卸等需要「未安装组件」的验收，
--   请不要用本快照，改为建空库后直接启动应用，让 Flyway 重建出与 V10 意图一致的库。
--   （清理脚本：scripts/postgres/cleanup_component_test_data.sql）
--
-- 【兼容性】
--   仅适用于 PostgreSQL。H2 请走 Flyway 迁移，不要使用本文件。
-- ============================================================================--
-- ===== 以下为 pg_dump 原始输出 =====

--
-- PostgreSQL database dump
--


-- Dumped from database version 17.10 (Debian 17.10-1.pgdg13+1)
-- Dumped by pg_dump version 17.10 (Debian 17.10-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

ALTER TABLE IF EXISTS ONLY public.stock_type_vendor DROP CONSTRAINT IF EXISTS stock_type_vendor_stock_type_id_fkey;
ALTER TABLE IF EXISTS ONLY public.stock_type_maker DROP CONSTRAINT IF EXISTS stock_type_maker_stock_type_id_fkey;
ALTER TABLE IF EXISTS ONLY public.stock_grade DROP CONSTRAINT IF EXISTS stock_grade_stock_type_id_fkey;
ALTER TABLE IF EXISTS ONLY public.function_counter DROP CONSTRAINT IF EXISTS function_counter_function_id_fkey;
ALTER TABLE IF EXISTS ONLY public.user_role DROP CONSTRAINT IF EXISTS fk_user_role_user;
ALTER TABLE IF EXISTS ONLY public.user_role DROP CONSTRAINT IF EXISTS fk_user_role_role;
ALTER TABLE IF EXISTS ONLY public.user_option DROP CONSTRAINT IF EXISTS fk_user_option_user;
ALTER TABLE IF EXISTS ONLY public.user_department_access DROP CONSTRAINT IF EXISTS fk_uda_user;
ALTER TABLE IF EXISTS ONLY public.user_department_access DROP CONSTRAINT IF EXISTS fk_uda_installation;
ALTER TABLE IF EXISTS ONLY public.user_department_access DROP CONSTRAINT IF EXISTS fk_uda_department;
ALTER TABLE IF EXISTS ONLY public.location_register DROP CONSTRAINT IF EXISTS fk_location_parent;
ALTER TABLE IF EXISTS ONLY public.department DROP CONSTRAINT IF EXISTS fk_department_installation;
ALTER TABLE IF EXISTS ONLY public.component_type_stock_type DROP CONSTRAINT IF EXISTS fk_ctst_type;
ALTER TABLE IF EXISTS ONLY public.component_type_stock_type DROP CONSTRAINT IF EXISTS fk_ctst_stock;
ALTER TABLE IF EXISTS ONLY public.component_type_related_type DROP CONSTRAINT IF EXISTS fk_ctrt_type;
ALTER TABLE IF EXISTS ONLY public.component_type_related_type DROP CONSTRAINT IF EXISTS fk_ctrt_related;
ALTER TABLE IF EXISTS ONLY public.component_type_measure_point_def DROP CONSTRAINT IF EXISTS fk_ctmpd_type;
ALTER TABLE IF EXISTS ONLY public.component_type_counter_def DROP CONSTRAINT IF EXISTS fk_ctcd_type;
ALTER TABLE IF EXISTS ONLY public.component_type_children DROP CONSTRAINT IF EXISTS fk_ctc_parent;
ALTER TABLE IF EXISTS ONLY public.component_type_children DROP CONSTRAINT IF EXISTS fk_ctc_child;
ALTER TABLE IF EXISTS ONLY public.component_type DROP CONSTRAINT IF EXISTS fk_component_type_parent;
ALTER TABLE IF EXISTS ONLY public.component_measure_point DROP CONSTRAINT IF EXISTS component_measure_point_component_id_fkey;
ALTER TABLE IF EXISTS ONLY public.component_function_rotation DROP CONSTRAINT IF EXISTS component_function_rotation_function_id_fkey;
ALTER TABLE IF EXISTS ONLY public.component_counter DROP CONSTRAINT IF EXISTS component_counter_component_id_fkey;
DROP INDEX IF EXISTS public.idx_vendor_register_status;
DROP INDEX IF EXISTS public.idx_vendor_register_name;
DROP INDEX IF EXISTS public.idx_unit_register_status;
DROP INDEX IF EXISTS public.idx_unit_register_name;
DROP INDEX IF EXISTS public.idx_trade_status;
DROP INDEX IF EXISTS public.idx_trade_name;
DROP INDEX IF EXISTS public.idx_mpl_reading_date;
DROP INDEX IF EXISTS public.idx_mpl_measure_point;
DROP INDEX IF EXISTS public.idx_mpl_function;
DROP INDEX IF EXISTS public.idx_mpl_component;
DROP INDEX IF EXISTS public.idx_mf_status;
DROP INDEX IF EXISTS public.idx_mf_parent;
DROP INDEX IF EXISTS public.idx_mf_installation;
DROP INDEX IF EXISTS public.idx_mf_department;
DROP INDEX IF EXISTS public.idx_mf_criticality;
DROP INDEX IF EXISTS public.idx_mf_comp;
DROP INDEX IF EXISTS public.idx_maker_register_status;
DROP INDEX IF EXISTS public.idx_maker_register_name;
DROP INDEX IF EXISTS public.idx_location_register_status;
DROP INDEX IF EXISTS public.idx_location_register_parent;
DROP INDEX IF EXISTS public.idx_location_register_name;
DROP INDEX IF EXISTS public.idx_location_register_installation;
DROP INDEX IF EXISTS public.idx_job_class_status;
DROP INDEX IF EXISTS public.idx_job_class_name;
DROP INDEX IF EXISTS public.idx_function_criticality_active;
DROP INDEX IF EXISTS public.idx_fc_function;
DROP INDEX IF EXISTS public.idx_discipline_status;
DROP INDEX IF EXISTS public.idx_discipline_name;
DROP INDEX IF EXISTS public.idx_currency_register_status;
DROP INDEX IF EXISTS public.idx_currency_register_name;
DROP INDEX IF EXISTS public.idx_ctst_type;
DROP INDEX IF EXISTS public.idx_ctrt_type;
DROP INDEX IF EXISTS public.idx_ctmpd_type;
DROP INDEX IF EXISTS public.idx_ctcd_type;
DROP INDEX IF EXISTS public.idx_ctc_parent;
DROP INDEX IF EXISTS public.idx_ctc_child;
DROP INDEX IF EXISTS public.idx_csl_component_no;
DROP INDEX IF EXISTS public.idx_csl_component;
DROP INDEX IF EXISTS public.idx_crl_reading_date;
DROP INDEX IF EXISTS public.idx_crl_function;
DROP INDEX IF EXISTS public.idx_crl_counter;
DROP INDEX IF EXISTS public.idx_crl_component;
DROP INDEX IF EXISTS public.idx_component_status;
DROP INDEX IF EXISTS public.idx_component_parent;
DROP INDEX IF EXISTS public.idx_component_installation;
DROP INDEX IF EXISTS public.idx_component_department;
DROP INDEX IF EXISTS public.idx_cmp_component;
DROP INDEX IF EXISTS public.idx_cfr_function;
DROP INDEX IF EXISTS public.idx_cfr_component;
DROP INDEX IF EXISTS public.idx_cfr_compno;
DROP INDEX IF EXISTS public.idx_cfh_component_no;
DROP INDEX IF EXISTS public.idx_cfh_component;
DROP INDEX IF EXISTS public.idx_ca_component_no;
DROP INDEX IF EXISTS public.idx_budget_code_status;
DROP INDEX IF EXISTS public.idx_budget_code_name;
DROP INDEX IF EXISTS public.flyway_schema_history_s_idx;
ALTER TABLE IF EXISTS ONLY public.vendor_register DROP CONSTRAINT IF EXISTS vendor_register_vendor_no_key;
ALTER TABLE IF EXISTS ONLY public.vendor_register DROP CONSTRAINT IF EXISTS vendor_register_pkey;
ALTER TABLE IF EXISTS ONLY public.user_role DROP CONSTRAINT IF EXISTS user_role_pkey;
ALTER TABLE IF EXISTS ONLY public.user_option DROP CONSTRAINT IF EXISTS user_option_pkey;
ALTER TABLE IF EXISTS ONLY public.user_department_access DROP CONSTRAINT IF EXISTS user_department_access_pkey;
ALTER TABLE IF EXISTS ONLY public.location_register DROP CONSTRAINT IF EXISTS uq_location_install_code;
ALTER TABLE IF EXISTS ONLY public.maintenance_function DROP CONSTRAINT IF EXISTS uq_function_install_no;
ALTER TABLE IF EXISTS ONLY public.component_type_stock_type DROP CONSTRAINT IF EXISTS uq_ctst_pair;
ALTER TABLE IF EXISTS ONLY public.component_type_related_type DROP CONSTRAINT IF EXISTS uq_ctrt_pair;
ALTER TABLE IF EXISTS ONLY public.component_type_measure_point_def DROP CONSTRAINT IF EXISTS uq_ctmpd_pair;
ALTER TABLE IF EXISTS ONLY public.component_type_counter_def DROP CONSTRAINT IF EXISTS uq_ctcd_pair;
ALTER TABLE IF EXISTS ONLY public.component_type_children DROP CONSTRAINT IF EXISTS uq_ctc_pair;
ALTER TABLE IF EXISTS ONLY public.unit_register DROP CONSTRAINT IF EXISTS unit_register_pkey;
ALTER TABLE IF EXISTS ONLY public.unit_register DROP CONSTRAINT IF EXISTS unit_register_code_key;
ALTER TABLE IF EXISTS ONLY public.user_option DROP CONSTRAINT IF EXISTS uk_user_option;
ALTER TABLE IF EXISTS ONLY public.user_department_access DROP CONSTRAINT IF EXISTS uk_uda;
ALTER TABLE IF EXISTS ONLY public.role DROP CONSTRAINT IF EXISTS uk_role_code;
ALTER TABLE IF EXISTS ONLY public.installation DROP CONSTRAINT IF EXISTS uk_installation_code;
ALTER TABLE IF EXISTS ONLY public.department DROP CONSTRAINT IF EXISTS uk_department_inst_code;
ALTER TABLE IF EXISTS ONLY public.amos_user DROP CONSTRAINT IF EXISTS uk_amos_user_username;
ALTER TABLE IF EXISTS ONLY public.trade DROP CONSTRAINT IF EXISTS trade_pkey;
ALTER TABLE IF EXISTS ONLY public.trade DROP CONSTRAINT IF EXISTS trade_code_key;
ALTER TABLE IF EXISTS ONLY public.stock_type_vendor DROP CONSTRAINT IF EXISTS stock_type_vendor_pkey;
ALTER TABLE IF EXISTS ONLY public.stock_type DROP CONSTRAINT IF EXISTS stock_type_stock_type_no_key;
ALTER TABLE IF EXISTS ONLY public.stock_type DROP CONSTRAINT IF EXISTS stock_type_pkey;
ALTER TABLE IF EXISTS ONLY public.stock_type_maker DROP CONSTRAINT IF EXISTS stock_type_maker_pkey;
ALTER TABLE IF EXISTS ONLY public.stock_grade DROP CONSTRAINT IF EXISTS stock_grade_pkey;
ALTER TABLE IF EXISTS ONLY public.role DROP CONSTRAINT IF EXISTS role_pkey;
ALTER TABLE IF EXISTS ONLY public.measure_point_reading_log DROP CONSTRAINT IF EXISTS measure_point_reading_log_pkey;
ALTER TABLE IF EXISTS ONLY public.maker_register DROP CONSTRAINT IF EXISTS maker_register_pkey;
ALTER TABLE IF EXISTS ONLY public.maker_register DROP CONSTRAINT IF EXISTS maker_register_code_key;
ALTER TABLE IF EXISTS ONLY public.maintenance_function DROP CONSTRAINT IF EXISTS maintenance_function_pkey;
ALTER TABLE IF EXISTS ONLY public.location_register DROP CONSTRAINT IF EXISTS location_register_pkey;
ALTER TABLE IF EXISTS ONLY public.job_class DROP CONSTRAINT IF EXISTS job_class_pkey;
ALTER TABLE IF EXISTS ONLY public.job_class DROP CONSTRAINT IF EXISTS job_class_code_key;
ALTER TABLE IF EXISTS ONLY public.installation DROP CONSTRAINT IF EXISTS installation_pkey;
ALTER TABLE IF EXISTS ONLY public.function_criticality DROP CONSTRAINT IF EXISTS function_criticality_pkey;
ALTER TABLE IF EXISTS ONLY public.function_criticality DROP CONSTRAINT IF EXISTS function_criticality_degree_key;
ALTER TABLE IF EXISTS ONLY public.function_counter DROP CONSTRAINT IF EXISTS function_counter_pkey;
ALTER TABLE IF EXISTS ONLY public.flyway_schema_history DROP CONSTRAINT IF EXISTS flyway_schema_history_pk;
ALTER TABLE IF EXISTS ONLY public.discipline DROP CONSTRAINT IF EXISTS discipline_pkey;
ALTER TABLE IF EXISTS ONLY public.discipline DROP CONSTRAINT IF EXISTS discipline_code_key;
ALTER TABLE IF EXISTS ONLY public.department DROP CONSTRAINT IF EXISTS department_pkey;
ALTER TABLE IF EXISTS ONLY public.currency_register DROP CONSTRAINT IF EXISTS currency_register_pkey;
ALTER TABLE IF EXISTS ONLY public.currency_register DROP CONSTRAINT IF EXISTS currency_register_code_key;
ALTER TABLE IF EXISTS ONLY public.counter_reading_log DROP CONSTRAINT IF EXISTS counter_reading_log_pkey;
ALTER TABLE IF EXISTS ONLY public.component_type DROP CONSTRAINT IF EXISTS component_type_type_number_key;
ALTER TABLE IF EXISTS ONLY public.component_type_stock_type DROP CONSTRAINT IF EXISTS component_type_stock_type_pkey;
ALTER TABLE IF EXISTS ONLY public.component_type_related_type DROP CONSTRAINT IF EXISTS component_type_related_type_pkey;
ALTER TABLE IF EXISTS ONLY public.component_type DROP CONSTRAINT IF EXISTS component_type_pkey;
ALTER TABLE IF EXISTS ONLY public.component_type_measure_point_def DROP CONSTRAINT IF EXISTS component_type_measure_point_def_pkey;
ALTER TABLE IF EXISTS ONLY public.component_type_counter_def DROP CONSTRAINT IF EXISTS component_type_counter_def_pkey;
ALTER TABLE IF EXISTS ONLY public.component_type_children DROP CONSTRAINT IF EXISTS component_type_children_pkey;
ALTER TABLE IF EXISTS ONLY public.component_status_log DROP CONSTRAINT IF EXISTS component_status_log_pkey;
ALTER TABLE IF EXISTS ONLY public.component DROP CONSTRAINT IF EXISTS component_pkey;
ALTER TABLE IF EXISTS ONLY public.component DROP CONSTRAINT IF EXISTS component_number_key;
ALTER TABLE IF EXISTS ONLY public.component_measure_point DROP CONSTRAINT IF EXISTS component_measure_point_pkey;
ALTER TABLE IF EXISTS ONLY public.component_function_rotation DROP CONSTRAINT IF EXISTS component_function_rotation_pkey;
ALTER TABLE IF EXISTS ONLY public.component_function_history DROP CONSTRAINT IF EXISTS component_function_history_pkey;
ALTER TABLE IF EXISTS ONLY public.component_counter DROP CONSTRAINT IF EXISTS component_counter_pkey;
ALTER TABLE IF EXISTS ONLY public.component_archive DROP CONSTRAINT IF EXISTS component_archive_pkey;
ALTER TABLE IF EXISTS ONLY public.budget_code DROP CONSTRAINT IF EXISTS budget_code_pkey;
ALTER TABLE IF EXISTS ONLY public.budget_code DROP CONSTRAINT IF EXISTS budget_code_code_key;
ALTER TABLE IF EXISTS ONLY public.amos_user DROP CONSTRAINT IF EXISTS amos_user_pkey;
DROP TABLE IF EXISTS public.vendor_register;
DROP TABLE IF EXISTS public.user_role;
DROP TABLE IF EXISTS public.user_option;
DROP TABLE IF EXISTS public.user_department_access;
DROP TABLE IF EXISTS public.unit_register;
DROP TABLE IF EXISTS public.trade;
DROP TABLE IF EXISTS public.stock_type_vendor;
DROP TABLE IF EXISTS public.stock_type_maker;
DROP TABLE IF EXISTS public.stock_type;
DROP TABLE IF EXISTS public.stock_grade;
DROP TABLE IF EXISTS public.role;
DROP TABLE IF EXISTS public.measure_point_reading_log;
DROP TABLE IF EXISTS public.maker_register;
DROP TABLE IF EXISTS public.maintenance_function;
DROP TABLE IF EXISTS public.location_register;
DROP TABLE IF EXISTS public.job_class;
DROP TABLE IF EXISTS public.installation;
DROP TABLE IF EXISTS public.function_criticality;
DROP TABLE IF EXISTS public.function_counter;
DROP TABLE IF EXISTS public.flyway_schema_history;
DROP TABLE IF EXISTS public.discipline;
DROP TABLE IF EXISTS public.department;
DROP TABLE IF EXISTS public.currency_register;
DROP TABLE IF EXISTS public.counter_reading_log;
DROP TABLE IF EXISTS public.component_type_stock_type;
DROP TABLE IF EXISTS public.component_type_related_type;
DROP TABLE IF EXISTS public.component_type_measure_point_def;
DROP TABLE IF EXISTS public.component_type_counter_def;
DROP TABLE IF EXISTS public.component_type_children;
DROP TABLE IF EXISTS public.component_type;
DROP TABLE IF EXISTS public.component_status_log;
DROP TABLE IF EXISTS public.component_measure_point;
DROP TABLE IF EXISTS public.component_function_rotation;
DROP TABLE IF EXISTS public.component_function_history;
DROP TABLE IF EXISTS public.component_counter;
DROP TABLE IF EXISTS public.component_archive;
DROP TABLE IF EXISTS public.component;
DROP TABLE IF EXISTS public.budget_code;
DROP TABLE IF EXISTS public.amos_user;
-- *not* dropping schema, since initdb creates it
--
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

-- *not* creating schema, since initdb creates it


--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON SCHEMA public IS 'rebuilt for V1 identity rewrite';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: amos_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.amos_user (
    id bigint NOT NULL,
    username character varying(50) NOT NULL,
    display_name character varying(255),
    password_hash character varying(255) NOT NULL,
    status character varying(50) DEFAULT 'Active'::character varying NOT NULL
);


--
-- Name: amos_user_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.amos_user ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.amos_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: budget_code; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.budget_code (
    id bigint NOT NULL,
    code character varying(50) NOT NULL,
    name character varying(255),
    parent_budget_code character varying(50),
    description character varying(1000),
    status character varying(50) DEFAULT 'ACTIVE'::character varying NOT NULL
);


--
-- Name: budget_code_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.budget_code ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.budget_code_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: component; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.component (
    id bigint NOT NULL,
    number character varying(50),
    type_number character varying(50),
    name character varying(255),
    status character varying(50),
    maker character varying(50),
    type character varying(50),
    serial_no character varying(100),
    location character varying(100),
    department character varying(100),
    vendor character varying(255),
    function_no character varying(50),
    install_date character varying(20),
    installation character varying(50),
    parent_component character varying(50),
    component_type_model character varying(100),
    date_created character varying(20),
    date_modified character varying(20)
);


--
-- Name: component_archive; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.component_archive (
    id bigint NOT NULL,
    component_no character varying(50),
    kind character varying(20),
    from_department character varying(100),
    to_department character varying(100),
    archive_date character varying(20),
    data character varying(2000)
);


--
-- Name: component_archive_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.component_archive ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.component_archive_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: component_counter; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.component_counter (
    id bigint NOT NULL,
    component_id bigint,
    code character varying(50),
    description character varying(255),
    unit character varying(20),
    current_value double precision,
    depends_on character varying(50),
    latest_zeroed_date character varying(20),
    start_value numeric(18,4),
    average numeric(18,4),
    calculate character varying(10)
);


--
-- Name: component_counter_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.component_counter ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.component_counter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: component_function_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.component_function_history (
    id bigint NOT NULL,
    component_id bigint,
    component_no character varying(50),
    function_no character varying(50),
    function_description character varying(200),
    location character varying(100),
    action character varying(20),
    performed_by character varying(50),
    performed_at character varying(20)
);


--
-- Name: component_function_history_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.component_function_history ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.component_function_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: component_function_rotation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.component_function_rotation (
    id bigint NOT NULL,
    function_id bigint,
    component_id bigint,
    component_no character varying(50),
    component_name character varying(200),
    action character varying(20),
    performed_by character varying(50),
    performed_at character varying(20),
    details character varying(1000),
    new_location character varying(100),
    new_status character varying(50),
    installed_at character varying(20),
    removed_at character varying(20)
);


--
-- Name: component_function_rotation_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.component_function_rotation ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.component_function_rotation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: component_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.component ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.component_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: component_measure_point; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.component_measure_point (
    id bigint NOT NULL,
    component_id bigint,
    code character varying(50),
    description character varying(200),
    unit character varying(20),
    trend character varying(20),
    reading character varying(50),
    last_read_date character varying(20)
);


--
-- Name: component_measure_point_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.component_measure_point ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.component_measure_point_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: component_status_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.component_status_log (
    id bigint NOT NULL,
    component_id bigint,
    component_no character varying(50),
    old_status character varying(50),
    new_status character varying(50),
    reason character varying(200),
    changed_by character varying(50),
    changed_at character varying(20)
);


--
-- Name: component_status_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.component_status_log ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.component_status_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: component_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.component_type (
    id bigint NOT NULL,
    type_number character varying(50),
    name character varying(255),
    maker character varying(50),
    model character varying(100),
    type character varying(50),
    class_code character varying(50),
    preferred_vendor character varying(255),
    parent_type_number character varying(50),
    description character varying(1000),
    status character varying(50),
    date_created character varying(20),
    date_modified character varying(20),
    parent_id bigint,
    comp_type_model character varying(100)
);


--
-- Name: component_type_children; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.component_type_children (
    id bigint NOT NULL,
    parent_type_id bigint NOT NULL,
    child_type_id bigint NOT NULL,
    relation character varying(50)
);


--
-- Name: component_type_children_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.component_type_children ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.component_type_children_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: component_type_counter_def; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.component_type_counter_def (
    id bigint NOT NULL,
    component_type_id bigint NOT NULL,
    code character varying(50) NOT NULL,
    description character varying(255),
    unit character varying(20),
    sort_order integer DEFAULT 0
);


--
-- Name: component_type_counter_def_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.component_type_counter_def ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.component_type_counter_def_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: component_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.component_type ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.component_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: component_type_measure_point_def; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.component_type_measure_point_def (
    id bigint NOT NULL,
    component_type_id bigint NOT NULL,
    code character varying(50) NOT NULL,
    description character varying(255),
    trend character varying(20),
    unit character varying(20),
    sort_order integer DEFAULT 0
);


--
-- Name: component_type_measure_point_def_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.component_type_measure_point_def ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.component_type_measure_point_def_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: component_type_related_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.component_type_related_type (
    id bigint NOT NULL,
    component_type_id bigint NOT NULL,
    related_component_type_id bigint NOT NULL
);


--
-- Name: component_type_related_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.component_type_related_type ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.component_type_related_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: component_type_stock_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.component_type_stock_type (
    id bigint NOT NULL,
    component_type_id bigint NOT NULL,
    stock_type_id bigint NOT NULL,
    makers_ref character varying(100),
    quantity double precision DEFAULT 1,
    remarks character varying(500),
    alternative_no character varying(100)
);


--
-- Name: component_type_stock_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.component_type_stock_type ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.component_type_stock_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: counter_reading_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.counter_reading_log (
    id bigint NOT NULL,
    component_counter_id bigint,
    component_no character varying(50),
    function_no character varying(50),
    code character varying(50),
    description character varying(200),
    old_value numeric(18,4),
    new_value numeric(18,4),
    delta numeric(18,4),
    reading_date character varying(20),
    created_at timestamp without time zone,
    created_by character varying(50)
);


--
-- Name: counter_reading_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.counter_reading_log ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.counter_reading_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: currency_register; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.currency_register (
    id bigint NOT NULL,
    code character varying(10) NOT NULL,
    name character varying(255),
    symbol character varying(10),
    status character varying(50) DEFAULT 'ACTIVE'::character varying NOT NULL
);


--
-- Name: currency_register_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.currency_register ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.currency_register_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: department; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.department (
    id bigint NOT NULL,
    installation_id bigint NOT NULL,
    code character varying(50) NOT NULL,
    name character varying(255),
    status character varying(50) DEFAULT 'Active'::character varying NOT NULL
);


--
-- Name: department_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.department ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.department_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: discipline; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.discipline (
    id bigint NOT NULL,
    code character varying(50) NOT NULL,
    name character varying(255),
    description character varying(1000),
    status character varying(50) DEFAULT 'ACTIVE'::character varying NOT NULL
);


--
-- Name: discipline_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.discipline ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.discipline_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


--
-- Name: function_counter; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.function_counter (
    id bigint NOT NULL,
    function_id bigint,
    code character varying(50),
    description character varying(200),
    unit character varying(20),
    last_value numeric(18,4)
);


--
-- Name: function_counter_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.function_counter ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.function_counter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: function_criticality; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.function_criticality (
    id bigint NOT NULL,
    degree character varying(50) NOT NULL,
    description character varying(255),
    color character varying(20),
    sort_order integer DEFAULT 0 NOT NULL,
    active boolean DEFAULT true NOT NULL
);


--
-- Name: function_criticality_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.function_criticality ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.function_criticality_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: installation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.installation (
    id bigint NOT NULL,
    code character varying(50) NOT NULL,
    name character varying(255),
    status character varying(50) DEFAULT 'Active'::character varying NOT NULL
);


--
-- Name: installation_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.installation ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.installation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: job_class; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.job_class (
    id bigint NOT NULL,
    code character varying(50) NOT NULL,
    name character varying(255),
    description character varying(1000),
    status character varying(50) DEFAULT 'ACTIVE'::character varying NOT NULL
);


--
-- Name: job_class_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.job_class ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.job_class_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: location_register; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.location_register (
    id bigint NOT NULL,
    installation_id bigint,
    code character varying(50) NOT NULL,
    name character varying(255),
    parent_location_id bigint,
    location_type character varying(50),
    status character varying(50) DEFAULT 'ACTIVE'::character varying NOT NULL,
    remarks character varying(1000)
);


--
-- Name: location_register_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.location_register ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.location_register_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: maintenance_function; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.maintenance_function (
    id bigint NOT NULL,
    function_no character varying(50) NOT NULL,
    installation character varying(50),
    department character varying(100),
    description character varying(255),
    reference character varying(100),
    parent_function_no character varying(50),
    status character varying(50),
    location character varying(100),
    criticality character varying(50),
    installed_component_no character varying(50),
    sfi_code character varying(50),
    system character varying(100),
    sub_system character varying(100),
    remarks character varying(1000),
    serial_no character varying(100),
    maker character varying(100),
    model character varying(100),
    tag_no character varying(100),
    asset_value numeric(18,2),
    acquisition_date character varying(20),
    currency character varying(10),
    depreciation numeric(18,2)
);


--
-- Name: maintenance_function_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.maintenance_function ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.maintenance_function_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: maker_register; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.maker_register (
    id bigint NOT NULL,
    code character varying(50) NOT NULL,
    name character varying(255),
    status character varying(50) DEFAULT 'ACTIVE'::character varying NOT NULL,
    remarks character varying(1000)
);


--
-- Name: maker_register_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.maker_register ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.maker_register_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: measure_point_reading_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.measure_point_reading_log (
    id bigint NOT NULL,
    component_measure_point_id bigint,
    component_no character varying(50),
    function_no character varying(50),
    code character varying(50),
    description character varying(200),
    reading_value character varying(255),
    trend character varying(20),
    reading_date character varying(20),
    created_at timestamp without time zone,
    created_by character varying(50)
);


--
-- Name: measure_point_reading_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.measure_point_reading_log ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.measure_point_reading_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.role (
    id bigint NOT NULL,
    code character varying(50) NOT NULL,
    name character varying(255)
);


--
-- Name: role_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.role ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: stock_grade; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stock_grade (
    id bigint NOT NULL,
    stock_type_id bigint,
    stock_type_no character varying(50),
    grade character varying(20),
    price_factor double precision,
    description character varying(255)
);


--
-- Name: stock_grade_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.stock_grade ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.stock_grade_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: stock_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stock_type (
    id bigint NOT NULL,
    stock_type_no character varying(50),
    description character varying(255),
    maker character varying(50),
    makers_ref character varying(100),
    vendor character varying(255),
    grade character varying(20),
    unit character varying(20),
    best_price double precision,
    status character varying(50),
    installation character varying(50)
);


--
-- Name: stock_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.stock_type ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.stock_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: stock_type_maker; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stock_type_maker (
    id bigint NOT NULL,
    stock_type_id bigint,
    stock_type_no character varying(50),
    maker character varying(50),
    makers_ref character varying(100),
    price double precision,
    status character varying(50)
);


--
-- Name: stock_type_maker_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.stock_type_maker ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.stock_type_maker_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: stock_type_vendor; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stock_type_vendor (
    id bigint NOT NULL,
    stock_type_id bigint,
    stock_type_no character varying(50),
    vendor character varying(255),
    price double precision,
    lead_time integer,
    status character varying(50)
);


--
-- Name: stock_type_vendor_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.stock_type_vendor ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.stock_type_vendor_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: trade; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.trade (
    id bigint NOT NULL,
    code character varying(50) NOT NULL,
    name character varying(255),
    description character varying(1000),
    status character varying(50) DEFAULT 'ACTIVE'::character varying NOT NULL
);


--
-- Name: trade_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.trade ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.trade_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: unit_register; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.unit_register (
    id bigint NOT NULL,
    code character varying(50) NOT NULL,
    name character varying(255),
    description character varying(1000),
    status character varying(50) DEFAULT 'ACTIVE'::character varying NOT NULL
);


--
-- Name: unit_register_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.unit_register ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.unit_register_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: user_department_access; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_department_access (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    installation_id bigint NOT NULL,
    department_id bigint NOT NULL
);


--
-- Name: user_department_access_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.user_department_access ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.user_department_access_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: user_option; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_option (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    opt_key character varying(100) NOT NULL,
    opt_value text
);


--
-- Name: user_option_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.user_option ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.user_option_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: user_role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_role (
    user_id bigint NOT NULL,
    role_id bigint NOT NULL
);


--
-- Name: vendor_register; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.vendor_register (
    id bigint NOT NULL,
    vendor_no character varying(50) NOT NULL,
    name character varying(255),
    country character varying(100),
    currency character varying(10),
    payment_terms character varying(100),
    status character varying(50) DEFAULT 'ACTIVE'::character varying NOT NULL,
    remarks character varying(1000)
);


--
-- Name: vendor_register_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.vendor_register ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.vendor_register_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Data for Name: amos_user; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.amos_user (id, username, display_name, password_hash, status) FROM stdin;
1	admin	Administrator	$2a$10$Q98otlckVvNUS7xXWFg4eeYEMkuujqVuV4fjcityJ2j17uyZbAIVi	Active
\.


--
-- Data for Name: budget_code; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.budget_code (id, code, name, parent_budget_code, description, status) FROM stdin;
1	BG-OPS	Operations	\N	Operating budget	ACTIVE
\.


--
-- Data for Name: component; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.component (id, number, type_number, name, status, maker, type, serial_no, location, department, vendor, function_no, install_date, installation, parent_component, component_type_model, date_created, date_modified) FROM stdin;
41	C-TEST-02	CT-1002	Aux Boiler Feed Pump A	Available	GRUN	Pump	SN-GRUN-2002	Engine Room	ER	Grundfos	\N	\N	Traveller	\N	\N	\N	\N
43	C-TEST-04	CT-1004	Main Switchboard MCC	Available	ABBM	Switchboard	SN-ABBM-2004	Engine Room	ER	ABB Marine	\N	\N	Traveller	\N	\N	\N	\N
44	C-TEST-05	CT-1005	Deck Crane Hoist Motor	Available	MACG	Motor	SN-MACG-2005	Deck	ER	MacGregor	\N	\N	Traveller	\N	\N	\N	\N
42	C-TEST-03	CT-1003	Compressor Unit B	In Use	ALFA	Compressor	SN-ALFA-2003	Engine Room	ER	Alfa Laval	FN-ENG-02	\N	Traveller	\N	\N	\N	\N
40	C-TEST-01	CT-1001	ME Cylinder #2	In Use	WART	Liner	SN-WART-2001	Engine Room	ER	Wärtsilä Marine	FN-ENG-01	\N	Traveller	\N	\N	\N	\N
45	C-TEST-10	CT-1001	ME Piston #1	In Use	WART	Piston	SN-WART-2010	Engine Room	ER	Wärtsilä Marine	FN-ENG-01	2026-01-15	Traveller	\N	\N	\N	\N
\.


--
-- Data for Name: component_archive; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.component_archive (id, component_no, kind, from_department, to_department, archive_date, data) FROM stdin;
\.


--
-- Data for Name: component_counter; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.component_counter (id, component_id, code, description, unit, current_value, depends_on, latest_zeroed_date, start_value, average, calculate) FROM stdin;
1	\N	RUN-HRS	\N	h	\N	\N	\N	\N	\N	\N
4	45	OIL	Lub Oil Pressure	bar	4.5	\N	2026-01-15	\N	\N	\N
5	40	HRS	Running Hours	h	900	\N	2026-01-15	\N	\N	\N
3	45	HRS	Running Hours	h	1000	C-TEST-01	2026-07-28	\N	5.1546	\N
\.


--
-- Data for Name: component_function_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.component_function_history (id, component_id, component_no, function_no, function_description, location, action, performed_by, performed_at) FROM stdin;
1	39	C-TEST-10	FN-ENG-01	Main Engine	Engine Room	Removed	A. Admin	2026-07-28
2	34	C-TEST-01	FN-ENG-01	Main Engine	Engine Room	Installed	A. Admin	2026-07-28
3	35	C-TEST-02	FN-ENG-02	Auxiliary Boiler A	Engine Room	Installed	A. Admin	2026-07-28
4	36	C-TEST-03	111	111	ER	Installed	A. Admin	2026-07-28
5	35	C-TEST-02	FN-ENG-02	Auxiliary Boiler A	Engine Room	Removed	A. Admin	2026-07-28
6	36	C-TEST-03	111	111	ER	Removed	A. Admin	2026-07-28
7	35	C-TEST-02	111	111	ER	Installed	A. Admin	2026-07-28
8	35	C-TEST-02	111	111	ER	Removed	A. Admin	2026-07-28
9	35	C-TEST-02	FN-DECK-01	Deck Crane	Deck	Installed	A. Admin	2026-07-28
10	35	C-TEST-02	FN-DECK-01	Deck Crane	Deck	Removed	A. Admin	2026-07-28
11	37	C-TEST-04	FN-AUX-00001	10100101	ER	Installed	A. Admin	2026-07-28
12	37	C-TEST-04	FN-AUX-00001	10100101	ER	Removed	A. Admin	2026-07-28
13	37	C-TEST-04	FN-AUX-00001	10100101	ER	Installed	A. Admin	2026-07-28
14	42	C-TEST-03	FN-ENG-03	Auxiliary Boiler B	Engine Room	Installed	A. Admin	2026-07-28
15	42	C-TEST-03	FN-ENG-03	Auxiliary Boiler B	Engine Room	Removed	A. Admin	2026-07-28
16	42	C-TEST-03	FN-ENG-02	Auxiliary Boiler A	Engine Room	Installed	A. Admin	2026-07-28
17	40	C-TEST-01	FN-DECK-01	Deck Crane	Deck	Installed	A. Admin	2026-07-28
18	40	C-TEST-01	FN-DECK-01	Deck Crane	Deck	Removed	A. Admin	2026-07-28
19	45	C-TEST-10	FN-ENG-01	Main Engine	Engine Room	Removed	A. Admin	2026-07-28
20	40	C-TEST-01	FN-ENG-01	Main Engine	Engine Room	Installed	A. Admin	2026-07-28
\.


--
-- Data for Name: component_function_rotation; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.component_function_rotation (id, function_id, component_id, component_no, component_name, action, performed_by, performed_at, details, new_location, new_status, installed_at, removed_at) FROM stdin;
14	3	42	C-TEST-03	Compressor Unit B	Installed	A. Admin	2026-07-28	Installed via component			2026-07-28	
15	3	42	C-TEST-03	Compressor Unit B	Removed	A. Admin	2026-07-28	Moved to FN-ENG-02				2026-07-28
16	2	42	C-TEST-03	Compressor Unit B	Installed	A. Admin	2026-07-28	Installed via component			2026-07-28	
17	4	40	C-TEST-01	ME Cylinder #2	Installed	A. Admin	2026-07-28	Installed via component			2026-07-28	
18	4	40	C-TEST-01	ME Cylinder #2	Removed	A. Admin	2026-07-28	Moved to FN-ENG-01				2026-07-28
19	1	45	C-TEST-10	ME Piston #1	Removed	A. Admin	2026-07-28					2026-07-28
20	1	40	C-TEST-01	ME Cylinder #2	Installed	A. Admin	2026-07-28	Installed via component			2026-07-28	
\.


--
-- Data for Name: component_measure_point; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.component_measure_point (id, component_id, code, description, unit, trend, reading, last_read_date) FROM stdin;
1	45	T-EXH	Exhaust Temp	°C	Stable	420	2026-07-28
\.


--
-- Data for Name: component_status_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.component_status_log (id, component_id, component_no, old_status, new_status, reason, changed_by, changed_at) FROM stdin;
1	1	C-10001	In Use	Available	e2e-revert	e2e	2026-07-27
2	11	COMP-E2E-ER	Available	In Use		A. Admin	2026-07-27
3	1	C-10001	Available	Available		A. Admin	2026-07-27
4	42	C-TEST-03	Available	In Use	Installed on FN-ENG-03	A. Admin	2026-07-28
5	42	C-TEST-03	In Use	Available	Removed from FN-ENG-03	A. Admin	2026-07-28
6	42	C-TEST-03	Available	In Use	Installed on FN-ENG-02	A. Admin	2026-07-28
7	40	C-TEST-01	Available	In Use	Installed on FN-DECK-01	A. Admin	2026-07-28
8	40	C-TEST-01	In Use	Available	Removed from FN-DECK-01	A. Admin	2026-07-28
9	45	C-TEST-10	In Use	Available	Removed from FN-ENG-01	A. Admin	2026-07-28
10	40	C-TEST-01	Available	In Use	Installed on FN-ENG-01	A. Admin	2026-07-28
\.


--
-- Data for Name: component_type; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.component_type (id, type_number, name, maker, model, type, class_code, preferred_vendor, parent_type_number, description, status, date_created, date_modified, parent_id, comp_type_model) FROM stdin;
6	CT-DEMO-REL	Demo Related Pump	\N	\N	\N	\N	\N	\N	\N	Active	\N	\N	\N	\N
7	CT-DEMO-1	Demo Main Component Type	Demo Maker	DM-100	Equipment	\N	\N	\N	示例类型，含关联类型与备件链接	Active	\N	\N	\N	\N
8	CT-REPRO2	x	\N	\N	\N	\N	\N	\N	\N	Active	\N	\N	\N	\N
10	CT-51151	222	\N	\N	\N	\N	\N	\N	\N	Active	2026-07-27	2026-07-27	\N	\N
\.


--
-- Data for Name: component_type_children; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.component_type_children (id, parent_type_id, child_type_id, relation) FROM stdin;
\.


--
-- Data for Name: component_type_counter_def; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.component_type_counter_def (id, component_type_id, code, description, unit, sort_order) FROM stdin;
\.


--
-- Data for Name: component_type_measure_point_def; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.component_type_measure_point_def (id, component_type_id, code, description, trend, unit, sort_order) FROM stdin;
\.


--
-- Data for Name: component_type_related_type; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.component_type_related_type (id, component_type_id, related_component_type_id) FROM stdin;
2	7	6
\.


--
-- Data for Name: component_type_stock_type; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.component_type_stock_type (id, component_type_id, stock_type_id, makers_ref, quantity, remarks, alternative_no) FROM stdin;
4	7	1	\N	3	\N	ALT-DEMO-1
\.


--
-- Data for Name: counter_reading_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.counter_reading_log (id, component_counter_id, component_no, function_no, code, description, old_value, new_value, delta, reading_date, created_at, created_by) FROM stdin;
1	3	C-TEST-10	FN-ENG-01	HRS	Running Hours	900.0000	1000.0000	100.0000	2026-07-28	2026-07-28 14:44:48.223181	A. Admin
\.


--
-- Data for Name: currency_register; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.currency_register (id, code, name, symbol, status) FROM stdin;
1	EUR	Euro	€	ACTIVE
\.


--
-- Data for Name: department; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.department (id, installation_id, code, name, status) FROM stdin;
1	1	ER	Engine Room	Active
\.


--
-- Data for Name: discipline; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.discipline (id, code, name, description, status) FROM stdin;
1	WELD	Welding	Welding discipline	ACTIVE
\.


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1	init	SQL	V1__init.sql	-1901389511	postgres	2026-07-25 18:49:47.905178	28	t
2	2	component type children	SQL	V2__component_type_children.sql	1827124613	postgres	2026-07-25 18:49:47.952754	7	t
3	3	component type defs and relations	SQL	V3__component_type_defs_and_relations.sql	649730813	postgres	2026-07-25 21:39:35.64697	15	t
4	4	system scope	SQL	V4__system_scope.sql	-371556680	postgres	2026-07-25 22:20:29.480368	26	t
5	5	registers	SQL	V5__registers.sql	820216667	postgres	2026-07-25 23:09:36.057495	18	t
6	6	register indexes	SQL	V6__register_indexes.sql	119636329	postgres	2026-07-27 09:00:04.541429	30	t
7	7	component type stock type alternative	SQL	V7__component_type_stock_type_alternative.sql	23505576	postgres	2026-07-27 10:59:07.03267	5	t
8	8	components	SQL	V8__components.sql	138745920	postgres	2026-07-27 13:16:30.625084	27	t
9	9	functions	SQL	V9__functions.sql	-1374468576	postgres	2026-07-27 15:49:00.451838	27	t
10	10	component test data	SQL	V10__component_test_data.sql	-1183585715	postgres	2026-07-28 13:52:19.277618	12	t
11	11	counter measure point logs	SQL	V11__counter_measure_point_logs.sql	1143623587	postgres	2026-07-28 14:43:37.189869	25	t
\.


--
-- Data for Name: function_counter; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.function_counter (id, function_id, code, description, unit, last_value) FROM stdin;
1	1	HRS	Running Hours	h	5100.0000
\.


--
-- Data for Name: function_criticality; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.function_criticality (id, degree, description, color, sort_order, active) FROM stdin;
4	Fred01	010101	#1b26c0	1	t
1	CRIT	Critical	#d32f2f	1	t
5	TESTDEL	soft-delete test	\N	0	f
\.


--
-- Data for Name: installation; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.installation (id, code, name, status) FROM stdin;
1	Traveller	MV Traveller	Active
\.


--
-- Data for Name: job_class; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.job_class (id, code, name, description, status) FROM stdin;
1	CLASS-A	Class A Overhaul	Major planned overhaul	ACTIVE
\.


--
-- Data for Name: location_register; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.location_register (id, installation_id, code, name, parent_location_id, location_type, status, remarks) FROM stdin;
1	1	ER	Engine Room	\N	Space	ACTIVE	\N
\.


--
-- Data for Name: maintenance_function; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.maintenance_function (id, function_no, installation, department, description, reference, parent_function_no, status, location, criticality, installed_component_no, sfi_code, system, sub_system, remarks, serial_no, maker, model, tag_no, asset_value, acquisition_date, currency, depreciation) FROM stdin;
5	111	\N	\N	111	111	FN-ENG-01	In Use	ER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
7	111	Traveller	ER	111	111	FN-ENG-01	In Use	ER	Medium	\N	\N	\N	\N	\N	\N	\N	\N	\N	0.00	\N	\N	0.00
3	FN-ENG-03	Traveller	Engine Room	Auxiliary Boiler B	AUX BOILER B	FN-ENG-01	In Use	Engine Room	High	\N	23000000	Machinery	Boiler	\N	\N	\N	\N	\N	\N	\N	\N	\N
2	FN-ENG-02	Traveller	Engine Room	Auxiliary Boiler A	AUX BOILER A	FN-ENG-01	In Use	Engine Room	High	C-TEST-03	23000000	Machinery	Boiler	\N	\N	\N	\N	\N	\N	\N	\N	\N
4	FN-DECK-01	Traveller	Deck	Deck Crane	DCRANE	\N	In Use	Deck	Medium	\N	12000000	Hull	Deck Crane	\N	\N	\N	\N	\N	\N	\N	\N	\N
1	FN-ENG-01	Traveller	Engine Room	Main Engine	ME	\N	In Use	Engine Room	High	C-TEST-10	21000000	Machinery	Propulsion	\N	\N	\N	\N	\N	\N	\N	\N	\N
\.


--
-- Data for Name: maker_register; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.maker_register (id, code, name, status, remarks) FROM stdin;
2	SHELL	Shell	ACTIVE	\N
3	ABBM	ABB Marine	ACTIVE	\N
4	ALFA	Alfa Laval	ACTIVE	\N
5	AALB	Aalborg	ACTIVE	\N
6	GRUN	Grundfos	ACTIVE	\N
7	TYCO	Tyco International	ACTIVE	\N
8	RRM	Rolls-Royce Marine	ACTIVE	\N
9	SWEP	SWEP International	ACTIVE	\N
10	MANE	MAN Energy Solutions	ACTIVE	\N
11	CUMM	Cummins Inc.	ACTIVE	\N
12	MACG	MacGregor / Cargotec	ACTIVE	\N
13	USHA	Usha Martin	ACTIVE	\N
14	CARR	Carrier Corporation	ACTIVE	\N
15	SCHAT	Schat-Harding	ACTIVE	\N
16	KIDDE	Kidde (Carrier Fire Products)	ACTIVE	\N
17	FURU	Furuno Electric	ACTIVE	\N
18	TTS	TTS (MacGregor Group)	ACTIVE	\N
19	MANB	MAN B&W DIESEL A/S	ACTIVE	\N
23	22	11	ACTIVE	
22	2222	111	INACTIVE	www
20	4任4	3434	ACTIVE	343434
1	WART	Wärtsilä	INACTIVE	\N
\.


--
-- Data for Name: measure_point_reading_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.measure_point_reading_log (id, component_measure_point_id, component_no, function_no, code, description, reading_value, trend, reading_date, created_at, created_by) FROM stdin;
\.


--
-- Data for Name: role; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.role (id, code, name) FROM stdin;
1	ADMIN	Administrator
\.


--
-- Data for Name: stock_grade; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.stock_grade (id, stock_type_id, stock_type_no, grade, price_factor, description) FROM stdin;
\.


--
-- Data for Name: stock_type; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.stock_type (id, stock_type_no, description, maker, makers_ref, vendor, grade, unit, best_price, status, installation) FROM stdin;
1	ST-101	Cylinder Liner Gasket	WART	\N	Wärtsilä Marine	A	pcs	320	Active	Traveller
\.


--
-- Data for Name: stock_type_maker; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.stock_type_maker (id, stock_type_id, stock_type_no, maker, makers_ref, price, status) FROM stdin;
1	1	ST-101	WART	1698466	310	Approved
\.


--
-- Data for Name: stock_type_vendor; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.stock_type_vendor (id, stock_type_id, stock_type_no, vendor, price, lead_time, status) FROM stdin;
\.


--
-- Data for Name: trade; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.trade (id, code, name, description, status) FROM stdin;
1	MECH	Mechanical	Mechanical trade	ACTIVE
\.


--
-- Data for Name: unit_register; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.unit_register (id, code, name, description, status) FROM stdin;
1	PCS	Pieces	Individual count	ACTIVE
\.


--
-- Data for Name: user_department_access; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.user_department_access (id, user_id, installation_id, department_id) FROM stdin;
1	1	1	1
\.


--
-- Data for Name: user_option; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.user_option (id, user_id, opt_key, opt_value) FROM stdin;
1	1	theme	light
\.


--
-- Data for Name: user_role; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.user_role (user_id, role_id) FROM stdin;
1	1
\.


--
-- Data for Name: vendor_register; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.vendor_register (id, vendor_no, name, country, currency, payment_terms, status, remarks) FROM stdin;
1	V-WART	Wärtsilä Marine	Finland	EUR	Net 30	ACTIVE	\N
\.


--
-- Name: amos_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.amos_user_id_seq', 1, true);


--
-- Name: budget_code_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.budget_code_id_seq', 1, true);


--
-- Name: component_archive_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.component_archive_id_seq', 1, false);


--
-- Name: component_counter_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.component_counter_id_seq', 5, true);


--
-- Name: component_function_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.component_function_history_id_seq', 20, true);


--
-- Name: component_function_rotation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.component_function_rotation_id_seq', 20, true);


--
-- Name: component_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.component_id_seq', 45, true);


--
-- Name: component_measure_point_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.component_measure_point_id_seq', 1, true);


--
-- Name: component_status_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.component_status_log_id_seq', 10, true);


--
-- Name: component_type_children_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.component_type_children_id_seq', 1, false);


--
-- Name: component_type_counter_def_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.component_type_counter_def_id_seq', 1, true);


--
-- Name: component_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.component_type_id_seq', 10, true);


--
-- Name: component_type_measure_point_def_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.component_type_measure_point_def_id_seq', 1, true);


--
-- Name: component_type_related_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.component_type_related_type_id_seq', 2, true);


--
-- Name: component_type_stock_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.component_type_stock_type_id_seq', 4, true);


--
-- Name: counter_reading_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.counter_reading_log_id_seq', 1, true);


--
-- Name: currency_register_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.currency_register_id_seq', 1, true);


--
-- Name: department_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.department_id_seq', 1, true);


--
-- Name: discipline_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.discipline_id_seq', 1, true);


--
-- Name: function_counter_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.function_counter_id_seq', 1, true);


--
-- Name: function_criticality_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.function_criticality_id_seq', 5, true);


--
-- Name: installation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.installation_id_seq', 1, true);


--
-- Name: job_class_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.job_class_id_seq', 1, true);


--
-- Name: location_register_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.location_register_id_seq', 1, true);


--
-- Name: maintenance_function_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.maintenance_function_id_seq', 10, true);


--
-- Name: maker_register_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.maker_register_id_seq', 23, true);


--
-- Name: measure_point_reading_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.measure_point_reading_log_id_seq', 1, false);


--
-- Name: role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.role_id_seq', 1, true);


--
-- Name: stock_grade_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.stock_grade_id_seq', 1, false);


--
-- Name: stock_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.stock_type_id_seq', 1, true);


--
-- Name: stock_type_maker_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.stock_type_maker_id_seq', 1, true);


--
-- Name: stock_type_vendor_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.stock_type_vendor_id_seq', 1, false);


--
-- Name: trade_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.trade_id_seq', 1, true);


--
-- Name: unit_register_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.unit_register_id_seq', 3, true);


--
-- Name: user_department_access_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.user_department_access_id_seq', 1, true);


--
-- Name: user_option_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.user_option_id_seq', 1, true);


--
-- Name: vendor_register_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.vendor_register_id_seq', 3, true);


--
-- Name: amos_user amos_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.amos_user
    ADD CONSTRAINT amos_user_pkey PRIMARY KEY (id);


--
-- Name: budget_code budget_code_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.budget_code
    ADD CONSTRAINT budget_code_code_key UNIQUE (code);


--
-- Name: budget_code budget_code_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.budget_code
    ADD CONSTRAINT budget_code_pkey PRIMARY KEY (id);


--
-- Name: component_archive component_archive_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_archive
    ADD CONSTRAINT component_archive_pkey PRIMARY KEY (id);


--
-- Name: component_counter component_counter_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_counter
    ADD CONSTRAINT component_counter_pkey PRIMARY KEY (id);


--
-- Name: component_function_history component_function_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_function_history
    ADD CONSTRAINT component_function_history_pkey PRIMARY KEY (id);


--
-- Name: component_function_rotation component_function_rotation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_function_rotation
    ADD CONSTRAINT component_function_rotation_pkey PRIMARY KEY (id);


--
-- Name: component_measure_point component_measure_point_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_measure_point
    ADD CONSTRAINT component_measure_point_pkey PRIMARY KEY (id);


--
-- Name: component component_number_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component
    ADD CONSTRAINT component_number_key UNIQUE (number);


--
-- Name: component component_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component
    ADD CONSTRAINT component_pkey PRIMARY KEY (id);


--
-- Name: component_status_log component_status_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_status_log
    ADD CONSTRAINT component_status_log_pkey PRIMARY KEY (id);


--
-- Name: component_type_children component_type_children_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_type_children
    ADD CONSTRAINT component_type_children_pkey PRIMARY KEY (id);


--
-- Name: component_type_counter_def component_type_counter_def_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_type_counter_def
    ADD CONSTRAINT component_type_counter_def_pkey PRIMARY KEY (id);


--
-- Name: component_type_measure_point_def component_type_measure_point_def_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_type_measure_point_def
    ADD CONSTRAINT component_type_measure_point_def_pkey PRIMARY KEY (id);


--
-- Name: component_type component_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_type
    ADD CONSTRAINT component_type_pkey PRIMARY KEY (id);


--
-- Name: component_type_related_type component_type_related_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_type_related_type
    ADD CONSTRAINT component_type_related_type_pkey PRIMARY KEY (id);


--
-- Name: component_type_stock_type component_type_stock_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_type_stock_type
    ADD CONSTRAINT component_type_stock_type_pkey PRIMARY KEY (id);


--
-- Name: component_type component_type_type_number_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_type
    ADD CONSTRAINT component_type_type_number_key UNIQUE (type_number);


--
-- Name: counter_reading_log counter_reading_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.counter_reading_log
    ADD CONSTRAINT counter_reading_log_pkey PRIMARY KEY (id);


--
-- Name: currency_register currency_register_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.currency_register
    ADD CONSTRAINT currency_register_code_key UNIQUE (code);


--
-- Name: currency_register currency_register_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.currency_register
    ADD CONSTRAINT currency_register_pkey PRIMARY KEY (id);


--
-- Name: department department_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.department
    ADD CONSTRAINT department_pkey PRIMARY KEY (id);


--
-- Name: discipline discipline_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.discipline
    ADD CONSTRAINT discipline_code_key UNIQUE (code);


--
-- Name: discipline discipline_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.discipline
    ADD CONSTRAINT discipline_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: function_counter function_counter_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.function_counter
    ADD CONSTRAINT function_counter_pkey PRIMARY KEY (id);


--
-- Name: function_criticality function_criticality_degree_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.function_criticality
    ADD CONSTRAINT function_criticality_degree_key UNIQUE (degree);


--
-- Name: function_criticality function_criticality_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.function_criticality
    ADD CONSTRAINT function_criticality_pkey PRIMARY KEY (id);


--
-- Name: installation installation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.installation
    ADD CONSTRAINT installation_pkey PRIMARY KEY (id);


--
-- Name: job_class job_class_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_class
    ADD CONSTRAINT job_class_code_key UNIQUE (code);


--
-- Name: job_class job_class_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_class
    ADD CONSTRAINT job_class_pkey PRIMARY KEY (id);


--
-- Name: location_register location_register_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.location_register
    ADD CONSTRAINT location_register_pkey PRIMARY KEY (id);


--
-- Name: maintenance_function maintenance_function_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.maintenance_function
    ADD CONSTRAINT maintenance_function_pkey PRIMARY KEY (id);


--
-- Name: maker_register maker_register_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.maker_register
    ADD CONSTRAINT maker_register_code_key UNIQUE (code);


--
-- Name: maker_register maker_register_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.maker_register
    ADD CONSTRAINT maker_register_pkey PRIMARY KEY (id);


--
-- Name: measure_point_reading_log measure_point_reading_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.measure_point_reading_log
    ADD CONSTRAINT measure_point_reading_log_pkey PRIMARY KEY (id);


--
-- Name: role role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);


--
-- Name: stock_grade stock_grade_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stock_grade
    ADD CONSTRAINT stock_grade_pkey PRIMARY KEY (id);


--
-- Name: stock_type_maker stock_type_maker_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stock_type_maker
    ADD CONSTRAINT stock_type_maker_pkey PRIMARY KEY (id);


--
-- Name: stock_type stock_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stock_type
    ADD CONSTRAINT stock_type_pkey PRIMARY KEY (id);


--
-- Name: stock_type stock_type_stock_type_no_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stock_type
    ADD CONSTRAINT stock_type_stock_type_no_key UNIQUE (stock_type_no);


--
-- Name: stock_type_vendor stock_type_vendor_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stock_type_vendor
    ADD CONSTRAINT stock_type_vendor_pkey PRIMARY KEY (id);


--
-- Name: trade trade_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.trade
    ADD CONSTRAINT trade_code_key UNIQUE (code);


--
-- Name: trade trade_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.trade
    ADD CONSTRAINT trade_pkey PRIMARY KEY (id);


--
-- Name: amos_user uk_amos_user_username; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.amos_user
    ADD CONSTRAINT uk_amos_user_username UNIQUE (username);


--
-- Name: department uk_department_inst_code; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.department
    ADD CONSTRAINT uk_department_inst_code UNIQUE (installation_id, code);


--
-- Name: installation uk_installation_code; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.installation
    ADD CONSTRAINT uk_installation_code UNIQUE (code);


--
-- Name: role uk_role_code; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT uk_role_code UNIQUE (code);


--
-- Name: user_department_access uk_uda; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_department_access
    ADD CONSTRAINT uk_uda UNIQUE (user_id, installation_id, department_id);


--
-- Name: user_option uk_user_option; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_option
    ADD CONSTRAINT uk_user_option UNIQUE (user_id, opt_key);


--
-- Name: unit_register unit_register_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.unit_register
    ADD CONSTRAINT unit_register_code_key UNIQUE (code);


--
-- Name: unit_register unit_register_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.unit_register
    ADD CONSTRAINT unit_register_pkey PRIMARY KEY (id);


--
-- Name: component_type_children uq_ctc_pair; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_type_children
    ADD CONSTRAINT uq_ctc_pair UNIQUE (parent_type_id, child_type_id);


--
-- Name: component_type_counter_def uq_ctcd_pair; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_type_counter_def
    ADD CONSTRAINT uq_ctcd_pair UNIQUE (component_type_id, code);


--
-- Name: component_type_measure_point_def uq_ctmpd_pair; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_type_measure_point_def
    ADD CONSTRAINT uq_ctmpd_pair UNIQUE (component_type_id, code);


--
-- Name: component_type_related_type uq_ctrt_pair; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_type_related_type
    ADD CONSTRAINT uq_ctrt_pair UNIQUE (component_type_id, related_component_type_id);


--
-- Name: component_type_stock_type uq_ctst_pair; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_type_stock_type
    ADD CONSTRAINT uq_ctst_pair UNIQUE (component_type_id, stock_type_id);


--
-- Name: maintenance_function uq_function_install_no; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.maintenance_function
    ADD CONSTRAINT uq_function_install_no UNIQUE (installation, function_no);


--
-- Name: location_register uq_location_install_code; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.location_register
    ADD CONSTRAINT uq_location_install_code UNIQUE (installation_id, code);


--
-- Name: user_department_access user_department_access_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_department_access
    ADD CONSTRAINT user_department_access_pkey PRIMARY KEY (id);


--
-- Name: user_option user_option_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_option
    ADD CONSTRAINT user_option_pkey PRIMARY KEY (id);


--
-- Name: user_role user_role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_role
    ADD CONSTRAINT user_role_pkey PRIMARY KEY (user_id, role_id);


--
-- Name: vendor_register vendor_register_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vendor_register
    ADD CONSTRAINT vendor_register_pkey PRIMARY KEY (id);


--
-- Name: vendor_register vendor_register_vendor_no_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vendor_register
    ADD CONSTRAINT vendor_register_vendor_no_key UNIQUE (vendor_no);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_budget_code_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_budget_code_name ON public.budget_code USING btree (name);


--
-- Name: idx_budget_code_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_budget_code_status ON public.budget_code USING btree (status);


--
-- Name: idx_ca_component_no; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ca_component_no ON public.component_archive USING btree (component_no);


--
-- Name: idx_cfh_component; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cfh_component ON public.component_function_history USING btree (component_id);


--
-- Name: idx_cfh_component_no; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cfh_component_no ON public.component_function_history USING btree (component_no);


--
-- Name: idx_cfr_compno; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cfr_compno ON public.component_function_rotation USING btree (component_no);


--
-- Name: idx_cfr_component; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cfr_component ON public.component_function_rotation USING btree (component_id);


--
-- Name: idx_cfr_function; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cfr_function ON public.component_function_rotation USING btree (function_id);


--
-- Name: idx_cmp_component; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cmp_component ON public.component_measure_point USING btree (component_id);


--
-- Name: idx_component_department; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_component_department ON public.component USING btree (department);


--
-- Name: idx_component_installation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_component_installation ON public.component USING btree (installation);


--
-- Name: idx_component_parent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_component_parent ON public.component USING btree (parent_component);


--
-- Name: idx_component_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_component_status ON public.component USING btree (status);


--
-- Name: idx_crl_component; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_crl_component ON public.counter_reading_log USING btree (component_no);


--
-- Name: idx_crl_counter; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_crl_counter ON public.counter_reading_log USING btree (component_counter_id);


--
-- Name: idx_crl_function; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_crl_function ON public.counter_reading_log USING btree (function_no);


--
-- Name: idx_crl_reading_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_crl_reading_date ON public.counter_reading_log USING btree (reading_date);


--
-- Name: idx_csl_component; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_csl_component ON public.component_status_log USING btree (component_id);


--
-- Name: idx_csl_component_no; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_csl_component_no ON public.component_status_log USING btree (component_no);


--
-- Name: idx_ctc_child; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ctc_child ON public.component_type_children USING btree (child_type_id);


--
-- Name: idx_ctc_parent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ctc_parent ON public.component_type_children USING btree (parent_type_id);


--
-- Name: idx_ctcd_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ctcd_type ON public.component_type_counter_def USING btree (component_type_id);


--
-- Name: idx_ctmpd_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ctmpd_type ON public.component_type_measure_point_def USING btree (component_type_id);


--
-- Name: idx_ctrt_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ctrt_type ON public.component_type_related_type USING btree (component_type_id);


--
-- Name: idx_ctst_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ctst_type ON public.component_type_stock_type USING btree (component_type_id);


--
-- Name: idx_currency_register_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_currency_register_name ON public.currency_register USING btree (name);


--
-- Name: idx_currency_register_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_currency_register_status ON public.currency_register USING btree (status);


--
-- Name: idx_discipline_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_discipline_name ON public.discipline USING btree (name);


--
-- Name: idx_discipline_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_discipline_status ON public.discipline USING btree (status);


--
-- Name: idx_fc_function; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fc_function ON public.function_counter USING btree (function_id);


--
-- Name: idx_function_criticality_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_function_criticality_active ON public.function_criticality USING btree (active);


--
-- Name: idx_job_class_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_job_class_name ON public.job_class USING btree (name);


--
-- Name: idx_job_class_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_job_class_status ON public.job_class USING btree (status);


--
-- Name: idx_location_register_installation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_location_register_installation ON public.location_register USING btree (installation_id);


--
-- Name: idx_location_register_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_location_register_name ON public.location_register USING btree (name);


--
-- Name: idx_location_register_parent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_location_register_parent ON public.location_register USING btree (parent_location_id);


--
-- Name: idx_location_register_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_location_register_status ON public.location_register USING btree (status);


--
-- Name: idx_maker_register_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_maker_register_name ON public.maker_register USING btree (name);


--
-- Name: idx_maker_register_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_maker_register_status ON public.maker_register USING btree (status);


--
-- Name: idx_mf_comp; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_mf_comp ON public.maintenance_function USING btree (installed_component_no);


--
-- Name: idx_mf_criticality; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_mf_criticality ON public.maintenance_function USING btree (criticality);


--
-- Name: idx_mf_department; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_mf_department ON public.maintenance_function USING btree (department);


--
-- Name: idx_mf_installation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_mf_installation ON public.maintenance_function USING btree (installation);


--
-- Name: idx_mf_parent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_mf_parent ON public.maintenance_function USING btree (parent_function_no);


--
-- Name: idx_mf_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_mf_status ON public.maintenance_function USING btree (status);


--
-- Name: idx_mpl_component; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_mpl_component ON public.measure_point_reading_log USING btree (component_no);


--
-- Name: idx_mpl_function; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_mpl_function ON public.measure_point_reading_log USING btree (function_no);


--
-- Name: idx_mpl_measure_point; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_mpl_measure_point ON public.measure_point_reading_log USING btree (component_measure_point_id);


--
-- Name: idx_mpl_reading_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_mpl_reading_date ON public.measure_point_reading_log USING btree (reading_date);


--
-- Name: idx_trade_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_trade_name ON public.trade USING btree (name);


--
-- Name: idx_trade_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_trade_status ON public.trade USING btree (status);


--
-- Name: idx_unit_register_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_unit_register_name ON public.unit_register USING btree (name);


--
-- Name: idx_unit_register_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_unit_register_status ON public.unit_register USING btree (status);


--
-- Name: idx_vendor_register_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_vendor_register_name ON public.vendor_register USING btree (name);


--
-- Name: idx_vendor_register_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_vendor_register_status ON public.vendor_register USING btree (status);


--
-- Name: component_counter component_counter_component_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_counter
    ADD CONSTRAINT component_counter_component_id_fkey FOREIGN KEY (component_id) REFERENCES public.component(id);


--
-- Name: component_function_rotation component_function_rotation_function_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_function_rotation
    ADD CONSTRAINT component_function_rotation_function_id_fkey FOREIGN KEY (function_id) REFERENCES public.maintenance_function(id) ON DELETE CASCADE;


--
-- Name: component_measure_point component_measure_point_component_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_measure_point
    ADD CONSTRAINT component_measure_point_component_id_fkey FOREIGN KEY (component_id) REFERENCES public.component(id);


--
-- Name: component_type fk_component_type_parent; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_type
    ADD CONSTRAINT fk_component_type_parent FOREIGN KEY (parent_id) REFERENCES public.component_type(id);


--
-- Name: component_type_children fk_ctc_child; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_type_children
    ADD CONSTRAINT fk_ctc_child FOREIGN KEY (child_type_id) REFERENCES public.component_type(id);


--
-- Name: component_type_children fk_ctc_parent; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_type_children
    ADD CONSTRAINT fk_ctc_parent FOREIGN KEY (parent_type_id) REFERENCES public.component_type(id);


--
-- Name: component_type_counter_def fk_ctcd_type; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_type_counter_def
    ADD CONSTRAINT fk_ctcd_type FOREIGN KEY (component_type_id) REFERENCES public.component_type(id);


--
-- Name: component_type_measure_point_def fk_ctmpd_type; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_type_measure_point_def
    ADD CONSTRAINT fk_ctmpd_type FOREIGN KEY (component_type_id) REFERENCES public.component_type(id);


--
-- Name: component_type_related_type fk_ctrt_related; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_type_related_type
    ADD CONSTRAINT fk_ctrt_related FOREIGN KEY (related_component_type_id) REFERENCES public.component_type(id);


--
-- Name: component_type_related_type fk_ctrt_type; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_type_related_type
    ADD CONSTRAINT fk_ctrt_type FOREIGN KEY (component_type_id) REFERENCES public.component_type(id);


--
-- Name: component_type_stock_type fk_ctst_stock; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_type_stock_type
    ADD CONSTRAINT fk_ctst_stock FOREIGN KEY (stock_type_id) REFERENCES public.stock_type(id);


--
-- Name: component_type_stock_type fk_ctst_type; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.component_type_stock_type
    ADD CONSTRAINT fk_ctst_type FOREIGN KEY (component_type_id) REFERENCES public.component_type(id);


--
-- Name: department fk_department_installation; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.department
    ADD CONSTRAINT fk_department_installation FOREIGN KEY (installation_id) REFERENCES public.installation(id);


--
-- Name: location_register fk_location_parent; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.location_register
    ADD CONSTRAINT fk_location_parent FOREIGN KEY (parent_location_id) REFERENCES public.location_register(id);


--
-- Name: user_department_access fk_uda_department; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_department_access
    ADD CONSTRAINT fk_uda_department FOREIGN KEY (department_id) REFERENCES public.department(id);


--
-- Name: user_department_access fk_uda_installation; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_department_access
    ADD CONSTRAINT fk_uda_installation FOREIGN KEY (installation_id) REFERENCES public.installation(id);


--
-- Name: user_department_access fk_uda_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_department_access
    ADD CONSTRAINT fk_uda_user FOREIGN KEY (user_id) REFERENCES public.amos_user(id);


--
-- Name: user_option fk_user_option_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_option
    ADD CONSTRAINT fk_user_option_user FOREIGN KEY (user_id) REFERENCES public.amos_user(id);


--
-- Name: user_role fk_user_role_role; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_role
    ADD CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES public.role(id);


--
-- Name: user_role fk_user_role_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_role
    ADD CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES public.amos_user(id);


--
-- Name: function_counter function_counter_function_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.function_counter
    ADD CONSTRAINT function_counter_function_id_fkey FOREIGN KEY (function_id) REFERENCES public.maintenance_function(id) ON DELETE CASCADE;


--
-- Name: stock_grade stock_grade_stock_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stock_grade
    ADD CONSTRAINT stock_grade_stock_type_id_fkey FOREIGN KEY (stock_type_id) REFERENCES public.stock_type(id);


--
-- Name: stock_type_maker stock_type_maker_stock_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stock_type_maker
    ADD CONSTRAINT stock_type_maker_stock_type_id_fkey FOREIGN KEY (stock_type_id) REFERENCES public.stock_type(id);


--
-- Name: stock_type_vendor stock_type_vendor_stock_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stock_type_vendor
    ADD CONSTRAINT stock_type_vendor_stock_type_id_fkey FOREIGN KEY (stock_type_id) REFERENCES public.stock_type(id);


--
-- PostgreSQL database dump complete
--


