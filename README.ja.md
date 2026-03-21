# ModelRouter-App

> **警告**：本ソフトウェアは実験的プロジェクトです。公衆インターネットに公開しないでください。保証は一切なく、使用リスクと結果はすべてユーザーが負担します。詳細は [LEGAL.ja.md](LEGAL.ja.md) をご覧ください。
>
> **Readme**： [中文](README.md) | [English](README.en.md) | [日本語](README.ja.md)

<p align="center">
  <strong>モデルルーティングシステム</strong> · マルチプラットフォーム AI API の統一管理と転送
</p>

<p align="center">
  <a href="#機能">機能</a> ·
  <a href="#インストール">インストール</a> ·
  <a href="#使い方">使い方</a> ·
  <a href="#api">API</a> ·
  <a href="RELEASE_NOTES.md">Release Notes</a> ·
  <a href="docs/OPENCLAW.md">OpenClaw 設定</a>
</p>

---

## 概要

ModelRouter-App は、複数の AI サービス（OpenAI、アリババクラウド、智譜、DeepSeek など）を統合し、ルーティングと API キー管理により配信とコスト可視化を実現するオープンソースの AI モデルルーティング管理プラットフォームです。

- **すぐ使える**：SQLite スタンドアロンモード、PostgreSQL や Docker 不要
- **OpenAI 互換**：`/v1/chat/completions` 標準インターフェースを提供、ChatGPT クライアント、Open WebUI などに対応
- **クロスプラットフォーム**：Windows、macOS、Linux 対応

---

## 機能

| 機能 | 説明 |
|------|------|
| 故障転移ルーティング | プライマリモデル失敗時にバックアップモデルへ自動フェイルオーバー |
| マルチプラットフォーム | OpenAI、アリババ百煉、智譜、DeepSeek、OpenRouter など 10+ プラットフォーム対応 |
| API キー管理 | 複数キーのローテーション、プラットフォーム・モデル別の権限割り当て |
| 使用統計 | トークン・コスト記録、グラフ表示 |
| モデルテスト | ルーティングとキー検証用の組み込みチャットテスト |
| Web 管理画面 | プラットフォーム／モデル／API Key／ルート／利用状況／モデルテスト；管理者セッション |
| 表示言語 | 中国語・日本語・English（i18next） |
| GUI ランチャー | 任意：`modelrouter-launcher.jar`（`launcher-java`、Swing + 内蔵静的配信、UI は中/日/英） |

---

## アーキテクチャ（コードベースと一致）

| モジュール | 技術 | 説明 |
|------------|------|------|
| `backend/` | Spring Boot 3.2.x、Java 17 | REST 管理 API、`/v1` OpenAI 互換プロキシ；既定 `spring.profiles.active=sqlite`、データ `./data/modelrouter.db` |
| `frontend/` | React 18、Vite 5、MUI 5 | 管理 Web UI；ビルド時 `VITE_API_URL` |
| `launcher-java/` | Java 17、Swing、`HttpServer` | 任意；`frontend/dist` を配信しバックエンド子プロセスを起動 |
| `database/` | SQL / Python 補助 | 初期化・マイグレーション |

**既定ポート**（起動引数や設定で変更可）：

| サービス | ポート | 説明 |
|----------|--------|------|
| バックエンド HTTP | **20118** | REST、`/v1/*` チャット API |
| フロント静的（`start.*` / GUI ランチャー） | **20119** | ブラウザ管理画面 |

**セキュリティ**：LAN／ローカル向け。管理者およびルート用キーを公網に晒さないこと。詳細は [LEGAL.ja.md](LEGAL.ja.md)。

---

## プロジェクト構成

```
modelrouter-app/
├── backend/              # Spring Boot バックエンド（Java 17、backend/pom.xml）
├── launcher-java/        # Java GUI ランチャー（Java 17、launcher-java/pom.xml）
├── frontend/             # React + Vite + MUI（Node 18+、package.json engines）
├── database/             # 初期化・マイグレーションスクリプト
├── start.bat / start.sh  # CLI 起動（静的配信 + バックエンド）
├── launcher.bat / launcher.sh  # GUI ランチャー（modelrouter-launcher.jar が必要）
├── build-windows.bat / build-unix.sh   # メインビルド → modelrouter.jar + frontend/dist
├── build-launcher.bat / build-launcher.sh  # GUI ランチャー → modelrouter-launcher.jar
├── modelrouter.jar       # バックエンド実行 JAR
├── modelrouter-launcher.jar  # GUI ランチャー（任意）
└── data/
    └── modelrouter.db    # SQLite（実行時生成）
```

---

## 環境要件

リポジトリでは **バックエンドと launcher-java は Java 17**（`backend/pom.xml`、`launcher-java/pom.xml`）。実行は **JDK 17 以上** で可。

