<script setup lang="ts">
// 说说流
import type { Post, Comment } from '~/types'

const route = useRoute()
const page = ref(Number(route.query.page) || 1)

const { data } = await useAsyncData<any>(
  'shuoshuos',
  async () => {
    const { get } = useApi()
    try {
      return await get<any>('/api/shuoshuos', { page: page.value, size: 10 })
    } catch {
      return { records: [], total: 0 }
    }
  },
  { watch: [page] }
)

const list = computed<Post[]>(() => data.value?.records || [])
const total = computed(() => data.value?.total || 0)

useHead({ title: '说说' })

// 每条说说的点赞状态与评论展开
const liked = reactive<Record<number, boolean>>({})
const likedSet = () => {
  // localStorage 记录已点赞 id，防重复展示
  if (!import.meta.client) return
  try {
    const s = JSON.parse(localStorage.getItem('shuoshuo_liked') || '[]') as number[]
    s.forEach((id) => (liked[id] = true))
  } catch {}
}
onMounted(likedSet)

const { put } = useApi()
const like = async (p: Post) => {
  if (liked[p.id]) return
  try {
    const r = await put<any>(`/api/shuoshuos/${p.id}/like`)
    p.upvotes = r?.upvotes ?? (p.upvotes || 0) + 1
    liked[p.id] = true
    if (import.meta.client) {
      const s = JSON.parse(localStorage.getItem('shuoshuo_liked') || '[]') as number[]
      s.push(p.id)
      localStorage.setItem('shuoshuo_liked', JSON.stringify([...new Set(s)]))
    }
  } catch {}
}

// 评论
const openComments = reactive<Record<number, boolean>>({})
const commentsBy = reactive<Record<number, Comment[]>>({})
const commentTotals = reactive<Record<number, number>>({})
const { get } = useApi()

const toggleComments = async (p: Post) => {
  openComments[p.id] = !openComments[p.id]
  if (openComments[p.id] && !commentsBy[p.id]) {
    try {
      const r = await get<any>(`/api/shuoshuos/${p.id}/comments`, { page: 1, size: 20 })
      commentsBy[p.id] = r?.records || []
      commentTotals[p.id] = r?.total || 0
    } catch {
      commentsBy[p.id] = []
    }
  }
}

const { render } = useMarkdown()
</script>

<template>
  <div class="shuoshuo-page container">
    <div class="shuoshuo-head card">
      <h1>🙋 说说</h1>
      <p>碎碎念，记录日常</p>
    </div>

    <div v-for="p in list" :key="p.id" class="shuoshuo-card card">
      <div class="ss-header">
        <span class="ss-avatar">🙋</span>
        <div>
          <div class="ss-author">{{ p.authorNickname || '站长' }}</div>
          <div class="ss-time">{{ p.publishedAt || p.createTime }}</div>
        </div>
      </div>
      <div class="ss-content" v-html="render(p.contentHtml || p.contentMd)"></div>
      <div class="ss-actions">
        <button class="ss-action" :class="{ liked: liked[p.id] }" @click="like(p)">
          ❤️ {{ p.upvotes || 0 }}
        </button>
        <button class="ss-action" @click="toggleComments(p)">💬 {{ p.commentCount || 0 }}</button>
      </div>
      <div v-if="openComments[p.id]" class="ss-comments">
        <CommentForm :post-id="p.id" @success="() => { delete commentsBy[p.id]; toggleComments(p); openComments[p.id] = true }" />
        <CommentItem v-for="c in commentsBy[p.id] || []" :key="c.id" :comment="c" />
      </div>
    </div>

    <div v-if="!list.length" class="empty card"><p>还没有说说～</p></div>
    <CommonPagination
      v-if="total > 10"
      :page="page"
      :total="total"
      :size="10"
      @change="(n) => { page = n; navigateTo({ path: '/shuoshuo', query: { page: String(n) } }) }"
    />
  </div>
</template>

<style scoped>
.shuoshuo-head { padding: 24px; margin-bottom: 20px; }
.shuoshuo-head h1 { margin: 0 0 6px; font-size: 22px; }
.shuoshuo-head p { margin: 0; color: var(--text-secondary); }
.shuoshuo-card { padding: 20px 24px; margin-bottom: 18px; }
.ss-header { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.ss-avatar { font-size: 34px; }
.ss-author { font-weight: 600; }
.ss-time { font-size: 12px; color: var(--text-secondary); }
.ss-content { line-height: 1.8; margin-bottom: 12px; }
.ss-actions { display: flex; gap: 18px; }
.ss-action { border: none; background: none; cursor: pointer; color: var(--text-secondary); font-size: 14px; padding: 4px 8px; border-radius: 6px; }
.ss-action:hover { background: var(--bg-hover); }
.ss-action.liked { color: #f75676; }
.ss-comments { margin-top: 14px; border-top: 1px dashed var(--border-color); padding-top: 14px; display: flex; flex-direction: column; gap: 8px; }
.empty { text-align: center; padding: 50px 0; color: var(--text-secondary); }
</style>
