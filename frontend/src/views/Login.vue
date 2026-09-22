<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-head">
        <el-icon :size="32" class="brand-icon"><ShoppingBag /></el-icon>
        <h1>黑猫优选</h1>
        <p>账号密码登录，自动区分用户 / 管理员身份</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
          />
        </el-form-item>
        <el-button type="primary" class="submit-btn" size="large" :loading="loading" @click="submit">
          登 录
        </el-button>
      </el-form>

      <div class="auth-footer">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { authApi } from '../api'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function submit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const data = await authApi.login(form)
    store.setLogin(data)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect
    if (redirect) {
      router.push(String(redirect))
    } else {
      router.push(store.isAdmin ? '/admin' : '/')
    }
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
  width: 400px;
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
