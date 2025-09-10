<template>
  <div class="admin-page">
    <el-header class="header">
      <div class="header-content">
        <div class="logo"><h2>管理后台</h2></div>
        <el-menu mode="horizontal" :router="true" class="nav-menu">
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/admin">仪表盘</el-menu-item>
          <el-menu-item index="/admin/users">用户管理</el-menu-item>
          <el-menu-item index="/admin/merchants">商家管理</el-menu-item>
          <el-menu-item index="/admin/products">分类管理</el-menu-item>
          <el-menu-item index="/admin/orders">订单概览</el-menu-item>
          <el-menu-item index="/admin/system">系统统计</el-menu-item>
        </el-menu>
      </div>
    </el-header>

    <el-main class="main-content">
      <div class="welcome-section">
        <div class="welcome-text">
          <h1>欢迎回来，管理员！</h1>
          <p>今日数据概览，让您快速掌握平台运营状况</p>
        </div>
        <div class="refresh-btn">
          <el-button type="warning" @click="refreshAll" :loading="refreshing">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
        </div>
      </div>

      <el-row :gutter="16" class="cards">
        <el-col :span="6">
          <el-card class="kpi-card" @click="goToUsers">
            <div class="kpi-bar bar-users"></div>
            <div class="kpi">
              <div class="kpi-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="kpi-content">
                <div class="num">{{ stats?.totalUsers || 0 }}</div>
                <div class="label">用户数</div>
                <div class="trend" v-if="userTrend">
                  <el-icon :class="userTrend > 0 ? 'trend-up' : 'trend-down'">
                    <component :is="userTrend > 0 ? 'ArrowUp' : 'ArrowDown'" />
                  </el-icon>
                  <span>{{ Math.abs(userTrend) }}%</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="kpi-card" @click="goToMerchants">
            <div class="kpi-bar bar-merchants"></div>
            <div class="kpi">
              <div class="kpi-icon">
                <el-icon><Shop /></el-icon>
              </div>
              <div class="kpi-content">
                <div class="num">{{ stats?.totalMerchants || 0 }}</div>
                <div class="label">商家数</div>
                <div class="trend" v-if="merchantTrend">
                  <el-icon :class="merchantTrend > 0 ? 'trend-up' : 'trend-down'">
                    <component :is="merchantTrend > 0 ? 'ArrowUp' : 'ArrowDown'" />
                  </el-icon>
                  <span>{{ Math.abs(merchantTrend) }}%</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="kpi-card" @click="goToProducts">
            <div class="kpi-bar bar-products"></div>
            <div class="kpi">
              <div class="kpi-icon">
                <el-icon><Box /></el-icon>
              </div>
              <div class="kpi-content">
                <div class="num">{{ stats?.totalProducts || 0 }}</div>
                <div class="label">商品数</div>
                <div class="trend" v-if="productTrend">
                  <el-icon :class="productTrend > 0 ? 'trend-up' : 'trend-down'">
                    <component :is="productTrend > 0 ? 'ArrowUp' : 'ArrowDown'" />
                  </el-icon>
                  <span>{{ Math.abs(productTrend) }}%</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="kpi-card" @click="goToOrders">
            <div class="kpi-bar bar-orders"></div>
            <div class="kpi">
              <div class="kpi-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="kpi-content">
                <div class="num">{{ stats?.totalOrders || 0 }}</div>
                <div class="label">订单数</div>
                <div class="trend" v-if="orderTrend">
                  <el-icon :class="orderTrend > 0 ? 'trend-up' : 'trend-down'">
                    <component :is="orderTrend > 0 ? 'ArrowUp' : 'ArrowDown'" />
                  </el-icon>
                  <span>{{ Math.abs(orderTrend) }}%</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="section-card">
        <template #header>
          <div class="card-header">
            <span class="bar"></span>
            <span>最近7天 GMV / 订单数</span>
            <el-tag type="info" size="small" class="chart-tag">实时数据</el-tag>
          </div>
        </template>
        <div id="admin-trend" style="height:320px" v-loading="chartLoading"></div>
      </el-card>

      <el-row :gutter="16" style="margin-top:16px">
        <el-col :span="12">
          <el-card class="section-card">
            <template #header>
              <div class="card-header">
                <span class="bar"></span>
                <span>订单状态占比</span>
                <el-tag type="success" size="small" class="chart-tag">饼图</el-tag>
              </div>
            </template>
            <div id="admin-status" style="height:300px" v-loading="chartLoading"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="section-card">
            <template #header>
              <div class="card-header">
                <span class="bar"></span>
                <span>分类销售Top5</span>
                <el-tag type="warning" size="small" class="chart-tag">柱状图</el-tag>
              </div>
            </template>
            <div id="admin-category-top" style="height:300px" v-loading="chartLoading"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="section-card" style="margin-top:16px;">
        <template #header>
          <div class="card-header">
            <span class="bar"></span>
            <span>快速操作</span>
            <el-tag type="primary" size="small" class="chart-tag">常用功能</el-tag>
          </div>
        </template>
        <div class="quick-actions">
          <div class="action-group">
            <h4>用户管理</h4>
            <el-space wrap>
              <el-button type="primary" @click="$router.push('/admin/users')">
                <el-icon><User /></el-icon>
                用户管理
              </el-button>
              <el-button type="success" @click="$router.push('/admin/merchants')">
                <el-icon><Shop /></el-icon>
                商家管理
              </el-button>
            </el-space>
          </div>
          <div class="action-group">
            <h4>内容管理</h4>
            <el-space wrap>
              <el-button type="warning" @click="$router.push('/admin/products')">
                <el-icon><Box /></el-icon>
                分类管理
              </el-button>
              <el-button type="info" @click="$router.push('/admin/banners')">
                <el-icon><Picture /></el-icon>
                轮播管理
              </el-button>
            </el-space>
          </div>
          <div class="action-group">
            <h4>订单管理</h4>
            <el-space wrap>
              <el-button type="danger" @click="$router.push('/admin/orders')">
                <el-icon><Document /></el-icon>
                订单概览
              </el-button>
              <el-button type="primary" plain @click="$router.push('/admin/system')">
                <el-icon><Setting /></el-icon>
                系统配置
              </el-button>
            </el-space>
          </div>
        </div>
      </el-card>
    </el-main>
  </div>
  </template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getAdminStatistics, getAdminTrend, getAdminStatusDistribution, getAdminCategoryTop } from '@/api/admin'
