<script setup lang="ts">
/**
 * 粒子星空背景（点/线/光点网络）
 * - 粒子直径 2-6px，透明度阶梯 0.15-0.6
 * - 缓慢漂移（单帧位移 0.03-0.12px，呈 8-15s 级别的流动感）+ 低频噪点抖动
 * - 视距内粒子连线，线透明度随距离衰减（0.1-0.6 阶梯）
 * - Hover 局部聚合：鼠标附近的粒子受轻微引力并提高连线亮度
 * - prefers-reduced-motion 或手动关闭（Header ✨ 按钮 / localStorage）时静止
 * - 页面不可见时暂停 rAF，避免性能负担
 */
const canvasRef = ref<HTMLCanvasElement | null>(null)

const OFF_KEY = 'blog_particles_off'
// 全局共享开关（Header ✨ 按钮与本组件通过 useState 通信）
const enabled = useState('particles-enabled', () => true)

let raf = 0
let particles: P[] = []
let mx = -9999
let my = -9999
let w = 0
let h = 0
let dpr = 1
const ctx = ref<CanvasRenderingContext2D | null>(null)

interface P {
  x: number; y: number
  vx: number; vy: number
  r: number          // 半径 1-3
  a: number          // 基础透明度 0.15-0.6
  phase: number      // 噪点相位
  speed: number      // 噪点频率
}

const reducedMotion = () =>
  import.meta.client && window.matchMedia('(prefers-reduced-motion: reduce)').matches

function isDark() {
  return document.documentElement.classList.contains('dark')
}

/** 粒子颜色随明暗模式：暗色青白光点，亮色靛蓝 */
function dotColor(alpha: number) {
  return isDark() ? `rgba(148, 180, 255, ${alpha})` : `rgba(94, 114, 228, ${alpha})`
}
function lineColor(alpha: number) {
  return isDark() ? `rgba(120, 160, 255, ${alpha})` : `rgba(94, 114, 228, ${alpha})`
}

function resize() {
  const canvas = canvasRef.value
  if (!canvas) return
  dpr = Math.min(window.devicePixelRatio || 1, 2)
  w = window.innerWidth
  h = window.innerHeight
  canvas.width = w * dpr
  canvas.height = h * dpr
  canvas.style.width = w + 'px'
  canvas.style.height = h + 'px'
  ctx.value?.setTransform(dpr, 0, 0, dpr, 0, 0)
  seed()
}

function seed() {
  // 密度随面积自适应，上限 130，小屏减半
  const density = w < 768 ? 22000 : 13000
  const count = Math.min(130, Math.floor((w * h) / density))
  particles = Array.from({ length: count }, () => ({
    x: Math.random() * w,
    y: Math.random() * h,
    vx: (Math.random() - 0.5) * 0.16,
    vy: (Math.random() - 0.5) * 0.16,
    r: 1 + Math.random() * 2,          // 直径 2-6px
    a: 0.15 + Math.random() * 0.45,    // 透明度阶梯 0.15-0.6
    phase: Math.random() * Math.PI * 2,
    speed: 0.0004 + Math.random() * 0.0007  // 8-15s 级别低频
  }))
}

const LINK_DIST = 110
const HOVER_DIST = 150

