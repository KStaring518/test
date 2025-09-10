<template>
  <div class="merchant-register-container">
    <el-card class="merchant-register-card">
      <template #header>
        <h2>商家注册申请</h2>
      </template>
      
      <el-form
        ref="merchantFormRef"
        :model="merchantForm"
        :rules="merchantRules"
        label-width="100px"
        @submit.prevent="handleMerchantRegister"
      >
        <el-form-item label="店铺名称" prop="shopName">
          <el-input
            v-model="merchantForm.shopName"
            placeholder="请输入店铺名称"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="店铺描述" prop="shopDescription">
          <el-input
            v-model="merchantForm.shopDescription"
            type="textarea"
            :rows="3"
            placeholder="请描述您的店铺特色和经营范围"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input
            v-model="merchantForm.contactPhone"
            placeholder="请输入联系电话"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="营业执照" prop="businessLicense">
          <el-input
            v-model="merchantForm.businessLicense"
            placeholder="请输入营业执照号（选填）"
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleMerchantRegister" :loading="loading">
            提交申请
          </el-button>
          <el-button @click="$router.push('/')">
            返回首页
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { api } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const merchantFormRef = ref<FormInstance>()
const loading = ref(false)

const merchantForm = reactive({
  shopName: '',
  shopDescription: '',
  contactPhone: '',
  businessLicense: ''
})

const merchantRules: FormRules = {
  shopName: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' },
    { max: 100, message: '店铺名称长度不能超过100个字符', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { max: 20, message: '联系电话长度不能超过20个字符', trigger: 'blur' }
  ]
}

const handleMerchantRegister = async () => {
  if (!merchantFormRef.value) return
  
  try {
    // 必须已登录
    userStore.initUser()
    if (!userStore.isLoggedIn()) {
      ElMessage.warning('请先登录后再提交商家申请')
      // 仅提示，不强制跳转，避免误判401把用户踢出
      return
    }
    await merchantFormRef.value.validate()
    loading.value = true
    
    await api.post('/auth/merchant/register', merchantForm)
    
    ElMessage.success('商家申请提交成功，请等待管理员审核')
    router.push('/')
  } catch (error: any) {
    console.error('商家注册失败:', error)
    // 401时保持在当前页，保留表单
    const errorMessage = error.response?.data?.message || error.message || '申请提交失败'
    ElMessage.error(errorMessage)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 进入页面时也做一次登录检查
  userStore.initUser()
  if (!userStore.isLoggedIn()) {
    ElMessage.info('请先登录后再申请成为商家')
    router.push('/login')
  }
})
</script>

<style scoped>
.merchant-register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.merchant-register-card {
  width: 500px;
}
</style>
