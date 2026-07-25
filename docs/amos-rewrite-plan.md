# AMOS M&P Rewrite Plan

This document is the execution plan for rewriting the current AMOS Vue prototype and early Java backend into a persistent AMOS-like maintenance, stock, purchasing, and financial system.

Source materials:

- User guide: `/Users/zhenghai/Documents/2.FY26/1.项目/3.海工安装船舶维修保养项目（AMOS)/客户提供/AMOS M&P Vrs.10.0.30 User Guide.pdf`
- Frontend prototype: `/Users/zhenghai/Code/fde-training-lab/prototypes/amos`
- Backend scaffold: `/Users/zhenghai/Code/fde-training-lab/prototypes/amos-server`

## 1. Current Baseline

### 1.1 Frontend

The frontend is a Vue 3 + Vite desktop-style AMOS prototype. It reproduces the AMOS shell and many business workflows with in-memory mock data.

Implemented shell capabilities:

- Dashboard, alert overview, workflow notification entry.
- Menu bar: File, Maintenance, Stock, Purchase, Budget, Tools, Window, Help.
- Toolbar actions: New, Save, Refresh, Print, View, Options.
- Left shortcut bar with module groups.
- Multi-window tab workspace.
- Filter dialog, lookup dialog, record list, record detail tabs.
- Installation and department scope filtering.
- Global search, options, switch department, lock/about dialogs.

Key frontend files:

- `src/data/amosData.js`: installations, departments, menu, page registry.
- `src/windows/registry.js`: metadata-driven business windows.
- `src/mock/index.js`: mock database and lookup data.
- `src/services/*.js`: domain logic currently implemented against mock data.
- `src/pages/index.js`: page key to component mapping.

### 1.2 Backend

The backend is a Spring Boot modular monolith scaffold.

Current modules:

- `amos-common`: common response, exception handling, CORS, JWT utility, tenant context/filter, abstract CRUD controller.
- `amos-app`: main Spring Boot app, Flyway migration, register/maintenance/stock starter domains.

Current persistence:

- Default H2 memory database in PostgreSQL compatibility mode.
- PostgreSQL profile prepared.
- Flyway migration `V1__init.sql`.

Current API coverage:

- `/api/auth/login`
- `/api/register/makers`
- `/api/maintenance/component-types`
- `/api/maintenance/components`
- `/api/maintenance/component-counters`
- `/api/stock/stock-types`
- `/api/stock/stock-type-makers`
- `/api/stock/stock-type-vendors`
- `/api/stock/stock-grades`

Current backend gap:

- Most endpoints are generic CRUD.
- Core AMOS workflows are not backend-owned yet.
- Work orders, jobs, functions, stock transactions, purchasing, quotations, deliveries, contracts, budgets, and vouchers are not yet implemented as persistent domains.

## 2. Manual-To-System Module Map

| User guide chapter | AMOS area | Rewrite modules |
| --- | --- | --- |
| Chapter 1 Getting Started | Common shell and system functions | auth, user options, dashboard, windows, filters, print, audit, installation, department, global search |
| Chapter 2 Maintenance | Maintenance | component types, components, functions, counters, measure points, jobs, scheduling, rounds, work orders, projects, report work, maintenance history/log |
| Chapter 3 Stock Management | Stock | stock types, stock items, stock locations, transactions, stock wanted, stock control, transfer documents, forecast |
| Chapter 4 Purchasing | Purchasing | forms, requisitions, queries, quotations, comparison, purchase orders, deliveries, transport documents, quality checks, claims, contracts, custom clearance |
| Chapter 5 Financials | Financials | budgets, vouchers, credit notes, budget commitments, budget hierarchy, financial impact rules |
| Appendix A | Contract import/export | contract XML import/export, validation, future integration |

## 3. Target Architecture

### 3.1 Backend Style

Use a modular monolith first. Keep domain packages separated so they can be split later if required.

Recommended package layout:

```text
com.neusoft.amos
  common
  system
  register
  maintenance
  stock
  purchase
  financial
  workflow
  audit
```

