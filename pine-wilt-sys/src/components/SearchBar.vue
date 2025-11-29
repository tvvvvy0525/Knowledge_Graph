<template>
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索节点名称..."
        class="search-input"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
    </div>
  </template>
  
  <script setup>
  import { ref } from 'vue'
  import { Search } from '@element-plus/icons-vue'
  import { useGraphStore } from '@/stores/graphStore'
  
  const keyword = ref('')
  const store = useGraphStore()
  
  const handleSearch = () => {
    store.performSearch(keyword.value)
  }
  </script>
  
  <style scoped>
  .search-bar {
    width: 300px;
  }
  </style>