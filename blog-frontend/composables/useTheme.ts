/**
 * 主题管理：暗色模式三态（light / dark / auto）+ 主题色切换
 * - localStorage 持久化
 * - darkModeDefault（来自公开设置）作为未手动设置时的默认值
 */
export type ThemeMode = 'light' | 'dark' | 'auto'

const MODE_KEY = 'blog_theme_mode'
const COLOR_KEY = 'blog_theme_color'

export const THEME_COLORS: { name: string; value: string }[] = [
  { name: '默认蓝紫', value: '#5e72e4' },
  { name: '青绿', value: '#2dce89' },
  { name: '橙红', value: '#fb6340' },
  { name: '天蓝', value: '#11cdef' },
  { name: '粉紫', value: '#f75676' }
]

const systemDark = () =>
  import.meta.client && window.matchMedia('(prefers-color-scheme: dark)').matches

function applyDark(isDark: boolean) {
  if (!import.meta.client) return
  document.documentElement.classList.toggle('dark', isDark)
  const meta = document.querySelector('meta[name="theme-color"]')
  if (meta) meta.setAttribute('content', isDark ? '#1c2029' : '#f6f7fb')
}

function applyPrimary(color: string) {
  if (!import.meta.client) return
  const root = document.documentElement.style
  root.setProperty('--primary', color)
  // 简单的亮/暗变体
  root.setProperty('--primary-light', shade(color, 24))
  root.setProperty('--primary-dark', shade(color, -24))
}

/** 颜色明暗调整：amount > 0 变亮，< 0 变暗 */
function shade(hex: string, amount: number): string {
  const m = hex.replace('#', '')
  const num = parseInt(m.length === 3 ? m.split('').map((c) => c + c).join('') : m, 16)
  const clamp = (v: number) => Math.min(255, Math.max(0, v))
  const r = clamp((num >> 16) + amount)
  const g = clamp(((num >> 8) & 0xff) + amount)
  const b = clamp((num & 0xff) + amount)
  return `#${((r << 16) | (g << 8) | b).toString(16).padStart(6, '0')}`
}

export const useTheme = () => {
  const mode = ref<ThemeMode>('auto')
  const color = ref('#5e72e4')

  const isDark = computed(() =>
    mode.value === 'dark' || (mode.value === 'auto' && systemDark())
  )

  const apply = () => {
    applyDark(isDark.value)
    applyPrimary(color.value)
  }

  const setMode = (m: ThemeMode) => {
    mode.value = m
    localStorage.setItem(MODE_KEY, m)
    apply()
  }

  const setColor = (c: string) => {
    color.value = c
    localStorage.setItem(COLOR_KEY, c)
    apply()
  }

  /** 初始化：读取本地设置，其次取公开设置的默认暗色 */
  const init = (darkModeDefault?: string) => {
    if (!import.meta.client) return
    const savedMode = localStorage.getItem(MODE_KEY) as ThemeMode | null
    const savedColor = localStorage.getItem(COLOR_KEY)
    if (savedMode) {
      mode.value = savedMode
    } else if (darkModeDefault) {
      mode.value = darkModeDefault === '1' ? 'dark' : darkModeDefault === '0' ? 'light' : 'auto'
    }
    if (savedColor) color.value = savedColor
    apply()

    // 跟随系统：监听系统配色变化
    const mq = window.matchMedia('(prefers-color-scheme: dark)')
    mq.addEventListener('change', () => {
      if (mode.value === 'auto') apply()
    })
  }

  return { mode, color, isDark, setMode, setColor, init }
}
