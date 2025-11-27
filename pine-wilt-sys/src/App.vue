<template>
  <!-- 直接渲染我们的主页面 -->
  <HomeView />
</template>

<script setup>
// 引入我们在 views 目录下创建的主页组件
import HomeView from './views/HomeView.vue'
</script>

<style>
/* 确保 App 占满全屏 */
#app {
  width: 100vw;
  height: 100vh;
  margin: 0;
  padding: 0;
  overflow: hidden;
}
</style>