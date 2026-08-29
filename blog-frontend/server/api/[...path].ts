// Nitro 服务端代理：/api/** → Spring Boot
// 同时覆盖浏览器请求（同域）与 SSR 内部 $fetch（不经 Nginx）。
// 目标地址 NUXT_API_ORIGIN（默认本地 8080；生产容器为 http://backend:8080）
export default defineEventHandler(async (event) => {
  const origin = useRuntimeConfig(event).apiOrigin as string
  return proxyRequest(event, origin + event.path, {
    headers: { 'X-Forwarded-Host': getRequestHost(event) }
  })
})
