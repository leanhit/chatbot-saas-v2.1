const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 3003,
    host: '0.0.0.0',
    allowedHosts: 'all',
    historyApiFallback: true,
    hot: true,
    liveReload: true,
    client: {
      webSocketURL: 'auto://0.0.0.0:0/ws',
      overlay: {
        errors: true,
        warnings: false
      }
    },
    watchFiles: {
      paths: ['src/**/*'],
      options: {
        usePolling: false,
        ignored: /node_modules/
      }
    },
    proxy: {
      '/files': {
        target: 'http://cwsv.truyenthongviet.vn:9000',
        changeOrigin: true,
        secure: false,
        pathRewrite: {
          '^/files': ''
        }
      }
    }
  },
  configureWebpack: {
    resolve: {
      alias: {
        '@': require('path').resolve(__dirname, 'src'),
      }
    }
  },
  // Add environment variables support
  chainWebpack: config => {
    config.plugin('define').tap(args => {
      const [definitions] = args
      // Ensure process.env exists
      if (!definitions['process.env']) {
        definitions['process.env'] = {}
      }
      
      // Fix double slash issue - remove trailing slash from API URL
      const apiUrl = process.env.VITE_API_URL || 'http://localhost:8080/api'
      definitions['process.env'].VITE_API_URL = JSON.stringify(apiUrl.replace(/\/$/, ''))
      
      definitions['process.env'].VITE_WS_URL = JSON.stringify(process.env.VITE_WS_URL || 'ws://localhost:8080/ws/takeover')
      definitions['process.env'].FACEBOOK_APP_ID = JSON.stringify(process.env.FACEBOOK_APP_ID || '')
      definitions['process.env'].FACEBOOK_APP_SECRET = JSON.stringify(process.env.FACEBOOK_APP_SECRET || '')
      definitions['process.env'].FACEBOOK_CONFIG_ID = JSON.stringify(process.env.FACEBOOK_CONFIG_ID || '')
      
      return args
    })
  }
})
