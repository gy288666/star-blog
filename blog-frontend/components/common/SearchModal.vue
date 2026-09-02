<script setup lang="ts">
// 搜索弹窗：Ctrl+K 呼出，回车跳转搜索页
const props = defineProps<{ open: boolean }>()
const emit = defineEmits<{ (e: 'update:open', v: boolean): void }>()

const router = useRouter()
const keyword = ref('')
const inputRef = ref<HTMLInputElement | null>(null)

const close = () => emit('update:open', false)

const search = () => {
  const q = keyword.value.trim()
  if (!q) return
  close()
  router.push({ path: '/search', query: { q } })
}

watch(() => props.open, (v) => {
  if (v) {
    keyword.value = ''
    nextTick(() => inputRef.value?.focus())
  }
})

const onEsc = (e: KeyboardEvent) => {
  if (e.key === 'Escape') close()
}
onMounted(() => window.addEventListener('keydown', onEsc))
onBeforeUnmount(() => window.removeEventListener('keydown', onEsc))
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="open" class="search-mask" @click.self="close">
        <div class="search-modal card">
          <div class="search-input-row">
            <span class="search-icon"><UiIcon name="search" :size="16" /></span>
            <input
              ref="inputRef"
              v-model="keyword"
              class="search-input"
              type="text"
              placeholder="搜索文章…（回车搜索）"
              @keydown.enter="search"
            />
            <kbd class="search-kbd">Esc</kbd>
          </div>
          <p class="search-hint">输入关键词后按回车跳转到搜索结果页</p>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.search-mask {
  position: fixed;
  inset: 0;
  z-index: 200;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 15vh 16px 0;
}
.search-modal {
  width: 100%;
  max-width: 560px;
  padding: 18px;
}
.search-input-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.search-icon { font-size: 16px; }
.search-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 17px;
  color: var(--text-1);
  font-family: inherit;
}
.search-kbd {
  font-size: 11px;
  color: var(--text-3);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 2px 6px;
}
.search-hint {
  margin: 12px 0 0;
  font-size: 12.5px;
  color: var(--text-3);
}
</style>
