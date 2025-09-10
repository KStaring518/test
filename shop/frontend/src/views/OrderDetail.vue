<template>
  <div class="order-detail-page">
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
      <div class="order-detail-container">
        <!-- 返回按钮 -->
        <div class="back-section">
          <el-button @click="$router.push('/orders')" type="text" icon="ArrowLeft">返回订单列表</el-button>
        </div>

        <div v-if="loading" class="loading-section"><el-skeleton :rows="10" animated /></div>

        <div v-else-if="order" class="order-detail">
          <!-- 订单头部信息 -->
          <div class="order-header">
            <div class="order-basic-info">
              <h2>订单详情</h2>
              <div class="order-no">订单号：{{ order.orderNo }}</div>
              <div class="order-time">下单时间：{{ formatDate(order.createdAt) }}</div>
            </div>
            <div class="order-status">
              <el-tag :type="getStatusType(order.status)" size="large">{{ getStatusText(order.status) }}</el-tag>
            </div>
          </div>

          <!-- 状态进度条 -->
          <div class="progress-section">
            <el-steps :active="statusStepIndex(order.status)" :process-status="order.status==='CLOSED' ? 'error' : 'process'" finish-status="success" align-center>
              <el-step title="待付款"/>
              <el-step title="待发货"/>
              <el-step title="待收货"/>
              <el-step title="已完成"/>
              <el-step title="已取消"/>
            </el-steps>
          </div>

          <!-- 订单商品列表 -->
          <div class="order-items-section">
            <div class="section-title"><span class="bar"></span>商品信息</div>
            <div v-if="!order.orderItems || order.orderItems.length === 0" class="empty-items"><el-empty description="暂无商品信息" /></div>
            <div v-else class="order-items">
              <div v-for="item in order.orderItems" :key="item.id" class="order-item">
                <div class="item-image">
                  <img :src="item.product.coverImage" :alt="item.product.name">
                </div>
                <div class="item-info">
                  <h4>{{ item.product.name }}</h4>
                  <p class="item-subtitle">{{ item.product.subtitle }}</p>
                  <p class="item-price">¥{{ item.priceSnapshot }} × {{ item.quantity }}</p>
                </div>
                <div class="item-total">¥{{ item.subtotal }}</div>
                <div class="item-actions">
                  <el-button v-if="order.status === 'FINISHED'" :type="item.hasReview ? 'success' : 'primary'" size="small" @click="item.hasReview ? toggleReviews(item) : openReview(item)">{{ item.hasReview ? '查看评价' : '去评价' }}</el-button>
                  <el-button v-if="!item.hasReview" type="info" size="small" @click="toggleReviews(item)">{{ showReviews[item.id] ? '收起评价' : '查看评价' }}</el-button>
                </div>
                <div v-if="showReviews[item.id]" class="reviews-panel">
                  <div v-if="reviewsLoading[item.id]" style="padding:8px 0"><el-skeleton :rows="2" animated /></div>
                  <div v-else>
                    <el-empty v-if="(reviewsMap[item.id]||[]).length===0" description="暂无评价" />
                    <div v-else class="review-item" v-for="rv in reviewsMap[item.id]" :key="rv.id">
                      <div class="review-header">
                        <el-rate :model-value="rv.rating" :max="5" disabled />
                        <span class="reviewer-name">{{ rv.isAnonymous ? '匿名用户' : (rv.user?.nickname || rv.user?.username || '未知用户') }}</span>
                        <span class="review-time">{{ formatDate(rv.createdAt) }}</span>
                      </div>
                      <div class="review-content">{{ rv.content || '（无文字）' }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 收货信息 -->
          <div class="delivery-section">
            <div class="section-title"><span class="bar"></span>收货信息</div>
            <div class="delivery-info">
              <div class="info-item"><span class="label">收货人：</span><span class="value">{{ order.receiverName }}</span></div>
              <div class="info-item"><span class="label">联系电话：</span><span class="value">{{ order.receiverPhone }}</span></div>
              <div class="info-item"><span class="label">收货地址：</span><span class="value">{{ order.receiverAddress }}</span></div>
            </div>
          </div>

          <!-- 订单金额 -->
          <div class="amount-section">
            <div class="section-title"><span class="bar"></span>订单金额</div>
            <div class="amount-info">
              <div class="amount-item"><span class="label">商品总额：</span><span class="value">¥{{ order.totalAmount }}</span></div>
              <div class="amount-item total"><span class="label">实付金额：</span><span class="value">¥{{ order.totalAmount }}</span></div>
            </div>
          </div>

          <!-- 物流信息 -->
          <div v-if="order.shipment" class="logistics-section">
            <div class="section-title"><span class="bar"></span>物流信息</div>
            <div class="logistics-info">
              <div class="info-item"><span class="label">物流公司：</span><span class="value">{{ order.shipment.carrier }}</span></div>
              <div class="info-item"><span class="label">物流单号：</span><span class="value">{{ order.shipment.trackingNo }}</span></div>
              <div class="info-item"><span class="label">发货时间：</span><span class="value">{{ formatDate(order.shipment.shippedAt) }}</span></div>
            </div>
            <div v-if="logisticsTracks.length > 0" class="logistics-tracks">
              <h4>物流轨迹</h4>
              <el-timeline>
                <el-timeline-item v-for="track in logisticsTracks" :key="track.id" :timestamp="formatDate(track.trackTime)" placement="top">
                  <div class="track-item">
                    <div class="track-location">{{ track.location }}</div>
                    <div class="track-description">{{ track.description }}</div>
                  </div>
                </el-timeline-item>
              </el-timeline>
            </div>
          </div>

          <!-- 订单备注 -->
          <div v-if="order.remark" class="remark-section">
            <div class="section-title"><span class="bar"></span>订单备注</div>
            <div class="remark-content">{{ order.remark }}</div>
          </div>

          <!-- 订单操作 -->
          <div class="order-actions">
            <el-button v-if="order.status === 'UNPAID'" type="warning" size="large" @click="handleGoToPayment">去支付</el-button>
            <el-button v-if="order.status === 'SHIPPED' && isOrderOwner" type="success" size="large" @click="handleConfirmReceive">确认收货</el-button>
            <el-button v-if="order.status === 'UNPAID'" type="danger" size="large" @click="handleCancelOrder">取消订单</el-button>
            <el-button type="info" size="large" @click="$router.push('/orders')">返回列表</el-button>
          </div>
        </div>

        <div v-else class="error-section">
          <el-empty description="订单不存在或已被删除"><el-button type="primary" @click="$router.push('/orders')">返回订单列表</el-button></el-empty>
        </div>
      </div>
      <!-- 评价弹窗 -->
      <el-dialog v-model="reviewVisible" title="发表评价" width="520px">
        <el-form label-width="80px">
          <el-form-item label="评分"><el-rate v-model="reviewForm.rating" :max="5" /></el-form-item>
          <el-form-item label="匿名"><el-switch v-model="reviewForm.anonymous" /></el-form-item>
          <el-form-item label="内容"><el-input v-model="reviewForm.content" type="textarea" :rows="4" placeholder="说说你的使用感受吧" /></el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="reviewVisible=false">取消</el-button>
          <el-button type="primary" :loading="reviewSubmitting" @click="submitReview">提交</el-button>
        </template>
      </el-dialog>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getOrderDetail, cancelOrder, confirmReceive, mockPay, getLogisticsTracks } from '@/api/order'
