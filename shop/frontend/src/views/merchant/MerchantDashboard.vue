<template>
  <div class="merchant-dashboard">
    <!-- 导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo">
          <h2>商家后台管理系统</h2>
        </div>
        
        <el-menu mode="horizontal" :router="true" class="nav-menu">
          <el-menu-item index="/merchant">首页</el-menu-item>
          <el-menu-item index="/merchant/products">商品管理</el-menu-item>
          <el-menu-item index="/merchant/orders">订单管理</el-menu-item>
          <el-menu-item index="/merchant/settings">店铺设置</el-menu-item>
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
      <div class="dashboard-container">
        <h2 class="page-title">商家后台首页</h2>
        
        <!-- 统计卡片 -->
        <div class="stats-section">
          <el-row :gutter="16">
            <el-col :span="4"><el-card class="stat-card"><div class="stat-bar bar-all"></div><div class="stat-content"><div class="stat-number">{{ stats.totalProducts }}</div><div class="stat-label">商品总数</div></div></el-card></el-col>
            <el-col :span="4"><el-card class="stat-card"><div class="stat-bar bar-onsale"></div><div class="stat-content"><div class="stat-number">{{ stats.onSaleProducts }}</div><div class="stat-label">在售商品</div></div></el-card></el-col>
            <el-col :span="4"><el-card class="stat-card"><div class="stat-bar bar-orders"></div><div class="stat-content"><div class="stat-number">{{ stats.totalOrders }}</div><div class="stat-label">订单总数</div></div></el-card></el-col>
            <el-col :span="4"><el-card class="stat-card"><div class="stat-bar bar-pending"></div><div class="stat-content"><div class="stat-number">{{ stats.pendingOrders }}</div><div class="stat-label">待处理订单</div></div></el-card></el-col>
            <el-col :span="4"><el-card class="stat-card"><div class="stat-bar bar-shipped"></div><div class="stat-content"><div class="stat-number">{{ stats.shippedOrders }}</div><div class="stat-label">已发货订单</div></div></el-card></el-col>
            <el-col :span="4"><el-card class="stat-card"><div class="stat-bar bar-revenue"></div><div class="stat-content"><div class="stat-number">¥{{ stats.totalRevenue }}</div><div class="stat-label">总收入</div></div></el-card></el-col>
          </el-row>
          <el-row :gutter="16" style="margin-top: 16px;">
            <el-col :span="6"><el-card class="stat-card revenue-card"><div class="stat-content"><div class="stat-number">¥{{ stats.todayRevenue }}</div><div class="stat-label">今日收入</div></div></el-card></el-col>
          </el-row>
        </div>
        
        <!-- 趋势与Top榜 -->
        <div class="charts-section">
          <el-card class="chart-card">
            <template #header><div class="card-header"><span class="bar"></span>最近30天 销售额 / 订单数</div></template>
            <div id="merchant-trend" style="height:320px"></div>
          </el-card>
          <el-card class="chart-card">
            <template #header><div class="card-header"><span class="bar"></span>热门商品Top5</div></template>
            <div id="merchant-top" style="height:300px"></div>
          </el-card>
        </div>

        <!-- 快捷操作 -->
        <div class="quick-actions">
          <h3 class="section-title"><span class="bar"></span>快捷操作</h3>
          <el-row :gutter="16">
            <el-col :span="6"><el-card class="action-card" @click="$router.push('/merchant/products')"><div class="action-content"><el-icon size="40" color="#409eff"><Plus /></el-icon><div class="action-text">添加商品</div></div></el-card></el-col>
            <el-col :span="6"><el-card class="action-card" @click="$router.push('/merchant/orders')"><div class="action-content"><el-icon size="40" color="#67c23a"><Document /></el-icon><div class="action-text">查看订单</div></div></el-card></el-col>
            <el-col :span="6"><el-card class="action-card" @click="$router.push('/merchant/settings')"><div class="action-content"><el-icon size="40" color="#e6a23c"><Document /></el-icon><div class="action-text">店铺设置</div></div></el-card></el-col>
          </el-row>
        </div>
        
        <!-- 最近订单 -->
        <div class="recent-orders">
          <h3 class="section-title"><span class="bar"></span>最近订单</h3>
          <el-table :data="recentOrders" style="width: 100%">
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="user.nickname" label="买家" width="120" />
            <el-table-column prop="totalAmount" label="金额" width="100"><template #default="scope">¥{{ scope.row.totalAmount }}</template></el-table-column>
            <el-table-column prop="status" label="状态" width="100"><template #default="scope"><el-tag :type="getOrderStatusType(scope.row.status)">{{ getOrderStatusText(scope.row.status) }}</el-tag></template></el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="180" />
            <el-table-column label="操作" width="120"><template #default="scope"><el-button size="small" type="primary" @click="viewOrder(scope.row.id)">查看详情</el-button></template></el-table-column>
          </el-table>
        </div>
      </div>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Document } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getMerchantOrders, getMerchantStatistics, getMerchantTrend, getMerchantTopProducts } from '@/api/merchant'
import * as echarts from 'echarts'

const router = useRouter()
const userStore = useUserStore()

// 统计数据
const stats = ref({
  totalProducts: 0,
  onSaleProducts: 0,
  totalOrders: 0,
  pendingOrders: 0,
  shippedOrders: 0,
  totalRevenue: 0,
  todayRevenue: 0
})

// 最近订单
import type { Order } from '@/api/merchant'
const recentOrders = ref<Order[]>([])

