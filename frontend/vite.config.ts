import { fileURLToPath, URL } from 'node:url';

import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';
import vueJsx from '@vitejs/plugin-vue-jsx';

import { resolve, dirname } from 'node:path';
import VueI18nPlugin from '@intlify/unplugin-vue-i18n/vite';

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
    // Load env file based on `mode` in the current working directory.
    // Set the third parameter to '' to load all env regardless of the `VITE_` prefix.
    const env = loadEnv(mode, process.cwd(), '');
    
    return {
        plugins: [
            vue(),
            vueJsx(),
            VueI18nPlugin({
                include: resolve(
                    dirname(fileURLToPath(import.meta.url)),
                    './src/i18n/locales/**'
                ), // provide a path to the folder where you'll store translation data (see below)
            }),
        ],
        resolve: {
            alias: {
                '@': fileURLToPath(new URL('./src', import.meta.url)),
            },
        },
        server: {
            port: 3003, // 🔥 Đổi cổng thành 3003
            strictPort: true, // Nếu cổng bị chiếm, không tự động đổi
            host: '0.0.0.0', // Cho phép truy cập từ mạng LAN
            proxy: {
                '/api': {
                    target: env.VITE_API_BASE_URL || 'http://localhost:8081',
                    changeOrigin: true
                }
            },
            watch: {
                ignored: ['**/node_modules/**', '**/dist/**']
            }
        },
    };
});
