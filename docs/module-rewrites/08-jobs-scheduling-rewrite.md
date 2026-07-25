# Jobs And Scheduling Rewrite

## 1. Module Goal

实现 AMOS 维护作业定义和调度规则，包括 Component Type Jobs、Component Jobs、Job Descriptions、周期/计数器/测点/触发式调度，以及依赖关系。

## 2. Required Inputs

必须先读取：

- 前端 `src/services/jobService.js`
- 前端 `src/composables/useJobTab.js`
- 前端 `src/windows/registry.js` 中 jobs/job-planning
- 前端 `src/mock/index.js` 中 `jobs`、`jobDescriptions`
- `docs/data-model.md` 中 jobs
- `docs/api-contract.md` 中 Jobs API

## 3. Manual Scope

Chapter 2：

- Maintenance Jobs，printed page 47
- Component Type Jobs，printed page 47
- Component Jobs，printed page 50
- Mandatory History and Reporting Options，printed page 61
- Spare Booking，printed page 63
- Required Disciplines，printed page 65
- Scheduling Jobs，printed page 69
- Periodic Frequencies and Planning Methods，printed page 69
- Scheduling Jobs: Counters，printed page 71
- Scheduling Jobs: Measure Points，printed page 74
- Scheduling Jobs: Triggers，printed page 76
- Job Planning Window，printed page 77
- Deactivating and Reactivating Jobs，printed page 87
- Function Driven Jobs，printed page 89

## 4. Backend Tasks

- 新增 job 聚合。
- 新增 job description register。
- 实现 type job 继承到 component job。
- 实现 required parts。
- 实现 required disciplines。
- 实现 job dependency。
- 实现 scheduling rule。
- 实现 deactivate/reactivate。
- 为 work order generation 暴露 due jobs。

## 5. API

```text
GET    /api/maintenance/jobs
POST   /api/maintenance/jobs
GET    /api/maintenance/jobs/{id}
PUT    /api/maintenance/jobs/{id}
POST   /api/maintenance/jobs/{id}/deactivate
POST   /api/maintenance/jobs/{id}/reactivate
POST   /api/maintenance/jobs/{id}/schedule
POST   /api/maintenance/jobs/{id}/copy
GET    /api/maintenance/job-descriptions
POST   /api/maintenance/job-descriptions
```

## 6. Database Tables

- `maintenance_job`
- `job_description`
- `job_required_part`
- `job_required_discipline`
- `job_dependency`
- `job_schedule_rule`

## 7. Acceptance Criteria

- 类型作业可继承到组件作业。
- 周期、计数器、测点、触发式调度字段可保存。
- Required parts/disciplines 可保存。
- 作业停用/启用有明确状态。
- Job Planning 可查询待生成工单的作业。

## 8. LLM Prompt

```text
请按照 docs/module-rewrites/08-jobs-scheduling-rewrite.md 开发 Jobs 和 Scheduling。
先对齐前端 jobService 中的继承、copy、linkable fields、dependency 逻辑，再设计后端事务和表结构。
```

