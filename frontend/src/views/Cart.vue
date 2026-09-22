<template>
  <div class="cart-page">
    <div class="page-head">
      <div>
        <h2>我的购物车</h2>
        <p>勾选要结算的商品，合计金额仅按勾选商品计算</p>
      </div>
      <el-button :icon="Refresh" @click="reload">刷新</el-button>
    </div>

    <el-card v-loading="loading" shadow="never" class="cart-card">
      <el-empty v-if="!loading && !store.items.length" description="购物车还是空的，快去挑选好物吧～">
        <el-button type="primary" @click="router.push('/buy')">去购物</el-button>
      </el-empty>

      <template v-else>
        <!-- 表头 -->
        <div class="row head-row">
          <label class="check-cell" @click.prevent="toggleAll">
            <span class="cb" :class="{ on: allChecked }">{{ allChecked ? '✔️' : '' }}</span>
            <span>全选</span>
          </label>
          <span class="col-product">商品信息</span>
          <span class="col-price">单价</span>
          <span class="col-seller">卖家</span>
          <span class="col-status">状态</span>
          <span class="col-op">操作</span>
        </div>

        <!-- 商品行 -->
        <div
          v-for="item in store.items"
          :key="item.productId"
          class="row item-row"
          :class="{ disabled: !isPurchasable(item) }"
        >
          <label class="check-cell" @click.prevent="toggleOne(item)">
            <span class="cb" :class="{ on: isChecked(item), disabled: !isPurchasable(item) }">
              {{ isChecked(item) ? '✔️' : '' }}
            </span>
          </label>

          <div class="col-product product-cell" @click="goDetail(item.productId)">
            <el-image v-if="item.coverImage" :src="item.coverImage" fit="cover" class="thumb" />
            <div v-else class="thumb placeholder"><el-icon><Picture /></el-icon></div>
            <div class="p-info">
              <span class="p-title">{{ item.title }}</span>
              <span class="p-cat">{{ item.categoryName }}</span>
            </div>
          </div>

          <span class="col-price price">¥{{ Number(item.price).toFixed(2) }}</span>
          <span class="col-seller">{{ item.sellerName }}</span>
          <span class="col-status">
            <el-tag v-if="isPurchasable(item)" type="success" effect="plain" size="small">可购买</el-tag>
            <el-tag v-else type="info" effect="plain" size="small">{{ unavailableText(item) }}</el-tag>
          </span>
          <span class="col-op">
            <el-button type="danger" link @click="removeOne(item)">移除</el-button>
          </span>
        </div>

        <!-- 结算栏 -->
        <div class="settle-bar">
          <label class="check-cell" @click.prevent="toggleAll">
            <span class="cb" :class="{ on: allChecked }">{{ allChecked ? '✔️' : '' }}</span>
            <span>全选</span>
          </label>
          <el-button type="danger" link :disabled="!store.items.length" @click="clearAll">清空购物车</el-button>

          <div class="settle-right">
            <span class="selected-tip">
              已选 <b>{{ checkedPurchasable.length }}</b> 件
            </span>
            <span class="total-label">合计：</span>
            <span class="total-amount">¥{{ totalAmount.toFixed(2) }}</span>
            <el-button
              type="primary"
              size="large"
              class="pay-btn"
              :loading="submitting"
              :disabled="checkedPurchasable.length === 0"
              @click="confirmPay"
            >
              确认购买
            </el-button>
          </div>
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Refresh } from '@element-plus/icons-vue'
import { cartApi } from '../api'
import { useCartStore } from '../stores/cart'

const router = useRouter()
const store = useCartStore()

const loading = ref(false)
const submitting = ref(false)
// 被勾选的购物车记录ID（购物车表主键）
const checkedIds = ref(new Set())

const checkedItems = computed(() =>
  store.items.filter((p) => checkedIds.value.has(p.id))
)
// 只有可购买的勾选商品才计入合计
const checkedPurchasable = computed(() =>
  checkedItems.value.filter((p) => isPurchasable(p))
)
const totalAmount = computed(() =>
  checkedPurchasable.value.reduce((sum, p) => sum + Number(p.price || 0), 0)
)
const purchasableAll = computed(() => store.items.filter((p) => isPurchasable(p)))
const allChecked = computed(
  () => purchasableAll.value.length > 0 &&
    purchasableAll.value.every((p) => checkedIds.value.has(p.id))
)

function isPurchasable(item) {
  return item.listed && item.status === 'ONSALE'
}
function unavailableText(item) {
  if (!item.listed) return '已下架'
  if (item.status === 'RESERVED') return '已被预购'
  if (item.status === 'OUT_OF_STOCK') return '无库存'
  return '不可购买'
}
function isChecked(item) {
  return checkedIds.value.has(item.id)
}
function toggleOne(item) {
  if (!isPurchasable(item)) {
    ElMessage.warning('该商品当前不可购买')
    return
  }
  const next = new Set(checkedIds.value)
  if (next.has(item.id)) next.delete(item.id)
  else next.add(item.id)
  checkedIds.value = next
}
function toggleAll() {
  const next = new Set(checkedIds.value)
  if (allChecked.value) {
    // 取消全选：移除所有可购买项的勾选
    purchasableAll.value.forEach((p) => next.delete(p.id))
  } else {
    purchasableAll.value.forEach((p) => next.add(p.id))
  }
  checkedIds.value = next
}

