import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/chat': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/test': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/banners': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 仅代理后端商品公开接口，避免前端路由 /products 被转发到后端导致 404
      '/products/public': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/categories': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/alipay': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
