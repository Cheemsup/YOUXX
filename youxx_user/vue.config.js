const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8080,
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
