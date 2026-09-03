<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
// 分类管理
definePageMeta({ layout: 'admin' })

useHead({ title: '分类管理' })

const { get, post, put, del } = useApi()

const records = ref<any[]>([])
const loading = ref(false)
const dialog = ref(false)
const editing = ref<any>(null)
const form = reactive({ name: '', slug: '', description: '' })

const load = async () => {
  loading.value = true
  try {
    records.value = (await get('/api/admin/categories')) || []
  } catch {} finally { loading.value = false }
}
onMounted(load)

const openNew = () => {
  editing.value = null
  Object.assign(form, { name: '', slug: '', description: '' })
  dialog.value = true
}
const openEdit = (row: any) => {
  editing.value = row
  Object.assign(form, { name: row.name, slug: row.slug || '', description: row.description || '' })
  dialog.value = true
}
const save = async () => {
  if (!form.name.trim()) return ElMessage.warning('请输入分类名')
  try {
    if (editing.value) {
      await put(`/api/admin/categories/${editing.value.id}`, form)
    } else {
      await post('/api/admin/categories', { ...form, slug: form.slug || form.name })
    }
    ElMessage.success('已保存')
    dialog.value = false
    load()
  } catch (e: any) { ElMessage.error(e?.message || '保存失败') }
}
const remove = async (row: any) => {
  await ElMessageBox.confirm(`确定删除分类「${row.name}」？`, '删除确认', { type: 'warning' })
  try {
    await del(`/api/admin/categories/${row.id}`)
    ElMessage.success('已删除')
    load()
  } catch (e: any) { ElMessage.error(e?.message || '删除失败') }
}
</script>

<template>
  <div>
    <div style="margin-bottom: 14px">
      <el-button type="primary" @click="openNew">＋ 新建分类</el-button>
    </div>
    <el-table :data="records" v-loading="loading" class="card" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column prop="slug" label="slug" min-width="140" />
      <el-table-column prop="description" label="描述" min-width="200" />
      <el-table-column prop="postCount" label="文章数" width="90" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="editing ? '编辑分类' : '新建分类'" width="440">
      <el-form label-width="70px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="slug"><el-input v-model="form.slug" placeholder="URL 标识" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