import { ElMessage } from 'element-plus'
import { User, Shop, Box, Document, Refresh, ArrowUp, ArrowDown, Picture, Setting } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const router = useRouter()
const userStore = useUserStore()
const stats = ref<{ totalUsers: number; totalMerchants: number; totalOrders: number; totalProducts: number } | null>(null)
const refreshing = ref(false)
const chartLoading = ref(false)
const userTrend = ref(0)
const merchantTrend = ref(0)
const productTrend = ref(0)
const orderTrend = ref(0)

const loadStats = async () => {
  try {
    const { data } = await getAdminStatistics()
    stats.value = data
    
    // 使用后端返回的真实增长率
    if (data) {
      // 用户增长率：从后端获取真实增长率
      userTrend.value = data.userGrowthRate || 0
      
      // 商家增长率：从后端获取真实增长率
      merchantTrend.value = data.merchantGrowthRate || 0
      
      // 商品增长率：从后端获取真实增长率
      productTrend.value = data.productGrowthRate || 0
      
      // 订单增长率：从后端获取真实增长率
      orderTrend.value = data.orderGrowthRate || 0
      
      // 记录调试信息
      console.log('真实增长率数据:', {
        users: data.userGrowthRate,
        merchants: data.merchantGrowthRate,
        products: data.productGrowthRate,
        orders: data.orderGrowthRate
      })
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const refreshAll = async () => {
  refreshing.value = true
  chartLoading.value = true
  try {
    await loadStats()
    await renderTrend()
    await renderCharts()
    ElMessage.success('数据刷新成功')
  } catch (error) {
    ElMessage.error('数据刷新失败')
  } finally {
    refreshing.value = false
    chartLoading.value = false
  }
}

const goToUsers = () => router.push('/admin/users')
const goToMerchants = () => router.push('/admin/merchants')
const goToProducts = () => router.push('/admin/products')
const goToOrders = () => router.push('/admin/orders')

const renderTrend = async () => {
  const el = document.getElementById('admin-trend') as HTMLDivElement
  if (!el) return
  const chart = echarts.init(el)
  const { data } = await getAdminTrend(7)
  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['GMV', '订单数'] },
    xAxis: { type: 'category', data: data.labels },
    yAxis: [{ type: 'value', name: 'GMV' }, { type: 'value', name: '订单数' }],
    series: [
      { name: 'GMV', type: 'line', data: data.gmv, yAxisIndex: 0, smooth: true },
      { name: '订单数', type: 'line', data: data.orders, yAxisIndex: 1, smooth: true }
    ]
  }
  chart.setOption(option)
}

const renderCharts = async () => {
  // 饼图
  const el1 = document.getElementById('admin-status') as HTMLDivElement
  if (el1) {
    const chart = echarts.init(el1)
    const { data } = await getAdminStatusDistribution()
    chart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{
        name: '状态占比',
        type: 'pie',
        radius: '60%',
        data: data.labels.map((l, i) => ({ name: l, value: data.values[i] }))
      }]
    })
  }
  const el2 = document.getElementById('admin-category-top') as HTMLDivElement
  if (el2) {
    const chart = echarts.init(el2)
    const { data } = await getAdminCategoryTop()
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.labels },
      yAxis: { type: 'value' },
      series: [{ name: '销售额', type: 'bar', data: data.values }]
    })
  }
}

