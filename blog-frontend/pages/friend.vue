<script setup lang="ts">
// 友链页
import type { Friend } from '~/types'

const { data: friends } = await useAsyncData<Friend[]>('friends', async () => {
  const { get } = useApi()
  try {
    return (await get<Friend[]>('/api/friends')) || []
  } catch {
    return []
  }
})

useHead({ title: '友情链接' })
</script>

<template>
  <div class="friend-page container">
    <div class="friend-head card">
      <h1 class="page-heading"><UiIcon name="link" :size="20" /> 友情链接</h1>
      <p>共 {{ friends?.length || 0 }} 位朋友</p>
    </div>
    <div v-if="friends?.length" class="friend-grid">
      <a
        v-for="f in friends"
        :key="f.id"
        :href="f.url"
        target="_blank"
        rel="noopener nofollow"
        class="friend-card card card-hover"
      >
        <img :src="f.avatar || '/favicon.svg'" :alt="f.name" class="friend-avatar" loading="lazy" />
        <div class="friend-info">
          <div class="friend-name">{{ f.name }}</div>
          <div class="friend-desc">{{ f.description || f.url }}</div>
        </div>
      </a>
    </div>
    <div v-else class="empty card"><p>暂无友链</p></div>
  </div>
</template>

<style scoped>
.friend-head { padding: 24px; margin-bottom: 20px; }
.friend-head h1 { margin: 0 0 8px; font-size: 22px; }
.friend-head p { margin: 0; color: var(--text-secondary); }
.friend-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 16px; }
.friend-card { display: flex; align-items: center; gap: 14px; padding: 16px; text-decoration: none; }
.friend-avatar { width: 48px; height: 48px; border-radius: 50%; object-fit: cover; }
.friend-name { font-weight: 600; color: var(--text-primary); }
.friend-desc { font-size: 13px; color: var(--text-secondary); margin-top: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.empty { text-align: center; padding: 50px 0; color: var(--text-secondary); }
</style>
