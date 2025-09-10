import { api } from './index'

export interface CartItem {
  id: number
  product: {
    id: number
    name: string
    subtitle: string
    coverImage: string
    price: number
    unit: string
    stock: number
  }
  quantity: number
  checked: boolean
  createdAt: string
}

export interface CartSummary {
  totalItems: number
  checkedItems: number
  totalAmount: number
}

// 添加商品到购物车
export const addToCart = (data: { productId: number; quantity: number }) => {
  return api.post('/cart/add', data)
}

// 获取购物车列表
export const getCartList = () => {
  return api.get<CartItem[]>('/cart/list')
}

// 更新购物车商品数量
export const updateCartQuantity = (cartItemId: number, quantity: number) => {
  return api.put(`/cart/${cartItemId}/quantity?quantity=${quantity}`)
}

// 删除购物车商品
export const removeCartItem = (cartItemId: number) => {
  return api.delete(`/cart/${cartItemId}`)
}

// 切换商品选择状态
export const toggleCartItem = (cartItemId: number) => {
  return api.put(`/cart/${cartItemId}/toggle`)
}

// 获取购物车统计信息
export const getCartSummary = () => {
  return api.get<CartSummary>('/cart/summary')
}

// 清空购物车
export const clearCart = () => {
  return api.delete('/cart/clear')
}