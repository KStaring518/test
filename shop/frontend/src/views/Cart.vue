<template>
  <div class="cart-page">
    <!-- Header -->
    <div class="header">
      <div class="header-content">
        <div class="logo">零食商城</div>
        <div class="nav">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/products" class="nav-link">商品列表</router-link>
          <router-link to="/orders" class="nav-link">我的订单</router-link>
        </div>
        <div class="user-info">
          <span>欢迎, {{ userStore.user?.nickname || userStore.user?.username }}</span>
          <el-button type="text" @click="userStore.logout">退出</el-button>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="main-content">
      <div class="cart-container">
        <div class="cart-title">
          <el-icon class="title-ico"><ShoppingCart /></el-icon>
          <h2>我的购物车</h2>
          <el-badge :value="checkedCount" class="checked-badge" v-if="cartItems.length>0" type="warning" />
        </div>
        <el-alert v-if="cartItems.length>0" type="warning" :closable="false" show-icon description="温馨提示：满99元包邮，部分商品库存有限，请尽快结算~" class="top-alert" />

        <!-- Empty Cart -->
        <div v-if="cartItems.length === 0" class="empty-cart">
          <el-empty description="购物车是空的">
            <el-button type="primary" @click="$router.push('/products')">去购物</el-button>
          </el-empty>
        </div>

        <!-- Cart Items -->
        <div v-else class="cart-items">
          <!-- Cart Header -->
          <div class="cart-header">
            <el-checkbox 
              v-model="selectAll" 
              @change="handleSelectAll"
              :indeterminate="isIndeterminate"
            >
              全选
            </el-checkbox>
            <span class="header-product">商品信息</span>
            <span class="header-price">单价</span>
            <span class="header-quantity">数量</span>
            <span class="header-subtotal">小计</span>
            <span class="header-action">操作</span>
          </div>

          <!-- Cart Item List -->
          <div class="cart-item-list">
            <div v-for="item in cartItems" :key="item.id" class="cart-item">
              <el-checkbox 
                v-model="item.checked" 
                @change="() => toggleItem(item)"
              />
              <div class="item-product">
                <img :src="item.product.coverImage || '/placeholder.png'" :alt="item.product.name" class="product-image">
                <div class="product-info">
                  <h4>{{ item.product.name }}</h4>
                  <p>{{ item.product.subtitle }}</p>
                </div>
              </div>
              <div class="item-price">¥{{ item.product.price }}</div>
              <div class="item-quantity">
                <el-input-number 
                  v-model="item.quantity" 
                  :min="1" 
                  :max="item.product.stock"
                  size="small"
                  @change="(value: number) => updateQuantity(item.id, value)"
                />
              </div>
              <div class="item-subtotal">¥{{ (item.product.price * item.quantity).toFixed(2) }}</div>
              <div class="item-action">
                <el-button type="danger" size="small" @click="removeItem(item.id)">删除</el-button>
              </div>
            </div>
          </div>

          <!-- Cart Footer -->
          <div class="cart-footer">
            <div class="cart-summary">
              <span>已选择 {{ checkedCount }} 件商品</span>
              <span class="total-amount">合计: ¥{{ totalAmount.toFixed(2) }}</span>
            </div>
            <div class="cart-actions">
              <el-button @click="clearCart">清空购物车</el-button>
              <el-button type="primary" @click="checkout" :disabled="checkedCount === 0">
                结算 ({{ checkedCount }})
              </el-button>
            </div>
          </div>

          <!-- 吸附底栏 -->
          <div class="checkout-bar">
            <div class="bar-left">
              <el-checkbox 
                v-model="selectAll" 
                @change="handleSelectAll"
                :indeterminate="isIndeterminate"
              >全选</el-checkbox>
              <span class="bar-text">已选 {{ checkedCount }} 件</span>
            </div>
            <div class="bar-right">
              <div class="bar-total">合计：<b>¥{{ totalAmount.toFixed(2) }}</b></div>
              <el-button type="warning" :disabled="checkedCount===0" @click="checkout">去结算</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
  
  <script setup lang="ts">
  import { ref, computed, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { useUserStore } from '@/stores/user'
  import { getCartList, updateCartQuantity, removeCartItem, clearCart as clearCartApi, toggleCartItem } from '@/api/cart'
  import { ShoppingCart } from '@element-plus/icons-vue'
  
  const router = useRouter()
  const userStore = useUserStore()
  
  const cartItems = ref<any[]>([])
  const selectAll = ref(false)
  const isIndeterminate = ref(false)
  
  // Computed properties
  const checkedCount = computed(() => {
    return cartItems.value.filter(item => item.checked).length
  })
  
  const totalAmount = computed(() => {
    return cartItems.value
      .filter(item => item.checked)
      .reduce((total, item) => total + item.product.price * item.quantity, 0)
  })
  
  // Methods
  const loadCart = async () => {
    try {
      const response = await getCartList()
      cartItems.value = response.data
      updateSelectAllState()
    } catch (error) {
      console.error('Failed to load cart:', error)
      ElMessage.error('加载购物车失败')
    }
  }
  
  const updateSelectAllState = () => {
    const checkedItems = cartItems.value.filter(item => item.checked)
    selectAll.value = checkedItems.length === cartItems.value.length && cartItems.value.length > 0
    isIndeterminate.value = checkedItems.length > 0 && checkedItems.length < cartItems.value.length
  }
  
  const handleSelectAll = async (checked: boolean) => {
    try {
      // 仅对状态不一致的项调用后端切换，避免多余请求
      const needToggle = cartItems.value.filter(item => item.checked !== checked)
      for (const item of needToggle) {
        await toggleCartItem(item.id)
        item.checked = checked
      }
      updateSelectAllState()
      ElMessage.success(checked ? '已全选' : '已取消全选')
    } catch (error) {
      console.error('Failed to toggle all:', error)
      ElMessage.error('更新选择状态失败')
      await loadCart()
    }
  }
  
  const toggleItem = async (item: any) => {
    try {
      await toggleCartItem(item.id)
      // 本地已通过 v-model 改变，成功后仅同步整体选择状态
      updateSelectAllState()
    } catch (error) {
      console.error('Failed to toggle item:', error)
      ElMessage.error('更新选择状态失败')
      await loadCart()
    }
  }
  
  const updateQuantity = async (itemId: number, quantity: number) => {
    try {
      await updateCartQuantity(itemId, quantity)
      ElMessage.success('数量更新成功')
    } catch (error) {
      console.error('Failed to update quantity:', error)
      ElMessage.error('数量更新失败')
      await loadCart()
    }
  }
  
  const removeItem = async (itemId: number) => {
    try {
      await ElMessageBox.confirm('确定要删除这个商品吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      await removeCartItem(itemId)
      ElMessage.success('删除成功')
      await loadCart()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('Failed to remove item:', error)
        ElMessage.error('删除失败')
      }
    }
  }
  
  const clearCart = async () => {
    try {
      await ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      await clearCartApi()
      ElMessage.success('购物车已清空')
      await loadCart()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('Failed to clear cart:', error)
        ElMessage.error('清空失败')
      }
    }
  }
  
  const checkout = async () => {
  const checkedItems = cartItems.value.filter(item => item.checked)
  if (checkedItems.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }
  
  // 跳转到结算页面，传递选中的商品ID
  const cartItemIds = checkedItems.map(item => item.id)
  router.push({
    path: '/checkout',
    query: { cartItemIds: cartItemIds.join(',') }
  })
}
  
  onMounted(() => {
    // 恢复登录状态，确保刷新页面后不会丢失登录状态
    userStore.initUser()
    
    loadCart()
  })
  </script>
  
  <style scoped>
