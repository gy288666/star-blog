<script setup lang="ts">
// 根组件：主题初始化 + 访问统计
const { settings, load } = useSettings()
const theme = useTheme()

// 主题初始化：先按本地存储应用，设置加载后用 darkModeDefault 兜底
onMounted(() => {
  theme.init()
})
watch(() => settings.value.darkModeDefault, (v) => {
  if (v !== undefined && !localStorage.getItem('blog_theme_mode')) theme.init(v)
})

// 访问统计：路由切换后上报（仅客户端，后台页除外）
const route = useRoute()
onMounted(() => {
  let lastUrl = ''
  watch(() => route.fullPath, (url) => {
    if (url.startsWith('/admin') || url === lastUrl) return
    lastUrl = url
    const { post } = useApi()
    post('/api/visit', { url, referer: document.referrer || '' }).catch(() => {})
  }, { immediate: true })
})
</script>

<template>
  <NuxtLayout>
    <NuxtPage />
  </NuxtLayout>
</template>
