// src/api/graph.js

const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms))

// 1. 初始数据：只留一个“松材线虫病”
const MOCK_INIT_DATA = {
  nodes: [
    { id: 'root', name: '松材线虫病', category: '病害', symbolSize: 50, description: '一种毁灭性的松树病害。' }
  ],
  links: [] // 初始没有连线
}

// 2. 邻居数据：定义点击后加载什么
const MOCK_NEIGHBORS = {
  'root': { // 点击"松材线虫病" -> 展开：松墨天牛、马尾松、高温干旱
    nodes: [
      { id: 'insect1', name: '松墨天牛', category: '昆虫', symbolSize: 30, description: '主要的传播媒介昆虫。' },
      { id: 'tree1', name: '马尾松', category: '植物', symbolSize: 20 },
      { id: 'env1', name: '高温干旱', category: '环境', symbolSize: 20 }
    ],
    links: [
      { source: 'insect1', target: 'root', name: '传播媒介' }, // 昆虫 -> 病
      { source: 'root', target: 'tree1', name: '感染' },     // 病 -> 树
      { source: 'env1', target: 'root', name: '诱发' }       // 环境 -> 病
    ]
  },
  'insect1': { // 点击"松墨天牛" -> 展开：生活史
    nodes: [
      { id: 'cycle1', name: '1年1代', category: '环境', symbolSize: 15 }
    ],
    links: [
      { source: 'insect1', target: 'cycle1', name: '生活史' }
    ]
  }
}

export async function getInitGraph() {
  console.log('[Mock API] Fetching Init Graph...')
  await delay(500)
  return { code: 200, data: MOCK_INIT_DATA }
}

export async function getNeighbors(nodeId) {
  console.log(`[Mock API] Fetching neighbors for ${nodeId}...`)
  await delay(300)
  // 如果Mock数据里有这个节点的邻居，就返回；否则返回空
  const result = MOCK_NEIGHBORS[nodeId] || { nodes: [], links: [] }
  return { code: 200, data: result }
}