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
        '/api/auth': { target: env.VITE_AUTH_SERVICE_URL || 'http://localhost:8081', changeOrigin: true },
        '/api/v1/admin/accounts': { target: env.VITE_AUTH_SERVICE_URL || 'http://localhost:8081', changeOrigin: true },
        '/api/v1/admin/roles': { target: env.VITE_AUTH_SERVICE_URL || 'http://localhost:8081', changeOrigin: true },
        '/api/v1/admin/customers': { target: env.VITE_CUSTOMER_SERVICE_URL || 'http://localhost:8082', changeOrigin: true },
        '/api/v1/customer': { target: env.VITE_CUSTOMER_SERVICE_URL || 'http://localhost:8082', changeOrigin: true },
        '/api/v1/bookings': { target: env.VITE_CUSTOMER_SERVICE_URL || 'http://localhost:8082', changeOrigin: true },
        '/api/v1/vouchers': { target: env.VITE_CUSTOMER_SERVICE_URL || 'http://localhost:8082', changeOrigin: true },
        '/api/payments': { target: env.VITE_PAYMENT_SERVICE_URL || 'http://localhost:8080', changeOrigin: true },
        '/api/payment': { target: env.VITE_CUSTOMER_SERVICE_URL || 'http://localhost:8082', changeOrigin: true },
        '/api/v1/staff': { target: env.VITE_STAFF_SERVICE_URL || 'http://localhost:8083', changeOrigin: true },
        '/api/v1/refunds': { target: env.VITE_STAFF_SERVICE_URL || 'http://localhost:8083', changeOrigin: true },
        '/api/v1/route-stops': { target: env.VITE_STAFF_SERVICE_URL || 'http://localhost:8083', changeOrigin: true },
        '/api/v1/coach-stops': { target: env.VITE_STAFF_SERVICE_URL || 'http://localhost:8083', changeOrigin: true },
        '/api/v1/routes': { target: env.VITE_STAFF_SERVICE_URL || 'http://localhost:8083', changeOrigin: true },
        '/api/v1/trips': { target: env.VITE_STAFF_SERVICE_URL || 'http://localhost:8083', changeOrigin: true },
        '/api/v1/manager/trips': { target: env.VITE_STAFF_SERVICE_URL || 'http://localhost:8083', changeOrigin: true },
        '/api/v1/manager/cargo-types': { target: env.VITE_STAFF_SERVICE_URL || 'http://localhost:8083', changeOrigin: true },
        '/api/v1/manager/cargo-type-prices': { target: env.VITE_STAFF_SERVICE_URL || 'http://localhost:8083', changeOrigin: true },
        '/api/v1/ticket-staff/cargo-tickets': { target: env.VITE_STAFF_SERVICE_URL || 'http://localhost:8083', changeOrigin: true },
        '/api/v1/admin/staff': { target: env.VITE_STAFF_SERVICE_URL || 'http://localhost:8083', changeOrigin: true },
        '/api/v1/admin/ticket-agencies': { target: env.VITE_STAFF_SERVICE_URL || 'http://localhost:8083', changeOrigin: true },
        '/api/v2/goong': { target: env.VITE_STAFF_SERVICE_URL || 'http://localhost:8083', changeOrigin: true },
        '/api/v1/coaches': { target: env.VITE_DRIVER_SERVICE_URL || 'http://localhost:8084', changeOrigin: true },
        '/api/v1/coach-types': { target: env.VITE_DRIVER_SERVICE_URL || 'http://localhost:8084', changeOrigin: true },
      },
    },
  }
})
