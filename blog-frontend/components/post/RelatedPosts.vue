<script setup lang="ts">
// 相关文章（同分类/标签，最多 6 篇）
import type { Post } from '~/types'

const props = defineProps<{ postId: number }>()

const { get } = useApi()
const list = ref<Post[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    list.value = (await get<Post[]>(`/api/posts/${props.postId}/related`)) || []
  } catch {
    list.value = [] // 降级空态
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section v-if="!loading && list.length" class="related-posts card">
    <h3 class="section-title">相关文章</h3>
    <ul class="related-list">
      <li v-for="item in list" :key="item.id">
        <NuxtLink :to="`/post/${item.slug || item.id}`" class="related-item">
          <img v-if="item.cover" :src="item.cover" :alt="item.title" loading="lazy" class="related-cover" />
          <span v-else class="related-cover no-img">📄</span>
          <span class="related-info">
            <span class="related-title">{{ item.title }}</span>
            <span class="related-meta">👁 {{ item.views ?? 0 }} · {{ formatDate(item.publishedAt || item.createTime, false) }}</span>
          </span>
        </NuxtLink>
      </li>
    </ul>
  </section>
</template>

<style scoped>
.related-posts { padding: 20px 22px; margin-top: 28px; }
.section-title {
  margin: 0 0 14px;
  font-size: 17px;
  color: var(--text-1);
  border-left: 4px solid var(--primary);
  padding-left: 10px;
}
.related-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}
.related-item {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 8px;
  border-radius: 10px;
  color: inherit;
  transition: background 0.2s;
}
.related-item:hover { background: var(--bg-hover); }
.related-cover {
  width: 72px;
  height: 48px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.related-cover.no-img { background: var(--bg-hover); font-size: 20px; }
.related-info { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.related-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-1);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.related-item:hover .related-title { color: var(--primary); }
.related-meta { font-size: 12px; color: var(--text-3); }
</style>
