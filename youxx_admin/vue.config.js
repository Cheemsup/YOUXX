const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    // 管理端独立部署端口，区别于用户端 8080
    port: 8079,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      // 商品图片等静态资源由后端 /upload_resources/** 提供，开发期代理到后端
      '/upload_resources': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      }
    }
  }
})
