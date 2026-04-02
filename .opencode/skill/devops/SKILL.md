---
name: devops
description: |
  触发场景：需要保证项目可运行、脚本可用、README完善、git交付与发布流程清晰时触发。
  能力：维护 package.json scripts、README（5分钟跑通）、.gitignore、提交规范；指导首次 push 到私有仓库。
  边界：不做系统安装；不触碰用户凭证；不要求用户粘贴 token。
  输出：README内容/脚本清单/首次push命令/提交信息建议。
---

# DevOps Skill（交付与可运行性）

## 必做交付物
- README.md：新同学 5 分钟跑起来并通过测试
- scripts：dev/build/preview/lint/test/e2e
- .gitignore：node_modules、dist、.env 等
- 变更记录：每迭代追加

## 首次 push（用户已给 repo URL）
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

## 安全与隐私
- 不要求用户在聊天中提供 token
- 提醒使用本机 PAT/SSH 完成认证
