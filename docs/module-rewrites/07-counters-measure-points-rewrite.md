# Counters And Measure Points Rewrite

## 1. Module Goal

实现组件和功能位置的计数器、测点、读数更新、日志、平均值计算，以及基于计数器/测点的作业触发能力。

## 2. Required Inputs

必须先读取：

- 前端 `src/services/counterService.js`
- 前端 `src/views/CountersOverviewView.vue`
- 前端 `src/windows/registry.js` 中 `update-counters`、`update-measure-points`
- 前端 `src/mock/index.js` 中 counters、measure logs
- 后端 `ComponentCounter.java`
- `docs/data-model.md` 中 counters and measure points

## 3. Manual Scope

Chapter 2：

- Component Counters，printed page 34
- Component Measure Points，printed page 35
- Function Counters，printed page 46
- Scheduling Jobs: Counters，printed page 71
- Scheduling Jobs: Measure Points，printed page 74
- Updating Counters and Measure Points，printed page 150
- Measure Point Trends，printed page 152
- Counter/Measure Point Overview, Logs，printed page 154

## 4. Backend Tasks

- 完善 component counter。
- 新增 function counter。
- 新增 component/function measure point。
- 新增 counter reading log。
- 新增 measure point reading log。
- 实现读数更新 command。
- 实现组件计数器变化同步 function counter 的规则。
- 实现 overview 查询。
- 为后续 job scheduling 暴露触发数据。

## 5. API

```text
GET  /api/maintenance/counters/overview
POST /api/maintenance/components/{componentId}/counters/{counterId}/readings
POST /api/maintenance/functions/{functionId}/counters/{counterId}/readings
POST /api/maintenance/components/{componentId}/measure-points/{pointId}/readings
GET  /api/maintenance/counter-logs
GET  /api/maintenance/measure-logs
```

## 6. Acceptance Criteria

- 读数更新写入不可变日志。
- 当前值正确更新。
- 平均值/趋势可查询。
- 已安装组件的计数器可按规则同步到 function。
- Counter Overview 页面后端化。

## 7. LLM Prompt

```text
请按照 docs/module-rewrites/07-counters-measure-points-rewrite.md 开发 Counters 和 Measure Points。
重点实现读数更新、日志、当前值、overview 和组件/function 同步规则。
```

