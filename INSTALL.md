# ModelRouter 安装手册

本文档详细介绍在 Windows、macOS、Linux 上安装与运行 ModelRouter 的步骤。

---

## 一、系统要求

| 环境 | 要求 |
|------|------|
| **操作系统** | Windows 10/11、macOS 10.15+、主流 Linux 发行版 |
| **JDK** | JDK 17 及以上（**运行后端与 GUI 启动器必需**） |
| **Node.js** | 18+（**构建前端**及使用 `start.bat` / `start.sh` 时必需；仅用 **GUI 启动器**且已具备 `frontend/dist` 时运行期可不装） |
| **Maven** | 3.6+（可选；可用 `backend/mvnw` 代替系统 Maven） |
| **内存** | 建议 512MB 可用内存 |
| **磁盘** | 约 100MB（含数据目录） |

> 注意：ModelRouter 使用 SQLite 单机模式，无需安装 PostgreSQL、Docker 等额外组件。

---

## 二、安装 JDK 17

运行 ModelRouter 前必须先安装 JDK 17。

### 2.1 Windows

**方式 A：winget（推荐）**

```powershell
winget install Microsoft.OpenJDK.17
```

**方式 B：Chocolatey**

```powershell
choco install temurin17
```

**方式 C：手动安装**

1. 访问 [Adoptium](https://adoptium.net/) 或 [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)
2. 下载 Windows x64 安装包
3. 安装后，将 JDK 的 `bin` 目录加入系统 PATH，或设置 `JAVA_HOME`

**验证安装：**

```cmd
java -version
```

输出应包含 `version "17.x.x"` 或更高。

### 2.2 macOS

```bash
brew install openjdk@17
```

配置 PATH（如使用 zsh）：

```bash
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

### 2.3 Linux

**Ubuntu / Debian：**

```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

**CentOS / RHEL / Fedora：**

```bash
sudo dnf install java-17-openjdk
```

**验证：**

```bash
java -version
```

---

## 三、Windows 安装

### 3.1 方式一：使用安装包（推荐）

适用场景：已下载 `ModelRouter-x.x.x-preview.x-Windows.zip` 安装包。

1. **解压**  
   将 ZIP 解压到任意目录（如 `C:\ModelRouter`）。

2. **目录结构**  
   解压后应包含：
   - `start.bat`（启动脚本）
   - `modelrouter.jar`（后端程序）
   - `frontend/dist`（前端构建产物）
   - `data`（数据目录，首次运行自动初始化）
   - `使用说明.txt`

3. **启动**  
   双击 `start.bat`（需已安装 Node.js 和 JDK 17+）。  
   - 将打开两个窗口：前端(20119)、后端(20118)  
   - **Web 管理界面**：http://localhost:20119  

   若使用 **`modelrouter-launcher.jar`**（GUI 启动器），一般只需 JDK 17+，无需 Node；解压包内需含 `modelrouter-launcher.jar` 与构建好的 `frontend/dist`（详见主仓库 [README.md](README.md)）。

4. **停止**  
   关闭前端/后端窗口即可。

### 3.2 方式二：从源码构建并运行

适用场景：已克隆项目源码，需要自行构建。

**前置要求：** Node.js 18+、Maven 3.6+（或使用项目自带的 Maven Wrapper）

1. **构建项目**
   ```cmd
   build-windows.bat
   ```
   脚本会完成前端构建、后端打包，并生成 `modelrouter.jar`。

2. **运行**
   - 推荐：双击 `start.bat`（默认前端 20119，后端 20118；可传参：`start.bat 20119 20118`）
   - 或：`java -jar modelrouter.jar`（仅后端；此时需另启前端或使用内嵌静态页策略，见主 README）
   - 可选：`java -jar modelrouter-launcher.jar`（GUI 启动器，需先 `build-launcher.bat` 构建）

### 3.3 方式三：仅运行 jar

若已有 `modelrouter.jar`，可直接执行：

```cmd
java -jar modelrouter.jar
```

启动后访问 http://localhost:20118 。

---

## 四、macOS 安装

### 4.1 使用安装包

若提供 macOS 安装包，解压后执行：

```bash
java -jar modelrouter.jar
```

或使用启动脚本：

```bash
chmod +x start.sh
./start.sh
```

### 4.2 从源码构建

1. **安装依赖**
   ```bash
   brew install openjdk@17 maven node
   ```

2. **构建**
   ```bash
   chmod +x build-unix.sh
   ./build-unix.sh
   ```

3. **运行**
   ```bash
   java -jar modelrouter.jar
   ```

4. **访问**  
   http://localhost:20118

---

## 五、Linux 安装

### 5.1 从源码构建

1. **安装依赖**
   - Ubuntu/Debian：`sudo apt install openjdk-17-jdk maven nodejs npm`
   - CentOS/RHEL：`sudo dnf install java-17-openjdk maven nodejs npm`

2. **构建**
   ```bash
   chmod +x build-unix.sh
   ./build-unix.sh
   ```

3. **运行**
   ```bash
   java -jar modelrouter.jar
   ```

4. **访问**  
   http://localhost:20118

### 5.2 后台运行（可选）

使用 nohup：

```bash
nohup java -jar modelrouter.jar > modelrouter.log 2>&1 &
```

或使用 systemd 创建服务（需自行编写 unit 文件）。

---

## 六、首次配置

1. **打开 Web 管理界面**  
   在已启动**前端**的前提下，浏览器访问 **http://localhost:20119**（与 `start.bat` / GUI 启动器默认一致）。首次使用按页面提示 **创建管理员账号**。  
   若仅运行 `java -jar modelrouter.jar` 且未启动前端，请先按主文档启动静态前端或 GUI 启动器，否则无法使用完整管理 UI。

2. **添加平台**  
   在「平台管理」中添加 Provider（如 OpenAI、阿里云百炼、智谱等）。

3. **添加模型**  
   在「模型管理」中为各平台添加可用模型。

4. **添加 API Key**  
   在「API Key」中填写 Key 与 Secret，并绑定到对应模型。

5. **创建路由**  
   在「路由」中设置主模型与备用模型，获取 API Key 供客户端使用。

6. **测试**  
   在「模型测试」中验证对话是否正常。

---

## 七、数据与端口

| 项目 | 说明 |
|------|------|
| **数据库** | SQLite，文件位于 `data/modelrouter.db` |
| **后端 API** | 默认 **20118**，例如 `http://localhost:20118/v1/chat/completions` |
| **Web 管理界面** | 默认 **20119**（`start.bat` / GUI 启动器提供的前端静态页） |

备份数据时，复制 `data` 目录即可。

---

## 八、常见问题

### 8.1 提示「未找到 Java」

- 确认已安装 JDK 17+
- 在终端执行 `java -version` 检查
- Windows 需将 JDK 的 `bin` 加入 PATH，或正确设置 `JAVA_HOME`

### 8.2 启动后无法访问

- 检查防火墙是否拦截 20118 端口
- 确认无其他程序占用 20118
- 查看启动器或终端的错误日志

### 8.3 端口被占用

修改 `application-sqlite.yml` 或启动参数：

```cmd
java -jar modelrouter.jar --server.port=20119
```

### 8.4 数据迁移

若从其他环境迁移数据，将 `data/modelrouter.db` 复制到新环境的 `data` 目录即可。

---

## 九、升级

1. 停止当前运行的 ModelRouter  
2. 使用新版本覆盖 `modelrouter.jar`（或使用新安装包覆盖）  
3. 保留原 `data` 目录，重新启动  
4. 首次启动会自动执行数据库迁移（若有）

---

## 十、卸载

1. 停止 ModelRouter  
2. 删除安装目录  
3. 如需彻底清除，可删除 `data` 目录以移除所有配置与数据

---

## 十一、获取帮助

- **项目地址**：https://github.com/modelrouter/modelrouter-app  
- **问题反馈**：提交 GitHub Issue
