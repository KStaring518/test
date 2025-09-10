<template>
  <el-menu-item index="/cart">
    <template #title>
      <template v-if="userLogged && count>0">
        <el-badge :value="count" type="warning" :offset="[10, 10]">
          <span class="cart-label">购物车</span>
        </el-badge>
      </template>
      <template v-else>
        <span class="cart-label">购物车</span>
      </template>
    </template>
  </el-menu-item>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { getCartSummary } from '@/api/cart'

const count = ref(0)
const userLogged = !!localStorage.getItem('token')

const load = async () => {
  if (!userLogged) { count.value = 0; return }
  try {
    const res = await getCartSummary()
    count.value = res.data?.totalItems || 0
  } catch { count.value = 0 }
}

const onCartUpdated = () => load()
const onVisibility = () => { if (document.visibilityState === 'visible') load() }

onMounted(() => {
  load()
  window.addEventListener('cart-updated', onCartUpdated as EventListener)
  document.addEventListener('visibilitychange', onVisibility)
})

onBeforeUnmount(() => {
  window.removeEventListener('cart-updated', onCartUpdated as EventListener)
  document.removeEventListener('visibilitychange', onVisibility)
})
</script>

<style scoped>
.cart-label { display: inline-block; padding-right: 2px; }
</style>


