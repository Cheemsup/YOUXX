<template>
  
  <router-view/>
</template>

<script>
import { onMounted } from 'vue'

export default {
  name: 'App',
  setup() {
    onMounted(() => {
      // 登录态已迁移至 sessionStorage（按标签页隔离，避免同浏览器多账号互相覆盖）。
      // 此处仅做一次性版本清理：清掉 localStorage 中残留的旧版登录态，标记 key 仍放 localStorage 只执行一次。
      const CLEANUP_KEY = 'dataCleanup_v4'
      const hasCleaned = localStorage.getItem(CLEANUP_KEY)

      if (!hasCleaned) {
        localStorage.removeItem('token')
        localStorage.removeItem('userId')
        localStorage.removeItem('username')
        localStorage.removeItem('userRole')

        localStorage.setItem(CLEANUP_KEY, 'true')
        console.log('数据清理完成：已清理登录状态数据')
      }
    })
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  overflow: hidden;
  height: 100vh;
  width: 100vw;
}

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  height: 100vh;
  overflow: hidden;
}

nav {
  padding: 30px;
}

nav a {
  font-weight: bold;
  color: #2c3e50;
}

nav a.router-link-exact-active {
  color: #42b983;
}
</style>
