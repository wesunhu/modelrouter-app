# ModelRouter-App

> **Warning**: This software is an experimental project. Do not expose it to the public internet. No warranty is provided; all risks and consequences are borne solely by the user. See [LEGAL.en.md](LEGAL.en.md) for details.
>
> **Readme**: [中文](README.md) | [English](README.en.md) | [日本語](README.ja.md)

<p align="center">
  <strong>Model Routing System</strong> · Unified management and forwarding of multi-platform AI APIs
</p>

<p align="center">
  <a href="#features">Features</a> ·
  <a href="#installation">Installation</a> ·
  <a href="#usage">Usage</a> ·
  <a href="#api">API</a> ·
  <a href="RELEASE_NOTES.md">Release Notes</a> ·
  <a href="docs/OPENCLAW.md">OpenClaw Setup</a>
</p>

---

## Introduction

ModelRouter-App is an open-source AI model routing management platform that unifies multiple AI services (OpenAI, Alibaba Cloud, Zhipu, DeepSeek, etc.) with distribution and cost visibility through routing and API key management.

- **Out of the box**: SQLite standalone mode, no PostgreSQL or Docker required
- **OpenAI compatible**: Provides `/v1/chat/completions` standard interface for ChatGPT clients, Open WebUI, etc.
- **Cross-platform**: Supports Windows, macOS, Linux

---

## Features

| Feature | Description |
|---------|-------------|
| Fault-tolerant routing | Automatic failover to backup model when primary fails |
| Multi-platform | Supports 10+ platforms: OpenAI, Alibaba Bailian, Zhipu, DeepSeek, OpenRouter, etc. |
| API Key management | Multi-key rotation, permission assignment by platform and model |
| Usage statistics | Token and cost tracking with charts |
| Model testing | Built-in chat test to verify routing and keys |
| Web admin UI | Providers / models / API keys / routes / usage / model test; admin session |
| UI languages | Chinese, Japanese, English (i18next) |
| GUI launcher | Optional `modelrouter-launcher.jar` (`launcher-java`, Swing + embedded static server, UI in zh/ja/en) |

---

## Architecture (matches the codebase)

| Module | Stack | Notes |
|--------|-------|--------|
| `backend/` | Spring Boot 3.2.x, Java 17 | REST admin API, `/v1` OpenAI-compatible proxy; default `spring.profiles.active=sqlite`, `./data/modelrouter.db` |
| `frontend/` | React 18, Vite 5, MUI 5 | Admin UI; `VITE_API_URL` at build time |
| `launcher-java/` | Java 17, Swing, `HttpServer` | Optional; serves `frontend/dist`, spawns backend child process |
| `database/` | SQL / Python helpers | Init and migration scripts |

**Default ports** (override via args or config):

| Service | Port | Purpose |
|---------|------|---------|
| Backend HTTP | **20118** | REST, `/v1/*` chat API |
| Frontend static (`start.*` / GUI launcher) | **20119** | Browser admin UI |

**Security**: Intended for LAN/localhost only; do not expose admin or route keys to the internet. See [LEGAL.en.md](LEGAL.en.md).

---

## Project Structure

```
modelrouter-app/
├── backend/              # Spring Boot backend (Java 17, see backend/pom.xml)
├── launcher-java/        # Java GUI launcher sources (Java 17, see launcher-java/pom.xml)
├── frontend/             # React + Vite + MUI (Node 18+, see package.json engines)
├── database/             # Init and migration scripts
├── start.bat / start.sh  # CLI start (static server + backend)
├── launcher.bat / launcher.sh  # Run GUI launcher (needs modelrouter-launcher.jar)
├── build-windows.bat / build-unix.sh   # Main build → modelrouter.jar + frontend/dist
├── build-launcher.bat / build-launcher.sh  # Build GUI launcher → modelrouter-launcher.jar
├── modelrouter.jar       # Backend runnable jar
├── modelrouter-launcher.jar  # GUI launcher (optional)
└── data/
    └── modelrouter.db    # SQLite (generated at runtime)
```

---

## Requirements

Versions should match the repo: **backend and launcher-java target Java 17** (`java.version` / `release` in `backend/pom.xml` and `launcher-java/pom.xml`). Any **JDK 17 or newer** runtime can run these artifacts.

