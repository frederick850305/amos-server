# Financials Rewrite

## 1. Module Goal

实现预算、预算层级、预算占用、预算进度、凭证、发票、贷项，以及采购/库存/维护对预算的影响。

## 2. Required Inputs

必须先读取：

- 前端 `src/views/BudgetsView.vue`
- 前端 `src/views/VouchersView.vue`
- 前端 `src/services/budgetService.js`
- 前端 `src/services/voucherService.js`
- 前端 `src/mock/index.js` 中 `budgets`、`vouchers`
- `docs/data-model.md` 中 Financial

## 3. Manual Scope

Chapter 5：

- Creating a Budget，printed page 348
- Defining a Budget Code，printed page 348
- Adding Budget Details，printed page 348
- Copying a Budget，printed page 351
- Budget Specifications，printed page 351
- Vouchers，printed page 352
- Creating an Invoice for a Purchase Form，printed page 352
- Creating an Invoice Without Reference，printed page 353
- Voucher Amounts, Discounts and Budget Codes，printed page 354
- Credit Notes，printed page 355
- Budget Breakdowns and Progress，printed page 355
- Budget Commitment Control，printed page 356
- Budget Warnings and Limits，printed page 357
- Budget Commitment Formula，printed page 358
- Budget Hierarchies，printed page 359
- Elements Which May Affect the Budget，printed page 360

## 4. Backend Tasks

- 新增 budget。
- 新增 budget hierarchy。
- 新增 budget transaction。
- 新增 voucher。
- 新增 voucher line。
- 新增 credit note。
- 实现 budget progress。
- 实现 purchase order commitment。
- 实现 voucher actual impact。
- 预留 stock transaction 和 maintenance log 对预算影响。

## 5. API

```text
GET  /api/financial/budgets
POST /api/financial/budgets
GET  /api/financial/budgets/{id}
PUT  /api/financial/budgets/{id}
POST /api/financial/budgets/{id}/copy
GET  /api/financial/budgets/{id}/progress
GET  /api/financial/budget-transactions

GET  /api/financial/vouchers
POST /api/financial/vouchers
GET  /api/financial/vouchers/{id}
PUT  /api/financial/vouchers/{id}
POST /api/financial/vouchers/{id}/approve
POST /api/financial/vouchers/{id}/credit-note
```

## 6. Acceptance Criteria

- Budgets 和 Vouchers 页面后端化。
- voucher total = net - discount + VAT + additional costs。
- PO 审批可产生 budget commitment。
- Voucher 审批可产生 actual budget transaction。
- 预算超限/预警规则可测试。

## 7. LLM Prompt

```text
请按照 docs/module-rewrites/18-financials-rewrite.md 开发 Financials。
先实现 Budget、Voucher 和 Budget Transaction，再逐步接入 Purchase Order、Stock Transaction、Maintenance Log 的预算影响。
```

