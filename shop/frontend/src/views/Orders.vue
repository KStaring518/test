<template>
  <div class="orders-page">
    <!-- 导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo">
          <h2>零食商城</h2>
        </div>
        
        <el-menu mode="horizontal" :router="true" class="nav-menu">
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/products">商品列表</el-menu-item>
          <el-menu-item index="/cart">购物车</el-menu-item>
          <el-menu-item index="/orders">我的订单</el-menu-item>
        </el-menu>
        
        <div class="user-info">
          <template v-if="userStore.isLoggedIn()">
            <span>欢迎，{{ userStore.user?.nickname || userStore.user?.username }}</span>
            <el-button @click="handleLogout" type="text">退出</el-button>
          </template>
          <template v-else>
            <el-button @click="$router.push('/login')" type="primary">登录</el-button>
          </template>
        </div>
      </div>
    </el-header>
    
    <!-- 主要内容 -->
    <el-main class="main-content">
      <div class="orders-container">
        <h2>我的订单</h2>

        <!-- 状态筛选 -->
        <div class="status-filter">
          <el-radio-group v-model="filters.status" @change="onStatusChange">
            <el-radio-button label="ALL">全部</el-radio-button>
            <el-radio-button label="UNPAID">待付款</el-radio-button>
            <el-radio-button label="PAID">待发货</el-radio-button>
            <el-radio-button label="SHIPPED">待收货</el-radio-button>
            <el-radio-button label="FINISHED">已完成</el-radio-button>
            <el-radio-button label="CLOSED">已取消</el-radio-button>
          </el-radio-group>
        </div>
        
        <!-- 订单统计 -->
        <div class="order-summary" v-if="orderSummary">
          <el-row :gutter="16">
            <el-col :span="4"><div class="summary-item">
              <div class="summary-number all">{{ orderSummary.totalOrders }}</div>
              <div class="summary-label">全部订单</div>
            </div></el-col>
            <el-col :span="4"><div class="summary-item">
              <div class="summary-number unpaid">{{ orderSummary.unpaidOrders }}</div>
              <div class="summary-label">待付款</div>
            </div></el-col>
            <el-col :span="4"><div class="summary-item">
              <div class="summary-number paid">{{ orderSummary.paidOrders }}</div>
              <div class="summary-label">待发货</div>
            </div></el-col>
            <el-col :span="4"><div class="summary-item">
              <div class="summary-number shipped">{{ orderSummary.shippedOrders }}</div>
              <div class="summary-label">待收货</div>
            </div></el-col>
            <el-col :span="4"><div class="summary-item">
              <div class="summary-number finished">{{ orderSummary.finishedOrders }}</div>
              <div class="summary-label">已完成</div>
            </div></el-col>
            <el-col :span="4"><div class="summary-item">
              <div class="summary-number cancelled">{{ orderSummary.cancelledOrders }}</div>
              <div class="summary-label">已取消</div>
            </div></el-col>
          </el-row>
        </div>
        
        <!-- 订单列表 -->
        <div class="orders-list">
          <div v-if="orders.length === 0" class="empty-orders">
            <el-empty description="暂无订单"><el-button type="primary" @click="$router.push('/products')">去购物</el-button></el-empty>
          </div>
          <div v-else>
            <div v-for="order in orders" :key="order.id" class="order-item">
              <div class="order-header">
                <div class="order-info">
                  <span class="order-no">订单号：{{ order.orderNo }}</span>
                  <span class="order-time">{{ formatDate(order.createdAt) }}</span>
                </div>
                <div class="order-status">
                  <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
                </div>
              </div>
              <div class="order-bar" :class="'bar-'+order.status"></div>
              <div class="order-content">
                <div v-for="item in order.orderItems" :key="item.id" class="order-product">
                  <img :src="item.product && item.product.coverImage ? normalizeProductImageUrl(item.product.coverImage) : '/placeholder-product.png'" :alt="item.product ? item.product.name : item.productNameSnapshot" class="product-image"/>
                  <div class="product-info">
                    <h4>{{ item.product ? item.product.name : item.productNameSnapshot }}</h4>
                    <p v-if="item.product && item.product.subtitle">{{ item.product.subtitle }}</p>
                    <p class="product-price">¥{{ item.priceSnapshot }} × {{ item.quantity }}</p>
                    <div class="review-status" v-if="order.status === 'FINISHED'">
                      <el-tag :type="item.hasReview ? 'success' : 'warning'" size="small">{{ item.hasReview ? '已评价' : '未评价' }}</el-tag>
                    </div>
                  </div>
                </div>
              </div>
              <div class="order-footer">
                <div class="order-address">收货地址：{{ order.receiverAddress }}</div>
                <div class="order-total">总计：¥{{ order.totalAmount }}</div>
                <div class="order-actions">
                  <el-button v-if="order.status === 'UNPAID'" type="warning" size="small" @click="handleGoToPayment(order)">去支付</el-button>
                  <el-button v-if="order.status === 'SHIPPED'" type="success" size="small" @click="handleConfirmReceive(order)">确认收货</el-button>
                  <el-button v-if="order.status === 'UNPAID'" type="danger" size="small" @click="handleCancelOrder(order)">取消订单</el-button>
                  <el-button type="info" size="small" @click="viewOrderDetail(order)">查看详情</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 分页 -->
        <div class="pagination-section" v-if="orders.length > 0">
          <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :total="pagination.total" :page-sizes="[5, 10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
        </div>
      </div>
    </el-main>
  </div>

  <!-- 智能客服组件 -->
  <ChatWidget />
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { useUserStore } from '@/stores/user'
import { getMyOrders, cancelOrder, confirmReceive, mockPay, getOrderSummary } from '@/api/order'
import type { Order, OrderSummary } from '@/api/order'
import { normalizeProductImageUrl } from '@/api/upload'
import ChatWidget from '@/components/ChatWidget.vue'
  
  const router = useRouter()
  const userStore = useUserStore()
  
  const orders = ref<Order[]>([])
  const orderSummary = ref<OrderSummary | null>(null)
  const filters = reactive<{ status: 'ALL' | 'UNPAID' | 'PAID' | 'SHIPPED' | 'FINISHED' | 'CLOSED' }>({ status: 'ALL' })
  
  const pagination = reactive({
    page: 1,
    size: 10,
    total: 0
  })
  
  // 加载订单列表
  const loadOrders = async () => {
    try {
      const response = await getMyOrders({
        page: pagination.page,
        size: pagination.size,
        status: filters.status === 'ALL' ? undefined : filters.status
      })
      orders.value = response.data.list || []
      pagination.total = response.data.total || 0
    } catch (error) {
      console.error('加载订单失败:', error)
      ElMessage.error('加载订单失败')
    }
  }
  
  // 加载订单统计
  const loadOrderSummary = async () => {
    try {
      const response = await getOrderSummary()
      orderSummary.value = response.data
    } catch (error) {
      console.error('加载订单统计失败:', error)
    }
  }
  
  // 处理支付
  const handlePay = async (order: Order) => {
    try {
      await ElMessageBox.confirm('确定要支付这个订单吗？', '确认支付', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      await mockPay(order.orderNo)
      ElMessage.success('支付成功')
      await loadOrders()
      await loadOrderSummary()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('支付失败:', error)
        ElMessage.error('支付失败')
      }
    }
  }
  
  // 处理确认收货
  const handleConfirmReceive = async (order: Order) => {
    try {
      await ElMessageBox.confirm('确定已收到商品吗？', '确认收货', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      await confirmReceive(order.id)
      ElMessage.success('确认收货成功')
      await loadOrders()
      await loadOrderSummary()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('确认收货失败:', error)
        ElMessage.error('确认收货失败')
      }
    }
  }
  
  // 处理取消订单
  const handleCancelOrder = async (order: Order) => {
    try {
      await ElMessageBox.confirm('确定要取消这个订单吗？', '确认取消', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      await cancelOrder(order.id)
      ElMessage.success('订单已取消')
      await loadOrders()
      await loadOrderSummary()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('取消订单失败:', error)
        ElMessage.error('取消订单失败')
      }
    }
  }
  
  // 查看订单详情
  const viewOrderDetail = (order: Order) => {
    router.push(`/order/${order.id}`)
  }

  // 处理去支付
  const handleGoToPayment = (order: Order) => {
    router.push({
      path: '/payment',
      query: {
        orderNo: order.orderNo,
        totalAmount: String(order.totalAmount)
      }
    })
  }
  
  // 分页处理
  const handleSizeChange = (size: number) => {
    pagination.size = size
    pagination.page = 1
    loadOrders()
  }
  
  const handleCurrentChange = (page: number) => {
    pagination.page = page
    loadOrders()
  }

  // 状态切换
  const onStatusChange = () => {
    pagination.page = 1
    loadOrders()
  }
  
  // 格式化日期
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('zh-CN')
  }
  
  // 获取状态类型
  const getStatusType = (status: string) => {
    const statusMap: Record<string, string> = {
      'UNPAID': 'warning',
      'PAID': 'primary',
      'SHIPPED': 'success',
      'FINISHED': 'success',
      'CLOSED': 'info'
    }
    return statusMap[status] || 'info'
  }
  
  // 获取状态文本
  const getStatusText = (status: string) => {
    const statusMap: Record<string, string> = {
      'UNPAID': '待付款',
      'PAID': '待发货',
      'SHIPPED': '待收货',
      'FINISHED': '已完成',
      'CLOSED': '已取消'
    }
    return statusMap[status] || status
  }
  
  // 退出登录
  const handleLogout = () => {
    userStore.logout()
    ElMessage.success('退出成功')
    router.push('/login')
  }
  
  onMounted(() => {
    // 恢复登录状态，确保刷新页面后不会丢失登录状态
    userStore.initUser()
    
    loadOrders()
    loadOrderSummary()
  })
  </script>
  
  <style scoped>
  .orders-page { min-height: 100vh; background: linear-gradient(180deg,#fff9e6 0%,#fffef8 60%,#ffffff 100%); }
.header { background-color: #fffef8; box-shadow: 0 2px 4px rgba(0,0,0,.08); position: fixed; top: 0; left: 0; right: 0; z-index: 1000; border-bottom: 1px solid #fae5b2; }
.header-content { display: flex; align-items: center; justify-content: space-between; height: 100%; }
.logo h2 { margin: 0; color: #f7c948; }
.nav-menu { flex: 1; margin: 0 50px; }
.user-info { display: flex; align-items: center; gap: 10px; }
.main-content { margin-top: 60px; padding: 20px; }

.status-filter { margin-bottom: 12px; }

.order-summary { margin-bottom: 20px; padding: 16px; background:#fffdf2; border:1px solid #fae5b2; border-radius:12px; }
.summary-item { text-align: center; padding: 14px; background-color:#fff; border-radius:10px; box-shadow: 0 2px 8px rgba(0,0,0,.06); }
.summary-number { font-size: 22px; font-weight: 800; margin-bottom: 4px; }
.summary-number.all { color:#b27a00; }
.summary-number.unpaid { color:#e6a23c; }
.summary-number.paid { color:#409eff; }
.summary-number.shipped { color:#67c23a; }
.summary-number.finished { color:#67c23a; }
.summary-number.cancelled { color:#f56c6c; }
.summary-label { font-size: 13px; color:#666; }

.order-item { background-color: #fff; border-radius: 12px; margin-bottom: 20px; box-shadow: 0 4px 16px rgba(0,0,0,.06); overflow: hidden; border:1px solid #f0e9cc; }
.order-header { display: flex; justify-content: space-between; align-items: center; padding: 14px 18px; background-color: #fffef8; border-bottom: 1px solid #f4edd6; }
.order-bar { height: 3px; }
.bar-UNPAID { background:#ffe08a; }
.bar-PAID { background:#a0c4ff; }
.bar-SHIPPED { background:#caffbf; }
.bar-FINISHED { background:#caffbf; }
.bar-CLOSED { background:#fde2e2; }
.order-info { display: flex; gap: 18px; }
.order-no { font-weight: 700; color:#333; }
.order-time { color:#666; font-size: 14px; }
.order-content { padding: 16px; }
.order-product { display: flex; gap: 12px; padding: 10px 0; border-bottom: 1px dashed #f0e9cc; }
.order-product:last-child { border-bottom: none; }
.product-image { width: 80px; height: 80px; object-fit: cover; border-radius: 8px; }
.product-info h4 { margin: 0 0 4px 0; color:#333; }
.product-info p { margin: 0 0 4px 0; color:#666; font-size: 14px; }
.product-price { color:#e67e22; font-weight: 700; }
.review-status { margin-top: 6px; }
.order-footer { display: flex; justify-content: space-between; align-items: center; padding: 14px 18px; background-color:#fffef8; border-top:1px solid #f4edd6; }
.order-address { flex: 1; color:#666; font-size: 14px; }
.order-total { font-size: 18px; font-weight: 800; color:#e67e22; margin: 0 12px; }
.order-actions { display: flex; gap: 10px; }

.pagination-section { display: flex; justify-content: center; margin-top: 18px; }
:deep(.el-button--warning){ --el-color-warning:#f7c948; --el-color-warning-light-9:#fff7da; }
</style>