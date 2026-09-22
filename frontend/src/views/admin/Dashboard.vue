<template>
  <div v-loading="loading" class="dashboard-page">
    <div class="page-head">
      <h2>平台数据大盘</h2>
      <p>监控平台用户、商品与订单核心数据</p>
    </div>

    <!-- 顶部统计卡片 -->
    <div class="stat-grid">
      <el-card v-for="item in stats" :key="item.label" shadow="never" class="stat-card">
        <div class="stat-icon" :style="{ background: item.bg, color: item.color }">
          <el-icon :size="26"><component :is="item.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </div>
      </el-card>
    </div>

    <!-- 图表区 -->
    <div class="charts-grid">
      <!-- 1) 条形图：商品销量 Top 8 -->
      <el-card shadow="never" class="chart-card chart-bar">
        <template #header>
          <div class="chart-head">
            <span class="chart-title">商品销量 Top 8</span>
            <span class="chart-sub">按有效订单数统计</span>
          </div>
        </template>
        <div ref="barRef" class="chart-canvas"></div>
      </el-card>

      <!-- 2) 折线图：近 7 天销售趋势 -->
      <el-card shadow="never" class="chart-card chart-line">
        <template #header>
          <div class="chart-head">
            <span class="chart-title">近 7 天销售趋势</span>
            <el-radio-group v-model="trendDays" size="small" @change="loadTrend">
              <el-radio-button :value="7">7天</el-radio-button>
              <el-radio-button :value="30">30天</el-radio-button>
            </el-radio-group>
          </div>
        </template>
        <div ref="lineRef" class="chart-canvas"></div>
      </el-card>

      <!-- 3) 饼图：各品类销售占比 -->
      <el-card shadow="never" class="chart-card chart-pie">
        <template #header>
          <div class="chart-head">
            <span class="chart-title">各品类销售占比</span>
            <span class="chart-sub">按订单数</span>
          </div>
        </template>
        <div ref="pieRef" class="chart-canvas"></div>
      </el-card>
    </div>

    <el-card shadow="never" class="note-card">
      <template #header>统计口径</template>
      <el-alert
        title="销售额 = 已支付 + 退款中 + 已退款 的订单金额；销量 = 上述订单按商品/品类分组计数。在售商品数 = 上架商品数；已下架商品数 = 商品总数 - 在售商品数。"
        type="info"
        :closable="false"
        show-icon
      />
    </el-card>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { CircleCheck, CircleClose, Goods, Tickets, User } from '@element-plus/icons-vue'
import { adminApi } from '../../api'

const loading = ref(false)
const trendDays = ref(7)

const data = ref({
  userCount: 0, productCount: 0, onSaleCount: 0, offShelfCount: 0, orderCount: 0
})
const productSales = ref([])      // 条形图数据：[{name, value}]
const salesTrend = ref([])        // 折线图数据：[{date, count, amount}]
const categoryDist = ref([])      // 饼图数据：[{name, value}]

const barRef = ref()
const lineRef = ref()
const pieRef = ref()
let barChart, lineChart, pieChart
let resizeHandler

const stats = computed(() => [
  { label: '用户总数', value: data.value.userCount, icon: User, bg: '#e8f1f8', color: '#1f4e79' },
  { label: '商品总数', value: data.value.productCount, icon: Goods, bg: '#eef6ec', color: '#3e8e41' },
  { label: '在售商品数', value: data.value.onSaleCount, icon: CircleCheck, bg: '#eafaf0', color: '#188a5a' },
  { label: '已下架商品数', value: data.value.offShelfCount, icon: CircleClose, bg: '#fdeeee', color: '#c0392b' },
  { label: '订单数量', value: data.value.orderCount, icon: Tickets, bg: '#fdf3e7', color: '#d97706' }
])

/* ============== 图表配色 ============== */
const BLUE = '#2563eb'
const CYAN = '#38bdf8'
const PURPLE = '#8b5cf6'
const PIE_COLORS = ['#2563eb', '#38bdf8', '#8b5cf6', '#a78bfa', '#06b6d4', '#22d3ee', '#6366f1', '#818cf8']

async function loadStats() {
  loading.value = true
  try {
    data.value = await adminApi.stats()
  } finally {
    loading.value = false
  }
}

async function loadBar() {
  productSales.value = await adminApi.productSales(8)
  if (!barChart) return
  const names = productSales.value.map((d) => d.name).reverse()
  const values = productSales.value.map((d) => d.value).reverse()
  barChart.setOption({
    grid: { left: 110, right: 24, top: 16, bottom: 24 },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    xAxis: { type: 'value', splitLine: { lineStyle: { color: 'rgba(99,102,241,0.12)' } } },
    yAxis: { type: 'category', data: names, axisLabel: { color: '#475569', fontSize: 12 } },
    series: [{
      type: 'bar',
      data: values,
      barWidth: 16,
      itemStyle: {
        borderRadius: [0, 8, 8, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: BLUE },
          { offset: 1, color: PURPLE }
        ])
      },
      label: { show: true, position: 'right', color: '#1f2937', fontWeight: 600 }
    }]
  })
}

