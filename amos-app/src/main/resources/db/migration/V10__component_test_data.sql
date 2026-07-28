-- ============================================================
-- AMOS V10：Component Installation 浏览器端验证测试数据
-- ------------------------------------------------------------
-- 目的：为「模块 06 Component Installation」手工/浏览器验证造数据。
-- 配套已有的 V9 种子 function：FN-ENG-01/02/03（Engine Room）、FN-DECK-01（Deck），
-- 均属 installation=Traveller。
-- 本 migration 全部 IF NOT EXISTS 幂等，可重复执行；服务启动即自动应用（postgres profile）。
-- 验证覆盖：
--   场景 A 安装写日志 / 场景 C 拆卸写日志 / 场景 D BusinessWindow 改组件
--     -> 用 C-TEST-01..05（status=Available，未安装）
--   场景 A「已装别处拒绝」/ 场景 B「重复安装拒绝」
--     -> 用 C-TEST-10（已预装在 FN-ENG-01，function_no + function.installed_component_no 同步）
--   场景 C「空 function 拆卸拒绝」
--     -> 用 FN-DECK-01（本身未装组件，天然可触发该拒绝）
-- H2 / PostgreSQL 双兼容。
-- ============================================================

-- 注意：ComponentsView 的 scopeByDepartment 同时按「当前部门」过滤，而系统种子里
-- department 的 code 是 'ER'（name=Engine Room），且 department 表仅存在 'ER' 一个部门。
-- 因此测试组件的 department 必须写部门 code 'ER'（而非名称），否则会被前端作用域过滤隐藏。
-- location 字段保留物理位置（Engine Room / Deck）以维持语义。

-- 1) 待安装组件（status=Available，未装任何 function），覆盖多个 maker / type
insert into component (number, type_number, name, status, maker, type, serial_no, location, department, vendor, installation)
select 'C-TEST-01', 'CT-1001', 'ME Cylinder #2',            'Available', 'WART',  'Liner',       'SN-WART-2001', 'Engine Room', 'ER', 'Wärtsilä Marine', 'Traveller'
where not exists (select 1 from component where number = 'C-TEST-01');

insert into component (number, type_number, name, status, maker, type, serial_no, location, department, vendor, installation)
select 'C-TEST-02', 'CT-1002', 'Aux Boiler Feed Pump A',    'Available', 'GRUN',  'Pump',        'SN-GRUN-2002', 'Engine Room', 'ER', 'Grundfos',        'Traveller'
where not exists (select 1 from component where number = 'C-TEST-02');

insert into component (number, type_number, name, status, maker, type, serial_no, location, department, vendor, installation)
select 'C-TEST-03', 'CT-1003', 'Compressor Unit B',         'Available', 'ALFA',  'Compressor',  'SN-ALFA-2003', 'Engine Room', 'ER', 'Alfa Laval',      'Traveller'
where not exists (select 1 from component where number = 'C-TEST-03');

insert into component (number, type_number, name, status, maker, type, serial_no, location, department, vendor, installation)
select 'C-TEST-04', 'CT-1004', 'Main Switchboard MCC',      'Available', 'ABBM',  'Switchboard', 'SN-ABBM-2004', 'Engine Room', 'ER', 'ABB Marine',      'Traveller'
where not exists (select 1 from component where number = 'C-TEST-04');

insert into component (number, type_number, name, status, maker, type, serial_no, location, department, vendor, installation)
select 'C-TEST-05', 'CT-1005', 'Deck Crane Hoist Motor',   'Available', 'MACG',  'Motor',       'SN-MACG-2005', 'Deck',        'ER', 'MacGregor',       'Traveller'
where not exists (select 1 from component where number = 'C-TEST-05');

-- 2) 预装组件（用于触发「已装别处拒绝」/「重复安装拒绝」）：C-TEST-10 已装在 FN-ENG-01
insert into component (number, type_number, name, status, maker, type, serial_no, location, department, vendor, function_no, install_date, installation)
select 'C-TEST-10', 'CT-1001', 'ME Piston #1',             'In Use', 'WART', 'Piston', 'SN-WART-2010', 'Engine Room', 'ER', 'Wärtsilä Marine', 'FN-ENG-01', '2026-01-15', 'Traveller'
where not exists (select 1 from component where number = 'C-TEST-10');

-- 同步 function 的 installed_component_no，保持 UI 状态一致（仅当该 function 当前未装组件时）
update maintenance_function
   set installed_component_no = 'C-TEST-10'
 where function_no = 'FN-ENG-01'
   and installation = 'Traveller'
   and (installed_component_no is null or installed_component_no = '');