| 環境 | 用途 |
|------|------|
| **JDK 17+** | `modelrouter.jar` の実行、GUI ランチャーのビルド・実行 |
| **Node.js 18+** | フロントエンドビルド；`start.bat`/`start.sh` 利用時は静的配信に必要（`frontend/package.json` の `engines.node` と一致） |
| **Maven 3.6+** | バックエンド・GUI ランチャーのビルド（推奨；`backend/mvnw` のみでも可） |

**GUI ランチャー**：フロント配信に Node は不要（内蔵 HTTP）。ただし事前にメインビルドで `frontend/dist` が必要。**`start.bat` / `start.sh`** 利用時は Node（`npx serve`）が必要。

---

## インストール

### Windows

#### 1. JDK 17 のインストール

```powershell
# winget を使用
winget install Microsoft.OpenJDK.17

# または Chocolatey
choco install temurin17
```

`JAVA_HOME` が JDK 17 のインストールディレクトリを指していることを確認してください。

#### 2. Maven のインストール（推奨、Wrapper のダウンロード問題を回避）

```powershell
winget install Apache.Maven
# または
choco install maven
```

#### 3. ビルド

```cmd
# プロジェクトルートで実行
build-windows.bat
```

スクリプトは順に：フロントエンド依存関係インストール → フロントエンドビルド → バックエンドビルド → `modelrouter.jar` をプロジェクトルートにコピー。

#### 4. 実行

**方式 A：コマンドライン（推奨）**

```cmd
start.bat
# 既定: フロント 20119、バック 20118；指定: start.bat 20119 20118
```

**方式 B：バックエンドのみ**

```cmd
java -jar modelrouter.jar
```

**方式 C：GUI ランチャー（任意、フロントに Node 不要）**

**JDK 17+** が必要。上記ビルドで `frontend/dist` と `modelrouter.jar` を生成したあと、ランチャーをビルド：

```cmd
build-launcher.bat
```

実行：

```cmd
launcher.bat
REM または: java -jar modelrouter-launcher.jar
```

**macOS / Linux** は `./build-launcher.sh` と `./launcher.sh`（実行権限: `chmod +x`）。

#### 5. アクセス

ブラウザで **http://localhost:20119**（フロント）または **http://localhost:20118**（バック API）を開く。

---

### macOS

#### 1. 依存関係のインストール

```bash
# Homebrew を使用
brew install openjdk@17
brew install maven
brew install node
```

Java の設定：

```bash
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

#### 2. ビルド

```bash
chmod +x build-unix.sh
./build-unix.sh
```

#### 3. 実行

```bash
chmod +x start.sh
./start.sh
```

またはバックエンドのみ：

```bash
java -jar modelrouter.jar
```

GUI ランチャー（任意）：先に `./build-unix.sh`、続けて `./build-launcher.sh`、実行は `./launcher.sh`。

#### 4. アクセス

**http://localhost:20119**（フロント）または **http://localhost:20118**（バック API）

---

### Linux

#### 1. 依存関係のインストール

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

#### 2. ビルド

```bash
chmod +x build-unix.sh
./build-unix.sh
```

#### 3. 実行

```bash
chmod +x start.sh
./start.sh
```

またはバックエンドのみ：

```bash
java -jar modelrouter.jar
```

GUI ランチャー（任意）：先に `./build-unix.sh`、続けて `./build-launcher.sh`、実行は `./launcher.sh`。

#### 4. アクセス

**http://localhost:20119**（フロント）または **http://localhost:20118**（バック API）

---

### Docker（オプション、クロスプラットフォーム）

Docker デプロイ（PostgreSQL モード）の場合：

```bash
docker-compose up -d
```

- フロントエンド：http://localhost:20119  
- バックエンド：http://localhost:20118  

制限のあるネットワークでは、プロジェクトルートに `.env` を作成：

```
DOCKER_MIRROR=docker.1ms.run/library/
```

---

## 使い方

### 基本フロー

1. **初回アクセス**：ブラウザで **http://localhost:20119** を開く（`java -jar modelrouter.jar` のみでフロントを起動していない場合は、[インストール](#インストール)に従いフロントを起動するか GUI ランチャーを使用）。案内に従い **管理者を初期化**（`POST /api/auth/init`、セッションは `/api/auth/login`）。
2. **プラットフォーム追加**：「プラットフォーム管理」で Provider を設定（OpenAI、アリババ百煉など）
3. **モデル追加**：「モデル管理」でモデルを追加、プラットフォームに紐付け
4. **API キー追加**：「API Key」で Key と Secret を入力、モデルにバインド
5. **ルート作成**：「ルート」でプライマリとバックアップモデルを設定し、**ルート用 API キー**（`/v1/chat/completions` 用）を取得
6. **テスト**：「モデルテスト」でチャットを検証

### データファイル

- **SQLite モード**：データは `./data/modelrouter.db` に保存
- 初回実行でテーブル自動作成、プラットフォームリスト初期化

---

## API

### OpenAI 互換（プロキシ）

- `POST /v1/chat/completions`（同等：`POST /api/v1/chat/completions`）
- `GET /v1/models`、`GET /v1/models/{modelId}`（`/api/v1/...` も可）

認証：ルート画面で発行した **ルート API キー**、`Authorization: Bearer <キー>`。

```bash
curl -X POST http://localhost:20118/v1/chat/completions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ROUTE_API_KEY" \
  -d '{
    "model": "gpt-3.5-turbo",
    "messages": [{"role": "user", "content": "こんにちは"}],
    "max_tokens": 256
  }'
