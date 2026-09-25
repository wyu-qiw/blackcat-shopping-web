<template>
  <el-container class="admin-layout" :class="{ 'mobile-menu-open': mobileMenuOpen }">
    <div v-if="mobileMenuOpen" class="admin-menu-backdrop" @click="mobileMenuOpen = false"></div>
    <el-aside :width="asideWidth" class="aside" :class="{ 'mobile-open': mobileMenuOpen, 'desktop-collapsed': desktopMenuCollapsed }">
      <div class="admin-brand">
        <el-icon :size="22"><DataAnalysis /></el-icon>
        <span>商城管理后台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :default-openeds="defaultOpeneds"
        router
        class="admin-menu"
        :collapse="desktopMenuCollapsed"
        background-color="#ffffff"
        text-color="#4b5563"
        active-text-color="#1f4e79"
      >
        <el-menu-item index="/admin">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据大盘</span>
        </el-menu-item>

        <!-- 商品管理：可展开 → 商品列表 / 商品分类 / 品牌分类 -->
        <el-sub-menu index="product-manage">
          <template #title>
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </template>

          <!-- 1. 商品列表：全部商品，格式与原商品管理一致 -->
          <el-menu-item index="/admin/products">
            <el-icon><List /></el-icon>
            <span>商品列表</span>
          </el-menu-item>

          <!-- 2. 商品分类：与商城页面分类一致（动态同步，新增分类自动出现） -->
          <el-sub-menu index="product-categories">
            <template #title>
              <el-icon><MenuIcon /></el-icon>
              <span>商品分类</span>
            </template>
            <el-menu-item
              v-for="c in categories"
              :key="c.id"
              :index="`/admin/products?cat=${c.id}`"
            >
              <span class="leaf-dot"></span>
              <span>{{ c.name }}</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 3. 品牌分类：展开显示各品牌名称，点击查看该品牌商品 -->
          <el-sub-menu index="product-brands">
            <template #title>
              <el-icon><Collection /></el-icon>
              <span>品牌分类</span>
            </template>
            <el-menu-item
              v-for="b in brands"
              :key="b.id"
              :index="`/admin/products?brand=${b.id}`"
            >
              <span class="leaf-dot brand"></span>
              <span>{{ b.name }}</span>
            </el-menu-item>
          </el-sub-menu>
        </el-sub-menu>

        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <el-icon><Tickets /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="admin-body">
      <el-header class="admin-header">
        <!-- 左侧：返回按钮 + 页面标题 -->
        <button type="button" class="admin-mobile-menu-btn" aria-label="打开后台菜单" @click="toggleAdminMenu">
            <el-icon :size="20"><Menu /></el-icon>
          </button>
          <div class="header-left">
          <button class="header-back" type="button" :title="backTitle" @click="goBack">
            <el-icon :size="16"><ArrowLeft /></el-icon>
            <span>返回</span>
          </button>
          <div class="header-title">{{ route.meta.title || '管理后台' }}</div>
        </div>
        <!-- 右侧：返回商城 + 用户 -->
        <div class="header-right">
          <el-button text @click="router.push('/')">
            <el-icon><Shop /></el-icon>
            <span>返回商城</span>
          </el-button>
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-trigger">
              <el-avatar :size="30" :src="store.user?.avatar">
                {{ store.user?.nickname?.slice(0, 1) }}
              </el-avatar>
              <span>{{ store.user?.nickname }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, ArrowDown, Collection, DataAnalysis, Goods, List, Menu, Menu as MenuIcon,
  Shop, Tickets, User
} from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { brandApi, categoryApi } from '../api'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

// 左侧菜单中的商品分类 / 品牌（与商城分类同源，后续新增分类会自动同步）
const categories = ref([])
const brands = ref([])
const mobileMenuOpen = ref(false)
const desktopMenuCollapsed = ref(false)

const asideWidth = computed(() => desktopMenuCollapsed.value ? '64px' : '220px')

function isMobileViewport() {
  return typeof window !== 'undefined' && window.matchMedia('(max-width: 768px)').matches
}

function toggleAdminMenu() {
  if (isMobileViewport()) {
    desktopMenuCollapsed.value = false
    mobileMenuOpen.value = true
    return
  }
  desktopMenuCollapsed.value = !desktopMenuCollapsed.value
}

function handleViewportChange() {
  if (isMobileViewport()) {
    desktopMenuCollapsed.value = false
  } else {
    mobileMenuOpen.value = false
  }
}
watch(() => route.fullPath, () => { mobileMenuOpen.value = false })

// 商品管理默认展开，便于直接看到三个子板块
const defaultOpeneds = ref(['product-manage'])

// 当前高亮项：商品区需把 query（cat/brand）计入，才能高亮具体分类或品牌
const activeMenu = computed(() => {
  if (route.path === '/admin/products') {
    const parts = []
    if (route.query.cat) parts.push(`cat=${route.query.cat}`)
    if (route.query.brand) parts.push(`brand=${route.query.brand}`)
    return parts.length ? `/admin/products?${parts.join('&')}` : '/admin/products'
  }
  return route.path
})

