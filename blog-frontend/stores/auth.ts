import { defineStore } from 'pinia'
import type { AdminUser } from '~/types'

/** 管理端登录态（token 持久化到 localStorage） */
export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '' as string,
    user: null as AdminUser | null,
    _initialized: false
  }),
  getters: {
    isLoggedIn: (s) => !!s.token
  },
  actions: {
    /** 客户端启动时从 localStorage 恢复 */
    init() {
      if (this._initialized || !import.meta.client) return
      this._initialized = true
      try {
        const token = localStorage.getItem('blog_admin_token')
        const user = localStorage.getItem('blog_admin_user')
        if (token) this.token = token
        if (user) this.user = JSON.parse(user)
      } catch { /* ignore */ }
    },
    setLogin(token: string, user: AdminUser) {
      this.token = token
      this.user = user
      if (import.meta.client) {
        localStorage.setItem('blog_admin_token', token)
        localStorage.setItem('blog_admin_user', JSON.stringify(user))
      }
    },
    logout() {
      this.token = ''
      this.user = null
      if (import.meta.client) {
        localStorage.removeItem('blog_admin_token')
        localStorage.removeItem('blog_admin_user')
      }
    }
  }
})
