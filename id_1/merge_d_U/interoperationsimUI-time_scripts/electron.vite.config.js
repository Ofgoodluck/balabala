import {resolve} from 'path'
import {defineConfig, externalizeDepsPlugin} from 'electron-vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
    main: {
        build: {
            lib: {
                entry: ['app/index.js', 'src/components/Util/GetSubscribedData.mjs']
            }
        },
        plugins: [externalizeDepsPlugin()]
    },
    preload: {
        build: {
            lib: {
                entry: 'app/preload.js'
            }
        },
        plugins: [externalizeDepsPlugin()]
    },
    renderer: {
        build: {
            rollupOptions: {
                input: './index.html'
            }
        },
        root: './',
        resolve: {
            alias: {
                '@': resolve('/src')
            }
        },
        plugins: [vue()]
    }
})
