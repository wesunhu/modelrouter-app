# Changelog

## [0.1.0-preview.1] - 2026-03-19

### 修复
- **ModelApiClient**: 本地模型调用路径由 `/chat/completions` 修正为 `/v1/chat/completions`，与 local 服务路由一致
- **Docker 网络**: application-docker.yml 中本地服务地址由 `modelrouter-local` 改为 `local`（Docker Compose 服务名）
- **init.sql**: Local Provider 种子数据 base_url 改为 `http://localhost:8081`，适用于本地开发
- **callLocal 错误处理**: 增加 HttpStatusCodeException 捕获，返回更清晰的错误信息
- **Frontend server**: 将 server.js 重命名为 server.mjs，确保 Node.js 正确解析 ESM 模块

### 变更
- 无破坏性变更
