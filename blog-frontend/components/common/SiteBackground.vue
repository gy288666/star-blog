<script setup lang="ts">
/**
 * 站点背景图层：明/暗两套壁纸 + 可调透明度（Argon page_background_* 的对应实现）
 * - 亮色壁纸 light / 暗色壁纸 dark，随 html.dark 切换，过渡平滑
 * - 暗色下额外压暗去饱和，保证前景可读（低饱和、宏观宁静）
 * - 透明度来自公开设置 pageBackgroundOpacity（默认 0.6）
 */
const { settings } = useSettings()

const lightBg = computed(() => settings.value.pageBackgroundImage || '/bg/page-light.webp')
const darkBg = computed(() => settings.value.pageBackgroundImageDark || '/bg/page-dark.webp')
const opacity = computed(() => {
  const v = Number(settings.value.pageBackgroundOpacity)
  return Number.isFinite(v) && v > 0 ? Math.min(v, 1) : 0.6
})
</script>

<template>
  <div class="site-bg" aria-hidden="true">
    <img class="bg-img light" :src="lightBg" alt="" :style="{ opacity }" />
    <img class="bg-img dark" :src="darkBg" alt="" :style="{ opacity }" />
    <!-- 暗色压暗纱 + 底部渐隐，亮色柔光纱 -->
    <div class="veil veil-light"></div>
    <div class="veil veil-dark"></div>
  </div>
</template>

<style scoped>
.site-bg {
  position: fixed;
  inset: 0;
  z-index: -1;
  overflow: hidden;
  background: var(--bg-page);
}
.bg-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: opacity 0.6s ease, filter 0.6s ease;
}
.bg-img.light { display: block; }
.bg-img.dark { display: none; filter: brightness(0.6) saturate(0.75); }
html.dark .bg-img.light { display: none; }
html.dark .bg-img.dark { display: block; }
/* 纱层：保证文字可读性 */
.veil { position: absolute; inset: 0; transition: opacity 0.6s ease; }
.veil-light {
  background: linear-gradient(180deg, rgba(246, 247, 251, 0.34) 0%, rgba(246, 247, 251, 0.5) 100%);
}
.veil-dark { display: none; }
html.dark .veil-light { display: none; }
html.dark .veil-dark {
  display: block;
  background:
    radial-gradient(ellipse at 20% 0%, rgba(94, 114, 228, 0.1), transparent 55%),
    linear-gradient(180deg, rgba(14, 17, 26, 0.42) 0%, rgba(14, 17, 26, 0.62) 100%);
}
</style>
