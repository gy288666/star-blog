<script setup lang="ts">
// 仪表盘：统计卡片 + 访问趋势图 + 热门文章
definePageMeta({ layout: 'admin' })

useHead({ title: '仪表盘' })

const { get } = useApi()

const summary = ref<Record<string, any>>({})
const trend = ref<{ date: string; count: number }[]>([])
const topPosts = ref<{ id: number; title: string; views: number }[]>([])

const cards = computed(() => [
  { label: '文章', value: summary.value.postCount ?? '-', icon: 'doc' },
  { label: '说说', value: summary.value.shuoshuoCount ?? '-', icon: 'quote' },
  { label: '评论', value: summary.value.commentCount ?? '-', icon: 'chat' },
  { label: '待审评论', value: summary.value.pendingComments ?? '-', icon: 'hourglass' },
  { label: '总阅读', value: summary.value.viewsSum ?? '-', icon: 'eye' },
  { label: '友链', value: summary.value.friendCount ?? '-', icon: 'link' },
  { label: '运行天数', value: summary.value.runDays ?? '-', icon: 'clock' }
])

const chartEl = ref<HTMLDivElement | null>(null)
let chart: any = null

const renderChart = async () => {
  if (!chartEl.value) return
  const echarts = await import('echarts')
  if (!chart) chart = echarts.init(chartEl.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: trend.value.map((t) => t.date.slice(5)) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ name: '访问量', type: 'line', smooth: true, data: trend.value.map((t) => t.count),
      areaStyle: { opacity: 0.15 }, lineStyle: { color: '#5e72e4' }, itemStyle: { color: '#5e72e4' } }]
  })
}

onMounted(async () => {
  try {
    summary.value = (await get('/api/admin/stats/summary')) || {}
    trend.value = (await get('/api/admin/stats/views', { days: 14 })) || []
    topPosts.value = (await get('/api/admin/stats/topPosts', { limit: 8 })) || []
  } catch { /* 降级空态 */ }
  await renderChart()
  window.addEventListener('resize', resizeChart)
})
const resizeChart = () => chart?.resize()
onUnmounted(() => { window.removeEventListener('resize', resizeChart); chart?.dispose() })
</script>

<template>
  <div class="dashboard">
    <div class="stat-cards">
      <div v-for="c in cards" :key="c.label" class="stat-card card">
        <span class="stat-icon"><UiIcon :name="c.icon" :size="24" /></span>
        <div>
          <div class="stat-value">{{ c.value }}</div>
          <div class="stat-label">{{ c.label }}</div>
        </div>
      </div>
    </div>
    <div class="dash-grid">
      <div class="card chart-card">
        <h3><UiIcon name="trend" :size="16" /> 近 14 天访问趋势</h3>
        <div ref="chartEl" class="chart"></div>
      </div>
      <div class="card top-card">
        <h3><UiIcon name="fire" :size="16" /> 热门文章</h3>
        <ul class="top-list">
          <li v-for="(p, i) in topPosts" :key="p.id">
            <span class="rank" :class="`rank-${i + 1}`">{{ i + 1 }}</span>
            <NuxtLink :to="`/post/${p.id}`" target="_blank" class="top-title">{{ p.title }}</NuxtLink>
            <span class="top-views">{{ p.views }} 阅读</span>
          </li>
          <li v-if="!topPosts.length" class="top-empty">暂无数据</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stat-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 14px; margin-bottom: 20px; }
.stat-card { display: flex; align-items: center; gap: 12px; padding: 18px; }
.stat-icon { font-size: 30px; }
.stat-value { font-size: 22px; font-weight: 700; }
.stat-label { font-size: 12px; color: var(--text-secondary); }
.dash-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.chart-card, .top-card { padding: 20px; }
.chart-card h3, .top-card h3 { margin-top: 0; }
.chart { height: 300px; }
.top-list { list-style: none; padding: 0; margin: 0; }
.top-list li { display: flex; align-items: center; gap: 10px; padding: 8px 0; border-bottom: 1px dashed var(--border-color); }
.rank { width: 22px; height: 22px; border-radius: 6px; background: var(--bg-hover); font-size: 12px; display: inline-flex; align-items: center; justify-content: center; }
.rank-1 { background: #fb6340; color: #fff; }
.rank-2 { background: #fbaf40; color: #fff; }
.rank-3 { background: #ffd166; color: #fff; }
.top-title { flex: 1; color: var(--text-primary); text-decoration: none; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.top-views { color: var(--text-secondary); font-size: 12px; }
.top-empty { justify-content: center; color: var(--text-secondary); border: none !important; }
@media (max-width: 900px) { .dash-grid { grid-template-columns: 1fr; } }
</style>
