<template>
  <div class="my-orders-page">
    <div class="page-head">
      <div>
        <h2>我的购买订单</h2>
        <p>查看我在平台购买商品生成的订单记录</p>
      </div>
      <el-button @click="loadOrders">
        <el-icon><Refresh /></el-icon>
        <span>刷新</span>
      </el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="orders" stripe>
        <el-table-column prop="orderNo" label="订单编号" width="200" />
        <el-table-column label="商品" min-width="240">
          <template #default="{ row }">
            <div class="product-cell" @click="goDetail(row.productId)">
              <el-image v-if="row.productCover" :src="row.productCover" fit="cover" class="thumb" />
              <div v-else class="thumb placeholder">
                <el-icon><Picture /></el-icon>
              </div>
              <span class="product-title">{{ row.productTitle }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="orderStatusTag(row.status).type">{{ orderStatusTag(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PAID'">
              <el-button type="warning" link @click="applyCancel(row)">取消订单</el-button>
              <el-button type="danger" link @click="applyRefund(row)">申请退款</el-button>
            </template>
            <el-tag v-else type="info" effect="plain" size="small">暂无操作</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Picture, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '../api'
import { orderStatusTag } from '../utils/status'

const router = useRouter()
const orders = ref([])
const loading = ref(false)

async function loadOrders() {
  loading.value = true
  try {
    orders.value = await orderApi.mine()
  } finally {
    loading.value = false
  }
}

function goDetail(id) {
  router.push(`/products/${id}`)
}

async function applyCancel(row) {
  try {
    await ElMessageBox.confirm(`确定取消订单 ${row.orderNo} 吗？取消后商品将恢复可售。`, '取消订单', {
      type: 'warning',
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想'
    })
    await orderApi.cancel(row.id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      // 接口错误已由请求层提示
    }
  }
}

async function applyRefund(row) {
  try {
    await ElMessageBox.confirm(`确定对订单 ${row.orderNo} 申请退款吗？提交后需管理员审核。`, '申请退款', {
      type: 'warning',
      confirmButtonText: '确认申请',
      cancelButtonText: '再想想'
    })
    await orderApi.refund(row.id)
    ElMessage.success('退款申请已提交，等待管理员处理')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      // 接口错误已由请求层提示
    }
  }
}

onMounted(loadOrders)
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
  color: #1f2937;
  font-size: 22px;
}

.page-head p {
  margin: 0;
  color: #8a94a6;
  font-size: 13px;
}

.table-card {
  border-radius: 8px;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.thumb {
  width: 52px;
  height: 52px;
  border-radius: 6px;
  flex-shrink: 0;
}

.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  color: #b9c1cc;
}

.product-title {
  color: #1f2937;
}

.price {
  color: #d4380d;
  font-weight: 600;
}
</style>
