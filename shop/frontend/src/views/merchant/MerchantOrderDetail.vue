<template>
  <div class="merchant-order-detail">
    <el-header class="header">
      <div class="header-content">
        <div class="logo"><h2>商家订单详情</h2></div>
        <el-menu mode="horizontal" :router="true" class="nav-menu">
          <el-menu-item index="/merchant">首页</el-menu-item>
          <el-menu-item index="/merchant/products">商品管理</el-menu-item>
          <el-menu-item index="/merchant/orders">订单管理</el-menu-item>
          <el-menu-item index="/merchant/categories">分类管理</el-menu-item>
        </el-menu>
      </div>
    </el-header>

    <el-main class="main-content">
      <div class="order-detail-container">
        <div class="back-section"><el-button @click="$router.push('/merchant/orders')" type="text" icon="ArrowLeft">返回订单列表</el-button></div>

        <div v-if="loading" class="loading-section"><el-skeleton :rows="10" animated /></div>

        <div v-else-if="order" class="order-detail">
          <div class="order-header">
            <div class="order-basic-info">
              <h2>订单详情</h2>
              <div class="order-no">订单号：{{ order.orderNo }}</div>
              <div class="order-time">下单时间：{{ formatDate(order.createdAt) }}</div>
            </div>
            <div class="order-status"><el-tag :type="getStatusType(order.status)" size="large">{{ getStatusText(order.status) }}</el-tag></div>
          </div>

          <div class="progress-section">
            <el-steps :active="statusStepIndex(order.status)" :process-status="order.status==='CLOSED' ? 'error' : 'process'" finish-status="success" align-center>
              <el-step title="待付款"/>
              <el-step title="待发货"/>
              <el-step title="已发货"/>
              <el-step title="已完成"/>
              <el-step title="已关闭"/>
            </el-steps>
          </div>

          <!-- 追加轨迹对话框 -->
          <el-dialog v-model="addTrackVisible" title="追加物流轨迹" width="520px">
            <el-form label-width="90px">
              <el-form-item label="位置"><el-input v-model="addTrackForm.location" placeholder="如：配送站、转运中心" /></el-form-item>
              <el-form-item label="描述"><el-input v-model="addTrackForm.description" type="textarea" :rows="3" placeholder="轨迹说明" /></el-form-item>
              <el-form-item label="时间"><el-date-picker v-model="addTrackForm.trackTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="不填默认当前时间" /></el-form-item>
            </el-form>
            <template #footer><el-button @click="addTrackVisible=false">取消</el-button><el-button type="primary" :loading="addTrackLoading" @click="submitAddTrack">提交</el-button></template>
          </el-dialog>

          <div class="buyer-section"><div class="section-title"><span class="bar"></span>买家信息</div>
            <div class="buyer-info"><div class="info-item"><span class="label">买家ID：</span><span class="value">{{ order.userId }}</span></div><div class="info-item"><span class="label">联系电话：</span><span class="value">{{ order.receiverPhone }}</span></div></div>
          </div>

          <div class="order-items-section"><div class="section-title"><span class="bar"></span>商品信息</div>
            <div v-if="!order.orderItems || order.orderItems.length === 0" class="empty-items"><el-empty description="暂无商品信息" /></div>
            <div v-else class="order-items">
              <div v-for="item in order.orderItems" :key="item.id" class="order-item"><div class="item-image"><img :src="item.product?.coverImage || '/placeholder.png'" :alt="item.productNameSnapshot"></div><div class="item-info"><h4>{{ item.productNameSnapshot }}</h4><p class="item-price">¥{{ item.priceSnapshot }} × {{ item.quantity }}</p></div><div class="item-total">¥{{ item.subtotal }}</div></div>
            </div>
          </div>

          <div class="delivery-section"><div class="section-title"><span class="bar"></span>收货信息</div>
            <div class="delivery-info"><div class="info-item"><span class="label">收货人：</span><span class="value">{{ order.receiverName }}</span></div><div class="info-item"><span class="label">联系电话：</span><span class="value">{{ order.receiverPhone }}</span></div><div class="info-item"><span class="label">收货地址：</span><span class="value">{{ order.receiverAddress }}</span></div></div>
          </div>

          <div class="amount-section"><div class="section-title"><span class="bar"></span>订单金额</div>
            <div class="amount-info"><div class="amount-item"><span class="label">商品总额：</span><span class="value">¥{{ order.totalAmount }}</span></div><div class="amount-item total"><span class="label">实付金额：</span><span class="value">¥{{ order.totalAmount }}</span></div></div>
          </div>

          <div v-if="order.shipment" class="logistics-section"><div class="section-title"><span class="bar"></span>物流信息</div>
            <div class="logistics-info"><div class="info-item"><span class="label">物流公司：</span><span class="value">{{ order.shipment.carrier }}</span></div><div class="info-item"><span class="label">物流单号：</span><span class="value">{{ order.shipment.trackingNo }}</span></div><div class="info-item"><span class="label">发货时间：</span><span class="value">{{ formatDate(order.shipment.shippedAt) }}</span></div><div class="info-item" v-if="order.shipment.senderName"><span class="label">发货人：</span><span class="value">{{ order.shipment.senderName }}</span></div><div class="info-item" v-if="order.shipment.senderPhone"><span class="label">联系电话：</span><span class="value">{{ order.shipment.senderPhone }}</span></div><div class="info-item" v-if="order.shipment.shippingAddress"><span class="label">发货地址：</span><span class="value">{{ order.shipment.shippingAddress }}</span></div></div>
            <div v-if="logisticsTracks.length > 0" class="logistics-tracks"><h4>物流轨迹</h4><el-timeline><el-timeline-item v-for="track in logisticsTracks" :key="track.id" :timestamp="formatDate(track.trackTime)" placement="top"><div class="track-item"><div class="track-location">{{ track.location }}</div><div class="track-description">{{ track.description }}</div></div></el-timeline-item></el-timeline><el-button type="primary" @click="openAddTrack" style="margin-top:10px">追加轨迹</el-button></div>
          </div>

          <div v-if="order.status === 'PAID' && !order.shipment" class="ship-form-section"><div class="section-title"><span class="bar"></span>发货信息</div>
            <el-form :model="shipmentForm" label-width="100px" style="max-width:520px"><el-form-item label="地址簿"><el-select v-model="selectedAddressId" placeholder="从地址簿选择发货地址" @change="onPickAddress"><el-option v-for="addr in addressBook" :key="addr.id" :label="`${addr.receiverName} ${addr.phone} | ${addr.province} ${addr.city} ${addr.district} ${addr.detail}`" :value="addr.id" /></el-select><el-button v-if="selectedAddressId" style="margin-left:8px" @click="onFillAddress">一键填充</el-button></el-form-item><el-form-item label="发货人"><el-input v-model="shipmentForm.senderName" placeholder="请输入发货人姓名" /></el-form-item><el-form-item label="联系电话"><el-input v-model="shipmentForm.senderPhone" placeholder="请输入联系电话" /></el-form-item><el-form-item label="发货地址"><el-input v-model="shipmentForm.senderAddress" placeholder="请输入发货地址（省市区+详细）" /></el-form-item><el-form-item label="物流公司"><el-select v-model="shipmentForm.carrier" placeholder="请选择物流公司"><el-option label="顺丰速运" value="顺丰速运" /><el-option label="中通快递" value="中通快递" /><el-option label="圆通速递" value="圆通速递" /><el-option label="申通快递" value="申通快递" /><el-option label="京东物流" value="京东物流" /></el-select></el-form-item><el-form-item label="运单号"><el-input v-model="shipmentForm.trackingNo" placeholder="请输入快递单号" /></el-form-item><el-form-item><el-button type="primary" :loading="shipping" @click="handleShip">确认发货</el-button></el-form-item></el-form>
          </div>

          <div v-if="order.remark" class="remark-section"><div class="section-title"><span class="bar"></span>订单备注</div><div class="remark-content">{{ order.remark }}</div></div>

          <div class="order-actions"><el-button v-if="order.status === 'UNPAID'" type="danger" size="large" @click="handleCloseOrder">关闭订单</el-button><el-button type="info" size="large" @click="$router.push('/merchant/orders')">返回列表</el-button></div>
        </div>

        <div v-else class="error-section"><el-empty description="订单不存在或已被删除"><el-button type="primary" @click="$router.push('/merchant/orders')">返回订单列表</el-button></el-empty></div>
      </div>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetail } from '@/api/order'
