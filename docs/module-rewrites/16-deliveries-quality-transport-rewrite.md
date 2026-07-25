# Deliveries, Quality, And Transport Rewrite

## 1. Module Goal

实现采购订单到货、中转运输单据、收货入库、质检、索赔、退货流程。

## 2. Required Inputs

必须先读取：

- 前端 `src/windows/registry.js` 中 `deliveries`、`transport-documents`、`quality-checks`
- 前端 `src/mock/index.js` 中 `deliveries`、`transportDocs`、`qualityChecks`
- 前端 `src/services/stockItemService.js`
- `docs/data-model.md` 中 delivery、quality_check、stock_transaction

## 3. Manual Scope

Chapter 4：

- Deliveries，printed page 278
- Registering Deliveries on a Form，printed page 279
- Line Items on Deliveries，printed page 279
- Deliveries - Intermediate Locations，printed page 284
- Transport Documents，printed page 287
- Creating a Transport Document，printed page 287
- Adding Deliveries to a Transport Document，printed page 288
- Receiving a Delivery，printed page 294
- Quality Checks and Claims，printed page 296
- Quality Checks for an Entire Form，printed page 296
- Quality Checks for Items on a Form，printed page 297
- Escalating a Check to a Claim，printed page 300
- Returning Rejected Items，printed page 302

## 4. Backend Tasks

- 新增 delivery。
- 新增 delivery line。
- 新增 transport document。
- 新增 transport document delivery link。
- 实现 receive delivery command。
- receive 后创建/更新 stock items。
- 实现 quality check。
- 实现 claim。
- 实现 rejected item return。

## 5. API

```text
GET  /api/purchase/deliveries
POST /api/purchase/deliveries
POST /api/purchase/deliveries/{id}/receive

GET  /api/purchase/transport-documents
POST /api/purchase/transport-documents
POST /api/purchase/transport-documents/{id}/add-delivery
POST /api/purchase/transport-documents/{id}/receive

GET  /api/purchase/quality-checks
POST /api/purchase/quality-checks
POST /api/purchase/quality-checks/{id}/escalate-claim
POST /api/purchase/quality-checks/{id}/return-rejected-items
```

## 6. Acceptance Criteria

- PO 可以登记一次或多次 delivery。
- 收货会影响 stock item 和 stock transaction。
- 质检拒收不会错误入可用库存。
- 索赔和退货有状态记录。

## 7. LLM Prompt

```text
请按照 docs/module-rewrites/16-deliveries-quality-transport-rewrite.md 开发 Deliveries、Transport Documents 和 Quality Checks。
重点处理收货入库与库存交易联动，以及质检拒收/索赔/退货状态。
```

