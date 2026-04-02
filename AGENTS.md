# AGENTS 操作手册

（面向本仓库的智能编码代理，中文）
项目概览

- 技术栈：前端 Vue3 + Vite + TailwindCSS + Naive UI；测试 Vitest + Playwright（已安装依赖但暂无测试文件）；后端规划为 Java Spring Boot，SQLite 持久化并支持附件上传/下载。
- 当前代码：纯前端模板，尚无后端代码与测试用例，请在增量迭代时保持目录清晰。
- 包管理：NPM，已存在 package-lock.json，避免使用 yarn/pnpm。
- 禁止事项：不要修改 node_modules；不要执行系统级安装（apt/brew 等），保持本地可运行。
  命令速查（在仓库根目录）
- 安装依赖：`npm install`（仅使用本地 node，勿装全局工具）。
- 开发：`npm run dev`，默认端口 5173。
- 构建：`npm run build`，产出在 `dist/`。
- 预览构建：`npm run preview`。
- Lint：暂无脚本；如需检查可执行 `npx eslint src --ext .js,.vue`，必要时先添加 `.eslintrc`。
- 单元测试：尚未创建；若新增 Vitest 用例，运行 `npx vitest`。
- 运行单个 Vitest 测试：`npx vitest path/to/file.test.ts -t "test name"`。
- 覆盖率（需配置后使用）：`npx vitest --coverage`。
- E2E：引入了 `@playwright/test` 但未有配置，建议添加 `playwright.config.ts` 后使用 `npx playwright test`。
- 运行单个 Playwright 用例：`npx playwright test path/to/spec.ts --grep "name"`（创建配置后再用）。
- 后端若加入 Spring Boot：`./mvnw spring-boot:run` 或 `./gradlew bootRun`（需随后端引入），避免全局安装 Maven/Gradle。
- SQLite 数据准备：使用嵌入式文件，避免依赖系统服务；路径建议 `backend/data/app.db`。
  目录与文件约定
- 前端入口：`src/main.js`，页面根组件 `src/App.vue`。
- 公共样式：`src/style.css` 仅包含 Tailwind 指令，组件样式优先使用 `<style scoped>`。
- 资源：图片在 `src/assets/`，保持小写连字符命名。
- 组件：置于 `src/components/`，命名为帕斯卡式（`TaskList.vue`）。
- 未来后端：建议根下建 `backend/`（Spring Boot），分层 `controller/service/repository`，附件目录 `backend/uploads/`。
- 配置：`tailwind.config.js` 已指向 `./index.html` 与 `./src/**/*.{vue,js}`，新增路径记得同步。
- `.vscode/extensions.json` 推荐 Volar，勿删除。
- 无 `.cursor/rules` 与 Copilot 说明，若后续添加需在此文件补充。
- `.gitignore` 已覆盖 node_modules/dist/IDE，提交前确认不忽略必要产物。
  Git 提交流程
- 不要强制推送或重写用户历史；遵循最小变更。
- 每次修改前运行 `git status` 理解脏区；禁止重置用户修改。
- 提交信息建议：`feat: ...` `fix: ...` `chore: ...`，突出意图。
- 不要提交敏感文件（.env、证书、数据库文件），SQLite 数据文件仅供本地调试不应入库。
- 若新增格式化或 lint 配置，保持与现有风格兼容，必要时说明迁移步骤。
  依赖与包管理
- 仅使用 npm；新增依赖请更新 package-lock.json。
- 优先使用 ESM；保持 import 路径相对且无文件扩展。
- 第三方 UI 组件统一使用 Naive UI，避免混入其他 UI 库。
- CSS 工具以 Tailwind 为主，必要时使用 Naive UI 主题变量，勿引入冗余 CSS 框架。
- 禁止擅自升级 Vite 主要版本；如需升级先验证 dev/build/preview 流程。
- Playwright 需浏览器二进制，如缺失请提示用户手动执行 `npx playwright install chromium`（避免安装系统依赖）。
- 不要在代码中引用 node_modules 内的源码文件；通过正式包 API 使用。
  前端编码规范
