---
name: qa
description: |
  触发场景：需要把验收标准转成测试用例、实现 Vitest/Playwright、制定回归清单并作为质量闸门时触发。
  能力：为每个验收条款给出对应测试；维持最小但有效覆盖；输出可运行测试代码与复验命令。
  边界：不写业务功能；不引入复杂测试框架；不执行安装命令。
  输出：测试用例矩阵（A#→测试）+ Vitest/Playwright 代码 + 回归清单 + 复验命令。
---

# QA Skill（Vitest + Playwright）

## 测试策略
- Vitest：工具函数与存储层（storage、status、过滤排序）
- Playwright：关键 happy path（打开→新增→完成→删除→验证列表与反馈）

## 最小覆盖（Iteration 1）
- 1~2 个 Vitest：storage 读写/健壮性、时效状态计算
- 1 个 Playwright：创建任务并出现在列表（烟测）

## 输出
1) 用例矩阵：A1…A? → 对应（Vitest/Playwright/手测）
2) 测试文件路径 + 完整代码
3) 回归清单：每次迭代结束必测路径
4) 复验命令：npm run test / npx playwright test / npm run lint
