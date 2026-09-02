<script setup lang="ts">
// 单页（关于/留言板等）
import type { Post } from '~/types'

const route = useRoute()
const slug = route.params.slug as string

const { data: post, error } = await useAsyncData<Post>(
  `page-${slug}`,
  async () => {
    const { get } = useApi()
    try {
      return await get<Post>(`/api/pages/${encodeURIComponent(slug)}`)
    } catch {
      return null
    }
  }
)

useHead(() => ({ title: post.value?.title || '页面不存在' }))

const { render } = useMarkdown()
const contentHtml = computed(() => (post.value ? post.value.contentHtml || render(post.value.contentMd) : ''))

// 留言板页：允许评论时也展示评论区
const comments = ref<Comment[]>([])
const commentTotal = ref(0)
const loadComments = async () => {
  if (!post.value?.id) return
  try {
    const r = await get<any>('/api/comments', { postId: post.value.id, page: 1, size: 20 })
    comments.value = r?.records || []
    commentTotal.value = r?.total || 0
  } catch {
    comments.value = []
  }
}
await loadComments()
</script>

<template>
  <div class="page-view container">
    <div v-if="!post" class="empty card"><p>页面不存在</p></div>
    <template v-else>
      <article class="single-page card">
        <h1 class="page-title">{{ post.title }}</h1>
        <div class="page-content markdown-body" v-html="contentHtml"></div>
      </article>
      <section v-if="post.allowComment !== 0" class="comments-section card">
        <h2 class="section-title"><UiIcon name="chat" :size="17" /> 留言（{{ commentTotal }}）</h2>
        <CommentForm :post-id="post.id" @success="loadComments" />
        <div v-if="comments.length" class="comment-list">
          <CommentItem v-for="c in comments" :key="c.id" :comment="c" />
        </div>
        <p v-else class="comment-empty">还没有留言，来抢沙发～</p>
      </section>
    </template>
  </div>
</template>

<style scoped>
.page-title { margin-top: 0; font-size: 26px; margin-bottom: 16px; }
.single-page { padding: 32px; }
.comments-section { margin-top: 24px; padding: 24px; }
.section-title { margin-top: 0; font-size: 18px; }
.comment-list { margin-top: 20px; display: flex; flex-direction: column; gap: 8px; }
.comment-empty { text-align: center; color: var(--text-secondary); padding: 20px 0; }
.empty { text-align: center; padding: 60px 0; color: var(--text-secondary); }
</style>
