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
  <a href="#api">API</a>
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

---

## プロジェクト構成

```
modelrouter-app/
├── backend/           # Spring Boot バックエンド (Java 17)
├── frontend/          # React + Vite + MUI フロントエンド
├── database/          # 初期化・マイグレーションスクリプト
├── launcher/          # Windows ワンクリックランチャー
├── build-windows.bat  # Windows ビルドスクリプト
├── build-unix.sh      # macOS/Linux ビルドスクリプト
├── modelrouter.jar    # ビルド成果物（実行用）
└── data/
    └── modelrouter.db # SQLite データ（実行時に生成）
```

---

## 環境要件

| 環境 | 用途 |
|------|------|
| **JDK 17+** | バックエンド実行（必須） |
| **Node.js 18+** | フロントエンドビルド（ビルド時のみ） |
| **Maven 3.6+** | バックエンドビルド（推奨、または同梱 Maven Wrapper 使用） |
| **Python 3.8+** | オプション、ランチャーまたは EXE ビルド用 |

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

**方式 A：ワンクリックランチャー（推奨）**

```cmd
# 初回のみ EXE をビルド
cd launcher
pip install pyinstaller
pyinstaller --onefile --windowed --name ModelRouterLauncher launcher.py

# dist\ModelRouterLauncher.exe をプロジェクトルートにコピー（modelrouter.jar と同じ階層）
# ModelRouterLauncher.exe をダブルクリックして起動
```

**方式 B：Python スクリプト**

```cmd
python launcher\launcher.py
```

**方式 C：jar を直接実行**

```cmd
java -jar modelrouter.jar
```

#### 5. アクセス

ブラウザで **http://localhost:20118** を開く。

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
java -jar modelrouter.jar
```

または Python ランチャー：

```bash
python3 launcher/launcher.py
```

#### 4. アクセス

**http://localhost:20118**

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
java -jar modelrouter.jar
```

または：

```bash
python3 launcher/launcher.py
```

#### 4. アクセス

**http://localhost:20118**

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

1. **プラットフォーム追加**：「プラットフォーム管理」で Provider を設定（OpenAI、アリババ百煉など）
2. **モデル追加**：「モデル管理」でモデルを追加、プラットフォームに紐付け
3. **API キー追加**：「API Key」で Key と Secret を入力、モデルにバインド
4. **ルート作成**：「ルート」でプライマリとバックアップモデルを設定
5. **テスト**：「モデルテスト」でチャットを検証

### データファイル

- **SQLite モード**：データは `./data/modelrouter.db` に保存
- 初回実行でテーブル自動作成、プラットフォームリスト初期化

---

## API

### OpenAI 互換エンドポイント

ModelRouter は OpenAI 互換のチャット API を提供：

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

### 管理 API

- プラットフォーム：`GET/POST /api/providers`
- モデル：`GET/POST /api/models`
- API キー：`GET/POST /api/api-keys`
- ルート：`GET/POST /api/routes`
- 使用統計：`GET /api/usage-logs`

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

JDK 17 が必要。`java -version` で確認し、`JAVA_HOME` が JDK 17 を指していることを確認。

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

### ランチャー (Python)

| コンポーネント | 用途 | ライセンス |
|----------------|------|------------|
| tkinter | GUI | Python 標準ライブラリ（PSF License） |
| PyInstaller | EXE パッケージング（オプション） | GPL-2.0 / 商用デュアルライセンス |
