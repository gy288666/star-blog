// 管理后台全局路由守卫：未登录跳转登录页
// 后台为 ssr:false，仅在客户端执行
export default defineNuxtRouteMiddleware((to) => {
  if (!import.meta.client) return
  if (!to.path.startsWith('/admin') || to.path === '/admin/login') return

  const auth = useAuthStore()
  auth.init()
  if (!auth.isLoggedIn) {
    return navigateTo('/admin/login', { replace: true })
  }
})
