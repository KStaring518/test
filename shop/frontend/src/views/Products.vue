<template>
  <div class="products-page">
    <!-- 导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo">
          <h2>零食商城</h2>
        </div>
        
        <el-menu
          mode="horizontal"
          :router="true"
          class="nav-menu"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/products">商品列表</el-menu-item>
          
          <template v-if="userStore.isLoggedIn()">
            <CartNavItem />
            <el-menu-item index="/orders">我的订单</el-menu-item>
            <el-menu-item index="/favorites">收藏</el-menu-item>
            <el-menu-item v-if="userStore.user?.role === 'USER'" index="/merchant-register">申请成为商家</el-menu-item>
            <el-menu-item v-if="userStore.user?.role === 'MERCHANT'" index="/merchant">商家后台</el-menu-item>
            <el-menu-item v-if="userStore.user?.role === 'ADMIN'" index="/admin">管理后台</el-menu-item>
          </template>
        </el-menu>
        
        <div class="user-info">
          <template v-if="userStore.isLoggedIn()">
            <span>欢迎，{{ userStore.user?.nickname || userStore.user?.username }}</span>
            <el-button @click="$router.push('/account')" type="text">个人中心</el-button>
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
      <!-- 筛选栏 -->
      <div class="filter-section">
        <el-row :gutter="20" align="middle">
          <el-col :span="6">
            <el-select v-model="filters.categoryId" placeholder="选择分类" clearable @change="handleFilter" @clear="onCategoryClear">
              <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
            </el-select>
          </el-col>
          <el-col :span="8">
            <el-input v-model="filters.keyword" placeholder="搜索商品" clearable @keyup.enter="handleFilter">
              <template #append>
                <el-button type="warning" @click="handleFilter">搜索</el-button>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-select v-model="filters.sort" placeholder="排序方式" @change="handleFilter">
              <el-option label="默认排序" value="" />
              <el-option label="价格从低到高" value="price_asc" />
              <el-option label="价格从高到低" value="price_desc" />
              <el-option label="最新上架" value="created_desc" />
            </el-select>
          </el-col>
          <el-col :span="4" class="view-switch">
            <el-segmented v-model="viewMode" :options="viewOptions" size="small" />
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top:12px;" align="middle">
          <el-col :span="12">
            <div class="price-range">
              <span class="label">价格范围</span>
              <el-slider v-model="priceRange" range :min="0" :max="199" :step="1" style="max-width:320px" @change="handleFilter" />
              <span class="price-hint">¥{{ priceRange[0] }} - ¥{{ priceRange[1] }}</span>
            </div>
          </el-col>
          <el-col :span="12" class="toolbar-right">
            <el-button link type="primary" @click="clearFilters">重置筛选</el-button>
          </el-col>
        </el-row>
        <div class="quick-chips" v-if="categories.length">
          <el-scrollbar>
            <div class="chips-row">
              <el-check-tag
                v-for="c in categories"
                :key="c.id"
                :checked="filters.categoryId===c.id"
                @change="() => { filters.categoryId = filters.categoryId===c.id ? null : c.id; handleFilter() }"
              >
                <span class="chip-dot" :style="{ background: pickColor(c.id) }"></span>{{ c.name }}
              </el-check-tag>
            </div>
          </el-scrollbar>
        </div>
        <div class="hint-bar">
          <el-icon><InfoFilled /></el-icon>
          支持分类、关键词、价格区间与排序组合筛选；满99元包邮。
        </div>
        <div class="active-filters" v-if="hasActiveFilter">
          <span>已选：</span>
          <el-tag v-if="filters.categoryId" closable @close="onCategoryClear">{{ currentCategoryName }}</el-tag>
          <el-tag v-if="filters.keyword" closable @close="() => { filters.keyword=''; handleFilter() }">关键词：{{ filters.keyword }}</el-tag>
          <el-tag v-if="filters.sort" closable @close="() => { filters.sort=''; handleFilter() }">排序：{{ sortLabel }}</el-tag>
          <el-tag v-if="priceRangeChanged" closable @close="resetPrice">价格：¥{{ priceRange[0] }}-¥{{ priceRange[1] }}</el-tag>
        </div>
      </div>
      
      <!-- 商品列表（骨架屏） -->
      <div v-if="loading" class="products-grid">
        <el-row :gutter="20">
          <el-col :span="6" v-for="n in 8" :key="n">
            <el-card class="product-card" shadow="never">
              <el-skeleton :rows="4" animated />
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 商品列表 -->
      <div v-else-if="products.length > 0" class="products-grid">
        <el-row :gutter="20">
          <el-col :span="6" v-for="product in products" :key="product.id">
            <el-card :class="['product-card', viewMode]" shadow="hover" @click="goToProductDetail(product.id)">
              <div class="image-wrap">
                <img :src="product.coverImage" :alt="product.name" :class="['product-image', viewMode]">
                <el-tag v-if="product.stock>50" size="small" type="success" class="badge">充足</el-tag>
                <el-tag v-else-if="product.stock>0" size="small" type="warning" class="badge">紧俏</el-tag>
                <el-tag v-else size="small" type="danger" class="badge">售罄</el-tag>
              </div>
              <div class="product-info">
                <h4>{{ product.name }}</h4>
                <p class="product-subtitle">{{ product.subtitle }}</p>
                <div class="price-row">
                  <span class="product-price">¥{{ product.price }}</span>
                  <span class="product-stock">库存: {{ product.stock }}</span>
                </div>
              </div>
              <!-- 悬浮操作按钮 -->
              <div class="card-actions" @click.stop>
                <el-button size="small" type="warning" :disabled="product.stock===0" @click="quickAddToCart(product)">加入购物车</el-button>
                <el-button size="small" @click="goToProductDetail(product.id)">查看详情</el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 无结果提示 -->
      <el-empty v-else description="未找到相关商品，试试调整搜索关键词或清空筛选">
        <el-button type="primary" @click="clearFilters">清空筛选</el-button>
      </el-empty>
      
      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[8, 16, 24, 32]"
          layout="total, prev, pager, next, sizes"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getCategories, getProducts } from '@/api/product'
