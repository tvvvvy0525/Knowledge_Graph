import axios from 'axios'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api', // 这里假设后端接口在 /api 下，开发环境需配置 vite proxy
  timeout: 5000
})

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    // 假设后端约定 code 200 为成功
    if (res.code !== 200) {
      console.error('API Error:', res.message)
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res.data
  },
  error => {
    console.error('Request Failed:', error)
    return Promise.reject(error)
  }
)

export default service