async function reload() {
  loading.value = true
  try {
    await store.fetchCart(true)
  } finally {
    loading.value = false
  }
}

function goDetail(id) {
  router.push(`/products/${id}`)
}

async function removeOne(item) {
  try {
    await ElMessageBox.confirm(`将“${item.title}”移出购物车？`, '提示', {
      type: 'warning',
      confirmButtonText: '移除',
      cancelButtonText: '取消'
    })
    await store.remove(item.productId)
    const next = new Set(checkedIds.value)
    next.delete(item.id)
    checkedIds.value = next
    ElMessage.success('已移除')
  } catch (e) {
    // 用户取消
  }
}

async function clearAll() {
  try {
    await ElMessageBox.confirm('确定清空购物车内全部商品吗？', '提示', {
      type: 'warning',
      confirmButtonText: '清空',
      cancelButtonText: '取消'
    })
    await store.clear()
    checkedIds.value = new Set()
    ElMessage.success('购物车已清空')
  } catch (e) {
    // 用户取消
  }
}

async function confirmPay() {
  const ids = checkedPurchasable.value.map((p) => p.productId)
  if (!ids.length) {
    ElMessage.warning('请先勾选要购买的商品')
    return
  }
  submitting.value = true
  try {
    const orders = await cartApi.checkout(ids)
    const next = new Set(checkedIds.value)
    ids.forEach((pid) => {
      const it = store.items.find((x) => x.productId === pid)
      if (it) next.delete(it.id)
    })
    checkedIds.value = next
    await store.fetchCart(true)
    ElMessage.success(`购买成功，已生成 ${orders.length} 个订单`)
    router.push('/my/orders')
  } catch (e) {
    await store.fetchCart(true)
  } finally {
    submitting.value = false
  }
}

onMounted(reload)
</script>

<style scoped>
.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 18px;
}
.page-head h2 {
  margin: 0 0 6px;
  font-size: 24px;
  font-weight: 800;
  background: linear-gradient(120deg, #2563eb, #8b5cf6);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.page-head p { margin: 0; color: #64748b; font-size: 13px; }

.cart-card { border-radius: 20px; }

.row {
  display: grid;
  grid-template-columns: 84px 1fr 120px 120px 110px 90px;
  align-items: center;
  gap: 12px;
  padding: 14px 12px;
}
.head-row {
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
  border-bottom: 1.5px solid rgba(37, 99, 235, 0.25);
}
.item-row {
  border-bottom: 1px dashed rgba(37, 99, 235, 0.18);
  transition: background 0.18s;
}
.item-row:hover { background: rgba(239, 246, 255, 0.7); }
.item-row.disabled { opacity: 0.72; }

.check-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
  font-weight: 600;
  color: #334155;
}
.cb {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(37, 99, 235, 0.55);
  border-radius: 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  background: #fff;
  transition: all 0.15s;
  flex-shrink: 0;
}
.cb.on {
  background: linear-gradient(120deg, #2563eb, #38bdf8);
  border-color: #2563eb;
  color: #fff;
}
.cb.disabled { background: #f1f5f9; cursor: not-allowed; }

.product-cell { display: flex; align-items: center; gap: 12px; cursor: pointer; min-width: 0; }
.thumb { width: 56px; height: 56px; border-radius: 10px; flex-shrink: 0; }
.thumb.placeholder { display: flex; align-items: center; justify-content: center; background: #eef3fe; color: #94a3b8; }
.p-info { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.p-title {
  font-weight: 600; color: #1f2937;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.p-cat { font-size: 12px; color: #94a3b8; }

.price { color: #d4380d; font-weight: 800; font-size: 16px; }
.col-seller { color: #475569; font-size: 13px; }

.settle-bar {
  position: sticky;
  bottom: 0;
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 16px 12px;
  margin-top: 8px;
  background: rgba(255, 255, 255, 0.96);
  border: 1.5px solid rgba(37, 99, 235, 0.45);
  border-radius: 14px;
  box-shadow: 0 -6px 18px -10px rgba(37, 99, 235, 0.35);
}
.settle-right { margin-left: auto; display: flex; align-items: center; gap: 14px; }
.selected-tip { color: #475569; font-size: 14px; }
.selected-tip b { color: #2563eb; font-size: 16px; }
.total-label { color: #334155; font-weight: 700; }
.total-amount { color: #d4380d; font-size: 26px; font-weight: 800; }
.pay-btn { min-width: 150px; border-radius: 999px; }

@media (max-width: 1000px) {
  .row { grid-template-columns: 64px 1fr 100px 90px; }
  .col-seller, .col-status, .head-row .col-seller, .head-row .col-status { display: none; }
}
</style>