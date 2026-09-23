<template>
  <div class="shop-layout" :class="{ expanded, 'mobile-menu-open': mobileMenuOpen }">
    <div v-if="mobileMenuOpen" class="mobile-menu-backdrop" @click="mobileMenuOpen = false"></div>
    <!-- 左侧功能栏：品牌 + 可纵向展开菜单；宽度随展开在 collapsed/expanded 间过渡 -->
    <aside class="side" :class="{ expanded: expanded, 'mobile-open': mobileMenuOpen }">
      <!-- 品牌 -->
      <router-link to="/" class="brand" :title="'黑猫优选'">
        <img src="/assets/heimao-logo.jpg" alt="黑猫优选" class="brand-logo" />
        <span v-show="expanded" class="brand-name">黑猫优选</span>
      </router-link>

      <!-- 搜索：展开时显示 -->
      <div v-show="expanded" class="side-search">
        <el-input
          v-model="keyword"
          placeholder="搜索商品…"
          clearable
          :prefix-icon="Search"
          @keyup.enter="doSearch"
          @clear="doSearch"
        />
      </div>

      <!-- 功能菜单 -->
      <nav class="menu">
        <!-- 1. 全部商品（可纵向展开：全部 + 各分类，由列组成） -->
        <div class="menu-group" :class="{ open: openGoods }">
          <button
            type="button"
            class="menu-item parent"
            :class="{ active: isGoodsActive }"
            @click="toggleGoods"
          >
            <el-icon :size="19"><Goods /></el-icon>
            <span v-show="expanded" class="mi-label">全部商品</span>
            <el-icon v-show="expanded" class="mi-arrow" :class="{ rot: openGoods }"><ArrowDown /></el-icon>
          </button>

          <!-- 纵向展开的子列 -->
          <div v-show="expanded" class="sub-col" :class="{ show: openGoods }">
            <button
              type="button"
              class="menu-item sub"
              :class="{ active: route.path === '/buy' && !route.query.cat }"
              @click="goAll"
            >
              <span class="dot"></span>
              <span class="mi-label">全部</span>
            </button>
            <button
              v-for="c in categories"
              :key="c.id"
              type="button"
              class="menu-item sub"
              :class="{ active: route.path === '/buy' && String(route.query.cat) === String(c.id) }"
              @click="goCategory(c.id)"
            >
              <span class="dot"></span>
              <span class="mi-label">{{ c.name }}</span>
            </button>
          </div>
        </div>

        <!-- 2. 购物车（位于全部商品与发布商品之间，带数量角标） -->
        <button
          type="button"
          class="menu-item parent cart-menu"
          :class="{ active: route.path === '/my/cart' }"
          @click="goCart"
        >
          <el-badge :value="cartStore.count" :hidden="cartStore.count === 0" :max="99" class="cart-badge">
            <el-icon :size="19"><ShoppingCart /></el-icon>
          </el-badge>
          <span v-show="expanded" class="mi-label">购物车</span>
        </button>

        <!-- 3. 发布商品 -->
        <button
          type="button"
          class="menu-item parent"
          :class="{ active: route.path === '/publish' }"
          @click="goPublish"
        >
          <el-icon :size="19"><Upload /></el-icon>
          <span v-show="expanded" class="mi-label">发布商品</span>
        </button>

        <!-- 3. 个人中心（可纵向展开） -->
        <div class="menu-group" :class="{ open: openMe }">
          <button
            type="button"
            class="menu-item parent"
            :class="{ active: isMeActive }"
            @click="toggleMe"
          >
            <el-icon :size="19"><User /></el-icon>
            <span v-show="expanded" class="mi-label">个人中心</span>
            <el-icon v-show="expanded" class="mi-arrow" :class="{ rot: openMe }"><ArrowDown /></el-icon>
          </button>

          <div v-show="expanded" class="sub-col" :class="{ show: openMe }">
            <button
              type="button"
              class="menu-item sub"
              :class="{ active: route.path === '/my/profile' }"
              @click="goMe('/my/profile')"
            >
              <span class="dot"></span><span class="mi-label">账号信息</span>
            </button>
            <button
              type="button"
              class="menu-item sub"
              :class="{ active: route.path === '/my/orders' }"
              @click="goMe('/my/orders')"
            >
              <span class="dot"></span><span class="mi-label">我的订单</span>
            </button>
            <button
              type="button"
              class="menu-item sub"
              :class="{ active: route.path === '/my/products' }"
              @click="goMe('/my/products')"
            >
              <span class="dot"></span><span class="mi-label">我的商品</span>
            </button>
            <button
              v-if="store.isAdmin"
              type="button"
              class="menu-item sub admin-link"
              @click="goAdmin"
            >
              <span class="dot"></span><span class="mi-label">进入后台</span>
            </button>
          </div>
        </div>
      </nav>

      <!-- 底部：展开/收起切换 -->
      <button type="button" class="collapse-btn" @click="expanded = !expanded">
        <el-icon :size="18"><DArrowLeft v-if="expanded" /><DArrowRight v-else /></el-icon>
        <span v-show="expanded" class="mi-label">收起菜单</span>
      </button>
    </aside>

    <!-- 右侧主区域 -->
    <div class="main">
      <!-- 右上角用户头像 -->
      <header class="topbar">
        <button type="button" class="mobile-menu-btn" aria-label="打开功能菜单" @click="mobileMenuOpen = true">
          <el-icon :size="21"><Menu /></el-icon>
        </button>
        <div class="crumb">{{ route.meta.title || '黑猫优选' }}</div>
        <div class="top-right">
          <template v-if="store.isLogin">
            <el-dropdown trigger="click" @command="handleCommand">
              <span class="user-trigger">
                <el-avatar :size="34" :src="store.user?.avatar">
                  {{ store.user?.nickname?.slice(0, 1) }}
                </el-avatar>
                <span class="nickname">{{ store.user?.nickname }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item command="products">我的商品</el-dropdown-item>
                  <el-dropdown-item command="publish">发布商品</el-dropdown-item>
                  <el-dropdown-item v-if="store.isAdmin" command="admin" divided>进入后台</el-dropdown-item>
                  <el-dropdown-item command="logout" :divided="!store.isAdmin">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="login-link">登录</router-link>
            <router-link to="/register" class="register-btn">注册</router-link>
          </template>
        </div>
      </header>

      <!-- 右侧功能页面（左边菜单所对应的操作都在这里呈现） -->
      <main class="content">
        <router-view />
      </main>
    </div>

  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowDown, DArrowLeft, DArrowRight, Goods, Menu, Search, ShoppingCart, Upload, User
} from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'
import { categoryApi } from '../api'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const cartStore = useCartStore()

