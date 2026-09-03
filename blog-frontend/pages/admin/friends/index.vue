<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
// 友链管理
definePageMeta({ layout: 'admin' })

useHead({ title: '友链管理' })

const { get, post, put, del } = useApi()

const records = ref<any[]>([])
const loading = ref(false)
const dialog = ref(false)
const editing = ref<any>(null)
const form = reactive({ name: '', url: '', avatar: '', description: '', sortOrder: 0, status: 1 })

const load = async () => {
  loading.value = true
  try {
    records.value = (await get('/api/admin/friends')) || []
  } catch {} finally { loading.value = false }
}
onMounted(load)

const openNew = () => {
  editing.value = null
  Object.assign(form, { name: '', url: '', avatar: '', description: '', sortOrder: 0, status: 1 })
  dialog.value = true
}
const openEdit = (row: any) => {
  editing.value = row
  Object.assign(form, {
    name: row.name, url: row.url, avatar: row.avatar || '',
    description: row.description || '', sortOrder: row.sortOrder || 0, status: row.status ?? 1
  })
  dialog.value = true
}
const save = async () => {
  if (!form.name.trim() || !form.url.trim()) return ElMessage.warning('名称和 URL 必填')
  try {
    if (editing.value) {
      await put(`/api/admin/friends/${editing.value.id}`, form)
    } else {
      await post('/api/admin/friends', form)
    }
    ElMessage.success('已保存')
    dialog.value = false
    load()
  } catch (e: any) { ElMessage.error(e?.message || '保存失败') }
}
const remove = async (row: any) => {
  await ElMessageBox.confirm(`确定删除友链「${row.name}」？`, '删除确认', { type: 'warning' })
  try {
    await del(`/api/admin/friends/${row.id}`)
    ElMessage.success('已删除')
    load()
  } catch (e: any) { ElMessage.error(e?.message || '删除失败') }
}
</script>

<template>
  <div>
    <div style="margin-bottom: 14px">
      <el-button type="primary" @click="openNew">＋ 新建友链</el-button>
    </div>
    <el-table :data="records" v-loading="loading" class="card" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="url" label="URL" min-width="220" />
      <el-table-column prop="description" label="描述" min-width="180" />
      <el-table-column prop="sortOrder" label="排序" width="70" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '显示' : '隐藏' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="editing ? '编辑友链' : '新建友链'" width="480">
      <el-form label-width="70px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="URL"><el-input v-model="form.url" placeholder="https://" /></el-form-item>
        <el-form-item label="头像"><el-input v-model="form.avatar" placeholder="头像图 URL" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="显示">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
