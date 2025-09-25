import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  base: '/cardapio/',
  server: {
    port: 8003,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://192.168.2.218:8081',
        changeOrigin: true,
      }
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
  }
})