// 已登录则预载购物车，供左侧角标与小窗使用
if (store.isLogin) {
  cartStore.fetchCart(true).catch(() => {})
}

// 左侧默认展开（纵向菜单可见）。点击带箭头的分组可展开/收起子列。
const expanded = ref(true)
const openGoods = ref(true)
const openMe = ref(false)
const mobileMenuOpen = ref(false)
const keyword = ref((route.query.q || '').toString())
const categories = ref([])

const isGoodsActive = computed(() => route.path === '/buy' || route.path.startsWith('/products/'))
const isMeActive = computed(() => route.path.startsWith('/my/'))

// 进入对应功能时，自动展开所属分组
function ensureGroup(path) {
  if (path === '/buy' || path.startsWith('/products/')) openGoods.value = true
  if (path.startsWith('/my/')) openMe.value = true
}

function toggleGoods() {
  openGoods.value = !openGoods.value
  // 收起时若当前不在商品区，则不跳走；展开时若当前也不在商品区，进入全部商品
  if (openGoods.value && !isGoodsActive.value) router.push('/buy')
}
function toggleMe() {
  openMe.value = !openMe.value
  if (openMe.value && !isMeActive.value) router.push('/my/profile')
}

function goAll() {
  openGoods.value = true
  router.push({ path: '/buy' })
}
function goCategory(id) {
  openGoods.value = true
  router.push({ path: '/buy', query: { cat: id } })
}
function goCart() {
  if (!requireLogin()) return
  router.push('/my/cart')
}

function doSearch() {
  openGoods.value = true
  const q = keyword.value.trim()
  router.push({ path: '/buy', query: q ? { q } : {} })
}
function goPublish() {
  router.push('/publish')
}
function goMe(path) {
  openMe.value = true
  router.push(path)
}
function goAdmin() {
  router.push('/admin')
}

function requireLogin(next) {
  if (!store.isLogin) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return false
  }
  return true
}

