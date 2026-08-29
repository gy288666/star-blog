// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-01-01',
  devtools: { enabled: false },

  modules: ['@pinia/nuxt'],

  app: {
    head: {
      htmlAttrs: { lang: 'zh-CN' },
      meta: [
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
        { name: 'renderer', content: 'webkit' }
      ]
    }
  },

  css: [
    '~/assets/css/main.css',
    'katex/dist/katex.min.css'
  ],

  runtimeConfig: {
    // SSR 内部 /api 代理目标（server/api/[[...path]].ts），
    // 生产容器用 NUXT_API_ORIGIN=http://backend:8080 覆盖
    apiOrigin: 'http://localhost:8080'
  },

  nitro: {
    // 开发环境把 /uploads 静态资源反代到本地后端
    // （/api 走 server/api/[[...path]].ts 服务端代理，同时覆盖 SSR 内部 fetch）
    devProxy: {
      '/uploads': { target: 'http://localhost:8080/uploads', changeOrigin: true }
    }
  },

  routeRules: {
    // 管理后台纯客户端 SPA
    '/admin/**': { ssr: false }
  }
})
