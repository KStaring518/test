import { api } from "@/api/index";

/**
 * 创建支付宝支付页面
 */
export function createAlipayPayment(orderNo: string) {
  return api({
    url: '/alipay/create',
    method: 'post',
    params: { orderNo }
  })
}

/**
 * 查询支付宝支付状态
 */
export function queryAlipayPaymentStatus(orderNo: string) {
  return api({
    url: '/alipay/query',
    method: 'get',
    params: { orderNo }
  })
}