function handleCommand(command) {
  const map = {
    profile: '/my/profile',
    orders: '/my/orders',
    products: '/my/products',
    publish: '/publish'
  }
  if (command === 'logout') {
    ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
      .then(() => {
        store.logout()
        ElMessage.success('已退出登录')
        router.push('/')
      })
      .catch(() => {})
    return
  }
  if (command === 'admin') {
    router.push('/admin')
    return
  }
  const target = map[command]
  if (target === '/publish' || target.startsWith('/my/')) {
    if (!requireLogin()) return
  }
  if (target) {
    ensureGroup(target)
    router.push(target)
  }
}

watch(() => route.fullPath, () => { mobileMenuOpen.value = false })

onMounted(async () => {
  ensureGroup(route.path)
  keyword.value = (route.query.q || '').toString()
  try {
    categories.value = await categoryApi.list()
  } catch {
    categories.value = []
  }
})
</script>

<style scoped>
.shop-layout {
  display: flex;
  min-height: 100vh;
  background:
    radial-gradient(1000px 560px at 0% -10%, rgba(56, 189, 248, 0.12), transparent 60%),
    radial-gradient(900px 520px at 100% 0%, rgba(129, 140, 248, 0.14), transparent 55%),
    #f6f9ff;
}

/* ===== 左侧功能栏 ===== */
.side {
  position: sticky;
  top: 0;
  align-self: flex-start;
  height: 100vh;
  width: 76px;                 /* 收起态：只显图标 */
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.9);
  -webkit-backdrop-filter: blur(14px) saturate(1.2);
  backdrop-filter: blur(14px) saturate(1.2);
  border-right: 1.5px solid rgba(37, 99, 235, 0.45);
  box-shadow: 8px 0 30px -18px rgba(37, 99, 235, 0.35);
  overflow: hidden;
  transition: width 0.28s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 20;
}
.side.expanded {
  width: 248px;                /* 展开态：功能列完整显示 */
}

