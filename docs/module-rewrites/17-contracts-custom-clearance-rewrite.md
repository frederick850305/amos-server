# Contracts And Custom Clearance Rewrite

## 1. Module Goal

实现采购合同、折扣规则、价格矩阵、合同审批发布、合同套用到采购表单，以及清关合同/清关表单。

## 2. Required Inputs

必须先读取：

- 前端 `src/windows/registry.js` 中 `contracts`、`custom-clearance-forms`
- 前端 `src/services/purchaseFormService.js`
- 前端 `src/mock/index.js` 中 `contracts`
- `docs/data-model.md` 中 contract
- `docs/manual-mapping.md` Chapter 4 Contracts 和 Appendix A

## 3. Manual Scope

Chapter 4：

- Purchase Contracts，printed page 305
- Levels and Types of Discounts，printed page 305
- How AMOS Calculates and Applies Discounts，printed page 307
- Creating a Purchase Contract，printed page 311
- Defining Delivery Zones，printed page 315
- Product Groups，printed page 317
- Price Matrix，printed page 320
- Approving and Issuing a Contract，printed page 330
- Applying a Contract to a Purchase Form，printed page 331
- Changing a Contracted Purchase Form，printed page 338
- Removing a Contract From a Purchase Form，printed page 340
- Filtering Forms by Contract，printed page 341
- Custom Clearance，printed page 341
- Appendix A Contracts XML，printed page 364

## 4. Backend Tasks

- 新增 contract。
- 新增 contract line。
- 新增 delivery zone。
- 新增 product group。
- 新增 price matrix。
- 新增 surcharge。
- 实现 approve/issue contract。
- 实现 apply/remove contract to purchase form。
- 实现 contracted form line recalculation。
- 清关表单可后置为 P2。
- XML import/export 可后置为 P3。

## 5. API

```text
GET  /api/purchase/contracts
POST /api/purchase/contracts
GET  /api/purchase/contracts/{id}
PUT  /api/purchase/contracts/{id}
POST /api/purchase/contracts/{id}/approve
POST /api/purchase/contracts/{id}/issue
POST /api/purchase/contracts/{id}/calculate-price

POST /api/purchase/forms/{id}/apply-contract
POST /api/purchase/forms/{id}/remove-contract

GET  /api/purchase/custom-clearance-forms
POST /api/purchase/custom-clearance-forms
```

## 6. Acceptance Criteria

- 合同价格可被采购表单套用。
- 折扣和附加费计算可测试。
- 合同状态未批准/未发布时不能套用。
- 移除合同后采购表单价格按规则恢复或重算。

## 7. LLM Prompt

```text
请按照 docs/module-rewrites/17-contracts-custom-clearance-rewrite.md 开发 Contracts。
先实现合同、折扣、价格矩阵和 apply-contract，清关和 XML import/export 可作为后续子切片。
```

