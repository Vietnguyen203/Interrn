import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Chuyển tất cả các API calls về Gateway chạy ở cổng 8080
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/order': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/catalog': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/payment': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
