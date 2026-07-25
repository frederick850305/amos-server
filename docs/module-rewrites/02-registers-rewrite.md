# Registers Rewrite

## 1. Module Goal

建立 AMOS 各业务模块共用的基础注册表。Registers 是 Maintenance、Stock、Purchasing、Financials 的公共依赖。

## 2. Required Inputs

必须先读取：

- `docs/database-rewrite-guide.md`
- `docs/data-model.md`
- `docs/api-contract.md`
- `docs/manual-mapping.md`
- 前端 `src/mock/index.js` 中 `lookups`
- 前端 `src/windows/registry.js` 中所有 lookupKey
- 后端 `register/MakerRegister*`
- 后端 `stock/StockGrade*`

## 3. Manual Scope

重点来自：

- Chapter 2 Function Criticality，printed page 44
- Chapter 3 Stock Type Makers，printed page 173
- Chapter 3 Stock Type Vendors，printed page 174
- Chapter 3 Stock Grades，printed page 176
- Chapter 4 Purchasing vendors/contracts 相关章节
- Chapter 5 Budget Codes，printed page 348

## 4. Registers To Implement

P0/P1 registers：

- Makers
- Vendors
- Locations
- Units
- Currencies
- Function Criticalities
- Job Classes
- Trades
- Disciplines
- Budget Codes

## 5. Backend Tasks

- 保留并完善现有 `maker_register`
- 新增 `vendor_register`
- 新增 `location_register`
- 新增 `unit_register`
- 新增 `currency_register`
- 新增 `function_criticality`
- 新增 `job_class`
- 新增 `trade`
- 新增 `discipline`
- 新增 `budget_code`
- 为每个 register 提供 CRUD + search
- 增加重复编码校验
- 增加 active/inactive 状态

## 6. API

```text
GET    /api/register/makers
POST   /api/register/makers
PUT    /api/register/makers/{id}

GET    /api/register/vendors
POST   /api/register/vendors
PUT    /api/register/vendors/{id}

GET    /api/register/locations
GET    /api/register/function-criticalities
GET    /api/register/units
GET    /api/register/currencies
GET    /api/register/job-classes
GET    /api/register/trades
GET    /api/register/disciplines
GET    /api/register/budget-codes
```

## 7. Acceptance Criteria

- 前端 lookup 数据可从后端获取。
- 关键 register 支持按 code/name 搜索。
- 重复 code 返回明确错误。
- 后续模块不再硬编码供应商、地点、币种等列表。

## 8. LLM Prompt

```text
请按照 docs/module-rewrites/02-registers-rewrite.md 开发 Registers。
先扫描前端所有 lookupKey 和 mock lookup 数据，再设计通用 register API 与各 register 表。
保持 H2/PostgreSQL Flyway 双兼容，不执行任何批量删除操作。
```

