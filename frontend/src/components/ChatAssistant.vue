<template>
  <div>
    <!-- 1.2cm 悬浮球：可拖动，按住拖到屏幕任意位置；不拖则视为点击，打开面板 -->
    <div
      v-show="!open"
      ref="ballRef"
      class="ai-ball"
      :class="{ 'is-dragging': ballDrag.isDragging }"
      :style="ballStyle"
      @pointerdown="onBallPointerDown"
    >
      <img src="/assets/xiaoxin.png" alt="AI 助手" />
    </div>

    <transition name="fade">
      <div
        v-if="open"
        ref="panelRef"
        class="ai-panel"
        :class="{
          'is-maximized': isMaximized,
          'is-dragging': panelDrag.isDragging && !isMaximized
        }"
        :style="panelStyle"
      >
        <!-- 头部 = 拖拽手柄（仅在非全屏时可拖） -->
        <div
          class="ai-head"
          :class="{ 'no-drag': isMaximized }"
          @pointerdown="onPanelPointerDown"
        >
          <img src="/assets/xiaoxin.png" alt="" class="head-avatar" />
          <span class="head-title">小新助手</span>
          <span class="head-hint">{{ isMaximized ? '全屏模式' : '可拖动' }}</span>
          <div class="head-actions">
            <el-icon
              class="action"
              :title="isMaximized ? '还原' : '全屏'"
              @click="toggleMaximize"
            >
              <component :is="isMaximized ? 'Minus' : 'FullScreen'" />
            </el-icon>
            <el-icon class="action close" title="关闭" @click="closePanel">
              <Close />
            </el-icon>
          </div>
        </div>

        <div class="ai-msgs" ref="msgsRef">
          <div v-for="(m, i) in messages" :key="i" class="msg" :class="m.role">
            <span v-if="m.role === 'bot'" class="msg-avatar">
              <img src="/assets/xiaoxin.png" alt="" />
            </span>
            <span class="msg-bubble">{{ m.content }}</span>
          </div>
        </div>

        <div class="ai-input">
          <el-input
            v-model="text"
            placeholder="输入问题…"
            @keyup.enter="send"
            :disabled="loading"
          />
          <el-button type="primary" :loading="loading" @click="send">发送</el-button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { Close, FullScreen, Minus } from '@element-plus/icons-vue'
import { aiApi } from '../api'

// ===== 物理 / 尺寸常量 =====
const CM_TO_PX = 96 / 2.54                       // 96dpi 下 1cm ≈ 37.8px
const BALL_SIZE = 1.2 * CM_TO_PX                 // ≈ 45.35px
const PANEL_W = 420
const PANEL_H = 580
const MARGIN = 12                                // 离视口边缘最小留白
const CLICK_THRESHOLD = 4                        // 移动 > 4px 才算拖，否则视为点击

// ===== 通用拖拽状态工厂 =====
function createDragState() {
  return reactive({
    isDragging: false,
    moved: false,        // 是否真的发生了位移（用于区分"拖"和"点"）
    offsetX: 0,          // 鼠标按下时，相对目标左上角的偏移
    offsetY: 0,
    startX: 0,           // 鼠标按下的初始坐标（用于计算 moved）
    startY: 0
  })
}

// ===== 状态 =====
const open = ref(false)
const isMaximized = ref(false)
const text = ref('')
const loading = ref(false)
const panelRef = ref()
const ballRef = ref()
const msgsRef = ref()
const messages = ref([
  { role: 'bot', content: '哥哥姐姐们好呀！我是小新，商品、订单问题都可以问我的喔\~' }
])

// 面板坐标（独立于球的位置）
const panelPos = reactive({ left: 0, top: 0 })
const panelDrag = createDragState()
let panelPosInit = false

// 球坐标
const ballPos = reactive({ left: 0, top: 0 })
const ballDrag = createDragState()
let ballPosInit = false

