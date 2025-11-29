import { defineStore } from 'pinia'
import { ref } from 'vue'
// 引入真实的 API
import { getInitialGraph, getNodeDetail, searchNodes, createNode, updateNode, deleteNode, createRelation } from '@/api/graph'
import { ElMessage } from 'element-plus'
export const useGraphStore = defineStore('graph', () => {
  // === State ===
  const nodes = ref([])
  const links = ref([])
  const loading = ref(false)
  // 记录已展开的节点ID (存为 String 类型)
  const expandedNodeIds = ref(new Set())

  // === Helpers ===
  // 统一将 ID 转为字符串，防止 123 !== "123" 的问题
  const toStr = (val) => String(val)

  const nodeExists = (id) => nodes.value.some(n => toStr(n.id) === toStr(id))
  
  const linkExists = (s, t) => links.value.some(l => 
    (toStr(l.source) === toStr(s) && toStr(l.target) === toStr(t)) || 
    (toStr(l.source) === toStr(t) && toStr(l.target) === toStr(s))
  )

  // === Actions ===

  // 1. 初始化图谱
  async function fetchInitGraph() {
    loading.value = true
    try {
      // API 返回的是 List<EntityNode>
      const data = await getInitialGraph()
      
      // 直接赋值节点，清空连线
      nodes.value = data
      links.value = []
      expandedNodeIds.value.clear()
    } catch (error) {
      console.error('初始化失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 2. 核心逻辑：切换节点的展开/收起状态
  async function toggleNode(nodeId) {
    const idStr = toStr(nodeId)

    if (expandedNodeIds.value.has(idStr)) {
      // 这里的收起逻辑，我们复用你之前写的"叶子节点移除法"
      collapseNode(idStr)
      expandedNodeIds.value.delete(idStr)
    } else {
      // 执行展开
      await expandNode(idStr)
    }
  }

  // 2.1 展开逻辑 (适配新的后端结构)
  async function expandNode(nodeId) {
    loading.value = true
    try {
      // API 返回: { info: {...}, relations: { "DAMAGES": [ {target:..., relCnName:...} ] } }
      const res = await getNodeDetail(nodeId)
      
      const centerId = toStr(res.info.id)
      const relationsMap = res.relations || {}

      const newNodes = []
      const newLinks = []

      // 遍历 Map，拍平数据
      Object.keys(relationsMap).forEach(relType => {
        const list = relationsMap[relType]
        
        list.forEach(item => {
          const targetNode = item.target
          const targetId = toStr(targetNode.id)
          const relLabel = item.relCnName || relType // 优先用中文名

          // 收集节点 (稍后去重)
          newNodes.push(targetNode)

          // 收集连线
          newLinks.push({
            source: centerId,
            target: targetId,
            label: relLabel 
          })
        })
      })

      // 合并入 State (去重)
      newNodes.forEach(n => {
        if (!nodeExists(n.id)) {
          nodes.value.push(n)
        }
      })

      newLinks.forEach(l => {
        if (!linkExists(l.source, l.target)) {
          links.value.push(l)
        }
      })

      // 标记为已展开
      expandedNodeIds.value.add(toStr(nodeId))

    } catch (error) {
      console.error('展开失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 2.2 收起逻辑 (保持你原本的优秀逻辑，只加了 ID 类型转换)
  function collapseNode(centerId) {
    centerId = toStr(centerId)

    // 1. 找出直接连接的边
    const directLinks = links.value.filter(l => 
      toStr(l.source) === centerId || toStr(l.target) === centerId
    )
    
    // 2. 找出直接邻居 ID
    const neighborIds = directLinks.map(l => 
      toStr(l.source) === centerId ? toStr(l.target) : toStr(l.source)
    )

    if (neighborIds.length === 0) return

    // 3. 筛选出"叶子节点" (度为1的节点)
    const leafIdsToRemove = neighborIds.filter(nid => {
      // 计算该邻居在整个 links 中出现的次数
      const degree = links.value.reduce((acc, link) => {
        const s = toStr(link.source)
        const t = toStr(link.target)
        return acc + (s === nid || t === nid ? 1 : 0)
      }, 0)
      return degree === 1
    })

    // 4. 执行移除
    if (leafIdsToRemove.length > 0) {
      links.value = links.value.filter(l => {
        const s = toStr(l.source)
        const t = toStr(l.target)
        // 如果边的任意一端在待删除列表中，则该边要被删除
        return !leafIdsToRemove.includes(s) && !leafIdsToRemove.includes(t)
      })

      nodes.value = nodes.value.filter(n => !leafIdsToRemove.includes(toStr(n.id)))
    }
  }

  // 3. 搜索逻辑
  async function performSearch(keyword) {
    if (!keyword) return
    loading.value = true
    try {
      const res = await searchNodes(keyword)
      // 搜索结果通常是一堆节点，没有连线
      nodes.value = res
      links.value = []
      expandedNodeIds.value.clear()
      ElMessage.success(`找到 ${res.length} 个相关节点`)
    } catch (error) {
      console.error(error)
    } finally {
      loading.value = false
    }
  }

  async function addNode(nodeData) {
    try {
      const newNode = await createNode(nodeData)
      // 前端直接添加到数组，避免刷新
      nodes.value.push(newNode)
      ElMessage.success('创建成功')
      return newNode
    } catch (error) {
      console.error(error)
      throw error
    }
  }

  async function addLink(linkData) {
    try {
      await createRelation(linkData)
      // 前端更新视图：如果源节点和目标节点都在当前画布上，才画线
      const sourceExists = nodeExists(linkData.sourceId)
      const targetExists = nodeExists(linkData.targetId)
      
      if (sourceExists && targetExists) {
        links.value.push({
          source: toStr(linkData.sourceId),
          target: toStr(linkData.targetId),
          label: linkData.cnName || linkData.relType
        })
      }
      ElMessage.success('关系创建成功')
    } catch (error) {
      console.error(error)
      throw error
    }
  }

  async function updateNodeAction(nodeData) {
    try {
      const updatedNode = await updateNode(nodeData)
      // 更新本地 State
      const index = nodes.value.findIndex(n => toStr(n.id) === toStr(updatedNode.id))
      if (index !== -1) {
        // Vue 3 响应式替换
        nodes.value[index] = updatedNode
      }
      ElMessage.success('更新成功')
      return updatedNode
    } catch (error) {
      console.error(error)
      throw error
    }
  }

  async function deleteNodeAction(id) {
    try {
      await deleteNode(id)
      const idStr = toStr(id)
      // 级联删除前端数据
      nodes.value = nodes.value.filter(n => toStr(n.id) !== idStr)
      links.value = links.value.filter(l => toStr(l.source) !== idStr && toStr(l.target) !== idStr)
      ElMessage.success('删除成功')
    } catch (error) {
      console.error(error)
      throw error
    }
  }

  return {
    nodes,
    links,
    loading,
    fetchInitGraph,
    toggleNode,
    performSearch,addNode, addLink, updateNodeAction, deleteNodeAction
  }
})