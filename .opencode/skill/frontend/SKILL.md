---
name: frontend
description: |
  前端实现与联调：基于 PRD 与 UI Spec 实现 Vue3 页面与组件，完成任务管理与附件上传的前后端对接，并补齐可访问性、错误处理与基础测试路径。
  提供：前端目录/组件拆分建议、统一 apiClient 模板（baseURL/超时/错误格式化）、表单校验规则、加载/错误/重试与 toast 反馈模式、每迭代“文件全量内容 + 命令清单 + 手动验收步骤”输出规范。
  适用：实现或重构 Vue3 组件与状态管理、对接后端 API（任务 CRUD/附件上传下载）、实现暗色模式/响应式/可访问性、补齐前端可测逻辑与联调验证步骤。
  不适用：系统级安装与环境改造（apt/nvm/curl|bash）、超出 PRD 范围的新增大功能（登录/云同步/复杂协作等）、后端架构与数据库设计（交给 backend/orchestrator）。
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