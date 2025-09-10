<template>
  <div class="admin-page">
    <el-header class="header">
      <div class="header-content">
        <div class="logo"><h2>管理后台</h2></div>
        <el-menu mode="horizontal" :router="true" class="nav-menu">
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/admin">仪表盘</el-menu-item>
          <el-menu-item index="/admin/banners">轮播管理</el-menu-item>
        </el-menu>
      </div>
    </el-header>

    <el-main class="main-content">
      <!-- 顶部预览与统计 -->
      <el-row :gutter="16" class="top-area">
        <el-col :span="14">
          <el-card class="section-card">
            <template #header>
              <div class="card-header"><span class="bar"></span>实时预览</div>
            </template>
            <el-empty v-if="enabledBanners.length === 0" description="暂无启用的轮播" />
            <el-carousel v-else height="180px" :interval="4000" indicator-position="outside" type="card">
              <el-carousel-item v-for="item in enabledBanners" :key="item.id">
                <img :src="item.imageUrl" :alt="item.title" class="preview-img" />
              </el-carousel-item>
            </el-carousel>
          </el-card>
        </el-col>
        <el-col :span="10">
          <el-row :gutter="12">
            <el-col :span="12">
              <el-card class="kpi-card"><div class="kpi-bar bar-all"></div><div class="kpi"><div class="num">{{ banners.length }}</div><div class="label">总数</div></div></el-card>
            </el-col>
            <el-col :span="12">
              <el-card class="kpi-card"><div class="kpi-bar bar-enabled"></div><div class="kpi"><div class="num">{{ enabledBanners.length }}</div><div class="label">已启用</div></div></el-card>
            </el-col>
            <el-col :span="12">
              <el-card class="kpi-card"><div class="kpi-bar bar-disabled"></div><div class="kpi"><div class="num">{{ disabledBanners.length }}</div><div class="label">已禁用</div></div></el-card>
            </el-col>
            <el-col :span="12">
              <el-card class="kpi-card"><div class="kpi-bar bar-order"></div><div class="kpi"><div class="num">{{ maxSort + 1 }}</div><div class="label">排序跨度</div></div></el-card>
            </el-col>
          </el-row>
        </el-col>
      </el-row>

      <!-- 工具栏 -->
      <el-card class="toolbar-card">
        <el-form :inline="true" class="toolbar">
          <el-form-item label="关键词">
            <el-input v-model="keyword" placeholder="搜索标题/链接" clearable style="width:260px" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="statusFilter" placeholder="全部" style="width:150px">
              <el-option label="全部" value="" />
              <el-option label="启用" value="ENABLED" />
              <el-option label="禁用" value="DISABLED" />
            </el-select>
          </el-form-item>
          <el-form-item label="排序">
            <el-switch v-model="asc" active-text="升序" inactive-text="降序" />
          </el-form-item>
          <el-space>
            <el-button type="warning" @click="reload">刷新</el-button>
            <el-button type="success" :disabled="selectedCount === 0" @click="handleBatchEnable">批量启用</el-button>
            <el-button type="warning" :disabled="selectedCount === 0" @click="handleBatchDisable">批量禁用</el-button>
            <el-button type="info" @click="exportCSV">导出CSV</el-button>
            <el-button type="primary" @click="openDialog()">新增轮播</el-button>
          </el-space>
        </el-form>
      </el-card>

      <el-card>
        <template #header>
          <div class="card-header">
            <span>轮播列表</span>
          </div>
        </template>

        <el-table 
          :data="filteredBanners" 
          style="width:100%"
          @selection-change="handleSelectionChange"
          row-key="id"
          ref="tableRef"
          class="themed-table"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column label="排序" width="80">
            <template #default="{ row }">
              <div class="drag-handle" style="cursor: move; text-align: center;">
                <el-icon><Rank /></el-icon>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="图片" width="200">
            <template #default="{ row }">
              <div class="image-preview">
                <img 
                  :src="row.imageUrl" 
                  alt="" 
                  @click="previewImage(row.imageUrl)"
                  style="width:180px;height:80px;object-fit:cover;cursor:pointer;border-radius:4px" 
                />
                <div class="image-overlay">
                  <el-icon><ZoomIn /></el-icon>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" min-width="150" />
          <el-table-column prop="linkUrl" label="跳转链接" min-width="200">
            <template #default="{ row }">
              <el-link 
                v-if="row.linkUrl" 
                :href="row.linkUrl" 
                target="_blank" 
                type="primary"
              >
                {{ row.linkUrl }}
              </el-link>
              <span v-else class="text-muted">无跳转链接</span>
            </template>
          </el-table-column>
          <el-table-column prop="sortOrder" label="排序" width="100" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
                {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openDialog(row)">编辑</el-button>
              <el-button 
                size="small" 
                :type="row.status === 'ENABLED' ? 'warning' : 'success'"
                @click="toggleStatus(row)"
              >
                {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
              </el-button>
              <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 轮播图编辑对话框 -->
      <el-dialog v-model="visible" :title="form.id ? '编辑轮播' : '新增轮播'" width="600px">
        <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
          <el-form-item label="标题" prop="title">
            <el-input v-model="form.title" placeholder="请输入轮播图标题" />
          </el-form-item>
          
          <el-form-item label="图片" prop="imageUrl">
            <div class="image-upload-container">
              <el-input 
                v-model="form.imageUrl" 
                placeholder="请输入图片URL或选择本地图片"
                class="image-url-input"
              />
              <el-upload
                class="image-upload"
                :show-file-list="false"
                :before-upload="beforeImageUpload"
                accept="image/*"
              >
                <el-button type="primary">选择图片</el-button>
              </el-upload>
            </div>
            <div v-if="form.imageUrl" class="image-preview-small">
              <img :src="form.imageUrl" alt="预览" style="width:200px;height:100px;object-fit:cover;border-radius:4px" />
            </div>
          </el-form-item>
          
          <el-form-item label="跳转链接" prop="linkUrl">
            <el-input 
              v-model="form.linkUrl" 
              placeholder="请输入跳转链接（可选）"
            >
              <template #prepend>
                <el-select v-model="linkType" style="width: 120px" @change="handleLinkTypeChange">
                  <el-option label="内部链接" value="internal" />
                  <el-option label="外部链接" value="external" />
                  <el-option label="商品链接" value="product" />
                </el-select>
              </template>
            </el-input>
            <div class="link-suggestions" v-if="linkType === 'internal'">
              <el-tag 
                v-for="suggestion in internalLinkSuggestions" 
                :key="suggestion.value"
                @click="form.linkUrl = suggestion.value"
                style="margin-right: 8px; cursor: pointer"
              >
                {{ suggestion.label }}
              </el-tag>
            </div>
          </el-form-item>
          
          <el-form-item label="排序" prop="sortOrder">
            <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
            <span class="form-tip">数字越小排序越靠前</span>
          </el-form-item>
          
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio label="ENABLED">启用</el-radio>
              <el-radio label="DISABLED">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="visible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
        </template>
      </el-dialog>

      <!-- 图片预览对话框 -->
      <el-dialog v-model="imagePreviewVisible" title="图片预览" width="800px" center>
        <div class="image-preview-large">
          <img :src="previewImageUrl" alt="预览" style="width:100%;height:auto;max-height:500px;object-fit:contain" />
        </div>
      </el-dialog>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { ZoomIn, Rank } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import { 
  getAllBanners, 
  createBanner, 
  updateBanner, 
  deleteBanner, 
  uploadImage,
  updateBannerOrder,
  type BannerItem 
} from '@/api/admin'
import { normalizeImageUrl } from '@/api/upload'

const userStore = useUserStore()
const banners = ref<BannerItem[]>([])
const visible = ref(false)
const imagePreviewVisible = ref(false)
const previewImageUrl = ref('')
const submitting = ref(false)
const selectedBanners = ref<BannerItem[]>([])
const linkType = ref('internal')
const tableRef = ref()

const keyword = ref('')
const statusFilter = ref('')
const asc = ref(true)

const formRef = ref<FormInstance>()
const form = ref<BannerItem>({ 
  title: '', 
  imageUrl: '', 
  linkUrl: '', 
  status: 'ENABLED', 
  sortOrder: 0 
})

// 表单验证规则
const rules: FormRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  imageUrl: [
    { required: true, message: '请输入图片URL', trigger: 'blur' }
  ],
  linkUrl: [
    { pattern: /^(https?:\/\/|\/|\.\/)/, message: '请输入有效的链接地址', trigger: 'blur' }
  ]
}

// 内部链接建议
const internalLinkSuggestions = [
  { label: '首页', value: '/' },
  { label: '商品列表', value: '/products' },
  { label: '零食分类', value: '/products?category=snacks' },
  { label: '饮料分类', value: '/products?category=beverages' },
  { label: '坚果分类', value: '/products?category=nuts' }
]

// 统计与筛选
const enabledBanners = computed(() => banners.value.filter(b => b.status === 'ENABLED'))
const disabledBanners = computed(() => banners.value.filter(b => b.status === 'DISABLED'))
const maxSort = computed(() => banners.value.reduce((m, b) => Math.max(m, b.sortOrder || 0), 0))

const selectedCount = computed(() => {
  const rows = tableRef.value?.getSelectionRows ? tableRef.value.getSelectionRows() : selectedBanners.value
  return (rows ? rows.length : 0) as number
})

const filteredBanners = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  const list = banners.value.filter(b => {
    const matchKw = kw === '' || (b.title || '').toLowerCase().includes(kw) || (b.linkUrl || '').toLowerCase().includes(kw)
    const matchStatus = statusFilter.value === '' || b.status === statusFilter.value
    return matchKw && matchStatus
  })
  return list.sort((a, b) => {
    const sa = a.sortOrder || 0
    const sb = b.sortOrder || 0
    return asc.value ? sa - sb : sb - sa
  })
})

