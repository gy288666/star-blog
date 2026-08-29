<script setup lang="ts">
// 搜索结果页
import type { Post, PageResult } from '~/types'

const route = useRoute()
const q = computed(() => (route.query.q as string) || '')
const page = computed(() => Number(route.query.page) || 1)

const { data } = await useAsyncData<PageResult<Post>>(
  `search-${q.value}-${page.value}`,
  async () => {
    if (!q.value) return { records: [], total: 0, page: 1, size: 10 }
    const { get } = useApi()
    try {
      return await get<PageResult<Post>>('/api/search', { q: q.value, page: page.value, size: 10, type: 'article,page,shuoshuo' })
    } catch {
      return { records: [], total: 0, page: 1, size: 10 }
    }
  },
  { watch: [q, page] }
)

useHead(() => ({ title: `搜索：${q.value || ''}` }))
</script>

<template>
  <div class="search-page container">
    <div class="search-head card">
      <h1>🔍 搜索「{{ q }}」</h1>
      <p>找到 {{ data?.total || 0 }} 条结果</p>
    </div>
    <div v-if="data?.records?.length" class="post-list">
      <PostCard v-for="p in data.records" :key="p.id" :post="p" />
    </div>
    <div v-else class="empty card"><p>没有找到相关内容，换个关键词试试？</p></div>
    <CommonPagination
      v-if="(data?.total || 0) > 10"
      :page="page"
      :total="data?.total || 0"
      :size="10"
      @change="(p) => navigateTo({ path: '/search', query: { q, page: String(p) } })"
    />
  </div>
</template>

<style scoped>
.search-head { padding: 24px; margin-bottom: 20px; }
.search-head h1 { margin: 0 0 8px; font-size: 22px; }
.search-head p { margin: 0; color: var(--text-secondary); }
.post-list { display: flex; flex-direction: column; gap: 20px; }
.empty { text-align: center; padding: 50px 0; color: var(--text-secondary); }
</style>
