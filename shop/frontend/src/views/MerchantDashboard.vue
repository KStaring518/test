<template>
  <div class="merchant-dashboard">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h2>商家后台管理</h2>
          <div class="user-info">
            <span>{{ userStore.user?.nickname || userStore.user?.username }}</span>
            <el-button type="text" @click="handleLogout">退出</el-button>
          </div>
        </div>
      </el-header>
      
      <el-container>
        <el-aside width="200px" class="sidebar">
          <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="dashboard">
              <el-icon><DataBoard /></el-icon>
              <span>数据概览</span>
            </el-menu-item>
            <el-menu-item index="products">
              <el-icon><Goods /></el-icon>
              <span>商品管理</span>
            </el-menu-item>
            <el-menu-item index="orders">
              <el-icon><List /></el-icon>
              <span>订单管理</span>
            </el-menu-item>
            <el-menu-item index="settings">
              <el-icon><List /></el-icon>
              <span>店铺设置</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        
        <el-main class="main-content">
          <!-- 数据概览 -->
          <div v-if="activeMenu === 'dashboard'" class="dashboard-content">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-item">
                    <div class="stat-number">{{ statistics.totalProducts }}</div>
                    <div class="stat-label">商品总数</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-item">
                    <div class="stat-number">{{ statistics.onSaleProducts }}</div>
                    <div class="stat-label">在售商品</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-item">
                    <div class="stat-number">{{ statistics.totalOrders }}</div>
                    <div class="stat-label">订单总数</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-item">
                    <div class="stat-number">{{ statistics.pendingOrders }}</div>
                    <div class="stat-label">待处理订单</div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
            
            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :span="12">
                <el-card class="stat-card">
                  <div class="stat-item">
                    <div class="stat-number">¥{{ statistics.totalRevenue }}</div>
                    <div class="stat-label">总收入</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card class="stat-card">
                  <div class="stat-item">
                    <div class="stat-number">¥{{ statistics.todayRevenue }}</div>
                    <div class="stat-label">今日收入</div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
          
          <!-- 商品管理 -->
          <div v-if="activeMenu === 'products'" class="products-content">
            <div class="page-header">
              <h3>商品管理</h3>
              <el-button type="primary" @click="toProductsPage">添加商品</el-button>
            </div>
            
            <el-table :data="products" v-loading="loading" style="width: 100%">
              <el-table-column prop="name" label="商品名称" />
              <el-table-column prop="price" label="价格">
                <template #default="scope">
                  ¥{{ scope.row.price }}
                </template>
              </el-table-column>
              <el-table-column prop="stock" label="库存" />
              <el-table-column prop="status" label="状态">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 'ON_SALE' ? 'success' : 'info'">
                    {{ scope.row.status === 'ON_SALE' ? '在售' : '下架' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="scope">
                  <el-button size="small" @click="toProductsPage">编辑</el-button>
                  <el-button 
                    size="small" 
                    :type="scope.row.status === 'ON_SALE' ? 'warning' : 'success'"
                    @click="onToggleProduct(scope.row)"
                  >
                    {{ scope.row.status === 'ON_SALE' ? '下架' : '上架' }}
                  </el-button>
                  <el-button size="small" type="danger" @click="onDeleteProduct(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              style="margin-top: 20px; text-align: right;"
            />
          </div>
          
          <!-- 订单管理 -->
          <div v-if="activeMenu === 'orders'" class="orders-content">
            <div class="page-header">
              <h3>订单管理</h3>
            </div>
            
            <el-table :data="orders" v-loading="loading" style="width: 100%">
              <el-table-column prop="orderNo" label="订单号" />
              <el-table-column prop="totalAmount" label="金额">
                <template #default="scope">
                  ¥{{ scope.row.totalAmount }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态">
                <template #default="scope">
                  <el-tag :type="getOrderStatusType(scope.row.status)">
                    {{ getOrderStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="创建时间">
                <template #default="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="scope">
                  <el-button 
                    v-if="scope.row.status === 'PAID'"
                    size="small" 
                    type="primary" 
                    @click="viewOrderDetail(scope.row)"
                  >
                    发货
                  </el-button>
                  <el-button size="small" @click="viewOrderDetail(scope.row)">查看</el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              style="margin-top: 20px; text-align: right;"
            />
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DataBoard, Goods, List } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getMerchantStatistics, getMerchantProducts, getMerchantOrders, toggleProductStatus, deleteProduct, shipOrder as shipOrderApi } from '@/api/merchant'

const router = useRouter()
const userStore = useUserStore()

const activeMenu = ref('dashboard')
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const statistics = ref({
  totalProducts: 0,
  onSaleProducts: 0,
  totalOrders: 0,
  pendingOrders: 0,
  totalRevenue: 0,
  todayRevenue: 0
})

const products = ref<any[]>([])
const orders = ref<any[]>([])

const handleMenuSelect = (index: string) => {
  activeMenu.value = index
  if (index === 'dashboard') {
    loadStatistics()
  } else if (index === 'products') {
    loadProducts()
  } else if (index === 'orders') {
    loadOrders()
  }
}

const loadStatistics = async () => {
  try {
    const response = await getMerchantStatistics()
    statistics.value = response.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  }
}

const loadProducts = async () => {
  loading.value = true
  try {
    const response = await getMerchantProducts({
      page: currentPage.value,
      size: pageSize.value
    })
    products.value = response.data.list
    total.value = response.data.total
  } catch (error) {
    console.error('加载商品列表失败:', error)
    ElMessage.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

const loadOrders = async () => {
  loading.value = true
  try {
    const response = await getMerchantOrders({
      page: currentPage.value,
      size: pageSize.value
    })
    orders.value = response.data.list
    total.value = response.data.total
  } catch (error) {
    console.error('加载订单列表失败:', error)
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  if (activeMenu.value === 'products') {
    loadProducts()
  } else if (activeMenu.value === 'orders') {
    loadOrders()
  }
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  if (activeMenu.value === 'products') {
    loadProducts()
  } else if (activeMenu.value === 'orders') {
    loadOrders()
  }
}

const toProductsPage = () => { router.push('/merchant/products') }

// 替代与导入名冲突的方法包装
const onToggleProduct = async (product: any) => {
  try {
    await toggleProductStatus(product.id)
    ElMessage.success('操作成功')
    loadProducts()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const onDeleteProduct = async (product: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await deleteProduct(product.id)
    ElMessage.success('删除成功')
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

// 发货操作改为跳转详情页处理
const shipOrder = async (order: any) => {
  viewOrderDetail(order)
}

const viewOrderDetail = (order: any) => {
  router.push(`/merchant/order/${order.id}`)
}

const getOrderStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'UNPAID': 'warning',
    'PAID': 'primary',
    'SHIPPED': 'success',
    'FINISHED': 'success',
    'CLOSED': 'info'
  }
  return statusMap[status] || 'info'
}

const getOrderStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'UNPAID': '未支付',
    'PAID': '已支付',
    'SHIPPED': '已发货',
    'FINISHED': '已完成',
    'CLOSED': '已关闭'
  }
  return statusMap[status] || status
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleString()
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.merchant-dashboard {
  height: 100vh;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sidebar {
  background-color: #fff;
  border-right: 1px solid #e4e7ed;
}

.sidebar-menu {
  border-right: none;
}

.main-content {
  padding: 20px;
  background-color: #f5f7fa;
}

.dashboard-content {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
}

.stat-card {
  text-align: center;
}

.stat-item {
  padding: 20px;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 10px;
}

.stat-label {
  color: #606266;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.products-content,
.orders-content {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
}
</style>
