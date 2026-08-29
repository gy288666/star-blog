<script setup lang="ts">
// 文章详情：SEO + 正文渲染 + 浏览量 + 上下篇 + 相关 + 评论
import type { Post, PostDetail, Comment } from '~/types'

const route = useRoute()
const slug = route.params.slug as string
const { get, put } = useApi()

const postRef = ref<PostDetail | null>(null)
const passwordRequired = ref(false)
const password = ref('')
const loadError = ref(false)

const load = async () => {
  loadError.value = false
  try {
    const data = await get<any>(`/api/posts/${encodeURIComponent(slug)}`, password.value ? { password: password.value } : undefined)
    postRef.value = data?.post || null
    passwordRequired.value = !!data?.passwordRequired
    if (postRef.value) {
      postRef.value.prev = data?.prev || null
      postRef.value.next = data?.next || null
    }
  } catch {
    loadError.value = true
  }
}

await load()

const post = computed(() => postRef.value)

// SEO
const { siteTitle } = useSettings()
const { renderText } = useMarkdown()
useHead(() => ({
  title: post.value?.title || '文章不存在',
  meta: [
    { name: 'description', content: post.value?.summary || renderText(post.value?.contentMd, 100) },
    { property: 'og:title', content: `${post.value?.title || ''} · ${siteTitle.value}` },
    { property: 'og:type', content: 'article' }
  ]
}))

// 浏览量上报（仅客户端，成功后本地 +1 展示）
onMounted(() => {
  if (post.value?.id) {
    put(`/api/posts/${post.value.id}/views`).then((r: any) => {
      if (post.value && typeof r?.views === 'number') post.value.views = r.views
    }).catch(() => {})
  }
})

const retryWithPassword = async () => {
  await load()
}

// 评论区
const comments = ref<Comment[]>([])
const commentTotal = ref(0)
const commentPage = ref(1)
const replyTarget = ref<Comment | null>(null)

const loadComments = async () => {
  if (!post.value?.id) return
  try {
    const r = await get<any>('/api/comments', { postId: post.value.id, page: commentPage.value, size: 10 })
    comments.value = r?.records || []
    commentTotal.value = r?.total || 0
  } catch {
    comments.value = []
  }
}
await loadComments()

const { render } = useMarkdown()
const contentHtml = computed(() => {
  if (!post.value) return ''
  return post.value.contentHtml || render(post.value.contentMd)
})
</script>

<template>
  <div class="post-page container">
    <div v-if="loadError" class="empty card">
      <p>😕 文章不存在或加载失败</p>
      <NuxtLink to="/" class="back-link">← 返回首页</NuxtLink>
    </div>

    <template v-else-if="post">
      <article class="post-detail card post-article">
        <!-- 封面 hero -->
        <div v-if="post.cover" class="post-hero">
          <img :src="post.cover" :alt="post.title" />
        </div>
        <header class="post-header">
          <h1 class="post-title">{{ post.title }}</h1>
          <PostMeta :post="post" />
        </header>

        <!-- 密码保护 -->
        <div v-if="passwordRequired" class="password-gate">
          <p>🔒 本文已被密码保护，请输入密码查看：</p>
          <div class="pwd-form">
            <input v-model="password" type="password" placeholder="文章密码" @keyup.enter="retryWithPassword" />
            <button class="btn-primary" @click="retryWithPassword">查看</button>
          </div>
        </div>

        <!-- 正文 -->
        <div
          v-else
          class="post-content markdown-body"
          v-html="contentHtml"
        ></div>
      </article>

      <PostNavigation :post="post" />
      <RelatedPosts :post-id="post.id" />

      <section v-if="post.allowComment !== 0" class="comments-section card">
        <h2 class="section-title">💬 评论（{{ commentTotal }}）</h2>
        <CommentForm
          v-if="replyTarget"
          :key="`reply-${replyTarget.id}`"
          :post-id="post.id"
          :parent-id="replyTarget.id"
          :root-id="replyTarget.rootId || replyTarget.id"
          :reply-to="replyTarget.author"
          @success="() => { replyTarget = null; loadComments() }"
          @cancel="replyTarget = null"
        />
        <CommentForm v-else :post-id="post.id" @success="loadComments" />
        <div v-if="comments.length" class="comment-list">
          <CommentItem v-for="c in comments" :key="c.id" :comment="c" @reply="(t) => (replyTarget = t)" />
        </div>
        <p v-else class="comment-empty">还没有评论，来说两句吧～</p>
        <CommonPagination
          v-if="commentTotal > 10"
          :page="commentPage"
          :total="commentTotal"
          :size="10"
          @change="(p) => { commentPage = p; loadComments() }"
        />
      </section>
    </template>
  </div>
</template>

<style scoped>
.post-article .post-header,
.post-article .post-content,
.post-article .password-gate { padding-left: 34px; padding-right: 34px; }
.post-article .post-header { padding-top: 26px; }
.post-hero {
  margin: -1px -1px 0;
  border-radius: var(--radius-card) var(--radius-card) 0 0;
  overflow: hidden;
  max-height: 380px;
}
.post-hero img {
  width: 100%;
  height: 100%;
  max-height: 380px;
  object-fit: cover;
  display: block;
}
.post-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
}
.post-title {
  font-size: 28px;
  margin: 0 0 12px;
  line-height: 1.4;
}
.password-gate {
  text-align: center;
  padding: 40px 0;
  color: var(--text-secondary);
}
.pwd-form {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-top: 16px;
}
.pwd-form input {
  padding: 10px 14px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--bg-input);
  color: var(--text-primary);
  width: 220px;
}
.btn-primary {
  padding: 10px 22px;
  border: none;
  border-radius: 8px;
  background: var(--primary);
  color: #fff;
  cursor: pointer;
}
.btn-primary:hover { opacity: 0.9; }
.empty {
  text-align: center;
  padding: 60px 0;
  color: var(--text-secondary);
}
.back-link { color: var(--primary); margin-top: 10px; display: inline-block; }
.comments-section {
  margin-top: 24px;
  padding: 24px;
}
.section-title { margin-top: 0; font-size: 18px; }
.comment-list { margin-top: 20px; display: flex; flex-direction: column; gap: 8px; }
.comment-empty { text-align: center; color: var(--text-secondary); padding: 20px 0; }
</style>
