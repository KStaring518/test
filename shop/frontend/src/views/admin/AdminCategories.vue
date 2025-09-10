<template>
  <div class="admin-categories">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="tree-card">
          <template #header>
            <div class="card-header">
              <span class="bar"></span>
              <span>分类树</span>
              <el-tag type="info" size="small" class="count-tag">{{ getTotalCount(tree) }} 个分类</el-tag>
            </div>
          </template>
          <div class="tree-container">
            <el-tree 
              :data="tree" 
              node-key="id" 
              :props="{label:'name', children:'children'}" 
              highlight-current 
              @node-click="onSelect"
              class="category-tree"
              :expand-on-click-node="false"
            >
              <template #default="{ node, data }">
                <div class="tree-node">
                  <el-icon class="node-icon"><Folder /></el-icon>
                  <span class="node-label">{{ node.label }}</span>
                  <el-tag 
                    :type="data.status === 'ENABLED' ? 'success' : 'danger'" 
                    size="small" 
                    class="status-tag"
                  >
                    {{ data.status === 'ENABLED' ? '启用' : '禁用' }}
                  </el-tag>
                </div>
              </template>
            </el-tree>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span class="bar"></span>
              <span>分类编辑</span>
              <el-tag v-if="currentId" type="warning" size="small" class="edit-tag">编辑模式</el-tag>
              <el-tag v-else type="success" size="small" class="edit-tag">新建模式</el-tag>
            </div>
          </template>
          <el-form :model="form" label-width="100px" class="category-form">
            <el-form-item label="分类名称">
              <el-input 
                v-model="form.name" 
                placeholder="请输入分类名称"
                :prefix-icon="Collection"
              />
            </el-form-item>
            <el-form-item label="父级分类">
              <el-input 
                v-model.number="form.parentId" 
                type="number" 
                placeholder="留空=顶级，默认选中节点为父级"
                :prefix-icon="FolderOpened"
              />
              <div class="form-tip">留空表示顶级分类，或输入父级分类ID</div>
            </el-form-item>
            <el-form-item label="排序权重">
              <el-input-number 
                v-model="form.sortOrder" 
                :min="0" 
                :max="999"
                controls-position="right"
                class="sort-input"
              />
              <div class="form-tip">数值越小排序越靠前</div>
            </el-form-item>
            <el-form-item label="状态">
              <el-radio-group v-model="form.status" class="status-radio">
                <el-radio value="ENABLED">
                  <el-icon><Check /></el-icon>
                  启用
                </el-radio>
                <el-radio value="DISABLED">
                  <el-icon><Close /></el-icon>
                  禁用
                </el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-space>
                <el-button type="warning" @click="save" :loading="saving">
                  <el-icon><Check /></el-icon>
                  保存
                </el-button>
                <el-popconfirm title="确认删除该分类？删除后不可恢复！" @confirm="remove">
                  <template #reference>
                    <el-button type="danger" :disabled="!currentId">
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                  </template>
                </el-popconfirm>
                <el-button @click="newChild" :disabled="!currentId">
                  <el-icon><Plus /></el-icon>
                  新建子类
                </el-button>
                <el-button @click="reset">
                  <el-icon><Refresh /></el-icon>
                  重置
                </el-button>
              </el-space>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getCategoryTree, createCategory, updateCategory, deleteCategory } from '@/api/admin'
import { ElMessage } from 'element-plus'
import { Folder, Collection, FolderOpened, Check, Close, Delete, Plus, Refresh } from '@element-plus/icons-vue'

const tree = ref<any[]>([])
const currentId = ref<number | null>(null)
// 记录当前选中节点的父级ID（当编辑未显式填写父级时，默认沿用该值）
const selectedParentId = ref<number | undefined>(undefined)
const form = reactive<any>({ name: '', parentId: undefined, sortOrder: 0, status: 'ENABLED' })
const saving = ref(false)