function tick(now: number) {
  const c = ctx.value
  if (!c) return
  c.clearRect(0, 0, w, h)
  const t = now

  for (const p of particles) {
    // 低频噪点抖动 + 缓慢漂移
    const nx = Math.sin(t * p.speed + p.phase) * 0.12
    const ny = Math.cos(t * p.speed * 0.9 + p.phase) * 0.12
    // hover 聚合：鼠标附近粒子受轻微引力
    const dx = mx - p.x
    const dy = my - p.y
    const d2 = dx * dx + dy * dy
    let ax = 0, ay = 0
    if (d2 < HOVER_DIST * HOVER_DIST && d2 > 1) {
      const d = Math.sqrt(d2)
      const pull = (1 - d / HOVER_DIST) * 0.035
      ax = (dx / d) * pull
      ay = (dy / d) * pull
    }
    p.x += p.vx + nx + ax
    p.y += p.vy + ny + ay
    // 边缘环绕
    if (p.x < -10) p.x = w + 10
    if (p.x > w + 10) p.x = -10
    if (p.y < -10) p.y = h + 10
    if (p.y > h + 10) p.y = -10
  }

  // 连线：视距内透明度随距离衰减
  c.lineWidth = 1
  for (let i = 0; i < particles.length; i++) {
    const a = particles[i]
    for (let j = i + 1; j < particles.length; j++) {
      const b = particles[j]
      const dx = a.x - b.x
      const dy = a.y - b.y
      const d2 = dx * dx + dy * dy
      if (d2 < LINK_DIST * LINK_DIST) {
        const alpha = (1 - Math.sqrt(d2) / LINK_DIST) * 0.32
        c.strokeStyle = lineColor(Math.max(0.1, alpha))
        c.beginPath()
        c.moveTo(a.x, a.y)
        c.lineTo(b.x, b.y)
        c.stroke()
      }
    }
    // 鼠标连线（hover 交互增强）
    const mdx = a.x - mx
    const mdy = a.y - my
    const md2 = mdx * mdx + mdy * mdy
    if (md2 < HOVER_DIST * HOVER_DIST) {
      const alpha = (1 - Math.sqrt(md2) / HOVER_DIST) * 0.5
      c.strokeStyle = lineColor(alpha)
      c.beginPath()
      c.moveTo(a.x, a.y)
      c.lineTo(mx, my)
      c.stroke()
    }
  }

  // 光点
  for (const p of particles) {
    c.beginPath()
    c.arc(p.x, p.y, p.r, 0, Math.PI * 2)
    c.fillStyle = dotColor(p.a)
    c.fill()
    // 较大的粒子加光晕（screen 质感）
    if (p.r > 2.4) {
      c.beginPath()
      c.arc(p.x, p.y, p.r * 2.6, 0, Math.PI * 2)
      c.fillStyle = dotColor(p.a * 0.12)
      c.fill()
    }
  }

  raf = requestAnimationFrame(tick)
}

function start() {
  stop()
  if (!ctx.value) return
  if (reducedMotion()) {
    tick(0)            // 单帧静态
    cancelAnimationFrame(raf)
    return
  }
  raf = requestAnimationFrame(tick)
}
function stop() {
  cancelAnimationFrame(raf)
}

function onPointer(e: PointerEvent) {
  mx = e.clientX
  my = e.clientY
}
function onLeave() {
  mx = -9999
  my = -9999
}
function onVisibility() {
  document.hidden ? stop() : start()
}

onMounted(() => {
  enabled.value = localStorage.getItem(OFF_KEY) !== '1'
  ctx.value = canvasRef.value?.getContext('2d') || null
  resize()
  if (enabled.value) start()
  window.addEventListener('resize', onResize)
  window.addEventListener('pointermove', onPointer, { passive: true })
  document.addEventListener('pointerleave', onLeave)
  document.addEventListener('visibilitychange', onVisibility)
})

// 开关切换：启停动画
watch(enabled, (v) => {
  if (v) { resize(); start() } else stop()
})
let resizeTimer = 0
const onResize = () => {
  clearTimeout(resizeTimer)
  resizeTimer = window.setTimeout(() => { resize(); if (enabled.value) start() }, 150)
}

onBeforeUnmount(() => {
  stop()
  window.removeEventListener('resize', onResize)
  window.removeEventListener('pointermove', onPointer)
  document.removeEventListener('pointerleave', onLeave)
  document.removeEventListener('visibilitychange', onVisibility)
})
</script>

<template>
  <canvas ref="canvasRef" class="particle-bg" aria-hidden="true"></canvas>
</template>

<style scoped>
.particle-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  /* 亮色模式压低存在感，暗色模式用 screen 混出发光感 */
  mix-blend-mode: normal;
  opacity: 0.9;
}
html.dark .particle-bg {
  mix-blend-mode: screen;
  opacity: 0.85;
}
@media (max-width: 768px) {
  .particle-bg { display: none; } /* 移动端省电 */
}
</style>
