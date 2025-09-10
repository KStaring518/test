<template>
  <div class="home">
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
    
    <!-- 主要内容：先显示轮播，再显示左侧分类与商品 -->
    <el-main class="main-content">
      <!-- 轮播图（覆盖整行，位于分类与商品上方） -->
      <el-card class="banner-card">
        <el-carousel height="250px" class="banner" indicator-position="outside" type="card">
          <el-carousel-item v-for="item in homeBanners" :key="item.id">
            <img :src="item.imageUrl" :alt="item.title" class="banner-image" @click="handleBannerClick(item)">
          </el-carousel-item>
        </el-carousel>
      </el-card>

      <!-- 左右布局 -->
      <el-row :gutter="16" class="home-layout">
        <!-- 左侧分类导航 -->
        <el-col :span="5" class="left-nav">
          <el-card class="left-card">
            <template #header>
              <div class="left-title">
                <span class="bar"></span>商品分类
                <el-button text class="collapse-btn" @click="leftCollapsed = !leftCollapsed">
                  <el-icon><component :is="leftCollapsed ? Expand : Fold" /></el-icon>
                </el-button>
              </div>
            </template>
            <div v-show="!leftCollapsed">
              <el-input v-model="catKeyword" placeholder="搜索分类" clearable size="small" class="cat-search" />
              <el-tree
                class="cat-tree"
                :data="categoryTreeForNav"
                node-key="id"
                :props="{ label: 'name' }"
                highlight-current
                @node-click="onCategoryNodeClick"
              >
                <template #default="{ data }">
                  <div class="tree-row">
                    <span class="tree-dot" :style="{ background: pickColor(data.id) }"></span>
                    <span class="tree-label">{{ data.name }}</span>
                  </div>
                </template>
              </el-tree>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧主要内容 -->
        <el-col :span="19">
          <!-- 分类快捷入口（横向滑动） -->
          <div class="quick-cats" v-if="categories.length">
            <el-scrollbar>
              <div class="quick-row">
                <div class="quick-item" v-for="c in categories" :key="c.id" @click="goToProducts(c.id)">
                  <span class="dot" :style="{ background: pickColor(c.id) }"></span>
                  <span class="qi-text">{{ c.name }}</span>
                </div>
              </div>
            </el-scrollbar>
          </div>

          <!-- 商品切换标签（推荐/新品/特惠） -->
          <div class="products-section">
            <el-tabs v-model="activeTab" @tab-change="onTabChange">
              <el-tab-pane label="推荐" name="rec">
                <el-row :gutter="20">
                  <el-col :span="6" v-for="p in productsRec" :key="p.id">
                    <el-card class="product-card" shadow="hover" @click="goToProductDetail(p.id)">
                      <div class="image-wrap">
                        <img :src="p.coverImage" :alt="p.name" class="product-image">
                      </div>
                      <div class="product-info">
                        <h4>{{ p.name }}</h4>
                        <p class="product-subtitle">{{ p.subtitle }}</p>
                        <div class="price-row">
                          <span class="product-price">¥{{ p.price }}</span>
                        </div>
                      </div>
                    </el-card>
                  </el-col>
                </el-row>
              </el-tab-pane>
              <el-tab-pane label="新品" name="new">
                <el-row :gutter="20">
                  <el-col :span="6" v-for="p in productsNew" :key="p.id">
                    <el-card class="product-card" shadow="hover" @click="goToProductDetail(p.id)">
                      <div class="image-wrap">
                        <img :src="p.coverImage" :alt="p.name" class="product-image">
                      </div>
                      <div class="product-info">
                        <h4>{{ p.name }}</h4>
                        <p class="product-subtitle">{{ p.subtitle }}</p>
                        <div class="price-row">
                          <span class="product-price">¥{{ p.price }}</span>
                        </div>
                      </div>
                    </el-card>
                  </el-col>
                </el-row>
              </el-tab-pane>
              <el-tab-pane label="特惠" name="cheap">
                <el-row :gutter="20">
                  <el-col :span="6" v-for="p in productsCheap" :key="p.id">
                    <el-card class="product-card" shadow="hover" @click="goToProductDetail(p.id)">
                      <div class="image-wrap">
                        <img :src="p.coverImage" :alt="p.name" class="product-image">
                      </div>
                      <div class="product-info">
                        <h4>{{ p.name }}</h4>
                        <p class="product-subtitle">{{ p.subtitle }}</p>
                        <div class="price-row">
                          <span class="product-price">¥{{ p.price }}</span>
                        </div>
                      </div>
                    </el-card>
                  </el-col>
                </el-row>
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-col>
      </el-row>
    </el-main>
    
    <!-- 返回顶部 -->
    <el-backtop :right="24" :bottom="24" />
    
    <!-- 智能客服组件 -->
    <ChatWidget />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getCategories, getProducts } from '@/api/product'
import { normalizeImageUrl } from '@/api/upload'
import type { Category, Product } from '@/api/product'
import ChatWidget from '@/components/ChatWidget.vue'
import { Fold, Expand } from '@element-plus/icons-vue'
import CartNavItem from '@/components/CartNavItem.vue'

const router = useRouter()
const userStore = useUserStore()

const categories = ref<Category[]>([])
const catKeyword = ref('')
const leftCollapsed = ref(false)
const banners = ref<any[]>([])
const palette = ['#ffe08a', '#8bd3dd', '#ffd7ba', '#caffbf', '#bdb2ff']
const pickColor = (id: number) => palette[id % palette.length]

// 顶层分类 + 搜索
const categoryTreeForNav = computed(() => {
  const kw = catKeyword.value.trim().toLowerCase()
  const top = (categories.value || []).map(c => ({ id: c.id, name: c.name }))
  return kw ? top.filter(n => n.name.toLowerCase().includes(kw)) : top
})

