<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-head">
        <el-icon :size="32" class="brand-icon"><User /></el-icon>
        <h1>注册普通用户</h1>
        <p>注册后即可发布商品、购买平台在售商品</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="3-20个字符" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="选填，默认使用用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="6-32个字符" :prefix-icon="Lock" size="large" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="再次输入密码" :prefix-icon="Lock" size="large" />
        </el-form-item>
        <el-button type="primary" class="submit-btn" size="large" :loading="loading" @click="submit">
          注 册
        </el-button>
      </el-form>

      <div class="auth-footer">
        已有账号？<router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { authApi } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const store = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

function validateConfirm(rule, value, callback) {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度需在3-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度需在6-32个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function submit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const data = await authApi.register({
      username: form.username,
      nickname: form.nickname,
      password: form.password
    })
    store.setLogin(data)
    ElMessage.success('注册成功，已自动登录')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(900px 460px at 18% -5%, rgba(56, 189, 248, 0.18), transparent 60%),
    radial-gradient(800px 420px at 90% 5%, rgba(129, 140, 248, 0.2), transparent 60%),
    #f6f9ff;
  padding: 24px;
  box-sizing: border-box;
}

.auth-card {
  width: 420px;
  max-width: 100%;
  background: rgba(255, 255, 255, 0.78);
  -webkit-backdrop-filter: blur(14px) saturate(1.2);
  backdrop-filter: blur(14px) saturate(1.2);
  border: 1.5px solid rgba(99, 102, 241, 0.55);
  border-radius: 16px;
  box-shadow:
    0 24px 60px -10px rgba(37, 99, 235, 0.35),
    0 16px 36px -8px rgba(139, 92, 246, 0.25),
    inset 0 1px 0 rgba(255, 255, 255, 0.7);
  padding: 36px 32px 28px;
}

.auth-head {
  text-align: center;
  margin-bottom: 26px;
}

.brand-icon {
  color: var(--brand);
}

.auth-head h1 {
  margin: 10px 0 6px;
  font-size: 22px;
  color: var(--text-main);
}

.auth-head p {
  margin: 0;
  color: var(--text-sub);
  font-size: 13px;
}

.submit-btn {
  width: 100%;
  margin-top: 4px;
  background: linear-gradient(90deg, var(--brand), var(--brand-cyan));
  border: none;
  border-radius: 10px;
}

.auth-footer {
  margin-top: 18px;
  text-align: center;
  color: #6b7280;
  font-size: 13px;
}

.auth-footer a {
  color: var(--brand);
  text-decoration: none;
  font-weight: 600;
}
</style>
