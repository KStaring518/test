<template>
  <div class="user-center">
    <div class="uc-header">
      <h2>个人中心</h2>
      <div class="uc-sub">完善资料与地址，享受更顺畅的下单体验</div>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :md="18">
        <el-card class="uc-section">
          <el-tabs v-model="activeTab">
            <el-tab-pane name="profile">
              <template #label>
                <el-icon class="tab-ico"><User /></el-icon>
                <span>基本资料</span>
              </template>
              <el-alert type="warning" :closable="false" show-icon description="头像、昵称、联系电话用于订单沟通与售后服务" style="margin-bottom:12px" />
              <el-form :model="form" label-width="90px" class="form-narrow">
                <el-form-item label="头像">
                  <div style="display:flex;align-items:center;gap:12px">
                    <el-avatar :size="64" :src="form.avatarUrl" />
                    <el-upload :show-file-list="false" :http-request="onUploadAvatar" accept="image/*">
                      <el-button type="warning" :icon="UploadFilled">上传头像</el-button>
                    </el-upload>
                  </div>
                </el-form-item>
                <el-form-item label="昵称"><el-input v-model="form.nickname" prefix-icon="User" /></el-form-item>
                <el-form-item label="手机"><el-input v-model="form.phone" :prefix-icon="Phone" /></el-form-item>
                <el-form-item label="邮箱"><el-input v-model="form.email" :prefix-icon="Message" /></el-form-item>
                <el-form-item>
                  <el-space>
                    <el-button type="warning" @click="onSave" :loading="saving" :icon="Star">保存</el-button>
                    <el-button @click="load" :icon="Refresh">重置</el-button>
                  </el-space>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane name="address">
              <template #label>
                <el-icon class="tab-ico"><Location /></el-icon>
                <span>地址管理</span>
              </template>
              <div class="address-toolbar">
                <el-button type="warning" @click="openAdd" :icon="Plus">添加地址</el-button>
              </div>
              <el-card v-for="addr in addresses" :key="addr.id" class="addr-card" shadow="hover">
                <div class="addr-row">
                  <div class="addr-main">
                    <div class="addr-line">
                      <el-icon class="mini-ico"><User /></el-icon>
                      <span class="name">{{ addr.receiverName }}</span>
                      <span class="phone">{{ addr.phone }}</span>
                      <el-tag v-if="addr.isDefault" type="warning" size="small">默认</el-tag>
                    </div>
                    <div class="addr-detail">
                      <el-icon class="mini-ico"><Location /></el-icon>
                      {{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}
                    </div>
                  </div>
                  <div class="addr-actions">
                    <el-button v-if="!addr.isDefault" size="small" @click="onSetDefault(addr.id)" :icon="Check">设为默认</el-button>
                    <el-button size="small" type="warning" plain @click="onEdit(addr)" :icon="EditPen">编辑</el-button>
                    <el-button size="small" type="danger" @click="onDelete(addr.id)" :icon="Delete">删除</el-button>
                  </div>
                </div>
              </el-card>
              <el-empty v-if="addresses.length===0" description="暂无地址" />
            </el-tab-pane>

            <el-tab-pane name="security">
              <template #label>
                <el-icon class="tab-ico"><Lock /></el-icon>
                <span>安全设置</span>
              </template>
              <el-card class="form-card">
                <el-form :model="pwd" label-width="90px">
                  <el-form-item label="旧密码"><el-input v-model="pwd.oldPassword" type="password" show-password :prefix-icon="Lock" /></el-form-item>
                  <el-form-item label="新密码"><el-input v-model="pwd.newPassword" type="password" show-password :prefix-icon="Lock" /></el-form-item>
                  <el-form-item>
                    <el-button type="warning" @click="onChangePwd" :loading="pwdSaving" :icon="Unlock">保存新密码</el-button>
                  </el-form-item>
                </el-form>
              </el-card>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="6">
        <div class="sticky">
          <el-card shadow="never" class="side-card">
            <div class="side-user">
              <el-avatar :size="72" :src="form.avatarUrl" />
              <div class="side-name">{{ form.nickname || '未设置昵称' }}</div>
              <div class="side-sub"><el-icon class="mini-ico"><Phone /></el-icon>{{ form.phone || '未绑定手机' }}</div>
              <div class="side-sub"><el-icon class="mini-ico"><Message /></el-icon>{{ form.email || '未绑定邮箱' }}</div>
            </div>
            <el-divider></el-divider>
            <el-descriptions title="默认地址" :column="1" size="small" v-if="defaultAddress">
              <el-descriptions-item label="收货人">{{ defaultAddress.receiverName }}</el-descriptions-item>
              <el-descriptions-item label="电话">{{ defaultAddress.phone }}</el-descriptions-item>
              <el-descriptions-item label="地址">{{ defaultAddress.province }} {{ defaultAddress.city }} {{ defaultAddress.district }} {{ defaultAddress.detail }}</el-descriptions-item>
            </el-descriptions>
            <el-empty v-else description="暂无默认地址"></el-empty>
            <el-divider></el-divider>
            <el-space wrap>
              <el-button type="warning" plain @click="activeTab='address'" :icon="Location">管理地址</el-button>
              <el-button @click="activeTab='security'" :icon="Lock">修改密码</el-button>
            </el-space>
          </el-card>
        </div>
      </el-col>
    </el-row>

    <!-- 地址对话框 -->
    <el-dialog v-model="addrVisible" :title="editing ? '编辑地址' : '新增地址'" width="520px">
      <el-form ref="addrFormRef" :model="addrForm" :rules="addrRules" label-width="90px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addrForm.receiverName" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="手机" prop="phone">
          <el-input v-model="addrForm.phone" :prefix-icon="Phone" />
        </el-form-item>
        <el-form-item label="省市区" required>
          <el-row :gutter="8">
            <el-col :span="8">
              <el-input v-model="addrForm.province" placeholder="省" />
            </el-col>
            <el-col :span="8">
              <el-input v-model="addrForm.city" placeholder="市" />
            </el-col>
            <el-col :span="8">
              <el-input v-model="addrForm.district" placeholder="区/县" />
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="addrForm.detail" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="addrForm.isDefault">设为默认</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addrVisible=false">取消</el-button>
        <el-button type="primary" :loading="addrSaving" @click="onSubmitAddr" :icon="Check">保存</el-button>
      </template>
    </el-dialog>
  </div>
 </template>
 
 <script setup lang="ts">
 import { onMounted, reactive, ref, computed } from 'vue'
 import { useUserStore } from '@/stores/user'
 import { ElMessage, ElMessageBox } from 'element-plus'
 import { getProfile, updateProfile, uploadAvatar } from '@/api/user'
 import { changePassword } from '@/api/user'
 import { getAddresses, createAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/address'
 import { normalizeImageUrl } from '@/api/upload'
 /* 图标 */
 import { User, Location, Lock, EditPen, Plus, UploadFilled, Phone, Message, Check, Delete, Star, Refresh, Unlock } from '@element-plus/icons-vue'
 
 const userStore = useUserStore()
 const form = reactive({
   avatarUrl: '',
   nickname: '',
   phone: '',
   email: ''
 })
 const saving = ref(false)
 const pwd = reactive({ oldPassword: '', newPassword: '' })
 const pwdSaving = ref(false)

 // 地址管理
 const addresses = ref<any[]>([])
 const addrVisible = ref(false)
 const editing = ref<any | null>(null)
 const addrSaving = ref(false)
 const addrFormRef = ref()
 const addrForm = reactive({
   receiverName: '',
   phone: '',
   province: '',
   city: '',
   district: '',
   detail: '',
   isDefault: false as boolean | undefined
 })
 const addrRules = {
   receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
   phone: [{ required: true, message: '请输入手机', trigger: 'blur' }],
   detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
 }
 
 const activeTab = ref('profile')
 const defaultAddress = computed(() => addresses.value.find(a => a.isDefault) || null)
 
 const load = async () => {
   const { data } = await getProfile()
   form.avatarUrl = normalizeImageUrl(data.avatarUrl) || ''
   form.nickname = data.nickname || ''
   form.phone = data.phone || ''
   form.email = data.email || ''
   await loadAddresses()
 }
 
 const onSave = async () => {
   saving.value = true
   try {
     await updateProfile(form)
     ElMessage.success('保存成功')
   } finally {
     saving.value = false
   }
 }
 // 上传头像
 const onUploadAvatar = async (options: any) => {
   try {
     const file: File = options.file
     const { data } = await uploadAvatar(file)
     form.avatarUrl = normalizeImageUrl(data)
     ElMessage.success('头像上传成功，记得点保存')
     options.onSuccess && options.onSuccess(data)
   } catch (e) {
     ElMessage.error('头像上传失败')
     options.onError && options.onError(e)
   }
 }
 
 const onChangePwd = async () => {
   if (!pwd.oldPassword) {
     ElMessage.warning('请输入旧密码')
     return
   }
   if (!pwd.newPassword || pwd.newPassword.length < 6) {
     ElMessage.warning('新密码至少6位')
     return
   }
   pwdSaving.value = true
   try {
     await changePassword(pwd.oldPassword, pwd.newPassword)
     ElMessage.success('密码已更新')
     pwd.oldPassword = ''
     pwd.newPassword = ''
   } finally {
     pwdSaving.value = false
   }
 }
 
 // 地址操作
 const loadAddresses = async () => {
   const { data } = await getAddresses()
   addresses.value = data || []
 }
 const resetAddrForm = () => {
   editing.value = null
   addrForm.receiverName = ''
   addrForm.phone = ''
   addrForm.province = ''
   addrForm.city = ''
   addrForm.district = ''
   addrForm.detail = ''
   addrForm.isDefault = false
 }
 const openAdd = () => { resetAddrForm(); addrVisible.value = true }
 const onEdit = (addr: any) => {
   editing.value = addr
   addrForm.receiverName = addr.receiverName
   addrForm.phone = addr.phone
   addrForm.province = addr.province
   addrForm.city = addr.city
   addrForm.district = addr.district
   addrForm.detail = addr.detail
   addrForm.isDefault = addr.isDefault
   addrVisible.value = true
 }
 const onSubmitAddr = async () => {
   await addrFormRef.value.validate()
   addrSaving.value = true
   try {
     if (editing.value) {
       await updateAddress({ id: editing.value.id, ...(addrForm as any) })
       ElMessage.success('已更新')
     } else {
       await createAddress(addrForm as any)
       ElMessage.success('已添加')
     }
     addrVisible.value = false
     await loadAddresses()
   } finally {
     addrSaving.value = false
   }
 }
 const onDelete = async (id: number) => {
   await ElMessageBox.confirm('确定删除该地址吗？', '提示')
   await deleteAddress(id)
   ElMessage.success('已删除')
   await loadAddresses()
 }
 const onSetDefault = async (id: number) => {
   await setDefaultAddress(id)
   ElMessage.success('已设为默认')
   await loadAddresses()
 }

 onMounted(() => {
   // 恢复登录状态，确保刷新页面后不会丢失登录状态
   userStore.initUser()
   
   load()
 })
 </script>
 
 <style scoped>
.user-center {
  --brand: #f7c948;              /* 主色：明亮黄 */
  --brand-weak: #fff7da;         /* 主色浅色 */
  --text-secondary: #606266;
  /* 新增辅色与强调色 */
  --accent-orange: #ff9f1c;      /* 活力橙 */
  --accent-amber: #ffbf69;       /* 琥珀 */
  --accent-teal: #2ec4b6;        /* 蓝绿 */
  --accent-mint: #8bd3dd;        /* 薄荷蓝 */
  --accent-green: #5cc689;       /* 清新绿 */
  --accent-brown: #8d6e63;       /* 木色 */
  padding: 16px;
  /* 减少纯白：整体使用更明显的暖黄浅底 */
  background: linear-gradient(180deg, #fff9e6 0%, #fff7da 40%, #fffcef 100%);
  border-radius: 8px;
}

/* 顶部条与副标题更有层次 */
.uc-header { margin-bottom: 16px; padding: 12px 14px; border-radius: 10px; background: linear-gradient(90deg, #fff4cc, #fffaf0); border: 1px solid #fae5b2; }
.uc-header h2 { margin: 0; color: #303133; }
.uc-sub { color: var(--text-secondary); margin-top: 4px; }

.uc-section { margin-top: 16px; }
.section-title { display: flex; align-items: center; gap: 8px; font-weight: 600; }
.section-title .bar { width: 6px; height: 16px; background: var(--brand); border-radius: 3px; display: inline-block; box-shadow: 0 0 0 3px var(--brand-weak); }

.form-narrow { max-width: 520px; }
.form-card { max-width: 520px; }

/* Tabs 使用品牌色 */
:deep(.el-tabs__active-bar) { background-color: var(--brand); }
:deep(.el-tabs__item.is-active) { color: #9a6b00; font-weight: 600; }
:deep(.el-tabs__item:hover) { color: #b27a00; }
.tab-ico { margin-right: 6px; vertical-align: -2px; }
.mini-ico { margin-right: 4px; color: #b38b00; }

/* 输入聚焦时的高亮边 */
:deep(.el-input__wrapper) { background: #fffef9; }
:deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 1px var(--accent-teal) inset, 0 0 0 2px rgba(46,196,182,.12); }

/* 警告 Alert 的柔和底色 */
:deep(.el-alert--warning) { background-color: #fff3e0; border-color: var(--accent-amber); }

/* 卡片统一使用浅黄底，减少纯白 */
:deep(.el-card.uc-section) { background: #fffdf2; border-color: #fae5b2; }
.addr-card { margin-top: 12px; border-color: #f2e6bf; background: #fffef6; transition: box-shadow .2s ease, border-color .2s ease; }
.addr-card:hover { box-shadow: 0 4px 18px rgba(0,0,0,.06); border-color: var(--accent-mint); background: #fffdf0; }
.addr-row { display: flex; align-items: center; justify-content: space-between; }
.addr-line { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.name { font-weight: 600; }
.phone { color: var(--text-secondary); margin-left: 8px; }
.addr-detail { color: var(--text-secondary); }
.addr-actions { display: flex; gap: 8px; }
.address-toolbar { margin-bottom: 8px; }

/* 侧栏与粘性布局 */
.sticky { position: sticky; top: 16px; }
.side-card { border-color: #f2e6bf; background: linear-gradient(180deg, #fffdf5, #fff9e6); }
.side-user { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.side-name { font-weight: 600; }
.side-sub { color: var(--text-secondary); font-size: 12px; }

/* 按钮：warning 通道统一为品牌黄，plain 走辅色边框 */
:deep(.el-button--warning) {
  --el-color-warning: var(--brand);
  --el-color-warning-light-9: var(--brand-weak);
}
:deep(.el-button.is-plain.el-button--warning) {
  border-color: var(--accent-orange);
  color: var(--accent-orange);
  background: #fffaf2;
}

/* Tag 与 Divider 彩色化 */
:deep(.el-tag--warning) { --el-color-warning: var(--brand); }
:deep(.el-divider) { border-top-color: rgba(0,0,0,.06); }

/* 小的彩带点缀（标题条阴影、按钮 hover） */
:deep(.el-button--warning:hover) { filter: saturate(1.05) brightness(1.02); }
</style>
 
 
 

