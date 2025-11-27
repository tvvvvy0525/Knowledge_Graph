<template>
  <div ref="chartRef" class="graph-container"></div>
</template>

<script setup>
import { onMounted, ref, watch, toRaw } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  nodes: { type: Array, default: () => [] },
  links: { type: Array, default: () => [] }
})

const emit = defineEmits(['node-click']) // 向父组件发射点击事件

const chartRef = ref(null)
let myChart = null

// 配置项模板
const getOption = (nodes, links) => ({
  tooltip: {
    show: true,
    formatter: (params) => {
      if (params.dataType === 'node') {
        return `<strong>${params.data.name}</strong><br/>类型: ${params.data.category}`
      }
      return `${params.data.name || '关联'}`
    }
  },
  legend: {
    data: ['病害', '昆虫', '植物', '环境'] // 根据你的 Category 调整
  },
  series: [
    {
      type: 'graph',
      layout: 'force',
      data: nodes,
      links: links,
      categories: [
        { name: '病害', itemStyle: { color: '#5470c6' } },
        { name: '昆虫', itemStyle: { color: '#ee6666' } },
        { name: '植物', itemStyle: { color: '#91cc75' } },
        { name: '环境', itemStyle: { color: '#73c0de' } }
      ],
      roam: true, // 支持缩放平移
      label: {
        show: true,
        position: 'right',
        formatter: '{b}'
      },
      lineStyle: {
        color: 'source',
        curveness: 0.3
      },
      force: {
        repulsion: 300,
        edgeLength: 100
      }
    }
  ]
})

const initChart = () => {
  if (chartRef.value) {
    myChart = echarts.init(chartRef.value)
    myChart.on('click', (params) => {
      if (params.dataType === 'node') {
        // 点击节点，发射事件给父组件处理（展开逻辑）
        emit('node-click', params.data)
      }
    })
  }
}

// 监听数据变化，刷新图表
watch(() => [props.nodes, props.links], () => {
  if (myChart) {
    // 必须使用 toRaw 避免 Vue 代理对象导致的 ECharts 性能问题
    const option = getOption(toRaw(props.nodes), toRaw(props.links))
    myChart.setOption(option)
  }
}, { deep: true })

onMounted(() => {
  initChart()
})
</script>

<style scoped>
.graph-container {
  width: 100%;
  height: 100%;
  background-color: #f5f7fa;
}
</style>