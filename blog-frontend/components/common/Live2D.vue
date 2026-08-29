<script setup lang="ts">
/**
 * Live2D 看板娘 Doro（右下角浮窗）
 * - Cubism 4 模型 /live2d/Doro/Doro.model3.json，加载失败静默隐藏
 * - 可拖拽（位置存 localStorage）
 * - 点击 Doro 冒气泡：预设问候语 / 大模型回复（角色扮演 Doro）
 * - 聊天：气泡下方输入框，走后端 /api/live2d/chat（服务端代理，key 不出网）
 * - 收起开关，状态存 localStorage
 */
const MODEL_URL = '/live2d/Doro/Doro.model3.json'
const CORE_CDN = 'https://cubism.live2d.com/sdk-web/cubismcore/live2dcubismcore.min.js'
const POS_KEY = 'blog_live2d_pos'

const GREETINGS = [
  '汪！我是 Doro，欢迎回来～',
  '(๑>ᴗ<๑) 今天也要元气满满哦！',
  '偷橘子去了…啊，你看到我了！',
  '点我可以聊天哦，汪汪！',
  '(´･ω･`) 一个人看博客好无聊，陪陪我嘛',
  '٩(ˊωˋ*)و 摸摸头…汪！'
]

const containerRef = ref<HTMLCanvasElement | null>(null)
const widgetRef = ref<HTMLDivElement | null>(null)
const visible = ref(false)      // 模型加载成功后才显示
const collapsed = ref(false)
const pos = reactive({ x: -1, y: -1 })   // -1 表示默认右下角

// 气泡与聊天状态
const bubble = ref('')
const bubbleMode = ref<'say' | 'chat'>('say')
const chatting = ref(false)
const inputValue = ref('')
const history = ref<{ role: string; content: string }[]>([])

let destroyed = false
let app: any = null
let bubbleTimer = 0
let typingTimer = 0

onMounted(() => {
  collapsed.value = localStorage.getItem('blog_live2d_collapsed') === '1'
  try {
    const saved = JSON.parse(localStorage.getItem(POS_KEY) || 'null')
    if (saved && typeof saved.x === 'number') { pos.x = saved.x; pos.y = saved.y }
  } catch {}
  if (collapsed.value) return
  loadModel()
})

onBeforeUnmount(destroyAll)

function destroyAll() {
  destroyed = true
  cancelAnimationFrame(typingTimer)
  clearTimeout(bubbleTimer)
  try { app?.destroy(false, { children: true }) } catch {}
  app = null
}

function toggleCollapse() {
  collapsed.value = !collapsed.value
  localStorage.setItem('blog_live2d_collapsed', collapsed.value ? '1' : '0')
  if (!collapsed.value && !app) loadModel()
  else if (collapsed.value) destroyAll()
}

/* ---------- 气泡 ---------- */
function showBubble(text: string, mode: 'say' | 'chat' = 'say', holdMs = 5000) {
  bubble.value = ''
  bubbleMode.value = mode
  cancelAnimationFrame(typingTimer)
  clearTimeout(bubbleTimer)
  // 打字机逐字显示
  let i = 0
  const step = () => {
    if (i <= text.length) {
      bubble.value = text.slice(0, i++)
      typingTimer = requestAnimationFrame(step)
    }
  }
  step()
  if (holdMs > 0) {
    bubbleTimer = window.setTimeout(() => {
      if (bubbleMode.value === mode) bubble.value = ''
    }, holdMs + text.length * 45)
  }
}

function randomGreeting() {
  const pool = GREETINGS.filter((g) => g !== bubble.value)
  return pool[Math.floor(Math.random() * pool.length)]
}