Each business module should use this structure when it grows beyond simple CRUD:

```text
module
  api          REST controllers and request/response DTOs
  application  use-case services and transaction boundaries
  domain       entities, enums, domain services
  infra        repositories, specifications, integrations
```

The current flat package style is acceptable for the first few starter entities, but new complex modules should move toward the layered structure above.

### 3.2 API Principles

- Keep generic CRUD for registers and simple master data.
- Use explicit command endpoints for business actions.
- Keep state changes server-owned.
- Keep frontend field names stable where practical to reduce migration cost from mock data.
- Return DTOs rather than exposing JPA entities once relationships become complex.
- Use pagination and filtering for all large windows.
- Always include installation/department scope where AMOS behavior depends on it.

Example endpoint style:

```text
GET    /api/maintenance/components
POST   /api/maintenance/components
POST   /api/maintenance/components/{id}/install
POST   /api/maintenance/components/{id}/remove
POST   /api/maintenance/components/{id}/change-status
GET    /api/maintenance/components/{id}/status-log
```

### 3.3 Frontend Migration Strategy

Do not rewrite the full frontend at once.

Migrate one service at a time:

1. Keep the existing page and UI behavior.
2. Replace direct mock reads/writes in the matching `src/services/*Service.js`.
3. Add a centralized API client.
4. Keep mock fallback only during transition.
5. Remove fallback later after all endpoints exist and tests pass.

Primary integration points:

- `src/services/collectionService.js`
- domain services such as `componentService.js`, `functionService.js`, `workOrderService.js`, `stockItemService.js`, `purchaseFormService.js`, `budgetService.js`

## 4. Cross-Cutting Foundations

Build these before or alongside the first domain module.

### 4.1 API Client

Frontend:

- Add `src/services/apiClient.js`.
- Configure `VITE_AMOS_API_BASE_URL`.
- Include common JSON handling.
- Include authorization header once security is enabled.
- Normalize API errors into user-facing messages.

Backend:

- Standardize success/error response shape.
- Add validation errors.
- Add OpenAPI annotations after endpoints stabilize.

### 4.2 Identity, Scope, And Audit

Minimum entities:

- `amos_user`
- `role`
- `user_role`
- `installation`
- `department`
- `user_department_access`
- `audit_log`

Rules:

- Every scoped business record should include `installation`.
- Department-scoped records should include `department`.
- Mutating business commands should write audit entries.
- Important AMOS operations should support change reason later.

### 4.3 Registers

Recommended early registers:

- Makers
- Vendors
- Locations
- Units
- Currencies
- Function criticalities
- Stock grades
- Job classes
- Trades/disciplines
- Budget codes

## 5. Development Phases

### Phase 0 - Planning And Contracts

Goal: create a stable map from manual + mock frontend to backend modules.

Deliverables:

- `docs/amos-rewrite-plan.md`
- `docs/api-contract.md`
- `docs/manual-mapping.md`
- `docs/data-model.md`

Acceptance:

- Every current frontend page has an owner module.
- Every `dataKey` in `collectionService.js` has a target endpoint or a deliberate deferred status.
- Every high-priority page has manual chapter/page references.

### Phase 1 - Master Data Backbone

Goal: persist the records that other workflows depend on.

Modules:

- System: installation, department, user scope.
- Register: makers, vendors, units, locations.
- Maintenance: component types, components, counters, measure points, functions, criticalities.
- Stock: stock types, stock makers/vendors/grades.

Acceptance:

- Frontend can list/create/update/delete the above master records through API.
- Component and function data respects installation/department filtering.
- Existing frontend filter windows still work.
- Backend tests cover CRUD and scope filtering.

### Phase 2 - Maintenance Core

Goal: make maintenance workflows persistent and server-owned.

Modules:

- Functions hierarchy.
- Component install/remove/change status.
- Component status log and component archive.
- Component type jobs and component jobs.
- Job description register.
- Scheduling: periodic, counter, measure point, trigger.
- Work order generation and status flow.
- Report work.
- Maintenance history and maintenance log.

Acceptance:

