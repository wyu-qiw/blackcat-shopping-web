<template>
  <div class="my-products-page">
    <div class="section-head">
      <div>
        <h2>我的商品</h2>
        <p>管理自己上架的商品：编辑信息、上架 / 下架</p>
      </div>
      <el-button type="primary" class="gradient-btn" @click="router.push('/publish')">
        <el-icon><Plus /></el-icon>
        <span>发布新商品</span>
      </el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="products" stripe>
        <el-table-column label="商品" min-width="240">
          <template #default="{ row }">
            <div class="product-cell" @click="goDetail(row.id)">
              <el-image v-if="row.coverImage" :src="row.coverImage" fit="cover" class="thumb" />
              <div v-else class="thumb placeholder">
                <el-icon><Picture /></el-icon>
              </div>
              <span class="product-title">{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.categoryName" type="primary" effect="plain">{{ row.categoryName }}</el-tag>
            <span v-else>未分类</span>
          </template>
        </el-table-column>
        <el-table-column label="售价" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column label="商品状态" width="105">
          <template #default="{ row }">
            <el-tag :type="productStatusTag(row.status).type">{{ productStatusTag(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上架状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.listed ? 'success' : 'info'">{{ row.listed ? '已上架' : '已下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="160" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row.id)">详情</el-button>
            <el-button type="warning" link @click="openEdit(row)">编辑</el-button>
            <el-button :type="row.listed ? 'danger' : 'success'" link @click="toggleListed(row)">
              {{ row.listed ? '下架' : '上架' }}
            </el-button>
            <el-button type="danger" link @click="removeProduct(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑商品对话框 -->
    <el-dialog v-model="editVisible" title="编辑商品" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="editForm" :rules="editRules" label-width="90px">
        <el-form-item label="商品名称" prop="title">
          <el-input v-model="editForm.title" maxlength="100" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="售价" prop="price">
          <el-input-number v-model="editForm.price" :min="0.01" :precision="2" :step="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="editForm.categoryId" placeholder="请选择分类" style="width: 220px">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌">
          <el-select v-model="editForm.brandId" placeholder="留空则自动识别" clearable style="width: 220px">
            <el-option v-for="b in brands" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品图片" prop="images">
          <MultiImageUpload v-model="editForm.images" :max-count="8" />
          <span class="img-hint">最多 8 张，第一张为封面；可“设为封面”切换</span>
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="editForm.description" type="textarea" :rows="4" placeholder="请输入商品描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Plus } from '@element-plus/icons-vue'
import { brandApi, categoryApi, productApi } from '../api'
import { productStatusTag } from '../utils/status'
import MultiImageUpload from '../components/MultiImageUpload.vue'

const router = useRouter()
const products = ref([])
const categories = ref([])
const brands = ref([])
const loading = ref(false)

const editVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const editForm = reactive({
  id: null,
  title: '',
  price: 0,
  description: '',
  images: [],
  categoryId: null,
  brandId: null
})

const editRules = {
  title: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  images: [{
    validator: (rule, value, callback) => {
      if (!value || !value.length) callback(new Error('请至少保留一张商品图片'))
      else callback()
    },
    trigger: 'change'
  }],
  description: [{ required: true, message: '请输入商品描述', trigger: 'blur' }]
}

async function loadProducts() {
  loading.value = true
  try {
    products.value = await productApi.mine()
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    const [catList, brandList] = await Promise.all([
      categoryApi.list(),
      brandApi.list()
    ])
    categories.value = catList
    brands.value = brandList
  } catch {
    categories.value = []
    brands.value = []
  }
}

function goDetail(id) {
  router.push(`/products/${id}`)
}

function openEdit(row) {
  Object.assign(editForm, {
    id: row.id,
    title: row.title,
    price: Number(row.price),
    description: row.description,
    images: toImageList(row),
    categoryId: row.categoryId,
    brandId: row.brandId
  })
  editVisible.value = true
}

// 兼容旧数据：优先用 images 数组，没有则用单张封面兜底
function toImageList(row) {
  if (Array.isArray(row.images) && row.images.length) {
    return [...row.images]
  }
  return row.coverImage ? [row.coverImage] : []
}

async function saveEdit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  saving.value = true
  try {
    await productApi.update(editForm.id, {
      title: editForm.title,
      price: editForm.price,
      description: editForm.description,
      images: editForm.images,
      categoryId: editForm.categoryId,
      brandId: editForm.brandId
    })
    ElMessage.success('商品信息修改成功')
    editVisible.value = false
    loadProducts()
  } finally {
    saving.value = false
  }
}

async function toggleListed(row) {
  const action = row.listed ? '下架' : '上架'
  try {
    await ElMessageBox.confirm(`确定${action}“${row.title}”吗？`, '提示', { type: 'warning' })
    await productApi.toggleListed(row.id, !row.listed)
    ElMessage.success(`${action}成功`)
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      // 接口错误已由请求层提示
    }
  }
}

async function removeProduct(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除商品“${row.title}”吗？删除后其价格记录与评论将一并清理，且无法恢复。`,
      '删除商品',
      { type: 'warning', confirmButtonText: '确认删除', confirmButtonClass: 'el-button--danger' }
    )
    await productApi.remove(row.id)
    ElMessage.success('商品删除成功')
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      // 接口错误已由请求层提示
    }
  }
}

onMounted(() => {
  loadProducts()
  loadCategories()
})
</script>

<style scoped>
.table-card {
  border-radius: 14px;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.thumb {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  flex-shrink: 0;
}

.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f4ff;
  color: #b9c1cc;
}

.product-title {
  color: var(--text-main);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.price {
  color: var(--brand);
  font-weight: 700;
}

.cover-edit {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cover-preview {
  width: 90px;
  height: 90px;
  border-radius: 10px;
  border: 1px solid var(--border-soft);
}
</style>
