<script setup lang="ts">
// 管理端登录
import type { AdminUser } from '~/types'

definePageMeta({ layout: false })
useHead({ title: '登录' })

const { post } = useApi()
const auth = useAuthStore()
auth.init()
const router = useRouter()

const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

if (auth.isLoggedIn) {
  router.replace('/admin')
}

const submit = async () => {
  if (!username.value || !password.value) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const r = await post<any>('/api/auth/login', { username: username.value, password: password.value })
    auth.setLogin(r.token, r.user as AdminUser)
    router.replace('/admin')
  } catch (e: any) {
    error.value = e?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card card">
      <h1 class="login-title"><UiIcon name="pen" :size="20" /> 博客管理</h1>
      <form @submit.prevent="submit">
        <div class="form-item">
          <label>用户名</label>
          <el-input v-model="username" placeholder="用户名" size="large" />
        </div>
        <div class="form-item">
          <label>密码</label>
          <el-input v-model="password" type="password" placeholder="密码" size="large" show-password @keyup.enter="submit" />
        </div>
        <p v-if="error" class="login-error">{{ error }}</p>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" native-type="submit">
          登 录
        </el-button>
      </form>
      <p class="login-tip">默认账号 admin / admin123，登录后请修改密码</p>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
}
.login-card {
  width: 360px;
  padding: 36px;
}
.login-title { text-align: center; margin: 0 0 24px; }
.form-item { margin-bottom: 18px; }
.form-item label { display: block; margin-bottom: 6px; font-size: 13px; color: var(--text-secondary); }
.login-error { color: #f75676; font-size: 13px; margin: 0 0 12px; }
.login-tip { text-align: center; color: var(--text-secondary); font-size: 12px; margin-top: 16px; }
</style>
