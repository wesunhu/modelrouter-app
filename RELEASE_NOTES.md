# ModelRouter-App Release Notes

## Release rhythm

| Track | Artifact | Notes |
|-------|----------|--------|
| **Backend + Web UI** | `modelrouter-backend` **0.1.0-preview.1** (Maven), frontend **0.1.0-preview.1** (`package.json`) | Preview line; breaking changes possible before 1.0. |
| **GUI launcher** | `modelrouter-launcher` **1.0.1** (Maven `launcher-java/`) | Independent semver; ships as `modelrouter-launcher.jar`. |

Full history: [CHANGELOG.md](CHANGELOG.md).

---

## v1.0.1 (2026-03-21) — Launcher & documentation

### Highlights

| Area | What is new |
|------|-------------|
| **GUI launcher** | `launcher-java`: Java 17, Swing, embedded HTTP for static `frontend/dist`, start/stop backend process; UI languages zh / ja / en. |
| **Build & run** | `build-launcher.bat` / `build-launcher.sh` → `modelrouter-launcher.jar`; `launcher.bat` / `launcher.sh` to run. |
| **Docs** | README (zh/en/ja): architecture, ports **20118** (API) / **20119** (Web UI), admin bootstrap, REST/OpenAI API summary, GUI vs `start.bat` / `start.sh`. `INSTALL.md`: Web UI URL and Node/Maven notes fixed. |
| **Source headers** | English headers on backend Java, launcher Java, frontend `src` TS/TSX (`@version 1.0.1`, `@since 2026-03-21`, `@author wesun hu`). |

### Upgrade from v0.1.0-preview.1

- No database migration required for this release.
- To use the GUI launcher: run main build (`build-windows.bat` / `build-unix.sh`), then `build-launcher.bat` or `./build-launcher.sh`, then `launcher.bat` or `./launcher.sh`.

### Download & run (unchanged defaults)

```bash
./build-unix.sh          # macOS / Linux
./build-windows.bat      # Windows

./start.sh               # or start.bat — needs Node for static server
java -jar modelrouter.jar   # backend only

# Optional GUI launcher (after build-launcher)
./build-launcher.sh && ./launcher.sh
```

- **Web UI:** http://localhost:20119  
- **API:** http://localhost:20118  

---

## v0.1.0-preview.1 (2026-03-20)

### 概述

ModelRouter-App 0.1.0-preview.1 为首个预览版本，提供 AI 模型路由管理、多平台 API 统一接入、故障转移、使用统计等核心功能。

---

### 新增功能

| 功能 | 说明 |
|------|------|
| **多平台路由** | 支持 OpenAI、阿里云百炼、智谱、DeepSeek、OpenRouter、Ollama 等 10+ 平台 |
| **故障转移** | 主模型失败时自动切换到备用模型 |
| **API Key 管理** | 多 Key 轮询，按平台与模型分配权限 |
| **使用统计** | Token 与费用记录、图表展示 |
| **模型测试** | 内置对话测试，验证路由与 Key |
| **OpenAI 兼容 API** | `/v1/chat/completions`、`/v1/models`，兼容 ChatGPT、Open WebUI、OpenClaw 等 |
| **SQLite 单机模式** | 开箱即用，无需 PostgreSQL、Docker |
| **多语言文档** | README、LEGAL 提供中文、英文、日文版本 |

---

### API 与兼容性

- **路径支持**
  - `POST /v1/chat/completions` 标准路径
  - `POST /api/v1/chat/completions` 供 base URL 为 `/api` 的客户端使用
  - 支持带尾部斜杠的请求路径
- **认证**：Bearer Token（使用路由的 API Key）

---

### 文档与指南

- [README](README.md)（[EN](README.en.md) | [JA](README.ja.md)）
- [LEGAL 免责声明](LEGAL.md)（[EN](LEGAL.en.md) | [JA](LEGAL.ja.md)）
- [OpenClaw 配置指南](docs/OPENCLAW.md)
- [第三方组件授权](README.md#第三方组件授权)

---

### 许可证

- **本项目**：Apache License 2.0
- **第三方组件**：详见 README 中的「第三方组件授权」章节

---

### 已知限制与注意事项

1. **实验性项目**：仅供学习与研究，未部署企业级安全措施，严禁公网暴露
2. **安全**：仅在受控内网或本地环境运行
3. **数据**：API 密钥与配置的机密性、完整性不做保证

---

### 下载与运行

```bash
# 构建
./build-windows.bat   # Windows
./build-unix.sh       # macOS / Linux

# 运行
./start.sh          # Linux/macOS（默认前端 20119，后端 20118）
start.bat           # Windows
# 或仅后端
java -jar modelrouter.jar
```

访问管理界面：**http://localhost:20119**

---

*完整变更记录见 [CHANGELOG.md](CHANGELOG.md)。*
