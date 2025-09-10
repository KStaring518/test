<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>用户登录</h2>
          <p class="sub">欢迎回到零食商城</p>
        </div>
      </template>
      
      <el-alert type="info" :closable="false" class="tip-alert" show-icon>
        支持自动识别角色进入，或手动选择用户端/商家端/管理端
      </el-alert>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="80px"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" clearable>
            <template #prefix><el-icon class="fi"><User /></el-icon></template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password clearable>
            <template #prefix><el-icon class="fi"><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="进入方式">
          <el-radio-group v-model="entryMode">
            <el-radio label="AUTO">自动识别</el-radio>
            <el-radio label="USER">用户端</el-radio>
            <el-radio label="MERCHANT">商家端</el-radio>
            <el-radio label="ADMIN">管理端</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-divider class="soft-divider" />
        
        <el-form-item>
          <el-button type="warning" class="login-btn" @click="handleLogin" :loading="loading">登录</el-button>
          <el-button @click="$router.push('/register')">注册账号</el-button>
        </el-form-item>
      </el-form>
      
      <div class="demo-accounts">
        <h4>测试账号：</h4>
        <p>管理员：admin / 123456</p>
        <p>商家：merchant / 123456</p>
        <p>用户：testuser / 123456</p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const entryMode = ref<'AUTO' | 'USER' | 'MERCHANT' | 'ADMIN'>('AUTO')

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    await userStore.login(loginForm)
    ElMessage.success('登录成功')
    // 实际角色
    const role = userStore.getUserRole()
    // 依据选择的进入方式决定落地
    const mode = entryMode.value

    const go = (path: string) => { router.push(path) }

    const isAllowed = (r: string | null, m: string) => {
      if (!r) return false
      if (m === 'AUTO') return true
      if (m === 'USER') return r === 'USER' || r === 'MERCHANT'
      if (m === 'MERCHANT') return r === 'MERCHANT'
      if (m === 'ADMIN') return r === 'ADMIN'
      return false
    }

    if (!isAllowed(role, mode)) {
      ElMessage.warning('当前账号无权进入所选端，将为您跳转到可访问的页面')
    }

    // 跳转规则（含兜底）
    if (mode === 'ADMIN' && role === 'ADMIN') return go('/admin')
    if (mode === 'MERCHANT' && role === 'MERCHANT') return go('/merchant')
    if (mode === 'USER' && (role === 'USER' || role === 'MERCHANT')) return go('/')

    // AUTO 或兜底：按真实角色落地
    if (role === 'ADMIN') return go('/admin')
    if (role === 'MERCHANT') return go('/merchant')
    return go('/')
  } catch (error: any) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
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
}

.login-card { width: 460px; border: 1px solid #fae5b2; box-shadow: 0 8px 24px rgba(0,0,0,.08); border-radius: 12px; }
.card-header h2 { margin: 0; color: var(--text-primary); }
.card-header .sub { margin: 6px 0 0; color: var(--text-secondary); font-size: 13px; }
.tip-alert { margin-bottom: 14px; background-color: var(--brand-weak); border-color: var(--brand-light); color: var(--text-secondary); }
:deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 1px var(--brand) inset; }
.fi { color: #caa21b; }
.soft-divider { margin: 8px 0 2px; }
.login-btn { min-width: 110px; }
.demo-accounts { margin-top: 16px; padding: 14px; background-color: #fffdf2; border: 1px dashed #fae5b2; border-radius: 8px; }
.demo-accounts h4 { margin: 0 0 8px 0; color: var(--text-secondary); }
.demo-accounts p { margin: 4px 0; color: #909399; font-size: 13px; }
:deep(.el-button--warning){ --el-color-warning: var(--brand); --el-color-warning-light-9: var(--brand-weak); }
</style>
