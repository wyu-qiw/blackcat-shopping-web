import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Vite 开发服务器配置：前端 5173 与后端 8080 使用同一套 Vue 页面
// host: true 同时支持 localhost 与 127.0.0.1 访问；/api 与 /uploads 代理到后端
export default defineConfig({
  plugins: [vue()],
  server: {
    host: true,
    open: false,
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      }
    }
  }
})
