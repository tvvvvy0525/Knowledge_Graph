<template>
  <div class="home-container">
    <div class="header">
      <h2>松材线虫病知识图谱系统</h2>
      <el-button type="primary" :loading="store.loading" @click="initGraph">
        重置图谱
      </el-button>
    </div>
    
    <div class="content">
      <!-- 左侧：图谱区域 -->
      <div class="chart-wrapper">
        <GraphChart 
          :nodes="store.nodes" 
          :links="store.links" 
          @node-click="handleNodeClick"
        />
      </div>
      
      <!-- 右侧：详情面板组件 -->
      <!-- 将当前选中的节点通过 props 传给子组件 -->
      <InfoPanel :current-node="currentNode" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useGraphStore } from '../stores/graphStore'
import GraphChart from '../components/GraphChart.vue'
// 引入新拆分的组件
import InfoPanel from '../components/InfoPanel.vue'

const store = useGraphStore()
const currentNode = ref(null)

const initGraph = () => {
  store.fetchInitGraph()
  currentNode.value = null
}

const handleNodeClick = async (nodeData) => {
  currentNode.value = nodeData
  await store.toggleNode(nodeData.id)
}

onMounted(() => {
  initGraph()
})
</script>

<style scoped>
.home-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  padding: 0 20px;
  justify-content: space-between;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  z-index: 10;
}

.content {
  flex: 1;
  display: flex;
  overflow: hidden;
  position: relative;
}

.chart-wrapper {
  flex: 1;
  position: relative;
}
</style>