<script setup lang="ts">
// 首页：Banner + 文章列表 + 分页
import type { Post, PageResult } from '~/types'

const route = useRoute()
const page = computed(() => Number(route.query.page) || 1)

const { data, error } = await useAsyncData<PageResult<Post>>(
  'home-posts',
  async () => {
    const { get } = useApi()
    try {
      return await get<PageResult<Post>>('/api/posts', { page: page.value, size: 10 })
    } catch {
      return { records: [], total: 0, page: 1, size: 10 }
    }
  },
  { watch: [page] }
)

const posts = computed(() => data.value?.records || [])
const total = computed(() => data.value?.total || 0)

// 无搜索结果时提供seo
useHead({ title: () => page.value > 1 ? `第 ${page.value} 页` : '' })

const onPageChange = (p: number) => {
  navigateTo({ path: '/', query: p > 1 ? { page: String(p) } : {} })
}
</script>

<template>
  <div class="home-page container">
    <CommonBanner />
    <div v-if="posts.length" class="post-list">
      <PostCard v-for="p in posts" :key="p.id" :post="p" />
    </div>
    <div v-else class="empty card">
      <p>还没有文章，去后台写第一篇吧</p>
    </div>
    <CommonPagination
      v-if="total > 10"
      :page="page"
      :total="total"
      :size="10"
      @change="onPageChange"
    />
  </div>
</template>

<style scoped>
.post-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.empty {
  text-align: center;
  padding: 60px 0;
  color: var(--text-secondary);
}
</style>
