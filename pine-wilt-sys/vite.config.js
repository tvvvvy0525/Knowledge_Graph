import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  
  // 1. 关键补充：配置路径别名 @
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },

  server: {
    // 前端开发服务器端口（可选，默认5173）
    port: 3000, 
    
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 你的后端地址
        changeOrigin: true,
        
        // 2. 关键点：不要开启 rewrite！
        // 你的后端 Controller 写的是 @RequestMapping("/api/graph")
        // 所以后端是需要收到 "/api" 这个前缀的。
        // 如果你取消注释下面这行，后端收到的就是 "/graph/init"，会报 404。
        // rewrite: (path) => path.replace(/^\/api/, '') 
      }
    }
  }
})