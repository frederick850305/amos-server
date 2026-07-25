# Quotations And Comparison Rewrite

## 1. Module Goal

实现采购询价后的供应商报价管理，以及报价比较矩阵、最佳价格/交期选择、方案提出、批准、选择。

## 2. Required Inputs

必须先读取：

- 前端 `src/views/QuotationComparisonView.vue`
- 前端 `src/services/purchaseFormService.js`
- 前端 `src/mock/index.js` 中 `quotations`
- `docs/data-model.md` 中 quotation 和 quotation_comparison

## 3. Manual Scope

Chapter 4：

- Quotations，printed page 236
- Adding Quotations to a Query Form，printed page 236
- Printing and Sending a Quotation，printed page 240
- Quotation Notes and Attachments，printed page 242
- Updating Quotations，printed page 243
- Updating Prices and Discounts，printed page 244
- Quotation Additional Costs，printed page 248
- Comparing Quotations，printed page 251
- Quotation Comparison Scenarios，printed page 251
- Configuring the Matrix，printed page 252
- Best Price or Delivery Time，printed page 266
- Propose, Approve and Select Comparison Scenarios，printed page 268
- Selecting a Quotation，printed page 272

## 4. Backend Tasks

- 新增 quotation。
- 新增 quotation line。
- 新增 quotation additional cost。
- 新增 quotation attachment metadata。
- 新增 quotation comparison。
- 新增 comparison line decision。
- 实现 best price / best delivery 计算。
- 实现 propose/approve/select 状态流。
- 选择报价后更新 purchase form 或生成 PO。

## 5. API

```text
GET  /api/purchase/quotations
POST /api/purchase/quotations
GET  /api/purchase/quotations/{id}
PUT  /api/purchase/quotations/{id}

POST /api/purchase/quotation-comparisons
GET  /api/purchase/quotation-comparisons/{id}
POST /api/purchase/quotation-comparisons/{id}/calculate
POST /api/purchase/quotation-comparisons/{id}/propose
POST /api/purchase/quotation-comparisons/{id}/approve
POST /api/purchase/quotation-comparisons/{id}/select
```

## 6. Acceptance Criteria

- Quotations 可按 Query Form 管理。
- 比价矩阵可由真实报价行生成。
- 系统可计算最佳价格和最佳交期。
- 方案状态流可审计。
- 选择报价后采购表单状态/行价格正确更新。

## 7. LLM Prompt

```text
请按照 docs/module-rewrites/15-quotations-comparison-rewrite.md 开发 Quotations 和 Quotation Comparison。
重点是报价行、矩阵计算、方案状态流和选择报价后的采购表单联动。
```