const backTitle = computed(() => {
  if (typeof window !== 'undefined' && window.history && window.history.length > 1) {
    return '返回上一个页面'
  }
  return '返回数据大盘'
})

function goBack() {
  if (typeof window !== 'undefined' && window.history && window.history.length > 1) {
    router.back()
  } else {
    router.push('/admin')
  }
}

function handleCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
      .then(() => {
        store.logout()
        ElMessage.success('已退出登录')
        router.push('/login')
      })
      .catch(() => {})
  }
}

window.addEventListener('resize', handleViewportChange)

onMounted(async () => {
  try {
    const [catList, brandList] = await Promise.all([
      categoryApi.list(),
      brandApi.list()
    ])
    categories.value = catList
    brands.value = brandList
  } catch {
    // 请求失败已由请求层提示
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleViewportChange)
})
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.aside {
  background: rgba(255,255,255,0.88);
  border-right: 1.5px solid rgba(37, 99, 235, 0.45);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.admin-brand {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 20px;
  color: var(--brand);
  font-size: 17px;
  font-weight: 700;
  border-bottom: 1.5px solid rgba(37, 99, 235, 0.40);
  background: linear-gradient(90deg, rgba(239, 246, 255, 0.6), transparent);
}

.admin-menu {
  border-right: none;
  flex: 1;
}

.aside {
  transition: width 0.28s cubic-bezier(0.4, 0, 0.2, 1);
}

.aside.desktop-collapsed .admin-brand {
  padding: 0;
  justify-content: center;
}

.aside.desktop-collapsed .admin-brand span {
  display: none;
}

.admin-mobile-menu-btn {
  width: 38px;
  height: 38px;
  border: 1.5px solid rgba(37, 99, 235, 0.35);
  border-radius: 10px;
  background: #fff;
  color: var(--brand);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  cursor: pointer;
}

/* 三级菜单项前的小圆点：分类为蓝色，品牌为紫色 */
.leaf-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #2563eb;
  margin-right: 9px;
  flex-shrink: 0;
}
.leaf-dot.brand { background: #8b5cf6; }

.admin-header {
  background: rgba(255,255,255,0.92);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border-bottom: 1.5px solid rgba(37, 99, 235, 0.45);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  gap: 16px;
}

/* 左侧：返回按钮 + 标题 */
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
  flex: 1;
}
.header-back {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: 1.5px solid rgba(99, 102, 241, 0.60);
  background: rgba(255, 255, 255, 0.95);
  color: #1d4ed8;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.18s, box-shadow 0.18s, background 0.18s, border-color 0.18s;
  flex-shrink: 0;
}
.header-back:hover {
  background: #ffffff;
  color: #6d28d9;
  border-color: #6d28d9;
  transform: translateX(-2px);
  box-shadow: 0 6px 14px rgba(139, 92, 246, 0.30);
}
.header-back:active { transform: translateX(0) scale(0.98); }

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #333;
  outline: none;
}

.admin-main {
  background: rgba(246, 249, 255, 0.72);
  padding: 20px;
  overflow: auto;
  /* 隔离主内容区为独立合成层，列表/图表变化不会重绘整页 */
  contain: paint;
  will-change: scroll-position;
}

/* 小屏适配：缩小 padding，按钮紧凑 */
@media (max-width: 768px) {
  .admin-header { padding: 0 12px; gap: 8px; }
  .header-left { gap: 8px; }
  .header-back span { display: none; }
  .header-back { padding: 6px 8px; }
  .header-title { font-size: 14px; }
  .header-right { gap: 8px; }
  .header-right .el-button span { display: none; }
}

/* ===== 移动端：后台侧边栏改为抽屉 ===== */
@media (max-width: 768px) {
  .admin-layout {
    display: block !important;
    height: auto;
    min-height: 100vh;
  }

  .admin-menu-backdrop {
    position: fixed;
    inset: 0;
    z-index: 1000;
    background: rgba(15, 23, 42, 0.44);
    backdrop-filter: blur(2px);
  }

  .aside {
    position: fixed !important;
    top: 0 !important;
    left: 0 !important;
    width: min(82vw, 290px) !important;
    height: 100dvh !important;
    transform: translateX(-105%);
    transition: transform 0.28s cubic-bezier(0.4, 0, 0.2, 1);
    z-index: 1001;
  }

  .aside.mobile-open {
    transform: translateX(0);
  }

  .admin-body {
    width: 100%;
    min-height: 100vh;
  }

  .admin-header {
    height: auto !important;
    min-height: 56px;
    padding: 8px 10px !important;
  }

  .admin-mobile-menu-btn {
    width: 38px;
    height: 38px;
    border: 1.5px solid rgba(37, 99, 235, 0.35);
    border-radius: 10px;
    background: #fff;
    color: var(--brand);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .header-title {
    font-size: 15px;
  }

  .header-right .el-dropdown .user-trigger span:not(.el-avatar) {
    display: none;
  }

  .admin-main {
    padding: 12px !important;
  }
}
</style>

