---
name: frontend
description: |
  触发场景：需要实现 Vue3 组件、状态管理、表单校验、暗色模式、响应式与可访问性，并对接后端 API（任务CRUD/附件上传）时触发。
  能力：按 UI Spec 与 PRD 实现前端；封装 apiClient；处理加载/错误/重试；输出完整文件内容；给出用户需要运行的命令清单。
  边界：禁止系统级安装；禁止超范围开发；实现必须与验收标准对齐。
  输出：文件路径+完整文件内容；每迭代给命令清单与手动验收步骤。
---

# Frontend Skill（实现与结构）

## 核心要求
- API 对接：统一 apiClient（baseURL、超时、错误格式化）；每个请求有 loading/error 处理
- 表单校验：title 必填；日期合法；priority 必选；assignee 可为空
- 附件上传：使用 multipart/form-data；显示上传中状态；失败可重试；成功 toast
- 可访问性：aria-label、focus ring、键盘可用

## 输出格式（固定）
- 本迭代目标（对应验收标准编号）
- 文件变更列表（新增/修改）
- 每个文件：给出完整内容（含路径）
- 用户运行命令清单（前端 dev/lint/test/e2e）
- 手动验收步骤（5-8步）

## 禁止事项
- 不执行 apt/nvm/curl|bash
- 不引入未约定的大型依赖（除非说明收益并获确认）