import { defineStore } from 'pinia'
import { ref } from 'vue'
// 引入真实的 API
import { getInitialGraph, getNeighbors, searchNodes, createNode, updateNode, deleteNode, createRelation } from '@/api/graph'
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
            // 1. 先清空当前画布
            nodes.value = []
            links.value = []
            expandedNodeIds.value.clear()

            // 2. 指定初始化的中心节点 ID (根据你的需求是 1)
            const ROOT_ID = 1

            console.log(`正在初始化图谱，加载核心节点 ID: ${ROOT_ID}...`)

            // 3. 复用 getNeighbors 接口获取数据
            const res = await getNeighbors(ROOT_ID)

            // --- 数据解析逻辑 (与 expandNode 类似) ---

            // A. 解析中心节点
            const centerNode = res.node || res.info
            if (!centerNode) {
                throw new Error("未找到中心节点信息")
            }
            // 添加中心节点
            nodes.value.push(centerNode)

            // B. 解析邻居和连线
            const relationsMap = res.neighbors || res.relations || {}

            const newNodes = []
            const newLinks = []
            const centerIdStr = toStr(centerNode.id)

            Object.keys(relationsMap).forEach(relType => {
                const list = relationsMap[relType]
                list.forEach(item => {
                    const targetNode = item.target
                    if (!targetNode) return

                    const targetIdStr = toStr(targetNode.id)
                    const relLabel = item.relCnName || relType

                    // 收集邻居节点
                    newNodes.push(targetNode)

                    // 收集连线
                    newLinks.push({
                        source: centerIdStr,
                        target: targetIdStr,
                        label: relLabel
                    })
                })
            })

            // C. 将新数据去重后加入 State
            // 因为是初始化，其实不需要去重，直接 push 即可，但为了保险还是判断一下
            newNodes.forEach(n => {
                if (!nodeExists(n.id)) nodes.value.push(n)
            })

            newLinks.forEach(l => {
                if (!linkExists(l.source, l.target)) links.value.push(l)
            })

            // D. 标记中心节点为已展开
            expandedNodeIds.value.add(centerIdStr)

            console.log(`初始化完成: ${nodes.value.length} 节点, ${links.value.length} 连线`)

        } catch (error) {
            console.error('初始化图谱失败:', error)
            ElMessage.error('图谱初始化失败，请检查后端服务')
        } finally {
            loading.value = false
        }
    }

  // 2. 核心逻辑：切换节点的展开/收起状态
    async function toggleNode(nodeId) {
        const idStr = toStr(nodeId) // 强转字符串

        if (expandedNodeIds.value.has(idStr)) {
            console.log('节点已展开，执行收起:', idStr)
            collapseNode(idStr)
            expandedNodeIds.value.delete(idStr)
        } else {
            console.log('节点未展开，执行展开:', idStr)
            await expandNode(nodeId)
        }
    }

  // 2.1 展开逻辑 (适配新的后端结构)
    async function expandNode(nodeId) {
        loading.value = true
        try {
            console.log('正在请求邻居数据, ID:', nodeId)
            const res = await getNeighbors(nodeId)
            console.log('API返回数据:', res)

            // 1. 解析中心节点
            // 你的 JSON 里 key 是 "node"
            const centerNode = res.node || res.info
            if (!centerNode) {
                console.warn('返回数据中缺少 node 信息')
                return
            }
            const centerId = toStr(nodeId)

            // 2. 解析邻居 Map
            // 你的 JSON 里 key 是 "neighbors"
            const relationsMap = res.neighbors || res.relations || {}

            const newNodes = []
            const newLinks = []

            // 3. 遍历 Map
            Object.keys(relationsMap).forEach(relType => {
                const list = relationsMap[relType]

                list.forEach(item => {
                    // item 结构期望: { relType: "...", relCnName: "...", target: {...}, sourceId: 123 }
                    const targetNode = item.target
                    if (!targetNode) return

                    const neighborId = toStr(targetNode.id)
                    const relLabel = item.relCnName || relType

                    // 【新增逻辑】获取真实关系的起始ID (由后端返回)
                    // 如果后端没返回 sourceId，默认为 centerId (向外指)
                    const realSourceId = item.sourceId ? toStr(item.sourceId) : centerId

                    // 准备添加节点
                    newNodes.push(targetNode)

                    // 准备添加连线
                    // 逻辑判断：如果真实起点的ID 等于 邻居节点的ID，说明是【入边】(邻居 -> 我)
                    // 否则就是【出边】(我 -> 邻居)
                    const isIncoming = (realSourceId === neighborId)

                    newLinks.push({
                        source: isIncoming ? neighborId : centerId, // 起点
                        target: isIncoming ? centerId : neighborId, // 终点
                        label: relLabel
                    })
                })
            })

            console.log(`解析出 ${newNodes.length} 个新节点, ${newLinks.length} 条新连线`)

            // 4. 合并入 State (Vue 响应式更新)
            // 使用 batch 推送，性能更好
            let addedNodesCount = 0
            newNodes.forEach(n => {
                // 关键：必须确保 ID 类型一致，且 ID 不重复
                if (!nodeExists(n.id)) {
                    nodes.value.push(n)
                    addedNodesCount++
                }
            })

            let addedLinksCount = 0
            newLinks.forEach(l => {
                if (!linkExists(l.source, l.target)) {
                    links.value.push(l)
                    addedLinksCount++
                }
            })

            console.log(`实际添加到画布: ${addedNodesCount} 个节点, ${addedLinksCount} 条连线`)

            // 标记为已展开
            expandedNodeIds.value.add(centerId)

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
            // 1. 调用 API
            // 返回结构: { nodes: [...], links: [...] }
            const res = await searchNodes(keyword)

            // 2. 【关键修正】正确解构数据
            // 后端返回的 nodes 和 links 都在 res 对象里，不能直接赋给 nodes.value
            const searchNodesResult = res.nodes || []
            const searchLinksResult = res.links || []

            // 3. 赋值给 State
            // 这里的节点 ID 是数字，GraphChart 会自动转字符串，没问题
            nodes.value = searchNodesResult

            // 处理连线：确保 source/target 转为字符串 (为了稳健)
            links.value = searchLinksResult.map(l => ({
                source: toStr(l.source),
                target: toStr(l.target),
                label: l.label
            }))

            // 4. 重置展开状态
            expandedNodeIds.value.clear()

            // 5. 【关键修正】获取正确的长度
            if (searchNodesResult.length > 0) {
                ElMessage.success(`找到 ${searchNodesResult.length} 个相关节点`)
            } else {
                ElMessage.warning('未找到相关节点')
            }

        } catch (error) {
            console.error('搜索出错:', error)
            ElMessage.error('搜索失败')
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