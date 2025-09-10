<template>
  <div class="merchant-settings">
    <div class="page-header"><h2>店铺设置</h2></div>
    <el-card class="settings-card">
      <div class="section-title"><span class="bar"></span>基础信息</div>
      <el-row :gutter="20">
        <el-col :span="14">
          <el-form :model="form" label-width="100px" class="settings-form">
            <el-form-item label="店铺名称"><el-input v-model="form.shopName" placeholder="请输入店铺名称" /></el-form-item>
            <el-form-item label="店铺描述"><el-input v-model="form.shopDescription" type="textarea" :rows="4" placeholder="可填写主营特色、服务亮点" /></el-form-item>
            <el-form-item label="联系电话"><el-input v-model="form.contactPhone" placeholder="请输入联系电话" /></el-form-item>
            <el-form-item label="营业执照"><el-input v-model="form.businessLicense" placeholder="营业执照URL/编号" /></el-form-item>
            <el-form-item><el-button type="warning" @click="onSave" :loading="saving">保存</el-button></el-form-item>
          </el-form>
        </el-col>
        <el-col :span="10">
          <div class="preview-card">
            <div class="section-title small"><span class="bar"></span>店铺名片预览</div>
            <div class="shop-preview">
              <div class="shop-avatar">{{ avatarText }}</div>
              <div class="shop-meta">
                <div class="name">{{ form.shopName || '未命名店铺' }}</div>
                <div class="desc">{{ form.shopDescription || '（暂无描述）' }}</div>
                <div class="tel">☎ {{ form.contactPhone || '—' }}</div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { getMerchantProfile, updateMerchantProfile } from '@/api/merchant'

const userStore = useUserStore()
const form = reactive({ shopName: '', shopDescription: '', contactPhone: '', businessLicense: '' })
const saving = ref(false)

const load = async () => {
  const { data } = await getMerchantProfile()
  form.shopName = data.shopName || ''
  form.shopDescription = data.shopDescription || ''
  form.contactPhone = data.contactPhone || ''
  form.businessLicense = data.businessLicense || ''
}

const onSave = async () => {
  saving.value = true
  try { await updateMerchantProfile(form); ElMessage.success('保存成功') } finally { saving.value = false }
}

const avatarText = computed(() => (form.shopName?.trim()?.[0] || '店').toUpperCase())

onMounted(() => {
  userStore.initUser()
  load()
})
</script>

<style scoped>
.merchant-settings{ --brand:#f7c948; --brand-light:#fdf3d7; --brand-weak:#fff7da; min-height:100vh; background:linear-gradient(180deg,#fff9e6 0%,#fffef8 60%,#ffffff 100%); padding:16px }
.page-header h2{ margin:0 0 12px 0; color:#303133 }
.settings-card{ border:1px solid #fae5b2; background:#fffdf2; border-radius:12px; box-shadow:0 2px 10px rgba(0,0,0,.04); padding-bottom:12px }
.section-title{ display:flex; align-items:center; gap:8px; font-weight:600; color:#303133; margin:12px 0 }
.section-title.small{ margin-bottom:8px }
.section-title .bar{ width:6px; height:16px; background:var(--brand); border-radius:3px; box-shadow:0 2px 6px rgba(0,0,0,.1) }
.settings-form :deep(.el-input__wrapper.is-focus){ box-shadow:0 0 0 1px var(--brand) inset }
.preview-card{ background:#fff; border:1px solid #f0e9cc; border-radius:12px; padding:12px }
.shop-preview{ display:flex; gap:12px; align-items:center }
.shop-avatar{ width:56px; height:56px; border-radius:50%; background:var(--brand); color:#4a2c00; display:flex; align-items:center; justify-content:center; font-weight:800; font-size:20px; }
.shop-meta .name{ font-weight:700; color:#303133 }
.shop-meta .desc{ color:#606266; margin:2px 0 4px 0; font-size:13px; }
.shop-meta .tel{ color:#e67e22; font-weight:600; font-size:13px }
:deep(.el-button--warning){ --el-color-warning: var(--brand); --el-color-warning-light-9: var(--brand-weak); }
</style>


