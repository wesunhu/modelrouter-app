/**
 * Axios HTTP client with base URL and session cookie handling; input: API paths; output: JSON.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

import axios from 'axios'

// 空或 'PROXY' 表示使用同源（由 server.js 代理）；未设置时用直连后端
const envUrl = import.meta.env.VITE_API_URL as string | undefined
const API_BASE = envUrl === '' || envUrl === 'PROXY' ? '' : (envUrl || 'http://localhost:20118')

export const api = axios.create({
  baseURL: API_BASE,
  headers: { Accept: 'application/json' },
  // 与后端 Session 登录一致；后端 CORS 使用 localhost:* 等具体 Origin 模式（非 *）
  withCredentials: true,
})

// GET/HEAD 等无 body 请求不发送 Content-Type，避免部分后端返回 400
api.interceptors.request.use((config) => {
  const hasBody = config.method && ['post', 'put', 'patch'].includes(config.method.toLowerCase())
  if (hasBody) {
    config.headers['Content-Type'] = 'application/json'
  }
  return config
})

// 统一解析错误响应，便于显示后端返回的 message
api.interceptors.response.use(
  (r) => r,
  (err) => {
    const data = err.response?.data
    const msg = typeof data === 'object' && data && 'error' in data ? String(data.error) : err.message
    err.parsedMessage = msg
    return Promise.reject(err)
  }
)

export interface Provider {
  id: number
  name: string
  baseUrl: string
  apiType: string
  authHeader: boolean
  apiKey?: string
  registerUrl?: string
}

export interface Model {
  id: number
  name: string
  modelId: string
  modelType: string
  contextWindow: number
  maxTokens: number
  costInput: number
  costOutput: number
  tokenCost?: number
  status: string
  provider: Provider
}

export interface ApiKey {
  id: number
  key: string
  platform: string
  authType: string
  apiEndpoint?: string
  secret?: string
  quota?: number
  status: string
  modelIds?: number[]
}

export interface Route {
  id: number
  name: string
  apiKey: string
  primaryModelId?: number
  modelType: string
  timeout: number
  strategy: string
  tokenSellingPrice?: number
  status: string
  backupModelIds?: number[]
}

export interface UsageLog {
  id: number
  routeId?: number
  routeName?: string
  modelId?: number
  modelName?: string
  platform?: string
  promptTokens?: number
  completionTokens?: number
  totalTokens?: number
  cost?: number
  createdAt?: string
}
