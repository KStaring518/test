<template>
  <div class="admin-merchants">
    <el-card class="section-card">
      <template #header><div class="card-header"><span class="bar"></span>商家管理</div></template>
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索店铺名" style="width:260px" clearable @keyup.enter.native="load" />
        <el-select v-model="status" placeholder="状态" style="width:160px" clearable>
          <el-option label="全部" value="" />
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已暂停" value="SUSPENDED" />
        </el-select>
        <el-radio-group v-model="status" @change="onQuickFilter" class="quick-filter">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="PENDING">待审核</el-radio-button>
          <el-radio-button label="APPROVED">已通过</el-radio-button>
          <el-radio-button label="SUSPENDED">已暂停</el-radio-button>
        </el-radio-group>
        <el-button type="warning" @click="load">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
        <el-button type="primary" plain @click="exportCsv">导出CSV</el-button>
      </div>
      <div class="table-wrap" v-loading="loading">
        <el-table :data="rows" stripe class="yellow-table">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="shopName" label="店铺名" />
          <el-table-column prop="user.username" label="归属用户" />
          <el-table-column label="状态" width="140">
            <template #default="{row}">
              <el-tag :type="statusTagType(row.status)" effect="dark">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="260">
            <template #default="{row}">
              <el-button v-if="row.status==='PENDING'" size="small" type="success" @click="approve(row.id)">通过审核</el-button>
              <el-button v-if="row.status==='APPROVED'" size="small" type="warning" @click="suspend(row.id)">暂停</el-button>
              <el-button v-if="row.status==='SUSPENDED'" size="small" type="primary" @click="resume(row.id)">恢复启用</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pager"><el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @current-change="load" @size-change="onSize"/></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getAdminMerchants, approveMerchant, suspendMerchant, resumeMerchant } from '@/api/admin'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const keyword = ref('')
const status = ref('')
const rows = ref<any[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(10)

const load = async () => {
  loading.value = true
  const { data } = await getAdminMerchants({ page: page.value, size: size.value, keyword: keyword.value || undefined, status: status.value || undefined })
  rows.value = data.list
  total.value = data.total
  loading.value = false
}
const onSize = (s:number) => { size.value = s; page.value = 1; load() }
const approve = async (id:number) => { await approveMerchant(id); ElMessage.success('已通过'); load() }
const suspend = async (id:number) => { await suspendMerchant(id); ElMessage.success('已暂停'); load() }
const resume = async (id:number) => { await resumeMerchant(id); ElMessage.success('已恢复'); load() }

const resetFilters = () => { keyword.value=''; status.value=''; page.value=1; load() }
const onQuickFilter = () => { page.value = 1; load() }
const statusText = (s:string) => s==='PENDING'?'待审核': s==='APPROVED'?'已通过':'已暂停'
const statusTagType = (s:string) => s==='PENDING'?'warning': s==='APPROVED'?'success':'danger'
const exportCsv = () => {
  const header = ['ID','店铺名','归属用户','状态']
  const lines = rows.value.map(r => [r.id, r.shopName, r.user?.username || '', statusText(r.status)].join(','))
  const csv = [header.join(','), ...lines].join('\n')
  const blob = new Blob(["\ufeff" + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'merchants.csv'
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
.admin-merchants{ --brand:#f7c948; --brand-light:#fdf3d7; --brand-weak:#fff7da; min-height:100vh; background:linear-gradient(180deg,#fff9e6 0%,#fffef8 60%,#ffffff 100%); padding:16px }
.section-card{ border:1px solid #f0e9cc; background:#fffdf2; border-radius:12px }
.card-header{ display:flex; align-items:center; gap:8px; font-weight:600; color:#303133 }
.card-header .bar{ width:6px; height:16px; background:var(--brand); border-radius:3px; box-shadow:0 2px 6px rgba(0,0,0,.1) }
.toolbar{ display:flex; gap:8px; margin-bottom:12px }
.quick-filter{ margin-left:auto }
.table-wrap :deep(.el-table){ background:transparent }
.table-wrap :deep(.el-table th){ background:var(--brand-light) }
.table-wrap :deep(.yellow-table .el-table__body tr>td){ background-color:#fffaf0; }
.table-wrap :deep(.yellow-table .el-table__body tr.el-table__row--striped td){ background-color:#fff7e6; }
.table-wrap :deep(.el-table td), .table-wrap :deep(.el-table th.is-leaf){ border-color:#fae5b2; }
.table-wrap :deep(.el-table__body tr:hover>td){ background:var(--brand-weak)!important }
.pager{ display:flex; justify-content:center; margin-top:12px }
:deep(.el-button--warning){ --el-color-warning: var(--brand); --el-color-warning-light-9: var(--brand-weak); }
</style>


