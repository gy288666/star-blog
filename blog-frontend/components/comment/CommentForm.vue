<script setup lang="ts">
// 评论表单：昵称/邮箱/网址/内容/验证码，localStorage 记住访客信息
import type { Comment } from '~/types'

const props = withDefaults(defineProps<{
  postId: number
  parentId?: number
  rootId?: number
  replyTo?: string
}>(), { parentId: 0, rootId: 0 })

const emit = defineEmits<{ (e: 'success', comment: Comment): void; (e: 'cancel'): void }>()

const { get, post } = useApi()

const author = ref('')
const email = ref('')
const website = ref('')
const content = ref('')
const captchaCode = ref('')
const captcha = ref<{ key: string; image: string } | null>(null)
const submitting = ref(false)
const errorMsg = ref('')

onMounted(() => {
  author.value = localStorage.getItem('blog_comment_author') || ''
  email.value = localStorage.getItem('blog_comment_email') || ''
  website.value = localStorage.getItem('blog_comment_website') || ''
  refreshCaptcha()
})

const refreshCaptcha = async () => {
  captchaCode.value = ''
  try {
    captcha.value = await get<{ key: string; image: string }>('/api/captcha')
  } catch {
    captcha.value = null // 验证码不可用时后端应允许提交失败提示
  }
}

const submit = async () => {
  errorMsg.value = ''
  if (!author.value.trim() || !content.value.trim()) {
    errorMsg.value = '请填写昵称和评论内容'
    return
  }
  submitting.value = true
  try {
    const comment = await post<Comment>('/api/comments', {
      postId: props.postId,
      parentId: props.parentId,
      rootId: props.rootId,
      author: author.value.trim(),
      email: email.value.trim(),
      website: website.value.trim(),
      content: content.value.trim(),
      captchaKey: captcha.value?.key || '',
      captchaCode: captchaCode.value.trim()
    })
    // 记住访客信息
    localStorage.setItem('blog_comment_author', author.value.trim())
    localStorage.setItem('blog_comment_email', email.value.trim())
    localStorage.setItem('blog_comment_website', website.value.trim())
    content.value = ''
    emit('success', comment)
  } catch (e: any) {
    errorMsg.value = e?.message || '评论提交失败'
    refreshCaptcha()
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="comment-form card">
    <p v-if="replyTo" class="replying">
      回复 <b>{{ replyTo }}</b>
      <button class="cancel-reply" type="button" @click="emit('cancel')">取消</button>
    </p>
    <div class="form-row visitor-row">
      <input v-model="author" class="input" placeholder="昵称（必填）" maxlength="50" />
      <input v-model="email" class="input" type="email" placeholder="邮箱（不公开）" maxlength="100" />
      <input v-model="website" class="input" placeholder="网址（选填）" maxlength="200" />
    </div>
    <textarea v-model="content" class="input content-input" rows="4" placeholder="写下你的评论…支持 Markdown" maxlength="2000"></textarea>
    <div class="form-row submit-row">
      <div class="captcha-box">
        <input v-model="captchaCode" class="input captcha-input" placeholder="验证码" maxlength="10" @keydown.enter="submit" />
        <img v-if="captcha" :src="captcha.image" alt="验证码" class="captcha-img" title="点击刷新" @click="refreshCaptcha" />
      </div>
      <button class="btn" :disabled="submitting" @click="submit">{{ submitting ? '提交中…' : '发表评论' }}</button>
    </div>
    <p v-if="errorMsg" class="form-error">{{ errorMsg }}</p>
  </div>
</template>

<style scoped>
.comment-form { padding: 18px 20px; }
.replying {
  margin: 0 0 10px;
  font-size: 13px;
  color: var(--text-2);
}
.cancel-reply {
  margin-left: 10px;
  border: none;
  background: none;
  color: var(--danger);
  cursor: pointer;
  font-size: 13px;
}
.form-row { display: flex; gap: 10px; margin-bottom: 10px; flex-wrap: wrap; }
.visitor-row .input { flex: 1; min-width: 140px; }
.content-input { resize: vertical; min-height: 90px; margin-bottom: 10px; }
.submit-row { justify-content: space-between; align-items: center; margin-bottom: 0; }
.captcha-box { display: flex; gap: 8px; align-items: center; }
.captcha-input { width: 110px; }
.captcha-img { height: 38px; border-radius: 6px; cursor: pointer; }
.form-error { margin: 10px 0 0; color: var(--danger); font-size: 13px; }
</style>