- 使用 `<script setup>` 语法，组合式 API 优先，少用 Options API。
- 组件命名帕斯卡式；prop 采用驼峰；事件使用 kebab-case 对外，内部 emit 使用驼峰。
- 模板属性顺序：`v-if/v-for` -> `:key` -> `:prop`/`v-model` -> 事件 -> class/style。
- 避免在模板中声明内联复杂逻辑，提取为计算属性/方法。
- 状态管理：小型组件使用 `ref/computed`，跨组件共享可用 provide/inject 或引入轻量状态库时需评估。
- 异步使用 `async/await`，错误集中处理（下文错误处理约定）。
- 导入顺序：第三方 -> 别名路径（若后续配置） -> 相对路径 -> 样式；同组按字母排序。
- 未使用的变量/导入应清理，配合 ESLint（待添加配置）。
- 样式优先 Tailwind 原子类；需要复用时提取到组件局部样式，避免全局泄漏。
- Naive UI 主题：使用内置颜色，不手写硬编码十六进制，必要时集中在主题配置。
- 可访问性：表单元素配对 label；按钮提供 aria-label；列表项使用语义元素。
- 文本/提示统一中文，保持简短。
  组件与状态设计
- 页面布局保持响应式：容器使用 `max-w-*` + `mx-auto` + `p-*`。
- 列表渲染使用唯一 `:key`（ID 优先，其次稳定字段）。
- 表单使用受控数据对象，初始化完整字段避免 `undefined`。
- 表单校验：前端先做必填/格式校验，再发送后端；对接 Naive UI Form 可内置规则。
- 避免将网络模型直接透传到 UI，创建轻量 view model（含 UI 状态如 loading/error）。
- 对话框/抽屉组件解耦：通过布尔控制显隐，关闭时清理状态。
- 复杂组件拆分：容器组件负责数据获取，展示组件保持纯展示/事件回调。
- 路由尚未启用；如新增 Vue Router，请集中在 `src/router/` 并更新入口。
  UI/UX 与样式
- Tailwind 类名按布局->尺寸->排版->颜色顺序编排，减少跳跃。
- 使用 Naive UI 的基础色彩与尺寸，尽量保持浅色主题一致性，必要时支持暗色模式但默认浅色。
- 间距遵循 8px 系统（p-2/p-3/p-4）；圆角使用 `rounded-lg` 或组件默认。
- 交互反馈：提交/长耗时操作显示 loading 状态；成功/失败使用 Naive UI Message/Notification。
- 图标若需，请使用现有资源或引入统一库，避免内嵌 svg 过多。
- 空状态/错误状态提供简短描述和重试按钮。
- 动画保持轻量（过渡 150–250ms），避免过度动效。
- 移动端视口：使用 `w-full`、`gap-*` 避免硬编码宽度。
  数据与 API 约定（前端视角）
- 未来接口基址通过环境变量注入（如 `import.meta.env.VITE_API_BASE`），创建统一 apiClient（fetch/axios）模块。
- 请求包装：统一超时、重试策略（必要时）；错误格式化为 `{ message, code, details }`。
- GET/POST/PUT/PATCH/DELETE 对应 CRUD；附件上传使用 `multipart/form-data`。
- 上传限制：前端校验大小（如 10MB）、类型白名单，禁止路径穿越，文件名使用后端返回的安全名称。
- 下载：通过 `GET /attachments/{id}`，处理 `Content-Disposition`，提供浏览器下载。
- 列表分页：采用 `page`+`pageSize`，后端返回 `total`；前端保留分页状态。
- 乐观/悲观更新：小改动可乐观，失败回滚并提示；大批量操作使用悲观更新。
- 错误处理：网络层集中捕获并转换为用户可读消息，组件内只处理特定业务分支。
- 日志：开发阶段可 `console.debug`，但提交前移除噪声日志。
- 国际化：当前中文，无 i18n 框架，如需多语言请集中封装。
  后端约定（尚未落地，请按此准绳实现）
- 语言/框架：Java 17 + Spring Boot，模块目录建议 `backend/`.
- 构建：优先使用包装脚本 `mvnw` 或 `gradlew`；不要要求系统安装 Maven/Gradle。
- 配置：application.yml 指定 SQLite 数据源，文件路径相对仓库（例如 `./data/app.db`）。
- 数据模型：Task{id, title, description, status, dueDate, attachments[]}；Attachment{id, taskId, name, mimeType, size, storedPath, createdAt}。
- API 形态：REST JSON，错误响应 `{ code, message, details }`，HTTP 状态与业务码一致。
- 任务 CRUD：`GET /tasks`、`POST /tasks`、`GET /tasks/{id}`、`PUT /tasks/{id}`、`DELETE /tasks/{id}`。
- 附件：`POST /tasks/{id}/attachments` 上传（表单字段 `file`），`GET /attachments/{id}` 下载，`GET /tasks/{id}/attachments` 列表。
- 安全：限制上传大小（如 10MB），校验 MIME，阻止路径穿越，文件保存在 `backend/uploads/`，元数据入库。
- CORS：仅开放给前端 dev/build 域（本地 5173），禁止通配符 Credentials。
- 启动端口：建议 8080；若变更请在文档与前端 env 一致。
- 健康检查：`GET /health` 返回服务状态。
- 持久化：确保重启后任务与附件元数据存在，文件路径持久。
- 测试：使用 Spring Boot Test/MockMvc 覆盖 API 与附件流程。
  错误处理与日志
