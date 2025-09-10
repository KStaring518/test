<template>
  <div class="merchant-categories">
    <el-header class="header">
      <div class="header-content">
        <div class="logo"><h2>分类管理</h2></div>
        <el-menu mode="horizontal" :router="true" class="nav-menu">
          <el-menu-item index="/merchant">首页</el-menu-item>
          <el-menu-item index="/merchant/products">商品管理</el-menu-item>
          <el-menu-item index="/merchant/orders">订单管理</el-menu-item>
          <el-menu-item index="/merchant/categories">分类管理</el-menu-item>
        </el-menu>
      </div>
    </el-header>

    <el-main class="main-content">
      <el-card>
        <template #header>
          <div class="card-header">商品分类</div>
        </template>
        <div class="toolbar">
          <el-button type="primary" @click="openCreate()">新增分类</el-button>
        </div>
        <el-tree
          :data="treeData"
          :props="{ label: 'name', children: 'children' }"
          node-key="id"
          highlight-current
          default-expand-all
          v-loading="loading"
        >
          <template #default="{ data }">
            <span>{{ data.name }}</span>
            <span class="row-actions">
              <el-button link size="small" @click.stop="openCreate(data.id)">新增子类</el-button>
              <el-button link size="small" @click.stop="openRename(data)">重命名</el-button>
              <el-popconfirm title="确定删除该分类？" @confirm="onDelete(data.id)">
                <template #reference>
                  <el-button link size="small" type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </span>
          </template>
        </el-tree>
        <el-dialog v-model="dialogVisible" :title="isRename ? '重命名分类' : '新增分类'" width="420px">
          <el-form label-width="80px">
            <el-form-item label="名称">
              <el-input v-model="form.name" />
            </el-form-item>
            <el-form-item label="排序">
              <el-input v-model.number="form.sortOrder" type="number" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="dialogVisible=false">取消</el-button>
            <el-button type="primary" @click="onSubmit">确定</el-button>
          </template>
        </el-dialog>
      </el-card>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getCategoryTree, createMerchantCategory, updateMerchantCategory, deleteMerchantCategory } from '@/api/merchant'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const treeData = ref<any[]>([])
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const { data } = await getCategoryTree()
    treeData.value = data || []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 恢复登录状态，确保刷新页面后不会丢失登录状态
  userStore.initUser()
  
  load()
})

// CRUD
const dialogVisible = ref(false)
const form = ref<{ id?: number; name: string; parentId?: number; sortOrder: number }>({ name: '', parentId: undefined, sortOrder: 0 })
const isRename = ref(false)

const openCreate = (parentId?: number) => {
  isRename.value = false
  form.value = { name: '', parentId, sortOrder: 0 }
  dialogVisible.value = true
}
const openRename = (node: any) => {
  isRename.value = true
  form.value = { id: node.id, name: node.name, parentId: node.parentId, sortOrder: node.sortOrder || 0 }
  dialogVisible.value = true
}
const onSubmit = async () => {
  if (isRename.value && form.value.id) {
    await updateMerchantCategory(form.value.id, { name: form.value.name, parentId: form.value.parentId, sortOrder: form.value.sortOrder })
    ElMessage.success('已更新')
  } else {
    await createMerchantCategory({ name: form.value.name, parentId: form.value.parentId, sortOrder: form.value.sortOrder })
    ElMessage.success('已创建')
  }
  dialogVisible.value = false
  load()
}
const onDelete = async (id: number) => {
  await deleteMerchantCategory(id)
  ElMessage.success('已删除')
  load()
}
</script>

<style scoped>
.header{background:#fff;box-shadow:0 2px 4px rgba(0,0,0,.1);position:fixed;top:0;left:0;right:0;z-index:10}
.header-content{display:flex;align-items:center;justify-content:space-between}
.nav-menu{flex:1;margin:0 40px}
.main-content{margin-top:60px;padding:16px}
.card-header{font-weight:600}
.toolbar{margin-bottom:12px}
.row-actions{margin-left:12px}
</style>
