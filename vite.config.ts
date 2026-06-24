import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  build: {
    outDir: 'dist',
    sourcemap: true,
    rollupOptions: {
      output: {
        manualChunks: (id) => {
          if (id.includes('node_modules')) {
            if (id.includes('react') || id.includes('react-dom')) return 'react-vendor';
            if (id.includes('recharts')) return 'chart-vendor';
            if (id.includes('@google/generative-ai')) return 'ai-vendor';
            if (id.includes('react-markdown')) return 'markdown-vendor';
            return 'vendor';
          }
        }
      }
    },
    chunkSizeWarningLimit: 1000
  },
  server: {
    port: 3000,
    open: true,
    host: true
  },
  preview: {
    port: 4173,
    open: true
  },
  optimizeDeps: {
    include: ['react', 'react-dom', 'recharts', '@google/generative-ai', 'react-markdown']
  }
});
