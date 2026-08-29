import type { ApiResponse } from '~/types'

/**
 * 统一 API 请求封装：
 * - 所有响应 {code,msg,data}，code=0 才算成功
 * - 管理端自动携带 Bearer Token，401 时跳转登录
 */
export const useApi = () => {
  const auth = useAuthStore()

  const request = async <T>(url: string, opts: Record<string, any> = {}): Promise<T> => {
    const headers: Record<string, string> = { ...(opts.headers || {}) }
    if (auth.token) headers.Authorization = `Bearer ${auth.token}`
    try {
      const res = await $fetch<ApiResponse<T>>(url, { ...opts, headers })
      if (res.code !== 0) {
        if (res.code === 401 && auth.token) {
          // 登录态失效
          auth.logout()
          if (import.meta.client && url.startsWith('/api/admin') && !location.pathname.startsWith('/admin/login')) {
            navigateTo('/admin/login')
          }
        }
        throw createApiError(res.code, res.msg || '请求失败')
      }
      return res.data
    } catch (e: any) {
      // $fetch 对非 2xx 会抛 FetchError，但后端始终返回 200，这里兜底网络错误
      if (e && typeof e.statusCode === 'number' && e.data && typeof e.data === 'object' && 'code' in e.data) {
        throw createApiError(e.data.code, e.data.msg || '请求失败')
      }
      throw e
    }
  }

  const get = <T>(url: string, params?: Record<string, any>) =>
    request<T>(url, { method: 'GET', params })

  const post = <T>(url: string, body?: any) =>
    request<T>(url, { method: 'POST', body })

  const put = <T>(url: string, body?: any) =>
    request<T>(url, { method: 'PUT', body })

  const del = <T>(url: string, params?: Record<string, any>) =>
    request<T>(url, { method: 'DELETE', params })

  /** 上传文件（multipart） */
  const upload = <T>(url: string, file: File) => {
    const fd = new FormData()
    fd.append('file', file)
    return request<T>(url, { method: 'POST', body: fd })
  }

  return { request, get, post, put, del, upload }
}

function createApiError(code: number, msg: string): Error & { code: number } {
  const err = new Error(msg) as Error & { code: number }
  err.code = code
  return err
}

/** 分页参数便捷拼接 */
export const buildPageParams = (page: number, size: number, extra?: Record<string, any>) => ({
  page,
  size,
  ...(extra || {})
})
