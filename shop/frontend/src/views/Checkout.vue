<template>
    <div class="checkout-page">
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
            <el-menu-item index="/cart">购物车</el-menu-item>
            <el-menu-item index="/orders">我的订单</el-menu-item>
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
        <div class="checkout-container">
          <h2>订单结算</h2>
          
          <!-- 收货地址 -->
          <div class="address-section">
            <h3>收货地址</h3>
            <div v-if="addresses.length === 0" class="no-address">
              <el-empty description="暂无收货地址">
                <el-button type="primary" @click="addAddress">添加地址</el-button>
              </el-empty>
            </div>
            <div v-else class="address-list">
              <el-radio-group v-model="selectedAddressId">
                <div v-for="address in addresses" :key="address.id" class="address-item">
                  <el-radio :label="address.id">
                    <div class="address-info">
                      <div class="address-name">{{ address.receiverName }} {{ address.phone }}</div>
                      <div class="address-detail">{{ address.province }} {{ address.city }} {{ address.district }} {{ address.detail }}</div>
                    </div>
                  </el-radio>
                </div>
              </el-radio-group>
            </div>
          </div>
          
          <!-- 商品清单 -->
          <div class="products-section">
            <h3>商品清单</h3>
            <div class="product-list">
              <div v-for="item in selectedItems" :key="item.id" class="product-item">
                <img :src="item.product.coverImage" :alt="item.product.name" class="product-image">
                <div class="product-info">
                  <h4>{{ item.product.name }}</h4>
                  <p>{{ item.product.subtitle }}</p>
                  <p class="product-price">¥{{ item.product.price }} × {{ item.quantity }}</p>
                </div>
                <div class="product-subtotal">
                  ¥{{ (item.product.price * item.quantity).toFixed(2) }}
                </div>
              </div>
            </div>
          </div>
          
          <!-- 订单总计 -->
          <div class="order-summary">
            <div class="summary-item">
              <span>商品总价：</span>
              <span>¥{{ totalAmount.toFixed(2) }}</span>
            </div>
            <div class="summary-item">
              <span>运费：</span>
              <span>¥0.00</span>
            </div>
            <div class="summary-item total">
              <span>实付金额：</span>
              <span>¥{{ totalAmount.toFixed(2) }}</span>
            </div>
          </div>
          
          <!-- 提交订单 -->
          <div class="submit-section">
            <el-button 
              type="primary" 
              size="large" 
              :disabled="!selectedAddressId || selectedItems.length === 0"
              @click="submitOrder"
              :loading="submitting"
            >
              提交订单
            </el-button>
          </div>
        </div>
      </el-main>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, computed, onMounted } from 'vue'
  import { useRouter, useRoute } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import { useUserStore } from '@/stores/user'
  import { getCartList } from '@/api/cart'
  import { createOrder } from '@/api/order'
  import { getAddresses } from '@/api/address'
  import type { CartItem } from '@/api/cart'
  import type { Address } from '@/api/address'
  
  const router = useRouter()
  const route = useRoute()
  const userStore = useUserStore()
  
  const addresses = ref<Address[]>([])
  const selectedAddressId = ref<number | null>(null)
  const selectedItems = ref<CartItem[]>([])
  const submitting = ref(false)
  
  // 计算总金额
  const totalAmount = computed(() => {
    return selectedItems.value.reduce((total, item) => {
      return total + item.product.price * item.quantity
    }, 0)
  })
  
  // 加载购物车商品
  const loadCartItems = async () => {
    try {
      const cartItemIds = route.query.cartItemIds as string
      if (!cartItemIds) {
        ElMessage.error('参数错误')
        router.push('/cart')
        return
      }
      
      const response = await getCartList()
      const allItems = response.data || []
      const selectedIds = cartItemIds.split(',').map(id => parseInt(id))
      
      selectedItems.value = allItems.filter(item => selectedIds.includes(item.id))
    } catch (error) {
      console.error('加载购物车商品失败:', error)
      ElMessage.error('加载购物车商品失败')
    }
  }
  
  // 加载收货地址
  const loadAddresses = async () => {
    try {
      const response = await getAddresses()
      addresses.value = response.data || []
      
      // 如果没有地址，创建一个默认地址
      if (addresses.value.length === 0) {
        ElMessage.warning('您还没有收货地址，请先添加地址')
        return
      }
      
      // 选择默认地址或第一个地址
      const defaultAddress = addresses.value.find(addr => addr.isDefault)
      selectedAddressId.value = defaultAddress ? defaultAddress.id : addresses.value[0].id
    } catch (error) {
      console.error('加载地址失败:', error)
      ElMessage.error('加载地址失败')
    }
  }
  
  // 添加地址
  const addAddress = () => {
    router.push('/address')
  }
  
  // 提交订单
  const submitOrder = async () => {
    if (!selectedAddressId.value) {
      ElMessage.warning('请选择收货地址')
      return
    }
    
    if (selectedItems.value.length === 0) {
      ElMessage.warning('请选择要购买的商品')
      return
    }
    
    submitting.value = true
    try {
      const cartItemIds = selectedItems.value.map(item => item.id)
      const response = await createOrder({
        addressId: selectedAddressId.value,
        cartItemIds: cartItemIds
      })
      
      ElMessage.success('订单创建成功，请完成支付')
      // 跳转到支付页面并携带订单号与金额
      const orderNo = (response.data && (response.data.orderNo)) || ''
      const amount = (response.data && (response.data.totalAmount)) || totalAmount.value
      router.push({
        path: '/payment',
        query: { orderNo: String(orderNo), totalAmount: String(amount) }
      })
    } catch (error) {
      console.error('创建订单失败:', error)
      ElMessage.error('创建订单失败')
    } finally {
      submitting.value = false
    }
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
    
    loadCartItems()
    loadAddresses()
  })
  </script>
  
  <style scoped>
  .checkout-page {
    min-height: 100vh;
  }
  
  .header {
    background-color: #fff;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 1000;
  }
  
  .header-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 100%;
  }
  
  .logo h2 {
    margin: 0;
    color: #409eff;
  }
  
  .nav-menu {
    flex: 1;
    margin: 0 50px;
  }
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 10px;
  }
  
  .main-content {
    margin-top: 60px;
    padding: 20px;
  }
  
  .checkout-container {
    max-width: 800px;
    margin: 0 auto;
  }
  
  .checkout-container h2 {
    margin-bottom: 20px;
    color: #333;
  }
  
  .address-section,
  .products-section {
    background-color: #fff;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
  
  .address-section h3,
  .products-section h3 {
    margin-bottom: 15px;
    color: #333;
  }
  
  .no-address {
    text-align: center;
    padding: 20px 0;
  }
  
  .address-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  
  .address-item {
    padding: 15px;
    border: 1px solid #eee;
    border-radius: 6px;
    cursor: pointer;
  }
  
  .address-item:hover {
    border-color: #409eff;
  }
  
  .address-info {
    margin-left: 10px;
  }
  
  .address-name {
    font-weight: bold;
    color: #333;
    margin-bottom: 5px;
  }
  
  .address-detail {
    color: #666;
    font-size: 14px;
  }
  
  .product-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
  }
  
  .product-item {
    display: flex;
    align-items: center;
    gap: 15px;
    padding: 15px;
    border: 1px solid #eee;
    border-radius: 6px;
  }
  
  .product-image {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border-radius: 4px;
  }
  
  .product-info {
    flex: 1;
  }
  
  .product-info h4 {
    margin: 0 0 5px 0;
    color: #333;
  }
  
  .product-info p {
    margin: 0 0 5px 0;
    color: #666;
    font-size: 14px;
  }
  
  .product-price {
    color: #e74c3c;
    font-weight: bold;
  }
  
  .product-subtotal {
    font-size: 18px;
    font-weight: bold;
    color: #e74c3c;
  }
  
  .order-summary {
    background-color: #fff;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
  
  .summary-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: 10px;
  }
  
  .summary-item.total {
    font-size: 18px;
    font-weight: bold;
    color: #e74c3c;
    border-top: 1px solid #eee;
    padding-top: 10px;
    margin-top: 10px;
  }
  
  .submit-section {
    text-align: center;
    padding: 20px;
  }
  </style>