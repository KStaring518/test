<template>
  <div class="address-page">
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
          <el-menu-item index="/address">地址管理</el-menu-item>
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
      <div class="address-container">
        <div class="page-header">
          <h2>收货地址管理</h2>
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            添加新地址
          </el-button>
        </div>
        
        <!-- 地址列表 -->
        <div class="address-list">
          <el-card v-for="address in addresses" :key="address.id" class="address-card">
            <div class="address-content">
              <div class="address-info">
                <div class="receiver-info">
                  <span class="name">{{ address.receiverName }}</span>
                  <span class="phone">{{ address.phone }}</span>
                  <el-tag v-if="address.isDefault" type="success" size="small">默认</el-tag>
                </div>
                <div class="address-detail">
                  {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detail }}
                </div>
              </div>
              <div class="address-actions">
                <el-button 
                  v-if="!address.isDefault" 
                  type="primary" 
                  size="small" 
                  @click="setDefaultAddress(address.id)"
                >
                  设为默认
                </el-button>
                <el-button type="warning" size="small" @click="editAddress(address)">
                  编辑
                </el-button>
                <el-button type="danger" size="small" @click="deleteAddress(address.id)">
                  删除
                </el-button>
              </div>
            </div>
          </el-card>
          
          <el-empty v-if="addresses.length === 0" description="暂无收货地址">
            <el-button type="primary" @click="showAddDialog = true">添加第一个地址</el-button>
          </el-empty>
        </div>
      </div>
    </el-main>
    
    <!-- 添加/编辑地址对话框 -->
    <el-dialog 
      v-model="showAddDialog" 
      :title="editingAddress ? '编辑地址' : '添加新地址'"
      width="500px"
    >
      <el-form 
        ref="addressFormRef" 
        :model="addressForm" 
        :rules="addressRules" 
        label-width="100px"
      >
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="所在地区" required>
          <el-row :gutter="10">
            <el-col :span="8">
              <el-select v-model="addressForm.province" placeholder="省份" @change="handleProvinceChange">
                <el-option label="北京市" value="北京市" />
                <el-option label="上海市" value="上海市" />
                <el-option label="广东省" value="广东省" />
                <el-option label="江苏省" value="江苏省" />
                <el-option label="浙江省" value="浙江省" />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="addressForm.city" placeholder="城市" @change="handleCityChange">
                <el-option 
                  v-for="city in cities" 
                  :key="city" 
                  :label="city" 
                  :value="city" 
                />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="addressForm.district" placeholder="区县">
                <el-option 
                  v-for="district in districts" 
                  :key="district" 
                  :label="district" 
                  :value="district" 
                />
              </el-select>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input 
            v-model="addressForm.detail" 
            type="textarea" 
            :rows="3"
            placeholder="请输入详细地址，如街道、门牌号等" 
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAddress" :loading="submitting">
          {{ editingAddress ? '更新' : '添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getAddresses, createAddress, deleteAddress as deleteAddressApi, updateAddress, setDefaultAddress as setDefaultAddressApi } from '@/api/address'
import type { Address, AddressCreateRequest } from '@/api/address'

const router = useRouter()
const userStore = useUserStore()

const addresses = ref<Address[]>([])
const showAddDialog = ref(false)
const editingAddress = ref<Address | null>(null)
const submitting = ref(false)
const addressFormRef = ref()

// 地址表单
const addressForm = reactive<AddressCreateRequest>({
  receiverName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
})

// 表单验证规则
const addressRules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  province: [
    { required: true, message: '请选择省份', trigger: 'change' }
  ],
  city: [
    { required: true, message: '请选择城市', trigger: 'change' }
  ],
  district: [
    { required: true, message: '请选择区县', trigger: 'change' }
  ],
  detail: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

// 城市和区县数据
const cities = ref<string[]>([])
const districts = ref<string[]>([])

// 城市数据映射
const cityMap: Record<string, string[]> = {
  '北京市': ['北京市'],
  '上海市': ['上海市'],
  '广东省': ['广州市', '深圳市', '东莞市', '佛山市'],
  '江苏省': ['南京市', '苏州市', '无锡市', '常州市'],
  '浙江省': ['杭州市', '宁波市', '温州市', '嘉兴市']
}

// 区县数据映射
const districtMap: Record<string, string[]> = {
  '北京市': ['朝阳区', '海淀区', '东城区', '西城区', '丰台区'],
  '上海市': ['浦东新区', '黄浦区', '徐汇区', '长宁区', '静安区'],
  '广州市': ['天河区', '越秀区', '海珠区', '荔湾区', '白云区'],
  '深圳市': ['南山区', '福田区', '罗湖区', '宝安区', '龙岗区'],
  '南京市': ['鼓楼区', '玄武区', '秦淮区', '建邺区', '栖霞区'],
  '杭州市': ['西湖区', '上城区', '下城区', '江干区', '拱墅区']
}

// 加载地址列表
const loadAddresses = async () => {
  try {
    const response = await getAddresses()
    addresses.value = response.data || []
  } catch (error) {
    console.error('加载地址失败:', error)
    ElMessage.error('加载地址失败')
  }
}

// 省份变化处理
const handleProvinceChange = () => {
  addressForm.city = ''
  addressForm.district = ''
  cities.value = cityMap[addressForm.province] || []
}

// 城市变化处理
const handleCityChange = () => {
  addressForm.district = ''
  districts.value = districtMap[addressForm.city] || []
}

// 编辑地址
const editAddress = (address: Address) => {
  editingAddress.value = address
  Object.assign(addressForm, {
    receiverName: address.receiverName,
    phone: address.phone,
    province: address.province,
    city: address.city,
    district: address.district,
    detail: address.detail,
    isDefault: address.isDefault
  })
  handleProvinceChange()
  handleCityChange()
  showAddDialog.value = true
}

// 提交地址
const submitAddress = async () => {
  try {
    await addressFormRef.value.validate()
    
    submitting.value = true
    
    if (editingAddress.value) {
      // 编辑地址
      await updateAddress({
        id: editingAddress.value.id,
        receiverName: addressForm.receiverName,
        phone: addressForm.phone,
        province: addressForm.province,
        city: addressForm.city,
        district: addressForm.district,
        detail: addressForm.detail,
        isDefault: addressForm.isDefault as boolean
      } as any)
      ElMessage.success('地址更新成功')
    } else {
      // 添加地址
      await createAddress(addressForm)
      ElMessage.success('地址添加成功')
    }
    
    showAddDialog.value = false
    resetForm()
    loadAddresses()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除地址
const deleteAddress = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteAddressApi(id)
    ElMessage.success('地址删除成功')
    loadAddresses()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 设为默认地址
const setDefaultAddress = async (id: number) => {
  try {
    await setDefaultAddressApi(id)
    ElMessage.success('设置默认地址成功')
    loadAddresses()
  } catch (error) {
    console.error('设置失败:', error)
    ElMessage.error('设置失败')
  }
}

// 重置表单
const resetForm = () => {
  editingAddress.value = null
  Object.assign(addressForm, {
    receiverName: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    isDefault: false
  })
  cities.value = []
  districts.value = []
}

// 退出登录
const handleLogout = () => {
  userStore.logout()
  ElMessage.success('退出成功')
  router.push('/login')
}

// 监听对话框关闭
const handleDialogClose = () => {
  resetForm()
}

onMounted(() => {
  loadAddresses()
})



</script>

<style scoped>
.address-page {
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

.address-container {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.address-card {
  border: 1px solid #eee;
}

.address-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.address-info {
  flex: 1;
}

.receiver-info {
  margin-bottom: 8px;
}

.receiver-info .name {
  font-weight: bold;
  font-size: 16px;
  margin-right: 15px;
}

.receiver-info .phone {
  color: #666;
  margin-right: 10px;
}

.address-detail {
  color: #666;
  line-height: 1.5;
}

.address-actions {
  display: flex;
  gap: 8px;
}
</style>
