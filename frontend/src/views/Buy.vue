<template>
  <div class="buy-page" v-loading="loading">
    <!-- 右侧功能页头部：当前范围 + 结果数 + 搜索关键词 + 批量勾选入口 -->
    <div class="buy-head">
      <div>
        <h2>{{ activeCategory ? currentCategoryName : '全部商品' }}</h2>
        <p>
          共 {{ filteredProducts.length }} 件好物
          <template v-if="keyword.trim()"> · 搜索“{{ keyword.trim() }}”</template>
        </p>
      </div>
      <div class="head-actions">
        <el-button
          :type="batchMode ? 'primary' : 'default'"
          class="batch-toggle"
          @click="toggleBatchMode"
        >
          <el-icon><Finished v-if="batchMode" /><Select v-else /></el-icon>
          <span>{{ batchMode ? '退出批量勾选' : '批量勾选' }}</span>
        </el-button>
        <el-button
          v-if="store.isLogin && cartStore.count > 0"
          class="cart-entry"
          @click="showMiniCart = true"
        >
          <el-badge :value="cartStore.count" :max="99">
            <el-icon :size="17"><ShoppingCart /></el-icon>
          </el-badge>
          <span>购物车</span>
        </el-button>
      </div>
    </div>

    <!-- 批量模式下的提示条 -->
    <div v-if="batchMode" class="batch-tip">
      <el-icon><InfoFilled /></el-icon>
      <span>勾选商品即可加入购物车，取消勾选则移出；右下角小窗可查看与结算。</span>
    </div>

    <!-- 商品网格（分类 / 搜索由左侧菜单与搜索框通过路由 query 驱动） -->
    <div class="sell-wrap">
      <div class="product-grid">
        <div
          v-for="product in filteredProducts"
          :key="product.id"
          class="card-slot"
          @click="goDetail(product.id)"
        >
          <!-- 批量勾选框 -->
          <span
            v-if="batchMode"
            class="pick-box"
            :class="{
              on: isInCart(product),
              disabled: !canPick(product)
            }"
            @click.stop="onTogglePick(product)"
          >
            {{ isInCart(product) ? '✔️' : '' }}
          </span>
          <ProductCard
            v-memo="[product.id, product.status, product.price, product.coverImage, product.title, batchMode, isInCart(product)]"
            :product="product"
          />
        </div>
      </div>
      <el-empty
        v-if="!loading && !filteredProducts.length"
        description="暂无符合条件的商品"
      />
    </div>

    <!-- 迷你购物车小窗 -->
    <transition name="mini-pop">
      <div v-if="showMiniCart && store.isLogin" class="mini-cart">
        <div class="mini-head">
          <span class="mini-title">
            <el-icon :size="18"><ShoppingCart /></el-icon>
            我的购物车
          </span>
          <el-icon class="mini-close" @click="showMiniCart = false"><Close /></el-icon>
        </div>

        <div class="mini-body" v-loading="cartStore.loading">
          <el-empty v-if="!cartStore.items.length" description="购物车为空" :image-size="64" />
          <div v-else>
            <div v-for="item in miniItems" :key="item.productId" class="mini-item">
              <el-image v-if="item.coverImage" :src="item.coverImage" fit="cover" class="mini-thumb" />
              <div v-else class="mini-thumb placeholder"><el-icon><Picture /></el-icon></div>
              <div class="mini-info">
                <span class="mini-name" :title="item.title">{{ item.title }}</span>
                <el-tag v-if="!canPick(item)" type="info" size="small" effect="plain">
                  {{ item.listed ? (item.status === 'RESERVED' ? '已被预购' : '不可购买') : '已下架' }}
                </el-tag>
              </div>
              <span class="mini-price">¥{{ Number(item.price).toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <div class="mini-total">
          <span>合计（{{ purchasableCount }} 件可结算<template v-if="purchasableCount < miniItems.length">，{{ miniItems.length - purchasableCount }} 件暂不可购</template>）</span>
          <span class="mini-sum">¥{{ miniTotal.toFixed(2) }}</span>
        </div>

        <div class="mini-actions">
          <el-button class="later-btn" @click="showMiniCart = false">稍后付款</el-button>
          <el-button type="primary" class="pay-now-btn" @click="goPay">
            确认付款
          </el-button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Close, Finished, InfoFilled, Picture, Select, ShoppingCart
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import ProductCard from '../components/ProductCard.vue'
import { categoryApi, productApi } from '../api'
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const cartStore = useCartStore()

const products = ref([])
const categories = ref([])
const loading = ref(false)
const activeCategory = ref(null)
const keyword = ref('')

// 批量勾选与迷你小窗
const batchMode = ref(false)
const showMiniCart = ref(false)

const filteredProducts = computed(() => {
  if (activeCategory.value === null) return products.value
  return products.value.filter((p) => p.categoryId === activeCategory.value)
})

const currentCategoryName = computed(() => {
  const c = categories.value.find((x) => x.id === activeCategory.value)
  return c ? c.name : '全部商品'
})

// 迷你小窗展示购物车内全部商品；合计仅统计可购买商品
const miniItems = computed(() => cartStore.items)
const miniTotal = computed(() =>
  cartStore.items
    .filter((i) => canPick(i))
    .reduce((sum, i) => sum + Number(i.price || 0), 0)
)
const purchasableCount = computed(() => cartStore.items.filter((i) => canPick(i)).length)

async function loadData() {
  loading.value = true
  try {
    const [catList, productList] = await Promise.all([
      categoryApi.list(),
      productApi.list(keyword.value.trim() || undefined)
    ])
    categories.value = catList
    products.value = productList
  } finally {
    loading.value = false
  }
}

function goDetail(id) {
  router.push(`/products/${id}`)
}

function canPick(product) {
  return product && product.listed && product.status === 'ONSALE'
}
function isInCart(product) {
  return cartStore.items.some((i) => i.productId === product.id)
}

async function toggleBatchMode() {
  if (!batchMode.value) {
    // 进入批量模式需要登录
    if (!store.isLogin) {
      ElMessage.warning('请先登录后再使用购物车')
      router.push({ path: '/login', query: { redirect: route.fullPath } })
      return
    }
    await cartStore.fetchCart(true)
    batchMode.value = true
  } else {
    batchMode.value = false
    showMiniCart.value = false
  }
}

async function onTogglePick(product) {
  if (!canPick(product)) {
    ElMessage.warning('该商品当前不可购买（已下架或已被预购）')
    return
  }
  try {
    if (isInCart(product)) {
      await cartStore.remove(product.id)
      ElMessage.info('已从购物车移除')
    } else {
      await cartStore.add(product.id)
      ElMessage.success('已加入购物车')
      // 勾选后弹出迷你购物车小窗
      showMiniCart.value = true
    }
  } catch {
    // 请求层已提示
  }
}

function goPay() {
  showMiniCart.value = false
  router.push('/my/cart')
}

// 左侧菜单的搜索 / 分类通过路由 query 联动
watch(
  [() => route.query.q, () => route.query.cat],
  ([q, cat]) => {
    keyword.value = (q || '').toString()
    activeCategory.value = cat != null ? Number(cat) : null
  },
  { immediate: true }
)

watch(keyword, () => loadData())
watch(activeCategory, () => loadData())

onMounted(async () => {
  await loadData()
  if (store.isLogin) {
    cartStore.fetchCart(true).catch(() => {})
  }
})
</script>

<style scoped>
.buy-page {
  min-height: 100%;
}

.buy-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}
.buy-head h2 {
  margin: 0 0 6px;
  font-size: 26px;
  font-weight: 800;
  letter-spacing: 0.5px;
  background: linear-gradient(120deg, #2563eb, #8b5cf6);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.buy-head p {
  margin: 0;
  color: #475569;
  font-size: 13px;
}
.head-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.batch-toggle,
.cart-entry {
  border-radius: 999px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.batch-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  margin-bottom: 16px;
  font-size: 13px;
  color: #1d4ed8;
  background: rgba(239, 246, 255, 0.9);
  border: 1.5px solid rgba(37, 99, 235, 0.4);
  border-radius: 12px;
}

.sell-wrap { min-height: 200px; }
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(232px, 1fr));
  gap: 20px;
}
.card-slot {
  position: relative;
  cursor: pointer;
}

