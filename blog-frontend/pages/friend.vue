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

    <!-- 官方 QQ 群 -->
    <a
      href="https://qm.qq.com/q/cqBDMznVbq"
      target="_blank"
      rel="noopener nofollow"
      class="qq-group-banner card"
    >
      <span class="qq-icon"><UiIcon name="chat" :size="26" /></span>
      <span class="qq-text">
        <b>聊天互赞扩列群</b>
        <small>本站官方 QQ 群 · 点击即可申请加入，一起来聊天互赞扩列</small>
      </span>
      <span class="qq-join">加入群聊</span>
    </a>
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
.qq-group-banner {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 22px;
  margin-bottom: 20px;
  text-decoration: none;
  border-left: 4px solid var(--primary);
}
.qq-icon {
  width: 48px; height: 48px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: var(--primary);
  color: #fff;
  flex-shrink: 0;
}
.qq-text { flex: 1; display: flex; flex-direction: column; gap: 3px; color: var(--text-1); }
.qq-text b { font-size: 15px; }
.qq-text small { color: var(--text-3); font-size: 12.5px; }
.qq-join {
  padding: 7px 18px;
  border-radius: 999px;
  background: var(--primary);
  color: #fff;
  font-size: 13px;
  transition: transform 0.2s, box-shadow 0.2s;
}
.qq-group-banner:hover .qq-join { transform: scale(1.06); box-shadow: 0 4px 14px var(--primary-glow); }
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
