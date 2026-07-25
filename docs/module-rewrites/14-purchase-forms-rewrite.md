# Purchase Forms Rewrite

## 1. Module Goal

实现 AMOS 采购表单体系：Requisition、Query、Purchase Order 共用表头/行结构，并支持转换、拆分、审批、合同套用。

## 2. Required Inputs

必须先读取：

- 前端 `src/views/PurchaseFormsView.vue`
- 前端 `src/services/purchaseFormService.js`
- 前端 `src/mock/index.js` 中 `purchaseForms`
- `docs/data-model.md` 中 purchase_form
- `docs/api-contract.md` 中 Purchase API

## 3. Manual Scope

Chapter 4：

- Purchasing with AMOS，printed page 220
- Forms Have Two Parts: Headers and Lines，printed page 220
- Working with Purchasing Forms，printed page 222
- Filtering the Forms Window，printed page 222
- Form Origin Identification，printed page 223
- Order Form Contents，printed page 224
- Delivery Destinations，printed page 226
- Additional Costs，printed page 227
- Line Items，printed page 228
- Requisition Forms，printed page 233
- Query Forms，printed page 235
- Purchase Orders，printed page 274
- Applying a Contract to a Purchase Form，printed page 331

## 4. Backend Tasks

- 新增 purchase_form。
- 新增 purchase_form_line。
- 新增 additional cost。
- 新增 delivery destination。
- 实现 requisition/query/purchase order 类型。
- 实现 requisition -> query。
- 实现 requisition/query -> purchase order。
- 实现 approve order。
- 实现 apply contract。
- 实现 split lines。

## 5. API

```text
GET  /api/purchase/forms
POST /api/purchase/forms
GET  /api/purchase/forms/{id}
PUT  /api/purchase/forms/{id}
POST /api/purchase/forms/{id}/convert-to-query
POST /api/purchase/forms/{id}/convert-to-purchase-order
POST /api/purchase/forms/{id}/approve
POST /api/purchase/forms/{id}/apply-contract
POST /api/purchase/forms/{id}/split-lines
```

## 6. Acceptance Criteria

- Forms 页面后端化。
- 三类 form 共享结构但状态规则清晰。
- 转换动作保留来源追踪。
- 表头金额由行和附加费用计算。
- PO 审批后可影响预算承诺。

## 7. LLM Prompt

```text
请按照 docs/module-rewrites/14-purchase-forms-rewrite.md 开发 Purchase Forms。
重点是 Requisition、Query、Purchase Order 的统一建模和转换命令，不要只做 CRUD。
```

