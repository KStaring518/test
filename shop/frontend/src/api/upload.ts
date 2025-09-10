import { api, getBackendBaseUrl } from './index'

// 统一规范化图片URL：将可能的相对地址补全为可访问的完整URL
export const normalizeImageUrl = (raw?: string | null): string => {
  if (!raw) return ''
  let url = String(raw).trim()
  
  // 已经是完整URL
  if (/^https?:\/\//i.test(url)) {
    return url
  }
  
  // 去掉错误前缀与多余斜杠
  url = url.replace(/^\/api\//, '/').replace(/\\/g, '/').replace(/\/+/, '/').trim()
  
  // 使用 axios baseURL
  const base = getBackendBaseUrl()
  if (url.startsWith('/uploads/')) return `${base}${url}`
  if (url.startsWith('uploads/')) return `${base}/${url}`
  // 兜底：其他相对路径
  return `${base}${url.startsWith('/') ? '' : '/'}${url}`
}

// 专用于商品主图：兼容数据库里仅存文件名的历史数据
export const normalizeProductImageUrl = (raw?: string | null): string => {
  if (!raw) return ''
  let url = String(raw).trim()
  
  // 完整URL
  if (/^https?:\/\//i.test(url)) {
    return url
  }
  
  url = url.replace(/^\/api\//, '/').replace(/\\/g, '/').replace(/\/+/, '/')
  const base = getBackendBaseUrl()
  if (url.startsWith('/uploads/')) return `${base}${url}`
  if (url.startsWith('uploads/')) return `${base}/${url}`
  // 仅文件名（如 product_25.jpg）
  if (!url.includes('/')) return `${base}/uploads/products/${url}`
  return `${base}${url.startsWith('/') ? '' : '/'}${url}`
}

// 上传商品图片
export const uploadProductImage = (file: File, productId?: number) => {
  const formData = new FormData()
  formData.append('file', file)
  if (productId) {
    formData.append('productId', productId.toString())
  }
  return api.post<{ data: string }>('/upload/product-image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 上传用户头像
export const uploadAvatar = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return api.post<{ data: string }>('/upload/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除图片
export const deleteImage = (imageUrl: string) => {
  return api.delete('/upload/image', { params: { imageUrl } })
}
