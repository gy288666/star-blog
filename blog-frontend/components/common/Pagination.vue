<script setup lang="ts">
// 通用分页组件
const props = withDefaults(defineProps<{
  page: number
  total: number
  size?: number
}>(), { size: 10 })

const emit = defineEmits<{ (e: 'change', page: number): void }>()

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.size)))

/** 生成页码列表，超出部分用省略号 */
const pages = computed<(number | string)[]>(() => {
  const t = totalPages.value
  const cur = props.page
  if (t <= 7) return Array.from({ length: t }, (_, i) => i + 1)
  const list: (number | string)[] = [1]
  if (cur > 3) list.push('…')
  for (let i = Math.max(2, cur - 1); i <= Math.min(t - 1, cur + 1); i++) list.push(i)
  if (cur < t - 2) list.push('…')
  list.push(t)
  return list
})

const go = (p: number | string) => {
  if (typeof p !== 'number' || p === props.page || p < 1 || p > totalPages.value) return
  emit('change', p)
}
</script>

<template>
  <nav v-if="totalPages > 1" class="pagination" aria-label="分页">
    <button class="page-btn" :disabled="page <= 1" @click="go(page - 1)">‹</button>
    <template v-for="(p, i) in pages" :key="i">
      <span v-if="p === '…'" class="page-ellipsis">…</span>
      <button v-else class="page-btn" :class="{ current: p === page }" @click="go(p)">{{ p }}</button>
    </template>
    <button class="page-btn" :disabled="page >= totalPages" @click="go(page + 1)">›</button>
  </nav>
</template>

<style scoped>
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  margin: 28px 0 8px;
  flex-wrap: wrap;
}
.page-btn {
  min-width: 36px;
  height: 36px;
  padding: 0 10px;
  border: none;
  border-radius: 10px;
  background: var(--bg-card);
  box-shadow: var(--shadow-card);
  color: var(--text-2);
  font-family: inherit;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.page-btn:hover:not(:disabled) { color: var(--primary); background: var(--bg-hover); }
.page-btn.current { background: var(--primary); color: #fff; }
.page-btn:disabled { opacity: 0.45; cursor: not-allowed; }
.page-ellipsis { color: var(--text-3); padding: 0 2px; }
</style>
