<script setup lang="ts">
// 文章目录 TOC：从正文 DOM 提取 h2/h3 生成，滚动高亮
const props = defineProps<{ containerId: string }>()

interface TocItem { id: string; text: string; level: number }

const items = ref<TocItem[]>([])
const activeId = ref('')

const buildToc = () => {
  if (!import.meta.client) return
  const container = document.getElementById(props.containerId)
  if (!container) return
  const headings = container.querySelectorAll('h2, h3')
  const list: TocItem[] = []
  headings.forEach((el, i) => {
    if (!el.id) el.id = `heading-${i}`
    list.push({ id: el.id, text: el.textContent?.trim() || '', level: Number(el.tagName.slice(1)) })
  })
  items.value = list
}

const scrollTo = (id: string) => {
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

let observer: IntersectionObserver | null = null

onMounted(async () => {
  // 等正文渲染完成
  await nextTick()
  buildToc()
  if (!items.value.length) return
  observer = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) activeId.value = entry.target.id
      }
    },
    { rootMargin: '-80px 0px -70% 0px' }
  )
  items.value.forEach((it) => {
    const el = document.getElementById(it.id)
    if (el) observer!.observe(el)
  })
})

onBeforeUnmount(() => observer?.disconnect())
</script>

<template>
  <nav v-if="items.length" class="toc card" aria-label="目录">
    <p class="toc-title">目录</p>
    <ul class="toc-list">
      <li v-for="item in items" :key="item.id">
        <button
          class="toc-link"
          :class="[`level-${item.level}`, { active: activeId === item.id }]"
          @click="scrollTo(item.id)"
        >{{ item.text }}</button>
      </li>
    </ul>
  </nav>
</template>

<style scoped>
.toc {
  padding: 16px;
  font-size: 13px;
  max-height: 60vh;
  overflow-y: auto;
}
.toc-title {
  margin: 0 0 8px;
  font-weight: 700;
  color: var(--text-1);
}
.toc-list { list-style: none; margin: 0; padding: 0; }
.toc-link {
  display: block;
  width: 100%;
  text-align: left;
  padding: 5px 8px;
  border: none;
  background: none;
  border-radius: 6px;
  color: var(--text-2);
  cursor: pointer;
  font-size: 13px;
  font-family: inherit;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: all 0.15s;
}
.toc-link.level-3 { padding-left: 22px; }
.toc-link:hover { background: var(--bg-hover); color: var(--primary); }
.toc-link.active { color: var(--primary); background: var(--bg-hover); font-weight: 600; }
</style>
