<template>
  <div class="home-page">
    <!-- 1) 顶部风景背景：fixed，滚动时透明度从 1 → 0，做轻微视差 -->
    <div class="bg-photo" :style="bgStyle" aria-hidden="true"></div>

    <!-- 2) Hero 区：占满首屏 100vh -->
    <section class="hero">

      <!-- 玻璃磨砂主体（约 3/4 屏宽，居中） -->
      <div class="glass" :style="glassStyle">
        <div class="glass-badge">✦ AI 驱动 · 全新升级</div>
        <h1 class="glass-title">
          <span class="grad-blue">黑猫</span><span class="grad-purple">优选</span>
        </h1>
        <p class="glass-sub">黑猫优选购物平台，让 AI 帮你逛、帮你挑、帮你写文案</p>

        <!-- 项目核心特点：4 个 AI 能力卡片（无白色背景，与玻璃融为一体） -->
        <div class="feature-grid">
          <div
            v-for="f in features"
            :key="f.title"
            class="feature-card"
            :style="{ '--accent': f.accent, '--accent2': f.accent2 }"
          >
            <div class="feature-icon">
              <el-icon :size="22"><component :is="f.icon" /></el-icon>
            </div>
            <div class="feature-body">
              <h3>{{ f.title }}</h3>
              <p>{{ f.desc }}</p>
            </div>
          </div>
        </div>

        <!-- 醒目的"购买入口"按钮：点击进入 /buy 购买页 -->
        <div class="buy-entry">
          <button class="buy-btn" @click="goBuy">
            <el-icon :size="20"><ShoppingCart /></el-icon>
            <span>立即购买</span>
            <span class="buy-arrow">→</span>
          </button>
        </div>

      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  ChatLineRound, EditPen, ShoppingCart, Ticket
} from '@element-plus/icons-vue'

const router = useRouter()

// ===== 滚动状态（rAF 节流，避免每帧 set 触发多次重渲染）=====
const scrollY = ref(0)
let ticking = false
function onScroll() {
  if (ticking) return
  ticking = true
  requestAnimationFrame(() => {
    scrollY.value = window.scrollY || document.documentElement.scrollTop || 0
    ticking = false
  })
}
onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
})
onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))

// 背景：滚动 0 → 500px 时透明度 1 → 0；轻微视差上移
// 用 transform 走 GPU 合成层；不设 transition，避免每帧都重启动过渡
const bgStyle = computed(() => {
  const o = Math.max(0, 1 - scrollY.value / 500)
  return {
    opacity: o,
    transform: `translate3d(0, ${scrollY.value * 0.15}px, 0)`
  }
})

// 玻璃卡片：只做透明度淡出，不再做 translate（避免 backdrop-filter 反复重算）
// "自然上移"由页面正常滚动带它走，无需额外 transform
const glassStyle = computed(() => ({
  opacity: Math.max(0.35, 1 - scrollY.value / 700)
}))

// 4 个 AI 能力卡片（无 to，纯展示；点击不跳转——避免误触）
const features = [
  {
    icon: ChatLineRound,
    title: 'AI 智能助手',
    desc: '随时对话，问商品、问订单、问平台规则',
    accent: '#2563eb', accent2: '#38bdf8'
  },
  {
    icon: EditPen,
    title: 'AI 生成描述',
    desc: '发布商品时一键生成卖点文案，省时省力',
    accent: '#8b5cf6', accent2: '#a78bfa'
  },
  {
    icon: ShoppingCart,
    title: '精选好物',
    desc: '分类清晰，搜索直达，购物体验干净流畅',
    accent: '#6366f1', accent2: '#818cf8'
  },
  {
    icon: Ticket,
    title: '订单全程跟踪',
    desc: '下单、付款、撤销、退款状态一目了然',
    accent: '#06b6d4', accent2: '#22d3ee'
  }
]

function goBuy() {
  router.push('/buy')
}
</script>

<style scoped>
/* =========================================================
   Home：全屏风景背景 + 玻璃磨砂主体（3/4 屏）
   配色：白 / 蓝 (#2563eb) / 紫 (#8b5cf6)
   性能要点：
   - 滚动驱动用 transform/opacity 走 GPU 合成层
   - 不在 scroll-driven 元素上设 transition（每帧都重启过渡会卡）
   - 玻璃卡片不做滚动位移，只淡出（避免 backdrop-filter 反复重算）
   - will-change 精确标注，不滥用
   ========================================================= */
.home-page {
  position: relative;
  width: 100%;
  min-height: 100vh;
}