// ===== 默认位置 =====
function defaultBallPos() {
  ballPos.left = window.innerWidth  - BALL_SIZE - MARGIN
  ballPos.top  = window.innerHeight - BALL_SIZE - MARGIN
}
function defaultPanelPos() {
  // 默认在球的正上方（球中心对齐面板水平中心）
  const ballCenterX = ballPos.left + BALL_SIZE / 2
  panelPos.left = Math.max(
    MARGIN,
    Math.min(ballCenterX - PANEL_W / 2, window.innerWidth - PANEL_W - MARGIN)
  )
  panelPos.top = Math.max(MARGIN, ballPos.top - PANEL_H - 12)
}

// ===== Pointer Events：同时支持鼠标、触控笔和手机触摸 =====
function onBallPointerDown(e) {
  if (e.pointerType === 'mouse' && e.button !== 0) return
  const rect = ballRef.value.getBoundingClientRect()
  ballDrag.isDragging = true
  ballDrag.moved = false
  ballDrag.startX = e.clientX
  ballDrag.startY = e.clientY
  ballDrag.offsetX = e.clientX - rect.left
  ballDrag.offsetY = e.clientY - rect.top
  window.addEventListener('pointermove', onBallPointerMove)
  window.addEventListener('pointerup', onBallPointerUp)
  window.addEventListener('pointercancel', onBallPointerUp)
  e.preventDefault()
}

function onBallPointerMove(e) {
  if (!ballDrag.isDragging) return
  if (Math.abs(e.clientX - ballDrag.startX) > CLICK_THRESHOLD ||
      Math.abs(e.clientY - ballDrag.startY) > CLICK_THRESHOLD) {
    ballDrag.moved = true
  }
  const nextLeft = e.clientX - ballDrag.offsetX
  const nextTop = e.clientY - ballDrag.offsetY
  ballPos.left = Math.max(0, Math.min(nextLeft, window.innerWidth - BALL_SIZE))
  ballPos.top = Math.max(0, Math.min(nextTop, window.innerHeight - BALL_SIZE))
}

function onBallPointerUp() {
  const wasMoved = ballDrag.moved
  ballDrag.isDragging = false
  ballDrag.moved = false
  window.removeEventListener('pointermove', onBallPointerMove)
  window.removeEventListener('pointerup', onBallPointerUp)
  window.removeEventListener('pointercancel', onBallPointerUp)
  if (!wasMoved) openPanel()
}

function onPanelPointerDown(e) {
  if (isMaximized.value) return
  if (e.target.closest('.head-actions')) return
  if (e.pointerType === 'mouse' && e.button !== 0) return
  const rect = panelRef.value.getBoundingClientRect()
  panelDrag.isDragging = true
  panelDrag.moved = false
  panelDrag.startX = e.clientX
  panelDrag.startY = e.clientY
  panelDrag.offsetX = e.clientX - rect.left
  panelDrag.offsetY = e.clientY - rect.top
  window.addEventListener('pointermove', onPanelPointerMove)
  window.addEventListener('pointerup', onPanelPointerUp)
  window.addEventListener('pointercancel', onPanelPointerUp)
  e.preventDefault()
}

function onPanelPointerMove(e) {
  if (!panelDrag.isDragging || isMaximized.value) return
  if (Math.abs(e.clientX - panelDrag.startX) > CLICK_THRESHOLD ||
      Math.abs(e.clientY - panelDrag.startY) > CLICK_THRESHOLD) {
    panelDrag.moved = true
  }
  const nextLeft = e.clientX - panelDrag.offsetX
  const nextTop = e.clientY - panelDrag.offsetY
  panelPos.left = Math.max(MARGIN, Math.min(nextLeft, window.innerWidth - PANEL_W - MARGIN))
  panelPos.top = Math.max(MARGIN, Math.min(nextTop, window.innerHeight - PANEL_H - MARGIN))
}

