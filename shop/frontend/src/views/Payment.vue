<template>
  <div class="payment-container">
    <div class="payment-header">
      <h2>订单支付</h2>
      <p>订单号：{{ orderNo }}</p>
      <p>支付金额：¥{{ totalAmount }}</p>
    </div>

    <div class="payment-methods">
      <h3>选择支付方式</h3>
      
      <div class="payment-method" @click="selectPaymentMethod('alipay')" :class="{ active: selectedMethod === 'alipay' }">
        <div class="method-icon">
          <i class="el-icon-money"></i>
        </div>
        <div class="method-info">
          <h4>支付宝支付</h4>
          <p>推荐使用支付宝扫码支付</p>
        </div>
        <div class="method-radio">
          <el-radio v-model="selectedMethod" label="alipay"></el-radio>
        </div>
      </div>

      <div class="payment-method" @click="selectPaymentMethod('mock')" :class="{ active: selectedMethod === 'mock' }">
        <div class="method-icon">
          <i class="el-icon-credit-card"></i>
        </div>
        <div class="method-info">
          <h4>模拟支付</h4>
          <p>仅用于测试，无需真实支付</p>
        </div>
        <div class="method-radio">
          <el-radio v-model="selectedMethod" label="mock"></el-radio>
        </div>
      </div>
    </div>

    <div class="payment-actions">
      <el-button type="primary" size="large" @click="handlePayment" :loading="paying">
        {{ paying ? '处理中...' : '立即支付' }}
      </el-button>
      <el-button size="large" @click="goBack">返回订单</el-button>
    </div>
    
    <!-- 支付状态检查区域 -->
    <div v-if="showStatusCheck" class="status-check">
      <el-alert
        title="支付完成后，如果页面没有自动跳转，请点击下方按钮检查订单状态"
        type="info"
        :closable="false"
        show-icon
      />
      <div style="margin-top: 10px;">
        <el-button type="success" @click="checkOrderStatus" :loading="checkingStatus">
          {{ checkingStatus ? '检查中...' : '检查订单状态' }}
        </el-button>
        <el-button @click="showStatusCheck = false">关闭提示</el-button>
      </div>
    </div>
    
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createAlipayPayment } from '@/api/alipay'
import { getBackendBaseUrl } from '@/api/index'
import { mockPay } from '@/api/order'

const route = useRoute()
const router = useRouter()

const orderNo = ref('')
const totalAmount = ref(0)
const selectedMethod = ref('alipay')
const paying = ref(false)
const showStatusCheck = ref(false)
const checkingStatus = ref(false)

onMounted(() => {
  // 从路由参数获取订单信息
  orderNo.value = route.query.orderNo || ''
  totalAmount.value = parseFloat(route.query.totalAmount) || 0

  if (!orderNo.value) {
    ElMessage.error('订单信息不完整')
    router.push('/orders')
    return
  }
  // 监听支付宝支付回调消息
  const handleMessage = (event) => {
    console.log('收到消息:', event.data)
    if (event.data && event.data.type === 'ALIPAY_PAYMENT_SUCCESS' && event.data.orderNo) {
      ElMessage.success('支付成功，订单已更新')
      // 自动跳转到订单页面
      router.push(`/orders?paid=1&orderNo=${event.data.orderNo}`)
    } else if (event.data && event.data.type === 'ALIPAY_RETURN' && event.data.orderNo) {
      ElMessage.success('支付成功，订单已更新')
      router.push(`/orders?paid=1&orderNo=${event.data.orderNo}`)
    }
  }
  window.addEventListener('message', handleMessage)
  
  // 备用方案：轮询检查订单状态
  let pollInterval = null
  const startPolling = () => {
    if (pollInterval) return
    console.log('开始轮询订单状态...')
    let pollCount = 0
    const maxPolls = 30 // 最多轮询30次（1分钟）
    
    pollInterval = setInterval(async () => {
      pollCount++
      console.log(`轮询订单状态 ${pollCount}/${maxPolls}`)
      
      try {
        const response = await fetch(`${getBackendBaseUrl()}/orders/${orderNo.value}`)
        if (response.ok) {
          const order = await response.json()
          console.log('订单状态:', order.data?.status)
          if (order.data && order.data.status === 'PAID') {
            console.log('检测到订单已支付，停止轮询')
            clearInterval(pollInterval)
            pollInterval = null
            ElMessage.success('支付成功，订单已更新')
            router.push(`/orders?paid=1&orderNo=${orderNo.value}`)
            return
          }
        }
      } catch (error) {
        console.log('轮询订单状态失败:', error)
      }
      
      // 达到最大轮询次数，停止轮询
      if (pollCount >= maxPolls) {
        console.log('轮询超时，停止轮询')
        clearInterval(pollInterval)
        pollInterval = null
        showStatusCheck.value = true
      }
    }, 2000) // 每2秒检查一次
  }
  
  // 组件卸载时移除监听器和轮询
  onUnmounted(() => {
    window.removeEventListener('message', handleMessage)
    if (pollInterval) {
      clearInterval(pollInterval)
      pollInterval = null
    }
  })
})

