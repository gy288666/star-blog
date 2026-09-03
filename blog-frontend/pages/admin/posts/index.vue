<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
// 文章管理列表
definePageMeta({ layout: 'admin' })

useHead({ title: '文章管理' })

const { get, del } = useApi()
const router = useRouter()

const records = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const type = ref<number | undefined>(undefined)
const status = ref<number | undefined>(undefined)
const keyword = ref('')
const loading = ref(false)

const statusMap: Record<number, { label: string; type: string }> = {
  0: { label: '草稿', type: 'info' },
  1: { label: '已发布', type: 'success' },
  2: { label: '密码保护', type: 'warning' }
}
const typeMap: Record<number, string> = { 0: '文章', 1: '页面', 2: '说说' }

const load = async () => {
  loading.value = true
  try {
    const r = await get<any>('/api/admin/posts', {
      page: page.value, size: size.value,
      type: type.value, status: status.value,
      keyword: keyword.value || undefined
    })
    records.value = r?.records || []
    total.value = r?.total || 0
  } catch {} finally { loading.value = false }
}
onMounted(load)

const remove = async (row: any) => {
  await ElMessageBox.confirm(`确定删除「${row.title}」？该操作不可恢复`, '删除确认', { type: 'warning' })
  try {
    await del(`/api/admin/posts/${row.id}`)
    ElMessage.success('已删除')
    load()
  } catch (e: any) { ElMessage.error(e?.message || '删除失败') }
}

const edit = (row: any) => router.push(`/admin/posts/edit-${row.id}`)

watch([page, type, status], load)
</script>

<template>
  <div class="post-manage">
    <div class="toolbar">
      <el-select v-model="type" placeholder="类型" clearable style="width: 110px">
        <el-option label="文章" :value="0" /><el-option label="页面" :value="1" /><el-option label="说说" :value="2" />
      </el-select>
      <el-select v-model="status" placeholder="状态" clearable style="width: 120px">
        <el-option label="草稿" :value="0" /><el-option label="已发布" :value="1" /><el-option label="密码保护" :value="2" />
      </el-select>
      <el-input v-model="keyword" placeholder="搜索标题" style="width: 200px" clearable @keyup.enter="() => { page = 1; load() }" />
      <el-button @click="() => { page = 1; load() }">搜索</el-button>
      <div style="flex: 1"></div>
      <el-button type="primary" @click="router.push('/admin/posts/new')">＋ 写文章</el-button>
    </div>

    <el-table :data="records" v-loading="loading" class="card" stripe>
      <el-table-column prop="id" label="ID" width="64" />
      <el-table-column label="标题" min-width="240">
        <template #default="{ row }">
          {{ row.title }}
          <el-tag v-if="row.isTop" size="small" type="danger" style="margin-left: 6px">置顶</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="80">
        <template #default="{ row }">{{ typeMap[row.type] }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="(statusMap[row.status]?.type as any) || 'info'">{{ statusMap[row.status]?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="views" label="阅读" width="80" />
      <el-table-column prop="commentCount" label="评论" width="80" />
      <el-table-column prop="updateTime" label="更新时间" width="170" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="edit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top: 16px; display: flex; justify-content: flex-end">
      <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next" />
    </div>
  </div>
</template>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; align-items: center; }
</style>