import type { Category, Product } from '@/api/product'
import { InfoFilled } from '@element-plus/icons-vue'
import { addToCart as addToCartApi } from '@/api/cart'
import CartNavItem from '@/components/CartNavItem.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const categories = ref<Category[]>([])
const products = ref<Product[]>([])
const loading = ref(false)

const filters = reactive({
  categoryId: null as number | null,
  keyword: '',
  sort: ''
})

const pagination = reactive({
  page: 1,
  size: 8,
  total: 0
})

const priceRange = ref<[number, number]>([0, 199])
const defaultPriceRange: [number, number] = [0, 199]
const priceRangeChanged = computed(() => priceRange.value[0] !== defaultPriceRange[0] || priceRange.value[1] !== defaultPriceRange[1])

const viewMode = ref<'grid' | 'compact'>('grid')
const viewOptions = [
  { label: '大卡片', value: 'grid' },
  { label: '紧凑', value: 'compact' }
]

const palette = ['#ffe08a', '#8bd3dd', '#ffd7ba', '#caffbf', '#bdb2ff']
const pickColor = (id: number) => palette[id % palette.length]

const currentCategoryName = computed(() => categories.value.find(c => c.id === filters.categoryId)?.name || '')
const sortLabel = computed(() => ({ 'price_asc': '价格从低到高', 'price_desc': '价格从高到低', 'created_desc': '最新上架' } as any)[filters.sort] || '默认排序')
const hasActiveFilter = computed(() => !!(filters.categoryId || filters.keyword || filters.sort || priceRangeChanged.value))