function onPanelPointerUp() {
  panelDrag.isDragging = false
  panelDrag.moved = false
  window.removeEventListener('pointermove', onPanelPointerMove)
  window.removeEventListener('pointerup', onPanelPointerUp)
  window.removeEventListener('pointercancel', onPanelPointerUp)
}
// ===== 球的：mousedown / mousemove / mouseup =====
function onBallMouseDown(e) {
  // 仅响应左键
  if (e.button !== 0) return
  const rect = ballRef.value.getBoundingClientRect()
  ballDrag.isDragging = true
  ballDrag.moved = false
  ballDrag.startX = e.clientX
  ballDrag.startY = e.clientY
  ballDrag.offsetX = e.clientX - rect.left
  ballDrag.offsetY = e.clientY - rect.top
  // 全局挂载，移出球也不会丢事件
  window.addEventListener('mousemove', onBallMouseMove)
  window.addEventListener('mouseup',   onBallMouseUp)
  e.preventDefault()                    // 阻止文本选中 / 浏览器默认行为
}

function onBallMouseMove(e) {
  if (!ballDrag.isDragging) return
  // 超过阈值才标记为"已移动"
  if (!ballDrag.moved) {
    if (Math.abs(e.clientX - ballDrag.startX) > CLICK_THRESHOLD ||
        Math.abs(e.clientY - ballDrag.startY) > CLICK_THRESHOLD) {
      ballDrag.moved = true
    }
  }
  // 核心公式：面板新位置 = 鼠标当前位置 - 按下时的偏移
  const nextLeft = e.clientX - ballDrag.offsetX
  const nextTop  = e.clientY - ballDrag.offsetY
  // 边界裁剪：球不能拖出视口
  const w = window.innerWidth
  const h = window.innerHeight
  ballPos.left = Math.max(MARGIN, Math.min(nextLeft, w - BALL_SIZE - MARGIN))
  ballPos.top  = Math.max(MARGIN, Math.min(nextTop,  h - BALL_SIZE - MARGIN))
}

function onBallMouseUp() {
  const wasMoved = ballDrag.moved
  ballDrag.isDragging = false
  ballDrag.moved = false
  window.removeEventListener('mousemove', onBallMouseMove)
  window.removeEventListener('mouseup',   onBallMouseUp)
  // 没真正移动 → 视为点击，打开面板
  if (!wasMoved) openPanel()
}

// ===== 面板的：mousedown / mousemove / mouseup =====
function onPanelMouseDown(e) {
  if (isMaximized.value) return
  if (e.button !== 0) return
  const rect = panelRef.value.getBoundingClientRect()
  panelDrag.isDragging = true
  panelDrag.moved = false
  panelDrag.startX = e.clientX
  panelDrag.startY = e.clientY
  panelDrag.offsetX = e.clientX - rect.left
  panelDrag.offsetY = e.clientY - rect.top
  window.addEventListener('mousemove', onPanelMouseMove)
  window.addEventListener('mouseup',   onPanelMouseUp)
  e.preventDefault()
}

function onPanelMouseMove(e) {
  if (!panelDrag.isDragging || isMaximized.value) return
  if (!panelDrag.moved) {
    if (Math.abs(e.clientX - panelDrag.startX) > CLICK_THRESHOLD ||
        Math.abs(e.clientY - panelDrag.startY) > CLICK_THRESHOLD) {
      panelDrag.moved = true
    }
  }
  const nextLeft = e.clientX - panelDrag.offsetX
  const nextTop  = e.clientY - panelDrag.offsetY
  const w = window.innerWidth
  const h = window.innerHeight
  panelPos.left = Math.max(MARGIN, Math.min(nextLeft, w - PANEL_W - MARGIN))
  panelPos.top  = Math.max(MARGIN, Math.min(nextTop,  h - PANEL_H - MARGIN))
}

function onPanelMouseUp() {
  // 什么都不做：panelPos.left/top 已经是松手瞬间的最终值
  // 不吸附、不回弹、不动画 → 精确停
  panelDrag.isDragging = false
  panelDrag.moved = false
  window.removeEventListener('mousemove', onPanelMouseMove)
  window.removeEventListener('mouseup',   onPanelMouseUp)
}

