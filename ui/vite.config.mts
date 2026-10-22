import { defineConfig, loadEnv } from 'vite';
import { slmUi } from './vite/src/index.ts'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, process.cwd(), '')
    return {
        server: {
            host: '0.0.0.0',
            allowedHosts: ['slm.local'],
            port: 8080,
            https: {
                key: 'certs/cert.key',
                cert: 'certs/cert.crt',
            },
            proxy: {
                '/notification-service': {
                    target: `${env.VITE_APP_NOTIFICATION_SERVICE_URL}`,
                    rewrite: (path) => path.replace(/^\/notification-service/, ''),
                    changeOrigin: true,
                    secure: false,
                    ws: true,
                    rewriteWsOrigin: true,
                },
                '/platform-management': {
                    target: `${env.VITE_APP_PLATFORM_MANAGEMENT_URL}`,
                    rewrite: (path) => path.replace(/^\/platform-management/, ''),
                    changeOrigin: true,
                    secure: false,
                },
                '/resource-management': {
                    target: `${env.VITE_APP_RESOURCE_MANAGEMENT_URL}`,
                    rewrite: (path) => path.replace(/^\/resource-management/, ''),
                    changeOrigin: true,
                    secure: false,
                },
                '/service-management': {
                    target: `${env.VITE_APP_SERVICE_MANAGEMENT_URL}`,
                    rewrite: (path) => path.replace(/^\/service-management/, ''),
                    changeOrigin: true,
                    secure: false,
                },
                '/slm-catalog': {
                    target: `${env.VITE_APP_CATALOG_SERVICE_URL}`,
                    rewrite: (path) => path.replace(/^\/slm-catalog/, ''),
                    changeOrigin: true,
                    secure: false,
                },
            },
        },
        plugins: slmUi(),
    }
});