import type { Order, LogisticsTrack, OrderItem as OrderItemType } from '@/api/order'
import { createReview, listReviewsByProduct } from '@/api/review'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const order = ref<Order | null>(null)
const loading = ref(true)
const isOrderOwner = ref(false)
const logisticsTracks = ref<LogisticsTrack[]>([])

// 评价弹窗
const reviewVisible = ref(false)
const reviewSubmitting = ref(false)
const reviewForm = ref({ orderItemId: 0, rating: 5, content: '', anonymous: false })

// 查看评价状态
const showReviews = ref<Record<number, boolean>>({})
const reviewsMap = ref<Record<number, any[]>>({})
const reviewsLoading = ref<Record<number, boolean>>({})

// 加载订单详情
const loadOrderDetail = async () => {
  try {
    const orderId = parseInt(route.params.id as string)
    const response = await getOrderDetail(orderId)
    order.value = response.data
    // 检查当前用户是否为订单所有者
    isOrderOwner.value = Number(order.value.userId) === Number(userStore.user?.id)
    console.log('订单详情数据:', response.data)
    console.log('订单项数量:', response.data.orderItems?.length || 0)
    
    // 如果有物流信息，加载物流轨迹
    if (order.value.shipment) {
      await loadLogisticsTracks(order.value.shipment.id)
    }
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
  } finally {
    loading.value = false
  }
}

