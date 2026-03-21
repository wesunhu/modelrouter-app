# ModelRouter-App

> **警告**：本软件为实验性项目，严禁暴露于公网。无任何担保，使用风险与后果完全由用户自行承担。详见 [LEGAL.md](LEGAL.md)（[EN](LEGAL.en.md) | [JA](LEGAL.ja.md)）。
>
> **Readme**： [中文](README.md) | [English](README.en.md) | [日本語](README.ja.md)

<p align="center">
  <strong>模型路由系统</strong> · 多平台 AI API 统一管理与转发
</p>

<p align="center">
  <a href="#功能特性">功能</a> ·
  <a href="#安装运行">安装</a> ·
  <a href="#使用指南">使用</a> ·
  <a href="#api-接口">API</a> ·
  <a href="docs/OPENCLAW.md">OpenClaw 配置</a> ·
  <a href="RELEASE_NOTES.md">Release Notes</a>
</p>

---

## 简介

ModelRouter-App 是一个开源的 AI 模型路由管理平台，可将多家 AI 服务（OpenAI、阿里云、智谱、DeepSeek 等）统一接入，通过路由与 Key 管理实现分发与成本可见。

- **开箱即用**：SQLite 单机模式，无需 PostgreSQL、无需 Docker
- **OpenAI 兼容**：提供 `/v1/chat/completions` 标准接口，对接 ChatGPT 客户端、Open WebUI 等
- **跨平台**：支持 Windows、macOS、Linux
<img width="400" height="345" alt="340a0e6ec6d340a2157f64fd844647fe" src="https://github.com/user-attachments/assets/f6a6b6f7-05bb-47fe-90cd-04431c17c6b7" />
<img width="400" height="500" alt="689c5ab5e6905cdcad098ded674da7bb" src="https://github.com/user-attachments/assets/cb905cbf-4dfc-40ec-9787-5e6b24a79ff9" />
<img width="400" height="500" alt="b0fe8abb05a79d222febb3c1288e59a9" src="https://github.com/user-attachments/assets/3709ed35-305b-4e69-8441-9b0469f59400" />
<img width="400" height="500" alt="c8e5f47bc0c4496898840de7733b8032" src="https://github.com/user-attachments/assets/649435a8-23ad-4bec-9bfe-f4101343b799" />
<img width="400" height="500" alt="c4865938e2356a22923f96248739bc18" src="https://github.com/user-attachments/assets/b620933f-5c35-46ba-9a30-d980c16030c3" />
<img width="400" height="500" alt="b741596575e6d6fcdcee924603601a9a" src="https://github.com/user-attachments/assets/0d75d397-5bdc-48f0-b0c9-6211268252d0" />

---

## 功能特性

| 功能 | 说明 |
|------|------|
| 故障转移路由 | 主模型失败时自动故障转移到备用模型 |
| 多平台 | 支持 OpenAI、阿里云百炼、智谱、DeepSeek、OpenRouter 等 10+ 平台 |
| API Key 管理 | 多 Key 轮询，按平台与模型分配权限 |
| 使用统计 | Token 与费用记录、图表展示 |
| 模型测试 | 内置对话测试，验证路由与 Key |
| Web 管理台 | 平台/模型/API Key/路由/用量/模型测试；管理员会话 |
| 界面语言 | 前端支持中文、日本語、English（i18next） |
| GUI 启动器 | 可选：`modelrouter-launcher.jar`（`launcher-java`，Swing + 内嵌静态服务，界面中/日/英） |

---

## 架构说明（与仓库实现一致）

| 模块 | 技术栈 | 说明 |
|------|--------|------|
| `backend/` | Spring Boot 3.2.x，Java 17 | REST 管理 API、`/v1` OpenAI 兼容转发；默认 `spring.profiles.active=sqlite`，数据 `./data/modelrouter.db` |
| `frontend/` | React 18、Vite 5、MUI 5 | 管理 Web UI；`VITE_API_URL` 构建时指向后端 |
| `launcher-java/` | Java 17、Swing、`HttpServer` | 可选；本地静态托管 `frontend/dist`、启动后端子进程 |
| `database/` | SQL / Python 辅助脚本 | 初始化与迁移；详见各脚本说明 |

