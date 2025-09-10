<template>
  <div class="product-detail-page">
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
          <CartNavItem />
          <el-menu-item index="/orders">我的订单</el-menu-item>
          <el-menu-item v-if="userStore.isLoggedIn()" index="/favorites">收藏</el-menu-item>
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
      <!-- 成功提示 -->
      <el-alert
        v-if="showSuccessMessage"
        title="已添加到购物车"
        type="success"
        :closable="false"
        show-icon
        class="success-alert"
      >
        <template #default>
          <div class="success-actions">
            <span>商品已成功添加到购物车</span>
            <el-button type="primary" size="small" @click="$router.push('/cart')">
              查看购物车
            </el-button>
            <el-button size="small" @click="showSuccessMessage = false">
              继续购物
            </el-button>
          </div>
        </template>
      </el-alert>

      <div v-if="product" class="product-detail">
        <el-row :gutter="40">
          <!-- 商品图片 -->
          <el-col :span="12">
            <div class="product-image-section">
              <el-card shadow="never" class="img-card">
                <img :src="product.coverImage" :alt="product.name" class="product-image">
              </el-card>
            </div>
          </el-col>
          
          <!-- 商品信息 -->
          <el-col :span="12">
            <div class="product-info-section">
              <div class="title-line">
                <el-tag type="warning" effect="dark" class="brand-tag">热卖</el-tag>
                <h1>{{ product.name }}</h1>
              </div>
              <p class="subtitle">{{ product.subtitle }}</p>
              <div class="price-section">
                <span class="price">¥{{ product.price }}</span>
                <span class="unit">/ {{ product.unit }}</span>
              </div>
              
              <el-alert type="warning" :closable="false" show-icon class="hint">满99元包邮，库存有限，请尽快下单</el-alert>
              
              <div class="stock-section">
                <span class="stock-label">库存：</span>
                <span class="stock-value">{{ product.stock }}</span>
              </div>
              
              <div class="quantity-section">
                <span class="quantity-label">数量：</span>
                <el-input-number
                  v-model="quantity"
                  :min="1"
                  :max="product.stock"
                  size="large"
                />
              </div>
              
              <div class="action-section">
                <el-button
                  type="warning"
                  size="large"
                  @click="addToCart"
                  :disabled="product.stock === 0"
                >
                  加入购物车
                </el-button>
                <el-button
                  type="primary"
                  size="large"
                  @click="buyNow"
                  :disabled="product.stock === 0"
                >
                  立即购买
                </el-button>
                <el-button
                  :type="isFav ? 'danger' : 'info'"
                  size="large"
                  @click="toggleFavorite"
                >
                  {{ isFav ? '已收藏' : '收藏' }}
                </el-button>
              </div>
            </div>
          </el-col>
        </el-row>
        
        <!-- 商品描述 -->
        <div class="product-description">
          <h3>商品描述</h3>
          <p>{{ product.description }}</p>
        </div>

        <!-- 商品评价 -->
        <div class="product-reviews">
          <div class="review-header-line">
            <h3>商品评价</h3>
            <el-badge :value="reviews.length" type="warning" v-if="reviews.length>0" />
          </div>
          <!-- 评价统计 -->
          <div v-if="reviews.length > 0" class="review-stats">
            <div class="stats-item">
              <span class="stats-label">平均评分：</span>
              <el-rate :model-value="averageRating" disabled />
              <span class="stats-value">{{ averageRating.toFixed(1) }}</span>
            </div>
            <div class="stats-item">
              <span class="stats-label">评价数量：</span>
              <span class="stats-value">{{ reviews.length }}条</span>
            </div>
            <div class="stats-item">
              <span class="stats-label">好评率：</span>
              <el-progress :percentage="goodReviewRate" status="success" :stroke-width="12" style="width:180px" />
              <span class="stats-value">{{ goodReviewRate }}%</span>
            </div>
          </div>
          
          <div v-if="loadingReviews" class="loading-reviews">
            <el-skeleton :rows="4" animated />
          </div>
          <div v-else>
            <el-empty v-if="reviews.length === 0" description="暂无评价" />
            <div v-else class="review-list">
              <div v-for="r in reviews" :key="r.id" class="review-item">
                <div class="review-header">
                  <el-rate :model-value="r.rating" disabled />
                  <span class="reviewer-name">{{ r.isAnonymous ? '匿名用户' : (r.user?.nickname || r.user?.username || '未知用户') }}</span>
                  <span class="review-time">{{ formatDate(r.createdAt) }}</span>
                </div>
                <div class="review-content">{{ r.content }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else class="loading-section">
        <el-skeleton :rows="10" animated />
      </div>
    </el-main>
    
    <!-- 智能客服组件 -->
    <ChatWidget />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import type { Product } from '@/api/product'
import { getProduct, addFavorite, removeFavorite, checkFavorite } from '@/api/product'
import { addToCart as addToCartApi } from '@/api/cart'
import { listReviewsByProduct } from '@/api/review'
import ChatWidget from '@/components/ChatWidget.vue'
import CartNavItem from '@/components/CartNavItem.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const product = ref<Product | null>(null)
const quantity = ref(1)
const showSuccessMessage = ref(false)
const reviews = ref<any[]>([])
const loadingReviews = ref(false)
const isFav = ref(false)

const loadProduct = async () => {
  try {
    const productId = route.params.id
    const response = await getProduct(parseInt(productId as string))
    product.value = response.data
    // 加载评价
    await loadReviews()
    await refreshFav()
  } catch (error) {
    console.error('加载商品详情失败:', error)
    ElMessage.error('加载商品详情失败')
  }
}

const loadReviews = async () => {
  if (!product.value) return
  loadingReviews.value = true
  try {
    const res = await listReviewsByProduct(product.value.id)
    reviews.value = res.data || []
  } catch (e) {
    reviews.value = []
  } finally {
    loadingReviews.value = false
  }
}

const addToCart = async () => {
  if (!userStore.isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    await addToCartApi({
      productId: product.value!.id,
      quantity: quantity.value
    })
    
    showSuccessMessage.value = true
    ElMessage.success('已添加到购物车')
    setTimeout(() => { showSuccessMessage.value = false }, 3000)
    window.dispatchEvent(new Event('cart-updated'))
  } catch (error) {
    console.error('Failed to add to cart:', error)
    ElMessage.error('添加到购物车失败')
  }
}

const buyNow = () => {
  if (!userStore.isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  ElMessage.success('跳转到订单确认页面')
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('退出成功')
  router.push('/login')
}

const formatDate = (dateString: string) => new Date(dateString).toLocaleString('zh-CN')

// 计算平均评分
const averageRating = computed(() => {
  if (reviews.value.length === 0) return 0
  const total = reviews.value.reduce((sum, review) => sum + review.rating, 0)
  return total / reviews.value.length
})

// 计算好评率（4星及以上）
const goodReviewRate = computed(() => {
  if (reviews.value.length === 0) return 0
  const goodReviews = reviews.value.filter(review => review.rating >= 4).length
  return Math.round((goodReviews / reviews.value.length) * 100)
})

onMounted(() => {
  loadProduct()
})

const refreshFav = async () => {
  if (!userStore.isLoggedIn() || !product.value) { isFav.value = false; return }
  try {
    const res = await checkFavorite(product.value.id)
    isFav.value = !!res.data
  } catch { isFav.value = false }
}

const toggleFavorite = async () => {
  if (!userStore.isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!product.value) return
  try {
    if (isFav.value) {
      await removeFavorite(product.value.id)
      isFav.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(product.value.id)
      isFav.value = true
      ElMessage.success('已收藏')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}
</script>

<style scoped>
.product-detail-page { min-height: 100vh; background: linear-gradient(180deg, #fff9e6 0%, #fff7da 50%, #fffef4 100%); }
.header { background-color: #fffef8; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08); position: fixed; top: 0; left: 0; right: 0; z-index: 1000; border-bottom: 1px solid #fae5b2; }
.header-content { display: flex; align-items: center; justify-content: space-between; height: 100%; }
.logo h2 { margin: 0; color: #f7c948; }
.nav-menu { flex: 1; margin: 0 50px; }
.user-info { display: flex; align-items: center; gap: 10px; }
.main-content { margin-top: 60px; padding: 20px; }
.product-detail { max-width: 1200px; margin: 0 auto; }

.product-image-section { text-align: center; }
.img-card { background: #fffdf2; border: 1px solid #fae5b2; }
.product-image { width: 100%; max-width: 420px; height: 420px; object-fit: cover; border-radius: 12px; box-shadow: 0 4px 14px rgba(0, 0, 0, 0.08); }

.product-info-section { padding: 20px; }
.title-line { display:flex; align-items:center; gap:10px; }
.brand-tag { background-color:#f7c948 !important; border-color:#e3b600 !important; color:#5a3d00 !important; }
.product-info-section h1 { margin: 0 0 10px 0; font-size: 26px; color: #303133; }
.subtitle { margin: 0 0 20px 0; font-size: 16px; color: #909399; }
.price-section { margin-bottom: 10px; }
.price { font-size: 30px; font-weight: bold; color: #e67e22; }
.unit { font-size: 16px; color: #909399; }
.hint { margin: 8px 0 16px; background: #fff8e6; border-color:#ffde9e; }

.stock-section, .quantity-section { margin-bottom: 18px; display: flex; align-items: center; gap: 10px; }
.stock-label, .quantity-label { font-size: 16px; color: #606266; }
.stock-value { font-size: 16px; color: #b27a00; font-weight: bold; }
.action-section { display: flex; gap: 15px; margin-top: 20px; }
:deep(.el-button--warning){ --el-color-warning:#f7c948; --el-color-warning-light-9:#fff7da; }

.product-description { margin-top: 30px; padding: 20px; background-color: #fffdf2; border-radius: 12px; border: 1px solid #fae5b2; }
.product-description h3 { margin: 0 0 15px 0; color: #303133; }
.product-description p { margin: 0; line-height: 1.7; color: #606266; }

.product-reviews { margin-top: 20px; padding: 20px; background-color: #fffdf2; border-radius: 12px; border: 1px solid #fae5b2; }
.review-header-line { display:flex; align-items:center; gap:8px; margin-bottom:8px; }
.review-stats { display: flex; gap: 30px; margin-bottom: 16px; padding: 15px; background: #fff; border-radius: 10px; border: 1px solid #f0e9cc; }
.stats-item { display: flex; align-items: center; gap: 8px; }
.stats-label { color: #606266; font-size: 14px; }
.stats-value { color: #b27a00; font-weight: bold; font-size: 14px; }
.review-list { display: flex; flex-direction: column; gap: 16px; }
.review-item { background: #fff; border: 1px solid #f3eccd; border-radius: 10px; padding: 12px 16px; }
.review-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.reviewer-name { font-weight: 600; color: #b27a00; font-size: 14px; }
.review-time { color: #909399; font-size: 12px; }
.review-content { color: #606266; line-height: 1.6; }
.loading-section { max-width: 800px; margin: 0 auto; padding: 20px; }
.success-alert { margin-bottom: 20px; }
.success-actions { display: flex; align-items: center; gap: 10px; }
</style>
