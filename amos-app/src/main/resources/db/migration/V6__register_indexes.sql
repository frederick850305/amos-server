-- ============================================================
-- AMOS V6：Registers 辅助索引（分页 / 过滤 / 排序加速）
-- 纯增量，H2(PostgreSQL 兼容模式) 与 PostgreSQL 双兼容。
-- 不改变既有表结构，仅补充二级索引；可重放。
-- 说明：
--   * status / active 为列表 ?status / ?active 过滤的常用条件；
--   * name 为 ?q 模糊搜索与排序（sort=name）的高频列；
--   * location_register.installation_id / parent_location_id 为
--     ?installation / ?parentId 过滤条件（已下沉到查询级）。
-- ============================================================

-- maker_register
create index if not exists idx_maker_register_status on maker_register (status);
create index if not exists idx_maker_register_name   on maker_register (name);

-- vendor_register
create index if not exists idx_vendor_register_status on vendor_register (status);
create index if not exists idx_vendor_register_name   on vendor_register (name);

-- location_register
create index if not exists idx_location_register_status       on location_register (status);
create index if not exists idx_location_register_name         on location_register (name);
create index if not exists idx_location_register_installation on location_register (installation_id);
create index if not exists idx_location_register_parent       on location_register (parent_location_id);

-- unit_register
create index if not exists idx_unit_register_status on unit_register (status);
create index if not exists idx_unit_register_name   on unit_register (name);

-- currency_register
create index if not exists idx_currency_register_status on currency_register (status);
create index if not exists idx_currency_register_name   on currency_register (name);

-- function_criticality（active 布尔过滤；该表无 name 列，仅对 active 建索引）
create index if not exists idx_function_criticality_active on function_criticality (active);

-- job_class
create index if not exists idx_job_class_status on job_class (status);
create index if not exists idx_job_class_name   on job_class (name);

-- trade
create index if not exists idx_trade_status on trade (status);
create index if not exists idx_trade_name   on trade (name);

-- discipline
create index if not exists idx_discipline_status on discipline (status);
create index if not exists idx_discipline_name   on discipline (name);

-- budget_code
create index if not exists idx_budget_code_status on budget_code (status);
create index if not exists idx_budget_code_name   on budget_code (name);
