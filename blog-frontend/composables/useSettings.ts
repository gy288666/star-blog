import type { PublicSettings } from '~/types'

/**
 * 公开站点设置（SSR 期间请求一次，全局共享）
 * 接口不可达时返回空对象降级，页面使用默认值
 */
export const useSettings = () => {
  const settings = useState<PublicSettings>('public-settings', () => ({}))
  const loaded = useState<boolean>('public-settings-loaded', () => false)

  const { get } = useApi()

  const load = async () => {
    if (loaded.value) return
    try {
      const data = await get<PublicSettings>('/api/settings/public')
      settings.value = data || {}
    } catch {
      settings.value = {}
    } finally {
      loaded.value = true
    }
  }

  if (!loaded.value) {
    // useAsyncData 包裹保证 SSR 只请求一次
    useAsyncData('public-settings', load)
  }

  const siteTitle = computed(() => settings.value.siteTitle || '我的博客')
  const siteSubtitle = computed(() => settings.value.siteSubtitle || '')
  const footerText = computed(() => settings.value.footerText || '')
  const icpText = computed(() => settings.value.icpText || '')
  const allowComment = computed(() => settings.value.allowComment !== '0')

  return { settings, load, siteTitle, siteSubtitle, footerText, icpText, allowComment }
}
