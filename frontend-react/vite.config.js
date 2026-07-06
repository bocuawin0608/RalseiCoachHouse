import { defineConfig, loadEnv } from 'vite'
import react, { reactCompilerPreset } from '@vitejs/plugin-react'
import babel from '@rolldown/plugin-babel'
import basicSsl from '@vitejs/plugin-basic-ssl'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  return {
    // The customer frontend always reaches Spring through the same-origin
    // proxy. This prevents shell-level VITE_API_BASE_URL values from leaking a
    // localhost address into browsers opened through a deployment tunnel.
    define: {
      'import.meta.env.VITE_API_BASE_URL': JSON.stringify('/api'),
    },
    plugins: [
      react(),
      babel({ presets: [reactCompilerPreset()] }),
      basicSsl(),
    ],
    server: {
      host: '0.0.0.0',
      allowedHosts: ['.trycloudflare.com'],
      port: 3000,
      strictPort: true,
      https: true,
      proxy: {
        // Keep browser requests on the frontend origin while Vite forwards API
        // and SSE traffic to the local Spring Boot process.
        '/api': {
          target: env.VITE_API_PROXY_TARGET || 'https://localhost:9090',
          changeOrigin: true,
          secure: false,
        },
      },
    },
  }
})
