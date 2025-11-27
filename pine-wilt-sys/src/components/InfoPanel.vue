<template>
    <div class="info-panel-container">
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>当前选中信息</span>
          </div>
        </template>
        
        <div v-if="currentNode">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="名称">{{ currentNode.name }}</el-descriptions-item>
            <el-descriptions-item label="类型">
              <el-tag>{{ currentNode.category }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="ID">{{ currentNode.id }}</el-descriptions-item>
            <el-descriptions-item label="描述">
              {{ currentNode.description || '暂无描述' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div v-else class="empty-text">
          <el-empty description="请点击图谱中的节点查看详情" image-size="60"></el-empty>
        </div>
      </el-card>
    </div>
  </template>
  
  <script setup>
  // 定义接收的参数
  defineProps({
    currentNode: {
      type: Object,
      default: null
    }
  })
  </script>
  
  <style scoped>
  .info-panel-container {
    width: 320px; /* 固定宽度 */
    height: 100%;
    border-left: 1px solid #dcdfe6;
    background: #fff;
    padding: 10px;
    box-shadow: -2px 0 5px rgba(0,0,0,0.05);
    z-index: 5;
    overflow-y: auto;
    box-sizing: border-box; /* 确保 padding 不会撑大宽度 */
  }
  
  .info-card {
    height: 100%;
    border: none;
    box-shadow: none !important;
  }
  
  /* 覆盖 ElementPlus description 的样式，防止内容溢出 */
  :deep(.el-descriptions__cell) {
    padding: 8px 10px !important;
  }
  
  .empty-text {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 300px;
  }
  </style>