// ===== 打开/关闭/全屏 =====
function openPanel() {
  if (!ballPosInit)  { defaultBallPos();  ballPosInit  = true }
  if (!panelPosInit) { defaultPanelPos(); panelPosInit = true }
  open.value = true
  nextTick(() => scrollToBottom())
}
function closePanel() {
  open.value = false
}
function toggleMaximize() {
  isMaximized.value = !isMaximized.value
  nextTick(() => scrollToBottom())
}

// ===== 消息滚动 =====
function scrollToBottom() {
  if (msgsRef.value) msgsRef.value.scrollTop = msgsRef.value.scrollHeight
}

// ===== 发送消息 =====
async function send() {
  const content = text.value.trim()
  if (!content || loading.value) return

  // 组装本次提问之前的最近对话作为上下文（界面的 bot 对应模型 assistant），
  // 并过滤掉“思考中…”等占位回复
  const history = messages.value
    .filter((m) => m.content && m.content !== '思考中…')
    .slice(-10)
    .map((m) => ({
      role: m.role === 'bot' ? 'assistant' : 'user',
      content: m.content
    }))

  text.value = ''
  messages.value.push({ role: 'user', content })
  messages.value.push({ role: 'bot', content: '思考中…' })
  await nextTick(); scrollToBottom()
  loading.value = true
  try {
    const reply = await aiApi.chat(content, history)
    messages.value[messages.value.length - 1].content = reply
  } catch {
    messages.value[messages.value.length - 1].content = 'AI 服务暂不可用，请稍后再试'
  } finally {
    loading.value = false
    await nextTick(); scrollToBottom()
  }
}

// ===== 窗口缩放：把两个浮元素都拉回视口 =====
function onResize() {
  const w = window.innerWidth
  const h = window.innerHeight
  if (ballPosInit) {
    ballPos.left = Math.max(MARGIN, Math.min(ballPos.left, w - BALL_SIZE - MARGIN))
    ballPos.top  = Math.max(MARGIN, Math.min(ballPos.top,  h - BALL_SIZE - MARGIN))
  }
  if (open.value && !isMaximized.value && panelPosInit) {
    panelPos.left = Math.max(MARGIN, Math.min(panelPos.left, w - PANEL_W - MARGIN))
    panelPos.top  = Math.max(MARGIN, Math.min(panelPos.top,  h - PANEL_H - MARGIN))
  }
}

// ===== 样式计算 =====
const ballStyle = computed(() => ({
  left: ballPos.left + 'px',
  top:  ballPos.top  + 'px',
  width:  BALL_SIZE + 'px',
  height: BALL_SIZE + 'px'
}))
const panelStyle = computed(() => {
  if (isMaximized.value) {
    return { left: '0px', top: '0px', width: '100vw', height: '100vh', borderRadius: '0' }
  }
  return {
    left:   panelPos.left + 'px',
    top:    panelPos.top  + 'px',
    width:  PANEL_W + 'px',
    height: PANEL_H + 'px'
  }
})

// ===== 生命周期 =====
onMounted(() => {
  defaultBallPos()
  defaultPanelPos()
  ballPosInit = true
  panelPosInit = true
  window.addEventListener('resize', onResize)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  // 兜底：组件卸载时清掉所有可能残留的全局监听
  window.removeEventListener('mousemove', onBallMouseMove)
  window.removeEventListener('mouseup',   onBallMouseUp)
  window.removeEventListener('mousemove', onPanelMouseMove)
  window.removeEventListener('mouseup',   onPanelMouseUp)
})
</script>

<style scoped>
/* ===== 1.2cm 悬浮球：可拖动 ===== */
.ai-ball {
  position: fixed;
  z-index: 999;
  border-radius: 50%;
  overflow: hidden;
  cursor: grab;
  border: 2.5px solid #fff;
  box-shadow: 0 8px 22px rgba(37, 99, 235, 0.55), 0 0 0 3px rgba(99, 102, 241, 0.25);
  background: linear-gradient(135deg, #2563eb, #38bdf8);
  transition: transform 0.2s, box-shadow 0.2s;
  user-select: none;
  -webkit-user-drag: none;
  -webkit-user-select: none;
}
.ai-ball.is-dragging {
  cursor: grabbing;
  transform: scale(1.06);
  box-shadow: 0 14px 32px rgba(37, 99, 235, 0.55);
  transition: none;       /* 拖拽中关闭过渡，避免"慢一拍" */
}
.ai-ball img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  pointer-events: none;   /* 让 img 不抢事件，统一在父元素上处理 */
}

