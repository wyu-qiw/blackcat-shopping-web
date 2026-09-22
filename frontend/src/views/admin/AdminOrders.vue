<template>
  <div class="admin-orders-page">
    <div class="page-head">
      <h2>全平台订单</h2>
      <p>查看所有用户产生的交易订单，共 {{ orders.length }} 笔</p>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="orders" stripe>
        <el-table-column prop="orderNo" label="订单编号" width="210" />
        <el-table-column label="商品" min-width="220">
          <template #default="{ row }">
            <div class="product-cell">
              <el-image v-if="row.productCover" :src="row.productCover" fit="cover" class="thumb" />
              <div v-else class="thumb placeholder">
                <el-icon><Picture /></el-icon>
              </div>
              <span>{{ row.productTitle || '已删除商品' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="buyerName" label="买家" width="130" />
        <el-table-column prop="sellerName" label="卖家" width="130" />
        <el-table-column label="金额" width="110">
          <template #default="{ row }">
            <span class="price">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="orderStatusTag(row.status).type">{{ orderStatusTag(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <div class="op-cell">
              <!-- 撤销：PAID / REFUNDED 可撤销；REFUNDING / CANCELLED 不可撤销 -->
              <el-button
                v-if="canCancel(row.status)"
                type="warning"
                link
                @click="handleCancel(row)"
              >
                <el-icon><RefreshLeft /></el-icon>
                <span>撤销</span>
              </el-button>

              <!-- 删除：仅 CANCELLED / REFUNDED 可删除 -->
              <el-button
                v-if="canDelete(row.status)"
                type="danger"
                link
                @click="handleDelete(row)"
              >
                <el-icon><Delete /></el-icon>
                <span>删除</span>
              </el-button>

              <!-- 退款处理 -->
              <template v-if="row.status === 'REFUNDING'">
                <el-button type="success" link @click="handleRefund(row, true)">同意退款</el-button>
                <el-button type="danger" link @click="handleRefund(row, false)">驳回</el-button>
              </template>

              <el-tag v-if="!canCancel(row.status) && !canDelete(row.status) && row.status !== 'REFUNDING'" type="info" effect="plain" size="small">
                暂无操作
              </el-tag>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Delete, Picture, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../../api'
import { orderStatusTag } from '../../utils/status'

const orders = ref([])
const loading = ref(false)

// 撤销逻辑：PAID（已支付）和 REFUNDED（已退款）的订单可以撤销为 CANCELLED
function canCancel(status) {
  return status === 'PAID' || status === 'REFUNDED'
}
// 删除逻辑：只有已撤销（CANCELLED）或已退款（REFUNDED）的订单允许物理删除
function canDelete(status) {
  return status === 'CANCELLED' || status === 'REFUNDED'
}

async function loadOrders() {
  loading.value = true
  try {
    orders.value = await adminApi.orders()
  } finally {
    loading.value = false
  }
}

/** 撤销订单：订单状态变更为"已取消"，商品恢复"可售" */
async function handleCancel(row) {
  try {
    await ElMessageBox.confirm(
      `确定撤销订单 ${row.orderNo} 吗？撤销后该订单将变为"已取消"，关联商品恢复可售。`,
      '撤销订单',
      { type: 'warning', confirmButtonText: '确认撤销', cancelButtonText: '取消' }
    )
    await adminApi.cancelOrder(row.id)
    ElMessage.success('订单已撤销，商品已恢复可售')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      // 业务错误已由请求拦截器统一提示
    }
  }
}

/** 删除订单：物理删除，需二次确认 */
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要彻底删除订单 ${row.orderNo} 吗？\n删除后无法恢复，请谨慎操作！`,
      '删除订单（不可恢复）',
      {
        type: 'error',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
    // 二次确认
    try {
      await ElMessageBox.confirm(
        `最后确认：是否删除订单 ${row.orderNo}？`,
        '再次确认',
        { type: 'error', confirmButtonText: '我确定要删除', cancelButtonText: '再想想' }
      )
    } catch {
      ElMessage.info('已取消删除')
      return
    }
    await adminApi.deleteOrder(row.id)
    ElMessage.success('订单已删除')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      // 业务错误已由请求拦截器统一提示
    }
  }
}

async function handleRefund(row, approved) {
  const text = approved ? '同意' : '驳回'
  try {
    await ElMessageBox.confirm(
      `${text}订单 ${row.orderNo} 的退款申请吗？`,
      approved ? '同意退款' : '驳回退款',
      { type: 'warning', confirmButtonText: '确认' }
    )
    await adminApi.handleRefund(row.id, approved)
    ElMessage.success(approved ? '退款已通过，商品恢复可售' : '退款已驳回')
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
}
.thumb {
  width: 48px;
  height: 48px;
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
.price {
  color: #d4380d;
  font-weight: 600;
}
.op-cell {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}
</style>