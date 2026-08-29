<script setup lang="ts">
// 文章元信息条
import type { Post } from '~/types'

defineProps<{ post: Post }>()
</script>

<template>
  <div class="post-meta">
    <span class="meta-item">👤 {{ post.authorNickname || '佚名' }}</span>
    <span class="meta-item">📅 {{ formatDate(post.publishedAt || post.createTime) }}</span>
    <span class="meta-item">👁 {{ post.views ?? 0 }} 阅读</span>
    <span class="meta-item">💬 {{ post.commentCount ?? 0 }} 评论</span>
    <span v-if="post.updateTime" class="meta-item">✏️ 更新于 {{ formatDate(post.updateTime, false) }}</span>
    <div class="meta-tax">
      <NuxtLink v-for="cat in post.categories || []" :key="cat.id" :to="`/category/${cat.slug}`" class="tag-chip cat-chip">
        {{ cat.name }}
      </NuxtLink>
      <NuxtLink v-for="tag in post.tags || []" :key="tag.id" :to="`/tag/${tag.slug}`" class="tag-chip">
        # {{ tag.name }}
      </NuxtLink>
    </div>
  </div>
</template>

<style scoped>
.post-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  align-items: center;
  font-size: 13px;
  color: var(--text-3);
}
.meta-tax {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  width: 100%;
}
.cat-chip { font-weight: 600; }
</style>