- Installing a component updates both function and component.
- Removing a component writes rotation/history/status records.
- Type jobs can generate/inherit component jobs.
- Scheduled jobs generate work orders.
- Work orders follow `Requested -> Planned -> Issued -> Completed`.
- Completed work writes maintenance history/log.

### Phase 3 - Stock Core

Goal: persist stock operations and connect them to maintenance.

Modules:

- Stock items.
- Stock locations.
- In/out/move/reverse stock transactions.
- Stock wanted calculation.
- Stock control and inventory by location.
- Transfer documents.
- Perishable stock and status handling.

Acceptance:

- Stock item movement creates balanced transactions.
- Reversing transactions is auditable and does not erase history.
- Stock wanted can generate purchase requisitions.
- Report work can consume stock and create stock transactions.

### Phase 4 - Purchasing Core

Goal: implement requisition-to-order-to-delivery.

Modules:

- Purchase forms: header, lines, additional costs, delivery destinations.
- Requisition forms.
- Query forms.
- Quotations.
- Quotation comparison scenarios and matrix.
- Purchase orders.
- Deliveries.
- Transport documents.
- Quality checks and claims.
- Purchase contracts and contract application.
- Custom clearance.

Acceptance:

- Requisition can convert to query.
- Query can collect quotations.
- Comparison can select a quotation.
- Query/requisition can convert to purchase order.
- Delivery receipt can create or update stock items.
- Quality rejection can create claim/return workflow.
- Contract application updates prices and discount rules.

### Phase 5 - Financials

Goal: connect budget and voucher impact to purchasing, stock, and maintenance.

Modules:

- Budget codes.
- Budget details.
- Budget hierarchy.
- Budget breakdown/progress/prognosis.
- Budget commitments.
- Vouchers with or without purchase form reference.
- Credit notes.
- Inter-company receivable transactions.

Acceptance:

- Purchase orders create budget commitments.
- Stock transactions can affect budget where configured.
- Maintenance log can affect budget where configured.
- Vouchers consume or adjust budget.
- Budget warnings and limits are enforced.

### Phase 6 - Workflow, Search, Reporting, Hardening

Goal: make the system operational beyond the happy path.

Modules:

- Workflow notifications.
- Approval tasks.
- Global search backend.
- Print/export.
- Audit trail and change reason enforcement.
- Permissions.
- Import/export.
- Reports and dashboards.
- Performance, pagination, and query tuning.

Acceptance:

- Workflow notification counts match pending user tasks.
- Global search can search major business objects by installation/department scope.
- Mutating operations are traceable.
- Large lists are paginated and filterable.

## 6. Module Backlog

### P0 Modules

| Module | Frontend page keys | Backend status | Main dependencies |
| --- | --- | --- | --- |
| Dashboard | `dashboard` | Not implemented | work orders, stock wanted, workflow |
| Component Types | `component-types` | Starter CRUD | makers, vendors, stock types |
| Components | `components`, `components-hierarchy`, `component-status-log`, `component-archive` | Starter CRUD | component types, functions, locations |
| Functions | `functions`, `functions-hierarchy` | Not implemented | locations, criticalities |
| Work Orders | `work-orders`, `work-planning`, `report-work` | Not implemented | jobs, components, stock |
| Stock Items | `stock-items`, `transactions` | Not implemented | stock types, locations |
| Stock Wanted | `wanted` | Not implemented | stock items, purchase forms |
| Purchase Forms | `forms` | Not implemented | vendors, stock wanted, budget |
| Quotation Comparison | `quotation-comparison` | Not implemented | quotations, purchase forms |
| Budget | `budgets` | Not implemented | purchase orders, vouchers |
| Vouchers | `vouchers` | Not implemented | purchase forms, budget |

### P1 Modules

