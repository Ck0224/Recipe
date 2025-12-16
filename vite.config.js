import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  // 路径别名（方便导入）
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  // 开发服务器配置（解决跨域，可选）
  server: {
    port: 5173, // 前端端口
    open: true, // 启动后自动打开浏览器
    proxy: {
      // 代理后端接口（替代CORS）
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