```

### 認証（管理セッション）

- `GET /api/auth/needs-init` — 初回管理者作成が必要か  
- `POST /api/auth/init` — 初回管理者作成（管理者が一人もいないときのみ）  
- `POST /api/auth/login`、`POST /api/auth/logout`、`GET /api/auth/me`

### 管理 REST（CRUD、詳細はコントローラ参照）

- プラットフォーム：`/api/providers`
- モデル：`/api/models`
- API キー：`/api/api-keys`
- ルート：`/api/routes`
- 使用統計：`/api/usage-logs`（`statistics`、`route/{routeId}` など）
- モデルテスト：`POST /api/test/chat`

---

## 設定

| ファイル | 説明 |
|----------|------|
| `application.yml` | デフォルト設定（SQLite） |
| `application-sqlite.yml` | SQLite データソース、dialect |
| `application-docker.yml` | Docker 環境（PostgreSQL） |

デフォルトは SQLite；Docker デプロイ時は `SPRING_PROFILES_ACTIVE=docker` で PostgreSQL に切り替え。

---

## よくある質問

### Windows：`mvn` または `mvnw.cmd` が見つからない

Maven をインストールして再試行：`winget install Apache.Maven` または `choco install maven`。

### Java バージョンエラー（class file version 61.0 / 52.0）

**JDK 17+** が必要（class 61 = Java 17）。`modelrouter.jar` と `modelrouter-launcher.jar` はいずれも Java 17 向け。`java -version` で確認し、`JAVA_HOME` は JDK 17 以上を指すこと。

### Maven ダウンロード失敗（zip END header not found）

ダウンロード不完全が原因のことが多い。ローカル Maven をインストールし `mvn` でビルド。

### プラットフォームリストが空、または auth_header エラー

マイグレーションスクリプトでデータを初期化/修復：

```bash
python database/import_register_url_sqlite.py
```

または `database\import-migrate-sqlite.bat` をダブルクリック（Windows）。

---

## 開発・貢献

- バックエンド：`cd backend && mvn spring-boot:run`
- フロントエンド：`cd frontend && npm run dev`

Issue と Pull Request を歓迎します。

---

## ライセンス

本プロジェクトは [Apache License 2.0](LICENSE) の下でライセンスされています。

---

## サードパーティライセンス

直接依存関係とそのライセンス。完全な推移依存関係は `mvn dependency:tree`（バックエンド）と `npx license-checker`（フロントエンド）で監査可能。

### バックエンド (Java / Maven)

| コンポーネント | ライセンス |
|----------------|------------|
| Spring Boot | Apache-2.0 |
| Spring Data JPA | Apache-2.0 |
| SQLite JDBC (org.xerial) | Apache-2.0 |
| Hibernate Community Dialects | LGPL-2.1-or-later |
| PostgreSQL JDBC | BSD-2-Clause |
| Jackson | Apache-2.0 |
| Apache HttpClient 5 | Apache-2.0 |
| Spring WebFlux | Apache-2.0 |
| Caffeine | Apache-2.0 |

### フロントエンド (Node.js / npm)

| コンポーネント | ライセンス |
|----------------|------------|
| React / react-dom | MIT |
| react-router-dom | MIT |
| @mui/material, @mui/icons-material | MIT |
| @emotion/react, @emotion/styled | MIT |
| axios | MIT |
| chart.js, react-chartjs-2 | MIT |
| i18next, react-i18next | MIT |
| TypeScript | Apache-2.0 |
| Vite, @vitejs/plugin-react | MIT |

### GUI ランチャー（`launcher-java`）

| コンポーネント | ライセンス |
|----------------|------------|
| JDK 標準ライブラリ（Swing、`HttpServer` 等） | GPL with Classpath（Oracle/OpenJDK 配布に準拠） |
