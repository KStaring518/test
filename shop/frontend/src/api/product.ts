import { api } from './index'

export interface Product {
  id: number
  name: string
  subtitle: string
  categoryId: number
  coverImage: string
  price: number
  unit: string
  stock: number
  status: number
  description: string
  createdAt: string
}

export interface Category {
  id: number
  name: string
  parentId?: number
  sortOrder: number
  status: number
  children?: Category[]
}

// 获取商品列表
export const getProducts = (params?: {
  page?: number
  size?: number
  categoryId?: number
  keyword?: string
  sort?: string
}) => {
  return api.get('/products/public/list', { params })
}

// 获取分类树
export const getCategories = () => {
  return api.get<Category[]>('/categories/public/tree')
}

// 获取单个商品详情
export const getProduct = (id: number) => {
  return api.get<Product>(`/products/public/${id}`)
}

// 收藏相关
export const addFavorite = (productId: number) => api.post(`/favorite/add/${productId}`)
export const removeFavorite = (productId: number) => api.delete(`/favorite/remove/${productId}`)
export const checkFavorite = (productId: number) => api.get<boolean>(`/favorite/check/${productId}`)
export interface FavoriteItem {
  favoriteId: number
  productId: number
  name: string
  subtitle: string
  coverImage: string
  price: number
  unit: string
  stock: number
  createdAt: string
}
export const listFavorites = (params?: { page?: number; size?: number }) =>
  api.get<{ total: number; totalPages: number; page: number; size: number; list: FavoriteItem[] }>(`/favorite/list`, { params })