<script setup lang="ts">
// 顶部导航：毛玻璃吸顶 + 移动端抽屉 + 暗色切换 + 搜索入口
const { siteTitle } = useSettings()
const theme = useTheme()
const route = useRoute()

const navItems = [
  { path: '/', label: '首页', icon: '🏠' },
  { path: '/archives', label: '归档', icon: '📅' },
  { path: '/shuoshuo', label: '说说', icon: '💬' },
  { path: '/friend', label: '友链', icon: '🔗' },
  { path: '/page/about', label: '关于', icon: '🌟' }
]

const scrolled = ref(false)
const drawerOpen = ref(false)
const searchOpen = ref(false)

const onScroll = () => { scrolled.value = window.scrollY > 10 }
onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
  onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))
})

const isActive = (path: string) => {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

// 全局 Ctrl+K 呼出搜索
const onKeydown = (e: KeyboardEvent) => {
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
    e.preventDefault()
    searchOpen.value = true
  }
}
onMounted(() => {
  window.addEventListener('keydown', onKeydown)
  onBeforeUnmount(() => window.removeEventListener('keydown', onKeydown))
})

watch(() => route.fullPath, () => { drawerOpen.value = false })

const cycleTheme = () => {
  const order: ThemeMode[] = ['light', 'dark', 'auto']
  const next = order[(order.indexOf(theme.mode.value) + 1) % order.length]
  theme.setMode(next)
}
const themeIcon = computed(() => theme.mode.value === 'dark' ? '🌙' : theme.mode.value === 'light' ? '☀️' : '🌗')

// 粒子背景开关（与 ParticleBackground 通过 useState 共享）
const particlesEnabled = useState('particles-enabled', () => true)
const toggleParticles = () => {
  particlesEnabled.value = !particlesEnabled.value
  localStorage.setItem('blog_particles_off', particlesEnabled.value ? '0' : '1')
}
</script>

<template>
  <header class="site-header" :class="{ scrolled }">
    <div class="header-inner container">
      <NuxtLink to="/" class="site-brand">
        <span class="brand-dot"></span>
        {{ siteTitle }}
      </NuxtLink>

      <nav class="desktop-nav">
        <NuxtLink v-for="item in navItems" :key="item.path" :to="item.path"
          class="nav-link" :class="{ active: isActive(item.path) }">
          <span class="nav-icon">{{ item.icon }}</span>
          <span class="nav-label">{{ item.label }}</span>
        </NuxtLink>
      </nav>

      <div class="header-actions">
        <button class="icon-btn" title="搜索 (Ctrl+K)" @click="searchOpen = true">🔍</button>
        <button class="icon-btn" :title="`主题：${theme.mode.value}`" @click="cycleTheme">{{ themeIcon }}</button>
        <button class="icon-btn" :class="{ off: !particlesEnabled }" :title="particlesEnabled ? '关闭粒子背景' : '开启粒子背景'" @click="toggleParticles">✨</button>
        <button class="icon-btn menu-toggle" @click="drawerOpen = !drawerOpen">☰</button>
      </div>
    </div>

    <!-- 移动端抽屉菜单 -->
    <Transition name="fade">
      <nav v-if="drawerOpen" class="mobile-drawer">
        <NuxtLink v-for="item in navItems" :key="item.path" :to="item.path"
          class="drawer-link" :class="{ active: isActive(item.path) }">
          <span class="nav-icon">{{ item.icon }}</span> {{ item.label }}
        </NuxtLink>
      </nav>
    </Transition>

    <CommonSearchModal v-model:open="searchOpen" />
  </header>
</template>

<style scoped>
.site-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--bg-card);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border-bottom: 1px solid var(--divider);
  transition: box-shadow 0.25s, background-color 0.3s;
}
.site-header.scrolled { box-shadow: var(--shadow-nav); }
.header-inner {
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.site-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-1);
}
.brand-dot {
  width: 10px; height: 10px;
  border-radius: 50%;
  background: var(--primary);
  box-shadow: 0 0 8px var(--primary);
}
.desktop-nav { display: flex; gap: 4px; }
.nav-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 14px;
  border-radius: 999px;
  color: var(--text-2);
  font-size: 14.5px;
  transition: all 0.2s;
}
.nav-icon { font-size: 14px; line-height: 1; transition: transform 0.25s ease; }
.nav-link:hover .nav-icon { transform: scale(1.25) rotate(-10deg); }
.nav-link:hover { color: var(--primary); background: var(--bg-hover); }
.nav-link.active { color: var(--primary); background: var(--bg-hover); font-weight: 600; }
.header-actions { display: flex; align-items: center; gap: 4px; }
.icon-btn {
  width: 36px; height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
  color: var(--text-2);
  transition: background 0.2s, color 0.2s;
}
.icon-btn:hover { background: var(--bg-hover); color: var(--primary); }
.icon-btn.off { opacity: 0.4; }
.menu-toggle { display: none; }
.mobile-drawer {
  display: none;
  flex-direction: column;
  padding: 8px 16px 14px;
  border-top: 1px solid var(--divider);
}
.drawer-link {
  padding: 10px 14px;
  border-radius: 8px;
  color: var(--text-2);
}
.drawer-link.active { background: var(--bg-hover); color: var(--primary); }
@media (max-width: 768px) {
  .desktop-nav { display: none; }
  .menu-toggle { display: inline-flex; }
  .mobile-drawer { display: flex; }
}
</style>
