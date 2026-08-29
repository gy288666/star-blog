/** 日期格式化工具 */
export const formatDate = (input?: string | null, withTime = true): string => {
  if (!input) return ''
  const d = new Date(input)
  if (isNaN(d.getTime())) return input
  const pad = (n: number) => String(n).padStart(2, '0')
  const date = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
  return withTime ? `${date} ${pad(d.getHours())}:${pad(d.getMinutes())}` : date
}

/** 自动生成 slug：中文取拼音不可行（离线），采用时间戳+随机串；英文/数字保留 */
export const genSlug = (title: string): string => {
  const ascii = title
    .toLowerCase()
    .replace(/[^a-z0-9\s-]/g, '')
    .trim()
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '')
  if (ascii.length >= 3) return ascii
  const ts = Date.now().toString(36)
  const rand = Math.random().toString(36).slice(2, 6)
  return `post-${ts}${rand}`
}