// 加载统计数据
const loadStats = async () => {
  try {
    const { data } = await getMerchantStatistics()
    stats.value = data || {
      totalProducts: 0,
      onSaleProducts: 0,
      totalOrders: 0,
      pendingOrders: 0,
      shippedOrders: 0,
      totalRevenue: 0,
      todayRevenue: 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载最近订单（取前5条）
const loadRecentOrders = async () => {
  try {
    const { data } = await getMerchantOrders({ page: 1, size: 5 })
    recentOrders.value = data?.list || []
  } catch (error) {
    console.error('加载最近订单失败:', error)
  }
}

// 获取订单状态类型
const getOrderStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'UNPAID': 'warning',
    'PAID': 'primary',
    'SHIPPED': 'success',
    'FINISHED': 'success',
    'CLOSED': 'info'
  }
  return statusMap[status] || 'info'
}

// 获取订单状态文本
const getOrderStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'UNPAID': '待付款',
    'PAID': '已付款',
    'SHIPPED': '已发货',
    'FINISHED': '已完成',
    'CLOSED': '已关闭'
  }
  return statusMap[status] || status
}

// 查看订单详情
const viewOrder = (orderId: number) => {
  router.push(`/merchant/orders/${orderId}`)
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
  
  loadStats()
  loadRecentOrders()
  // 渲染商家趋势
  setTimeout(async () => {
    const trendEl = document.getElementById('merchant-trend') as HTMLDivElement
    if (trendEl) {
      const chart = echarts.init(trendEl)
      const { data } = await getMerchantTrend(30)
      chart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['GMV', '订单数'] },
        xAxis: { type: 'category', data: data.labels },
        yAxis: [{ type: 'value', name: 'GMV' }, { type: 'value', name: '订单数' }],
        series: [
          { name: 'GMV', type: 'line', data: data.gmv, yAxisIndex: 0, smooth: true },
          { name: '订单数', type: 'line', data: data.orders, yAxisIndex: 1, smooth: true }
        ]
      })
    }
    const topEl = document.getElementById('merchant-top') as HTMLDivElement
    if (topEl) {
      const chart = echarts.init(topEl)
      const { data } = await getMerchantTopProducts()
      chart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: data.labels },
        yAxis: { type: 'value' },
        series: [{ name: '销量', type: 'bar', data: data.values }]
      })
    }
  }, 0)
})
</script>

<style scoped>
.merchant-dashboard { min-height: 100vh; background: linear-gradient(180deg,#fff9e6 0%,#fffef8 60%,#ffffff 100%); }
.header { background-color:#fffef8; box-shadow:0 2px 4px rgba(0,0,0,.08); position:fixed; top:0; left:0; right:0; z-index:1000; border-bottom:1px solid #fae5b2; }
.header-content { display:flex; align-items:center; justify-content:space-between; height:100%; }
.logo h2 { margin:0; color:#f7c948; }
.nav-menu { flex:1; margin:0 50px; }
.user-info { display:flex; align-items:center; gap:10px; }
.main-content { margin-top:60px; padding:20px; }
.dashboard-container { max-width: 1200px; margin: 0 auto; }
.page-title { margin-bottom: 16px; color:#303133; }
.stats-section { margin-bottom: 24px; }
.stat-card { text-align:center; padding: 14px; border-radius:12px; border:1px solid #f0e9cc; box-shadow:0 2px 10px rgba(0,0,0,.04); overflow:hidden; }
.stat-bar { height:4px; margin:-14px -14px 12px -14px; }
.bar-all{ background:#a8dadc; }
.bar-onsale{ background:#67c23a; }
.bar-orders{ background:#a0c4ff; }
.bar-pending{ background:#ffe08a; }
.bar-shipped{ background:#caffbf; }
.bar-revenue{ background:#f7c948; }
.stat-number { font-size:28px; font-weight:800; color:#303133; margin-bottom:6px; }
.stat-label { color:#606266; font-size:13px; }
.revenue-card { background: linear-gradient(135deg,#f7c948 0%,#ffb703 100%); color:#4a2c00; border:1px solid #f0e9cc; }
.revenue-card .stat-number{ color:#4a2c00; }
.revenue-card .stat-label{ color:#6e4d00; }
.chart-card { margin-bottom: 16px; border-radius:12px; border:1px solid #f0e9cc; }
.card-header { display:flex; align-items:center; gap:8px; font-weight:600; color:#303133; }
.card-header .bar { width:6px; height:16px; background:#f7c948; border-radius:3px; box-shadow:0 2px 6px rgba(0,0,0,.1); }
.quick-actions { margin-bottom: 24px; }
.section-title { display:flex; align-items:center; gap:8px; font-weight:600; color:#303133; margin-bottom:12px; }
.section-title .bar { width:6px; height:16px; background:#f7c948; border-radius:3px; box-shadow:0 2px 6px rgba(0,0,0,.1); }
.action-card { cursor:pointer; transition: transform .3s, box-shadow .3s; border-radius:12px; border:1px solid #f0e9cc; }
.action-card:hover { transform: translateY(-4px); box-shadow:0 10px 24px rgba(0,0,0,.08); }
.action-content { display:flex; flex-direction:column; align-items:center; padding:18px; }
.action-text { margin-top:12px; color:#303133; font-size:16px; }
.recent-orders .section-title{ margin-bottom:12px; }
:deep(.el-button--primary){ --el-button-bg-color:#409eff; }
</style>
