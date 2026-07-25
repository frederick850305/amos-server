# Stock Wanted, Transfer, And Control Rewrite

## 1. Module Goal

实现库存短缺计算、从短缺生成采购申请、库存盘点、地点库存核查、跨地点/跨船转移单。

## 2. Required Inputs

必须先读取：

- 前端 `src/views/StockWantedView.vue`
- 前端 `src/services/stockWantedService.js`
- 前端 `src/windows/registry.js` 中 `transfer-documents`、`stock-control`
- 前端 `src/mock/index.js` 中 `stockWanted`、`transferDocs`
- `docs/data-model.md` 中 stock_wanted 和 transfer_document

## 3. Manual Scope

Chapter 3：

- Stock Wanted，printed page 201
- Calculate Wanted Quantities，printed page 202
- Purchasing from the Stock Wanted Window，printed page 203
- Taking Inventory: Stock Control，printed page 205
- Checking Stock by Location，printed page 206
- Transfer Documents，printed page 208
- Creating a Transfer Document，printed page 208
- Submitting/Approving/Transferring/Receiving，printed pages 211-217
- Forecasting Stock Requirements，printed page 218

## 4. Backend Tasks

- 实现 stock wanted calculation。
- 实现 generate purchase requisition。
- 实现 stock control session。
- 实现 location inventory。
- 实现 transfer document 状态流。
- transfer receive 与 stock transaction 联动。

## 5. API

```text
POST /api/stock/wanted/calculate
GET  /api/stock/wanted
POST /api/stock/wanted/generate-requisition

GET  /api/stock/stock-control
POST /api/stock/stock-control
GET  /api/stock/location-inventory

GET  /api/stock/transfer-documents
POST /api/stock/transfer-documents
POST /api/stock/transfer-documents/{id}/submit
POST /api/stock/transfer-documents/{id}/approve
POST /api/stock/transfer-documents/{id}/transfer
POST /api/stock/transfer-documents/{id}/receive
```

## 6. Acceptance Criteria

- Stock Wanted 计算结果可持久化或可复现。
- 可从 wanted 生成 Requisition。
- Transfer Document 状态流不可跳步。
- 接收转移单会更新库存并写交易。
- 盘点结果可追溯。

## 7. LLM Prompt

```text
请按照 docs/module-rewrites/13-stock-wanted-transfer-control-rewrite.md 开发 Stock Wanted、Transfer Documents 和 Stock Control。
注意 Stock Wanted 与 Purchase Forms、Transfer Receive 与 Stock Transactions 的跨模块联动。
```

