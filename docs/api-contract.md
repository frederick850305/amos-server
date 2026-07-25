# AMOS API Contract

This document maps the current Vue prototype data keys and page actions to target backend APIs.

Status values:

- `existing`: endpoint exists in the backend.
- `planned`: endpoint should be implemented.
- `deferred`: intentionally delayed.

## 1. Conventions

Base URL:

```text
/api
```

Common query parameters for list endpoints:

| Parameter | Meaning |
| --- | --- |
| `installation` | Installation/vessel scope, for records that are vessel-specific. |
| `department` | Department/work-scope filter, for department-scoped records. |
| `page` | Zero-based page index. |
| `size` | Page size. |
| `sort` | Sort field and direction. |
| `q` | Generic text search. |

Recommended response shape:

```json
{
  "success": true,
  "data": {},
  "message": ""
}
```

Recommended error shape:

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "Human readable message",
  "details": []
}
```

## 2. Frontend DataKey To Endpoint Map

| Frontend dataKey | Current frontend area | Target endpoint | Status | Priority |
| --- | --- | --- | --- | --- |
| `componentTypes` | Component Types | `/api/maintenance/component-types` | existing | P0 |
| `components` | Components | `/api/maintenance/components` | existing | P0 |
| `componentStatusLog` | Component Status Log | `/api/maintenance/component-status-logs` | planned | P0 |
| `functions` | Functions | `/api/maintenance/functions` | planned | P0 |
| `jobs` | Component Type Jobs, Component Jobs, Job Planning | `/api/maintenance/jobs` | planned | P0 |
| `workOrders` | Work Orders, Work Planning, Report Work | `/api/maintenance/work-orders` | planned | P0 |
| `stockTypes` | Stock Types | `/api/stock/stock-types` | existing | P0 |
| `stockItems` | Stock Items | `/api/stock/stock-items` | planned | P0 |
| `stockWanted` | Stock Wanted | `/api/stock/wanted` | planned | P0 |
| `transactions` | Stock Transactions | `/api/stock/transactions` | planned | P0 |
| `stockTypeMakers` | Stock Type Makers | `/api/stock/stock-type-makers` | existing | P1 |
| `stockTypeVendors` | Stock Type Vendors | `/api/stock/stock-type-vendors` | existing | P1 |
| `stockGrades` | Stock Grades | `/api/stock/stock-grades` | existing | P1 |
| `transferDocs` | Transfer Documents | `/api/stock/transfer-documents` | planned | P1 |
| `purchaseForms` | Forms | `/api/purchase/forms` | planned | P0 |
| `quotations` | Quotations | `/api/purchase/quotations` | planned | P0 |
| `deliveries` | Deliveries | `/api/purchase/deliveries` | planned | P1 |
| `transportDocs` | Transport Documents | `/api/purchase/transport-documents` | planned | P2 |
| `qualityChecks` | Quality Checks | `/api/purchase/quality-checks` | planned | P1 |
| `contracts` | Contracts | `/api/purchase/contracts` | planned | P1 |
| `budgets` | Budget | `/api/financial/budgets` | planned | P0 |
| `vouchers` | Vouchers | `/api/financial/vouchers` | planned | P0 |
| `counterLogs` | Counter/Measure Logs | `/api/maintenance/counter-logs` | planned | P1 |
| `measureLogs` | Measure Point Logs | `/api/maintenance/measure-logs` | planned | P1 |
| `projects` | Projects | `/api/maintenance/projects` | planned | P1 |
| `maintenanceLog` | Maintenance Log | `/api/maintenance/log` | planned | P1 |
| `jobDescriptions` | Job Description register | `/api/maintenance/job-descriptions` | planned | P1 |
| `functionCriticalities` | Function Criticality register | `/api/register/function-criticalities` | planned | P1 |

## 3. Existing Endpoint Inventory

| Endpoint | Domain | Current behavior | Required upgrade |
| --- | --- | --- | --- |
| `POST /api/auth/login` | common | Login scaffold | Connect real user store and roles. |
| `/api/register/makers` | register | Generic CRUD | Add search, validation, duplicate checks. |
| `/api/maintenance/component-types` | maintenance | Generic CRUD | Add child collections, filters, register-as-component command. |
| `/api/maintenance/components` | maintenance | Generic CRUD | Add install/remove/change-status commands and status logs. |
| `/api/maintenance/component-counters` | maintenance | Generic CRUD | Add reading update and function counter propagation. |
| `/api/stock/stock-types` | stock | Generic CRUD | Add makers/vendors/grades as stable child collections. |
| `/api/stock/stock-type-makers` | stock | Generic CRUD | Add uniqueness per stock type/maker/ref. |
| `/api/stock/stock-type-vendors` | stock | Generic CRUD | Add lead time and approved vendor logic. |
| `/api/stock/stock-grades` | stock | Generic CRUD | Add stock-type-scoped validation. |

## 4. Maintenance API Backlog

### 4.1 Component Types

```text
GET    /api/maintenance/component-types
POST   /api/maintenance/component-types
GET    /api/maintenance/component-types/{id}
PUT    /api/maintenance/component-types/{id}
DELETE /api/maintenance/component-types/{id}
POST   /api/maintenance/component-types/{id}/register-component
GET    /api/maintenance/component-types/{id}/jobs
GET    /api/maintenance/component-types/{id}/parts
```

Filters:

- `status`
- `maker`
- `classCode`
- `typeNumber`
- `name`

Child collections:

- counters
- measure point definitions
- related component types
- parts / stock type links
- jobs

### 4.2 Components

```text
GET    /api/maintenance/components
POST   /api/maintenance/components
GET    /api/maintenance/components/{id}
PUT    /api/maintenance/components/{id}
POST   /api/maintenance/components/{id}/install
POST   /api/maintenance/components/{id}/remove
POST   /api/maintenance/components/{id}/change-status
GET    /api/maintenance/components/{id}/status-log
GET    /api/maintenance/components/{id}/function-history
GET    /api/maintenance/components/{id}/archive
```

Commands:

- Install component on function.
- Remove component from function.
- Change component status.
- Transfer component between departments.
- Update counters.
- Update measure points.

### 4.3 Functions

```text
GET    /api/maintenance/functions
POST   /api/maintenance/functions
GET    /api/maintenance/functions/{id}
PUT    /api/maintenance/functions/{id}
POST   /api/maintenance/functions/{id}/install-component
POST   /api/maintenance/functions/{id}/remove-component
POST   /api/maintenance/functions/{id}/change-status
GET    /api/maintenance/functions/hierarchy
GET    /api/maintenance/functions/{id}/rotation-log
```

Rules:

- A function can have at most one currently installed component.
- A function with an installed component cannot be changed to scrapped.
- Component installation updates component status and location.
- Removal writes rotation log and component status log.

### 4.4 Jobs And Work Orders

```text
GET    /api/maintenance/jobs
POST   /api/maintenance/jobs
PUT    /api/maintenance/jobs/{id}
POST   /api/maintenance/jobs/{id}/deactivate
POST   /api/maintenance/jobs/{id}/reactivate
POST   /api/maintenance/jobs/{id}/schedule
POST   /api/maintenance/jobs/{id}/generate-work-order

