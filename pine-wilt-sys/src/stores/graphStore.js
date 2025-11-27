import { defineStore } from 'pinia'
import { ref } from 'vue'
// 引入我们在上一步创建的 Mock API
import { getInitGraph, getNeighbors } from '../api/graph'

export const useGraphStore = defineStore('graph', () => {
  // === State ===
  const nodes = ref([])
  const links = ref([])
  const loading = ref(false)
  // 用于记录哪些节点目前处于"展开"状态，以便再次点击时执行"收起"
  const expandedNodeIds = ref(new Set())

  // === Helpers ===
  // 检查节点是否存在
  const nodeExists = (id) => nodes.value.some(n => n.id === id)
  // 检查边是否存在 (无向图逻辑，A->B 和 B->A 视为同一条边)
  const linkExists = (s, t) => links.value.some(l => 
    (l.source === s && l.target === t) || (l.source === t && l.target === s)
  )

  // === Actions ===

  // 1. 初始化图谱
  async function fetchInitGraph() {
    loading.value = true
    try {
      const res = await getInitGraph()
      if (res.code === 200) {
        nodes.value = res.data.nodes
        links.value = res.data.links
        // 重置展开状态
        expandedNodeIds.value.clear()
      }
    } catch (error) {
      console.error('初始化失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 2. 核心逻辑：切换节点的展开/收起状态
  async function toggleNode(nodeId) {
    if (expandedNodeIds.value.has(nodeId)) {
      // 如果已在集合中，说明已展开，执行收起
      collapseNode(nodeId)
      expandedNodeIds.value.delete(nodeId)
    } else {
      // 否则执行展开
      await expandNode(nodeId)
      // 注意：只有成功获取到邻居数据后，才标记为已展开
      // 这里简单处理，假设只要请求了就算展开
      expandedNodeIds.value.add(nodeId)
    }
  }

  // 2.1 展开逻辑 (Expand)
  async function expandNode(nodeId) {
    loading.value = true
    try {
      const res = await getNeighbors(nodeId)
      if (res.code === 200) {
        const { nodes: newNodes, links: newLinks } = res.data

        // 合并节点 (去重)
        newNodes.forEach(newNode => {
          if (!nodeExists(newNode.id)) {
            nodes.value.push(newNode)
          }
        })

        // 合并边 (去重)
        newLinks.forEach(newLink => {
          if (!linkExists(newLink.source, newLink.target)) {
            links.value.push(newLink)
          }
        })
      }
    } catch (error) {
      console.error('展开失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 2.2 收起逻辑 (Collapse)
  // 策略：只移除那些"仅与当前节点相连"的叶子节点
  function collapseNode(centerId) {
    // 第一步：找出所有直接连接到该 centerId 的邻居 ID
    const directLinks = links.value.filter(l => l.source === centerId || l.target === centerId)
    const neighborIds = directLinks.map(l => l.source === centerId ? l.target : l.source)

    if (neighborIds.length === 0) return

    // 第二步：筛选出"叶子节点"
    // 定义：在当前整个图谱中，度(Degree)为 1 的节点就是叶子节点
    // 意思就是这个邻居除了连接 centerId，没有连接其他任何人，那么删掉它是安全的
    const leafIdsToRemove = neighborIds.filter(nid => {
      // 计算该节点在 links 数组中出现的总次数
      const degree = links.value.reduce((acc, link) => {
        return acc + (link.source === nid || link.target === nid ? 1 : 0)
      }, 0)
      return degree === 1
    })

    // 第三步：执行移除
    if (leafIdsToRemove.length > 0) {
      // 1. 移除相关的边
      links.value = links.value.filter(l => {
        // 如果边的 source 或 target 在移除列表中，则过滤掉
        const refersToLeaf = leafIdsToRemove.includes(l.source) || leafIdsToRemove.includes(l.target)
        return !refersToLeaf
      })

      // 2. 移除节点
      nodes.value = nodes.value.filter(n => !leafIdsToRemove.includes(n.id))
    }
  }

  // 3. 搜索 (Mock Search)
  // 模拟搜索：清空当前画布，只显示搜索结果
  // 你需要在 Mock API 中也补充一个 searchNode 方法才能配合使用
  function searchAndRender(resultNodes, resultLinks) {
    nodes.value = resultNodes
    links.value = resultLinks
    expandedNodeIds.value.clear() // 重置状态
  }

  return {
    nodes,
    links,
    loading,
    fetchInitGraph,
    toggleNode, // 组件只调用这个即可
    searchAndRender
  }
})