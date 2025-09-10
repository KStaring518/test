<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2>用户注册</h2>
          <p class="sub">创建您的零食商城账户，开启美味之旅</p>
        </div>
      </template>

      <el-alert type="info" :closable="false" class="tip-alert" show-icon>
        注册成功后可在个人中心完善头像、昵称与收货地址
      </el-alert>
      
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="80px"
        @submit.prevent="handleRegister"
        class="register-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" clearable>
            <template #prefix><el-icon class="fi"><User /></el-icon></template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password clearable>
            <template #prefix><el-icon class="fi"><Lock /></el-icon></template>
          </el-input>
          <div class="pwd-strength">
            <el-progress :percentage="passwordStrength.percent" :color="passwordStrength.color" :stroke-width="6" :show-text="false" />
            <span class="strength-text" :style="{ color: passwordStrength.color }">{{ passwordStrength.label }}</span>
          </div>
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password clearable>
            <template #prefix><el-icon class="fi"><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="registerForm.nickname" placeholder="请输入昵称" clearable>
            <template #prefix><el-icon class="fi"><User /></el-icon></template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号" clearable>
            <template #prefix><el-icon class="fi"><Iphone /></el-icon></template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱" clearable>
            <template #prefix><el-icon class="fi"><Message /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-divider class="soft-divider" />

        <el-form-item prop="agree" class="agree-item">
          <el-checkbox v-model="registerForm.agree">我已阅读并同意《用户服务协议》与《隐私政策》</el-checkbox>
        </el-form-item>
        
        <el-form-item>
          <el-button type="warning" class="submit-btn" @click="handleRegister" :loading="loading">注册</el-button>
          <el-button @click="$router.push('/login')">返回登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Lock, Message, Iphone } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const registerFormRef = ref<FormInstance>()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
  email: '',
  agree: true
})

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const validateAgree = (rule: any, value: boolean, callback: any) => {
  if (!value) callback(new Error('请先阅读并同意相关条款'))
  else callback()
}

const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度在4到20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 50, message: '昵称长度不能超过50个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  agree: [
    { required: true, validator: validateAgree, trigger: 'change' }
  ]
}

const passwordStrength = computed(() => {
  const v = registerForm.password || ''
  let score = 0
  if (v.length >= 6) score += 1
  if (/[A-Z]/.test(v)) score += 1
  if (/[a-z]/.test(v)) score += 1
  if (/\d/.test(v)) score += 1
  if (/[^A-Za-z0-9]/.test(v)) score += 1
  const map = [
    { label: '弱', color: '#f56c6c', percent: 20 },
    { label: '较弱', color: '#e6a23c', percent: 40 },
    { label: '一般', color: '#f7c948', percent: 60 },
    { label: '良好', color: '#67c23a', percent: 80 },
    { label: '强', color: '#409eff', percent: 100 },
  ]
  return map[Math.min(score - 1, 4)] || { label: '弱', color: '#f56c6c', percent: 0 }
})

const handleRegister = async () => {
  if (!registerFormRef.value) return
  try {
    await registerFormRef.value.validate()
    loading.value = true

    console.log('注册数据:', registerForm)
    console.log('注册数据JSON:', JSON.stringify(registerForm))

    const response = await userStore.register(registerForm)
    console.log('注册响应:', response)

    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error: any) {
    console.error('注册错误:', error)
    console.error('错误详情:', error.response)
    console.error('错误状态:', error.response?.status)
    console.error('错误数据:', error.response?.data)

    const errorMessage = error.response?.data?.message || error.message || '注册失败'
    ElMessage.error(errorMessage)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  --brand: #f7c948;
  --brand-light: #fdf3d7;
  --brand-weak: #fff7da;
  --text-primary: #303133;
  --text-secondary: #606266;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, var(--brand-light) 0%, var(--brand-weak) 100%);
  padding: 20px;
}

.register-card { width: 520px; border: 1px solid #fae5b2; box-shadow: 0 8px 24px rgba(0,0,0,.08); border-radius: 12px; }
.card-header h2 { margin: 0; color: var(--text-primary); }
.card-header .sub { margin: 6px 0 0; color: var(--text-secondary); font-size: 13px; }
.tip-alert { margin-bottom: 14px; background-color: var(--brand-weak); border-color: var(--brand-light); color: var(--text-secondary); }
.register-form :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 1px var(--brand) inset; }
.fi { color: #caa21b; }
.soft-divider { margin: 10px 0 2px; }
.agree-item { margin-bottom: 4px; }
.pwd-strength { display: flex; align-items: center; gap: 8px; margin-top: 6px; }
.strength-text { font-size: 12px; }
.submit-btn { min-width: 120px; }
:deep(.el-button--warning){ --el-color-warning: var(--brand); --el-color-warning-light-9: var(--brand-weak); }
</style>