async function loadTrend() {
  salesTrend.value = await adminApi.salesTrend(trendDays.value)
  if (!lineChart) return
  const dates = salesTrend.value.map((d) => d.date.slice(5))  // MM-DD
  const counts = salesTrend.value.map((d) => d.count)
  const amounts = salesTrend.value.map((d) => Number(d.amount))
  lineChart.setOption({
    grid: { left: 48, right: 56, top: 32, bottom: 32 },
    tooltip: { trigger: 'axis' },
    legend: { top: 0, right: 0, textStyle: { color: '#475569' } },
    xAxis: {
      type: 'category', data: dates, boundaryGap: false,
      axisLine: { lineStyle: { color: 'rgba(99,102,241,0.3)' } },
      axisLabel: { color: '#475569', fontSize: 11 }
    },
    yAxis: [
      {
        type: 'value', name: '订单数', position: 'left',
        splitLine: { lineStyle: { color: 'rgba(99,102,241,0.10)' } },
        axisLabel: { color: '#475569' }
      },
      {
        type: 'value', name: '销售额(元)', position: 'right',
        splitLine: { show: false },
        axisLabel: { color: '#475569' }
      }
    ],
    series: [
      {
        name: '订单数', type: 'line', data: counts, smooth: true, symbol: 'circle', symbolSize: 7,
        lineStyle: { color: BLUE, width: 3 },
        itemStyle: { color: BLUE },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(37, 99, 235, 0.30)' },
            { offset: 1, color: 'rgba(37, 99, 235, 0.02)' }
          ])
        }
      },
      {
        name: '销售额', type: 'line', yAxisIndex: 1, data: amounts, smooth: true, symbol: 'circle', symbolSize: 7,
        lineStyle: { color: PURPLE, width: 3 },
        itemStyle: { color: PURPLE }
      }
    ]
  })
}

async function loadPie() {
  categoryDist.value = await adminApi.categoryDistribution()
  if (!pieChart) return
  const data = categoryDist.value.map((d, i) => ({
    name: d.name,
    value: d.value,
    itemStyle: { color: PIE_COLORS[i % PIE_COLORS.length] }
  }))
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 笔 ({d}%)' },
    legend: { orient: 'vertical', right: 8, top: 'middle', textStyle: { color: '#475569', fontSize: 12 } },
    series: [{
      name: '品类占比',
      type: 'pie',
      radius: ['45%', '72%'],
      center: ['38%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%', color: '#475569', fontSize: 12 },
      labelLine: { length: 8, length2: 12 },
      data
    }]
  })
}

async function initCharts() {
  await nextTick()
  if (barRef.value)  barChart  = echarts.init(barRef.value)
  if (lineRef.value) lineChart = echarts.init(lineRef.value)
  if (pieRef.value)  pieChart  = echarts.init(pieRef.value)
  await Promise.all([loadBar(), loadTrend(), loadPie()])
}

function handleResize() {
  barChart?.resize()
  lineChart?.resize()
  pieChart?.resize()
}

onMounted(async () => {
  await loadStats()
  await initCharts()
  resizeHandler = handleResize
  window.addEventListener('resize', resizeHandler)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeHandler)
  barChart?.dispose()
  lineChart?.dispose()
  pieChart?.dispose()
})

watch(trendDays, () => loadTrend())
</script>

<style scoped>
.page-head { margin-bottom: 18px; }
.page-head h2 { margin: 0 0 6px; color: #1f2937; font-size: 22px; }
.page-head p { margin: 0; color: #8a94a6; font-size: 13px; }

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 16px;
  margin-bottom: 18px;
}
.stat-card { border-radius: 8px; }
.stat-card :deep(.el-card__body) {
  display: flex; align-items: center; gap: 14px; padding: 20px;
}
.stat-icon {
  width: 52px; height: 52px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.stat-value { color: #1f2937; font-size: 24px; font-weight: 800; }
.stat-label { color: #8a94a6; font-size: 13px; margin-top: 2px; }

/* 图表区：3 张卡片，第一张占满宽，第二三张平分 */
.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: auto auto;
  gap: 16px;
  margin-bottom: 18px;
}
.chart-bar  { grid-column: 1 / -1; }
.chart-line { grid-column: 1 / 2; }
.chart-pie  { grid-column: 2 / 3; }

.chart-card { border-radius: 8px; }
.chart-head {
  display: flex; align-items: center; justify-content: space-between;
}
.chart-title { font-weight: 700; color: #1f2937; font-size: 15px; }
.chart-sub { color: #8a94a6; font-size: 12px; margin-left: 8px; }
.chart-canvas { width: 100%; height: 320px; }

.note-card { border-radius: 8px; }

@media (max-width: 1100px) {
  .charts-grid { grid-template-columns: 1fr; }
  .chart-bar, .chart-line, .chart-pie { grid-column: 1 / -1; }
}
</style>