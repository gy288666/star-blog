<script setup lang="ts">
// 文章列表卡片：封面 + 标题 + 摘要 + 元信息 + 分类标签
import type { Post } from '~/types'

const props = defineProps<{ post: Post }>()

const cover = computed(() => props.post.cover || '')
const summary = computed(() => props.post.summary || '')
</script>

<template>
  <article class="post-card card card-hover">
    <NuxtLink :to="`/post/${post.slug || post.id}`" class="card-link">
      <div class="card-cover" :class="{ 'no-cover': !cover }">
        <img v-if="cover" :src="cover" :alt="post.title" loading="lazy" />
        <span v-else class="cover-placeholder">📄</span>
        <span v-if="post.isTop" class="top-badge">置顶</span>
        <span v-if="post.status === 2" class="lock-badge">🔒 密码保护</span>
      </div>
      <div class="card-body">
        <h2 class="card-title">{{ post.title }}</h2>
        <p class="card-summary">{{ summary }}</p>
        <div class="card-meta">
          <span class="meta-item">👤 {{ post.authorNickname || '佚名' }}</span>
          <span class="meta-item">📅 {{ formatDate(post.publishedAt || post.createTime, false) }}</span>
          <span class="meta-item">👁 {{ post.views ?? 0 }}</span>
          <span class="meta-item">💬 {{ post.commentCount ?? 0 }}</span>
        </div>
      </div>
    </NuxtLink>
    <div v-if="(post.categories?.length || post.tags?.length)" class="card-tax">
      <NuxtLink
        v-for="cat in post.categories || []"
        :key="'c' + cat.id"
        :to="`/category/${cat.slug}`"
        class="tag-chip cat-chip"
      >{{ cat.name }}</NuxtLink>
      <NuxtLink
        v-for="tag in post.tags || []"
        :key="'t' + tag.id"
        :to="`/tag/${tag.slug}`"
        class="tag-chip"
      ># {{ tag.name }}</NuxtLink>
    </div>
  </article>
</template>

<style scoped>
.post-card { overflow: hidden; }
.card-link { display: block; color: inherit; }
.card-cover {
  position: relative;
  height: 200px;
  background: var(--bg-hover);
  overflow: hidden;
}
.card-cover::after {
  /* 封面底部轻渐隐，与玻璃卡身衔接 */
  content: '';
  position: absolute;
  left: 0; right: 0; bottom: 0;
  height: 42%;
  background: linear-gradient(180deg, transparent, rgba(20, 24, 40, 0.08));
  pointer-events: none;
}
.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}
.post-card:hover .card-cover img { transform: scale(1.06); }
.card-cover.no-cover {
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(ellipse at 30% 20%, rgba(255, 255, 255, 0.22), transparent 55%),
    linear-gradient(135deg, var(--primary), var(--primary-dark));
}
.cover-placeholder { font-size: 40px; opacity: 0.6; }
.top-badge, .lock-badge {
  position: absolute;
  top: 12px;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  color: #fff;
  backdrop-filter: blur(4px);
}
.top-badge { left: 12px; background: rgba(245, 54, 92, 0.9); }
.lock-badge { right: 12px; background: rgba(50, 50, 93, 0.7); }
.card-body { padding: 18px 20px 16px; }
.card-title {
  margin: 0 0 8px;
  font-size: 19px;
  font-weight: 600;
  line-height: 1.4;
  color: var(--text-1);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.2s;
}
.post-card:hover .card-title { color: var(--primary); }
.card-summary {
  margin: 0 0 12px;
  font-size: 14px;
  color: var(--text-2);
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12.5px;
  color: var(--text-3);
}
.card-tax {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: -6px;
  padding: 0 20px 16px;
}
.cat-chip { font-weight: 600; }
</style>
