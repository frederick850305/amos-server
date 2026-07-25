# Maintenance History, Projects, And Rounds Rewrite

## 1. Module Goal

补齐维护历史、维护日志、维护项目和维护轮次。该模块依赖 Jobs 和 Work Orders，可在核心工单流程完成后开发。

## 2. Required Inputs

必须先读取：

- 前端 `src/windows/registry.js` 中 rounds/projects/maintenance-history/maintenance-log
- 前端 `src/mock/index.js` 中相关集合
- 前端 `src/services/jobService.js`
- 前端 `src/services/workOrderService.js`
- `docs/manual-mapping.md` Chapter 2 Projects/Rounds/History

## 3. Manual Scope

Chapter 2：

- Maintenance Rounds，printed page 80
- Defining a Round，printed page 81
- Scheduling a Round，printed page 81
- Allocating Jobs to a Round，printed page 82
- Round Work Orders，printed page 92
- Maintenance Projects，printed page 120
- Project Sections，printed page 121
- Project Jobs，printed page 123
- Project Work Orders，printed page 132
- Sub-Contracting Project Jobs，printed page 136
- Project Costs，printed page 146
- Maintenance History，printed page 170
- Maintenance Log，printed page 170

## 4. Backend Tasks

- 新增 rounds。
- 新增 round jobs。
- 新增 round scheduling。
- 新增 project。
- 新增 project sections。
- 新增 project jobs。
- 新增 project work orders。
- 新增 project cost summary。
- 实现 maintenance history/log 查询。

## 5. API

```text
GET  /api/maintenance/rounds
POST /api/maintenance/rounds
POST /api/maintenance/rounds/{id}/schedule
POST /api/maintenance/rounds/{id}/generate-work-orders

GET  /api/maintenance/projects
POST /api/maintenance/projects
POST /api/maintenance/projects/{id}/copy
GET  /api/maintenance/projects/{id}/costs

GET  /api/maintenance/history
GET  /api/maintenance/log
```

## 6. Acceptance Criteria

- Rounds 可定义并生成 round work orders。
- Projects 可维护 section/job/work order。
- History 和 Log 从真实完成工单生成。
- 成本信息可供 Financials 后续引用。

## 7. LLM Prompt

```text
请按照 docs/module-rewrites/10-maintenance-history-projects-rounds-rewrite.md 开发 Maintenance History、Projects 和 Rounds。
该模块应在 Jobs 与 Work Orders 完成后执行，避免提前伪造业务联动。
```

