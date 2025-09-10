<template>
  <div class="merchant-orders">
    <el-header class="header">
      <div class="header-content">
        <div class="logo"><h2>订单管理</h2></div>
        <el-menu mode="horizontal" :router="true" class="nav-menu">
          <el-menu-item index="/merchant">首页</el-menu-item>
          <el-menu-item index="/merchant/products">商品管理</el-menu-item>
          <el-menu-item index="/merchant/orders">订单管理</el-menu-item>
        </el-menu>
      </div>
    </el-header>

    <el-main class="main-content">
      <el-card class="orders-card">
        <template #header>
          <div class="card-header">
            <div class="section-title"><span class="bar"></span>商家订单列表</div>
            <div class="toolbar">
              <el-segmented v-model="status" :options="statusOptions" @change="onStatusChange" />
              <el-input v-model="keyword" placeholder="搜索订单号/买家" clearable class="toolbar-item" @keyup.enter.native="reload" />
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                class="toolbar-item"
                @change="reload"
              />
              <el-button class="toolbar-item" @click="reload">刷新</el-button>
              <el-button type="warning" class="toolbar-item" @click="exportCSV">导出CSV</el-button>
            </div>
          </div>
        </template>

        <div class="kpis" v-if="rows.length">
          <div class="kpi">
            <div class="kpi-bar kpi-all"></div>
            <div class="kpi-content"><div class="num">{{ rows.length }}</div><div class="label">本页订单</div></div>
          </div>
          <div class="kpi">
            <div class="kpi-bar kpi-amount"></div>
            <div class="kpi-content"><div class="num">¥{{ pageAmount }}</div><div class="label">本页金额</div></div>
          </div>
          <div class="kpi">
            <div class="kpi-bar kpi-finished"></div>
            <div class="kpi-content"><div class="num">{{ finishedCount }}</div><div class="label">已完成</div></div>
          </div>
        </div>

        <el-empty v-if="filteredRows.length===0" description="暂无符合条件的订单" />

        <el-table v-else :data="filteredRows" stripe size="small" class="orders-table">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="orderNo" label="订单号" width="180" />
          <el-table-column prop="user.username" label="买家" width="140" />
          <el-table-column prop="totalAmount" label="金额" width="120">
            <template #default="{row}">¥{{ row.totalAmount }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{row}"><el-tag :type="statusTagType(row.status)">{{ row.status }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column label="操作" width="280">
            <template #default="{row}">
              <el-button v-if="row.status==='PAID'" size="small" type="primary" @click="ship(row.id)">发货</el-button>
              <el-button v-if="row.status==='UNPAID'" size="small" type="warning" @click="closeIt(row.id)">关闭</el-button>
              <el-button size="small" @click="view(row.id)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pager">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            :total="total"
            layout="total, prev, pager, next, sizes"
            :page-sizes="[10,20,30]"
            @current-change="load"
            @size-change="onSize"
          />
        </div>
      </el-card>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getMerchantOrders, shipOrder, closeOrder } from '@/api/merchant'

const router = useRouter()
const userStore = useUserStore()
const rows = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const status = ref<string|undefined>(undefined)
const keyword = ref('')
const dateRange = ref<[string, string] | null>(null)

const statusOptions = [
  { label: '全部', value: undefined },
  { label: '未支付', value: 'UNPAID' },
  { label: '已支付', value: 'PAID' },
  { label: '已发货', value: 'SHIPPED' },
  { label: '已完成', value: 'FINISHED' },
  { label: '已关闭', value: 'CLOSED' }
]

const load = async () => {
  const { data } = await getMerchantOrders({ page: page.value, size: size.value, status: status.value, keyword: keyword.value })
  rows.value = data.list
  total.value = data.total
}
const onSize = (s:number) => { size.value = s; page.value = 1; load() }
const onStatusChange = () => { page.value = 1; load() }
const reload = () => { page.value = 1; load() }

