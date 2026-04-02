---
name: code-review
description: |
  代码评审与质量闸门：在每次迭代结束对前后端改动做结构/正确性/体验/可访问性/工程质量检查，并以 Review Gate 形式输出 Pass/Fail 与分级问题清单。
  提供：P0/P1/P2 分级标准、固定 Review Gate 输出模板、复验命令集合（lint/test/e2e）、以及基础安全审计清单（上传限制/路径穿越/CORS/敏感信息/危险命令）。
  适用：每次迭代收尾准备 commit/push、用户要求检查 bug/质量风险、合并前审查、测试失败需要定位问题并给出可复验修复方案。
  不适用：新增业务功能或改变需求范围、系统级环境改造、性能调优/压测/架构大重构（除非用户明确要求）。
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
2) 正确性与边界：空值/异常、表单校验、日期/时区、API错误处理
3) 体验：empty/loading/error、toast、删除确认、键盘可用、上传反馈明确
4) 可访问性：aria-label、focus ring、对比度
5) 工程质量：lint/test/e2e 全绿；README 可跑通

## 基础安全审计（必须）
- 不提交 token/密码/个人信息
- 后端：上传限制（大小/类型）、路径穿越防护、CORS 合理、错误信息不过度泄露
- 文件：uploads/ 不入库；数据库文件/本地数据文件按需要加入 .gitignore 或明确策略
- 不出现危险命令说明（rm -rf、curl|bash 等）
- 不把 .env 提交进 git