// 加载物流轨迹
const loadLogisticsTracks = async (shipmentId: number) => {
  try {
    const response = await getLogisticsTracks(shipmentId)
    logisticsTracks.value = response.data
    console.log('物流轨迹数据:', response.data)
  } catch (error) {
    console.error('加载物流轨迹失败:', error)
    ElMessage.error('加载物流轨迹失败')
  }
}

// 处理支付
const handlePay = async () => {
  if (!order.value) return
  
  try {
    await ElMessageBox.confirm('确定要支付这个订单吗？', '确认支付', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await mockPay(order.value.orderNo)
    ElMessage.success('支付成功')
    await loadOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('支付失败:', error)
      ElMessage.error('支付失败')
    }
  }
}

// 处理去支付
const handleGoToPayment = () => {
  if (!order.value) return
  router.push({
    path: '/payment',
    query: {
      orderNo: order.value.orderNo,
      totalAmount: String(order.value.totalAmount)
    }
  })
}

// 处理确认收货
const handleConfirmReceive = async () => {
  if (!order.value) return
  
  try {
    await ElMessageBox.confirm('确定已收到商品吗？', '确认收货', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await confirmReceive(order.value.id)
    ElMessage.success('确认收货成功')
    await loadOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error)
      ElMessage.error('确认收货失败')
    }
  }
}

// 处理取消订单
const handleCancelOrder = async () => {
  if (!order.value) return
  
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '确认取消', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await cancelOrder(order.value.id)
    ElMessage.success('订单已取消')
    await loadOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
    }
  }
}

// 打开评价弹窗
const openReview = (item: OrderItemType) => {
  reviewForm.value = { orderItemId: item.id, rating: 5, content: '', anonymous: false }
  reviewVisible.value = true
}

// 提交评价
const submitReview = async () => {
  try {
    if (reviewForm.value.rating < 1 || reviewForm.value.rating > 5) {
      ElMessage.warning('评分需在1-5之间')
      return
    }
    reviewSubmitting.value = true
    await createReview({
      orderItemId: reviewForm.value.orderItemId,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content,
      anonymous: reviewForm.value.anonymous
    })
    ElMessage.success('评价已提交')
    reviewVisible.value = false
    // 本地更新对应订单项的评价状态，避免再次提交导致重复
    if (order.value && Array.isArray(order.value.orderItems)) {
      const idx = order.value.orderItems.findIndex(it => it.id === reviewForm.value.orderItemId)
      if (idx !== -1) {
        // @ts-ignore 后端DTO包含hasReview字段
        order.value.orderItems[idx].hasReview = true
      }
    }
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    reviewSubmitting.value = false
  }
}