**默认端口**（可在启动参数或配置中修改）：

| 服务 | 端口 | 说明 |
|------|------|------|
| 后端 HTTP | **20118** | REST、`/v1/*` 聊天 API |
| 前端静态（`start.*` / GUI 启动器） | **20119** | 浏览器管理界面 |

**安全提示**：仅适用于内网或本机；管理员与路由密钥勿暴露于公网。详见 [LEGAL.md](LEGAL.md)。

---

## 项目结构

```
modelrouter-app/
├── backend/              # Spring Boot 后端（Java 17，见 backend/pom.xml）
├── launcher-java/        # Java GUI 启动器源码（Java 17，见 launcher-java/pom.xml）
├── frontend/             # React + Vite + MUI 前端（Node 18+，见 package.json engines）
├── database/             # 初始化与迁移脚本
├── start.bat / start.sh  # 命令行启动（前端静态服务 + 后端）
├── launcher.bat / launcher.sh  # 运行 GUI 启动器（需先有 modelrouter-launcher.jar）
├── build-windows.bat / build-unix.sh   # 构建主项目 → modelrouter.jar + frontend/dist
├── build-launcher.bat / build-launcher.sh  # 构建 GUI 启动器 → modelrouter-launcher.jar
├── modelrouter.jar       # 后端可执行包（构建产物）
├── modelrouter-launcher.jar  # GUI 启动器（可选，由 build-launcher 生成）
└── data/
    └── modelrouter.db    # SQLite 数据（运行时生成）
```

---

## 环境要求

版本需与仓库配置一致：**后端与 launcher-java 均使用 Java 17**（`backend/pom.xml`、`launcher-java/pom.xml` 中 `java.version` / `release`）；运行任意 **JDK 17 或更高** 的 JRE/JDK 均可加载上述字节码。

| 环境 | 用途 |
|------|------|
| **JDK 17+** | 运行 `modelrouter.jar`、构建并运行 `modelrouter-launcher.jar`（GUI 启动器） |
| **Node.js 18+** | 构建前端（`npm install` / `npm run build`）；使用 `start.bat`/`start.sh` 时提供静态服务（与 `frontend/package.json` 的 `engines.node` 一致） |
| **Maven 3.6+** | 构建后端与 GUI 启动器（推荐安装；也可仅用项目内 `backend/mvnw` / `backend/mvnw.cmd`） |

说明：使用 **GUI 启动器** 运行前端时**不需要**安装 Node；但仍需先执行主构建以生成 `frontend/dist`。使用 **`start.bat` / `start.sh`** 时，需 Node（通过 `npx serve` 提供静态文件）。

---

## 安装运行

### 一、Windows

#### 1. 安装 JDK 17

```powershell
# 使用 winget
winget install Microsoft.OpenJDK.17

# 或使用 Chocolatey
choco install temurin17
```

确保 `JAVA_HOME` 指向 JDK 17 安装目录。

#### 2. 安装 Maven（推荐，避免 Wrapper 下载问题）

```powershell
winget install Apache.Maven
# 或
choco install maven
```

#### 3. 构建项目

```cmd
# 在项目根目录执行
build-windows.bat
```

脚本会依次执行：安装前端依赖 → 构建前端 → 构建后端 → 复制 `modelrouter.jar` 到项目根目录。

#### 4. 运行

**方式 A：命令行启动（推荐）**

```cmd
start.bat
# 默认前端 20119，后端 20118；指定端口：start.bat 20119 20118
```

**方式 B：仅运行后端**

```cmd
java -jar modelrouter.jar
```

**方式 C：GUI 启动器（可选，无需 Node 跑前端）**

需与后端相同 **JDK 17+**。先执行上文 **构建项目** 得到 `frontend/dist` 与 `modelrouter.jar`，再构建启动器：

