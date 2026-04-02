---
name: backend
description: |
  触发场景：需要实现后端接口、文件上传、持久化存储（服务重启后数据保留）、本地可运行交付时触发。
  能力：设计并实现 REST API（任务CRUD/附件上传下载）、选择轻量持久化方案（SQLite/本地文件存储），提供启动脚本与README说明，保证跨机器可运行。
  边界：默认不引入复杂微服务/云组件；不做登录鉴权（除非用户要求）；不执行系统级安装命令（apt/nvm）。
  输出：API 设计（路径/请求/响应/错误）+ 完整后端代码文件（路径+全量内容）+ 数据存储说明 + 用户运行命令清单。
---

# Backend Skill（API / 持久化 / 附件上传）

## MVP 必做
- Task CRUD：create/list/get/update/delete
- 附件上传：POST /tasks/:id/attachments（multipart）
- 附件列表：GET /tasks/:id/attachments
- 附件下载：GET /attachments/:attachmentId/download（或直接返回静态文件）
- 持久化：服务重启后 task/attachment 元数据仍保留（SQLite 或 JSON 文件 + 校验）
- 文件存储：本地 uploads/ 目录（不进 git），记录文件名/类型/大小/路径/关联 taskId

## 工程要求
- 清晰目录（server/ 或 backend/），与前端分离
- 统一错误格式：{ code, message, details? }
- CORS：仅允许前端开发地址（默认 http://localhost:5173）
- 安全基线：限制上传大小、允许类型白名单（至少做最小检查）、防止路径穿越

## 输出格式（固定）
1) 本迭代支持的接口清单（对应验收标准）
2) 数据库/文件 schema
3) 文件清单（路径→完整内容）
4) 运行命令清单（dev/test，如有）
5) 手动验收步骤（含上传验证）
