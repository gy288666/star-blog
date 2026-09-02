<script setup lang="ts">
/**
 * 统一线性图标集（自绘 SVG，stroke 1.6 / viewBox 24）
 * 替代 emoji 图标，保证全站视觉语言一致
 */
const props = withDefaults(defineProps<{
  name: string
  size?: number
}>(), { size: 16 })

const PATHS: Record<string, string> = {
  // 导航
  home: 'M3 10.5 12 3l9 7.5M5 9.5V21h5v-6h4v6h5V9.5',
  calendar: 'M4 5h16v16H4zM4 9h16M8 2v4M16 2v4',
  chat: 'M21 12a8 8 0 0 1-8 8H4l1.6-3.2A8 8 0 1 1 21 12z',
  link: 'M10 14a5 5 0 0 0 7.1 0l2.4-2.4a5 5 0 0 0-7.1-7.1L11 5.9M14 10a5 5 0 0 0-7.1 0l-2.4 2.4a5 5 0 0 0 7.1 7.1L13 18.1',
  star: 'm12 3 2.7 5.9 6.3.7-4.7 4.3 1.3 6.1L12 16.9 6.4 20l1.3-6.1L3 9.6l6.3-.7z',
  user: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM4 21c0-4 3.6-6 8-6s8 2 8 6',
  // 功能按钮
  search: 'M21 21l-4.8-4.8M17 10.5a6.5 6.5 0 1 1-13 0 6.5 6.5 0 0 1 13 0z',
  sun: 'M12 17a5 5 0 1 0 0-10 5 5 0 0 0 0 10zM12 1v3M12 20v3M4.2 4.2l2.1 2.1M17.7 17.7l2.1 2.1M1 12h3M20 12h3M4.2 19.8l2.1-2.1M17.7 6.3l2.1-2.1',
  moon: 'M20.6 14.5A8.5 8.5 0 0 1 9.5 3.4 8.5 8.5 0 1 0 20.6 14.5z',
  sparkles: 'M12 4l1.6 4.4L18 10l-4.4 1.6L12 16l-1.6-4.4L6 10l4.4-1.6zM19 15l.8 2.2L22 18l-2.2.8L19 21l-.8-2.2L16 18l2.2-.8zM5 16l.8 2.2L8 19l-2.2.8L5 22l-.8-2.2L2 19l2.2-.8z',
  // 写作/管理
  pen: 'M17 3l4 4L8 20l-5 1 1-5zM15 5l4 4',
  gear: 'M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6zm7.4-3a7.4 7.4 0 0 0-.1-1.2l2-1.5-2-3.4-2.3 1a7.4 7.4 0 0 0-2-1.2L14.6 3H9.4l-.4 2.7a7.4 7.4 0 0 0-2 1.2l-2.3-1-2 3.4 2 1.5a7.4 7.4 0 0 0 0 2.4l-2 1.5 2 3.4 2.3-1a7.4 7.4 0 0 0 2 1.2l.4 2.7h5.2l.4-2.7a7.4 7.4 0 0 0 2-1.2l2.3 1 2-3.4-2-1.5c.1-.4.1-.8.1-1.2z',
  grid: 'M4 4h7v7H4zM13 4h7v7h-7zM4 13h7v7H4zM13 13h7v7h-7z',
  folder: 'M3 6a1 1 0 0 1 1-1h5l2 3h9a1 1 0 0 1 1 1v11a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1z',
  image: 'M3 5h18v14H3zM8.5 11a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3zM21 15l-5-5-6 6-2.5-2.5L3 18',
  file: 'M6 2h8l5 5v15H6zM14 2v5h5',
  logout: 'M9 21H4V3h5M16 17l5-5-5-5M21 12H9',
  eye: 'M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7zm10 3a3 3 0 1 0 0-6 3 3 0 0 0 0 6z',
  heart: 'M12 21S3 14.7 3 8.9C3 5.6 5.4 3.5 8 3.5c1.7 0 3.2.9 4 2.3.8-1.4 2.3-2.3 4-2.3 2.6 0 5 2.1 5 5.4C21 14.7 12 21 12 21z',
  clock: 'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20zM12 6v6l4 2',
  send: 'M22 2 11 13M22 2l-7 20-4-9-9-4z',
  plus: 'M12 5v14M5 12h14',
  close: 'M6 6l12 12M18 6 6 18',
  menu: 'M4 7h16M4 12h16M4 17h16',
  arrowLeft: 'M19 12H5m6-7-7 7 7 7',
  arrowRight: 'M5 12h14m-7-7 7 7-7 7',
  trend: 'M3 17l6-6 4 4 8-8M15 7h6v6',
  fire: 'M12 22c4.4 0 7-2.8 7-6.5 0-3.8-3-5.5-3-9.5-2 1-2.7 2.8-2.7 4.5C11.9 8.6 11 6 12 2 7 5 5 9.5 5 13.5 5 19.2 7.6 22 12 22z',
  doc: 'M6 2h8l5 5v15H6zM14 2v5h5M9 13h6M9 17h6',
  hourglass: 'M6 2h12M6 22h12M7 2c0 8 5 8 5 10s-5 2-5 10M17 2c0 8-5 8-5 10s5 2 5 10',
  refresh: 'M21 4v6h-6M3 20v-6h6M21 10a9 9 0 0 0-15-5.7L3 7M3 14a9 9 0 0 0 15 5.7L21 17',
  upload: 'M12 16V4M6 10l6-6 6 6M4 20h16',
  trash: 'M4 7h16M9 7V4h6v3M6 7l1 14h10l1-14M10 11v6M14 11v6',
  lock: 'M6 11h12v10H6zM8 11V7a4 4 0 0 1 8 0v4',
  pin: 'M12 21s7-6.2 7-11.5A7 7 0 0 0 5 9.5C5 14.8 12 21 12 21zm0-8.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z',
  quote: 'M7 7h4v10H3V11a4 4 0 0 1 4-4zm10 0h4v10h-8V11a4 4 0 0 1 4-4z',
  list: 'M8 6h13M8 12h13M8 18h13M3.5 6h.01M3.5 12h.01M3.5 18h.01',
  contrast: 'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20zm0-18v16'
}
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.6"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
    class="ui-icon"
  >
    <path :d="PATHS[name] || PATHS.doc" />
  </svg>
</template>

<style scoped>
.ui-icon {
  display: inline-block;
  vertical-align: -0.15em;
  flex-shrink: 0;
}
</style>
