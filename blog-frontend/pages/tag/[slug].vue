<script setup lang="ts">
// 标签归档
import type { Post, PageResult } from '~/types'

const route = useRoute()
const slug = route.params.slug as string
const page = computed(() => Number(route.query.page) || 1)

const { data: tag } = await useAsyncData<any>(`tag-${slug}`, async () => {
  const { get } = useApi()
  try {
    const list = await get<any[]>('/api/tags')
    return (list || []).find((t) => t.slug === slug) || null
  } catch {
    return null
  }
})

const { data } = await useAsyncData<PageResult<Post>>(
  `tag-posts-${slug}-${page.value}`,
  async () => {
    const { get } = useApi()
    const t = tag.value
    if (!t) return { records: [], total: 0, page: 1, size: 10 }
    try {
      return await get<PageResult<Post>>('/api/posts', { page: page.value, size: 10, tagId: t.id })
    } catch {
      return { records: [], total: 0, page: 1, size: 10 }
    }
  },
  { watch: [page, tag] }
)

useHead(() => ({ title: `标签：${tag.value?.name || slug}` }))
</script>

<template>
  <div class="archive-page container">
    <div class="archive-head card">
      <h1>🏷️ {{ tag?.name || slug }}</h1>
      <p class="count">共 {{ data?.total || 0 }} 篇文章</p>
    </div>
    <div v-if="data?.records?.length" class="post-list">
      <PostCard v-for="p in data.records" :key="p.id" :post="p" />
    </div>
    <div v-else class="empty card"><p>该标签下暂无文章</p></div>
    <CommonPagination
      v-if="(data?.total || 0) > 10"
      :page="page"
      :total="data?.total || 0"
      :size="10"
      @change="(p) => navigateTo({ path: `/tag/${slug}`, query: { page: String(p) } })"
    />
  </div>
</template>

<style scoped>
.archive-head { padding: 24px; margin-bottom: 20px; }
.archive-head h1 { margin: 0 0 8px; font-size: 22px; }
.archive-head p { margin: 4px 0; color: var(--text-secondary); }
.post-list { display: flex; flex-direction: column; gap: 20px; }
.empty { text-align: center; padding: 50px 0; color: var(--text-secondary); }
</style>
