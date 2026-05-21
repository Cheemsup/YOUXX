<template>
  
  <router-view/>
</template>

<script>
import { onMounted } from 'vue'

export default {
  name: 'App',
  setup() {
    onMounted(() => {
      const CLEANUP_KEY = 'dataCleanup_v3'
      const hasCleaned = localStorage.getItem(CLEANUP_KEY)
      
      if (!hasCleaned) {
        sessionStorage.removeItem('username')
        sessionStorage.removeItem('userRole')
        
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