/* ---------- 聊天（大模型） ---------- */
const { post } = useApi()
async function sendChat() {
  const msg = inputValue.value.trim()
  if (!msg || chatting.value) return
  inputValue.value = ''
  chatting.value = true
  showBubble('汪…让我想想 (´･ω･`)', 'chat', 0)
  try {
    const r = await post<{ reply: string }>('/api/live2d/chat', {
      message: msg,
      history: history.value.slice(-6)
    })
    const reply = r?.reply || '汪？信号飘走了…'
    history.value.push({ role: 'user', content: msg }, { role: 'assistant', content: reply })
    showBubble(reply, 'chat', Math.max(8000, reply.length * 200))
  } catch {
    showBubble('呜…网络不稳定，汪！', 'chat', 5000)
  } finally {
    chatting.value = false
  }
}

/* ---------- 拖拽 + 点击 ---------- */
let dragState: { startX: number; startY: number; originX: number; originY: number; moved: boolean } | null = null

function onPointerDown(e: PointerEvent) {
  if ((e.target as HTMLElement).closest('.l2d-chat-box')) return
  const rect = widgetRef.value!.getBoundingClientRect()
  dragState = { startX: e.clientX, startY: e.clientY, originX: rect.left, originY: rect.top, moved: false }
  ;(e.currentTarget as HTMLElement).setPointerCapture(e.pointerId)
}
function onPointerMove(e: PointerEvent) {
  if (!dragState) return
  const dx = e.clientX - dragState.startX
  const dy = e.clientY - dragState.startY
  if (!dragState.moved && Math.abs(dx) + Math.abs(dy) < 6) return
  dragState.moved = true
  const w = widgetRef.value!.offsetWidth
  const h = widgetRef.value!.offsetHeight
  pos.x = Math.min(Math.max(0, dragState.originX + dx), window.innerWidth - w)
  pos.y = Math.min(Math.max(0, dragState.originY + dy), window.innerHeight - h)
}
function onPointerUp(e: PointerEvent) {
  if (!dragState) return
  const moved = dragState.moved
  dragState = null
  if (moved) {
    localStorage.setItem(POS_KEY, JSON.stringify({ x: pos.x, y: pos.y }))
  } else if (!(e.target as HTMLElement).closest('.l2d-chat-box')) {
    // 未拖动 = 点击 Doro：冒气泡打招呼
    showBubble(randomGreeting(), 'say', 4500)
  }
}
const widgetStyle = computed(() =>
  pos.x >= 0 ? { left: pos.x + 'px', top: pos.y + 'px', right: 'auto', bottom: 'auto' } : {}
)

/* ---------- 模型加载 ---------- */
function loadScript(src: string, timeout = 8000): Promise<void> {
  return new Promise((resolve, reject) => {
    if (document.querySelector(`script[src="${src}"]`)) return resolve()
    const s = document.createElement('script')
    s.src = src
    s.async = true
    const timer = setTimeout(() => { s.remove(); reject(new Error('timeout')) }, timeout)
    s.onload = () => { clearTimeout(timer); resolve() }
    s.onerror = () => { clearTimeout(timer); s.remove(); reject(new Error('load error')) }
    document.head.appendChild(s)
  })
}

async function loadModel() {
  try {
    await loadScript(CORE_CDN)
    const [{ Application, Ticker }, { Live2DModel }] = await Promise.all([
      import('pixi.js'),
      // @ts-ignore 子路径导出
      import('pixi-live2d-display/cubism4')
    ])
    if (destroyed) return
    ;(window as any).PIXI = { Ticker }

    app = new Application({
      view: containerRef.value!,
      width: 260,
      height: 320,
      backgroundAlpha: 0
    })

    const model = await Live2DModel.from(MODEL_URL, { autoInteract: false })
    if (destroyed) { model.destroy(); return }
    app.stage.addChild(model as any)

    const scale = Math.min(260 / model.width, 320 / model.height) * 0.95
    model.scale.set(scale)
    model.x = (260 - model.width) / 2
    model.y = (320 - model.height) / 2

    visible.value = true
    // 出场打招呼
    setTimeout(() => { if (!destroyed) showBubble(randomGreeting(), 'say', 5000) }, 800)
  } catch {
    destroyAll()
  }
}
</script>

