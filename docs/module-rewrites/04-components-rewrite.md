# Components Rewrite

## 1. Module Goal

实现实体组件管理。Component 是实际设备对象，后续会参与安装/拆卸、计数器、测点、作业、工单、库存备件、历史档案。

## 2. Required Inputs

必须先读取：

- 前端 `src/views/ComponentsView.vue`
- 前端 `src/views/ComponentsHierarchyView.vue`
- 前端 `src/services/componentService.js`
- 前端 `src/mock/index.js` 中 `components`
- 后端 `Component.java`
- 后端 `ComponentController.java`
- 后端 `ComponentRepository.java`
- `docs/data-model.md` 中 `component`

## 3. Manual Scope

Chapter 2：

- Components，printed page 32
- Creating Components in the Components Window，printed page 32
- Component Counters，printed page 34
- Component Measure Points，printed page 35
- Component Archive 相关行为来自前端扩展

## 4. Backend Tasks

- 完善 `component` 表字段
- 将 maker/type/location/vendor/function 从字符串逐步关联到 register/domain 表
- 增加 component list filters
- 增加 component hierarchy 查询
- 增加 component archive 查询
- 增加 component status log 查询
- 预留安装/拆卸 command，实际业务可在 `06-component-installation` 中完成

## 5. API

```text
GET    /api/maintenance/components
POST   /api/maintenance/components
GET    /api/maintenance/components/{id}
PUT    /api/maintenance/components/{id}
POST   /api/maintenance/components/{id}/change-status
GET    /api/maintenance/components/{id}/status-log
GET    /api/maintenance/components/{id}/archive
GET    /api/maintenance/components/hierarchy
```

## 6. Database Tables

核心表：

- `component`
- `component_status_log`
- `component_archive`

关联依赖：

- `component_type`
- `installation`
- `department`
- `location_register`
- `vendor_register`

## 7. Acceptance Criteria

- Components 页面可以从后端读取和保存。
- 组件状态变更必须写 status log。
- 组件按 installation/department 正确过滤。
- Components Hierarchy 可从后端获取树结构。

## 8. LLM Prompt

```text
请按照 docs/module-rewrites/04-components-rewrite.md 开发 Components。
重点是实体组件主数据、状态日志、档案和层级查询。安装/拆卸业务只预留接口或与 06 模块协调，不要把复杂流程混入普通 CRUD。
```

