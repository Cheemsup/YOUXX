const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8080,
    proxy: {//代理到minimax.chat
      '/api/minimax': {
        target: 'https://api.minimax.chat',
        changeOrigin: true,
        pathRewrite: { '^/api/minimax': '' },
        secure: true
      }
    }
  }
})
