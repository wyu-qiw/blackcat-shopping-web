<template>
  <transition name="fade">
    <button
      v-if="show"
      class="back-btn"
      type="button"
      :title="title"
      @click="goBack"
    >
      <el-icon :size="18"><ArrowLeft /></el-icon>
      <span class="back-label">返回</span>
    </button>
  </transition>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 显示规则：
// - 首页（/）不显示
// - 后台页面（/admin/**）的返回按钮由 AdminLayout 自己渲染，这里隐藏避免重复/遮挡
const show = computed(() => {
  if (!route) return false
  // 工作台（左侧菜单导航）、首页、后台均不显示；仅登录/注册等独立页需要返回首页
  if (route.path === '/' || route.path === '') return false
  if (route.path.startsWith('/admin')) return false
  if (route.path.startsWith('/buy')) return false
  if (route.path.startsWith('/products')) return false
  if (route.path.startsWith('/publish')) return false
  if (route.path.startsWith('/my')) return false
  return true
})

const title = computed(() => {
  if (typeof window !== 'undefined' && window.history && window.history.length > 1) {
    return '返回上一个页面'
  }
  return '返回首页'
})

function goBack() {
  if (typeof window !== 'undefined' && window.history && window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}
</script>

<style scoped>
.back-btn {
  position: fixed;
  top: 78px;        /* UserLayout 顶栏 64px + 14px 间隙 */
  left: 20px;       /* 贴左上角 */
  z-index: 90;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px 8px 12px;
  background: rgba(255, 255, 255, 0.95);
  -webkit-backdrop-filter: blur(12px);
  backdrop-filter: blur(12px);
  border: 1.5px solid rgba(99, 102, 241, 0.60);
  border-radius: 999px;
  color: #1d4ed8;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.12), 0 0 0 3px rgba(99, 102, 241, 0.10);
  transition: transform 0.18s, box-shadow 0.18s, background 0.18s;
  outline: none;
}
.back-btn:hover {
  transform: translateX(-2px);
  box-shadow: 0 10px 24px rgba(99, 102, 241, 0.35), 0 0 0 3px rgba(99, 102, 241, 0.18);
  background: #ffffff;
  color: #1e40af;
}
.back-btn:active { transform: translateX(0) scale(0.98); }
.back-btn .back-label { letter-spacing: 0.5px; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.18s, transform 0.18s; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateX(-8px); }
</style>