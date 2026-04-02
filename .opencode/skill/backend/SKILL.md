---
name: backend
description: |
  后端接口与持久化实现：设计并实现任务 CRUD 与附件上传/下载 REST API，采用轻量持久化（SQLite 或本地文件）保证服务重启后数据不丢，并提供本地可运行交付说明。
  提供：接口清单与统一错误格式（code/message/details）、数据 schema（Task/Attachment）、上传安全基线（大小/类型白名单/路径穿越防护）、CORS 限制建议、以及“后端代码全量文件 + 运行命令清单 + 手动验收步骤”输出规范。
  适用：需要实现或补齐后端 API（任务 CRUD、附件上传/列表/下载）、实现持久化（重启后任务与附件元数据仍存在）、本地 uploads 存储与元数据关联、联调前端并保证跨机器可运行。
  不适用：系统级安装与环境改造（apt/nvm/brew/curl|bash）、复杂微服务/云组件引入、默认加入登录鉴权/复杂权限（除非用户明确要求）、生产级部署与反向代理配置（除非用户明确要求）。
---

# Backend Skill（API / 持久化 / 附件上传）

## MVP 必做
- Task CRUD：create/list/get/update/delete
- 附件上传：POST /tasks/:id/attachments（multipart）
- 附件列表：GET /tasks/:id/attachments
- 附件下载：GET /attachments/:attachmentId/download（或直接返回静态文件）
- 持久化：服务重启后 task/attachment 元数据仍保留（SQLite 或 JSON 文件 + 校验）
- 文件存储：本地 uploads/ 目录（不进 git），记录文件名/类型/大小/路径/关联 taskId

## 默认上传策略（用于可测与可审计）
- 最大大小：10MB（超出返回 code=UPLOAD_TOO_LARGE）
- 允许类型（白名单，最小集）：image/*、application/pdf、text/plain
  （不在白名单返回 code=UNSUPPORTED_FILE_TYPE）
- 文件名与路径安全：禁止路径穿越；落盘文件名使用生成的安全名；原始文件名仅作为展示字段

## 工程要求
- 清晰目录（server/ 或 backend/），与前端分离
- 统一错误格式：{ code, message, details? }
- CORS：仅允许前端开发地址（默认 http://localhost:5173）
- 安全基线：限制上传大小、允许类型白名单、防止路径穿越、错误信息不过度泄露

## 输出格式（固定）
1) 本迭代支持的接口清单（对应验收标准）
2) 数据库/文件 schema
3) 文件清单（路径→完整内容）
4) 运行命令清单（dev/test，如有）
5) 手动验收步骤（含上传验证）