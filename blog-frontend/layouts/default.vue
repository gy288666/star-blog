<script setup lang="ts">
// 默认布局：背景图层 + 粒子星空 + Header + 内容 + Footer + Live2D 看板娘
const { siteTitle, load } = useSettings()

useHead({
  titleTemplate: (title) => (title ? `${title} · ${siteTitle.value}` : siteTitle.value)
})

// 标签页标题彩蛋：鼠标离开页面时 Doro 出来挽留，回来恢复
const LEAVE_TITLES = [
  '╭(°A°`)╮ 不要走！再看看嘛！',
  '(=´ω｀=) 汪…你去哪呀，等等我！',
  '(๑>ᴗ<๑) 偷橘子去啦？带我一个！',
  '(´･ω･`) 空调西瓜 Wi-Fi，留下嘛…'
]
let realTitle = ''
const onTitleChange = () => {
  // 路由切换/SEO 更新时同步记录真实标题
  if (!document.title.startsWith('➤')) realTitle = document.title
}
const onLeave = () => {
  realTitle = document.title
  document.title = '➤ ' + LEAVE_TITLES[Math.floor(Math.random() * LEAVE_TITLES.length)]
}
const onBack = () => {
  if (realTitle) document.title = realTitle
}
onMounted(() => {
  realTitle = document.title
  document.addEventListener('titlechange', onTitleChange)
  document.documentElement.addEventListener('mouseleave', onLeave)
  document.documentElement.addEventListener('mouseenter', onBack)
})
onBeforeUnmount(() => {
  document.removeEventListener('titlechange', onTitleChange)
  document.documentElement.removeEventListener('mouseleave', onLeave)
  document.documentElement.removeEventListener('mouseenter', onBack)
})
</script>

<template>
  <div class="default-layout">
    <CommonSiteBackground />
    <ClientOnly>
      <CommonParticleBackground />
    </ClientOnly>
    <div class="layout-content">
      <CommonTheHeader />
      <main class="layout-main">
        <slot />
      </main>
      <CommonTheFooter />
    </div>
    <ClientOnly>
      <CommonLive2D />
    </ClientOnly>
  </div>
</template>

<style scoped>
.default-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
}
.layout-content {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  flex: 1;
}
.layout-main {
  flex: 1;
  padding-bottom: 56px;
}
</style>
