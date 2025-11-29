<template>
  <div class="home-container">
    <!-- 头部区域 -->
    <div class="header">
      <div class="brand">
        <h2>松材线虫病 KG</h2>
      </div>

      <!-- Member A1: 搜索栏 -->
      <SearchBar />

      <!-- 右侧操作区 -->
      <div class="actions">
        <!-- Member A2: 新建实体 -->
        <el-button type="primary" plain @click="nodeDialogRef.open()">
          新建实体
        </el-button>
        <!-- Member A3: 新建关系 -->
        <el-button type="success" plain @click="linkDialogRef.open()">
          新建关联
        </el-button>
        <el-button @click="initGraph">重置图谱</el-button>
      </div>
    </div>
    
    <div class="content">
      <div class="chart-wrapper">
        <GraphChart 
          :nodes="store.nodes" 
          :links="store.links" 
          @node-click="handleNodeClick"
        />
      </div>
      
      <!-- Member B: 详情/编辑面板 -->
      <InfoPanel :current-node="currentNode" />
    </div>

    <!-- 弹窗组件挂载 -->
    <NodeCreateDialog ref="nodeDialogRef" />
    <LinkCreateDialog ref="linkDialogRef" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useGraphStore } from '@/stores/graphStore'
import GraphChart from '@/components/GraphChart.vue'
import InfoPanel from '@/components/InfoPanel.vue'
import SearchBar from '@/components/SearchBar.vue'
import NodeCreateDialog from '@/components/NodeCreateDialog.vue'
import LinkCreateDialog from '@/components/LinkCreateDialog.vue'

const store = useGraphStore()
const currentNode = ref(null)

// 引用弹窗组件实例
const nodeDialogRef = ref(null)
const linkDialogRef = ref(null)

const initGraph = () => {
  store.fetchInitGraph()
  currentNode.value = null
}

const handleNodeClick = async (nodeData) => {
  currentNode.value = nodeData
  // 如果 Store 里的 deleteNodeAction 执行了，
  // 最好在这里加个 watch 监听 store.nodes 变化来自动置空 currentNode，
  // 或者在 InfoPanel 删除成功后 emit 事件出来。
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
  justify-content: space-between; /* 左右分布 */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  z-index: 10;
}

.brand h2 {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.actions {
  display: flex;
  gap: 10px;
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