import { api } from './index'

export interface Address {
  id: number
  receiverName: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault: boolean
  createdAt: string
  updatedAt: string
}

export interface AddressCreateRequest {
  receiverName: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault?: boolean
}

// 获取地址列表
export const getAddresses = () => {
  return api.get<Address[]>('/address/list')
}

// 创建地址
export const createAddress = (data: AddressCreateRequest) => {
  return api.post<Address>('/address/create', data)
}

// 删除地址
export const deleteAddress = (id: number) => {
  return api.post('/address/delete', { id })
}

// 更新地址
export const updateAddress = (data: Address & { id: number }) => {
  return api.put<Address>('/address/update', data)
}

// 设为默认
export const setDefaultAddress = (id: number) => {
  return api.post('/address/set-default', id)
}