import { shipOrder, addMerchantTrack } from '@/api/merchant'
import { getLogisticsTracks } from '@/api/order'
import type { Order, LogisticsTrack } from '@/api/order'
import { getAddresses, type Address } from '@/api/address'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const order = ref<Order | null>(null)
const loading = ref(true)
const shipping = ref(false)
const logisticsTracks = ref<LogisticsTrack[]>([])
const addressBook = ref<Address[]>([])
const selectedAddressId = ref<number | null>(null)

// 发货表单
const shipmentForm = ref({
  carrier: '',
  trackingNo: '',
  senderName: '',
  senderPhone: '',
  senderAddress: ''
})

// 加载订单详情
const loadOrderDetail = async () => {
  try {
    const orderId = parseInt(route.params.id as string)
    const response = await getOrderDetail(orderId)
    order.value = response.data
    console.log('订单详情数据:', response.data)
    
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

// 加载商家地址簿（沿用用户地址接口，商家账号即用户）
const loadAddressBook = async () => {
  try {
    const { data } = await getAddresses()
    addressBook.value = data || []
  } catch (e) {
    console.error('加载地址簿失败', e)
  }
}

const onPickAddress = () => {
  // 不自动覆盖，提供“一键填充”按钮，避免误操作
}

const onFillAddress = () => {
  const addr = addressBook.value.find(a => a.id === selectedAddressId.value)
  if (!addr) return
  shipmentForm.value.senderName = addr.receiverName
  shipmentForm.value.senderPhone = addr.phone
  shipmentForm.value.senderAddress = `${addr.province} ${addr.city} ${addr.district} ${addr.detail}`
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

// 追加轨迹
const addTrackVisible = ref(false)
const addTrackForm = ref({ location: '', description: '', trackTime: '' })
const addTrackLoading = ref(false)

const openAddTrack = () => {
  addTrackForm.value = { location: '', description: '', trackTime: '' }
  addTrackVisible.value = true
}

const submitAddTrack = async () => {
  if (!order.value?.shipment) return
  if (!addTrackForm.value.location || !addTrackForm.value.description) {
    ElMessage.warning('请填写位置和描述')
    return
  }
  addTrackLoading.value = true
  try {
    await addMerchantTrack(order.value.shipment.id, {
      location: addTrackForm.value.location,
      description: addTrackForm.value.description,
      trackTime: addTrackForm.value.trackTime || undefined
    })
    ElMessage.success('已追加轨迹')
    await loadLogisticsTracks(order.value.shipment.id)
    addTrackVisible.value = false
  } catch (e) {
    ElMessage.error('追加失败')
  } finally {
    addTrackLoading.value = false
  }
}

// 处理发货
const handleShip = async () => {
  if (!order.value || !shipmentForm.value.carrier || !shipmentForm.value.trackingNo || !shipmentForm.value.senderName || !shipmentForm.value.senderAddress) {
    ElMessage.warning('请填写完整的发货信息')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要发货这个订单吗？', '确认发货', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    shipping.value = true
    await shipOrder(order.value.id, {
      carrier: shipmentForm.value.carrier,
      trackingNo: shipmentForm.value.trackingNo,
      senderName: shipmentForm.value.senderName,
      senderPhone: shipmentForm.value.senderPhone,
      senderAddress: shipmentForm.value.senderAddress
    })
    ElMessage.success('发货成功')
    await loadOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发货失败:', error)
      ElMessage.error('发货失败')
    }
  } finally {
    shipping.value = false
  }
}

// 处理关闭订单
const handleCloseOrder = async () => {
  if (!order.value) return
  
  try {
    await ElMessageBox.confirm('确定要关闭这个订单吗？', '确认关闭', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // TODO: 调用关闭订单API
    ElMessage.success('订单已关闭')
    await loadOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('关闭订单失败:', error)
      ElMessage.error('关闭订单失败')
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
    'SHIPPED': '已发货',
    'FINISHED': '已完成',
    'CLOSED': '已关闭'
  }
  return statusMap[status] || status
}

const statusStepIndex = (status: string) => { const map: Record<string, number> = { UNPAID: 0, PAID: 1, SHIPPED: 2, FINISHED: 3, CLOSED: 4 }; return typeof map[status] === 'number' ? map[status] : 0 }

onMounted(() => {
  // 恢复登录状态，确保刷新页面后不会丢失登录状态
  userStore.initUser()
  
  loadOrderDetail()
  loadAddressBook()
})
</script>

<style scoped>
.merchant-order-detail{ --brand:#f7c948; --brand-light:#fdf3d7; --brand-weak:#fff7da; min-height:100vh; background:linear-gradient(180deg,#fff9e6 0%,#fffef8 60%,#ffffff 100%); }
.header{ background:#fffef8; box-shadow:0 2px 4px rgba(0,0,0,.08); position:fixed; top:0; left:0; right:0; z-index:1000; border-bottom:1px solid #fae5b2; }
.header-content{ display:flex; align-items:center; justify-content:space-between; height:100% }
.logo h2{ margin:0; color:#f7c948 }
.nav-menu{ flex:1; margin:0 50px }
.main-content{ margin-top:60px; padding:20px }
.order-detail-container{ max-width:1000px; margin:0 auto }
.back-section{ margin-bottom:16px }
.loading-section{ padding:40px }
.order-detail{ background:#fff; border-radius:12px; box-shadow:0 4px 16px rgba(0,0,0,.06); overflow:hidden; border:1px solid #f0e9cc }
.order-header{ display:flex; justify-content:space-between; align-items:flex-start; padding:24px 28px; background:#fffef8; border-bottom:1px solid #f4edd6 }
.order-basic-info h2{ margin:0 0 12px 0; color:#333 }
.order-no{ font-size:16px; color:#666; margin-bottom:6px }
.order-time{ font-size:14px; color:#999 }
.order-status{ text-align:right }
.progress-section{ padding:16px 24px; background:#fffdf2; border-bottom:1px solid #f4edd6 }
.section-title{ display:flex; align-items:center; gap:8px; font-weight:600; color:#303133; margin-bottom:12px }
.section-title .bar{ width:6px; height:16px; background:var(--brand); border-radius:3px; box-shadow:0 2px 6px rgba(0,0,0,.1) }
.buyer-section,.order-items-section,.delivery-section,.amount-section,.shipment-section,.logistics-section,.remark-section{ padding:20px 24px; border-bottom:1px solid #eee }
.buyer-info,.delivery-info,.amount-info,.logistics-info{ display:flex; flex-direction:column; gap:12px }
.info-item,.amount-item{ display:flex; align-items:center }
.label{ width:100px; color:#666; font-size:14px }
.value{ color:#333; font-size:14px }
.order-items{ display:flex; flex-direction:column; gap:16px }
.order-item{ display:flex; align-items:center; gap:16px; padding:16px; background:#fffef8; border-radius:10px; border:1px solid #f4edd6 }
.item-image img{ width:80px; height:80px; object-fit:cover; border-radius:8px }
.item-info{ flex:1 }
.item-info h4{ margin:0 0 6px 0; color:#333; font-size:16px }
.item-price{ margin:0; color:#e67e22; font-weight:700 }
.item-total{ font-size:18px; font-weight:800; color:#e67e22 }
.amount-item.total{ font-weight:800; font-size:16px }
.amount-item.total .value{ color:#e67e22; font-size:18px }
.remark-content{ color:#666; font-size:14px; line-height:1.6 }
.logistics-tracks h4{ margin:12px 0; color:#333; font-size:16px }
.track-item{ padding:8px 0 }
.track-location{ font-weight:bold; color:#409eff; margin-bottom:4px }
.track-description{ color:#666; font-size:14px }
.ship-form-section{ padding:20px 24px; border-bottom:1px solid #eee }
.order-actions{ padding:20px 24px; display:flex; gap:12px; justify-content:center }
</style>

