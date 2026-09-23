<template>
  <div class="publish-page">
    <el-card shadow="never" class="publish-card">
      <template #header>
        <div class="card-header">
          <span>发布出售商品</span>
          <span class="tip">发布后商品自动上架，初始状态为“可售”</span>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px" class="publish-form">
        <el-form-item label="商品名称" prop="title">
          <el-input
            v-model="form.title"
            maxlength="100"
            show-word-limit
            placeholder="请输入商品名称"
          />
        </el-form-item>

        <el-form-item label="售价" prop="price">
          <el-input-number
            v-model="form.price"
            :min="0.01"
            :precision="2"
            :step="1"
            controls-position="right"
            style="width: 220px"
          />
          <span class="unit">元</span>
        </el-form-item>

        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择商品分类" style="width: 260px">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="品牌">
          <el-select
            v-model="form.brandId"
            placeholder="不选则按名称/描述自动识别"
            clearable
            style="width: 260px"
          >
            <el-option v-for="b in brands" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
          <span class="brand-hint">分类与品牌相互独立，可同时归属</span>
        </el-form-item>

        <el-form-item label="商品图片" prop="images">
          <MultiImageUpload v-model="form.images" :max-count="8" />
          <span class="img-hint">最多 8 张，第一张为封面；点击图片下方按钮可设为封面</span>
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <div class="ai-desc-bar">
            <el-button
              type="primary"
              plain
              size="small"
              :loading="aiGenerating"
              :disabled="!canGenerate"
              @click="generateDescription"
            >
              <el-icon style="margin-right: 4px"><MagicStick /></el-icon>
              AI 生成描述
            </el-button>
            <span class="ai-desc-tip">自动读取商品名称 / 分类 / 售价生成</span>
          </div>
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="7"
            maxlength="2000"
            show-word-limit
            placeholder="请描述商品新旧程度、规格、使用情况与交易方式"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">发布商品</el-button>
          <el-button @click="router.push('/')">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick } from '@element-plus/icons-vue'
import { aiApi, brandApi, categoryApi, productApi } from '../api'
import MultiImageUpload from '../components/MultiImageUpload.vue'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const aiGenerating = ref(false)

const canGenerate = () => form.title.trim() && form.categoryId != null

async function generateDescription() {
  if (!canGenerate()) {
    ElMessage.warning('请先填写商品名称与商品分类')
    return
  }
  if (form.description.trim() && !(await confirmOverwrite())) return

  aiGenerating.value = true
  try {
    const categoryName = categories.value.find((c) => c.id === form.categoryId)?.name || ''
    const description = await aiApi.describe({
      title: form.title,
      category: categoryName,
      price: form.price != null ? String(form.price) : '',
      sellPoints: ''
    })
    form.description = description
    ElMessage.success('AI 描述已生成')
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || '生成失败，请稍后再试')
  } finally {
    aiGenerating.value = false
  }
}

function confirmOverwrite() {
  return new Promise((resolve) => {
    ElMessageBox.confirm('将覆盖当前已有的描述，是否继续？', '提示', { type: 'warning' })
      .then(() => resolve(true))
      .catch(() => resolve(false))
  })
}
const categories = ref([])
const brands = ref([])

const form = reactive({
  title: '',
  price: undefined,
  images: [],
  description: '',
  categoryId: null,
  brandId: null
})

const rules = {
  title: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { max: 100, message: '商品名称最长100个字符', trigger: 'blur' }
  ],
  price: [{ required: true, message: '请选择售价', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  images: [
    {
      validator: (rule, value, callback) => {
        if (!value || value.length === 0) {
          callback(new Error('请至少上传一张商品图片'))
        } else if (value.length > 8) {
          callback(new Error('商品图片最多8张'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  description: [{ required: true, message: '请输入商品描述', trigger: 'blur' }]
}

async function loadOptions() {
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

async function submit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    await productApi.publish({ ...form })
    ElMessage.success('发布成功')
    router.push('/my/products')
  } finally {
    submitting.value = false
  }
}

onMounted(loadOptions)
</script>

<style scoped>
.publish-page {
  max-width: 860px;
}

.publish-card {
  border-radius: 14px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  color: #1f2937;
}

.tip {
  color: #8a94a6;
  font-size: 13px;
  font-weight: 400;
}

.publish-form {
  max-width: 640px;
}

.unit {
  margin-left: 10px;
  color: #6b7280;
}

.brand-hint {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #94a3b8;
}

.img-hint {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: #94a3b8;
}

.ai-desc-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.ai-desc-tip {
  color: #94a3b8;
  font-size: 12px;
}
</style>
