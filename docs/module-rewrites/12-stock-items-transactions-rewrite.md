# Stock Items And Transactions Rewrite

## 1. Module Goal

实现库存实例和库存交易。Stock Item 是实际库存数量/位置/成本对象，Stock Transaction 是所有库存变化的不可变流水。

## 2. Required Inputs

必须先读取：

- 前端 `src/views/StockItemsView.vue`
- 前端 `src/services/stockItemService.js`
- 前端 `src/mock/index.js` 中 `stockItems`、`transactions`
- `docs/data-model.md` 中 stock_item 和 stock_transaction

## 3. Manual Scope

Chapter 3：

- Registering a Stock Type as a Stock Item，printed page 182
- Stock Items，printed page 183
- Creating Stock Items，printed page 183
- Stock Item Locations，printed page 185
- Moving Stock Items，printed page 186
- Perishable Stock，printed page 187
- Setting Status on Stock Items，printed page 194
- In/Out of Stock，printed page 204
- Stock Transactions，printed page 207
- Altering/Reversing Stock Transactions，printed page 207

## 4. Backend Tasks

- 新增 stock_item。
- 新增 stock_transaction。
- 实现 register stock type as stock item。
- 实现 move stock item。
- 实现 in/out stock。
- 实现 reverse transaction。
- 实现 set status。
- 实现按 location/function/component 查询。

## 5. API

```text
GET  /api/stock/stock-items
POST /api/stock/stock-items
GET  /api/stock/stock-items/{id}
PUT  /api/stock/stock-items/{id}
POST /api/stock/stock-items/{id}/move
POST /api/stock/stock-items/{id}/in
POST /api/stock/stock-items/{id}/out
POST /api/stock/stock-items/{id}/set-status
GET  /api/stock/transactions
POST /api/stock/transactions/{id}/reverse
```

## 6. Acceptance Criteria

- 库存移动生成成对或明确的交易流水。
- 库存交易不可直接删除。
- 冲销创建 reversal，不覆盖原交易。
- Stock Items 页面后端化。
- Report Work 后续可消耗 stock item。

## 7. LLM Prompt

```text
请按照 docs/module-rewrites/12-stock-items-transactions-rewrite.md 开发 Stock Items 和 Transactions。
重点是库存变化必须通过 transaction command 完成，不能只修改当前数量。
```