```cmd
build-launcher.bat
```

运行：

```cmd
launcher.bat
REM 或: java -jar modelrouter-launcher.jar
```

**macOS / Linux** 对应使用 `./build-launcher.sh`、 `./launcher.sh`（需 `chmod +x`）。

#### 5. 访问

打开浏览器访问：**http://localhost:20119**（前端）或 **http://localhost:20118**（后端 API）

---

### 二、macOS

#### 1. 安装依赖

```bash
# 使用 Homebrew
brew install openjdk@17
brew install maven
brew install node
```

配置 Java：

```bash
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

#### 2. 构建项目

```bash
chmod +x build-unix.sh
./build-unix.sh
```

#### 3. 运行

```bash
chmod +x start.sh
./start.sh
# 默认前端 20119，后端 20118；指定端口：./start.sh 20119 20118
```

或仅运行后端：

```bash
java -jar modelrouter.jar
```

GUI 启动器（可选）：先 `./build-unix.sh`，再 `./build-launcher.sh`，然后 `./launcher.sh`。

#### 4. 访问

**http://localhost:20119**（前端）或 **http://localhost:20118**（后端 API）

---

### 三、Linux

#### 1. 安装依赖

**Ubuntu / Debian**

```bash
sudo apt update
sudo apt install openjdk-17-jdk maven nodejs npm
```

**CentOS / RHEL / Fedora**

```bash
sudo dnf install java-17-openjdk-devel maven nodejs npm
```

**Arch Linux**

```bash
sudo pacman -S jdk17-openjdk maven nodejs npm
```

#### 2. 构建项目

```bash
chmod +x build-unix.sh
./build-unix.sh
```

#### 3. 运行

```bash
chmod +x start.sh
./start.sh
```

或仅运行后端：

```bash
java -jar modelrouter.jar
```

GUI 启动器（可选）：先 `./build-unix.sh`，再 `./build-launcher.sh`，然后 `./launcher.sh`。

#### 4. 访问

**http://localhost:20119**（前端）或 **http://localhost:20118**（后端 API）

---

### 四、Docker 部署（跨平台可选）

如需使用 Docker 部署（PostgreSQL 模式）：

```bash
docker-compose up -d
```

- 前端：http://localhost:20119  
- 后端：http://localhost:20118  

国内网络可配置镜像，在项目根目录创建 `.env`：

```
DOCKER_MIRROR=docker.1ms.run/library/
```

---

## 使用指南

### 基本流程

1. **首次访问 Web 控制台**：浏览器打开 **http://localhost:20119**（若仅用 `java -jar modelrouter.jar` 且未单独启动前端，请先按上文「安装运行」启动前端或改用 GUI 启动器）。按系统提示 **初始化管理员账号**（`POST /api/auth/init`、`/api/auth/login` 会话）。
2. **添加平台**：在「平台管理」中配置 Provider（如 OpenAI、阿里云百炼等）
3. **添加模型**：在「模型管理」中添加模型，关联平台
4. **添加 API Key**：在「API Key」中填写 Key 与 Secret，并绑定模型
5. **创建路由**：在「路由」中设置主模型与备用模型，获取 **路由 API Key**（供客户端调用 `/v1/chat/completions`）
6. **测试**：在「模型测试」中验证对话

### 数据文件

- **SQLite 模式**：数据保存在 `./data/modelrouter.db`
- 首次运行会自动建表并初始化平台列表

---

## API 接口

### OpenAI 兼容接口（转发/路由）

- `POST /v1/chat/completions`（及等价路径 `POST /api/v1/chat/completions`）
- `GET /v1/models`、`GET /v1/models/{modelId}`（同上可用 `/api/v1/...`）

鉴权：使用 **路由** 上生成的 Key，`Authorization: Bearer <路由 API Key>`。

```bash
curl -X POST http://localhost:20118/v1/chat/completions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ROUTE_API_KEY" \
  -d '{
    "model": "gpt-3.5-turbo",
    "messages": [{"role": "user", "content": "你好"}],
    "max_tokens": 256
  }'