onMounted(async () => {
  // 恢复登录状态，确保刷新页面后不会丢失登录状态
  userStore.initUser()
  
  await loadStats()
  await renderTrend()
  await renderCharts()
})
</script>

<style scoped>
.admin-page{ --brand:#f7c948; --brand-light:#fdf3d7; --brand-weak:#fff7da; min-height:100vh; background:linear-gradient(180deg,#fff9e6 0%,#fffef8 60%,#ffffff 100%); }
.header{ background:#fffef8; box-shadow:0 2px 4px rgba(0,0,0,.08); position:fixed; top:0; left:0; right:0; z-index:10; border-bottom:1px solid #fae5b2 }
.header-content{ display:flex; align-items:center; justify-content:space-between }
.nav-menu{ flex:1; margin:0 40px }
.main-content{ margin-top:60px; padding:16px }

.welcome-section{ display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; padding:20px; background:linear-gradient(135deg,var(--brand-light) 0%,#fff 100%); border-radius:12px; border:1px solid #f0e9cc }
.welcome-text h1{ margin:0; color:#303133; font-size:24px; font-weight:600 }
.welcome-text p{ margin:8px 0 0; color:#666; font-size:14px }

.kpi-card{ border:1px solid #f0e9cc; border-radius:12px; overflow:hidden; cursor:pointer; transition:all 0.3s ease; background:#fff }
.kpi-card:hover{ transform:translateY(-2px); box-shadow:0 8px 25px rgba(0,0,0,0.1) }
.kpi-bar{ height:4px; }
.bar-users{ background:#a0c4ff }
.bar-merchants{ background:#67c23a }
.bar-products{ background:#ffd166 }
.bar-orders{ background:#f7c948 }
.kpi{ display:flex; align-items:center; padding:16px; gap:12px }
.kpi-icon{ width:48px; height:48px; border-radius:12px; display:flex; align-items:center; justify-content:center; font-size:20px; color:#fff }
.kpi-card:nth-child(1) .kpi-icon{ background:linear-gradient(135deg,#a0c4ff,#7bb3ff) }
.kpi-card:nth-child(2) .kpi-icon{ background:linear-gradient(135deg,#67c23a,#85ce61) }
.kpi-card:nth-child(3) .kpi-icon{ background:linear-gradient(135deg,#ffd166,#ffed4e) }
.kpi-card:nth-child(4) .kpi-icon{ background:linear-gradient(135deg,#f7c948,#ffd700) }
.kpi-content{ flex:1 }
.kpi .num{ font-size:28px; font-weight:800; color:#303133; margin:0 }
.kpi .label{ color:#666; margin:4px 0 0; font-size:14px }
.trend{ display:flex; align-items:center; gap:4px; margin-top:4px; font-size:12px }
.trend-up{ color:#67c23a }
.trend-down{ color:#f56c6c }

.section-card{ border:1px solid #f0e9cc; border-radius:12px; background:#fff }
.card-header{ display:flex; align-items:center; gap:8px; font-weight:600; color:#303133 }
.card-header .bar{ width:6px; height:16px; background:var(--brand); border-radius:3px; box-shadow:0 2px 6px rgba(0,0,0,.1) }
.chart-tag{ margin-left:auto }

.quick-actions{ display:grid; grid-template-columns:repeat(auto-fit,minmax(300px,1fr)); gap:20px; padding:16px 0 }
.action-group h4{ margin:0 0 12px; color:#303133; font-size:16px; font-weight:600; padding-bottom:8px; border-bottom:2px solid var(--brand-light) }
.action-group{ background:var(--brand-weak); padding:16px; border-radius:8px; border:1px solid #f0e9cc }

:deep(.el-button--warning){ --el-color-warning: var(--brand); --el-color-warning-light-9: var(--brand-weak); }
</style>
