import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  base: '/',
  plugins: [react()],
  server: {
    port: 20119,
    proxy: {
      '/api': { target: 'http://localhost:20118', changeOrigin: true },
      '/v1': { target: 'http://localhost:20118', changeOrigin: true },
    },
  },
})
