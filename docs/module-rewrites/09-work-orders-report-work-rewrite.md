# Work Orders And Report Work Rewrite

## 1. Module Goal

实现维护工单生命周期和工作上报：生成工单、计划、签发、完成、消耗资源/备件、记录历史、更新维护日志。

## 2. Required Inputs

必须先读取：

- 前端 `src/services/workOrderService.js`
- 前端 `src/views/WorkOrdersView.vue`
- 前端 `src/windows/registry.js` 中 work-planning/report-work
- 前端 `src/mock/index.js` 中 `workOrders`、`maintenanceLog`
- `docs/data-model.md` 中 work_order 和 maintenance_history

## 3. Manual Scope

Chapter 2：

- Initial Work Orders，printed page 78
- Handling Maintenance，printed page 92
- Maintenance Work Orders，printed page 93
- Generating Work Orders，printed page 93
- One-Off Work Orders，printed page 94
- Requisition Work，printed page 95
- Planning Work Orders，printed page 101
- Work Orders Window，printed page 101
- Work Planning Window，printed page 112
- Reporting Work，printed page 155
- Reporting Resources Used，printed page 157
- Reporting Stock Used，printed page 160
- Reporting History，printed page 161
- Reporting Overdue Work，printed page 163
- Reporting Unplanned Maintenance，printed page 163

## 4. Backend Tasks

- 新增 work order 聚合。
- 实现状态流：Requested -> Planned -> Issued -> Completed。
- 实现 generate work orders。
- 实现 one-off work order。
- 实现 plan/issue/complete commands。
- 实现 report work command。
- Report work 与 stock transaction、maintenance history、maintenance log 联动。

## 5. API

```text
GET  /api/maintenance/work-orders
POST /api/maintenance/work-orders
POST /api/maintenance/work-orders/generate
POST /api/maintenance/work-orders/{id}/plan
POST /api/maintenance/work-orders/{id}/issue
POST /api/maintenance/work-orders/{id}/complete
POST /api/maintenance/work-orders/{id}/report-work
GET  /api/maintenance/work-planning
GET  /api/maintenance/report-work
```

## 6. Database Tables

- `work_order`
- `work_order_resource`
- `work_order_part`
- `work_order_report`
- `maintenance_history`
- `maintenance_log`

## 7. Acceptance Criteria

- 工单状态只能按允许路径流转。
- 生成工单不会重复生成同一到期作业。
- 完成工单会写 history/log。
- 上报备件会创建库存交易。
- Work Orders 页面和 Report Work 页面后端化。

## 8. LLM Prompt

```text
请按照 docs/module-rewrites/09-work-orders-report-work-rewrite.md 开发 Work Orders 和 Report Work。
重点是工单状态机和 report-work 对 maintenance history、maintenance log、stock transaction 的联动。
```