<template>
  <div
    ref="widgetRef"
    class="live2d-widget"
    :class="{ collapsed, ready: visible }"
    :style="widgetStyle"
    @pointerdown="onPointerDown"
    @pointermove="onPointerMove"
    @pointerup="onPointerUp"
  >
    <!-- 气泡（含聊天输入） -->
    <div v-if="bubble" class="l2d-bubble" @pointerdown.stop>
      <span class="bubble-text">{{ bubble }}</span>
      <div class="l2d-chat-box">
        <input
          v-model="inputValue"
          class="chat-input"
          type="text"
          maxlength="200"
          placeholder="和 Doro 说点什么…"
          @keydown.enter="sendChat"
          @pointerdown.stop
        />
        <button class="chat-send" :disabled="chatting || !inputValue.trim()" @click="sendChat">
          {{ chatting ? '…' : '发送' }}
        </button>
      </div>
    </div>

    <button v-if="visible || !collapsed" class="live2d-toggle" title="收起/展开看板娘" @pointerdown.stop @click="toggleCollapse">
      {{ collapsed ? '😺' : '✕' }}
    </button>
    <span v-if="visible && !collapsed" class="l2d-hint">拖我 | 点我聊天</span>
    <canvas v-show="visible && !collapsed" ref="containerRef" class="live2d-canvas"></canvas>
  </div>
</template>

<style scoped>
.live2d-widget {
  position: fixed;
  right: 8px;
  bottom: 0;
  z-index: 90;
  pointer-events: none;
  display: none;
  touch-action: none;
}
.live2d-widget.ready { display: block; }
.live2d-widget:active { cursor: grabbing; }
.live2d-canvas {
  display: block;
  pointer-events: auto;
  cursor: grab;
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.18));
}
.live2d-toggle {
  position: absolute;
  top: 4px;
  right: 4px;
  z-index: 2;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 50%;
  background: var(--bg-card-solid);
  box-shadow: var(--shadow-card);
  color: var(--text-2);
  font-size: 13px;
  cursor: pointer;
  pointer-events: auto;
  display: flex;
  align-items: center;
  justify-content: center;
}
.live2d-toggle:hover { color: var(--primary); }
.l2d-hint {
  position: absolute;
  top: 8px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 10px;
  color: var(--text-3);
  background: var(--bg-card);
  backdrop-filter: blur(8px);
  padding: 2px 8px;
  border-radius: 999px;
  white-space: nowrap;
  pointer-events: none;
  opacity: 0.85;
}

/* 气泡 */
.l2d-bubble {
  position: absolute;
  bottom: calc(100% + 10px);
  right: 0;
  width: 250px;
  background: var(--bg-card-strong);
  backdrop-filter: blur(16px) saturate(1.4);
  border: 1px solid var(--border-color);
  border-radius: 14px 14px 4px 14px;
  box-shadow: var(--shadow-card);
  padding: 12px 14px;
  pointer-events: auto;
  cursor: default;
}
.bubble-text {
  display: block;
  font-size: 13.5px;
  line-height: 1.65;
  color: var(--text-1);
  min-height: 1.4em;
  word-break: break-word;
}
.l2d-chat-box {
  display: flex;
  gap: 6px;
  margin-top: 10px;
}
.chat-input {
  flex: 1;
  min-width: 0;
  padding: 6px 10px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--bg-input);
  color: var(--text-1);
  font-size: 12.5px;
  font-family: inherit;
  outline: none;
}
.chat-input:focus { border-color: var(--primary); }
.chat-send {
  padding: 6px 12px;
  border: none;
  border-radius: 8px;
  background: var(--primary);
  color: #fff;
  font-size: 12.5px;
  cursor: pointer;
  transition: opacity 0.2s;
}
.chat-send:disabled { opacity: 0.5; cursor: not-allowed; }
.chat-send:not(:disabled):hover { opacity: 0.88; }

@media (max-width: 768px) {
  .live2d-widget { display: none !important; }
}
</style>
