<script setup lang="ts">
// 上一篇 / 下一篇导航
import type { PostDetail } from '~/types'

const props = defineProps<{ post: PostDetail }>()

const prev = computed(() => props.post.prev || null)
const next = computed(() => props.post.next || null)
</script>

<template>
  <div v-if="prev || next" class="post-nav">
    <NuxtLink v-if="prev" :to="`/post/${prev.slug || prev.id}`" class="nav-card card card-hover prev">
      <span class="nav-label">« 上一篇</span>
      <span class="nav-title">{{ prev.title }}</span>
    </NuxtLink>
    <span v-else class="nav-card placeholder"></span>
    <NuxtLink v-if="next" :to="`/post/${next.slug || next.id}`" class="nav-card card card-hover next">
      <span class="nav-label">下一篇 »</span>
      <span class="nav-title">{{ next.title }}</span>
    </NuxtLink>
    <span v-else class="nav-card placeholder"></span>
  </div>
</template>

<style scoped>
.post-nav {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  margin-top: 28px;
}
.nav-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 16px 18px;
  color: var(--text-1);
}
.nav-card.next { text-align: right; }
.nav-card.placeholder { visibility: hidden; }
.nav-label { font-size: 12.5px; color: var(--text-3); }
.nav-title {
  font-size: 14.5px;
  font-weight: 600;
  color: var(--text-1);
  transition: color 0.2s;
}
.nav-card:hover .nav-title { color: var(--primary); }
@media (max-width: 560px) {
  .post-nav { grid-template-columns: 1fr; }
  .nav-card.placeholder { display: none; }
}
</style>
