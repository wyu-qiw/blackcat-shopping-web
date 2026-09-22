import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '../api'

// 用户状态：Token、用户信息、登录/退出/刷新个人信息
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLogin = computed(() => Boolean(token.value))
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function setLogin(loginData) {
    token.value = loginData.token
    user.value = loginData.user
    localStorage.setItem('token', loginData.token)
    localStorage.setItem('user', JSON.stringify(loginData.user))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  async function fetchMe() {
    const data = await authApi.me()
    user.value = data
    localStorage.setItem('user', JSON.stringify(data))
    return data
  }

  return { token, user, isLogin, isAdmin, setLogin, logout, fetchMe }
})

