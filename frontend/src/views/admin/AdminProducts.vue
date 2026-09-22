<template>
  <div class="admin-products-page">
    <div class="page-head">
      <div>
        <h2>{{ scopeTitle }}</h2>
        <p>{{ scopeDesc }}</p>
      </div>
      <el-button @click="loadProducts">
        <el-icon><Refresh /></el-icon>
        <span>刷新</span>
      </el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="products" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="封面" width="90">
          <template #default="{ row }">
            <el-image v-if="row.coverImage" :src="row.coverImage" fit="cover" class="thumb" />
            <div v-else class="thumb placeholder">
              <el-icon><Picture /></el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="商品名称" min-width="170" show-overflow-tooltip />
        <el-table-column prop="sellerName" label="卖家" width="120" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>

        <!-- 可移动：商品分类 -->
        <el-table-column label="商品分类" width="150">
          <template #default="{ row }">
            <el-select
              :model-value="row.categoryId"
              size="small"
              class="move-select"
              @change="handleCategoryMove(row, $event)"
            >
              <el-option
                v-for="c in categories"
                :key="c.id"
                :label="c.name"
                :value="c.id"
              />
            </el-select>
          </template>
        </el-table-column>

        <!-- 可移动：品牌（可设为无品牌） -->
        <el-table-column label="品牌" width="160">
          <template #default="{ row }">
            <el-select
              :model-value="row.brandId"
              size="small"
              class="move-select"
              clearable
              placeholder="无品牌"
              @change="handleBrandMove(row, $event)"
            >
              <el-option
                v-for="b in brands"
                :key="b.id"
                :label="b.name"
                :value="b.id"
              />
            </el-select>
          </template>
        </el-table-column>

        <el-table-column label="商品状态" width="135">
          <template #default="{ row }">
            <el-select
              :model-value="row.status"
              size="small"
              @change="handleStatusChange(row, $event)"
            >
              <el-option
                v-for="option in productStatusOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="上架状态" width="95">
          <template #default="{ row }">
            <el-tag :type="row.listed ? 'success' : 'info'">{{ row.listed ? '已上架' : '已下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="info" link @click="goDetail(row.id)">查看</el-button>
            <el-button v-if="row.listed" type="warning" link @click="toggleListed(row, false)">下架</el-button>
            <el-button v-else type="success" link @click="toggleListed(row, true)">上架</el-button>
            <el-button type="danger" link @click="removeProduct(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 管理员编辑任意商品弹窗 -->
    <el-dialog v-model="editVisible" title="编辑商品（管理员）" width="620px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="92px">
        <el-form-item label="商品名称" prop="title">
          <el-input v-model="editForm.title" maxlength="100" show-word-limit placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="售价" prop="price">
          <el-input-number v-model="editForm.price" :min="0.01" :precision="2" :step="1" controls-position="right" style="width: 220px" />
          <span class="unit">元</span>
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="editForm.categoryId" placeholder="请选择分类" style="width: 240px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌">
          <el-select v-model="editForm.brandId" placeholder="留空则自动识别" clearable style="width: 240px">
            <el-option v-for="b in brands" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品图片" prop="images">
          <MultiImageUpload v-model="editForm.images" :max-count="8" />
          <span class="img-hint">最多 8 张，第一张为封面；可“设为封面”切换（与卖家权限一致）</span>
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="5"
            maxlength="2000"
            show-word-limit
            placeholder="请输入商品描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveEdit">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Refresh } from '@element-plus/icons-vue'
import { adminApi, brandApi, categoryApi } from '../../api'
import { productStatusOptions } from '../../utils/status'
import MultiImageUpload from '../../components/MultiImageUpload.vue'

const route = useRoute()
const router = useRouter()
const products = ref([])
const categories = ref([])
const brands = ref([])
const loading = ref(false)

// 左侧菜单的“商品分类 / 品牌分类”通过路由 query 驱动筛选
const categoryId = computed(() =>
  route.query.cat != null && route.query.cat !== '' ? Number(route.query.cat) : null
)
const brandId = computed(() =>
  route.query.brand != null && route.query.brand !== '' ? Number(route.query.brand) : null
)

const scopeTitle = computed(() => {
  if (categoryId.value) {
    return categories.value.find((c) => c.id === categoryId.value)?.name || '商品分类'
  }
  if (brandId.value) {
    return brands.value.find((b) => b.id === brandId.value)?.name || '品牌分类'
  }
  return '全平台商品管理'
})

const scopeDesc = computed(() => {
  if (categoryId.value) {
    return `商品分类「${scopeTitle.value}」下的全部商品，共 ${products.value.length} 件`
  }
  if (brandId.value) {
    return `品牌「${scopeTitle.value}」的全部商品，共 ${products.value.length} 件`
  }
  return '查看所有商品；可编辑任意商品，并直接在“商品分类 / 品牌”列移动商品、修改状态与上下架'
})

// ===== 管理员编辑任意商品 =====
const editVisible = ref(false)
const saving = ref(false)
const editFormRef = ref()
const editForm = reactive({
  id: null,
  title: '',
  price: 0,
  categoryId: null,
  brandId: null,
  images: [],
  description: ''
})
const editRules = {
  title: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  images: [{
    validator: (rule, value, callback) => {
      if (!value || !value.length) callback(new Error('请至少保留一张商品图片'))
      else callback()
    },
    trigger: 'change'
  }],
  description: [{ required: true, message: '请输入商品描述', trigger: 'blur' }]
}

function openEdit(row) {
  Object.assign(editForm, {
    id: row.id,
    title: row.title,
    price: Number(row.price),
    categoryId: row.categoryId,
    brandId: row.brandId,
    images: Array.isArray(row.images) && row.images.length
      ? [...row.images]
      : (row.coverImage ? [row.coverImage] : []),
    description: row.description
  })
  editVisible.value = true
}


async function saveEdit() {
  try {
    await editFormRef.value.validate()
  } catch {
    return
  }
  saving.value = true
  try {
    // 复用用户端商品更新接口；后端对管理员放行（checkOwnerOrAdmin）
    await adminApi.productUpdate(editForm.id, {
      title: editForm.title,
      price: editForm.price,
      categoryId: editForm.categoryId,
      brandId: editForm.brandId,
      images: editForm.images,
      description: editForm.description
    })
    ElMessage.success('商品信息修改成功')
    editVisible.value = false
    loadProducts()
  } finally {
    saving.value = false
  }
}

async function loadMeta() {
  try {
    const [catList, brandList] = await Promise.all([
      categoryApi.list(),
      brandApi.list()
    ])
    categories.value = catList
    brands.value = brandList
  } catch {
    // 请求层已提示
  }
}

async function loadProducts() {
  loading.value = true
  try {
    const params = {}
    if (categoryId.value) params.categoryId = categoryId.value
    if (brandId.value) params.brandId = brandId.value
    products.value = await adminApi.products(
      Object.keys(params).length ? params : undefined
    )
  } finally {
    loading.value = false
  }
}

// 移动商品分类（即时保存，不影响品牌）
async function handleCategoryMove(row, targetCategoryId) {
  try {
    await adminApi.moveCategory(row.id, targetCategoryId)
    row.categoryId = targetCategoryId
    ElMessage.success('商品分类已调整')
    // 若当前正按某分类筛选，移走后从当前列表移除该行
    if (categoryId.value && categoryId.value !== targetCategoryId) {
      loadProducts()
    }
  } catch {
    loadProducts()
  }
}

// 移动品牌（即时保存，不影响分类；清空 = 无品牌）
async function handleBrandMove(row, targetBrandId) {
  try {
    await adminApi.moveBrand(row.id, targetBrandId ?? null)
    row.brandId = targetBrandId ?? null
    ElMessage.success(targetBrandId ? '商品品牌已调整' : '已设为无品牌')
    if (brandId.value && brandId.value !== targetBrandId) {
      loadProducts()
    }
  } catch {
    loadProducts()
  }
}

async function handleStatusChange(row, status) {
  try {
    await adminApi.updateProductStatus(row.id, status)
    row.status = status
    ElMessage.success('商品状态已更新')
  } catch {
    loadProducts()
  }
}

async function toggleListed(row, listed) {
  const action = listed ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确定${action}商品“${row.title}”吗？`, `${action}商品`, {
      type: 'warning',
      confirmButtonText: action,
      cancelButtonText: '取消'
    })
    await adminApi.updateProductListed(row.id, listed)
    row.listed = listed
    ElMessage.success(`商品已${action}`)
  } catch (error) {
    if (error !== 'cancel') {
      loadProducts()
    }
  }
}

async function removeProduct(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除商品“${row.title}”吗？删除后其价格记录与评论将一并清理，且无法恢复。`,
      '删除商品',
      { type: 'warning', confirmButtonText: '确认删除' }
    )
    await adminApi.productsRemove(row.id)
    ElMessage.success('商品删除成功')
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      // 接口错误已由请求层提示
    }
  }
}

function goDetail(id) {
  router.push(`/products/${id}`)
}

// 在商品分类 / 品牌菜单项之间切换时重新加载
watch([categoryId, brandId], () => loadProducts())

onMounted(async () => {
  await loadMeta()
  await loadProducts()
})
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

.move-select {
  width: 100%;
}

.thumb {
  width: 52px;
  height: 52px;
  border-radius: 6px;
  display: block;
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

.unit {
  margin-left: 10px;
  color: #6b7280;
}

.cover-edit {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cover-preview {
  width: 88px;
  height: 88px;
  border-radius: 10px;
}
</style>