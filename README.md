# Todolist (Vue + Spring Boot)

## 项目简介

- 轻量级看板式待办应用，支持拖拽分栏、月历视图、附件上传、本地 SQLite 持久化、搜索筛选排序、优先级与截止时间管理。

## 技术栈

- 前端：Vue 3、Vite、Naive UI、Tailwind CSS
- 后端：Spring Boot、Spring JDBC、SQLite（本地 `data/` 文件）、附件本地存储 `uploads/`

## 目录结构

```
.
├─ src/                   # 前端源码（Vue 3 + Tailwind + Naive UI）
├─ public/
├─ docs/                  # 交付/验收文档
├─ backend/               # 后端 Spring Boot
│  ├─ src/main/java/com/example/todolist/
│  │  ├─ controller/      # Task/Attachment/Health 控制器
│  │  ├─ service/         # 业务与存储（文件/任务/附件）
│  │  ├─ repository/      # JDBC Dao
│  │  ├─ dto/             # 请求/响应模型
│  │  ├─ config/          # CORS、Schema、数据目录初始化
│  │  ├─ exception/       # 全局异常与错误模型
│  │  └─ util/            # 日期、类型、路径工具
│  ├─ src/main/resources/ # application.yml、schema.sql
│  ├─ uploads/            # 附件物理文件（运行时生成）
│  └─ data/               # SQLite 数据库文件 app.db（运行时生成）
└─ package.json / pom.xml
```

## 环境要求

- Node.js ≥ 18（含 npm），无需全局安装额外工具
- JDK 17（用于启动后端），无需系统级依赖

## 启动方式（开发）

- 前端：`npm install` → `npm run dev`（http://localhost:5173）
- 后端：`cd backend && ./mvnw spring-boot:run`（http://localhost:8080）
- 健康检查：`GET http://localhost:8080/health` 返回 `{ status: "ok" }`

## 构建方式（生产）

- 前端：`npm run build` 产出 `dist/`；`npm run preview` 本地预览
- 后端：`cd backend && ./mvnw test`（含持久化与附件流程的测试）或 `./mvnw -DskipTests package`

## 配置说明

- 前端环境变量：`VITE_API_BASE`（默认 `http://localhost:8080`）
- 后端配置（`backend/src/main/resources/application.yml`）：
  - 端口：8080
  - CORS：允许 `http://localhost:5173`，不允许通配凭证
  - 上传限制：单文件/请求 10MB
  - 允许类型：`image/*`、`application/pdf`、`text/plain`
  - 目录：数据库 `data/app.db`，附件 `uploads/`

## API 概览

- `GET /tasks?from&to` 列表（支持按截止时间范围过滤）
- `GET /tasks/{id}` 获取任务
- `POST /tasks` 创建任务
- `PUT /tasks/{id}` 更新任务
- `DELETE /tasks/{id}` 删除任务（级联删除附件元数据与文件）
- `GET /tasks/{id}/attachments` 列表附件
- `POST /tasks/{id}/attachments` 上传附件（multipart, 10MB, MIME 白名单）
- `GET /attachments/{id}` 下载附件
- `GET /health` 健康检查

## 数据持久化说明

- SQLite 文件位于 `backend/data/app.db`，启动时自动创建，重启后任务/附件元数据保留。
- 附件实际文件保存在 `backend/uploads/`，记录路径存于数据库。
- 删除任务会先删除附件记录与文件，再删除任务，避免孤立文件。

## 功能使用指南

- 看板三列：待办/进行中/已完成，可拖拽跨列移动，状态即时同步。
- 搜索与筛选：标题关键字、防抖搜索；截止时间筛选（全部/指定日期范围）；优先级筛选；一键重置。
- 排序：优先级默认高→低（可重置）；支持按过滤条件查看。
- 任务表单：标题必填，描述可选，优先级高/中/低，截止时间日期时间选择。
- 附件：在任务详情中上传/下载附件，校验类型与大小并提示错误。
- 月历：仅展示有截止时间的任务，点击日期查看当日任务列表并可编辑、切换状态、删除和处理附件。

## 常见问题（Troubleshooting）

- 前端请求 NetworkError：确认后端已启动、端口与 `VITE_API_BASE` 一致、CORS 允许源。
- 上传失败：检查文件大小 ≤10MB、类型需为图片/PDF/文本，或后端日志中的 MIME 提示。
- SQLite 权限/路径：确保 `backend/data/` 可写；如自定义路径请同步更新 `application.yml`。

## 交付与验收

- QA 用例矩阵（简版）：A1 空列表/错误态；A2 新建含截止时间；A3 编辑与状态切换；A4 删除；A5 上传限制（10MB+MIME）；A6 下载；A7 重启后数据与附件仍在；A19 拖拽移动；A20 拖拽失败回滚；A21-A26 月历渲染/切换/弹窗/截止规则/响应式/回归。
- Review Gate：Pass（P0/P1/P2 问题：无）。

