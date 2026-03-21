# ModelRouter-App 配置 OpenClaw 指南

本文档说明如何将 [OpenClaw](https://docs.openclaw.ai/) 配置为使用 ModelRouter-App 作为 AI 模型后端，实现多平台路由与故障转移。

---

## 前置条件

1. **ModelRouter-App 已启动**：访问 http://localhost:20118 可打开管理界面
2. **已创建路由**：在 ModelRouter 的「路由」中至少有一个已配置主模型、API Key 的活跃路由
3. **已获取路由 API Key**：在路由详情中复制该路由的 API Key（用于鉴权）

---

## 配置步骤

### 1. 确认 OpenClaw 配置文件位置

OpenClaw 的配置文件为：

- **Linux / macOS**：`~/.openclaw/openclaw.json`
- **Windows**：`%USERPROFILE%\.openclaw\openclaw.json`

若文件不存在，可运行 `openclaw configure` 或 `openclaw onboard` 创建，或手动创建该文件。

### 2. 添加 ModelRouter 作为 Provider

在 `openclaw.json` 的 `models.providers` 中添加 `modelrouter` 配置：

```json5
{
  agents: {
    defaults: {
      model: {
        primary: "modelrouter/你的路由名称"
      }
    }
  },
  models: {
    providers: {
      modelrouter: {
        baseUrl: "http://localhost:20118/v1",
        apiKey: "你的路由API_KEY",
        api: "openai-completions",
        models: [
          { id: "你的路由名称", name: "你的路由名称" }
        ]
      }
    }
  }
}
```

### 3. 参数说明

| 参数 | 说明 | 示例 |
|------|------|------|
| `baseUrl` | ModelRouter 的 API 根地址，需包含 `/v1` | `http://localhost:20118/v1` |
| `apiKey` | 在 ModelRouter 中创建的路由的 API Key | `mr_xxx...` |
| `api` | 使用 OpenAI 兼容接口 | `openai-completions` |
| `models[].id` | 与 ModelRouter 中路由名称一致 | 路由管理中的「名称」 |
| `models[].name` | 显示名称，可与 id 相同 | 同上 |

### 4. 多路由配置示例

若在 ModelRouter 中有多个路由，可配置多个 model 供切换：

```json5
{
  agents: {
    defaults: {
      model: {
        primary: "modelrouter/main-route",
        fallback: "modelrouter/backup-route"
      }
    }
  },
  models: {
    providers: {
      modelrouter: {
        baseUrl: "http://localhost:20118/v1",
        apiKey: "主路由的API_KEY",
        api: "openai-completions",
        models: [
          { id: "main-route", name: "主路由 (Ollama)" },
          { id: "backup-route", name: "备用路由 (DeepSeek)" }
        ]
      }
    }
  }
}
```

> **注意**：不同路由使用不同 `apiKey`。若需在 OpenClaw 中切换路由，通常需为每个路由单独配置一个 provider，或使用环境变量动态切换。

### 5. 使用环境变量（推荐）

将 API Key 放在环境变量中，避免写入配置文件：

```json5
{
  models: {
    providers: {
      modelrouter: {
        baseUrl: "http://localhost:20118/v1",
        apiKey: "${MODELROUTER_API_KEY}",
        api: "openai-completions",
        models: [{ id: "my-route", name: "My Route" }]
      }
    }
  }
}
```

在启动 OpenClaw 前设置环境变量：

```bash
# Linux / macOS
export MODELROUTER_API_KEY="mr_你的路由API_KEY"

# Windows (PowerShell)
$env:MODELROUTER_API_KEY = "mr_你的路由API_KEY"
```

---

## 获取 ModelRouter 中的路由信息

1. 打开 http://localhost:20118
2. 进入「路由」页面
3. 找到要使用的路由，点击查看详情
4. 复制 **API Key** 和 **名称**（即路由名称，用作 `model` 的 id）

---

## 可选：baseUrl 使用 /api 路径

若 OpenClaw 或其它客户端要求 base URL 以 `/api` 结尾，可使用：

```json5
baseUrl: "http://localhost:20118/api/v1"
```

ModelRouter 同时支持 `/v1` 与 `/api/v1` 路径。

---

## 常见问题

### 1. `Request method 'POST' is not supported`

- **原因**：请求发到了错误路径（如根路径 `/` 或静态资源路径）
- **解决**：确认 `baseUrl` 为 `http://localhost:20118/v1` 或 `http://localhost:20118/api/v1`，不要使用 `http://localhost:20118` 或 `http://localhost:20118/`

### 2. `Invalid API key` 或 401

- **原因**：`apiKey` 与 ModelRouter 中路由的 API Key 不一致
- **解决**：在 ModelRouter 路由详情中重新复制 API Key，确保无多余空格

### 3. `The model 'xxx' does not exist`

- **原因**：`models[].id` 与 ModelRouter 中路由名称不一致
- **解决**：在 ModelRouter 中查看路由名称，将 `id` 和 `name` 改为与该名称完全一致

### 4. ModelRouter 未启动

- **现象**：连接超时或连接被拒绝
- **解决**：先启动 ModelRouter（`java -jar modelrouter.jar` 或 `start.bat` / `./start.sh`），确认 http://localhost:20118 可访问

---

## 验证配置

使用 curl 测试 ModelRouter 接口是否正常：

```bash
curl -X POST http://localhost:20118/v1/chat/completions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer 你的路由API_KEY" \
  -d '{
    "model": "你的路由名称",
    "messages": [{"role": "user", "content": "你好"}],
    "max_tokens": 50
  }'
```

若返回 JSON 且包含 `choices`，说明配置正确。

---

## 参考链接

- [OpenClaw 配置文档](https://docs.openclaw.ai/configuration)
- [OpenClaw 模型与 Provider 配置](https://docs.openclaw.ai/concepts/models)
- [ModelRouter-App README](../README.md)
