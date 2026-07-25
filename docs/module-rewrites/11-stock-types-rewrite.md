# Stock Types Rewrite

## 1. Module Goal

完善备件类型主数据，包括制造商、供应商、等级、价格、替代/替换/废弃件关系，以及与 Component Types 的备件关联。

## 2. Required Inputs

必须先读取：

- 前端 `src/windows/registry.js` 中 `stock-types`
- 前端 `src/services/stockTypeService.js`
- 前端 `src/mock/index.js` 中 `stockTypes`、`stockTypeMakers`、`stockTypeVendors`、`stockGrades`
- 后端 `stock/StockType*`
- 后端 `stock/StockTypeMaker*`
- 后端 `stock/StockTypeVendor*`
- 后端 `stock/StockGrade*`

## 3. Manual Scope

Chapter 3：

- Stock Types，printed page 172
- Defining a Stock Type，printed page 172
- Stock Type Makers，printed page 173
- Stock Type Vendors，printed page 174
- Stock Grades，printed page 176
- Stock Types in Component Types，printed page 180
- Alternative Parts，printed page 188
- Replacement and Obsolete Parts，printed page 189
- Setting Status on Stock Types，printed page 192
- Stock Purchase History，printed page 197

## 4. Backend Tasks

- 完善现有 stock type aggregate。
- 将 makers/vendors/grades 作为稳定 child collection。
- 增加 alternative/replacement/obsolete 关系表。
- 增加 status command。
- 增加 purchase history 查询接口。
- 与 component_type_stock_type 对齐。

## 5. API

```text
GET    /api/stock/stock-types
POST   /api/stock/stock-types
GET    /api/stock/stock-types/{id}
PUT    /api/stock/stock-types/{id}
POST   /api/stock/stock-types/{id}/set-status
GET    /api/stock/stock-types/{id}/purchase-history
GET    /api/stock/stock-types/{id}/alternatives
```

## 6. Acceptance Criteria

- Stock Types 页面后端化。
- makers/vendors/grades 可保存。
- best price 可计算或维护。
- 状态变更可审计。
- 可被 Component Types 的 Parts 标签引用。

## 7. LLM Prompt

```text
请按照 docs/module-rewrites/11-stock-types-rewrite.md 开发 Stock Types。
基于现有 starter CRUD 升级为聚合 API，重点处理 makers/vendors/grades 和备件关系。
```

