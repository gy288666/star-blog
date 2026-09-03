<script setup lang="ts">
// 说说流：任意访客可发布，仅发布人/管理员可删除
import type { Post, Comment } from '~/types'

const route = useRoute()
const page = ref(Number(route.query.page) || 1)

// 访客身份：localStorage 持久化 UUID，作为发布/删除凭证（X-Guest-Id 头）
const guestId = ref('')
if (import.meta.client) {
  let gid = localStorage.getItem('blog_guest_id')
  if (!gid) {
    gid = crypto.randomUUID().replace(/-/g, '')
    localStorage.setItem('blog_guest_id', gid)
  }
  guestId.value = gid
}
const guestHeaders = () => ({ 'X-Guest-Id': guestId.value })

const { request, get, put } = useApi()
const gget = <T,>(url: string, params?: Record<string, any>) =>
  request<T>(url, { method: 'GET', params, headers: guestHeaders() })

const loadShuoshuos = async () => {
  try {
    return await gget<any>('/api/shuoshuos', { page: page.value, size: 10 })
  } catch {
    return { records: [], total: 0 }
  }
}

const { data } = await useAsyncData<any>('shuoshuos', loadShuoshuos, { watch: [page] })

const list = computed<Post[]>(() => data.value?.records || [])
const total = computed(() => data.value?.total || 0)

useHead({ title: '说说' })

// ---------- 游客发布 ----------
const nickname = ref('')
const draft = ref('')
const publishing = ref(false)
const publish = async () => {
  if (draft.value.trim().length < 2 || publishing.value) return
  publishing.value = true
  try {
    await request<any>('/api/shuoshuos', {
      method: 'POST',
      body: { content: draft.value, nickname: nickname.value || undefined },
      headers: guestHeaders()
    })
    draft.value = ''
    data.value = await loadShuoshuos()
  } catch (e: any) {
    alert(e?.message || '发布失败')
  } finally {
    publishing.value = false
  }
}

// ---------- 删除（仅自己发布的显示按钮） ----------
const deleting = ref<number | null>(null)
const remove = async (p: Post) => {
  if (!confirm('确定删除这条说说吗？')) return
  deleting.value = p.id
  try {
    await request<any>(`/api/shuoshuos/${p.id}`, { method: 'DELETE', headers: guestHeaders() })
    data.value = await loadShuoshuos()
  } catch (e: any) {
    alert(e?.message || '删除失败')
  } finally {
    deleting.value = null
  }
}

// ---------- 点赞 ----------
const liked = reactive<Record<number, boolean>>({})
const likedSet = () => {
  if (!import.meta.client) return
  try {
    const s = JSON.parse(localStorage.getItem('shuoshuo_liked') || '[]') as number[]
    s.forEach((id) => (liked[id] = true))
  } catch {}
}
onMounted(likedSet)

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