/* ===== 面板 ===== */
.ai-panel {
  position: fixed;
  z-index: 1000;
  will-change: transform, left, top;
  background: rgba(255, 255, 255, 0.70);
  -webkit-backdrop-filter: blur(14px) saturate(1.2);
  backdrop-filter: blur(14px) saturate(1.2);
  border: 1.5px solid rgba(99, 102, 241, 0.55);
  border-radius: 16px;
  box-shadow:
    0 24px 60px -10px rgba(37, 99, 235, 0.30),
    0 16px 36px -8px rgba(139, 92, 246, 0.20),
    inset 0 1px 0 rgba(255, 255, 255, 0.7);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.ai-panel.is-maximized {
  border: none;
}
.ai-panel.is-dragging {
  /* 拖拽中降低模糊度到 6px（普通 14px），避免每帧重算 backdrop-filter 拖慢拖动 */
  -webkit-backdrop-filter: blur(6px) saturate(1.1) !important;
  backdrop-filter: blur(6px) saturate(1.1) !important;
  box-shadow: 0 30px 80px rgba(15, 23, 42, 0.32);
  transition: none;
  user-select: none;
}

.ai-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  color: #fff;
  font-weight: 700;
  background: linear-gradient(120deg, #2563eb, #38bdf8);
  cursor: grab;
  user-select: none;
}
.ai-head.no-drag {
  cursor: default;
}
.ai-head:active:not(.no-drag) {
  cursor: grabbing;
}

.head-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #fff;
  object-fit: cover;
  flex-shrink: 0;
  pointer-events: none;
}
.head-title {
  font-size: 15px;
}
.head-hint {
  font-size: 11px;
  font-weight: 500;
  opacity: 0.75;
  margin-left: 4px;
}
.head-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 4px;
}
.head-actions .action {
  cursor: pointer;
  opacity: 0.85;
  padding: 4px;
  border-radius: 6px;
  transition: background 0.15s, opacity 0.15s;
}
.head-actions .action:hover {
  opacity: 1;
  background: rgba(255, 255, 255, 0.18);
}
.head-actions .action.close:hover {
  background: rgba(239, 68, 68, 0.45);
}

.ai-msgs {
  flex: 1;
  overflow: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  /* 蜡笔小新风格聊天背景：图片铺满 + 30% 白色蒙版，保证消息气泡可读 */
  background: linear-gradient(180deg, rgba(255,255,255,0.55), rgba(255,255,255,0.65)),
              url('/assets/xiaobai.jpg') center / cover no-repeat;
}
.msg {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  max-width: 90%;
}
.msg.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}
.msg-avatar {
  flex-shrink: 0;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb, #38bdf8);
  padding: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.msg-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
  background: #fff;
  pointer-events: none;
}
.msg-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.55;
  word-break: break-word;
  white-space: pre-wrap;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.10);
  border: 1.5px solid rgba(15, 23, 42, 0.06);
}
.msg.user .msg-bubble {
  color: #fff;
  background: linear-gradient(120deg, #2563eb, #38bdf8);
  border-bottom-right-radius: 4px;
}
.msg.bot .msg-bubble {
  background: #fff;
  color: #0f172a;
  border-bottom-left-radius: 4px;
}
.ai-input {
  display: flex;
  gap: 8px;
  padding: 12px;
  border-top: 1.5px solid rgba(37, 99, 235, 0.40);
  background: #fff;
}

/* ===== 过渡 ===== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.98);
}
@media (pointer: coarse) {
  .ai-ball,
  .ai-head:not(.no-drag) { touch-action: none; }
}
</style>

