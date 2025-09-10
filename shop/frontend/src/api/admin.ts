import { api } from './index'

export interface PageResult<T> {
  total: number
  totalPages: number
  page: number
  size: number
  list: T[]
}

export interface AdminStatistics {
  totalUsers: number
  totalMerchants: number
  totalOrders: number
  totalProducts: number
  
  // 添加历史数据字段
  previousUsers?: number
  previousMerchants?: number
  previousProducts?: number
  previousOrders?: number
  
  // 添加增长率字段
  userGrowthRate?: number
  merchantGrowthRate?: number
  productGrowthRate?: number
  orderGrowthRate?: number
}

export interface UserItem {
  id: number
  username: string
  nickname?: string
  phone?: string
  email?: string
  role: 'USER' | 'MERCHANT' | 'ADMIN'
  status: 'ENABLED' | 'DISABLED'
  createdAt: string
}

export interface MerchantItem {
  id: number
  shopName: string
  contactPhone?: string
  status: 'PENDING' | 'APPROVED' | 'SUSPENDED'
  user: UserItem
  createdAt: string
}

export interface CategoryItem {
  id: number
  name: string
  parentId?: number
  sortOrder?: number
  status: 'ENABLED' | 'DISABLED'
  children?: CategoryItem[]
}

export interface CategoryCreateRequest {
  name: string
  parentId?: number
  sortOrder?: number
  status?: 'ENABLED' | 'DISABLED'
}

export interface CategoryUpdateRequest extends CategoryCreateRequest {}

export const getAdminStatistics = () => api.get<AdminStatistics>('/admin/statistics')
export const getAdminOverview = () => api.get<{ todayOrders: number; todayGmv: number }>('/admin/statistics/overview')
export const getAdminTrend = (days: number) => api.get<{ labels: string[]; gmv: number[]; orders: number[] }>('/admin/statistics/trend', { params: { days } })
export const getAdminStatusDistribution = () => api.get<{ labels: string[]; values: number[] }>('/admin/statistics/status-distribution')
export const getAdminCategoryTop = () => api.get<{ labels: string[]; values: number[] }>('/admin/statistics/category-top')

export const getAdminUsers = (params: { page?: number; size?: number; keyword?: string; role?: string; status?: string }) =>
  api.get<PageResult<UserItem>>('/admin/users', { params })

export const toggleUserStatus = (id: number) => api.post<UserItem>(`/admin/users/${id}/toggle-status`)
export const deleteUser = (id: number) => api.delete(`/admin/users/${id}`)

export const getAdminMerchants = (params: { page?: number; size?: number; keyword?: string; status?: string }) =>
  api.get<PageResult<MerchantItem>>('/admin/merchants', { params })

export const approveMerchant = (id: number) => api.post<MerchantItem>(`/admin/merchants/${id}/approve`)
export const suspendMerchant = (id: number) => api.post<MerchantItem>(`/admin/merchants/${id}/suspend`)
export const resumeMerchant = (id: number) => api.post<MerchantItem>(`/admin/merchants/${id}/resume`)

export const getCategoryTree = () => api.get<CategoryItem[]>('/admin/categories/tree')
export const createCategory = (data: CategoryCreateRequest) => api.post<CategoryItem>('/admin/categories', data)
export const updateCategory = (id: number, data: CategoryUpdateRequest) => api.put<CategoryItem>(`/admin/categories/${id}`, data)
export const deleteCategory = (id: number) => api.delete(`/admin/categories/${id}`)

export const getAdminOrders = (params: { page?: number; size?: number; status?: 'UNPAID' | 'PAID' | 'SHIPPED' | 'FINISHED' | 'CLOSED' }) =>
  api.get<PageResult<any>>('/admin/orders', { params })

export interface SystemConfig {
  siteName: string
  contactPhone: string
  contactEmail: string
  orderAutoCloseMinutes: number
  shipmentAutoConfirmDays: number
}

export const getSystemConfig = () => api.get<SystemConfig>('/admin/system/config')
export const saveSystemConfig = (data: SystemConfig) => api.post<SystemConfig>('/admin/system/config', data)
 
// 轮播管理（管理员）
export interface BannerItem {
  id?: number
  title?: string
  imageUrl: string
  linkUrl?: string
  sortOrder?: number
  status?: 'ENABLED' | 'DISABLED'
  createdAt?: string
}

export const getPublicBanners = () => api.get<BannerItem[]>('/banners/public/list')
export const getAllBanners = () => api.get<BannerItem[]>('/banners/admin/list')
export const createBanner = (data: BannerItem) => api.post<BannerItem>('/banners/admin/create', data)
export const updateBanner = (id: number, data: BannerItem) => api.put<BannerItem>(`/banners/admin/${id}`, data)
export const deleteBanner = (id: number) => api.delete(`/banners/admin/${id}`)
export const updateBannerOrder = (data: { id: number; sortOrder: number }[]) => api.put('/banners/admin/order', data)

// 图片上传
export const uploadImage = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return api.post<string>('/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
