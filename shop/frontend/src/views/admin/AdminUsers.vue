<template>
  <div class="admin-users">
    <el-card class="section-card">
      <template #header>
        <div class="card-header"><span class="bar"></span>用户管理</div>
      </template>
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索用户名/昵称" style="width:260px" clearable @keyup.enter.native="load" />
        <el-select v-model="role" placeholder="角色" style="width:140px" clearable>
          <el-option label="全部" value="" />
          <el-option label="用户" value="USER" />
          <el-option label="商家" value="MERCHANT" />
          <el-option label="管理员" value="ADMIN" />
        </el-select>
        <el-select v-model="status" placeholder="状态" style="width:140px" clearable>
          <el-option label="全部" value="" />
          <el-option label="启用" value="ENABLED" />
          <el-option label="禁用" value="DISABLED" />
        </el-select>
        <el-radio-group v-model="status" @change="quickFilter" class="quick-filter">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="ENABLED">启用</el-radio-button>
          <el-radio-button label="DISABLED">禁用</el-radio-button>
        </el-radio-group>
        <el-button type="warning" @click="load">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
        <el-button type="primary" plain @click="exportCsv">导出CSV</el-button>
      </div>

      <div class="table-wrap" v-loading="loading">
        <el-table :data="rows" stripe class="yellow-table">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="nickname" label="昵称" />
          <el-table-column label="角色" width="120">
            <template #default="{row}">
              <el-tag :type="roleTagType(row.role)" effect="dark">{{ roleText(row.role) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{row}">
              <el-tag :type="row.status==='ENABLED'?'success':'danger'" effect="dark">{{ row.status==='ENABLED'?'ENABLED':'DISABLED' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220">
            <template #default="{row}">
              <el-button size="small" type="warning" @click="toggle(row.id)">切换启用</el-button>
              <el-popconfirm title="确认删除该用户？" @confirm="remove(row.id)">
                <template #reference>
                  <el-button size="small" type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && rows.length===0" description="暂无用户数据" />
      </div>

      <div class="pager">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @current-change="load" @size-change="onSize" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getAdminUsers, toggleUserStatus, deleteUser } from '@/api/admin'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const rows = ref<any[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const role = ref('')
const status = ref('')

const load = async () => {
  loading.value = true
  const { data } = await getAdminUsers({ page: page.value, size: size.value, keyword: keyword.value || undefined, role: role.value || undefined, status: status.value || undefined })
  rows.value = data.list
  total.value = data.total
  loading.value = false
}

const onSize = (s:number) => { size.value = s; page.value = 1; load() }
const toggle = async (id:number) => { await toggleUserStatus(id); ElMessage.success('已更新'); load() }
const remove = async (id:number) => { await deleteUser(id); ElMessage.success('已删除'); load() }

const resetFilters = () => { keyword.value=''; role.value=''; status.value=''; page.value=1; load() }
const quickFilter = () => { page.value=1; load() }
const roleText = (r:string) => r==='ADMIN'?'ADMIN': r==='MERCHANT'?'MERCHANT':'USER'
const roleTagType = (r:string) => r==='ADMIN'?'warning': r==='MERCHANT'?'primary':'info'
const exportCsv = () => {
  const header = ['ID','用户名','昵称','角色','状态']
  const lines = rows.value.map(r => [r.id, r.username, r.nickname || '', roleText(r.role), r.status].join(','))
  const csv = [header.join(','), ...lines].join('\n')
  const blob = new Blob(["\ufeff" + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'users.csv'
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
.admin-users{ --brand:#f7c948; --brand-light:#fdf3d7; --brand-weak:#fff7da; min-height:100vh; background:linear-gradient(180deg,#fff9e6 0%,#fffef8 60%,#ffffff 100%); padding:16px }
.section-card{ border:1px solid #f0e9cc; background:#fffdf2; border-radius:12px }
.card-header{ display:flex; align-items:center; gap:8px; font-weight:600; color:#303133 }
.card-header .bar{ width:6px; height:16px; background:var(--brand); border-radius:3px; box-shadow:0 2px 6px rgba(0,0,0,.1) }
.toolbar{ display:flex; gap:8px; margin-bottom:12px }
.quick-filter{ margin-left:auto }
.table-wrap :deep(.el-table){ background:transparent }
.table-wrap :deep(.el-table th){ background:var(--brand-light) }
/* 基础行底色改为柔和浅黄，斑马更浅 */
.table-wrap :deep(.yellow-table .el-table__body tr>td){ background-color:#fffaf0; }
.table-wrap :deep(.yellow-table .el-table__body tr.el-table__row--striped td){ background-color:#fff7e6; }
/* 边框颜色与黄系协调 */
.table-wrap :deep(.el-table td),
.table-wrap :deep(.el-table th.is-leaf){ border-color:#fae5b2; }
.table-wrap :deep(.el-table__body tr:hover>td){ background:var(--brand-weak)!important }
.pager{ display:flex; justify-content:center; margin-top:12px }
:deep(.el-button--warning){ --el-color-warning: var(--brand); --el-color-warning-light-9: var(--brand-weak); }
</style>


