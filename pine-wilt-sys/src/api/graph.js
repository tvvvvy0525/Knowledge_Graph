import request from '@/utils/request' // 引用你的 axios 实例



/**
 * 获取初始图谱数据 (GET /api/graph/init)
 * @returns {Promise} 返回节点列表
 */
export function getInitialGraph() {
  return request({
    url: '/api/graph/init',
    method: 'get'
  })
}

/**
 * 获取节点详情及邻居 (GET /api/graph/detail/{id})
 * 注意：对应 Controller 中的 /detail/{id}
 * @param {Number|String} id 节点业务ID
 * @returns {Promise} 返回 { info: {}, relations: {} }
 */
export function getNeighbors(id) {
  return request({
    url: `/api/graph/neighbors/${id}`,
    method: 'get'
  })
}

/**
 * 搜索节点 (GET /api/graph/search?q=...)
 * @param {String} keyword 搜索关键词
 * @returns {Promise} 返回匹配的节点列表
 */
export function searchNodes(keyword) {
  return request({
    url: '/api/graph/search',
    method: 'get',
    params: { q: keyword }
  })
}

/**
 * 创建新节点 (POST /api/manage/node)
 * @param {Object} data { id, name, cnName, category, description }
 */
export function createNode(data) {
  return request({
    url: '/api/manage/node',
    method: 'post',
    data
  })
}

/**
 * 更新节点 (PUT /api/manage/node)
 * @param {Object} data { id, name, cnName, category, description }
 */
export function updateNode(data) {
  return request({
    url: '/api/manage/node',
    method: 'put',
    data
  })
}

/**
 * 删除节点 (DELETE /api/manage/node/{id})
 * @param {Number|String} id 节点ID
 */
export function deleteNode(id) {
  return request({
    url: `/api/manage/node/${id}`,
    method: 'delete'
  })
}

/**
 * 创建关系 (POST /api/manage/relation)
 * @param {Object} data { sourceId, targetId, relType, cnName }
 */
export function createRelation(data) {
  return request({
    url: '/api/manage/relation',
    method: 'post',
    data
  })
}

export function searchRelationTypes(keyword) {
    return request({
        url: '/api/graph/relation/types',
        method: 'get',
        params: { q: keyword }
    })
}