/* 展开后与右侧内容的间距为左侧宽度的 1/8（248/8 ≈ 31px），
   这里用 margin 负间距思路：直接给 .content 留 padding-left，见下；
   侧栏右缘本身的分隔线已体现边界，间距在 .content 中控制为 1/8。 */

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 64px;
  padding: 0 20px;
  flex-shrink: 0;
  text-decoration: none;
  border-bottom: 1.5px solid rgba(37, 99, 235, 0.18);
  white-space: nowrap;
}
.brand-logo {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  border-radius: 10px;
  object-fit: cover;
  display: block;
  border: 1.5px solid rgba(37, 99, 235, 0.45);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.28);
}
.brand-name {
  font-size: 19px;
  font-weight: 800;
  letter-spacing: 1px;
  background: linear-gradient(120deg, #1d4ed8, #8b5cf6);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.side-search {
  padding: 14px 16px 6px;
  flex-shrink: 0;
}

.menu {
  flex: 1;
  overflow-y: auto;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-group { display: flex; flex-direction: column; }

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 11px 14px;
  border: 1.5px solid transparent;
  border-radius: 12px;
  background: transparent;
  color: #334155;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  text-align: left;
  white-space: nowrap;
  transition: background 0.18s, border-color 0.18s, color 0.18s, transform 0.18s;
}
.menu-item .mi-label { overflow: hidden; text-overflow: ellipsis; }
.menu-item:hover {
  background: rgba(239, 246, 255, 0.9);
  border-color: rgba(99, 102, 241, 0.45);
  color: #1d4ed8;
}
.menu-item.active {
  background: linear-gradient(120deg, rgba(37, 99, 235, 0.12), rgba(139, 92, 246, 0.12));
  border-color: rgba(37, 99, 235, 0.65);
  color: #1d4ed8;
  box-shadow: 0 6px 16px -8px rgba(37, 99, 235, 0.5);
}
/* 购物车角标在菜单中的对齐 */
.cart-menu .cart-badge {
  display: inline-flex;
  align-items: center;
  line-height: 1;
}
.cart-menu .cart-badge :deep(.el-badge__content) {
  border: 1.5px solid #fff;
}

.mi-arrow {
  margin-left: auto;
  transition: transform 0.22s;
}
.mi-arrow.rot { transform: rotate(180deg); }

/* 纵向展开的子列 */
.sub-col {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin: 2px 0 4px;
  padding-left: 14px;
  border-left: 1.5px dashed rgba(99, 102, 241, 0.35);
  margin-left: 22px;
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.3s ease;
}
.sub-col.show { max-height: 520px; }
.menu-item.sub {
  padding: 8px 12px;
  font-size: 13px;
  font-weight: 500;
}
.menu-item.sub .dot {
  width: 7px; height: 7px; border-radius: 50%;
  background: #94a3b8; flex-shrink: 0;
}
.menu-item.sub.active .dot { background: #2563eb; box-shadow: 0 0 0 3px rgba(37,99,235,0.18); }
.menu-item.sub.admin-link { color: #6d28d9; }

.collapse-btn {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 8px 12px 14px;
  padding: 10px 14px;
  border: 1.5px solid rgba(99, 102, 241, 0.35);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  color: #1d4ed8;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  justify-content: center;
}
.side.expanded .collapse-btn { justify-content: flex-start; }
.collapse-btn:hover { background: #fff; border-color: #2563eb; }

/* ===== 右侧主区域 ===== */
.main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}
.topbar {
  position: sticky;
  top: 0;
  z-index: 15;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  background: rgba(255, 255, 255, 0.78);
  -webkit-backdrop-filter: blur(14px) saturate(1.2);
  backdrop-filter: blur(14px) saturate(1.2);
  border-bottom: 1.5px solid rgba(37, 99, 235, 0.30);
}
.crumb {
  font-size: 17px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: 0.5px;
}
.top-right { display: flex; align-items: center; gap: 14px; }
.user-trigger {
  display: flex; align-items: center; gap: 8px;
  cursor: pointer; outline: none;
  padding: 4px 10px 4px 4px;
  border: 1.5px solid transparent;
  border-radius: 999px;
  transition: border-color 0.18s, background 0.18s;
}
.user-trigger:hover { border-color: rgba(37, 99, 235, 0.5); background: rgba(239,246,255,0.7); }
.nickname { font-weight: 600; color: #1e293b; max-width: 120px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.login-link {
  color: #1d4ed8; font-weight: 700; text-decoration: none;
  padding: 8px 14px; border-radius: 8px; border: 1.5px solid transparent;
}
.login-link:hover { border-color: rgba(37,99,235,0.5); background: rgba(239,246,255,0.6); }
.register-btn {
  background: linear-gradient(120deg, #2563eb, #38bdf8);
  color: #fff; padding: 8px 18px; border-radius: 999px;
  text-decoration: none; font-weight: 700; font-size: 14px;
  border: 1.5px solid rgba(37,99,235,0.65);
  box-shadow: 0 4px 12px rgba(37,99,235,0.3);
}

/* 右侧内容：与左栏间距约为左侧宽度的 1/8（展开 248 → 约 31px；收起 76 → 约 10px） */
.content {
  flex: 1;
  /* 收起态：侧栏 76px 的 1/8 ≈ 10px */
  padding: 24px 36px 36px 10px;
  min-width: 0;
}
.shop-layout.expanded .content {
  /* 展开态：侧栏 248px 的 1/8 = 31px */
  padding: 31px 36px 36px 31px;
}

/* 小屏适配：侧边栏改为抽屉 */
@media (max-width: 900px) {
  .shop-layout { display: block; }

  .mobile-menu-backdrop {
    position: fixed;
    inset: 0;
    z-index: 90;
    background: rgba(15, 23, 42, 0.42);
  }

  .side,
  .side.expanded {
    position: fixed;
    top: 0;
    left: 0;
    width: min(82vw, 300px);
    height: 100dvh;
    transform: translateX(-105%);
    transition: transform 0.28s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 18px 0 44px -20px rgba(15, 23, 42, 0.45);
    z-index: 100;
  }

  .side.mobile-open { transform: translateX(0); }
  .collapse-btn { display: none; }

  .main {
    width: 100%;
    min-height: 100vh;
  }

  .topbar {
    height: 56px;
    padding: 0 12px;
    gap: 10px;
  }

  .mobile-menu-btn {
    width: 38px;
    height: 38px;
    border: 1.5px solid rgba(37, 99, 235, 0.35);
    border-radius: 11px;
    background: #fff;
    color: var(--brand);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .crumb {
    font-size: 16px;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .nickname { display: none; }

  .content,
  .shop-layout.expanded .content {
    padding: 16px 12px 28px;
  }
}
</style>
