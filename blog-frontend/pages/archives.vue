<script setup lang="ts">
// 归档时间轴
import type { ArchiveYear } from '~/types'

const { data: years } = await useAsyncData<ArchiveYear[]>('archives', async () => {
  const { get } = useApi()
  try {
    return (await get<ArchiveYear[]>('/api/archives')) || []
  } catch {
    return []
  }
})

useHead({ title: '归档' })
const total = computed(() => (years.value || []).reduce((s, y) => s + y.posts.length, 0))
</script>

<template>
  <div class="archives-page container">
    <div class="archives-head card">
      <h1>📅 归档</h1>
      <p>共 {{ total }} 篇文章</p>
    </div>
    <div v-for="y in years || []" :key="y.year" class="year-group card">
      <h2 class="year-title">{{ y.year }}</h2>
      <ul class="timeline">
        <li v-for="p in y.posts" :key="p.id">
          <span class="dot"></span>
          <NuxtLink :to="`/post/${p.slug || p.id}`" class="tl-title">{{ p.title }}</NuxtLink>
          <span class="tl-date">{{ p.createTime?.slice(5, 10) }}</span>
        </li>
      </ul>
    </div>
    <div v-if="!years?.length" class="empty card"><p>暂无文章</p></div>
  </div>
</template>

<style scoped>
.archives-head { padding: 24px; margin-bottom: 20px; }
.archives-head h1 { margin: 0 0 8px; font-size: 22px; }
.archives-head p { color: var(--text-secondary); margin: 0; }
.year-group { padding: 20px 24px; margin-bottom: 20px; }
.year-title { margin: 0 0 14px; font-size: 18px; color: var(--primary); }
.timeline { list-style: none; margin: 0; padding: 0; }
.timeline li { display: flex; align-items: baseline; gap: 10px; padding: 8px 0; position: relative; }
.dot { width: 8px; height: 8px; border-radius: 50%; background: var(--primary); flex-shrink: 0; position: relative; top: -1px; }
.tl-title { color: var(--text-primary); text-decoration: none; flex: 1; }
.tl-title:hover { color: var(--primary); }
.tl-date { color: var(--text-secondary); font-size: 13px; }
.empty { text-align: center; padding: 50px 0; color: var(--text-secondary); }
</style>
