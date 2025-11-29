<template>
  <div ref="chartRef" class="graph-container"></div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch, toRaw, nextTick } from 'vue'
import * as echarts from 'echarts'

// 接收父组件处理好的扁平化 nodes 和 links
// nodes 结构: [{ id, name, cn_name, category, description, ... }]
// links 结构: [{ source: id, target: id, label: '关系名' }]
const props = defineProps({
  nodes: { type: Array, default: () => [] },
  links: { type: Array, default: () => [] }
})

const emit = defineEmits(['node-click'])

const chartRef = ref(null)
let myChart = null

// 预定义一套好看的配色方案
const colorPalette = [
  '#5470c6', '#91cc75', '#fac858', '#ee6666', 
  '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc'
]

/**
 * 将业务数据转换为 ECharts 需要的格式
 */
const processData = (rawNodes, rawLinks) => {
  // 1. 提取所有类别并去重
  const categoriesSet = new Set(rawNodes.map(n => n.category || '其他'))
  const categories = Array.from(categoriesSet).map(c => ({ name: c }))

  // 2. 转换节点数据
  const nodes = rawNodes.map(node => {
    // 寻找该节点类别在 categories 数组中的索引，用于自动匹配颜色
    const categoryIndex = categories.findIndex(c => c.name === (node.category || '其他'))
    
    return {
      id: String(node.id), // ECharts 建议 ID 为字符串
      name: String(node.id), // 唯一标识，用于连线 source/target 匹配
      // 自定义数据，方便在 Tooltip 和 Label 中使用
      data: {
        ...node,
        displayName: node.cn_name || node.name // 优先显示中文
      },
      category: categoryIndex, // 对应 categories 数组的索引
      symbolSize: node.symbolSize || 40, // 允许父组件控制大小，默认40
      draggable: true
    }
  })

  // 3. 转换边数据
  const links = rawLinks.map(link => ({
    source: String(link.source),
    target: String(link.target),
    label: {
      show: true,
      formatter: link.label || '' // 显示关系名
    },
    lineStyle: {
      curveness: 0.2 //稍微弯曲，避免重叠
    }
  }))

  return { nodes, links, categories }
}

const getOption = (nodes, links, categories) => ({
  // 提示框
  tooltip: {
    show: true,
    trigger: 'item',
    formatter: (params) => {
      if (params.dataType === 'node') {
        const d = params.data.data
        return `
          <div style="font-weight:bold; margin-bottom:5px;">${d.cn_name || d.name}</div>
          <span style="color:#888">英文名:</span> ${d.name}<br/>
          <span style="color:#888">类型:</span> ${d.category}<br/>
          ${d.description ? `<div style="margin-top:5px; max-width:200px; white-space:normal; font-size:12px; color:#666">${d.description}</div>` : ''}
        `
      }
      // 边的 Tooltip
      if (params.dataType === 'edge') {
        return `${params.name || '关系'} <br/> 类型: ${params.data.label.formatter}`
      }
      return ''
    }
  },
  // 图例
  legend: {
    data: categories.map(c => c.name),
    bottom: 10,
    textStyle: { color: '#666' }
  },
  // 动画配置
  animationDurationUpdate: 1500,
  animationEasingUpdate: 'quinticInOut',
  series: [
    {
      type: 'graph',
      layout: 'force', // 力引导布局
      data: nodes,
      links: links,
      categories: categories,
      roam: true, // 开启缩放和平移
      
      // 节点标签配置
      label: {
        show: true,
        position: 'right',
        formatter: (params) => {
          return params.data.data.displayName // 显示处理过的中文名
        },
        fontSize: 12,
        color: '#333'
      },
      
      // 边的全局样式
      lineStyle: {
        color: 'source',
        width: 1.5,
        curveness: 0.3
      },
      
      // 高亮样式 (鼠标悬停时)
      emphasis: {
        focus: 'adjacency', // 只高亮邻居
        lineStyle: {
          width: 4
        }
      },
      
      // 力引导布局配置
      force: {
        repulsion: 500,  // 斥力，越大节点分得越开
        edgeLength: [80, 200], // 边的长度范围
        gravity: 0.1     // 引力，越大越往中心靠
      }
    }
  ]
})

const initChart = () => {
  if (chartRef.value) {
    myChart = echarts.init(chartRef.value)
    
    // 绑定点击事件
    myChart.on('click', (params) => {
      if (params.dataType === 'node') {
        // 将原始业务数据传回给父组件
        emit('node-click', params.data.data)
      }
    })
  }
}

// 渲染或更新图表
const renderChart = () => {
  if (!myChart) initChart()
  
  const rawNodes = toRaw(props.nodes)
  const rawLinks = toRaw(props.links)
  
  if (rawNodes.length === 0) {
    myChart.clear()
    return
  }

  const { nodes, links, categories } = processData(rawNodes, rawLinks)
  const option = getOption(nodes, links, categories)
  
  myChart.setOption(option)
}

// 监听窗口大小变化
const handleResize = () => {
  myChart && myChart.resize()
}

// 监听数据变化
watch(
  () => [props.nodes, props.links],
  () => {
    nextTick(() => {
      renderChart()
    })
  },
  { deep: true }
)

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (myChart) {
    myChart.dispose()
  }
})
</script>

<style scoped>
.graph-container {
  width: 100%;
  height: 100%;
  min-height: 400px; /* 给个最小高度防止塌陷 */
  background-color: #ffffff; /* 纯白或浅灰背景 */
  border-radius: 8px;
}
</style>