/* 批量勾选框：覆盖在卡片左上角 */
.pick-box {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 5;
  width: 26px;
  height: 26px;
  border-radius: 8px;
  border: 2px solid rgba(37, 99, 235, 0.65);
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.22);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  transition: transform 0.15s, background 0.15s;
}
.pick-box:hover { transform: scale(1.08); }
.pick-box.on {
  background: linear-gradient(120deg, #2563eb, #38bdf8);
  border-color: #2563eb;
  color: #fff;
}
.pick-box.disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

/* ===== 迷你购物车小窗 ===== */
.mini-cart {
  position: fixed;
  right: 28px;
  bottom: 28px;
  width: 360px;
  max-width: calc(100vw - 32px);
  z-index: 1000;
  background: rgba(255, 255, 255, 0.92);
  -webkit-backdrop-filter: blur(16px) saturate(1.2);
  backdrop-filter: blur(16px) saturate(1.2);
  border: 1.5px solid rgba(99, 102, 241, 0.55);
  border-radius: 20px;
  box-shadow: 0 24px 60px -18px rgba(37, 99, 235, 0.45);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.mini-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1.5px solid rgba(37, 99, 235, 0.25);
}
.mini-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 800;
  color: #1e293b;
}
.mini-close {
  cursor: pointer;
  color: #64748b;
  transition: color 0.15s;
}
.mini-close:hover { color: #ef4444; }

.mini-body {
  max-height: 280px;
  overflow-y: auto;
  padding: 8px 16px;
}
.mini-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px dashed rgba(37, 99, 235, 0.15);
}
.mini-item:last-child { border-bottom: none; }
.mini-thumb {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  flex-shrink: 0;
}
.mini-thumb.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef3fe;
  color: #94a3b8;
}
.mini-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.mini-name {
  font-size: 13px;
  color: #1f2937;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.mini-price {
  color: #d4380d;
  font-weight: 800;
  font-size: 14px;
  flex-shrink: 0;
}
.mini-total {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  font-size: 14px;
  color: #334155;
  font-weight: 600;
  border-top: 1.5px solid rgba(37, 99, 235, 0.2);
  background: rgba(239, 246, 255, 0.6);
}
.mini-sum { color: #d4380d; font-size: 22px; font-weight: 800; }
.mini-actions {
  display: flex;
  gap: 10px;
  padding: 12px 16px 16px;
}
.mini-actions .el-button { flex: 1; border-radius: 999px; }
.later-btn { font-weight: 700; }
.pay-now-btn { font-weight: 800; }

.mini-pop-enter-active,
.mini-pop-leave-active {
  transition: transform 0.25s ease, opacity 0.25s ease;
}
.mini-pop-enter-from,
.mini-pop-leave-to {
  transform: translateY(24px) scale(0.96);
  opacity: 0;
}

@media (max-width: 900px) {
  .product-grid { grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 14px; }
}
</style>