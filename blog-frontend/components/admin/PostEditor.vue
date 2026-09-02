<script setup lang="ts">
// 文章/说说编辑器（新建与编辑共用）
import type { Post } from '~/types'

const props = defineProps<{ postId?: number }>()
const { get, post, put } = useApi()
const { render } = useMarkdown()

const form = reactive({
  type: 0,
  title: '',
  slug: '',
  summary: '',
  cover: '',
  contentMd: '',
  status: 1,
  password: '',
  isTop: 0,
  allowComment: 1,
  categoryIds: [] as number[],
  tagIds: [] as number[],
  publishedAt: ''
})

const categories = ref<any[]>([])
const tags = ref<any[]>([])
const loading = ref(false)
const saving = ref(false)

onMounted(async () => {
  try {
    categories.value = (await get('/api/admin/categories')) || []
    tags.value = (await get('/api/admin/tags')) || []
  } catch {}
  if (props.postId) {
    try {
      const p = await get<Post>(`/api/admin/posts/${props.postId}`)
      Object.assign(form, {
        type: p.type, title: p.title, slug: p.slug || '', summary: p.summary || '',
        cover: p.cover || '', contentMd: p.contentMd || '', status: p.status,
        password: p.password || '', isTop: p.isTop || 0, allowComment: p.allowComment ?? 1,
        categoryIds: p.categoryIds || [], tagIds: p.tagIds || [],
        publishedAt: (p as any).publishedAt || ''
      })
    } catch {}
  }
})

const previewHtml = computed(() => render(form.contentMd))

// slug 自动生成（新建文章时）
const autoSlug = () => {
  if (props.postId || form.type !== 0) return
  if (form.slug) return
  form.slug = `post-${Date.now().toString(36)}`
}

const save = async (publish: boolean) => {
  if (!form.title.trim()) return ElMessage.warning('请输入标题')
  if (form.status === 2 && !form.password) return ElMessage.warning('密码保护文章需设置密码')
  saving.value = true
  const payload = { ...form, status: publish ? form.status : 0 }
  try {
    if (props.postId) {
      await put(`/api/admin/posts/${props.postId}`, payload)
      ElMessage.success('已保存')
    } else {
      await post('/api/admin/posts', payload)
      ElMessage.success('已创建')
    }
    await navigateTo('/admin/posts')
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const typeLabel = computed(() => form.type === 2 ? '说说' : form.type === 1 ? '页面' : '文章')
</script>

<template>
  <div class="post-editor">
    <div class="editor-top">
      <el-radio-group v-model="form.type" size="default">
        <el-radio-button :value="0">文章</el-radio-button>
        <el-radio-button :value="1">页面</el-radio-button>
        <el-radio-button :value="2">说说</el-radio-button>
      </el-radio-group>
      <div class="top-actions">
        <el-button @click="save(false)" :loading="saving">存草稿</el-button>
        <el-button type="primary" @click="save(true)" :loading="saving">发布</el-button>
      </div>
    </div>

    <el-input v-model="form.title" placeholder="标题" size="large" class="title-input" @blur="autoSlug" />

    <div v-if="form.type !== 2" class="editor-grid">
      <div class="grid-main">
        <div class="md-split card">
          <div class="md-pane">
            <div class="pane-label">Markdown</div>
            <el-input v-model="form.contentMd" type="textarea" :rows="22" placeholder="正文（支持 Markdown / KaTeX 公式 / 代码高亮）" />
          </div>
          <div class="md-pane preview">
            <div class="pane-label">预览</div>
            <div class="preview-body markdown-body" v-html="previewHtml"></div>
          </div>
        </div>
      </div>
      <div class="grid-side">
        <div class="card side-card">
          <h4>状态</h4>
          <el-radio-group v-model="form.status">
            <el-radio :value="1">发布</el-radio>
            <el-radio :value="0">草稿</el-radio>
            <el-radio :value="2">密码保护</el-radio>
          </el-radio-group>
          <el-input v-if="form.status === 2" v-model="form.password" placeholder="访问密码" style="margin-top: 10px" />
        </div>
        <div v-if="form.type === 0" class="card side-card">
          <h4>分类</h4>
          <el-select v-model="form.categoryIds" multiple placeholder="选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
          <h4 style="margin-top: 14px">标签</h4>
          <el-select v-model="form.tagIds" multiple filterable allow-create placeholder="选择或新建标签" style="width: 100%">
            <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </div>
        <div class="card side-card">
          <h4>其他</h4>
          <el-input v-model="form.slug" placeholder="slug（留空自动生成）" style="margin-bottom: 10px">
            <template #prepend>/post/</template>
          </el-input>
          <el-input v-model="form.cover" placeholder="封面图 URL" style="margin-bottom: 10px" />
          <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="摘要（留空自动截取）" />
          <div style="margin-top: 10px">
            <el-checkbox v-model="form.isTop" :true-value="1" :false-value="0">置顶</el-checkbox>
            <el-checkbox v-model="form.allowComment" :true-value="1" :false-value="0">允许评论</el-checkbox>
          </div>
        </div>
      </div>
    </div>

    <!-- 说说编辑：单栏 -->
    <div v-else class="card shuoshuo-edit">
      <el-input v-model="form.contentMd" type="textarea" :rows="6" placeholder="说点什么吧…" />
      <div style="margin-top: 10px">
        <el-checkbox v-model="form.allowComment" :true-value="1" :false-value="0">允许评论</el-checkbox>
      </div>
    </div>
  </div>
</template>

<style scoped>
.editor-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.top-actions { display: flex; gap: 8px; }
.title-input { margin-bottom: 14px; }
.title-input :deep(input) { font-size: 18px; font-weight: 600; }
.editor-grid { display: grid; grid-template-columns: 1fr 300px; gap: 16px; }
.md-split { display: grid; grid-template-columns: 1fr 1fr; gap: 0; overflow: hidden; }
.md-pane { padding: 12px; }
.md-pane.preview { border-left: 1px solid var(--border-color); background: var(--bg-secondary, rgba(0,0,0,0.02)); }
.pane-label { font-size: 12px; color: var(--text-secondary); margin-bottom: 8px; }
.preview-body { max-height: 520px; overflow: auto; }
.side-card { padding: 16px; margin-bottom: 14px; }
.side-card h4 { margin: 0 0 10px; }
.shuoshuo-edit { padding: 20px; }
@media (max-width: 1100px) { .editor-grid { grid-template-columns: 1fr; } .md-split { grid-template-columns: 1fr; } .md-pane.preview { border-left: none; border-top: 1px solid var(--border-color); } }
</style>
