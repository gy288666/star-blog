import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js/lib/common'
import katexPlugin from '@traptitech/markdown-it-katex'

let md: MarkdownIt | null = null

function createMarkdown(): MarkdownIt {
  const instance = new MarkdownIt({
    html: true,
    linkify: true,
    breaks: true,
    highlight(code: string, lang: string): string {
      // highlight.js 高亮，未知语言或出错时转义输出
      if (lang && hljs.getLanguage(lang)) {
        try {
          return `<pre class="hljs"><code>${hljs.highlight(code, { language: lang, ignoreIllegals: true }).value}</code></pre>`
        } catch { /* fallthrough */ }
      }
      return `<pre class="hljs"><code>${instance.utils.escapeHtml(code)}</code></pre>`
    }
  })

  // KaTeX 数学公式 $...$ / $$...$$
  instance.use(katexPlugin)

  // 外链新窗口打开 + nofollow
  const defaultLinkOpen = instance.renderer.rules.link_open || ((tokens, idx, options, env, self) => self.renderToken(tokens, idx, options))
  instance.renderer.rules.link_open = (tokens, idx, options, env, self) => {
    const token = tokens[idx]
    const href = token.attrGet('href') || ''
    if (/^https?:\/\//.test(href) && !href.startsWith('/')) {
      token.attrSet('target', '_blank')
      token.attrSet('rel', 'nofollow noopener noreferrer')
    }
    return defaultLinkOpen(tokens, idx, options, env, self)
  }

  // 图片懒加载
  const defaultImage = instance.renderer.rules.image || ((tokens, idx, options, env, self) => self.renderToken(tokens, idx, options))
  instance.renderer.rules.image = (tokens, idx, options, env, self) => {
    tokens[idx].attrSet('loading', 'lazy')
    return defaultImage(tokens, idx, options, env, self)
  }

  return instance
}

/**
 * markdown 渲染统一入口（单例）。
 * 服务端与客户端使用相同配置，保证 SSR 一致。
 */
export const useMarkdown = () => {
  if (!md) md = createMarkdown()

  const render = (src: string | null | undefined): string => {
    if (!src) return ''
    return md!.render(src)
  }

  /** 渲染为纯文本（用于摘要） */
  const renderText = (src: string | null | undefined, length = 120): string => {
    if (!src) return ''
    const html = md!.render(src)
    const text = html.replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim()
    return text.length > length ? text.slice(0, length) + '…' : text
  }

  return { render, renderText }
}