| Requirement | Purpose |
|-------------|---------|
| **JDK 17+** | Run `modelrouter.jar` and build/run `modelrouter-launcher.jar` (GUI launcher) |
| **Node.js 18+** | Build frontend (`npm install` / `npm run build`); required for `start.bat`/`start.sh` static server (matches `frontend/package.json` `engines.node`) |
| **Maven 3.6+** | Build backend and GUI launcher (recommended; or use `backend/mvnw` / `backend/mvnw.cmd` only) |

**GUI launcher**: Node is **not** required to *serve* the frontend (embedded HTTP server), but you must run the main build first to produce `frontend/dist`. **`start.bat` / `start.sh`** still need Node (`npx serve`).

---

## Installation

### Windows

#### 1. Install JDK 17

```powershell
# Using winget
winget install Microsoft.OpenJDK.17

# Or Chocolatey
choco install temurin17
```

Ensure `JAVA_HOME` points to JDK 17 installation directory.

#### 2. Install Maven (recommended, avoids Wrapper download issues)

```powershell
winget install Apache.Maven
# or
choco install maven
```

#### 3. Build

```cmd
# Run in project root
build-windows.bat
```

The script will: install frontend deps → build frontend → build backend → copy `modelrouter.jar` to project root.

#### 4. Run

**Option A: Command line (recommended)**

```cmd
start.bat
# Default frontend 20119, backend 20118; custom: start.bat 20119 20118
```

**Option B: Backend only**

```cmd
java -jar modelrouter.jar
```

**Option C: GUI launcher (optional, no Node for frontend)**

Requires **JDK 17+** (same as backend). After the main build (step 3) produces `frontend/dist` and `modelrouter.jar`, build the launcher:

```cmd
build-launcher.bat
```

Run:

```cmd
launcher.bat
REM or: java -jar modelrouter-launcher.jar
```

On **macOS / Linux**, use `./build-launcher.sh` and `./launcher.sh` (`chmod +x` if needed).

#### 5. Access

Open browser: **http://localhost:20119** (frontend) or **http://localhost:20118** (backend API)

---

### macOS

#### 1. Install dependencies

```bash
# Using Homebrew
brew install openjdk@17
brew install maven
brew install node
```

Configure Java:

```bash
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

#### 2. Build

```bash
chmod +x build-unix.sh
./build-unix.sh
```

#### 3. Run

```bash
chmod +x start.sh
./start.sh
```

Or backend only:

```bash
java -jar modelrouter.jar
```

GUI launcher (optional): run `./build-unix.sh` first, then `./build-launcher.sh`, then `./launcher.sh`.

#### 4. Access

**http://localhost:20119** (frontend) or **http://localhost:20118** (backend API)

---

### Linux

#### 1. Install dependencies

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

#### 2. Build

```bash
chmod +x build-unix.sh
./build-unix.sh
```

#### 3. Run

```bash
chmod +x start.sh
./start.sh
```

Or backend only:

```bash
java -jar modelrouter.jar
```

GUI launcher (optional): run `./build-unix.sh` first, then `./build-launcher.sh`, then `./launcher.sh`.

#### 4. Access

**http://localhost:20119** (frontend) or **http://localhost:20118** (backend API)

---

### Docker (optional, cross-platform)

For Docker deployment (PostgreSQL mode):

```bash
docker-compose up -d
```

- Frontend: http://localhost:20119  
- Backend: http://localhost:20118  

For networks with restricted access, create `.env` in project root:

```
DOCKER_MIRROR=docker.1ms.run/library/
```

---

## Usage

### Basic workflow

1. **First visit**: Open **http://localhost:20119** in a browser (if you only run `java -jar modelrouter.jar` without a separate frontend, start the frontend as in [Installation](#installation) or use the GUI launcher). **Initialize the admin account** when prompted (`POST /api/auth/init`, then session via `/api/auth/login`).
2. **Add platform**: Configure Provider in "Platform Management" (e.g., OpenAI, Alibaba Bailian)
3. **Add model**: Add models in "Model Management", link to platform
4. **Add API Key**: Enter Key and Secret in "API Key", bind to models
5. **Create route**: Set primary and backup models in "Routes"; obtain the **route API key** for clients calling `/v1/chat/completions`
6. **Test**: Verify in "Model Test" chat

### Data files

- **SQLite mode**: Data saved in `./data/modelrouter.db`
- First run auto-creates tables and initializes platform list

---

## API

### OpenAI-compatible (proxy)

- `POST /v1/chat/completions` (equivalent: `POST /api/v1/chat/completions`)
- `GET /v1/models`, `GET /v1/models/{modelId}` (also under `/api/v1/...`)

Auth: **route API key** from the Routes UI, `Authorization: Bearer <route key>`.

```bash
curl -X POST http://localhost:20118/v1/chat/completions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ROUTE_API_KEY" \
  -d '{
    "model": "gpt-3.5-turbo",
    "messages": [{"role": "user", "content": "Hello"}],
    "max_tokens": 256
  }'
