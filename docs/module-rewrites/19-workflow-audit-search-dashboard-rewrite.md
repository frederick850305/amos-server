# Workflow, Audit, Search, And Dashboard Rewrite

## 1. Module Goal

实现企业化横向能力：工作流通知、审批任务、审计轨迹、变更原因、全局搜索、Dashboard 指标和告警。

## 2. Required Inputs

必须先读取：

- 前端 `src/views/DashboardView.vue`
- 前端 `src/views/GlobalSearchView.vue`
- 前端 `src/views/WorkflowNotificationsView.vue`
- 前端 `src/views/OptionsView.vue`
- 前端 `src/store.js`
- 前端 `src/mock/index.js` 中 dashboard/search/notifications
- 后端 `common/GlobalExceptionHandler.java`
- 后端 `common/TenantFilter.java`

## 3. Manual Scope

Chapter 1：

- Dashboard，printed page 2
- Alerts Overview，printed page 3
- Notifications，printed page 4
- Audit Trails and Change Reasons，printed page 16
- Selecting or Changing the View，printed page 18
- Global Search，printed page 21

Chapter 4/5：

- 采购审批、合同审批、订单审批、预算影响等会产生 workflow/audit。

## 4. Backend Tasks

- 新增 workflow notification。
- 新增 approval task。
- 新增 audit log。
- 实现 change reason 捕获。
- 实现 global search API。
- 实现 dashboard summary API。
- 实现 alerts API。
- 实现 notification count API。
- 实现 user saved views / saved filters。

## 5. API

```text
GET  /api/dashboard/summary
GET  /api/dashboard/alerts
GET  /api/workflow/notifications
POST /api/workflow/notifications/{id}/acknowledge
GET  /api/workflow/tasks
POST /api/workflow/tasks/{id}/approve
POST /api/workflow/tasks/{id}/reject
GET  /api/audit/logs
GET  /api/search?q=...
GET  /api/system/me/views
POST /api/system/me/views
```

## 6. Acceptance Criteria

- Dashboard 不再依赖 Mock 汇总。
- Workflow Notifications 反映真实待办。
- Global Search 可搜索主要业务对象。
- 关键 command 写 audit log。
- 需要变更原因的动作可记录 reason。

## 7. LLM Prompt

```text
请按照 docs/module-rewrites/19-workflow-audit-search-dashboard-rewrite.md 开发 Workflow、Audit、Search 和 Dashboard。
该模块应在主要业务模块具备真实数据后逐步接入，不要用静态假数据替代业务汇总。
```

