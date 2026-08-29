<script setup lang="ts">
// 首页 Banner：默认沉浸式（透出全站星空壁纸+粒子），配置了自定义大图时走图片模式
import type { Banner } from '~/types'

const { settings } = useSettings()

const { data: banners } = await useAsyncData<Banner[]>(
  'home-banners',
  async () => {
    const { get } = useApi()
    try {
      const list = await get<Banner[]>('/api/banners')
      return (list || []).filter((b) => b.isActive !== 0)
    } catch {
      return []
    }
  },
  { default: () => [] }
)

const banner = computed<Banner | null>(() => banners.value?.[0] || null)

const title = computed(() => banner.value?.title || settings.value.bannerTitle || settings.value.siteTitle || '我的博客')
const typingEnabled = computed(() => {
  const flag = banner.value?.typingEffect ?? settings.value.bannerTypingEffect
  return String(flag ?? '1') === '1'
})
const rawSubtitle = computed(() => banner.value?.subtitle || settings.value.bannerSubtitle || settings.value.siteSubtitle || '记录生活，分享技术')
const { output } = useTypewriter(() => (typingEnabled.value ? rawSubtitle.value : ''), { speed: 130 })
const subtitle = computed(() => (typingEnabled.value ? output.value : rawSubtitle.value))

/** 仅当明确配置了自定义图片时使用图片模式；bing/无图走沉浸式 */
const imageUrl = computed(() => {
  const custom = banner.value?.imageUrl || settings.value.bannerImage || ''
  const source = banner.value ? 'custom' : (settings.value.bannerSource || 'custom')
  return source === 'custom' && custom ? custom : ''
})
</script>

<template>
  <section class="banner" :class="{ 'with-image': imageUrl }" :style="imageUrl ? { backgroundImage: `linear-gradient(180deg, rgba(10,14,25,0.3), rgba(10,14,25,0.45)), url(${imageUrl})` } : {}">
    <!-- 沉浸式模式的光晕装饰 -->
    <div v-if="!imageUrl" class="banner-glow" aria-hidden="true"></div>
    <div class="banner-content container">
      <h1 class="banner-title" :class="{ gradient: !imageUrl }">{{ title }}</h1>
      <p class="banner-subtitle">
        {{ subtitle }}<span class="type-cursor">|</span>
      </p>
    </div>
    <div class="banner-fade" aria-hidden="true"></div>
  </section>
</template>

<style scoped>
.banner {
  position: relative;
  height: 52vh;
  min-height: 360px;
  max-height: 620px;
  background-size: cover;
  background-position: center;
  background-color: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-1);
  text-align: center;
  overflow: hidden;
}
.banner.with-image { color: #fff; }

/* 沉浸式：主色光晕自上方洒下，与星空背景融合 */
.banner-glow {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(ellipse 60% 45% at 50% 8%, var(--primary-glow), transparent 70%),
    radial-gradient(ellipse 35% 30% at 78% 22%, rgba(45, 206, 137, 0.08), transparent 70%);
}

.banner-content { position: relative; width: 100%; }
.banner-title {
  margin: 0 0 16px;
  font-size: clamp(34px, 6vw, 58px);
  font-weight: 800;
  letter-spacing: 0.04em;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
}
.banner-title.gradient {
  background: linear-gradient(120deg, var(--primary) 20%, var(--primary-light) 55%, var(--primary-dark) 90%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
html.dark .banner-title.gradient {
  background: linear-gradient(120deg, #aebcf7 10%, var(--primary-light) 50%, #7e9bff 90%);
  -webkit-background-clip: text;
  background-clip: text;
}
.banner-subtitle {
  margin: 0;
  font-size: clamp(14px, 2vw, 18px);
  letter-spacing: 0.06em;
  color: var(--text-2);
  min-height: 1.6em;
}
.banner.with-image .banner-subtitle { color: rgba(255, 255, 255, 0.92); text-shadow: 0 1px 8px rgba(0, 0, 0, 0.45); }
.banner.with-image .banner-title { text-shadow: 0 2px 16px rgba(0, 0, 0, 0.5); }
.type-cursor {
  display: inline-block;
  margin-left: 2px;
  color: var(--primary);
  animation: blink 1s step-end infinite;
  font-weight: 300;
}
.banner.with-image .type-cursor { color: #fff; }
@keyframes blink { 50% { opacity: 0; } }

/* 底部渐隐，与内容区自然衔接 */
.banner-fade {
  position: absolute;
  left: 0; right: 0; bottom: 0;
  height: 120px;
  pointer-events: none;
  background: linear-gradient(180deg, transparent, var(--bg-page));
  opacity: 0.55;
}
</style>