// 切换并加载评价
const toggleReviews = async (item: OrderItemType) => {
  const id = item.id
  showReviews.value[id] = !showReviews.value[id]
  if (showReviews.value[id] && !reviewsMap.value[id]) {
    reviewsLoading.value[id] = true
    try {
      const res = await listReviewsByProduct(item.product.id)
      reviewsMap.value[id] = res.data || []
    } catch (e) {
      ElMessage.error('加载评价失败')
      reviewsMap.value[id] = []
    } finally {
      reviewsLoading.value[id] = false
    }
  }
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

// 在现有逻辑基础上新增一个状态到步进条的映射函数
const statusStepIndex = (status: string) => {
  const map: Record<string, number> = { UNPAID: 0, PAID: 1, SHIPPED: 2, FINISHED: 3, CLOSED: 4 }
  return typeof map[status] === 'number' ? map[status] : 0
}

onMounted(() => {
  // 恢复登录状态，确保刷新页面后不会丢失登录状态
  userStore.initUser()
  
  loadOrderDetail()
})
</script>

<style scoped>
.order-detail-page { min-height: 100vh; background: linear-gradient(180deg,#fff9e6 0%,#fffef8 60%,#ffffff 100%); }
.header { background-color:#fffef8; box-shadow:0 2px 4px rgba(0,0,0,.08); position:fixed; top:0; left:0; right:0; z-index:1000; border-bottom:1px solid #fae5b2; }
.header-content { display:flex; align-items:center; justify-content:space-between; height:100%; }
.logo h2 { margin:0; color:#f7c948; }
.nav-menu { flex:1; margin:0 50px; }
.user-info { display:flex; align-items:center; gap:10px; }
.main-content { margin-top:60px; padding:20px; }
.order-detail-container { max-width: 1000px; margin: 0 auto; }
.back-section { margin-bottom: 16px; }
.loading-section { padding: 40px; }
.order-detail { background:#fff; border-radius:12px; box-shadow:0 4px 16px rgba(0,0,0,.06); overflow:hidden; border:1px solid #f0e9cc; }
.order-header { display:flex; justify-content:space-between; align-items:flex-start; padding:24px 28px; background:#fffef8; border-bottom:1px solid #f4edd6; }
.order-basic-info h2 { margin:0 0 12px 0; color:#333; }
.order-no { font-size:16px; color:#666; margin-bottom:6px; }
.order-time { font-size:14px; color:#999; }
.order-status { text-align:right; }
.progress-section { padding: 16px 24px; background:#fffdf2; border-bottom:1px solid #f4edd6; }

.section-title { display:flex; align-items:center; gap:8px; font-weight:600; color:#303133; margin-bottom:14px; }
.section-title .bar { width:6px; height:18px; background:#f7c948; border-radius:3px; box-shadow:0 2px 6px rgba(0,0,0,.1); }

.order-items-section, .delivery-section, .amount-section, .logistics-section, .remark-section { padding:20px 24px; border-bottom:1px solid #eee; }
.order-items { display:flex; flex-direction:column; gap:16px; }
.order-item { display:flex; align-items:center; gap:16px; padding:16px; background:#fffef8; border-radius:10px; border:1px solid #f4edd6; }
.item-image img { width:80px; height:80px; object-fit:cover; border-radius:8px; }
.item-info { flex:1; }
.item-info h4 { margin:0 0 6px 0; color:#333; font-size:16px; }
.item-subtitle { margin:0 0 6px 0; color:#666; font-size:14px; }
.item-price { margin:0; color:#e67e22; font-weight:700; }
.item-total { font-size:18px; font-weight:800; color:#e67e22; }
.item-actions { margin-left:auto; display:flex; gap:8px; }
.reviews-panel { margin-top:12px; padding:12px; background:#fffdf2; border-radius:8px; border:1px dashed #fae5b2; }
.review-item { padding:10px 0; border-bottom:1px solid #eee; }
.review-item:last-child { border-bottom:none; }
.review-header { display:flex; align-items:center; gap:10px; margin-bottom:8px; }
.reviewer-name { font-weight:bold; color:#e67e22; font-size:14px; }
.review-time { color:#999; font-size:12px; margin-left:auto; }
.review-content { color:#333; font-size:14px; line-height:1.5; }
.delivery-info, .amount-info { display:flex; flex-direction:column; gap:12px; }
.info-item, .amount-item { display:flex; align-items:center; }
.label { width:100px; color:#666; font-size:14px; }
.value { color:#333; font-size:14px; }
.amount-item.total { font-size:18px; font-weight:800; color:#e67e22; border-top:1px solid #eee; padding-top:12px; margin-top:12px; }
.remark-content { padding:12px; background:#fffdf2; border:1px dashed #fae5b2; border-radius:8px; color:#666; font-size:14px; }
.logistics-info { display:flex; flex-direction:column; gap:12px; margin-bottom:16px; }
.logistics-tracks h4 { margin:12px 0; color:#333; font-size:16px; }
.track-item { padding:8px 0; }
.track-location { font-weight:bold; color:#409eff; margin-bottom:4px; }
.track-description { color:#666; font-size:14px; }
.order-actions { padding:20px 24px; display:flex; gap:12px; justify-content:center; }
.error-section { text-align:center; padding:60px 0; }
:deep(.el-button--warning){ --el-color-warning:#f7c948; --el-color-warning-light-9:#fff7da; }
</style>
