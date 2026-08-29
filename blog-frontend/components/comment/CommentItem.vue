<script setup lang="ts">
// 单条评论（嵌套两级：children 直接展示， grandchildren 由后端归到 children 下，前端用 @某某 标注）
import type { Comment } from '~/types'

const props = withDefaults(defineProps<{
  comment: Comment
  /** 二级子评论的回复对象标记 */
  replyLabel?: string
  depth?: number
}>(), { depth: 1 })

const emit = defineEmits<{ (e: 'reply', comment: Comment): void }>()

const avatarColor = computed(() => {
  let hash = 0
  for (const ch of props.comment.author) hash = (hash * 31 + ch.charCodeAt(0)) % 360
  return `hsl(${hash}, 55%, 60%)`
})
const initial = computed(() => props.comment.author.slice(0, 1).toUpperCase())

const renderReplyLabel = (c: Comment) =>
  (c.parentId && c.parentId !== c.rootId) ? c.parentAuthor || '' : ''
</script>

<template>
  <div class="comment-item">
    <div class="comment-main">
      <span v-if="comment.avatar" class="avatar"><img :src="comment.avatar" :alt="comment.author" loading="lazy" /></span>
      <span v-else class="avatar letter" :style="{ background: avatarColor }">{{ initial }}</span>
      <div class="comment-body">
        <div class="comment-head">
          <span class="comment-author" :class="{ admin: comment.isAdmin }">
            {{ comment.author }}<span v-if="comment.isAdmin" class="admin-badge">站长</span>
          </span>
          <span v-if="replyLabel" class="reply-tag">@ {{ replyLabel }}</span>
          <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
        </div>
        <div class="comment-content markdown-body" v-html="comment.contentHtml"></div>
        <button class="reply-btn" @click="emit('reply', comment)">回复</button>
      </div>
    </div>

    <!-- 二级嵌套 -->
    <div v-if="comment.children?.length" class="comment-children">
      <template v-for="child in comment.children" :key="child.id">
        <CommentItem
          :comment="child"
          :reply-label="child.parentId !== comment.id ? findParentName(comment.children, child.parentId) : ''"
          :depth="depth + 1"
          @reply="emit('reply', $event)"
        />
      </template>
    </div>
  </div>
</template>

<script lang="ts">
// 在子评论前缀中查找其父评论的作者名（仅限同一 root 的 children 列表内）
function findParentName(siblings: Comment[], parentId: number): string {
  return siblings.find((c) => c.id === parentId)?.author || ''
}
</script>

<style scoped>
.comment-item { padding: 14px 0; }
.comment-item + .comment-item { border-top: 1px solid var(--divider); }
.comment-main { display: flex; gap: 12px; }
.avatar {
  width: 40px; height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
  overflow: hidden;
}
.avatar img { width: 100%; height: 100%; object-fit: cover; }
.avatar.letter {
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-weight: 700; font-size: 16px;
}
.comment-body { flex: 1; min-width: 0; }
.comment-head {
  display: flex; align-items: baseline; gap: 10px; flex-wrap: wrap;
}
.comment-author { font-weight: 600; font-size: 14px; color: var(--text-1); }
.admin-badge {
  margin-left: 6px;
  font-size: 11px;
  color: #fff;
  background: var(--primary);
  padding: 1px 6px;
  border-radius: 999px;
  vertical-align: 1px;
}
.reply-tag { font-size: 12.5px; color: var(--primary); }
.comment-time { font-size: 12px; color: var(--text-3); }
.comment-content { font-size: 14px; margin: 6px 0; }
.comment-content :deep(p) { margin: 0.3em 0; }
.reply-btn {
  border: none; background: none; padding: 0;
  color: var(--text-3); font-size: 12.5px; cursor: pointer;
}
.reply-btn:hover { color: var(--primary); }
.comment-children {
  margin-left: 52px;
  padding-left: 12px;
  border-left: 2px solid var(--divider);
}
</style>
