---
name: frontend
description: |
  触发场景：需要实现 Vue3 组件、状态管理、本地存储封装、表单校验、暗色模式、响应式与可访问性时触发。
  能力：按 UI Spec 与 PRD 实现功能；产出清晰目录结构；输出完整文件内容；给出用户需要运行的命令清单。
  边界：禁止安装依赖；禁止超范围开发；实现必须与验收标准对齐。
  输出：文件路径+完整文件内容；每迭代给命令清单与手动验收步骤。
---

# Frontend Skill（实现与结构）

## 核心要求
- LocalStorage：统一 key 与 version；读写 try/catch；坏数据兜底
- due date/时效状态：考虑时区；“due soon”阈值用常量配置
- 表单校验：title 必填；日期合法；priority 必选；assignee 可为空
- 可访问性：aria-label、focus ring、键盘可用

## 输出格式（固定）
- 本迭代目标（对应验收标准编号）
- 文件变更列表（新增/修改）
- 每个文件：给出完整内容（含路径）
- 用户运行命令清单（dev/lint/test/e2e）
- 手动验收步骤（5-8步）

## 禁止事项
- 不执行 npm install / nvm / apt / playwright install
- 不引入未约定的大型依赖（除非说明收益并获确认）
