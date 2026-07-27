-- ============================================================
-- AMOS V7：component_type_stock_type 增加 alternative_no 列
-- ------------------------------------------------------------
-- 对应手册 P37「可互换替代件编号」语义（前端 Parts 标签的
-- alternativeNo 字段）。H2(PostgreSQL 兼容模式) 与 PostgreSQL
-- 双兼容，全部 IF NOT EXISTS 幂等，遵循「不改动历史 migration」。
-- ============================================================

alter table component_type_stock_type
    add column if not exists alternative_no varchar(100);