```

### Auth (admin session)

- `GET /api/auth/needs-init` — whether first admin must be created  
- `POST /api/auth/init` — create first admin (only when none exists)  
- `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/me`

### Management REST (CRUD; see controllers for details)

- Providers: `/api/providers`
- Models: `/api/models`
- API keys: `/api/api-keys`
- Routes: `/api/routes`
- Usage: `/api/usage-logs` (includes `statistics`, `route/{routeId}`, etc.)
- Model test: `POST /api/test/chat`

---

## Configuration

| File | Description |
|------|-------------|
| `application.yml` | Default config (SQLite) |
| `application-sqlite.yml` | SQLite datasource, dialect |
| `application-docker.yml` | Docker env (PostgreSQL) |

Default profile is SQLite; use `SPRING_PROFILES_ACTIVE=docker` for Docker/PostgreSQL.

---

## FAQ

### Windows: `mvn` or `mvnw.cmd` not found

Install Maven and retry: `winget install Apache.Maven` or `choco install maven`.

### Java version error (class file version 61.0 / 52.0)

**JDK 17+** required (class file 61 = Java 17). Both `modelrouter.jar` and `modelrouter-launcher.jar` are built for Java 17. Check: `java -version`, ensure `JAVA_HOME` points to JDK 17 or newer.

### Maven download failed (zip END header not found)

Often caused by incomplete download. Install local Maven and use `mvn` to build.

### Empty platform list or auth_header error

Run migration script to init/fix data:

```bash
python database/import_register_url_sqlite.py
```

Or double-click `database\import-migrate-sqlite.bat` (Windows).

---

## Development & Contributing

- Backend: `cd backend && mvn spring-boot:run`
- Frontend: `cd frontend && npm run dev`

Issues and Pull Requests are welcome.

---

## License

This project is licensed under [Apache License 2.0](LICENSE).

---

## Third-Party Licenses

Direct dependencies and their licenses. Full transitive deps can be audited with `mvn dependency:tree` (backend) and `npx license-checker` (frontend).

### Backend (Java / Maven)

| Component | License |
|-----------|---------|
| Spring Boot | Apache-2.0 |
| Spring Data JPA | Apache-2.0 |
| SQLite JDBC (org.xerial) | Apache-2.0 |
| Hibernate Community Dialects | LGPL-2.1-or-later |
| PostgreSQL JDBC | BSD-2-Clause |
| Jackson | Apache-2.0 |
| Apache HttpClient 5 | Apache-2.0 |
| Spring WebFlux | Apache-2.0 |
| Caffeine | Apache-2.0 |

### Frontend (Node.js / npm)

| Component | License |
|-----------|---------|
| React / react-dom | MIT |
| react-router-dom | MIT |
| @mui/material, @mui/icons-material | MIT |
| @emotion/react, @emotion/styled | MIT |
| axios | MIT |
| chart.js, react-chartjs-2 | MIT |
| i18next, react-i18next | MIT |
| TypeScript | Apache-2.0 |
| Vite, @vitejs/plugin-react | MIT |

### GUI launcher (`launcher-java`)

| Component | Notes |
|-----------|--------|
| JDK standard library (Swing, `HttpServer`, etc.) | Subject to your JDK distribution license |