const selectPaymentMethod = (method) => {
  selectedMethod.value = method
}

const handlePayment = async () => {
  if (selectedMethod.value === 'alipay') {
    const base = getBackendBaseUrl()
    window.open(`${base}/alipay/create-page?orderNo=${encodeURIComponent(orderNo.value)}`, '_blank', 'noopener,noreferrer')
    // 启动轮询检查订单状态
    startPolling()
    // 显示状态检查提示
    setTimeout(() => {
      showStatusCheck.value = true
    }, 10000) // 10秒后显示检查提示
  } else if (selectedMethod.value === 'mock') {
    await handleMockPayment()
  }
}

// 旧的内嵌表单方案已弃用，保留占位避免引用错误
const handleAlipayPayment = async () => {}

const handleMockPayment = async () => {
  try {
    paying.value = true
    // 调用后端模拟支付接口，这会扣减库存
    await mockPay(orderNo.value)
    ElMessage.success('模拟支付成功！库存已扣减')
    // 跳转到支付成功页面
    router.push({
      path: '/payment/success',
      query: { orderNo: orderNo.value }
    })
  } catch (error) {
    ElMessage.error('模拟支付失败：' + (error.response?.data?.message || error.message))
  } finally {
    paying.value = false
  }
}

// 不再内嵌支付宝页面
const closeAlipayPage = () => {}

const goBack = () => {
  router.push('/orders')
}

// 手动检查订单状态
const checkOrderStatus = async () => {
  try {
    checkingStatus.value = true
    const response = await fetch(`${getBackendBaseUrl()}/orders/${orderNo.value}`)
    if (response.ok) {
      const order = await response.json()
      if (order.data && order.data.status === 'PAID') {
        ElMessage.success('支付成功，订单已更新')
        router.push(`/orders?paid=1&orderNo=${orderNo.value}`)
      } else {
        ElMessage.warning('订单尚未支付，请完成支付后再次检查')
      }
    } else {
      ElMessage.error('检查订单状态失败')
    }
  } catch (error) {
    console.error('检查订单状态失败:', error)
    ElMessage.error('检查订单状态失败：' + error.message)
  } finally {
    checkingStatus.value = false
  }
}
</script>

<style scoped>
.payment-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.payment-header {
  text-align: center;
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.payment-header h2 {
  color: #303133;
  margin-bottom: 15px;
}

.payment-header p {
  color: #606266;
  margin: 5px 0;
  font-size: 16px;
}

.payment-methods {
  margin-bottom: 30px;
}

.payment-methods h3 {
  margin-bottom: 20px;
  color: #303133;
}

.payment-method {
  display: flex;
  align-items: center;
  padding: 20px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.payment-method:hover {
  border-color: #409eff;
}

.payment-method.active {
  border-color: #409eff;
  background: #f0f9ff;
}

.method-icon {
  font-size: 24px;
  color: #409eff;
  margin-right: 15px;
  width: 40px;
  text-align: center;
}

.method-info {
  flex: 1;
}

.method-info h4 {
  margin: 0 0 5px 0;
  color: #303133;
}

.method-info p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.method-radio {
  margin-left: 15px;
}

.payment-actions {
  text-align: center;
  margin-bottom: 30px;
}

.status-check {
  margin-top: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  text-align: center;
}

.payment-actions .el-button {
  margin: 0 10px;
}

.alipay-page {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: white;
  z-index: 1000;
  overflow: auto;
}

.alipay-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
  background: #f8f9fa;
}

.alipay-header h3 {
  margin: 0;
  color: #303133;
}

.alipay-content {
  padding: 20px;
  min-height: 500px;
}
</style>
