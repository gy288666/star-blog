<script setup lang="ts">
// 评论管理：筛选/审核/回复/删除
definePageMeta({ layout: 'admin' })

useHead({ title: '评论管理' })

const { get, put, post, del } = useApi()

const records = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const status = ref<number | undefined>(undefined)
const loading = ref(false)

const statusMap: Record<number, { label: string; type: string }> = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '垃圾', type: 'danger' }
}

const load = async () => {
  loading.value = true
  try {
    const r = await get<any>('/api/admin/comments', { page: page.value, size: 15, status: status.value })
    records.value = r?.records || []
    total.value = r?.total || 0
  } catch {} finally { loading.value = false }
}
onMounted(load)
watch([page, status], load)

const setStatus = async (row: any, s: number) => {
  try {
    await put(`/api/admin/comments/${row.id}/status`, { status: s })
    ElMessage.success('已更新')
    row.status = s
  } catch (e: any) { ElMessage.error(e?.message || '操作失败') }
}

const remove = async (row: any) => {
  await ElMessageBox.confirm('确定删除该评论及其子评论？', '删除确认', { type: 'warning' })
  try {
    await del(`/api/admin/comments/${row.id}`)
    ElMessage.success('已删除')
    load()
  } catch (e: any) { ElMessage.error(e?.message || '删除失败') }
}

// 回复
const replyTo = ref<any>(null)
const replyContent = ref('')
const sendReply = async () => {
  if (!replyContent.value.trim()) return
  try {
    await post('/api/admin/comments', {
      postId: replyTo.value.postId,
      parentId: replyTo.value.id,
      rootId: replyTo.value.rootId || replyTo.value.id,
      author: '站长',
      content: replyContent.value
    })
    ElMessage.success('已回复')
    replyTo.value = null
    replyContent.value = ''
    load()
  } catch (e: any) { ElMessage.error(e?.message || '回复失败') }
}
</script>

<template>
  <div>
    <div class="toolbar">
      <el-radio-group v-model="status" @change="() => { page = 1; load() }">
        <el-radio-button :value="undefined">全部</el-radio-button>
        <el-radio-button :value="0">待审核</el-radio-button>
        <el-radio-button :value="1">已通过</el-radio-button>
        <el-radio-button :value="2">垃圾</el-radio-button>
      </el-radio-group>
    </div>

    <el-table :data="records" v-loading="loading" class="card" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="作者" width="140">
        <template #default="{ row }">
          {{ row.author }}
          <el-tag v-if="row.isAdmin" size="small" type="primary" style="margin-left: 4px">管理员</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="内容" min-width="240">
        <template #default="{ row }">
          <div class="c-content">{{ row.contentMd }}</div>
          <div class="c-reply">回复 @{{ row.postTitle || '未知文章' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="(statusMap[row.status]?.type as any)">{{ statusMap[row.status]?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="170" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status !== 1" size="small" type="success" @click="setStatus(row, 1)">通过</el-button>
          <el-button v-if="row.status !== 2" size="small" type="warning" @click="setStatus(row, 2)">垃圾</el-button>
          <el-button size="small" @click="replyTo = row">回复</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top: 16px; display: flex; justify-content: flex-end">
      <el-pagination v-model:current-page="page" :page-size="15" :total="total" layout="total, prev, pager, next" />
    </div>

    <el-dialog v-model="replyTo" title="回复评论" width="500" @closed="replyContent = ''">
      <div v-if="replyTo" class="reply-quote">
        <b>{{ replyTo.author }}</b>：{{ replyTo.contentMd }}
      </div>
      <el-input v-model="replyContent" type="textarea" :rows="4" placeholder="输入回复内容（Markdown）" />
      <template #footer>
        <el-button @click="replyTo = null">取消</el-button>
        <el-button type="primary" @click="sendReply">回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar { margin-bottom: 14px; }
.c-content { max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.c-reply { font-size: 12px; color: var(--text-secondary); margin-top: 2px; }
.reply-quote { background: var(--bg-hover, rgba(0,0,0,0.04)); padding: 10px; border-radius: 6px; margin-bottom: 10px; font-size: 13px; }
</style>