.cart-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fff9e6 0%, #fff7da 40%, #fffcef 100%);
}

.header {
  background-color: #fffef8;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid #fae5b2;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: #f7c948;
}

.nav {
  display: flex;
  gap: 20px;
}

.nav-link {
  text-decoration: none;
  color: #5c5c5c;
  font-weight: 500;
}

.nav-link:hover {
  color: #b27a00;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.main-content {
  max-width: 1200px;
  margin: 20px auto 100px; /* 给吸底条留空间 */
  padding: 0 20px;
}

.cart-container {
  background-color: #fffdf2;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #fae5b2;
}

.cart-title { display:flex; align-items:center; gap:8px; }
.title-ico { color:#b38b00; }
.checked-badge { margin-left: 6px; }
.top-alert { margin: 10px 0 0; }

.cart-container h2 {
  margin: 0;
  color: #303133;
}

.empty-cart {
  text-align: center;
  padding: 40px 0;
}

.cart-header {
  display: grid;
  grid-template-columns: 50px 2fr 1fr 1fr 1fr 100px;
  gap: 20px;
  padding: 15px 0;
  border-bottom: 1px solid #f0e9cc;
  font-weight: 500;
  color: #7a7a7a;
}

.cart-item {
  display: grid;
  grid-template-columns: 50px 2fr 1fr 1fr 1fr 100px;
  gap: 20px;
  padding: 20px 0;
  border-bottom: 1px solid #f3eccd;
  align-items: center;
}

.item-product {
  display: flex;
  align-items: center;
  gap: 15px;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
}

.product-info h4 { margin: 0 0 5px 0; color: #333; }
.product-info p { margin: 0; color: #9a9a9a; font-size: 14px; }

.item-price,
.item-subtotal { font-weight: 600; color: #e67e22; }

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  margin-top: 10px;
  border-top: 1px solid #f0e9cc;
}

.cart-summary { display: flex; flex-direction: column; gap: 5px; }
.total-amount { font-size: 18px; font-weight: 700; color: #e67e22; }
.cart-actions { display: flex; gap: 10px; }

/* 吸附结算条 */
.checkout-bar {
  position: fixed;
  left: 0; right: 0; bottom: 0;
  background: rgba(255, 250, 240, .98);
  border-top: 1px solid #fae5b2;
  box-shadow: 0 -2px 8px rgba(0,0,0,.04);
  padding: 12px 20px;
}
.checkout-bar .bar-left { max-width:1200px; margin:0 auto; display:flex; align-items:center; gap:12px; }
.checkout-bar .bar-right { position: absolute; right: 20px; top: 12px; display:flex; align-items:center; gap:12px; }
.checkout-bar .bar-total b { color:#e67e22; font-size:18px; }

/* 黄色主题按钮 */
:deep(.el-button--warning) { --el-color-warning:#f7c948; --el-color-warning-light-9:#fff7da; }
</style>


