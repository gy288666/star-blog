<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
// 说说管理
definePageMeta({ layout: 'admin' })

useHead({ title: '说说管理' })

const { get, post, del } = useApi()

const records = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const content = ref('')
const publishing = ref(false)
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const r = await get<any>('/api/admin/posts', { page: page.value, size: 10, type: 2 })
    records.value = r?.records || []
    total.value = r?.total || 0
  } catch {} finally { loading.value = false }
}
onMounted(load)

const publish = async () => {
  if (!content.value.trim()) return ElMessage.warning('说点什么吧')
  publishing.value = true
  try {
    await post('/api/admin/posts', { type: 2, title: '', contentMd: content.value, status: 1 })
    ElMessage.success('已发布')
    content.value = ''
    load()
  } catch (e: any) { ElMessage.error(e?.message || '发布失败') } finally { publishing.value = false }
}

const remove = async (row: any) => {
  await ElMessageBox.confirm('确定删除这条说说？', '删除确认', { type: 'warning' })
  try {
    await del(`/api/admin/posts/${row.id}`)
    ElMessage.success('已删除')
    load()
  } catch (e: any) { ElMessage.error(e?.message || '删除失败') }
}
</script>

<template>
  <div>
    <div class="card pub-box">
      <el-input v-model="content" type="textarea" :rows="3" placeholder="发布新说说…" />
      <div style="margin-top: 10px; text-align: right">
        <el-button type="primary" :loading="publishing" @click="publish">发布</el-button>
      </div>
    </div>

    <el-table :data="records" v-loading="loading" class="card" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="内容" min-width="300">
        <template #default="{ row }">{{ row.contentMd }}</template>
      </el-table-column>
      <el-table-column prop="upvotes" label="点赞" width="70" />
      <el-table-column prop="commentCount" label="评论" width="70" />
      <el-table-column prop="createTime" label="时间" width="170" />
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top: 16px; display: flex; justify-content: flex-end">
      <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="total, prev, pager, next" />
    </div>
  </div>
</template>

<style scoped>
.pub-box { padding: 16px; margin-bottom: 14px; }
</style>
