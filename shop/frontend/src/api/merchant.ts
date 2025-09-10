import { api } from './index'

export interface MerchantStatistics {
  totalProducts: number
  onSaleProducts: number
  totalOrders: number
  pendingOrders: number
  shippedOrders: number
  totalRevenue: number
  todayRevenue: number
}

export interface MerchantProfile {
  id: number
  shopName: string
  shopDescription?: string
  contactPhone?: string
  businessLicense?: string
}

export interface Product {
  id: number
  name: string
  subtitle?: string
  category: {
    id: number
    name: string
  }
  coverImage?: string
  price: number
  unit?: string
  stock: number
  status: string
  description?: string
  createdAt: string
}

export interface Order {
  id: number
  orderNo: string
  user: {
    id: number
    username: string
    nickname?: string
  }
  status: string
  totalAmount: number
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  createdAt: string
}

// 获取商家统计信息
export function getMerchantStatistics() {
  return api.get<MerchantStatistics>('/merchant/statistics')
}

export function getMerchantTrend(days: number) {
  return api.get<{ labels: string[]; gmv: number[]; orders: number[] }>('/merchant/statistics/trend', { params: { days } })
}

export function getMerchantTopProducts() {
  return api.get<{ labels: string[]; values: number[] }>('/merchant/statistics/product-top')
}

// 获取商家商品列表
export function getMerchantProducts(params: {
  page: number
  size: number
  keyword?: string
  categoryId?: number
  status?: string
}) {
  return api.get<{
    total: number
    totalPages: number
    currentPage: number
    pageSize: number
    list: Product[]
  }>('/merchant/products', { params })
}

// 创建商品
export function createProduct(data: {
  name: string
  subtitle?: string
  categoryId: number
  coverImage?: string
  price: number
  unit?: string
  stock: number
  description?: string
}) {
  return api.post<Product>('/merchant/products', data)
}

// 更新商品
export function updateProduct(id: number, data: {
  name: string
  subtitle?: string
  categoryId: number
  coverImage?: string
  price: number
  unit?: string
  stock: number
  description?: string
}) {
  return api.put<Product>(`/merchant/products/${id}`, data)
}

// 删除商品
export function deleteProduct(id: number) {
  return api.delete(`/merchant/products/${id}`)
}

// 上架/下架商品
export function toggleProductStatus(id: number) {
  return api.post<Product>(`/merchant/products/${id}/toggle-status`)
}

// 获取商家订单列表
export function getMerchantOrders(params: {
  page: number
  size: number
  status?: string
  keyword?: string // 仅前端过滤/后端忽略也不影响
}) {
  return api.get<{
    total: number
    totalPages: number
    currentPage: number
    pageSize: number
    list: Order[]
  }>('/merchant/orders', { params })
}

// 发货
export function shipOrder(orderId: number, data: { carrier: string; trackingNo: string; senderName: string; senderPhone?: string; senderAddress: string }) {
  return api.post(`/merchant/orders/${orderId}/ship`, data)
}

// 追加物流轨迹
export function addMerchantTrack(shipmentId: number, data: { location: string; description: string; trackTime?: string }) {
  return api.post(`/merchant/logistics/${shipmentId}/tracks`, data)
}

export function getMerchantProfile() {
  return api.get<MerchantProfile>('/merchant/profile')
}

export function updateMerchantProfile(data: Partial<MerchantProfile>) {
  return api.put<MerchantProfile>('/merchant/profile', data)
}

export function closeOrder(orderId: number) {
  return api.post(`/merchant/orders/${orderId}/close`)
}

// 获取商品分类树
export function getCategoryTree() {
  return api.get<Array<{
    id: number
    name: string
    parentId?: number
    children?: any[]
  }>>('/merchant/categories/tree')
}

export function createMerchantCategory(data: { name: string; parentId?: number; sortOrder: number }) {
  return api.post('/merchant/categories', data)
}

export function updateMerchantCategory(id: number, data: { name: string; parentId?: number; sortOrder: number }) {
  return api.put(`/merchant/categories/${id}`, data)
}

export function deleteMerchantCategory(id: number) {
  return api.delete(`/merchant/categories/${id}`)
}