GET    /api/maintenance/work-orders
POST   /api/maintenance/work-orders
POST   /api/maintenance/work-orders/{id}/plan
POST   /api/maintenance/work-orders/{id}/issue
POST   /api/maintenance/work-orders/{id}/complete
POST   /api/maintenance/work-orders/{id}/report-work
```

Work order status flow:

```text
Requested -> Planned -> Issued -> Completed
```

## 5. Stock API Backlog

```text
GET    /api/stock/stock-items
POST   /api/stock/stock-items
PUT    /api/stock/stock-items/{id}
POST   /api/stock/stock-items/{id}/move
POST   /api/stock/stock-items/{id}/set-status
GET    /api/stock/transactions
POST   /api/stock/transactions/{id}/reverse
POST   /api/stock/wanted/calculate
POST   /api/stock/wanted/generate-requisition
GET    /api/stock/transfer-documents
POST   /api/stock/transfer-documents
POST   /api/stock/transfer-documents/{id}/submit
POST   /api/stock/transfer-documents/{id}/approve
POST   /api/stock/transfer-documents/{id}/transfer
POST   /api/stock/transfer-documents/{id}/receive
```

## 6. Purchase API Backlog

```text
GET    /api/purchase/forms
POST   /api/purchase/forms
POST   /api/purchase/forms/{id}/convert-to-query
POST   /api/purchase/forms/{id}/convert-to-purchase-order
POST   /api/purchase/forms/{id}/apply-contract
POST   /api/purchase/forms/{id}/approve

GET    /api/purchase/quotations
POST   /api/purchase/quotations
POST   /api/purchase/quotation-comparisons
POST   /api/purchase/quotation-comparisons/{id}/propose
POST   /api/purchase/quotation-comparisons/{id}/approve
POST   /api/purchase/quotation-comparisons/{id}/select

GET    /api/purchase/deliveries
POST   /api/purchase/deliveries
POST   /api/purchase/deliveries/{id}/receive

GET    /api/purchase/quality-checks
POST   /api/purchase/quality-checks
POST   /api/purchase/quality-checks/{id}/escalate-claim
```

## 7. Financial API Backlog

```text
GET    /api/financial/budgets
POST   /api/financial/budgets
PUT    /api/financial/budgets/{id}
POST   /api/financial/budgets/{id}/copy
GET    /api/financial/budgets/{id}/progress

GET    /api/financial/vouchers
POST   /api/financial/vouchers
PUT    /api/financial/vouchers/{id}
POST   /api/financial/vouchers/{id}/approve
POST   /api/financial/vouchers/{id}/credit-note
```

## 8. Frontend Migration Checklist

For each service:

- Add API calls.
- Preserve current method names where possible.
- Keep the UI component unchanged unless the API requires a deliberate contract change.
- Replace direct `db.*` mutation with service commands.
- Handle loading and error states.
- Validate the workflow in the browser.

