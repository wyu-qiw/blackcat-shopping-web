import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { cartApi } from '../api'

// 全局购物车状态：供左侧菜单角标、购买页小窗、购物车页共享
export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const loading = ref(false)
  // 内部标记：是否已加载过，避免重复请求；无需暴露给组件
  const loaded = ref(false)

  const count = computed(() => items.value.length)

  async function fetchCart(force = false) {
    if (loading.value) return
    if (loaded.value && !force) return
    loading.value = true
    try {
      items.value = (await cartApi.list()) || []
      loaded.value = true
    } finally {
      loading.value = false
    }
  }

  // 加入购物车（详情页“+购物车”/购买页勾选均走此方法，后端按用户+商品去重）
  async function add(productId) {
    await cartApi.add(productId)
    // 加入后重新拉取，确保与后端去重后的状态一致
    await fetchCart(true)
  }

  async function remove(productId) {
    await cartApi.remove(productId)
    items.value = items.value.filter((p) => p.productId !== productId)
  }

  async function clear() {
    await cartApi.clear()
    items.value = []
  }

  return {
    items,
    loading,
    count,
    fetchCart,
    add,
    remove,
    clear
  }
})