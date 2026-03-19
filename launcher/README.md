# ModelRouter Windows 启动引导

一键启动 ModelRouter，**无需 Docker**，使用 SQLite 数据库。

## 功能

- 启动后端服务（SQLite 单机模式）
- 自动打开管理界面
- 点击「打开」可在浏览器中访问
- 「停止服务」关闭后端

## 地址

| 服务   | 地址                   |
|--------|------------------------|
| 管理界面 | http://localhost:20118 |

## 使用方式

### 前置条件

- **JDK 17+**（需已安装并配置 `JAVA_HOME` 或 PATH）
- 已构建 jar（运行 `build-windows.bat`）

### 方式一：直接运行 Python 脚本

```bash
cd launcher
python launcher.py
```

或在项目根目录执行：`python launcher/launcher.py`

### 方式二：构建 EXE 后运行

1. 安装 Python 3.8+（仅构建时需要）
2. 双击 `build.bat` 或执行：
   ```bash
   cd launcher
   pip install pyinstaller
   pyinstaller --onefile --windowed --name ModelRouterLauncher launcher.py
   ```
3. 将 `ModelRouterLauncher.exe` 与 `modelrouter.jar` 放在同一目录（项目根目录）
4. 双击 `ModelRouterLauncher.exe` 运行

**重要**：EXE 需与 `modelrouter.jar` 同目录，或位于包含 `modelrouter.jar` / `backend/target/*.jar` 的目录。
