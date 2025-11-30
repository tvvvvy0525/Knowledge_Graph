import axios from 'axios'
import { ElMessage } from 'element-plus' // 建议加上 UI 提示

// 创建 axios 实例
const service = axios.create({
  // 修改点 1: 去掉 /api，防止与 api/graph.js 里的路径重复拼接成 /api/api/...
  // 如果你在 vite.config.js 里配置了 proxy 转发 /api，这里留空即可，浏览器会自动请求当前域名
  baseURL: '', 
  timeout: 60000
})

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 修改点 2: 后端直接返回了数据，没有 code 包装层
    // SpringBoot 的 ResponseEntity.ok(body) 返回的就是 body 本身
    return response.data
  },
  error => {
    console.error('Request Failed:', error)
    
    // 建议：处理 HTTP 状态码错误
    let msg = '请求失败'
    if (error.response) {
      const status = error.response.status
      switch (status) {
        case 400: msg = '请求参数错误'; break;
        case 404: msg = '接口不存在'; break;
        case 500: msg = '服务器内部错误'; break;
        default: msg = error.message;
      }
    }
    
    // 使用 ElementPlus 弹出错误提示
    ElMessage.error(msg)
    
    return Promise.reject(error)
  }
)

export default service