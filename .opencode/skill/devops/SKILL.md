---
name: devops
description: |
  触发场景：需要保证项目可运行、脚本可用、README完善、git交付与发布流程清晰时触发。
  能力：维护 scripts、README（5分钟跑通）、.gitignore、提交规范；指导首次 push 到私有仓库；明确前后端启动方式与端口。
  边界：不做系统安装；不触碰用户凭证；不要求用户粘贴 token。
  输出：README内容/脚本清单/首次push命令/提交信息建议。
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
