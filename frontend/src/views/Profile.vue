<template>
  <div class="profile-page">
    <el-card shadow="never" class="profile-card">
      <div class="profile-head">
        <el-avatar :size="72" :src="store.user?.avatar">
          {{ store.user?.nickname?.slice(0, 1) }}
        </el-avatar>
        <div>
          <h2>{{ store.user?.nickname }}</h2>
          <p>@{{ store.user?.username }}</p>
        </div>
      </div>

      <el-descriptions :column="1" border class="descriptions">
        <el-descriptions-item label="用户ID">{{ store.user?.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ store.user?.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ store.user?.nickname }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="store.isAdmin ? 'warning' : 'success'">
            {{ store.isAdmin ? '平台管理员' : '普通用户' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ store.user?.createTime }}</el-descriptions-item>
      </el-descriptions>

      <!-- 每月修改次数额度 -->
      <div class="limit-box">
        <div class="limit-info">
          <div>
            <div class="limit-title">本月修改额度</div>
            <div class="limit-desc">
              {{ limit.remaining > 0
                ? `本月还可修改 ${limit.remaining} 次（信息 / 密码共用）`
                : '本月修改次数已达上限（2次），请下月再试' }}
            </div>
          </div>
          <div class="limit-num">{{ limit.used }} / {{ limit.total }}</div>
        </div>
        <el-progress
          :percentage="limitPercentage"
          :show-text="false"
          :stroke-width="8"
          :color="limit.remaining > 0 ? '#2f6bff' : '#ef4444'"
        />
      </div>

      <div class="actions">
        <el-button type="primary" class="gradient-btn" @click="openProfileEdit">
          修改个人信息
        </el-button>
        <el-button type="warning" plain @click="openPasswordEdit">修改密码</el-button>
        <el-button v-if="store.isAdmin" type="primary" @click="router.push('/admin')">
          进入管理后台
        </el-button>
        <el-button @click="refreshAll">
          <el-icon><Refresh /></el-icon>
          <span>刷新信息</span>
        </el-button>
      </div>
    </el-card>

    <!-- 编辑个人信息 -->
    <el-dialog v-model="profileVisible" title="修改个人信息" width="440px" destroy-on-close>
      <el-alert
        v-if="limit.remaining <= 0"
        type="error"
        :closable="false"
        show-icon
        title="本月修改次数已达上限"
        description="修改次数用完后，本月的信息与密码将无法修改，请下月再试。"
        class="limit-alert"
      />
      <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="70px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="profileForm.nickname" maxlength="50" />
        </el-form-item>
        <el-form-item label="头像" prop="avatar">
          <div class="avatar-edit">
            <el-avatar :size="56" :src="profileForm.avatar">
              {{ (profileForm.nickname || '?').slice(0, 1) }}
            </el-avatar>
            <el-input v-model="profileForm.avatar" placeholder="头像图片地址（选填）" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileVisible = false">取消</el-button>
        <el-button type="primary" :disabled="limit.remaining <= 0" :loading="savingProfile" @click="saveProfile">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改密码 -->
    <el-dialog v-model="passwordVisible" title="修改密码" width="440px" destroy-on-close>
      <el-alert
        v-if="limit.remaining <= 0"
        type="error"
        :closable="false"
        show-icon
        title="本月修改次数已达上限"
        description="修改次数用完后，本月的信息与密码将无法修改，请下月再试。"
        class="limit-alert"
      />
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordVisible = false">取消</el-button>
        <el-button type="warning" :disabled="limit.remaining <= 0" :loading="savingPassword" @click="savePassword">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { userApi } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const store = useUserStore()

const limit = reactive({ used: 0, total: 2, remaining: 2 })

const limitPercentage = computed(() => {
  if (!limit.total) return 0
  return Math.round((limit.used / limit.total) * 100)
})

// 个人信息
const profileVisible = ref(false)
const savingProfile = ref(false)
const profileFormRef = ref()
const profileForm = reactive({ nickname: '', avatar: '' })
const profileRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

// 密码
const passwordVisible = ref(false)
const savingPassword = ref(false)
const passwordFormRef = ref()
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '新密码长度需在6-32个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

async function loadLimit() {
  try {
    const data = await userApi.profileLimit()
    limit.used = data.used
    limit.total = data.total
    limit.remaining = data.remaining
  } catch {
    // 忽略
  }
}

function openProfileEdit() {
  if (limit.remaining <= 0) {
    ElMessage.warning('本月修改次数已达上限，无法修改')
    return
  }
  profileForm.nickname = store.user?.nickname || ''
  profileForm.avatar = store.user?.avatar || ''
  profileVisible.value = true
}

async function saveProfile() {
  try {
    await profileFormRef.value.validate()
  } catch {
    return
  }
  savingProfile.value = true
  try {
    const user = await userApi.updateProfile({
      nickname: profileForm.nickname,
      avatar: profileForm.avatar || null
    })
    store.user = user
    localStorage.setItem('user', JSON.stringify(user))
    ElMessage.success('个人信息修改成功')
    profileVisible.value = false
    loadLimit()
  } finally {
    savingProfile.value = false
  }
}

function openPasswordEdit() {
  if (limit.remaining <= 0) {
    ElMessage.warning('本月修改次数已达上限，无法修改')
    return
  }
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordVisible.value = true
}

async function savePassword() {
  try {
    await passwordFormRef.value.validate()
  } catch {
    return
  }
  savingPassword.value = true
  try {
    await userApi.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请使用新密码登录')
    passwordVisible.value = false
    loadLimit()
  } finally {
    savingPassword.value = false
  }
}

async function refreshAll() {
  await store.fetchMe()
  await loadLimit()
  ElMessage.success('信息已刷新')
}

onMounted(() => {
  store.fetchMe()
  loadLimit()
})
</script>

<style scoped>
.profile-page {
  max-width: 780px;
}

.profile-card {
  border-radius: 14px;
}

.profile-head {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 10px 0 24px;
}

.profile-head h2 {
  margin: 0 0 4px;
  color: var(--text-main);
  font-size: 20px;
}

.profile-head p {
  margin: 0;
  color: var(--text-faint);
  font-size: 13px;
}

.descriptions {
  margin-bottom: 20px;
}

.limit-box {
  background: linear-gradient(120deg, #eef4ff, #f7faff);
  border: 1px solid rgba(47, 107, 255, 0.16);
  border-radius: 12px;
  padding: 14px 18px;
  margin-bottom: 20px;
}

.limit-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.limit-title {
  font-weight: 700;
  color: var(--text-main);
  font-size: 14px;
}

.limit-desc {
  color: var(--text-sub);
  font-size: 12px;
  margin-top: 2px;
}

.limit-num {
  font-size: 20px;
  font-weight: 800;
  color: var(--brand);
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.avatar-edit {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.limit-alert {
  margin-bottom: 14px;
}
</style>
