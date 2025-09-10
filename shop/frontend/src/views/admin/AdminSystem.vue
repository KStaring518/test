<template>
  <div class="admin-system">
    <el-card class="section-card">
      <template #header><div class="card-header"><span class="bar"></span>系统配置</div></template>
      <el-row :gutter="20">
        <el-col :span="14">
          <el-form :model="form" label-width="160px" class="system-form">
            <el-form-item label="站点名称"><el-input v-model="form.siteName" placeholder="请输入站点名称" /></el-form-item>
            <el-form-item label="联系电话"><el-input v-model="form.contactPhone" placeholder="请输入联系电话" /></el-form-item>
            <el-form-item label="联系邮箱"><el-input v-model="form.contactEmail" placeholder="请输入联系邮箱" /></el-form-item>
            <el-form-item label="未支付自动关闭(分钟)"><el-input v-model.number="form.orderAutoCloseMinutes" type="number" /></el-form-item>
            <el-form-item label="发货后自动确认(天)"><el-input v-model.number="form.shipmentAutoConfirmDays" type="number" /></el-form-item>
            <el-form-item>
              <el-button type="warning" @click="save">保存</el-button>
              <el-button @click="load">重置</el-button>
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="10">
          <div class="preview-card">
            <div class="section-title small"><span class="bar"></span>站点信息预览</div>
            <div class="preview-line"><span class="label">名称：</span><span class="value">{{ form.siteName || '未命名' }}</span></div>
            <div class="preview-line"><span class="label">电话：</span><span class="value">{{ form.contactPhone || '未设置' }}</span></div>
            <div class="preview-line"><span class="label">邮箱：</span><span class="value">{{ form.contactEmail || '未设置' }}</span></div>
            <div class="preview-line"><span class="label">未支付关闭：</span><span class="value">{{ form.orderAutoCloseMinutes }} 分钟</span></div>
            <div class="preview-line"><span class="label">发货后确认：</span><span class="value">{{ form.shipmentAutoConfirmDays }} 天</span></div>
            <el-alert type="info" :closable="false" class="tip" show-icon title="提示">
              以上配置影响订单流程，请谨慎调整。
            </el-alert>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getSystemConfig, saveSystemConfig } from '@/api/admin'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const form = reactive({ siteName: '', contactPhone: '', contactEmail: '', orderAutoCloseMinutes: 30, shipmentAutoConfirmDays: 7 })

const load = async () => { const { data } = await getSystemConfig(); Object.assign(form, data) }
const save = async () => { await saveSystemConfig(form as any); ElMessage.success('保存成功') }

onMounted(() => {
  userStore.initUser()
  load()
})
</script>

<style scoped>
.admin-system{ --brand:#f7c948; --brand-light:#fdf3d7; --brand-weak:#fff7da; min-height:100vh; background:linear-gradient(180deg,#fff9e6 0%,#fffef8 60%,#ffffff 100%); padding:16px }
.section-card{ border:1px solid #f0e9cc; background:#fffdf2; border-radius:12px }
.card-header{ display:flex; align-items:center; gap:8px; font-weight:600; color:#303133 }
.card-header .bar{ width:6px; height:16px; background:var(--brand); border-radius:3px; box-shadow:0 2px 6px rgba(0,0,0,.1) }
.system-form :deep(.el-input__wrapper.is-focus){ box-shadow:0 0 0 1px var(--brand) inset }
.preview-card{ background:#fff; border:1px solid #f0e9cc; border-radius:12px; padding:12px }
.section-title.small{ margin-bottom:8px }
.preview-line{ display:flex; gap:8px; margin-bottom:6px; color:#303133 }
.preview-line .label{ width:96px; color:#606266; text-align:right }
.tip{ margin-top:8px; background:var(--brand-weak); border-color:var(--brand-light) }
:deep(.el-button--warning){ --el-color-warning: var(--brand); --el-color-warning-light-9: var(--brand-weak); }
</style>


