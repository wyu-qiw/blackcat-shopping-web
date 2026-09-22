<template>
  <div v-loading="loading" class="detail-page">
    <div class="detail-top">
      <button type="button" class="detail-back" title="返回上一界面" @click="goBack">
        <el-icon :size="16"><ArrowLeft /></el-icon>
        <span>返回</span>
      </button>
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/buy' }">购物</el-breadcrumb-item>
        <el-breadcrumb-item>商品详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <template v-if="product">
      <div class="detail-box">
        <div class="image-panel">
          <el-carousel
            v-if="gallery.length"
            :autoplay="false"
            indicator-position="outside"
            arrow="hover"
            height="360px"
            class="gallery"
          >
            <el-carousel-item v-for="(img, i) in gallery" :key="`${img}_${i}`">
              <el-image :src="img" fit="cover" class="main-image" preview-src-list="gallery" :initial-index="i" preview-teleported />
            </el-carousel-item>
          </el-carousel>
          <div v-else class="main-image placeholder">
            <el-icon :size="48"><Picture /></el-icon>
          </div>
        </div>

        <div class="info-panel">
          <div class="info-title">{{ product.title }}</div>
          <div class="info-line">
            <el-tag :type="productStatusTag(product.status).type" effect="dark">
              {{ productStatusTag(product.status).label }}
            </el-tag>
            <el-tag v-if="!product.listed" type="danger" effect="plain">已下架</el-tag>
          </div>

          <div class="price-box">
            <span class="price-label">售价</span>
            <span class="price">¥{{ product.price }}</span>
          </div>

          <el-descriptions :column="1" class="descriptions" border>
            <el-descriptions-item label="卖家">{{ product.sellerName }}</el-descriptions-item>
            <el-descriptions-item label="发布时间">{{ product.createTime }}</el-descriptions-item>
          </el-descriptions>

          <div class="buy-zone">
            <template v-if="product.listed">
              <el-button
                type="primary"
                size="large"
                class="buy-btn"
                :disabled="product.status !== 'ONSALE'"
                @click="buy"
              >
                {{ buyButtonText }}
              </el-button>
              <el-button
                size="large"
                class="cart-btn"
                :disabled="product.status !== 'ONSALE'"
                :loading="addingCart"
                @click="addToCart"
              >
                <el-icon class="cart-ico"><ShoppingCart /></el-icon>
                <span>+购物车</span>
              </el-button>
            </template>
            <el-button v-else type="info" size="large" disabled>商品已下架</el-button>
          </div>
        </div>
      </div>

      <el-card class="desc-card" shadow="never">
        <template #header>商品详情</template>
        <div class="description">{{ product.description }}</div>
      </el-card>

      <!-- 评论区 -->
      <el-card class="comment-card" shadow="never">
        <template #header>
          <div class="comment-head">
            <span>商品评论</span>
            <el-tag size="small" type="primary" effect="plain">{{ comments.length }} 条</el-tag>
          </div>
        </template>

        <div class="comment-input">
          <el-input
            v-model="commentText"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="说说你的看法吧（需登录）"
          />
          <div class="comment-actions">
            <el-button type="primary" :loading="submitting" @click="submitComment">
              发表评论
            </el-button>
          </div>
        </div>

        <el-empty v-if="!comments.length" description="还没有评论，快来抢沙发～" :image-size="80" />

        <ul v-else class="comment-list">
          <li v-for="comment in comments" :key="comment.id" class="comment-item">
            <el-avatar :size="38" :src="comment.avatar">
              {{ (comment.nickname || '?').slice(0, 1) }}
            </el-avatar>
            <div class="comment-main">
              <div class="comment-top">
                <span class="comment-name">{{ comment.nickname || '匿名用户' }}</span>
                <span class="comment-time">{{ comment.createTime }}</span>
              </div>
              <p class="comment-content">{{ comment.content }}</p>
            </div>
          </li>
        </ul>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Picture, ShoppingCart } from '@element-plus/icons-vue'
import { orderApi, productApi } from '../api'
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'
import { productStatusTag } from '../utils/status'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const cartStore = useCartStore()
const addingCart = ref(false)

// 返回上一界面；若没有可返回的历史（如直接打开详情链接），则回到购物页
function goBack() {
  if (window.history.state && window.history.state.back) {
    router.back()
  } else {
    router.push('/buy')
  }
}
const product = ref(null)
const loading = ref(false)
const comments = ref([])
const commentText = ref('')
const submitting = ref(false)