const loadCategories = async () => {
  try {
    const response = await getCategories()
    categories.value = response.data || []
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const loadProducts = async () => {
  try {
    loading.value = true
    const params: any = {
      page: pagination.page,
      size: pagination.size,
      ...filters
    }
    // 价格区间（后端若不支持会忽略）
    params.minPrice = priceRange.value[0]
    params.maxPrice = priceRange.value[1]
    const cleanParams = Object.fromEntries(
      Object.entries(params).filter(([key, value]) => value !== null && value !== undefined && value !== '')
    )
    const response = await getProducts(cleanParams)
    products.value = response.data?.list || []
    pagination.total = response.data?.total || 0
  } catch (error) {
    console.error('加载商品失败:', error)
  } finally {
    loading.value = false
  }
}

const handleFilter = () => { pagination.page = 1; loadProducts() }
const onCategoryClear = () => { filters.categoryId = null; handleFilter() }
const handleSizeChange = (size: number) => { pagination.size = size; pagination.page = 1; loadProducts() }
const handleCurrentChange = (page: number) => { pagination.page = page; loadProducts() }
const resetPrice = () => { priceRange.value = [...defaultPriceRange]; handleFilter() }
const clearFilters = () => { filters.categoryId = null; filters.keyword = ''; filters.sort = ''; resetPrice() }
const goToProductDetail = (productId: number) => { router.push(`/product/${productId}`) }
const handleLogout = () => { userStore.logout(); ElMessage.success('退出成功'); router.push('/login') }

const quickAddToCart = async (product: Product) => {
  if (!userStore.isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!product.stock || product.stock <= 0) {
    ElMessage.warning('该商品暂时缺货')
    return
  }
  try {
    await addToCartApi({ productId: product.id, quantity: 1 })
    ElMessage.success('已加入购物车')
    window.dispatchEvent(new Event('cart-updated'))
  } catch (e) {
    ElMessage.error('加入购物车失败')
  }
}

watch(() => route.query.categoryId, (newCategoryId) => {
  if (newCategoryId) { filters.categoryId = parseInt(newCategoryId as string); handleFilter() }
}, { immediate: true })

onMounted(() => { userStore.initUser(); loadCategories(); loadProducts() })
</script>

<style scoped>
.products-page { min-height: 100vh; background: linear-gradient(180deg,#fff9e6 0%,#fffef8 60%,#ffffff 100%); }
.header { background-color: #fffef8; box-shadow: 0 2px 4px rgba(0,0,0,.08); position: fixed; top: 0; left: 0; right: 0; z-index: 1000; border-bottom: 1px solid #fae5b2; }
.header-content { display: flex; align-items: center; justify-content: space-between; height: 100%; }
.logo h2 { margin: 0; color: #f7c948; }
.nav-menu { flex: 1; margin: 0 50px; }
.user-info { display: flex; align-items: center; gap: 10px; }
.main-content { margin-top: 60px; padding: 20px; }

.filter-section { margin-bottom: 20px; padding: 16px; background: #fffdf2; border: 1px solid #fae5b2; border-radius: 12px; }
.hint-bar { margin-top: 8px; color:#7a6c00; display:flex; align-items:center; gap:6px; font-size:13px; }
.quick-chips{ margin-top:10px }
.chips-row{ display:flex; gap:8px; align-items:center }
.chip-dot{ display:inline-block; width:8px; height:8px; border-radius:50%; margin-right:6px }
.price-range{ display:flex; align-items:center; gap:10px }
.price-range .label{ color:#8a6d00; font-size:13px }
.price-hint{ color:#8d8d8d; font-size:12px }
.view-switch{ display:flex; justify-content:flex-end }
.active-filters{ margin-top:8px; display:flex; align-items:center; gap:8px }
.toolbar-right{ display:flex; justify-content:flex-end }

.products-grid { margin-bottom: 30px; }
.product-card { cursor: pointer; transition: transform .25s ease, box-shadow .25s ease; margin-bottom: 20px; border-radius: 12px; overflow:hidden; position: relative; }
.product-card:hover { transform: translateY(-6px); box-shadow: 0 10px 24px rgba(0,0,0,.08); }
.image-wrap { position: relative; }
.badge { position: absolute; left: 8px; top: 8px; }
.product-image.grid { width: 100%; height: 200px; object-fit: cover; }
.product-image.compact{ width:100%; height: 160px; object-fit: cover }
.product-info { padding: 12px 12px 14px; background:#fff; }
.product-info h4 { margin: 0 0 6px 0; font-size: 16px; color: #303133; }
.product-subtitle { margin: 0 0 10px 0; font-size: 13px; color: #909399; }
.price-row { display:flex; align-items:center; justify-content: space-between; }
.product-price { font-size: 18px; font-weight: 700; color: #e67e22; background: #fff7da; padding: 2px 8px; border-radius: 6px; }
.product-stock { font-size: 12px; color: #909399; }

/* 悬浮操作按钮 */
.card-actions { position: absolute; inset: auto 12px 12px 12px; display:flex; gap:8px; opacity:0; transform: translateY(6px); transition: all .2s ease; }
.product-card:hover .card-actions { opacity:1; transform: translateY(0); }

.pagination-section { display: flex; justify-content: center; margin-top: 30px; }
:deep(.el-button--warning){ --el-color-warning:#f7c948; --el-color-warning-light-9:#fff7da; }
</style>
