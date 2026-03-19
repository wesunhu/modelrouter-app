/**
 * 静态服务 + API 反向代理
 * 解决 serve 将 /api 当作 SPA 路由返回 index.html 的问题
 */
import http from 'node:http'
import { createReadStream, existsSync } from 'node:fs'
import { join, extname } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = fileURLToPath(new URL('.', import.meta.url))
const DIST = join(__dirname, 'dist')
const PROXY_TARGET = process.env.BACKEND_URL || 'http://backend:20118'

function serveFile(pathname, res) {
  const file = pathname === '/' ? join(DIST, 'index.html') : join(DIST, pathname)
  if (!existsSync(file)) {
    res.writeHead(404)
    res.end()
    return
  }
  const ext = extname(file)
  const types = { '.html': 'text/html', '.js': 'application/javascript', '.css': 'text/css', '.ico': 'image/x-icon', '.json': 'application/json', '.svg': 'image/svg+xml', '.woff2': 'font/woff2' }
  res.setHeader('Content-Type', types[ext] || 'application/octet-stream')
  createReadStream(file).pipe(res)
}

function proxy(pathname, req, res) {
  const url = new URL(pathname + (req.url.includes('?') ? req.url.slice(req.url.indexOf('?')) : ''), PROXY_TARGET)
  const headers = { ...req.headers, host: url.host }
  // GET/HEAD 不应带 Content-Type，部分后端会因此返回 400
  if (req.method === 'GET' || req.method === 'HEAD') {
    delete headers['content-type']
    delete headers['content-length']
  }
  const options = {
    hostname: url.hostname,
    port: url.port || 80,
    path: url.pathname + url.search,
    method: req.method,
    headers,
  }
  const proxyReq = http.request(options, (proxyRes) => {
    res.writeHead(proxyRes.statusCode, proxyRes.headers)
    proxyRes.pipe(res)
  })
  proxyReq.on('error', (e) => {
    res.writeHead(502)
    res.end(JSON.stringify({ error: 'Backend unreachable: ' + e.message }))
  })
  req.pipe(proxyReq)
}

const server = http.createServer((req, res) => {
  const pathname = decodeURIComponent(new URL(req.url, 'http://x').pathname)
  if (pathname.startsWith('/api') || pathname.startsWith('/v1')) {
    proxy(pathname, req, res)
    return
  }
  if (pathname !== '/' && existsSync(join(DIST, pathname))) {
    serveFile(pathname, res)
    return
  }
  serveFile('/', res)
})

server.listen(80, () => console.log('Frontend server on :80, proxy target:', PROXY_TARGET))
