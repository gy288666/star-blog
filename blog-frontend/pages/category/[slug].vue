<script setup lang="ts">
// 分类归档
import type { Post, PageResult } from '~/types'

const route = useRoute()
const slug = route.params.slug as string
const page = computed(() => Number(route.query.page) || 1)

const { data: category } = await useAsyncData<any>(`cat-${slug}`, async () => {
  const { get } = useApi()
  try {
    const list = await get<any[]>('/api/categories')
    return (list || []).find((c) => c.slug === slug) || null
  } catch {
    return null
  }
})

const { data, error } = await useAsyncData<PageResult<Post>>(
  `cat-posts-${slug}-${page.value}`,
  async () => {
    const { get } = useApi()
    const cat = category.value
    if (!cat) return { records: [], total: 0, page: 1, size: 10 }
    try {
      return await get<PageResult<Post>>('/api/posts', { page: page.value, size: 10, categoryId: cat.id })
    } catch {
      return { records: [], total: 0, page: 1, size: 10 }
    }
  },
  { watch: [page, category] }
)

useHead(() => ({ title: `分类：${category.value?.name || slug}` }))
</script>

<template>
  <div class="archive-page container">
    <div class="archive-head card">
      <h1 class="page-heading"><UiIcon name="list" :size="20" /> {{ category?.name || slug }}</h1>
      <p v-if="category?.description">{{ category.description }}</p>
      <p class="count">共 {{ data?.total || 0 }} 篇文章</p>
    </div>
    <div v-if="data?.records?.length" class="post-list">
      <PostCard v-for="p in data.records" :key="p.id" :post="p" />
    </div>
    <div v-else class="empty card"><p>该分类下暂无文章</p></div>
    <CommonPagination
      v-if="(data?.total || 0) > 10"
      :page="page"
      :total="data?.total || 0"
      :size="10"
      @change="(p) => navigateTo({ path: `/category/${slug}`, query: { page: String(p) } })"
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