// ---------- 评论 ----------
const openComments = reactive<Record<number, boolean>>({})
const commentsBy = reactive<Record<number, Comment[]>>({})
const toggleComments = async (p: Post) => {
  openComments[p.id] = !openComments[p.id]
  if (openComments[p.id] && !commentsBy[p.id]) {
    try {
      const r = await get<any>(`/api/shuoshuos/${p.id}/comments`, { page: 1, size: 20 })
      commentsBy[p.id] = r?.records || []
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
      <h1 class="page-heading"><UiIcon name="quote" :size="20" /> 说说</h1>
      <p>碎碎念，记录日常 —— 所有人都可以发一条</p>
    </div>

    <!-- 发布框：任何访客可发 -->
    <div class="publish-box card">
      <div class="pub-row">
        <input v-model="nickname" class="pub-name" type="text" maxlength="20" placeholder="昵称（可留空 = 匿名访客）" />
        <span class="pub-hint">以浏览器身份发布，仅你（和管理员）能删除</span>
      </div>
      <textarea v-model="draft" class="pub-text" rows="3" maxlength="1000" placeholder="说点什么吧…"></textarea>
      <div class="pub-actions">
        <span class="pub-count">{{ draft.length }}/1000</span>
        <button class="pub-btn" :disabled="publishing || draft.trim().length < 2" @click="publish">
          {{ publishing ? '发布中…' : '发布说说' }}
        </button>
      </div>
    </div>

    <div v-for="p in list" :key="p.id" class="shuoshuo-card card">
      <div class="ss-header">
        <span class="ss-avatar"><UiIcon name="user" :size="20" /></span>
        <div>
          <div class="ss-author">{{ p.authorNickname || '匿名访客' }}<span v-if="p.mine" class="mine-tag">我发的</span></div>
          <div class="ss-time">{{ p.publishedAt || p.createTime }}</div>
        </div>
        <button
          v-if="p.mine"
          class="ss-delete"
          :disabled="deleting === p.id"
          title="删除这条说说"
          @click="remove(p)"
        >
          <UiIcon name="trash" :size="14" /> {{ deleting === p.id ? '删除中' : '删除' }}
        </button>
      </div>
      <div class="ss-content" v-html="render(p.contentHtml || p.contentMd)"></div>
      <div class="ss-actions">
        <button class="ss-action" :class="{ liked: liked[p.id] }" @click="like(p)">
          <UiIcon name="heart" :size="14" /> {{ p.upvotes || 0 }}
        </button>
        <button class="ss-action" @click="toggleComments(p)"><UiIcon name="chat" :size="14" /> {{ p.commentCount || 0 }}</button>
      </div>
      <div v-if="openComments[p.id]" class="ss-comments">
        <CommentForm :post-id="p.id" @success="() => { delete commentsBy[p.id]; toggleComments(p); openComments[p.id] = true }" />
        <CommentItem v-for="c in commentsBy[p.id] || []" :key="c.id" :comment="c" />
      </div>
    </div>

    <div v-if="!list.length" class="empty card"><p>还没有说说，来发第一条吧～</p></div>
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
.shuoshuo-head p { margin: 0; color: var(--text-secondary, var(--text-3)); }

/* 发布框 */
.publish-box { padding: 18px 20px; margin-bottom: 20px; }
.pub-row { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; }
.pub-name {
  padding: 7px 12px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--bg-input);
  color: var(--text-1);
  font-size: 13px;
  width: 220px;
  outline: none;
}
.pub-name:focus { border-color: var(--primary); }
.pub-hint { font-size: 12px; color: var(--text-3); }
.pub-text {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  background: var(--bg-input);
  color: var(--text-1);
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  outline: none;
}
.pub-text:focus { border-color: var(--primary); }
.pub-actions { display: flex; align-items: center; justify-content: flex-end; gap: 12px; margin-top: 10px; }
.pub-count { font-size: 12px; color: var(--text-3); }
.pub-btn {
  padding: 8px 22px;
  border: none;
  border-radius: 8px;
  background: var(--primary);
  color: #fff;
  font-size: 13.5px;
  cursor: pointer;
  transition: opacity 0.2s;
}
.pub-btn:disabled { opacity: 0.45; cursor: not-allowed; }
.pub-btn:not(:disabled):hover { opacity: 0.88; }

.shuoshuo-card { padding: 20px 24px; margin-bottom: 18px; }
.ss-header { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; position: relative; }
.ss-avatar {
  width: 38px; height: 38px;
  display: inline-flex; align-items: center; justify-content: center;
  border-radius: 50%;
  background: var(--bg-hover);
  color: var(--primary);
  flex-shrink: 0;
}
.ss-author { font-weight: 600; display: flex; align-items: center; gap: 8px; }
.mine-tag {
  font-size: 11px;
  font-weight: 500;
  color: var(--primary);
  background: var(--bg-hover);
  padding: 1px 8px;
  border-radius: 999px;
}
.ss-time { font-size: 12px; color: var(--text-secondary, var(--text-3)); }
.ss-delete {
  position: absolute;
  right: 0; top: 4px;
  display: inline-flex; align-items: center; gap: 4px;
  border: 1px solid var(--border-color);
  background: none;
  border-radius: 8px;
  padding: 4px 10px;
  font-size: 12px;
  color: var(--text-3);
  cursor: pointer;
  transition: color 0.2s, border-color 0.2s;
}
.ss-delete:hover { color: var(--danger, #f5365c); border-color: var(--danger, #f5365c); }
.ss-delete:disabled { opacity: 0.5; cursor: not-allowed; }
.ss-content { line-height: 1.8; margin-bottom: 12px; }
.ss-actions { display: flex; gap: 18px; }
.ss-action {
  display: inline-flex; align-items: center; gap: 5px;
  border: none; background: none; cursor: pointer;
  color: var(--text-secondary, var(--text-3)); font-size: 14px;
  padding: 4px 8px; border-radius: 6px;
}
.ss-action:hover { background: var(--bg-hover); }
.ss-action.liked { color: #f75676; }
.ss-comments { margin-top: 14px; border-top: 1px dashed var(--border-color); padding-top: 14px; display: flex; flex-direction: column; gap: 8px; }
.empty { text-align: center; padding: 50px 0; color: var(--text-secondary, var(--text-3)); }
</style>