// 加载轮播图列表
const load = async () => {
  try {
    const { data } = await getAllBanners()
    const list = data || []
    // 统一补全为可访问URL
    banners.value = list.map((b: BannerItem) => ({
      ...b,
      imageUrl: normalizeImageUrl(b.imageUrl)
    }))
  } catch (error) {
    console.error('加载轮播图失败:', error)
    ElMessage.error('加载轮播图失败')
  }
}
const reload = async () => { await load(); ElMessage.success('已刷新') }

// 打开对话框
const openDialog = (row?: BannerItem) => {
  if (row) {
    form.value = { ...row, imageUrl: normalizeImageUrl(row.imageUrl) }
  } else {
    form.value = { title: '', imageUrl: '', linkUrl: '', status: 'ENABLED', sortOrder: 0 }
  }
  visible.value = true
}

// 图片上传前处理
const beforeImageUpload = async (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) { ElMessage.error('只能上传图片文件!'); return false }
  if (!isLt2M) { ElMessage.error('图片大小不能超过 2MB!'); return false }
  try {
    const { data } = await uploadImage(file)
    form.value.imageUrl = normalizeImageUrl(data) || ''
    ElMessage.success('图片上传成功')
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败')
  }
  return false
}

// 处理链接类型变化
const handleLinkTypeChange = (value: string) => {
  if (value === 'product') {
    form.value.linkUrl = '/products/'
  } else if (value === 'internal') {
    form.value.linkUrl = '/'
  } else {
    form.value.linkUrl = 'https://'
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    if (form.value.id) {
      await updateBanner(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createBanner(form.value)
      ElMessage.success('创建成功')
    }
    visible.value = false
    await load()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

// 切换状态
const toggleStatus = async (row: BannerItem) => {
  try {
    const newStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
    await updateBanner(row.id!, { ...row, status: newStatus })
    ElMessage.success('状态更新成功')
    await load()
  } catch (error) {
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败')
  }
}

// 删除轮播图
const handleDelete = async (id?: number) => {
  if (!id) return
  try {
    await ElMessageBox.confirm('确认删除该轮播图吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteBanner(id)
    ElMessage.success('删除成功')
    await load()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 批量启用/禁用
const handleBatchEnable = async () => {
  try {
    await ElMessageBox.confirm(`确认启用选中的 ${selectedBanners.value.length} 个轮播图吗？`, '提示')
    for (const banner of selectedBanners.value) {
      if (banner.id) { await updateBanner(banner.id, { ...banner, status: 'ENABLED' }) }
    }
    ElMessage.success('批量启用成功'); await load(); selectedBanners.value = []
  } catch (error) {
    if (error !== 'cancel') { console.error('批量启用失败:', error); ElMessage.error('批量启用失败') }
  }
}
const handleBatchDisable = async () => {
  try {
    await ElMessageBox.confirm(`确认禁用选中的 ${selectedBanners.value.length} 个轮播图吗？`, '提示')
    for (const banner of selectedBanners.value) {
      if (banner.id) { await updateBanner(banner.id, { ...banner, status: 'DISABLED' }) }
    }
    ElMessage.success('批量禁用成功'); await load(); selectedBanners.value = []
  } catch (error) {
    if (error !== 'cancel') { console.error('批量禁用失败:', error); ElMessage.error('批量禁用失败') }
  }
}

// 选择变化
const handleSelectionChange = (selection: BannerItem[]) => { selectedBanners.value = selection }

// 预览图片
const previewImage = (url: string) => { previewImageUrl.value = url; imagePreviewVisible.value = true }

// 格式化日期
const formatDate = (dateStr?: string) => { if (!dateStr) return '-'; return new Date(dateStr).toLocaleString('zh-CN') }

// 导出CSV
const exportCSV = () => {
  const header = ['ID','标题','图片地址','跳转链接','排序','状态','创建时间']
  const rows = filteredBanners.value.map(b => [b.id, b.title || '', b.imageUrl, b.linkUrl || '', b.sortOrder || 0, b.status || '', b.createdAt || ''])
  const csv = [header, ...rows].map(r => r.map(x => `"${(x ?? '').toString().replace(/"/g,'""')}"`).join(',')).join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'banners.csv'
  a.click()
  URL.revokeObjectURL(url)
}

// 初始化拖拽排序
const initSortable = () => {
  nextTick(() => {
    const tbody = tableRef.value?.$el?.querySelector('.el-table__body-wrapper tbody')
    if (tbody) {
      new Sortable(tbody, {
        handle: '.drag-handle',
        animation: 150,
        onEnd: async (evt) => {
          const { oldIndex, newIndex } = evt
          if (oldIndex !== newIndex) {
            const movedItem = banners.value.splice(oldIndex!, 1)[0]
            banners.value.splice(newIndex!, 0, movedItem)
            try {
              const orderData = banners.value
                .filter(banner => banner.id !== undefined)
                .map((banner, index) => ({ id: banner.id!, sortOrder: index }))
              await updateBannerOrder(orderData)
              ElMessage.success('排序更新成功')
            } catch (error) {
              console.error('排序更新失败:', error)
              ElMessage.error('排序更新失败')
              await load()
            }
          }
        }
      })
    }
  })
}

onMounted(() => {
  userStore.initUser()
  load()
  initSortable()
})
</script>

<style scoped>
.header {
  background: #fffef8;
  box-shadow: 0 2px 4px rgba(0,0,0,.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 10;
  border-bottom: 1px solid #fae5b2;
}
.header-content { display:flex; align-items:center; justify-content:space-between }
.nav-menu { flex:1; margin:0 40px }
.main-content { margin-top: 60px; padding: 16px; background: linear-gradient(180deg,#fff9e6 0%,#fffef8 60%,#ffffff 100%); }

.section-card { border:1px solid #f0e9cc; border-radius:12px }
.card-header { display:flex; align-items:center; gap:8px; font-weight:600; color:#303133 }
.card-header .bar{ width:6px; height:16px; background:#f7c948; border-radius:3px; box-shadow:0 2px 6px rgba(0,0,0,.1) }

.top-area .preview-img{ width:100%; height:180px; object-fit:cover; border-radius:8px }

.kpi-card{ border:1px solid #f0e9cc; border-radius:12px; overflow:hidden }
.kpi-bar{ height:4px }
.bar-all{ background:#ffd166 }
.bar-enabled{ background:#67c23a }
.bar-disabled{ background:#a0c4ff }
.bar-order{ background:#f7c948 }
.kpi{ text-align:center; padding:12px 0 }
.kpi .num{ font-size:22px; font-weight:800; color:#303133 }
.kpi .label{ color:#666; margin-top:4px }

.toolbar-card{ border:1px solid #f0e9cc; border-radius:12px; margin:12px 0 }
.toolbar{ display:flex; align-items:center; gap:12px; flex-wrap:wrap }

.themed-table :deep(.el-table__header-wrapper th){ background:#fff7da }
.themed-table :deep(.el-table__row){ background:#fffef6 }
.themed-table :deep(.el-table--striped .el-table__body tr.el-table__row--striped td){ background:#fff9e6 }

.image-preview { position: relative; display: inline-block; }
.image-overlay { position:absolute; inset:0; background: rgba(0,0,0,.5); display:flex; align-items:center; justify-content:center; opacity:0; transition:opacity .3s; border-radius:4px }
.image-overlay .el-icon { color:#fff; font-size:20px }
.image-preview:hover .image-overlay { opacity:1 }

.image-upload-container { display:flex; gap:10px; align-items:center }
.image-url-input { flex:1 }
.image-preview-small { margin-top:10px }
.link-suggestions { margin-top:8px }
.form-tip { margin-left:10px; color:#999; font-size:12px }
.text-muted { color:#999 }
.image-preview-large { text-align:center }
</style>


