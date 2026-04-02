# 本地快速启动（SETUP）

1. Clone：`git clone <repo> && cd todolist-vue`
2. 前端依赖：`npm install`
3. 后端启动：`cd backend && ./mvnw spring-boot:run`（端口 8080，健康检查 `/health`）
4. 前端启动：新终端回到仓库根目录 `npm run dev`（端口 5173，默认调用 `http://localhost:8080`）
5. 验证接口：在浏览器或 `curl http://localhost:8080/tasks` / `/health`，应返回 JSON
6. 构建与测试：
   - 前端打包：`npm run build`；预览：`npm run preview`
   - 后端测试：`cd backend && ./mvnw test` 或跳过测试打包：`./mvnw -DskipTests package`
