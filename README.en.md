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
  <a href="#api">API</a>
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

---

## Project Structure

```
modelrouter-app/
├── backend/           # Spring Boot backend (Java 17)
├── frontend/          # React + Vite + MUI frontend
├── database/          # Init and migration scripts
├── launcher/          # Windows one-click launcher
├── build-windows.bat  # Windows build script
├── build-unix.sh      # macOS/Linux build script
├── modelrouter.jar   # Build output (for running)
└── data/
    └── modelrouter.db # SQLite data (generated at runtime)
```

---

## Requirements

| Requirement | Purpose |
|-------------|---------|
| **JDK 17+** | Run backend (required) |
| **Node.js 18+** | Build frontend (build time only) |
| **Maven 3.6+** | Build backend (recommended, or use bundled Maven Wrapper) |
| **Python 3.8+** | Optional, for launcher or EXE build |

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

**Option A: One-click launcher (recommended)**

```cmd
# Build EXE first (one-time)
cd launcher
pip install pyinstaller
pyinstaller --onefile --windowed --name ModelRouterLauncher launcher.py

# Copy dist\ModelRouterLauncher.exe to project root (same level as modelrouter.jar)
# Double-click ModelRouterLauncher.exe to start
```

**Option B: Python script**

```cmd
python launcher\launcher.py
```

**Option C: Run jar directly**

```cmd
java -jar modelrouter.jar
```

#### 5. Access

Open browser: **http://localhost:20118**

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
java -jar modelrouter.jar
```

Or Python launcher:

```bash
python3 launcher/launcher.py
```

#### 4. Access

**http://localhost:20118**

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
java -jar modelrouter.jar
```

Or:

```bash
python3 launcher/launcher.py
```

#### 4. Access

**http://localhost:20118**

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

1. **Add platform**: Configure Provider in "Platform Management" (e.g., OpenAI, Alibaba Bailian)
2. **Add model**: Add models in "Model Management", link to platform
3. **Add API Key**: Enter Key and Secret in "API Key", bind to models
4. **Create route**: Set primary and backup models in "Routes"
5. **Test**: Verify in "Model Test" chat

### Data files

- **SQLite mode**: Data saved in `./data/modelrouter.db`
- First run auto-creates tables and initializes platform list

---

## API

### OpenAI-compatible endpoint

ModelRouter provides OpenAI-compatible chat API:

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

### Management API

- Platforms: `GET/POST /api/providers`
- Models: `GET/POST /api/models`
- API Keys: `GET/POST /api/api-keys`
- Routes: `GET/POST /api/routes`
- Usage stats: `GET /api/usage-logs`

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

JDK 17 required. Check: `java -version`, ensure `JAVA_HOME` points to JDK 17.

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

### Launcher (Python)

| Component | Purpose | License |
|-----------|----------|---------|
| tkinter | GUI | Python stdlib (PSF License) |
| PyInstaller | EXE packaging (optional) | GPL-2.0 / Commercial dual-license |