const onCategoryNodeClick = (node: any) => { goToProducts(node.id) }

// 首页轮播（仅启用 + 排序）
const homeBanners = computed(() => {
  return (banners.value || [])
    .filter((b: any) => b.status === 'ENABLED')
    .sort((a: any, b: any) => (a.sortOrder || 0) - (b.sortOrder || 0))
})

// 商品三组
const activeTab = ref<'rec' | 'new' | 'cheap'>('rec')
const productsRec = ref<Product[]>([])
const productsNew = ref<Product[]>([])
const productsCheap = ref<Product[]>([])

const loadBanners = async () => {
  try {
    const res = await fetch('/banners/public/list', { headers: { Authorization: localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : '' } })
    const json = await res.json()
    const list = json.data || []
    banners.value = list.map((b: any) => ({ ...b, imageUrl: normalizeImageUrl(b.imageUrl) }))
  } catch {}
}

const handleBannerClick = (item: any) => {
  const url = item.linkUrl || ''
  if (!url) return
  if (url.startsWith('http')) {
    window.open(url, '_blank', 'noopener,noreferrer')
  } else {
    router.push(url)
  }
}

const loadCategories = async () => {
  try { const response = await getCategories(); categories.value = response.data || [] } catch {}
}

const loadProductsGroup = async () => {
  try {
    const r = await getProducts({ page: 1, size: 8 })
    productsRec.value = r.data?.list || []
    const n = await getProducts({ page: 1, size: 8, sort: 'created_desc' })
    productsNew.value = n.data?.list || []
    const c = await getProducts({ page: 1, size: 8, sort: 'price_asc' })
    productsCheap.value = c.data?.list || []
  } catch {}
}

const goToProducts = (categoryId?: number) => { router.push({ path: '/products', query: categoryId ? { categoryId: String(categoryId) } : {} }) }
const goToProductDetail = (productId: number) => { router.push(`/product/${productId}`) }
const handleLogout = () => { userStore.logout(); ElMessage.success('退出成功'); router.push('/login') }
const onTabChange = () => { /* 数据已预取，切换不再额外请求 */ }

onMounted(() => { userStore.initUser(); loadCategories(); loadProductsGroup(); loadBanners() })
</script>

<style scoped>
.home { min-height: 100vh; background: linear-gradient(180deg,#fff9e6 0%,#fffef8 60%,#ffffff 100%); }
.header { background-color: #fffef8; box-shadow: 0 2px 4px rgba(0,0,0,.08); position: fixed; top: 0; left: 0; right: 0; z-index: 1000; border-bottom: 1px solid #fae5b2; }
.header-content { display: flex; align-items: center; justify-content: space-between; height: 100%; }
.logo h2 { margin: 0; color: #f7c948; }
.nav-menu { flex: 1; margin: 0 50px; }
.user-info { display: flex; align-items: center; gap: 10px; }
.main-content { margin-top: 60px; padding: 20px; }

.banner-card{ border:1px solid #fae5b2; border-radius:12px; margin-bottom:16px; overflow:hidden; background:#fffef8 }
.banner { border-radius: 12px; overflow: hidden; }
.banner-image { width: 100%; height: 250px; object-fit: cover; cursor:pointer; }

.home-layout { align-items: stretch }
.left-nav { position: relative }
.left-card{ position: sticky; top: 76px; border:1px solid #f0e9cc; border-radius:12px; margin-top: 76px }
.left-title{ display:flex; align-items:center; gap:8px; font-weight:600; color:#303133 }
.left-title .bar{ width:6px; height:16px; background:#f7c948; border-radius:3px; box-shadow:0 2px 6px rgba(0,0,0,.1) }
.collapse-btn{ margin-left:auto }
.cat-search{ margin-bottom:8px }
.cat-tree{ max-height: 520px; overflow:auto }
.tree-row{ display:flex; align-items:center; gap:8px; padding:4px 0 }
.tree-dot{ width:8px; height:8px; border-radius:50% }
.tree-label{ flex:1 }

.quick-cats { background:#fffdf2; border:1px solid #fae5b2; border-radius:12px; padding:10px 8px; margin-bottom: 20px; }
.quick-row { display:flex; gap:10px; align-items:center; }
.quick-item { display:flex; align-items:center; gap:8px; padding:6px 12px; border-radius:999px; background:#fff; border:1px solid #e9e2c6; color:#444; cursor:pointer; transition: all .2s ease; box-shadow: 0 0 0 rgba(0,0,0,0); }
.quick-item:hover { box-shadow: 0 6px 14px rgba(0,0,0,.06); transform: translateY(-1px); }
.dot { width:8px; height:8px; border-radius:50%; display:inline-block; }
.qi-text { font-weight: 500; font-size: 13px; }

.products-section { margin-top: 16px; }
.product-card { cursor: pointer; transition: transform .25s ease, box-shadow .25s ease; margin-bottom: 20px; border-radius:12px; overflow:hidden; position:relative; }
.product-card:hover { transform: translateY(-6px); box-shadow: 0 10px 24px rgba(0,0,0,.08); }
.image-wrap { position: relative; }
.product-image { width: 100%; height: 200px; object-fit: cover; }
.product-info { padding: 12px; background:#fff; }
.product-info h4 { margin: 0 0 6px 0; font-size: 16px; color: #303133; }
.product-subtitle { margin: 0 0 10px 0; font-size: 13px; color: #909399; }
.price-row { display:flex; align-items:center; justify-content:flex-start; gap:8px; }
.product-price { font-size: 18px; font-weight: 700; color: #e67e22; background: #fff7da; padding: 2px 8px; border-radius: 6px; }
</style>