<script setup lang="ts">
// 文件管理：上传/列表/删除
definePageMeta({ layout: 'admin' })

useHead({ title: '文件管理' })

const { get, del, upload } = useApi()

const records = ref<any[]>([])
const loading = ref(false)
const dir = ref('')

const load = async () => {
  loading.value = true
  try {
    records.value = (await get('/api/admin/files', { dir: dir.value, page: 1, size: 200 })) || []
  } catch {} finally { loading.value = false }
}
onMounted(load)

const onUpload = async (file: any) => {
  try {
    await upload('/api/admin/files/upload', file.file)
    ElMessage.success('上传成功')
    load()
  } catch (e: any) {
    ElMessage.error(e?.message || '上传失败')
  }
}

const remove = async (row: any) => {
  await ElMessageBox.confirm(`确定删除 ${row.name}？`, '删除确认', { type: 'warning' })
  try {
    await del('/api/admin/files', { path: row.url.replace('/uploads/', '') })
    ElMessage.success('已删除')
    load()
  } catch (e: any) { ElMessage.error(e?.message || '删除失败') }
}

const copyUrl = (row: any) => {
  navigator.clipboard.writeText(row.url).then(() => ElMessage.success('已复制'))
}

const fmtSize = (s: number) => {
  if (s < 1024) return s + ' B'
  if (s < 1024 * 1024) return (s / 1024).toFixed(1) + ' KB'
  return (s / 1024 / 1024).toFixed(1) + ' MB'
}
</script>

<template>
  <div>
    <div class="toolbar">
      <el-upload :show-file-list="false" :http-request="onUpload" multiple>
        <el-button type="primary">⬆ 上传文件</el-button>
      </el-upload>
      <span class="tip">支持 jpg/png/gif/webp/svg/mp4/mp3/pdf/zip/txt/md，单文件 50MB 以内</span>
    </div>

    <el-table :data="records" v-loading="loading" class="card" stripe>
      <el-table-column label="预览" width="80">
        <template #default="{ row }">
          <img v-if="/\.(jpg|jpeg|png|gif|webp|svg)$/i.test(row.name)" :src="row.url" class="thumb" loading="lazy" />
          <span v-else>📄</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="文件名" min-width="240" />
      <el-table-column label="URL" min-width="260">
        <template #default="{ row }"><code class="url-code">{{ row.url }}</code></template>
      </el-table-column>
      <el-table-column label="大小" width="100">
        <template #default="{ row }">{{ fmtSize(row.size) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="copyUrl(row)">复制</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; }
.tip { font-size: 12px; color: var(--text-secondary); }
.thumb { width: 48px; height: 48px; object-fit: cover; border-radius: 6px; }
.url-code { font-size: 12px; background: var(--bg-hover, rgba(0,0,0,0.05)); padding: 2px 6px; border-radius: 4px; }
</style>
