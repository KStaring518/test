<template>
  <div class="admin-orders">
    <el-card class="section-card">
      <template #header>
        <div class="card-header"><span class="bar"></span>订单概览</div>
      </template>
      <div class="toolbar">
        <el-select v-model="status" placeholder="状态" style="width:160px" clearable @change="onStatus">
          <el-option label="全部" value="" />
          <el-option label="待付款" value="UNPAID" />
          <el-option label="待发货" value="PAID" />
          <el-option label="待收货" value="SHIPPED" />
          <el-option label="已完成" value="FINISHED" />
          <el-option label="已取消" value="CLOSED" />
        </el-select>
        <el-radio-group v-model="status" @change="onStatus" class="quick-filter">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="UNPAID">待付款</el-radio-button>
          <el-radio-button label="PAID">待发货</el-radio-button>
          <el-radio-button label="SHIPPED">待收货</el-radio-button>
          <el-radio-button label="FINISHED">已完成</el-radio-button>
          <el-radio-button label="CLOSED">已取消</el-radio-button>
        </el-radio-group>
        <el-button type="warning" @click="load">刷新</el-button>
        <el-button @click="exportCsv">导出CSV</el-button>
      </div>
      <div class="summary">
        <el-space wrap>
          <el-tag type="info">共 {{ total }} 条</el-tag>
          <el-tag type="warning">待付款 {{ countBy('UNPAID') }}</el-tag>
          <el-tag type="primary">待发货 {{ countBy('PAID') }}</el-tag>
          <el-tag type="success">已完成 {{ countBy('FINISHED') }}</el-tag>
          <el-tag type="danger">已取消 {{ countBy('CLOSED') }}</el-tag>
        </el-space>
      </div>
      <div class="table-wrap" v-loading="loading">
        <el-table :data="rows" stripe class="yellow-table">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="订单号">
            <template #default="{row}">
              <el-space>
                <span>{{ row.orderNo }}</span>
                <el-button size="small" text @click="copy(row.orderNo)">复制</el-button>
              </el-space>
            </template>
          </el-table-column>
          <el-table-column prop="buyerName" label="买家" width="140" />
          <el-table-column prop="merchantName" label="商家" width="160" />
          <el-table-column label="状态" width="120">
            <template #default="{row}">
              <el-tag :type="statusTagType(row.status)" effect="dark">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="120">
            <template #default="{row}">¥ {{ Number(row.totalAmount).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" />
        </el-table>
        <el-empty v-if="!loading && rows.length===0" description="暂无订单数据" />
      </div>
      <div class="pager"><el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @current-change="load" @size-change="onSize"/></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getAdminOrders } from '@/api/admin'

const userStore = useUserStore()
type StatusUnion = 'UNPAID' | 'PAID' | 'SHIPPED' | 'FINISHED' | 'CLOSED'
const rows = ref<any[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(10)
const status = ref<StatusUnion | string>('')

const load = async () => {
  loading.value = true
  const { data } = await getAdminOrders({ page: page.value, size: size.value, status: status.value === '' ? undefined : status.value as StatusUnion })
  rows.value = data.list
  total.value = data.total
  loading.value = false
}
const onSize = (s:number) => { size.value = s; page.value = 1; load() }
const onStatus = () => { page.value = 1; load() }

const statusText = (s:StatusUnion) => s==='UNPAID'?'待付款': s==='PAID'?'待发货': s==='SHIPPED'?'待收货': s==='FINISHED'?'已完成':'已取消'
const statusTagType = (s:StatusUnion) => s==='UNPAID'?'warning': s==='PAID'?'primary': s==='SHIPPED'?'info': s==='FINISHED'?'success':'danger'
const countBy = (s:StatusUnion) => rows.value.filter((r:any) => r.status === s).length
const copy = async (text:string) => { try{ await navigator.clipboard.writeText(text) }catch(e){ /* ignore */ } }
const exportCsv = () => {
  const header = ['ID','订单号','买家','商家','状态','金额','创建时间']
  const lines = rows.value.map((r:any) => [r.id, r.orderNo, r.buyerName, r.merchantName, statusText(r.status), r.totalAmount, r.createdAt].join(','))
  const csv = [header.join(','), ...lines].join('\n')
  const blob = new Blob(["\ufeff" + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'orders.csv'
  a.click()
  URL.revokeObjectURL(url)
}
onMounted(() => {
  // 恢复登录状态，确保刷新页面后不会丢失登录状态
  userStore.initUser()
  
  load()
})
</script>

<style scoped>
.admin-orders{ --brand:#f7c948; --brand-light:#fdf3d7; --brand-weak:#fff7da; min-height:100vh; background:linear-gradient(180deg,#fff9e6 0%,#fffef8 60%,#ffffff 100%); padding:16px }
.section-card{ border:1px solid #f0e9cc; background:#fffdf2; border-radius:12px }
.card-header{ display:flex; align-items:center; gap:8px; font-weight:600; color:#303133 }
.card-header .bar{ width:6px; height:16px; background:var(--brand); border-radius:3px; box-shadow:0 2px 6px rgba(0,0,0,.1) }
.toolbar{ display:flex; gap:8px; margin-bottom:12px }
.quick-filter{ margin-left:auto }
.summary{ margin: 8px 0 }
.table-wrap :deep(.el-table){ background:transparent }
.table-wrap :deep(.el-table th){ background:var(--brand-light) }
.table-wrap :deep(.yellow-table .el-table__body tr>td){ background-color:#fffaf0; }
.table-wrap :deep(.yellow-table .el-table__body tr.el-table__row--striped td){ background-color:#fff7e6; }
.table-wrap :deep(.el-table td), .table-wrap :deep(.el-table th.is-leaf){ border-color:#fae5b2; }
.table-wrap :deep(.el-table__body tr:hover>td){ background:var(--brand-weak)!important }
.pager{ display:flex; justify-content:center; margin-top:12px }
:deep(.el-button--warning){ --el-color-warning: var(--brand); --el-color-warning-light-9: var(--brand-weak); }
</style>


