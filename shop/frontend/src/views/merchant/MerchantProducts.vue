<template>
  <div class="merchant-products">
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
      <div class="products-container">
        <div class="page-header">
          <h2>商品管理</h2>
          <el-button type="warning" @click="showAddDialog = true"><el-icon><Plus /></el-icon>添加商品</el-button>
        </div>
        
        <!-- 筛选栏 -->
        <div class="filter-section">
          <div class="section-title"><span class="bar"></span>筛选条件</div>
          <el-row :gutter="16">
            <el-col :span="6">
              <el-select v-model="filters.categoryId" placeholder="选择分类" clearable @change="handleFilter">
                <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
              </el-select>
            </el-col>
            <el-col :span="6">
              <el-select v-model="filters.status" placeholder="商品状态" clearable @change="handleFilter">
                <el-option label="在售" value="ON_SALE" />
                <el-option label="下架" value="OFF_SALE" />
                <el-option label="缺货" value="OUT_OF_STOCK" />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-input v-model="filters.keyword" placeholder="搜索商品名称" clearable @keyup.enter="handleFilter">
                <template #append><el-button @click="handleFilter">搜索</el-button></template>
              </el-input>
            </el-col>
          </el-row>
        </div>
        
        <!-- 商品列表 -->
        <div class="products-table">
          <el-table :data="products" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column label="商品图片" width="100">
              <template #default="scope">
                <div class="product-image-cell">
                  <img v-if="scope.row.coverImage" :src="scope.row.coverImage" :alt="scope.row.name" class="product-image"/>
                  <div v-else class="no-image">无图片</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="商品名称" width="200" />
            <el-table-column prop="subtitle" label="副标题" width="180" />
            <el-table-column prop="price" label="价格" width="100"><template #default="scope">¥{{ scope.row.price }}</template></el-table-column>
            <el-table-column prop="stock" label="库存" width="80" />
            <el-table-column prop="status" label="状态" width="100"><template #default="scope"><el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag></template></el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="180" />
            <el-table-column label="操作" width="220"><template #default="scope"><el-button size="small" @click="editProduct(scope.row)">编辑</el-button><el-button v-if="scope.row.status === 'ON_SALE'" size="small" type="warning" @click="toggleProductStatus(scope.row.id, 'OFF_SALE')">下架</el-button><el-button v-else size="small" type="success" @click="toggleProductStatus(scope.row.id, 'ON_SALE')">上架</el-button><el-button size="small" type="danger" @click="deleteProduct(scope.row.id)">删除</el-button></template></el-table-column>
          </el-table>
        </div>
        
        <!-- 分页 -->
        <div class="pagination-section">
          <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :total="pagination.total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
        </div>
      </div>
    </el-main>
    
    <!-- 添加/编辑商品对话框 -->
    <el-dialog v-model="showAddDialog" :title="editingProduct ? '编辑商品' : '添加商品'" width="800px">
      <el-form ref="productFormRef" :model="productForm" :rules="productRules" label-width="100px">
        <el-form-item label="商品名称" prop="name"><el-input v-model="productForm.name" placeholder="请输入商品名称" /></el-form-item>
        <el-form-item label="副标题" prop="subtitle"><el-input v-model="productForm.subtitle" placeholder="请输入商品副标题" /></el-form-item>
        <el-form-item label="商品分类" prop="categoryId"><el-select v-model="productForm.categoryId" placeholder="请选择商品分类"><el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" /></el-select></el-form-item>
        <el-form-item label="商品价格" prop="price"><el-input-number v-model="productForm.price" :precision="2" :step="0.01" :min="0" placeholder="请输入商品价格" /></el-form-item>
        <el-form-item label="商品库存" prop="stock"><el-input-number v-model="productForm.stock" :min="0" :precision="0" placeholder="请输入商品库存" /></el-form-item>
        <el-form-item label="计量单位" prop="unit"><el-input v-model="productForm.unit" placeholder="如：包、袋、瓶等" /></el-form-item>
        <el-form-item label="商品图片" prop="coverImage">
          <div class="image-upload-section">
            <el-upload class="image-uploader" :show-file-list="false" :before-upload="beforeImageUpload" :http-request="handleImageUpload" accept="image/*">
              <img v-if="productForm.coverImage" :src="productForm.coverImage" class="uploaded-image" />
              <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="image-upload-tips">
              <p>支持 JPG、PNG、GIF 格式，文件大小不超过 5MB</p>
              <el-button v-if="productForm.coverImage" type="danger" size="small" @click="removeImage">删除图片</el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="商品描述" prop="description"><el-input v-model="productForm.description" type="textarea" :rows="4" placeholder="请输入商品详细描述" /></el-form-item>
        <el-form-item label="商品状态" prop="status"><el-select v-model="productForm.status" placeholder="请选择商品状态"><el-option label="在售" value="ON_SALE" /><el-option label="下架" value="OFF_SALE" /><el-option label="缺货" value="OUT_OF_STOCK" /></el-select></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="warning" @click="submitProduct" :loading="submitting">{{ editingProduct ? '更新' : '添加' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getCategories } from '@/api/product'
import { getMerchantProducts, createProduct as createProductApi, updateProduct as updateProductApi, deleteProduct as deleteProductApi, toggleProductStatus as toggleProductStatusApi } from '@/api/merchant'
import { uploadProductImage, normalizeProductImageUrl } from '@/api/upload'

const router = useRouter()
const userStore = useUserStore()

const products = ref<any[]>([])
const categories = ref<any[]>([])
const showAddDialog = ref(false)
const editingProduct = ref<any>(null)
const submitting = ref(false)
const productFormRef = ref()

// 筛选条件
const filters = reactive({
  categoryId: null,
  status: '',
  keyword: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 商品表单 - 改用 ref 避免响应式问题
const productForm = ref({
  name: '',
  subtitle: '',
  categoryId: null,
  price: 0,
  stock: 0,
  unit: '',
  coverImage: '',
  description: '',
  status: 'ON_SALE'
})

// 表单验证规则
const productRules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入商品库存', trigger: 'blur' }
  ],
  unit: [
    { required: true, message: '请输入计量单位', trigger: 'blur' }
  ]
}

// 加载商品列表
const loadProducts = async () => {
  try {
    const { data } = await getMerchantProducts({
      page: pagination.page,
      size: pagination.size,
      keyword: filters.keyword || undefined,
      categoryId: filters.categoryId || undefined,
      status: filters.status || undefined
    })
    const list = data.list || []
    // 规范化图片URL，兼容相对路径与仅文件名
    products.value = list.map((p: any) => ({
      ...p,
      coverImage: normalizeProductImageUrl(p.coverImage)
    }))
    pagination.total = data.total || 0
  } catch (error) {
    console.error('加载商品列表失败:', error)
    ElMessage.error('加载商品列表失败')
  }
}

// 加载分类列表
const loadCategories = async () => {
  try {
    const response = await getCategories()
    categories.value = response.data || []
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 筛选处理
const handleFilter = () => {
  pagination.page = 1
  loadProducts()
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadProducts()
}

// 当前页变化
const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadProducts()
}

// 编辑商品
const editProduct = (product: any) => {
  editingProduct.value = product
  console.log('编辑商品:', product)
  console.log('商品图片:', product.coverImage)
  
  productForm.value = {
    name: product.name,
    subtitle: product.subtitle,
    categoryId: product.category?.id ?? product.categoryId,
    price: product.price,
    stock: product.stock,
    unit: product.unit,
    coverImage: normalizeProductImageUrl(product.coverImage) || '',
    description: product.description,
    status: product.status
  }
  
  console.log('设置后的productForm:', productForm.value)
  console.log('设置后的coverImage:', productForm.value.coverImage)
  
  showAddDialog.value = true
}

// 提交商品
const submitProduct = async () => {
  try {
    await productFormRef.value.validate()
    
    submitting.value = true
    
    if (editingProduct.value) {
      await updateProductApi(editingProduct.value.id, {
        name: productForm.value.name,
        subtitle: productForm.value.subtitle,
        categoryId: Number(productForm.value.categoryId),
        coverImage: productForm.value.coverImage,
        price: productForm.value.price,
        unit: productForm.value.unit,
        stock: productForm.value.stock,
        description: productForm.value.description
      })
      ElMessage.success('商品更新成功')
    } else {
      await createProductApi({
        name: productForm.value.name,
        subtitle: productForm.value.subtitle,
        categoryId: Number(productForm.value.categoryId),
        coverImage: productForm.value.coverImage,
        price: productForm.value.price,
        unit: productForm.value.unit,
        stock: productForm.value.stock,
        description: productForm.value.description
      })
      ElMessage.success('商品添加成功')
    }
    
    showAddDialog.value = false
    resetForm()
    loadProducts()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

// 切换商品状态
const toggleProductStatus = async (productId: number, status: string) => {
  try {
    const action = status === 'ON_SALE' ? '上架' : '下架'
    await ElMessageBox.confirm(`确定要${action}这个商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await toggleProductStatusApi(productId)
    ElMessage.success(`商品${action}成功`)
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 删除商品
const deleteProduct = async (productId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？删除后无法恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteProductApi(productId)
    ElMessage.success('商品删除成功')
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 重置表单
const resetForm = () => {
  editingProduct.value = null
  productForm.value = {
    name: '',
    subtitle: '',
    categoryId: null,
    price: 0,
    stock: 0,
    unit: '',
    coverImage: '',
    description: '',
    status: 'ON_SALE'
  }
}

// 获取状态类型
const getStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'ON_SALE': 'success',
    'OFF_SALE': 'warning',
    'OUT_OF_STOCK': 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'ON_SALE': '在售',
    'OFF_SALE': '下架',
    'OUT_OF_STOCK': '缺货'
  }
  return statusMap[status] || status
}

// 图片上传相关方法
const beforeImageUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 图片上传处理
const handleImageUpload = async (options: any) => {
  try {
    const productId = editingProduct.value?.id
    console.log('=== 前端图片上传调试 ===')
    console.log('商品ID:', productId)
    console.log('文件:', options.file)
    
    const response = await uploadProductImage(options.file, productId)
    console.log('上传响应:', response)
    console.log('响应数据:', response.data)
    
    // 修复：确保imageUrl是字符串类型
    let imageUrl = ''
    if (response.data && response.data.data) {
      imageUrl = response.data.data
    } else if (response.data && typeof response.data === 'string') {
      imageUrl = response.data
    } else {
      throw new Error('无法解析图片URL')
    }
    
    console.log('提取的图片URL:', imageUrl)
    
    // 统一规范化为可访问URL
    imageUrl = normalizeProductImageUrl(imageUrl)
    console.log('完整图片URL:', imageUrl)
    
    // 设置图片URL
    productForm.value.coverImage = imageUrl
    console.log('设置后的coverImage:', productForm.value.coverImage)
    
    // 立即更新商品列表
    if (productId && products.value.length > 0) {
      const productIndex = products.value.findIndex(p => p.id === productId)
      if (productIndex !== -1) {
        products.value[productIndex].coverImage = imageUrl
      }
    }
    
    ElMessage.success('图片上传成功')
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败')
  }
}

const removeImage = () => {
  productForm.value.coverImage = ''
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
  
  loadProducts()
  loadCategories()
})
</script>

<style scoped>
.merchant-products { --brand:#f7c948; --brand-light:#fdf3d7; --brand-weak:#fff7da; min-height: 100vh; background: linear-gradient(180deg,#fff9e6 0%,#fffef8 60%,#ffffff 100%); }
.header { background-color:#fffef8; box-shadow:0 2px 4px rgba(0,0,0,.08); position:fixed; top:0; left:0; right:0; z-index:1000; border-bottom:1px solid #fae5b2; }
.header-content { display:flex; align-items:center; justify-content:space-between; height:100%; }
.logo h2 { margin:0; color:#f7c948; }
.nav-menu { flex:1; margin:0 50px; }
.user-info { display:flex; align-items:center; gap:10px; }
.main-content { margin-top:60px; padding:20px; }
.products-container { max-width: 1200px; margin: 0 auto; }
.page-header { display:flex; justify-content:space-between; align-items:center; margin-bottom: 14px; }
.page-header h2 { margin:0; color:#303133; }
.filter-section { margin-bottom:14px; padding:14px; background:#fffdf2; border:1px solid #fae5b2; border-radius:12px; }
.section-title { display:flex; align-items:center; gap:8px; font-weight:600; color:#303133; margin-bottom:12px; }
.section-title .bar { width:6px; height:16px; background:#f7c948; border-radius:3px; box-shadow:0 2px 6px rgba(0,0,0,.1); }

/* 柔化表格与背景对比 */
.products-table { margin-bottom: 14px; background:#fffdf2; border:1px solid #fae5b2; border-radius:12px; box-shadow:0 2px 10px rgba(0,0,0,.04); padding:10px; }
.products-table :deep(.el-table) { background-color: transparent; }
.products-table :deep(.el-table th) { background-color: var(--brand-light); color:#303133; }
.products-table :deep(.el-table__body tr:hover>td) { background-color: var(--brand-weak) !important; }
.products-table :deep(.el-table--striped .el-table__body tr.el-table__row--striped td){ background-color:#fffaf0; }

.product-image { width:60px; height:60px; object-fit:cover; border-radius:6px; border:1px solid #eee; }
.pagination-section { display:flex; justify-content:center; margin-top: 12px; }
.image-upload-section { display:flex; flex-direction:column; gap:10px; }
.image-uploader { border:1px dashed #f0e9cc; border-radius:10px; cursor:pointer; position:relative; overflow:hidden; width:200px; height:200px; display:flex; justify-content:center; align-items:center; background:#fffef8; }
.image-uploader:hover { border-color:#f7c948; }
.image-uploader-icon { font-size:28px; color:#caa21b; width:200px; height:200px; line-height:200px; text-align:center; }
.uploaded-image { width:200px; height:200px; object-fit:cover; border-radius:10px; }
.image-upload-tips { font-size:12px; color:#666; }
.image-upload-tips p { margin:0 0 8px 0; }
.product-image-cell { display:flex; justify-content:center; align-items:center; height:60px; }
.no-image { width:50px; height:50px; background-color:#fffdf2; border:1px dashed #fae5b2; border-radius:6px; display:flex; align-items:center; justify-content:center; font-size:12px; color:#999; }
:deep(.el-button--warning){ --el-color-warning:#f7c948; --el-color-warning-light-9:#fff7da; }
</style>