```

### 认证（管理后台会话）

- `GET /api/auth/needs-init` — 是否需要初始化首个管理员  
- `POST /api/auth/init` — 创建首个管理员（仅当系统中尚无管理员时）  
- `POST /api/auth/login`、`POST /api/auth/logout`、`GET /api/auth/me`

### 管理 REST（CRUD，具体以控制器为准）

- 平台：`/api/providers`
- 模型：`/api/models`
- API Key：`/api/api-keys`
- 路由：`/api/routes`
- 使用统计：`/api/usage-logs`（含 `statistics`、`route/{routeId}` 等子路径）
- 模型测试：`POST /api/test/chat`

---

## 配置说明

| 文件 | 说明 |
|------|------|
| `application.yml` | 默认配置（SQLite） |
| `application-sqlite.yml` | SQLite 数据源、 dialect |
| `application-docker.yml` | Docker 环境（PostgreSQL） |

启动时默认使用 SQLite；Docker 部署时通过 `SPRING_PROFILES_ACTIVE=docker` 切换到 PostgreSQL。

---

## 常见问题

### Windows：`mvn` 或 `mvnw.cmd` 找不到

安装 Maven 后重试构建：`winget install Apache.Maven` 或 `choco install maven`。

### Java 版本错误（class file version 61.0 / 52.0）

需要 **JDK 17+**（class 61 = Java 17）。`modelrouter.jar` 与 `modelrouter-launcher.jar` 均为 Java 17 编译。检查：`java -version`，并确保 `JAVA_HOME` 指向 JDK 17 或更高。

### Maven 下载失败（zip END header not found）

多因网络问题导致 Maven 发行版下载不完整。建议安装本地 Maven 后使用 `mvn` 构建。

### 平台列表为空或 auth_header 报错

执行迁移脚本初始化/修复数据：

```bash
python database/import_register_url_sqlite.py
```

或双击 `database\import-migrate-sqlite.bat`（Windows）。

---

## 开发与贡献

- 后端：`cd backend && mvn spring-boot:run`
- 前端：`cd frontend && npm run dev`

欢迎提交 Issue 与 Pull Request。

---

## 许可证

本项目采用 [Apache License 2.0](LICENSE) 许可证。

---

## 第三方组件授权

以下为本项目直接依赖的第三方组件及其授权信息。完整传递依赖可通过 `mvn dependency:tree`（后端）与 `npx license-checker`（前端）审计。

### 后端 (Java / Maven)

| 组件 | 版本 | 授权 |
|------|------|------|
| Spring Boot | 3.2.x | Apache-2.0 |
| Spring Data JPA | - | Apache-2.0 |
| SQLite JDBC (org.xerial) | - | Apache-2.0 |
| Hibernate Community Dialects | - | LGPL-2.1-or-later |
| PostgreSQL JDBC | - | BSD-2-Clause |
| Jackson (com.fasterxml.jackson) | - | Apache-2.0 |
| Apache HttpClient 5 | - | Apache-2.0 |
| Spring WebFlux | - | Apache-2.0 |
| Caffeine | - | Apache-2.0 |

### 前端 (Node.js / npm)

| 组件 | 授权 |
|------|------|
| React / react-dom | MIT |
| react-router-dom | MIT |
| @mui/material, @mui/icons-material | MIT |
| @emotion/react, @emotion/styled | MIT |
| axios | MIT |
| chart.js, react-chartjs-2 | MIT |
| i18next, react-i18next | MIT |
| TypeScript | Apache-2.0 |
| Vite, @vitejs/plugin-react | MIT |

### GUI 启动器（`launcher-java`）

| 组件 | 说明 |
|------|------|
| JDK 标准库（Swing、`com.sun.net.httpserver` 等） | 随 JDK 发行许可（如 Oracle/OpenJDK） |


