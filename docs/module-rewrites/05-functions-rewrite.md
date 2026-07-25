# Functions Rewrite

## 1. Module Goal

实现 AMOS 的固定功能位置管理。Function 表示船舶上的功能位置或系统位置，是组件安装、维护作业和计数器同步的核心对象。

## 2. Required Inputs

必须先读取：

- 前端 `src/windows/registry.js` 中 `functions`
- 前端 `src/views/FunctionsHierarchyView.vue`
- 前端 `src/services/functionService.js`
- 前端 `src/mock/index.js` 中 `functions`
- `docs/data-model.md` 中 `function`
- `docs/api-contract.md` 中 Functions API

## 3. Manual Scope

Chapter 2：

- Functions，printed page 36
- Working with Functions，printed page 36
- Function Criticality，printed page 44
- Function Counters，printed page 46

## 4. Backend Tasks

- 新增 `function` 表和实体，建议 Java 命名为 `MaintenanceFunction`，避免与 Java/function 概念混淆。
- 实现 parent/child hierarchy。
- 实现 function list filters。
- 关联 location、criticality、installation、department。
- 支持 installed component 只读信息。
- 支持 rotation log 查询。
- 状态变更规则：装有组件的 function 不能随意 scrapped。

## 5. API

```text
GET    /api/maintenance/functions
POST   /api/maintenance/functions
GET    /api/maintenance/functions/{id}
PUT    /api/maintenance/functions/{id}
GET    /api/maintenance/functions/hierarchy
POST   /api/maintenance/functions/{id}/change-status
GET    /api/maintenance/functions/{id}/rotation-log
GET    /api/maintenance/functions/{id}/installed-component
```

## 6. Database Tables

- `maintenance_function`
- `function_counter`
- `component_function_rotation`

## 7. Acceptance Criteria

- Functions 和 Functions Hierarchy 均从后端读取。
- parent/child 树结构正确。
- 关键性颜色/degree 可显示。
- 当前安装组件信息可查看。
- installation/department scope 生效。

## 8. LLM Prompt

```text
请按照 docs/module-rewrites/05-functions-rewrite.md 开发 Functions。
先分析前端 functions registry 和 functionService，再设计 maintenance_function 表、层级查询、DTO 和状态规则。
```

