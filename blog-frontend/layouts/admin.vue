<script setup lang="ts">
// 后台布局：左侧菜单 + 顶栏
const auth = useAuthStore()
auth.init()
const route = useRoute()
const router = useRouter()

const collapse = ref(false)

const activeMenu = computed(() => route.path)

const menuItems = [
  { path: '/admin', label: '仪表盘', icon: '📊' },
  { path: '/admin/posts', label: '文章管理', icon: '📝' },
  { path: '/admin/comments', label: '评论管理', icon: '💬' },
  { path: '/admin/shuoshuos', label: '说说管理', icon: '🙋' },
  { path: '/admin/categories', label: '分类管理', icon: '🗂️' },
  { path: '/admin/tags', label: '标签管理', icon: '🏷️' },
  { path: '/admin/friends', label: '友链管理', icon: '🔗' },
  { path: '/admin/banners', label: '横幅管理', icon: '🖼️' },
  { path: '/admin/files', label: '文件管理', icon: '📁' },
  { path: '/admin/settings', label: '站点设置', icon: '⚙️' }
]

const handleLogout = () => {
  auth.logout()
  router.push('/admin/login')
}
</script>

<template>
  <div class="admin-layout">
    <aside class="admin-aside" :class="{ collapsed: collapse }">
      <div class="aside-brand">
        <span v-if="!collapse">博客管理</span>
        <span v-else>管</span>
      </div>
      <nav class="aside-menu">
        <NuxtLink
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="menu-item"
          :class="{ active: activeMenu === item.path || (item.path !== '/admin' && activeMenu.startsWith(item.path)) }"
        >
          <span class="menu-icon">{{ item.icon }}</span>
          <span v-if="!collapse" class="menu-label">{{ item.label }}</span>
        </NuxtLink>
      </nav>
    </aside>

    <div class="admin-body">
      <header class="admin-topbar">
        <button class="topbar-btn" @click="collapse = !collapse">☰</button>
        <div class="topbar-right">
          <NuxtLink to="/" target="_blank" class="topbar-btn" title="访问前台">🌐</NuxtLink>
          <span class="topbar-user">{{ auth.user?.nickname || auth.user?.username || '管理员' }}</span>
          <button class="topbar-btn" @click="handleLogout">退出</button>
        </div>
      </header>
      <main class="admin-main">
        <slot />
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg-page);
}
.admin-aside {
  width: 210px;
  background: var(--bg-card-solid);
  border-right: 1px solid var(--divider);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: width 0.2s;
  overflow: hidden;
}
.admin-aside.collapsed { width: 64px; }
.aside-brand {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  color: var(--primary);
  border-bottom: 1px solid var(--divider);
}
.aside-menu {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}
.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  margin: 2px 0;
  border-radius: 8px;
  color: var(--text-2);
  white-space: nowrap;
  transition: background 0.2s, color 0.2s;
}
.menu-item:hover { background: var(--bg-hover); color: var(--primary); }
.menu-item.active { background: var(--primary); color: #fff; }
.admin-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.admin-topbar {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: var(--bg-card);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--divider);
  position: sticky;
  top: 0;
  z-index: 10;
}
.topbar-right { display: flex; align-items: center; gap: 14px; }
.topbar-user { color: var(--text-2); font-size: 14px; }
.topbar-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  color: var(--text-2);
  padding: 6px;
  border-radius: 6px;
}
.topbar-btn:hover { background: var(--bg-hover); color: var(--primary); }
.admin-main {
  flex: 1;
  padding: 20px;
}
@media (max-width: 768px) {
  .admin-aside { position: fixed; z-index: 100; height: 100vh; }
}
</style>
