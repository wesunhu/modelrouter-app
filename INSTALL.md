# ModelRouter 安装手册

本文档详细介绍在 Windows、macOS、Linux 上安装与运行 ModelRouter 的步骤。

---

## 一、系统要求

| 环境 | 要求 |
|------|------|
| **操作系统** | Windows 10/11、macOS 10.15+、主流 Linux 发行版 |
| **JDK** | JDK 17 及以上（**运行必需**） |
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
   - `ModelRouterLauncher.exe`（启动器）
   - `modelrouter.jar`（后端程序）
   - `data`（数据目录，首次运行自动初始化）
   - `使用说明.txt`

3. **启动**  
   双击 `ModelRouterLauncher.exe`。  
   - 首次启动约 5 秒后会自动打开浏览器  
   - 管理界面地址：http://localhost:20118  
   - 启动器窗口会显示运行日志，可保留或最小化

4. **停止**  
   在启动器窗口点击「停止服务」，或直接关闭窗口（会提示是否停止服务）。

### 3.2 方式二：从源码构建并运行

适用场景：已克隆项目源码，需要自行构建。

**前置要求：** Node.js 18+、Maven 3.6+（或使用项目自带的 Maven Wrapper）

1. **构建项目**
   ```cmd
   build-windows.bat
   ```
   脚本会完成前端构建、后端打包，并生成 `modelrouter.jar`。

2. **构建启动器（可选）**
   ```cmd
   cd launcher
   pip install pyinstaller
   pyinstaller --onefile --windowed --name ModelRouterLauncher launcher.py
   ```
   将 `dist\ModelRouterLauncher.exe` 复制到项目根目录。

3. **运行**
   - 推荐：双击 `ModelRouterLauncher.exe`
   - 或：`python launcher\launcher.py`
   - 或：`java -jar modelrouter.jar`

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

或使用 Python 启动器：

```bash
python3 launcher/launcher.py
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

1. **打开管理界面**  
   浏览器访问 http://localhost:20118  

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
| **管理端口** | 20118（http） |
| **API 接口** | http://localhost:20118/v1/chat/completions |

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
