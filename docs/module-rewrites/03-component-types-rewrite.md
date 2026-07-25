# Component Types Rewrite

## 1. Module Goal

将 Component Types 从前端 Mock 转写为后端持久化聚合，支持组件类型基本信息、计数器模板、测点模板、相关类型、备件关联，以及 Register as Component。

## 2. Detailed Slice

本模块已有更详细任务书：

- `docs/component-types-slice.md`

后续开发应优先读取该文件。

## 3. Required Inputs

必须先读取：

- `docs/component-types-slice.md`
- 前端 `src/windows/registry.js` 中 `component-types`
- 前端 `src/views/ComponentTypesView.vue`
- 前端 `src/services/componentService.js`
- 前端 `src/mock/index.js` 中 `componentTypes`
- 后端 `ComponentType.java`
- 后端 `ComponentTypeController.java`
- 后端 `ComponentTypeRepository.java`
- 后端 `V1__init.sql`

## 4. Manual Scope

Chapter 2：

- Component Types，printed page 26
- Defining a Component Type，printed page 26
- Working with Component Types，printed page 27
- Registering a Component Type as a Component，printed page 30
- Component Counters，printed page 34
- Component Measure Points，printed page 35
- Stock Types in Component Types，printed page 180

## 5. Backend Tasks

- 扩展 `component_type`
- 新增 child tables：
  - `component_type_counter_def`
  - `component_type_measure_point_def`
  - `component_type_related_type`
  - `component_type_stock_type`
- 新增 DTO 和 service
- 将 Controller 从纯 CRUD 升级为聚合 API
- 实现 filters
- 实现 register-as-component command

## 6. API

```text
GET    /api/maintenance/component-types
POST   /api/maintenance/component-types
GET    /api/maintenance/component-types/{id}
PUT    /api/maintenance/component-types/{id}
DELETE /api/maintenance/component-types/{id}
POST   /api/maintenance/component-types/{id}/register-component
```

## 7. Acceptance Criteria

- Component Types 页面主数据来自后端。
- 子表可保存和回显。
- Register as Component 可创建 Component。
- 过滤条件和现有 UI 行为保持稳定。

## 8. LLM Prompt

```text
请按照 docs/component-types-slice.md 和 docs/module-rewrites/03-component-types-rewrite.md 开发 Component Types。
这是第一个完整闭环模块，请建立后续模块可复用的 Flyway、DTO、Service、Controller、测试和前端 API 集成模式。
```