// 轮播图：优先 images 数组，老数据退化为单张封面
const gallery = computed(() => {
  if (Array.isArray(product.value?.images) && product.value.images.length) {
    return product.value.images
  }
  return product.value?.coverImage ? [product.value.coverImage] : []
})

const buyButtonText = computed(() => {
  if (!product.value) return ''
  if (product.value.status === 'RESERVED') return '已预购，暂不可购买'
  if (product.value.status === 'OUT_OF_STOCK') return '无库存，暂不可购买'
  return '立即购买'
})

async function loadDetail() {
  loading.value = true
  try {
    product.value = await productApi.detail(route.params.id)
    loadComments()
  } catch {
    router.replace('/')
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  try {
    comments.value = (await productApi.comments(route.params.id)) || []
  } catch {
    comments.value = []
  }
}

async function submitComment() {
  if (!store.isLogin) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (!commentText.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  submitting.value = true
  try {
    await productApi.addComment(route.params.id, commentText.value.trim())
    ElMessage.success('评论成功')
    commentText.value = ''
    loadComments()
  } finally {
    submitting.value = false
  }
}

async function addToCart() {
  if (!store.isLogin) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (product.value.status !== 'ONSALE') {
    ElMessage.warning('该商品当前不可加入购物车')
    return
  }
  addingCart.value = true
  try {
    await cartStore.add(product.value.id)
    ElMessage.success('已加入购物车')
  } catch {
    // 请求层已提示错误
  } finally {
    addingCart.value = false
  }
}
async function buy() {
  if (!store.isLogin) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认以 ¥${product.value.price} 购买“${product.value.title}”吗？`,
      '确认购买',
      { type: 'warning', confirmButtonText: '确认购买', cancelButtonText: '再想想' }
    )
    await orderApi.create(product.value.id)
    ElMessage.success('购买成功，商品状态已变为已预购')
    loadDetail()
  } catch (error) {
    if (error !== 'cancel') {
      // 接口错误已由请求层提示
    }
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.detail-page {
  min-height: 480px;
}

.detail-top {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.detail-back {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 7px 14px 7px 10px;
  border: 1.5px solid rgba(99, 102, 241, 0.60);
  background: rgba(255, 255, 255, 0.95);
  color: #1d4ed8;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.12);
  transition: transform 0.18s, box-shadow 0.18s, background 0.18s, border-color 0.18s;
}
.detail-back:hover {
  background: #fff;
  color: #6d28d9;
  border-color: #6d28d9;
  transform: translateX(-2px);
  box-shadow: 0 8px 20px rgba(139, 92, 246, 0.28);
}
.detail-back:active {
  transform: translateX(0) scale(0.98);
}

.breadcrumb {
  margin-bottom: 0;
}

.detail-box {
  display: grid;
  grid-template-columns: minmax(320px, 440px) 1fr;
  gap: 26px;
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

.image-panel {
  height: 360px;
}

.main-image,
.placeholder {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  display: block;
}

.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  color: #b9c1cc;
}

.info-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-title {
  color: #1f2937;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.4;
}

.info-line {
  display: flex;
  gap: 8px;
}

.price-box {
  background: #fff7e6;
  border: 1px solid #ffe7ba;
  border-radius: 8px;
  padding: 14px 16px;
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.price-label {
  color: #8a94a6;
  font-size: 13px;
}

.price {
  color: #d4380d;
  font-size: 28px;
  font-weight: 800;
}

.descriptions {
  max-width: 460px;
}

.buy-zone {
  margin-top: auto;
}

.cart-btn {
  min-width: 150px;
  margin-left: 12px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.cart-btn .cart-ico { font-size: 17px; }

.buy-btn {
  min-width: 220px;
}

.desc-card {
  margin-top: 18px;
  border-radius: 8px;
}

.comment-card {
  margin-top: 18px;
  border-radius: 8px;
}

.comment-head {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
}

.comment-input {
  margin-bottom: 16px;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.comment-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 14px 0;
  border-top: 1px dashed var(--border-soft);
}

.comment-item:first-child {
  border-top: none;
}

.comment-main {
  flex: 1;
  min-width: 0;
}

.comment-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.comment-name {
  font-weight: 600;
  color: var(--text-main);
}

.comment-time {
  font-size: 12px;
  color: var(--text-faint);
}

.comment-content {
  margin: 6px 0 0;
  color: var(--text-sub);
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.description {
  color: #4b5563;
  line-height: 1.9;
  white-space: pre-wrap;
}

@media (max-width: 800px) {
  .detail-box {
    grid-template-columns: 1fr;
  }

  .image-panel {
    height: 260px;
  }
}
</style>
