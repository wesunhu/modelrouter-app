# Changelog

All notable changes to ModelRouter-App are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

**Versioning note:** The backend Maven artifact remains `0.1.0-preview.1` until a stable backend release; the **GUI launcher** (`launcher-java`) is versioned separately (e.g. `1.0.1`). Frontend `package.json` tracks the same preview line as the backend unless stated otherwise.

## [1.0.1] - 2026-03-21

### Added
- **`launcher-java`**: Java 17 Swing GUI launcher with embedded static HTTP server for `frontend/dist`, subprocess management for `modelrouter.jar`, zh/ja/en UI (`ResourceBundle`).
- **Scripts**: `build-launcher.bat`, `build-launcher.sh`, `launcher.bat`, `launcher.sh`; root artifact `modelrouter-launcher.jar` (version **1.0.1**).
- **Documentation**: README / README.en.md / README.ja.md expanded (architecture, ports 20118/20119, admin init, full API list, GUI launcher vs `start.*`); `INSTALL.md` corrected (Web UI on **20119**).
- **Source file headers**: English JSDoc/Javadoc-style headers (`@version 1.0.1`, `@since 2026-03-21`, `@author wesun hu`) on backend Java, launcher Java, frontend `src` TS/TSX; Python migration script and `scripts/add_source_headers.py` documented.

### Changed
- **Launcher JAR name**: Maven output `modelrouter-launcher-1.0.1.jar` (aligned with `launcher-java` **1.0.1** in `pom.xml`).

## [0.1.0-preview.1] - 2026-03-20

### 新增
- **免责声明与协议**: 新增 LEGAL.md / LEGAL.en.md / LEGAL.ja.md（中英日三语）
- **多语言 README**: 新增 README.en.md、README.ja.md
- **OpenClaw 配置指南**: 新增 docs/OPENCLAW.md，指导配置 OpenClaw 使用 ModelRouter
- **StaticResourceMethodFilter**: 拦截对静态路径的 POST 请求，返回 405 而非 500
- **API 路径扩展**: 支持 `/api/v1/*` 及尾部斜杠，兼容更多客户端

### 修复
- **POST 405 错误**: 客户端 POST 到静态路径时抛出异常，现返回 JSON 405
- **/api/v1 路径**: 部分客户端 base URL 为 /api 时请求失败，现已支持
- **ModelApiClient**: 本地模型调用路径由 `/chat/completions` 修正为 `/v1/chat/completions`
- **Docker 网络**: application-docker.yml 中本地服务地址由 `modelrouter-local` 改为 `local`
- **init.sql**: Local Provider 种子数据 base_url 改为 `http://localhost:8081`
- **callLocal 错误处理**: 增加 HttpStatusCodeException 捕获
- **Frontend server**: 将 server.js 重命名为 server.mjs，确保 Node.js 正确解析 ESM

### 变更
- **许可证**: 项目许可证由 MIT 改为 Apache License 2.0
- **第三方组件授权**: README 新增组件授权列表
- **术语**: 「智能」改为「故障转移」，「成本控制」改为「成本可见」

### 文档
- README 顶部添加免责警告及 LEGAL 链接
- 开发/启动日志中输出免责提示
- Web 界面顶部显示免责警告