- 前端：所有 API 调用包裹 try/catch，捕获后调用统一 error formatter，UI 层弹出 message/toast。
- 后端：全局异常处理器返回标准错误体；记录警告日志但避免泄露敏感路径。
- 输入校验：前端必填校验，后端使用 Bean Validation（如 `@Valid`）。
- 文件操作：使用随机文件名或 UUID，保存绝对路径，响应返回下载 URL。
- 超时：前端 fetch/axios 设定超时（如 10s），后端设置连接/读取超时。
- 重试：只对幂等 GET/PUT 实施有限次重试；非幂等操作用错误提示代替重试。
  测试策略
- 单元测试（Vitest）：为纯函数或小型组件添加测试；使用 jsdom 环境。
- 组件测试：使用 `@vue/test-utils` + Vitest，覆盖渲染、交互、事件触发。
- Mock：使用 `vi.mock` 模拟 API 调用，保持快而稳定。
- 覆盖率：必要时开启 `coverage`，确保关键逻辑（任务新增/附件上传流程）覆盖。
- E2E（Playwright）：待添加 `playwright.config.ts`；覆盖核心用户流（任务 CRUD、附件上传/下载、过滤排序）。
- 浏览器安装：如缺少浏览器二进制，请提示用户执行 `npx playwright install chromium`；不要自动装系统依赖。
- 后端测试：Spring Boot Test 覆盖 API、数据库持久化与附件读写，SQLite 使用临时文件或内存库，避免污染生产数据。
- 测试数据：使用工厂方法或 Fixture，测试结束清理临时文件。
- CI：如用户添加 GitHub Actions，使用 npm install & npm run build & vitest/playwright，后端使用 mvnw/gradlew；保持缓存路径一致。
  性能与可维护性
- 避免不必要的全局状态；组件卸载时释放监听/定时器。
- 网络请求并发控制：批量操作使用 Promise.allSettled，并提供进度提示。
- 列表渲染避免巨大 DOM；必要时分页或虚拟滚动。
- 图片/附件下载：使用浏览器下载 API，避免 data URL 占用内存。
- 代码分割：如引入路由或大组件，使用按需加载。
- 依赖体积：优先使用 Naive UI 按需导入（后续可配置）；避免引入大型未使用库。
  安全与隐私
- 不在代码或日志中泄露文件系统路径、数据库位置。
- 上传前检查文件名，后端重新命名；前端不信任用户输入。
- 使用 HTTPS URL 访问外部资源；避免混合内容。
- 本地存储仅存放非敏感偏好（如 UI 状态），敏感 token 放 Cookie/HttpOnly（若后端加入认证）。
- 防范 XSS：输出前转义；避免使用 `v-html`，如必须使用需严格白名单。
- 防范 CSRF：后端提供 CSRF 机制或基于 token；前端遵循后端策略。
- 依赖安全：新增依赖前检查维护状态；避免使用弃用包。
  文档与沟通
- 新增模块请在 AGENTS.md 或 README.md 补充命令与约定。
- 变更配置（端口、路径、环境变量）务必写明来源与影响。
- 用户提示：保持简短、可操作，避免冗长背景介绍。
- 提交 PR/变更前，列出手动验证步骤（dev、build、测试）。
- 若需新环境变量，提供示例 `.env.example`。
- 与后端接口变更同步更新前端 API 类型/契约。
  工具与 IDE
- 推荐 VS Code + Volar；禁用 Vetur。
- 可使用 ESLint/Prettier 插件，但需与仓库配置一致，避免自动大改格式。
- 光标/自动补全：当前无 `.cursor/rules` 或 Copilot 说明，若后续添加请同步本文件。
- 终端命令默认在仓库根目录执行。
  其他注意事项
- 保持中文注释/文案，必要时附英文关键词以利搜索。
- 新建文件默认使用 UTF-8/ASCII，避免引入全角空格。
- 二进制文件（图片等）请放 `public/` 或 `src/assets/`，并在 git lfs 不可用时控制大小。
- 如果需要运行数据库迁移或初始化，请提供可重复的脚本或 README 指引。
- 本文件优先级高于模板 README；若二者冲突以此为准。
