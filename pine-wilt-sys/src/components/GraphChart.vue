<template>
  <div ref="chartRef" class="graph-container"></div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch, toRaw, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  nodes: { type: Array, default: () => [] },
  links: { type: Array, default: () => [] }
})

const emit = defineEmits(['node-click'])

const chartRef = ref(null)
let myChart = null

// 扩展配色方案，确保颜色够多，循环使用
const colorPalette = [
  '#c23531', '#2f4554', '#61a0a8', '#d48265', '#91c7ae',
  '#749f83', '#ca8622', '#bda29a', '#6e7074', '#546570',
  '#c4ccd3', '#5470c6', '#91cc75', '#fac858', '#ee6666'
]

/**
 * 核心数据处理函数
 * 1. 自动提取 Category
 * 2. 自动匹配中文名
 * 3. 自动分配颜色
 */
const processData = (rawNodes, rawLinks) => {
  if (!rawNodes || rawNodes.length === 0) return { nodes: [], links: [], categories: [] }

  // 1. 动态提取所有类别 (Set 去重)
  const categoriesSet = new Set()
  rawNodes.forEach(n => {
    // 如果没有 category，归为 '其他'
    categoriesSet.add(n.category || '其他')
  })

  // 2. 生成 ECharts 的 categories 配置数组
  // 这里使用了 index % colorPalette.length 来循环分配颜色，不用写死
  const categories = Array.from(categoriesSet).map((name, index) => ({
    name: name,
    itemStyle: {
      color: colorPalette[index % colorPalette.length]
    }
  }))

  // 3. 转换节点数据
  const nodes = rawNodes.map(node => {
    // 找到当前节点对应的类别索引
    const categoryName = node.category || '其他'
    const categoryIndex = categories.findIndex(c => c.name === categoryName)

    // 关键修复：后端传的是 cnName (驼峰)，旧代码可能是 cn_name
    // 这里做兼容处理，优先显示中文
    const displayName = node.cnName || node.cn_name || node.name || '未知节点'

    return {
      id: String(node.id), // 必须转字符串，防止 ID 匹配错误
      name: String(node.id), // ECharts 内部索引
      // 存入业务数据供 Tooltip 使用
      data: {
        ...node,
        displayName: displayName,
        categoryName: categoryName
      },
      category: categoryIndex, // 关联到上面的 categories 数组
      symbolSize: node.symbolSize || 50, // 默认大小
      draggable: true
    }
  })

  // 4. 转换边数据
  const links = rawLinks.map(link => ({
    source: String(link.source),
    target: String(link.target),
    label: {
      show: true,
      formatter: link.label || '' // 显示关系名
    },
    lineStyle: {
      curveness: 0.2, // 稍微弯曲
      color: 'source' // 线条颜色跟随源节点
    }
  }))

  return { nodes, links, categories }
}

const getOption = (nodes, links, categories) => ({
  // 悬停提示
  tooltip: {
    show: true,
    trigger: 'item',
    enterable: true, // 允许鼠标进入提示框
    formatter: (params) => {
      // 节点的 Tooltip
      if (params.dataType === 'node') {
        const d = params.data.data
        return `
          <div style="font-size:14px; font-weight:bold; margin-bottom:5px;">${d.displayName}</div>
          <span style="color:#888">英文:</span> ${d.name}<br/>
          <span style="color:#888">类型:</span> ${d.categoryName}<br/>
          ${d.description ? `<div style="margin-top:5px; max-width:250px; white-space:pre-wrap; font-size:12px; color:#666; line-height:1.4;">${d.description}</div>` : ''}
        `
      }
      // 连线的 Tooltip
      if (params.dataType === 'edge') {
        return `${params.name || '关系'} <br/> 类型: ${params.data.label.formatter}`
      }
      return ''
    }
  },
  // 图例 (自动根据提取出的 categories 生成)
  legend: {
    data: categories.map(c => c.name),
    bottom: 0,
    type: 'scroll', // 如果分类太多，允许滚动
    textStyle: { color: '#666' }
  },
  // 动画配置
  animationDurationUpdate: 1500,
  animationEasingUpdate: 'quinticInOut',
  series: [
    {
      type: 'graph',
      layout: 'force',
      data: nodes,
      links: links,
      categories: categories,
      roam: true, // 允许缩放平移
      label: {
        show: true,
        position: 'bottom', // 放在底部防止遮挡图标
        formatter: (params) => {
          // 显示处理好的中文名
          return params.data.data.displayName
        },
        fontSize: 12,
        color: '#333'
      },
      // 高亮样式
      emphasis: {
        focus: 'adjacency', // 聚焦邻居
        lineStyle: { width: 4 }
      },
      // 力引导布局参数
      force: {
        repulsion: 400, // 斥力
        edgeLength: [100, 250], // 连线长度范围
        gravity: 0.05 // 引力
      },
      lineStyle: {
        width: 2,
        curveness: 0.2
      }
    }
  ]
})

const initChart = () => {
  if (chartRef.value) {
    // 销毁旧实例防止内存泄漏或 ghost effect
    if (myChart) myChart.dispose()
    myChart = echarts.init(chartRef.value)

    myChart.on('click', (params) => {
      if (params.dataType === 'node') {
        emit('node-click', params.data.data)
      }
    })
  }
}

// 渲染图表的主逻辑
const renderChart = () => {
  if (!myChart) initChart()

  const rawNodes = toRaw(props.nodes)
  const rawLinks = toRaw(props.links)

  // 数据为空时清空画布
  if (!rawNodes || rawNodes.length === 0) {
    myChart.clear()
    return
  }

  const { nodes, links, categories } = processData(rawNodes, rawLinks)
  const option = getOption(nodes, links, categories)

  // setOption(option, notMerge)
  // 第二个参数 true 表示不合并，完全重绘，避免旧数据残留
  myChart.setOption(option, true)
}

const handleResize = () => {
  myChart && myChart.resize()
}

// 深度监听数据变化
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
  min-height: 400px;
  background-color: #ffffff;
  border-radius: 8px;
  overflow: hidden; /* 防止溢出 */
}
</style>