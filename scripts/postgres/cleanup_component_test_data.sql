-- ============================================================
-- AMOS 清理脚本：回滚 Component Installation 浏览器验证测试数据
-- ------------------------------------------------------------
-- 对应 db/migration/V10__component_test_data.sql 造的 C-TEST-* 数据。
-- 本脚本【不】进 Flyway 自动迁移，需手动执行：
--   docker exec -i pg17-local psql -U postgres -d amos -f /dev/stdin < cleanup_component_test_data.sql
-- 或本地有 psql 时：
--   PGPASSWORD=postgres psql -h localhost -p 5432 -U postgres -d amos -f cleanup_component_test_data.sql
-- 仅清理 C-TEST-* 测试组件及其行为日志，复位 FN-ENG-01 安装状态，不影响其它真实数据。
-- H2 / PostgreSQL 双兼容，可重复执行。
-- ============================================================

-- 1) 复位 FN-ENG-01 的安装组件（仅当仍是测试组件时）
update maintenance_function
   set installed_component_no = null
 where function_no = 'FN-ENG-01'
   and installation = 'Traveller'
   and installed_component_no like 'C-TEST-%';

-- 2) 清理测试组件在安装/拆卸验证中产生的行为日志
delete from component_status_log
 where component_no like 'C-TEST-%';

delete from component_function_history
 where component_no like 'C-TEST-%';

delete from component_function_rotation
 where component_no like 'C-TEST-%';

-- 3) 删除测试组件（测试组件无子表数据，直接删）
delete from component
 where number like 'C-TEST-%';
