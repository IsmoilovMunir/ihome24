import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  build: {
    rollupOptions: {
      input: {
        main: fileURLToPath(new URL('./index.html', import.meta.url)),
        products: fileURLToPath(new URL('./products/index.html', import.meta.url)),
        contacts: fileURLToPath(new URL('./support/contacts/index.html', import.meta.url)),
        oferta: fileURLToPath(new URL('./support/oferta/index.html', import.meta.url)),
        orderTracking: fileURLToPath(new URL('./order-tracking/index.html', import.meta.url)),
      },
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
