<template>
  <div class="product-card tech-card" @click="$emit('click')">
    <div class="cover-wrap">
      <el-image v-if="product.coverImage" :src="product.coverImage" fit="cover" class="cover" lazy />
      <div v-else class="cover placeholder">
        <el-icon :size="34"><Picture /></el-icon>
      </div>
      <el-tag class="status-tag" :type="productStatusTag(product.status).type" effect="dark">
        {{ productStatusTag(product.status).label }}
      </el-tag>
      <span v-if="product.categoryName" class="cat-chip">{{ product.categoryName }}</span>
    </div>
    <div class="card-body">
      <div class="card-title" :title="product.title">{{ product.title }}</div>
      <div class="card-meta">
        <span class="seller">
          <el-icon><User /></el-icon>
          {{ product.sellerName || '未知卖家' }}
        </span>
      </div>
      <div class="card-bottom">
        <span class="price">¥{{ product.price }}</span>
        <span class="view-btn">查看详情 →</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Picture, User } from '@element-plus/icons-vue'
import { productStatusTag } from '../utils/status'

defineProps({
  product: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.product-card {
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.28s ease, box-shadow 0.28s ease, border-color 0.28s ease;
}

.product-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 18px 40px rgba(37, 99, 235, 0.28);
  border-color: rgba(37, 99, 235, 0.85) !important;
}

.cover-wrap {
  position: relative;
  height: 176px;
  background: linear-gradient(135deg, #e8f1ff 0%, #f4f8ff 55%, #eaf3fe 100%);
  overflow: hidden;
}

.cover,
.placeholder {
  width: 100%;
  height: 100%;
  display: block;
  transition: transform 0.35s ease;
}

.product-card:hover .cover,
.product-card:hover .placeholder {
  transform: scale(1.04);
}

.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #b9c1cc;
}

.status-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  border: 1.5px solid rgba(255, 255, 255, 0.6) !important;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.25);
  font-weight: 700;
}

.cat-chip {
  position: absolute;
  left: 10px;
  bottom: 10px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  color: var(--brand);
  background: rgba(255, 255, 255, 0.92);
  border: 1.5px solid rgba(37, 99, 235, 0.55);
  backdrop-filter: blur(6px);
  font-weight: 700;
  box-shadow: 0 2px 6px rgba(37, 99, 235, 0.10);
}

.card-body {
  padding: 14px 16px 16px;
}

.card-title {
  color: var(--text-main);
  font-size: 15px;
  font-weight: 600;
  line-height: 1.45;
  height: 44px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-meta {
  margin: 8px 0 12px;
}

.seller {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--text-faint);
  font-size: 13px;
}

.card-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price {
  color: var(--brand);
  font-size: 19px;
  font-weight: 800;
}

.view-btn {
  font-size: 13px;
  color: var(--brand);
  opacity: 0.85;
  transition: opacity 0.2s, transform 0.2s;
}

.product-card:hover .view-btn {
  opacity: 1;
  transform: translateX(2px);
}
</style>