const filteredRows = computed(() => {
  let list = rows.value
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    list = list.filter((r:any) => (r.orderNo || '').toLowerCase().includes(kw) || (r.user?.username || '').toLowerCase().includes(kw))
  }
  if (dateRange.value && dateRange.value.length === 2) {
    const [start, end] = dateRange.value
    const s = new Date(start).getTime()
    const e = new Date(end).getTime() + 24*60*60*1000 - 1
    list = list.filter((r:any) => new Date(r.createdAt).getTime() >= s && new Date(r.createdAt).getTime() <= e)
  }
  return list
})

const pageAmount = computed(() => filteredRows.value.reduce((sum:number, r:any) => sum + (Number(r.totalAmount) || 0), 0).toFixed(0))
const finishedCount = computed(() => filteredRows.value.filter((r:any) => r.status === 'FINISHED').length)

const ship = async (orderId: number) => { router.push(`/merchant/orders/${orderId}`) }
const view = (orderId: number) => { router.push(`/merchant/orders/${orderId}`) }

const statusTagType = (s:string) => {
  const map: Record<string,string> = { UNPAID: 'info', PAID: 'warning', SHIPPED: 'primary', FINISHED: 'success', CLOSED: 'danger' }
  return map[s] || 'default'
}

onMounted(() => {
  userStore.initUser()
  load()
})

const closeIt = async (orderId: number) => {
  try {
    await ElMessageBox.confirm('确定关闭该未支付订单？', '提示', { type: 'warning' })
    await closeOrder(orderId)
    ElMessage.success('订单已关闭')
    load()
  } catch (_) {}
}

const exportCSV = () => {
  const header = ['ID','订单号','买家','金额','状态','创建时间']
  const rowsData = filteredRows.value.map((r:any) => [r.id, r.orderNo, r.user?.username || '', r.totalAmount, r.status, r.createdAt])
  const csv = [header, ...rowsData].map(r => r.map(x => `"${String(x ?? '').replace(/"/g,'""')}"`).join(',')).join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `merchant-orders-page-${page.value}.csv`
  a.click()
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.merchant-orders{ --brand:#f7c948; --brand-light:#fdf3d7; --brand-weak:#fff7da; min-height:100vh; background:linear-gradient(180deg,#fff9e6 0%,#fffef8 60%,#ffffff 100%); }
.header{ background:#fffef8; box-shadow:0 2px 4px rgba(0,0,0,.08); position:fixed; top:0; left:0; right:0; z-index:10; border-bottom:1px solid #fae5b2; }
.header-content{ display:flex; align-items:center; justify-content:space-between }
.nav-menu{ flex:1; margin:0 40px }
.main-content{ margin-top:60px; padding:16px }
.orders-card{ border:1px solid #fae5b2; background:#fffdf2; border-radius:12px; box-shadow:0 2px 10px rgba(0,0,0,.04); }
.card-header{ display:flex; justify-content:space-between; align-items:center; gap:12px; flex-wrap:wrap }
.section-title{ display:flex; align-items:center; gap:8px; font-weight:600; color:#303133 }
.section-title .bar{ width:6px; height:16px; background:var(--brand); border-radius:3px; box-shadow:0 2px 6px rgba(0,0,0,.1) }
.toolbar{ display:flex; align-items:center; gap:8px; flex-wrap:wrap }
.toolbar-item{ width:220px }
.kpis{ display:flex; gap:12px; margin:12px 0 }
.kpi{ border:1px solid #f0e9cc; border-radius:10px; background:#fff; min-width:150px; overflow:hidden }
.kpi-bar{ height:4px }
.kpi-all{ background:#ffd166 }
.kpi-amount{ background:#67c23a }
.kpi-finished{ background:#409eff }
.kpi-content{ padding:10px 12px }
.kpi-content .num{ font-size:20px; font-weight:800; color:#303133 }
.kpi-content .label{ font-size:12px; color:#666; margin-top:4px }
.orders-table :deep(.el-table){ background:transparent }
.orders-table :deep(.el-table th){ background:var(--brand-light) }
.orders-table :deep(.el-table__body tr:hover>td){ background:var(--brand-weak)!important }
.pager{ display:flex; justify-content:center; margin-top:12px }
</style>
