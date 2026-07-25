# AMOS Module Rewrite Index

本目录收纳 AMOS 系统逐模块转写任务书。每份文件都面向后续 LLM 开发使用，作用是把“应该读什么、实现什么、怎么验收”固定下来。

使用方式：

1. 先让 LLM 阅读本索引。
2. 再指定一个模块文件。
3. 要求 LLM 按模块文件中的提示词执行。
4. 每次只开发一个模块或一个明确子切片。
5. 不执行任何批量删除操作。

## 推荐开发顺序

| 顺序 | 文件 | 模块 | 目标 |
| ---: | --- | --- | --- |
| 1 | `01-system-foundation-rewrite.md` | System Foundation | 安装地点、部门、用户、权限、租户范围 |
| 2 | `02-registers-rewrite.md` | Registers | 制造商、供应商、地点、单位、币种、关键性等基础字典 |
| 3 | `03-component-types-rewrite.md` | Component Types | 组件类型、计数器模板、测点模板、备件关联 |
| 4 | `04-components-rewrite.md` | Components | 实体组件、状态、档案、安装前准备 |
| 5 | `05-functions-rewrite.md` | Functions | 功能位置、层级、关键性、位置 |
| 6 | `06-component-installation-rewrite.md` | Component Installation | 组件安装/拆卸、轮换日志、状态日志 |
| 7 | `07-counters-measure-points-rewrite.md` | Counters And Measure Points | 计数器、测点、读数、日志、触发 |
| 8 | `08-jobs-scheduling-rewrite.md` | Jobs And Scheduling | 类型作业、组件作业、调度规则、依赖 |
| 9 | `09-work-orders-report-work-rewrite.md` | Work Orders And Report Work | 工单生成、计划、签发、完成、工作上报 |
| 10 | `10-maintenance-history-projects-rounds-rewrite.md` | Maintenance History, Projects, Rounds | 历史、日志、项目、轮次 |
| 11 | `11-stock-types-rewrite.md` | Stock Types | 备件类型、制造商、供应商、等级、替代件 |
| 12 | `12-stock-items-transactions-rewrite.md` | Stock Items And Transactions | 库存实例、移动、出入库、冲销 |
| 13 | `13-stock-wanted-transfer-control-rewrite.md` | Stock Wanted, Transfer, Control | 短缺计算、转移单、盘点 |
| 14 | `14-purchase-forms-rewrite.md` | Purchase Forms | 申请、询价、订单表头与行 |
| 15 | `15-quotations-comparison-rewrite.md` | Quotations And Comparison | 报价、比价矩阵、方案选择 |
| 16 | `16-deliveries-quality-transport-rewrite.md` | Deliveries, Quality, Transport | 到货、中转、质检、索赔 |
| 17 | `17-contracts-custom-clearance-rewrite.md` | Contracts And Custom Clearance | 合同、价格矩阵、清关 |
| 18 | `18-financials-rewrite.md` | Financials | 预算、预算占用、凭证、贷项 |
| 19 | `19-workflow-audit-search-dashboard-rewrite.md` | Workflow, Audit, Search, Dashboard | 工作流、审计、全局搜索、看板 |

## 通用执行规则

每个模块开发前必须读取：

- `docs/database-rewrite-guide.md`
- `docs/amos-rewrite-plan.md`
- `docs/api-contract.md`
- `docs/data-model.md`
- `docs/manual-mapping.md`
- 当前模块对应的 `docs/module-rewrites/*.md`
- 前端相关页面、service、mock 数据
- 后端现有实体、Controller、Repository、Flyway migration

每个模块完成时必须验证：

- Flyway migration 可在 H2 跑通。
- Flyway migration 可在 PostgreSQL 跑通。
- 后端测试通过。
- 前端 build 通过。
- 对应页面核心流程可用。
- 文档中的 API 契约同步更新。

