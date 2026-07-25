# Component Installation Rewrite

## 1. Module Goal

实现组件安装到功能位置、从功能位置拆卸、状态自动推导、轮换日志、状态日志，以及相关作业停用规则。

这是 AMOS Maintenance 中最关键的业务动作之一，不应只通过普通 update 实现。

## 2. Required Inputs

必须先读取：

- 前端 `src/services/componentService.js`
- 前端 `src/services/functionService.js`
- 前端 `src/views/ComponentsView.vue`
- 前端 `src/windows/registry.js` 中 Functions 的 Options
- 前端 `src/mock/index.js` 中 `componentFunctionHistory`、`componentStatusLog`
- 后端 Components 和 Functions 已实现代码

## 3. Manual Scope

Chapter 2：

- Installing a Component on a Function，printed page 40
- Removing a Component from a Function，printed page 42
- Component status/log/archive 相关行为
- Function Driven Jobs，printed page 89

## 4. Backend Business Rules

安装：

- 一个 function 同一时间只能安装一个 component。
- 如果 function 已有组件，必须明确处理旧组件拆卸。
- 安装后 component.status = `In Use`。
- 安装后 component.location 同步为 function.location。
- function.installedComponent 指向该 component。
- 写入 rotation log。
- 写入 component status log。

拆卸：

- function.installedComponent 清空。
- component.function 清空。
- component.status 通常变为 `Available`，除非请求指定新状态。
- component.location 可清空或设为用户指定位置。
- 写入 rotation log 的 removed 字段。
- 写入 component status log。
- 如有 round/job 依赖该 function+component，应按手册规则停用相关轮次作业。

## 5. API

```text
POST /api/maintenance/functions/{functionId}/install-component
POST /api/maintenance/functions/{functionId}/remove-component
POST /api/maintenance/components/{componentId}/install
POST /api/maintenance/components/{componentId}/remove
GET  /api/maintenance/functions/{functionId}/rotation-log
GET  /api/maintenance/components/{componentId}/function-history
```

建议以 function command 为主，component command 可作为别名或前端适配。

## 6. Database Tables

- `component`
- `maintenance_function`
- `component_function_rotation`
- `component_status_log`
- `component_archive`

## 7. Acceptance Criteria

- 安装动作同时更新 function 和 component。
- 拆卸动作同时更新 function 和 component。
- 日志不可变。
- 重复安装、安装不存在组件、安装到不存在 function 返回明确错误。
- 前端 Components / Functions 两边状态一致。

## 8. LLM Prompt

```text
请按照 docs/module-rewrites/06-component-installation-rewrite.md 开发组件安装/拆卸业务。
这不是 CRUD，请使用事务性 Service 和明确 command endpoint，确保 function、component、rotation log、status log 一致。
```

