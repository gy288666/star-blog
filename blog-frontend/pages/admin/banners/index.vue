<script setup lang="ts">
// 横幅管理
definePageMeta({ layout: 'admin' })

useHead({ title: '横幅管理' })

const { get, post, put, del } = useApi()

const records = ref<any[]>([])
const loading = ref(false)
const dialog = ref(false)
const editing = ref<any>(null)
const form = reactive({ title: '', subtitle: '', imageUrl: '', bgColor: '#5e72e4', typingEffect: 0, isActive: 1 })

const load = async () => {
  loading.value = true
  try {
    records.value = (await get('/api/admin/banners')) || []
  } catch {} finally { loading.value = false }
}
onMounted(load)

const openNew = () => {
  editing.value = null
  Object.assign(form, { title: '', subtitle: '', imageUrl: '', bgColor: '#5e72e4', typingEffect: 0, isActive: 1 })
  dialog.value = true
}
const openEdit = (row: any) => {
  editing.value = row
  Object.assign(form, {
    title: row.title || '', subtitle: row.subtitle || '', imageUrl: row.imageUrl || '',
    bgColor: row.bgColor || '#5e72e4', typingEffect: row.typingEffect || 0, isActive: row.isActive ?? 1
  })
  dialog.value = true
}
const save = async () => {
  try {
    if (editing.value) {
      await put(`/api/admin/banners/${editing.value.id}`, form)
    } else {
      await post('/api/admin/banners', form)
    }
    ElMessage.success('已保存')
    dialog.value = false
    load()
  } catch (e: any) { ElMessage.error(e?.message || '保存失败') }
}
const remove = async (row: any) => {
  await ElMessageBox.confirm('确定删除该横幅？', '删除确认', { type: 'warning' })
  try {
    await del(`/api/admin/banners/${row.id}`)
    ElMessage.success('已删除')
    load()
  } catch (e: any) { ElMessage.error(e?.message || '删除失败') }
}
</script>

<template>
  <div>
    <div style="margin-bottom: 14px">
      <el-button type="primary" @click="openNew">＋ 新建横幅</el-button>
    </div>
    <el-table :data="records" v-loading="loading" class="card" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" min-width="160" />
      <el-table-column prop="subtitle" label="副标题" min-width="200" />
      <el-table-column label="预览" width="120">
        <template #default="{ row }">
          <div class="banner-preview" :style="{ background: row.bgColor || '#5e72e4' }"></div>
        </template>
      </el-table-column>
      <el-table-column label="打字机" width="90">
        <template #default="{ row }">{{ row.typingEffect ? '开' : '关' }}</template>
      </el-table-column>
      <el-table-column label="启用" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.isActive === 1 ? 'success' : 'info'">{{ row.isActive === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="editing ? '编辑横幅' : '新建横幅'" width="500">
      <el-form label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="副标题"><el-input v-model="form.subtitle" /></el-form-item>
        <el-form-item label="背景图"><el-input v-model="form.imageUrl" placeholder="图片 URL（可空）" /></el-form-item>
        <el-form-item label="背景色"><el-color-picker v-model="form.bgColor" /></el-form-item>
        <el-form-item label="打字机"><el-switch v-model="form.typingEffect" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.isActive" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.banner-preview { width: 80px; height: 32px; border-radius: 6px; }
</style>