/* 1) 顶部风景背景：fixed，opacity 由 JS 控制 */
.bg-photo {
  position: fixed;
  inset: 0;
  z-index: -1;
  background-image: url('../assets/xiaoba.png');
  background-size: cover;
  background-position: center;
  filter: saturate(1.15) contrast(1.05);
  will-change: opacity, transform;
  /* 不设 transition：rAF 已经把更新频率限制在 60fps，加 transition 反而每帧重启动画 */
}

/* 2) Hero：占满首屏 */
.hero {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80px 24px 24px;
  box-sizing: border-box;
  overflow: hidden;
  /* 把 hero 隔离在独立合成层，避免里面元素变化触发整页 reflow */
  contain: layout paint;
}



.glass {
  position: relative;
  z-index: 1;
  width: min(75vw, 1100px);
  max-width: 1100px;
  min-height: 60vh;
  padding: 44px 52px 38px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.70);
  -webkit-backdrop-filter: blur(14px) saturate(1.2);
  backdrop-filter: blur(14px) saturate(1.2);
  border: 1.5px solid rgba(99, 102, 241, 0.55);
  box-shadow:
    0 30px 60px -20px rgba(37, 99, 235, 0.35),
    0 18px 40px -10px rgba(139, 92, 246, 0.20),
    inset 0 1px 0 rgba(255, 255, 255, 0.7);
  text-align: center;
  will-change: opacity;
  contain: layout paint;
}

.glass-badge {
  display: inline-block;
  padding: 6px 16px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.4px;
  color: #1d4ed8;
  background: rgba(255, 255, 255, 0.92);
  border: 1.5px solid rgba(99, 102, 241, 0.55);
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.20);
  margin-bottom: 16px;
}

.glass-title {
  margin: 0 0 10px;
  font-size: clamp(32px, 4.5vw, 52px);
  font-weight: 800;
  letter-spacing: 1.5px;
  line-height: 1.12;
}
.grad-blue {
  background: linear-gradient(120deg, #1d4ed8, #38bdf8);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.grad-purple {
  background: linear-gradient(120deg, #8b5cf6, #a78bfa);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.dot { color: #cbd5e1; margin: 0 6px; }

.glass-sub {
  margin: 0 0 28px;
  color: #475569;
  font-size: 15px;
  font-weight: 500;
}

/* 4) AI 能力卡片：透明背景，与玻璃卡融为一体（去掉嵌套白色板） */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin: 22px 0 28px;
}
.feature-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 16px;
  background: transparent;                          /* ← 关键：去掉白色底 */
  border: 1.5px solid rgba(99, 102, 241, 0.45);
  text-align: left;
  transition: transform 0.2s, border-color 0.2s, box-shadow 0.2s, background 0.2s;
  cursor: default;
}
.feature-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 14px 30px rgba(37, 99, 235, 0.18);
  border-color: var(--accent, #2563eb);
  background: rgba(255, 255, 255, 0.35);           /* hover 时浮起一层更亮的玻璃感 */
}
.feature-icon {
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, var(--accent, #2563eb), var(--accent2, #38bdf8));
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.28);
}
.feature-body h3 {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
}
.feature-body p {
  margin: 0;
  font-size: 12px;
  color: #64748b;
  line-height: 1.45;
}

/* 5) 购买入口 */
.buy-entry {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin: 8px 0 26px;
  flex-wrap: wrap;
}
.buy-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 16px 40px;
  font-size: 17px;
  font-weight: 800;
  color: #fff;
  border: none;
  border-radius: 999px;
  background: linear-gradient(120deg, #2563eb 0%, #38bdf8 50%, #8b5cf6 100%);
  background-size: 200% 100%;
  box-shadow: 0 14px 32px -6px rgba(99, 102, 241, 0.55);
  cursor: pointer;
  letter-spacing: 1px;
  transition: transform 0.22s, box-shadow 0.22s, background-position 0.5s;
  animation: shine 4s linear infinite;
}
.buy-btn:hover {
  transform: translateY(-3px) scale(1.04);
  box-shadow: 0 20px 40px -8px rgba(139, 92, 246, 0.6);
  background-position: 100% 0;
}
.buy-btn .buy-arrow { font-size: 20px; margin-left: 4px; transition: transform 0.2s; }
.buy-btn:hover .buy-arrow { transform: translateX(4px); }
@keyframes shine {
  0%   { background-position: 0% 0; }
  100% { background-position: 200% 0; }
}



/* 7) 响应式 */
@media (max-width: 960px) {
  .glass { padding: 32px 24px 28px; width: 90vw; }
  .feature-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 600px) {
  .feature-grid { grid-template-columns: 1fr; }
  .buy-entry { flex-direction: column; }
  .buy-btn { width: 100%; justify-content: center; }
}
</style>




