import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
export const api = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 300000, // 增加到5分钟，给Ollama更多响应时间
  headers: {
    'Content-Type': 'application/json'
  }
})

// 返回 axios baseURL 去掉末尾斜杠，用于拼接静态资源等完整地址
export const getBackendBaseUrl = (): string => {
  const raw = api.defaults.baseURL || ''
  // e.g. http://localhost:8080/api
  return raw.endsWith('/') ? raw.slice(0, -1) : raw
}

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    console.log('API响应:', response)
    const { data } = response
    if (data.code === 200) {
      return data
    } else {
      console.error('API业务错误:', data)
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message || '请求失败'))
    }
  },
  (error) => {
    console.error('API网络错误:', error)
    if (error.response?.status === 401) {
      // 不再强制清空登录并跳转，避免误判导致用户被登出
      // 交由页面根据需要引导用户登录
      ElMessage.error(error.response?.data?.message || '未登录或登录已过期')
      return Promise.reject(error)
    }
    ElMessage.error(error.response?.data?.message || '网络错误')
    return Promise.reject(error)
  }
)
