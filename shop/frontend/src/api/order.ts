import { api } from './index'

export interface OrderItem {
  id: number
  product: {
    id: number
    name: string
    subtitle: string
    coverImage: string
    price: number
    unit: string
  }
  productNameSnapshot: string
  priceSnapshot: number
  quantity: number
  subtotal: number
  hasReview?: boolean
}

export interface Order {
  id: number
  orderNo: string
  userId: number
  totalAmount: number
  status: string
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  remark?: string
  payType?: string
  payTime?: string
  orderItems: OrderItem[]
  createdAt: string
  updatedAt: string
  shipment?: Shipment
}

export interface Shipment {
  id: number
  shipmentNo: string
  carrier: string
  trackingNo: string
  status: string
  shippedAt: string
  shippingAddress: string
  senderName?: string
  senderPhone?: string
}

export interface LogisticsTrack {
  id: number
  location: string
  description: string
  trackTime: string
  createdAt: string
}

export interface OrderCreateRequest {
  addressId: number
  cartItemIds: number[]
}

export interface OrderSummary {
  totalOrders: number
  unpaidOrders: number
  paidOrders: number
  shippedOrders: number
  finishedOrders: number
  cancelledOrders: number
}

// 创建订单
export const createOrder = (data: OrderCreateRequest) => {
  return api.post<Order>('/order/create', data)
}

// 获取我的订单列表
export const getMyOrders = (params?: {
  page?: number
  size?: number
  status?: 'UNPAID' | 'PAID' | 'SHIPPED' | 'FINISHED' | 'CLOSED'
}) => {
  return api.get<{ list: Order[]; total: number; totalPages: number; page: number; size: number }>('/order/my', { params })
}

// 获取订单详情
export const getOrderDetail = (orderId: number) => {
  return api.get<Order>(`/order/${orderId}`)
}

// 取消订单
export const cancelOrder = (orderId: number) => {
  return api.post(`/order/${orderId}/cancel`)
}

// 确认收货
export const confirmReceive = (orderId: number) => {
  return api.post(`/order/${orderId}/confirm`)
}

// 模拟支付
export const mockPay = (orderNo: string) => {
  return api.post<Order>(`/order/pay/mock?orderNo=${orderNo}`)
}

// 发货
export const shipOrder = (orderNo: string) => {
  return api.post(`/order/ship?orderNo=${orderNo}`)
}

// 获取订单统计
export const getOrderSummary = () => {
  return api.get<OrderSummary>('/order/summary')
}

// 获取物流轨迹
export const getLogisticsTracks = (shipmentId: number) => {
  return api.get<LogisticsTrack[]>(`/logistics/shipment/${shipmentId}/tracks`)
}
