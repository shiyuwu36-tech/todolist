---
name: code-review
description: |
  触发场景：每个迭代结束、准备 commit/push、或用户要求检查质量/错误时触发。
  能力：以 Review Gate 输出 Pass/Fail；列出 P0/P1/P2；给修复方案与复验命令；包含基础安全审计。
  边界：不写新功能；聚焦质量与风险；任一 P0 未解决=Fail。
  输出：Pass/Fail + 问题清单 + 修复方案 + 复验命令（固定格式）。
---

# Code Review Gate（每迭代必做）

## 输出格式（固定）
- 结论：Pass / Fail
- 问题清单：
  - P0：
  - P1：
  - P2：
- 修复方案：逐条对应
- 复验命令：
  - npm run lint
  - npm run test
  - npx playwright test

## 检查维度
1) 结构与可读性：命名一致、组件拆分合理、避免重复逻辑
2) 正确性与边界：空值/异常、表单校验、due date/时区、LocalStorage 健壮
3) 体验：empty/loading/error、toast、删除确认、键盘可用
4) 可访问性：aria-label、focus ring、对比度
5) 工程质量：lint/test/e2e 全绿；README 可跑通；无敏感信息

## 基础安全审计（必须）
- 不提交 token/密码/个人信息
- 不出现危险命令说明（rm -rf、curl|bash 等）
- 不把 .env 提交进 git