const load = async () => { const { data } = await getCategoryTree(); tree.value = data }
// el-tree 的 node-click 事件签名为 (data, node)
const onSelect = (data:any, node:any) => { 
  console.log('选中节点:', data)
  currentId.value = data.id
  // 优先从树节点结构获取父ID（大多数树数据不包含 parentId 字段）
  selectedParentId.value = node?.parent?.data?.id
  Object.assign(form, { 
    name: data.name, 
    parentId: selectedParentId.value, 
    sortOrder: data.sortOrder, 
    status: data.status 
  })
  console.log('表单更新后:', form)
}
const reset = () => { currentId.value = null; Object.assign(form, { name: '', parentId: undefined, sortOrder: 0, status: 'ENABLED' }) }
const newChild = () => {
  // 以当前选中节点作为父级，准备创建子分类
  console.log('新建子分类 - 当前选中ID:', currentId.value)
  
  if (!currentId.value) {
    ElMessage.warning('请先选择一个分类作为父级')
    return
  }
  const pid = currentId.value
  currentId.value = null; // 切换为"创建"模式
  Object.assign(form, { name: '', parentId: pid, sortOrder: 0, status: 'ENABLED' })
  
  console.log('新建子分类 - 表单已重置:', form)
}
const save = async () => {
  if (!form.name) { ElMessage.error('请输入名称'); return }
  // 防止把自己设置为自己的父级
  if (currentId.value && form.parentId === currentId.value) {
    ElMessage.error('父级ID不能与自身相同');
    return
  }
  
  console.log('保存分类数据:', {
    currentId: currentId.value,
    form: form,
    isUpdate: !!currentId.value,
    willCallCreate: !currentId.value,
    willCallUpdate: !!currentId.value
  })
  
  try {
    saving.value = true
    // 编辑时如果未显式填写父级，则沿用原父级
    const payload:any = { ...form }
    if (currentId.value && (payload.parentId === undefined || payload.parentId === null)) {
      payload.parentId = selectedParentId.value
    }
    if (currentId.value) { 
      console.log('调用updateCategory，ID:', currentId.value)
      await updateCategory(currentId.value, payload) 
    } else { 
      console.log('调用createCategory，数据:', form)
      await createCategory(payload) 
    }
    ElMessage.success('已保存'); 
    await load()
  } catch (error: any) {
    console.error('保存分类失败:', error)
    ElMessage.error('保存失败: ' + (error.response?.data?.message || error.message))
  } finally {
    saving.value = false
  }
}

// 计算分类总数
const getTotalCount = (nodes: any[]): number => {
  let count = 0
  const countNodes = (nodeList: any[]) => {
    nodeList.forEach(node => {
      count++
      if (node.children && node.children.length > 0) {
        countNodes(node.children)
      }
    })
  }
  countNodes(nodes)
  return count
}
const remove = async () => { if (!currentId.value) return; await deleteCategory(currentId.value); ElMessage.success('已删除'); reset(); await load() }

onMounted(load)
</script>

<style scoped>
.admin-categories {
  --brand: #f7c948;
  --brand-light: #fdf3d7;
  --brand-weak: #fff7da;
  --amber: #f59e0b;
  --amber-light: #fef3c7;
  --orange: #f97316;
  --orange-light: #fed7aa;
  --teal: #14b8a6;
  --teal-light: #ccfbf1;
  --mint: #10b981;
  --mint-light: #d1fae5;
  --fresh-green: #22c55e;
  --fresh-green-light: #dcfce7;
  --wood: #a78b5b;
  --wood-light: #f3f0e8;
  
  min-height: 100vh;
  background: linear-gradient(180deg, #fff9e6 0%, #fffef8 60%, #ffffff 100%);
  padding: 16px;
}

.tree-card, .form-card {
  border: 1px solid #f0e9cc;
  background: #fffdf2;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.card-header .bar {
  width: 6px;
  height: 16px;
  background: var(--brand);
  border-radius: 3px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.count-tag, .edit-tag {
  margin-left: auto;
}

.tree-container {
  max-height: 600px;
  overflow-y: auto;
  padding: 8px;
}

.category-tree {
  background: transparent;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 4px 0;
}

.node-icon {
  color: var(--amber);
  font-size: 16px;
}

.node-label {
  flex: 1;
  font-weight: 500;
  color: #303133;
}

.status-tag {
  font-size: 10px;
  height: 18px;
  line-height: 16px;
}

.category-form {
  padding: 8px;
}

.category-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--brand) inset;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}

.sort-input {
  width: 120px;
}

.status-radio {
  display: flex;
  gap: 16px;
}

.status-radio :deep(.el-radio) {
  margin-right: 0;
}

.status-radio :deep(.el-radio__label) {
  display: flex;
  align-items: center;
  gap: 4px;
}

:deep(.el-button--warning) {
  --el-color-warning: var(--brand);
  --el-color-warning-light-9: var(--brand-weak);
}

:deep(.el-tree-node__content:hover) {
  background-color: var(--brand-weak);
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: var(--brand-light);
  color: #303133;
}

:deep(.el-tree-node__expand-icon) {
  color: var(--amber);
}

:deep(.el-tree-node__expand-icon.is-leaf) {
  color: var(--mint);
}
</style>


