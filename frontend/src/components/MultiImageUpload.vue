<template>
  <div class="multi-upload">
    <!-- 已上传图片缩略图：第一张带“封面”角标，可设封面/删除 -->
    <div
      v-for="(url, index) in modelValue"
      :key="`${url}__${index}`"
      class="img-card"
      :class="{ cover: index === 0 }"
    >
      <el-image :src="url" fit="cover" class="img-thumb" />
      <span v-if="index === 0" class="cover-badge">封面</span>
      <div class="img-mask">
        <el-icon
          v-if="index !== 0"
          class="mask-btn"
          title="设为封面"
          @click="makeCover(index)"
        >
          <Star />
        </el-icon>
        <el-icon class="mask-btn danger" title="删除" @click="remove(index)">
          <Delete />
        </el-icon>
      </div>
    </div>

    <!-- 上传按钮：未满 8 张时显示 -->
    <el-upload
      v-if="modelValue.length < maxCount"
      :show-file-list="false"
      :multiple="true"
      accept="image/*"
      :http-request="handleUpload"
      class="upload-trigger"
    >
      <div class="upload-box" :class="{ disabled: uploading }">
        <el-icon :size="22"><Plus /></el-icon>
        <span class="upload-text">{{ uploading ? '上传中…' : '上传图片' }}</span>
        <span class="upload-count">{{ modelValue.length }}/{{ maxCount }}</span>
      </div>
    </el-upload>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete, Plus, Star } from '@element-plus/icons-vue'
import { uploadFile } from '../api'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  maxCount: {
    type: Number,
    default: 8
  }
})
const emit = defineEmits(['update:modelValue'])

const uploading = ref(false)

async function handleUpload(options) {
  if (props.modelValue.length >= props.maxCount) {
    ElMessage.warning(`最多只能上传${props.maxCount}张图片`)
    return
  }
  uploading.value = true
  try {
    const url = await uploadFile(options.file)
    // 若短时间内已达上限则不再追加
    if (props.modelValue.length < props.maxCount) {
      emit('update:modelValue', [...props.modelValue, url])
    }
  } catch {
    // 请求层已提示
  } finally {
    uploading.value = false
  }
}

// 把指定图片移动到第一张作为封面
function makeCover(index) {
  const list = [...props.modelValue]
  const [target] = list.splice(index, 1)
  list.unshift(target)
  emit('update:modelValue', list)
  ElMessage.success('已切换封面')
}

function remove(index) {
  const list = props.modelValue.filter((_, i) => i !== index)
  emit('update:modelValue', list)
}
</script>


<style scoped>
.multi-upload {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.img-card {
  position: relative;
  width: 104px;
  height: 104px;
  border-radius: 10px;
  overflow: hidden;
  border: 2px solid #e5edff;
}
.img-card.cover {
  border-color: #2563eb;
  box-shadow: 0 6px 16px -6px rgba(37, 99, 235, 0.6);
}

.img-thumb {
  width: 100%;
  height: 100%;
  display: block;
}

.cover-badge {
  position: absolute;
  top: 0;
  left: 0;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(120deg, #2563eb, #38bdf8);
  border-bottom-right-radius: 8px;
}

.img-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: rgba(15, 23, 42, 0.42);
  opacity: 0;
  transition: opacity 0.18s;
}
.img-card:hover .img-mask { opacity: 1; }

.mask-btn {
  color: #fff;
  font-size: 19px;
  cursor: pointer;
}
.mask-btn:hover { color: #38bdf8; }
.mask-btn.danger:hover { color: #f87171; }

.upload-trigger :deep(.el-upload) {
  width: 104px;
  height: 104px;
}

.upload-box {
  width: 104px;
  height: 104px;
  border: 2px dashed #93b4f5;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  color: #2563eb;
  background: rgba(239, 246, 255, 0.6);
  cursor: pointer;
  transition: border-color 0.18s, background 0.18s;
}
.upload-box:hover {
  border-color: #2563eb;
  background: #eff6ff;
}
.upload-box.disabled { opacity: 0.7; }
.upload-text { font-size: 12px; font-weight: 700; }
.upload-count { font-size: 11px; color: #8a94a6; }
</style>