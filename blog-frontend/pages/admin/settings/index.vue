<script setup lang="ts">
// 站点设置 + 修改密码
definePageMeta({ layout: 'admin' })

useHead({ title: '站点设置' })

const { get, put } = useApi()

const form = reactive<Record<string, string>>({
  siteTitle: '', siteSubtitle: '', siteLogo: '', footerText: '', icpText: '',
  bannerTitle: '', bannerSubtitle: '', bannerImage: '', bannerSource: 'custom',
  bannerTypingEffect: '0', allowComment: '1', darkModeDefault: 'light', siteUrl: ''
})

const loading = ref(true)
onMounted(async () => {
  try {
    Object.assign(form, (await get('/api/admin/settings')) || {})
  } catch {}
  loading.value = false
})

const saving = ref(false)
const save = async () => {
  saving.value = true
  try {
    await put('/api/admin/settings', { ...form })
    ElMessage.success('已保存')
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally { saving.value = false }
}

// 修改密码
const pwd = reactive({ oldPassword: '', newPassword: '', confirm: '' })
const changePwd = async () => {
  if (!pwd.oldPassword || !pwd.newPassword) return ElMessage.warning('请填写完整')
  if (pwd.newPassword.length < 6) return ElMessage.warning('新密码至少 6 位')
  if (pwd.newPassword !== pwd.confirm) return ElMessage.warning('两次输入的新密码不一致')
  try {
    await put('/api/admin/password', { oldPassword: pwd.oldPassword, newPassword: pwd.newPassword })
    ElMessage.success('密码已修改')
    pwd.oldPassword = pwd.newPassword = pwd.confirm = ''
  } catch (e: any) {
    ElMessage.error(e?.message || '修改失败')
  }
}
</script>

<template>
  <div v-loading="loading" class="settings-page">
    <div class="card set-card">
      <h3>站点信息</h3>
      <el-form label-width="120px">
        <el-form-item label="站点标题"><el-input v-model="form.siteTitle" /></el-form-item>
        <el-form-item label="副标题"><el-input v-model="form.siteSubtitle" /></el-form-item>
        <el-form-item label="Logo URL"><el-input v-model="form.siteLogo" /></el-form-item>
        <el-form-item label="站点 URL"><el-input v-model="form.siteUrl" placeholder="https://blog.20260006.xyz" /></el-form-item>
        <el-form-item label="页脚文案"><el-input v-model="form.footerText" /></el-form-item>
        <el-form-item label="ICP 备案号"><el-input v-model="form.icpText" /></el-form-item>
      </el-form>
    </div>

    <div class="card set-card">
      <h3>首页横幅</h3>
      <el-form label-width="120px">
        <el-form-item label="标题"><el-input v-model="form.bannerTitle" /></el-form-item>
        <el-form-item label="副标题"><el-input v-model="form.bannerSubtitle" /></el-form-item>
        <el-form-item label="背景图"><el-input v-model="form.bannerImage" placeholder="图片 URL" /></el-form-item>
        <el-form-item label="背景源">
          <el-radio-group v-model="form.bannerSource">
            <el-radio value="custom">自定义</el-radio>
            <el-radio value="bing">Bing 每日一图</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="打字机效果">
          <el-switch v-model="form.bannerTypingEffect" active-value="1" inactive-value="0" />
        </el-form-item>
      </el-form>
    </div>

    <div class="card set-card">
      <h3>功能开关</h3>
      <el-form label-width="120px">
        <el-form-item label="允许评论">
          <el-switch v-model="form.allowComment" active-value="1" inactive-value="0" />
        </el-form-item>
        <el-form-item label="默认暗色模式">
          <el-radio-group v-model="form.darkModeDefault">
            <el-radio value="light">浅色</el-radio>
            <el-radio value="dark">深色</el-radio>
            <el-radio value="auto">跟随系统</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </div>

    <div class="card set-card">
      <h3>修改密码</h3>
      <el-form label-width="120px" style="max-width: 420px">
        <el-form-item label="原密码"><el-input v-model="pwd.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="pwd.newPassword" type="password" show-password /></el-form-item>
        <el-form-item label="确认新密码"><el-input v-model="pwd.confirm" type="password" show-password /></el-form-item>
        <el-form-item>
          <el-button type="warning" @click="changePwd">修改密码</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div style="margin-top: 6px">
      <el-button type="primary" size="large" :loading="saving" @click="save">保存设置</el-button>
    </div>
  </div>
</template>

<style scoped>
.set-card { padding: 20px 24px; margin-bottom: 16px; }
.set-card h3 { margin-top: 0; }
.settings-page { max-width: 720px; }
</style>
