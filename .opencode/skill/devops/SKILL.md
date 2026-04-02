---
name: devops
description: |
  本地可运行交付与仓库规范：保证项目“5分钟跑通”，补齐 README/脚本/.gitignore，并指导安全的 git 提交流程（不触碰凭证）。
  提供：README（启动/测试/端口/环境变量）模板、脚本清单与建议（dev/build/test/e2e/后端启动）、.gitignore 基线、提交信息建议、以及首次 push/remote 配置命令清单。
  适用：需要完善可运行交付说明、补齐或修复启动脚本、整理 .gitignore、防止提交敏感文件、首次将项目推送到私有仓库、明确前后端启动方式与端口。
  不适用：系统级安装与环境改造、要求用户提供 token/密钥、默认新增 CI/CD 或 Docker 或反向代理/网关配置（除非用户明确要求发布部署）。
---

# DevOps Skill（交付与可运行性）

## 必做交付物
- README.md：新同学 5 分钟跑起来并通过基础测试（含前端+后端）
- scripts：前端 dev/build/preview/lint/test/e2e；后端 dev/start（如适用）
- .gitignore：node_modules、dist、uploads、sqlite/db 文件（按策略）、.env 等
- 变更记录：每迭代追加

## 本地运行要求（公司办公本）
- 依赖尽量少；启动命令清晰；默认端口不冲突
- 前端：http://localhost:5173
- 后端：http://localhost:3000（示例，可调整）
- README 必须写清：如何启动前后端、如何配置 API baseURL（最好用 .env.example）

## Git 推送（用户 repo）
repo: https://github.com/shiyuwu36-tech/todolist.git

若 origin 不存在：
- git branch -M main
- git remote add origin <url>

若 origin 已存在：
- git remote set-url origin <url>

然后：
- git add .
- git commit -m "iteration1: ..."
- git push -u origin main

## 认证提醒（GitHub）
- HTTPS 已不支持密码：用 SSH key 或 PAT
- 不要求用户在聊天中提供 token；只提示在本机完成认证