| Module | Frontend page keys | Backend status | Main dependencies |
| --- | --- | --- | --- |
| Component Jobs | `component-type-jobs`, `component-jobs`, `job-planning` | Not implemented | component types, components, counters |
| Rounds | `rounds` | Not implemented | jobs, work orders |
| Projects | `projects` | Not implemented | jobs, work orders, budget |
| Stock Types | `stock-types` | Starter CRUD | makers, vendors, grades |
| Transfer Documents | `transfer-documents` | Not implemented | stock items, workflow |
| Quotations | `quotations` | Not implemented | purchase forms, vendors |
| Deliveries | `deliveries` | Not implemented | purchase orders, stock items |
| Quality Checks | `quality-checks` | Not implemented | deliveries |
| Contracts | `contracts` | Not implemented | vendors, stock types, purchase forms |

### P2 Modules

| Module | Frontend page keys | Backend status | Main dependencies |
| --- | --- | --- | --- |
| Options | `options` | Not implemented | users |
| Global Search | `global-search` | Not implemented | all searchable modules |
| Workflow Notifications | `workflow-notifications` | Not implemented | workflow |
| Counters Overview | `counters-overview`, `update-counters`, `update-measure-points` | Partial counter CRUD | components, functions, jobs |
| Maintenance History/Log | `maintenance-history`, `maintenance-log` | Not implemented | report work |
| Transport Documents | `transport-documents` | Not implemented | deliveries |
| Custom Clearance | `custom-clearance-forms` | Not implemented | purchase, contracts |

## 7. Suggested Execution Order

1. Add API contract and mock-to-endpoint map.
2. Add frontend API client.
3. Finish registers: maker, vendor, unit, location, criticality.
4. Finish Component Types backend and frontend API integration.
5. Finish Components backend and frontend API integration.
6. Add Functions backend and frontend API integration.
7. Implement install/remove component commands.
8. Implement counters and measure points.
9. Implement jobs and job descriptions.
10. Implement work order generation and status flow.
11. Implement report work and maintenance history/log.
12. Implement stock types fully.
13. Implement stock items and stock transactions.
14. Implement stock wanted and requisition generation.
15. Implement purchasing forms.
16. Implement quotations and comparison.
17. Implement purchase orders and deliveries.
18. Implement quality checks and claims.
19. Implement contracts.
20. Implement budgets.
21. Implement vouchers.
22. Implement workflow notifications, global search, audit trail, print/export.

## 8. Per-Module AI Work Template

Use this prompt for each module.

```text
请基于 AMOS 用户手册中 [模块名/章节页码]，以及当前前端
/Users/zhenghai/Code/fde-training-lab/prototypes/amos 的 Mock 页面和 service，
开发后端 /Users/zhenghai/Code/fde-training-lab/prototypes/amos-server 中的 [模块名]。

要求：
1. 先列出前端字段、Mock 数据、页面动作、手册业务规则。
2. 设计数据库表、实体、Repository、Service、Controller。
3. 不只做 CRUD，要实现页面里的核心业务动作。
4. 补充 Flyway migration。
5. 补充基础测试。
6. 最后把前端对应 service 从 Mock 改为调用 API。
7. 不执行任何批量删除操作。
```

## 9. Definition Of Done

Each module is complete only when all items below are true:

- Manual behavior is mapped to explicit backend rules.
- Database tables and indexes exist through Flyway.
- API endpoints are documented in `docs/api-contract.md`.
- Backend tests cover at least create/list/update and major business commands.
- Frontend service uses the API instead of direct mock mutation.
- Existing UI still opens and performs the workflow.
- Error states are handled visibly in the frontend.
- No unrelated files are modified.
- No batch deletion is performed.

## 10. First Implementation Slice

Recommended first concrete slice: Component Types.

Why:

- It already has frontend pages and backend starter CRUD.
- It is a dependency for components, component jobs, counters, measure points, and spare parts.
- It is small enough to establish conventions before larger workflows.

Scope:

- Confirm frontend fields from `component-types` window registry.
- Extend backend entity/table to cover missing fields used by the frontend.
- Add child tables for type counters, measure point definitions, related types, and parts links.
- Add filtering by status, maker, class, type number, name.
- Add create/update endpoints with child collections.
- Add register-as-component command.
- Update frontend `componentService` or `collectionService` to use the backend endpoint.
- Add tests for CRUD, filters, and register-as-component.

