<template>
  <div class="favorites-page">
    <el-card class="header-card" shadow="never">
      <div class="title-row">
        <div class="title-left">
          <el-icon color="#f7c948" size="22"><Star /></el-icon>
          <h2>我的收藏</h2>
        </div>
        <div class="title-right">
          <el-button type="warning" plain @click="load">刷新</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="list-card" shadow="never">
      <el-empty v-if="items.length===0" description="暂无收藏" />
      <el-row v-else :gutter="16">
        <el-col v-for="it in items" :key="it.favoriteId" :span="6">
          <el-card class="fav-item" shadow="hover">
            <div class="thumb" @click="goDetail(it.productId)">
              <img :src="it.coverImage" :alt="it.name" />
            </div>
            <div class="info">
              <div class="name" @click="goDetail(it.productId)">{{ it.name }}</div>
              <div class="sub">{{ it.subtitle }}</div>
              <div class="meta">
                <div class="price">¥{{ it.price }} <span class="unit">/ {{ it.unit }}</span></div>
                <el-button size="small" type="danger" plain @click="unfav(it.productId)">取消收藏</el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <div class="pager" v-if="total>size">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="(p:number)=>{page=p;load()}"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listFavorites, removeFavorite, type FavoriteItem } from '@/api/product'
import { Star } from '@element-plus/icons-vue'

const router = useRouter()
const items = ref<FavoriteItem[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(12)

const load = async () => {
  try {
    const res = await listFavorites({ page: page.value, size: size.value })
    items.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    items.value = []
  }
}

const goDetail = (productId: number) => router.push(`/product/${productId}`)

const unfav = async (productId: number) => {
  try {
    await removeFavorite(productId)
    ElMessage.success('已取消收藏')
    load()
  } catch {
    ElMessage.error('操作失败')
  }
}

onMounted(load)
</script>

<style scoped>
.favorites-page { padding: 16px; background: linear-gradient(180deg, #fff9e6 0%, #fff7da 50%, #fffef4 100%); min-height: calc(100vh - 60px); }
.header-card { margin-bottom: 12px; border: 1px solid #fae5b2; background: #fffef8; }
.title-row { display: flex; align-items: center; justify-content: space-between; }
.title-left { display: flex; align-items: center; gap: 8px; }
.title-left h2 { margin: 0; color: #b27a00; }
.list-card { border: 1px solid #fae5b2; background: #fffdf2; }
.fav-item { border: 1px solid #f3eccd; overflow: hidden; }
.thumb { height: 160px; background: #fff; display: flex; align-items: center; justify-content: center; cursor: pointer; }
.thumb img { max-width: 100%; max-height: 100%; object-fit: cover; }
.info { padding: 10px; }
.name { font-weight: 600; color: #303133; line-height: 1.4; cursor: pointer; }
.sub { color: #909399; font-size: 12px; margin: 4px 0 8px; height: 34px; overflow: hidden; }
.meta { display: flex; align-items: center; justify-content: space-between; }
.price { color: #e67e22; font-weight: 700; }
.unit { color: #909399; font-weight: 400; font-size: 12px; }
.pager { display: flex; justify-content: center; padding: 12px 0 0; }
</style>


