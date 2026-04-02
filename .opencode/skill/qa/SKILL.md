---
name: qa
description: |
  触发场景：需要把验收标准转成测试用例、实现 Vitest/Playwright、制定回归清单并作为质量闸门时触发。
  能力：为每个验收条款给出对应测试；前端单测+E2E覆盖；必要时对后端API做最小冒烟（可用 Playwright request 或简易脚本）。
  边界：不写业务功能；不执行系统级安装命令；不引入复杂测试框架。
  输出：测试用例矩阵（A#→测试）+ Vitest/Playwright 代码 + 回归清单 + 复验命令。
---

# QA Skill（Vitest + Playwright）

## 测试策略
- Vitest：前端工具函数、apiClient 错误处理、表单校验逻辑（可测的部分）
- Playwright：关键用户路径（任务CRUD + 上传附件 + 重启后仍存在的验证步骤）
- 后端最小冒烟：可选（若不引入新框架，可在 e2e 里调用页面流程间接覆盖）

## 最小覆盖（Iteration 1）
- 1~2 个 Vitest：校验/错误处理
- 1 个 Playwright：创建任务并出现在列表（烟测）

## 后续覆盖建议（Iteration 2/3）
- Playwright：上传附件成功→列表出现→刷新仍在
- 持久化验证：重启后端后刷新页面→任务/附件仍存在（此条可作为手动验收 + 半自动化说明）

## 输出
1) 用例矩阵：A1…A? → 对应（Vitest/Playwright/手测）
2) 测试文件路径 + 完整代码
3) 回归清单：每次迭代结束必测路径
4) 复验命令：npm run lint / npm run test / npx playwright test





