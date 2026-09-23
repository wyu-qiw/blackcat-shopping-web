<template>
  <div class="user-layout">
    <!-- 顶部功能区：黑猫优选标题 + 搜索 + 全部分类 -->
    <header class="top-bar">
      <div class="top-inner">
        <router-link to="/" class="brand">
          <el-icon :size="24"><ShoppingBag /></el-icon>
          <span>黑猫优选</span>
        </router-link>

        <div class="top-search">
          <el-input
            v-model="keyword"
            placeholder="搜索商品名称或描述…"
            clearable
            size="large"
            :prefix-icon="Search"
            @keyup.enter="doSearch"
            @clear="doSearch"
          >
            <template #append>
              <el-button @click="doSearch">搜索</el-button>
            </template>
          </el-input>
        </div>

        <el-button class="cat-btn" @click="catDrawer = true">
          <el-icon><Grid /></el-icon>
          <span>全部分类</span>
          <el-icon class="arrow"><ArrowDown /></el-icon>
        </el-button>

        <div class="user-area">
          <template v-if="store.isLogin">
            <el-dropdown trigger="click" @command="handleCommand">
              <span class="user-trigger">
                <el-avatar :size="30" :src="store.user?.avatar">
                  {{ store.user?.nickname?.slice(0, 1) }}
                </el-avatar>
                <span class="nickname">{{ store.user?.nickname }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item v-if="store.isAdmin" command="admin">进入后台</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="text-btn">登录</router-link>
            <router-link to="/register" class="primary-btn">注册</router-link>
          </template>
        </div>
      </div>
    </header>

    <!-- 全部分类弹层 -->
    <el-drawer v-model="catDrawer" title="全部分类" size="340px">
      <div class="cat-grid">
        <div class="cat-cell" @click="pickCategory(null)">
          <el-icon :size="22"><Grid /></el-icon>
          <span>全部商品</span>
        </div>
        <div
          v-for="c in categories"
          :key="c.id"
          class="cat-cell"
          @click="pickCategory(c.id)"
        >
          <el-icon :size="22"><Goods /></el-icon>
          <span>{{ c.name }}</span>
        </div>
      </div>
    </el-drawer>

    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { categoryApi } from '../api'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const keyword = ref((route.query.q || '').toString())
const catDrawer = ref(false)
const categories = ref([])

watch(
  () => route.query.q,
  (q) => {
    keyword.value = (q || '').toString()
  }
)

async function loadCategories() {
  try {
    categories.value = await categoryApi.list()
  } catch {
    categories.value = []
  }
}

function doSearch() {
  const q = keyword.value.trim()
  const query = { ...route.query }
  router.push({ path: '/buy', query: q ? { q } : {} })
}

function pickCategory(id) {
  catDrawer.value = false
  const query = { ...route.query }
  router.push({ path: '/buy', query: id != null ? { cat: id } : {} })
}

function handleCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
      .then(() => {
        store.logout()
        ElMessage.success('已退出登录')
        router.push('/login')
      })
      .catch(() => {})
  } else if (command === 'admin') {
    router.push('/admin')
  } else if (command === 'profile') {
    router.push('/my/profile')
  }
}

onMounted(loadCategories)
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.top-bar {
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(14px) saturate(1.2);
  -webkit-backdrop-filter: blur(14px) saturate(1.2);
  border-bottom: 1.5px solid rgba(37, 99, 235, 0.40);
  box-shadow: 0 8px 24px -12px rgba(37, 99, 235, 0.25);
  position: sticky;
  top: 0;
  z-index: 100;
}

.top-inner {
  max-width: 1280px;
  margin: 0 auto;
  height: 64px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 19px;
  font-weight: 800;
  text-decoration: none;
  white-space: nowrap;
  background: linear-gradient(120deg, var(--brand-deep), var(--brand-cyan));
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.brand :deep(svg) {
  background: none;
  -webkit-text-fill-color: var(--brand);
  filter: drop-shadow(0 2px 6px rgba(37, 99, 235, 0.35));
}

.top-search {
  flex: 1;
  max-width: 520px;
  margin: 0 auto;
}

.top-search :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px rgba(37, 99, 235, 0.18) inset, 0 8px 22px rgba(37, 99, 235, 0.10);
}

.top-search :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--brand) inset, 0 10px 26px rgba(37, 99, 235, 0.22);
}

.top-search :deep(.el-input-group__append) {
  background: var(--brand-gradient);
  border: none;
  border-radius: 0 12px 12px 0;
}

.top-search :deep(.el-input-group__append .el-button) {
  border: none;
  background: transparent;
  color: #fff;
  font-weight: 600;
}

.cat-btn {
  border-radius: 999px;
  border: 1px solid rgba(37, 99, 235, 0.32);
  color: var(--brand);
  font-weight: 600;
  background: rgba(255, 255, 255, 0.6);
}

.cat-btn:hover {
  border-color: var(--brand);
  background: rgba(239, 246, 255, 0.9);
}

.cat-btn .arrow {
  margin-left: 2px;
  font-size: 12px;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #333;
  outline: none;
}

.nickname {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
}

.text-btn {
  color: var(--brand);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
}

.primary-btn {
  background: var(--brand-gradient);
  color: #fff;
  padding: 8px 18px;
  border-radius: 999px;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.22s;
}

.primary-btn:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-btn);
}

/* 全部分类弹层 */
.cat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.cat-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 8px;
  border: 1px solid rgba(37, 99, 235, 0.14);
  border-radius: 14px;
  background: #fff;
  color: var(--brand);
  cursor: pointer;
  transition: all 0.2s;
}

.cat-cell span {
  color: var(--text-main);
  font-size: 14px;
  font-weight: 600;
}

.cat-cell:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.16);
  border-color: rgba(37, 99, 235, 0.4);
}

.main-content {
  flex: 1;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  box-sizing: border-box;
}

@media (max-width: 900px) {
  .top-search { max-width: none; }
  .cat-btn span { display: none; }
  .brand span { display: none; }
  .nickname { display: none; }
}

@media (max-width: 640px) {
  .top-inner {
    height: auto;
    min-height: 58px;
    padding: 10px 12px;
    gap: 10px;
    flex-wrap: wrap;
  }

  .brand {
    gap: 6px;
    font-size: 17px;
  }

  .brand span {
    display: inline;
  }

  .top-search {
    order: 4;
    flex-basis: 100%;
  }

  .top-search :deep(.el-input-group__append) {
    padding-left: 14px;
    padding-right: 14px;
  }

  .cat-btn {
    padding: 9px 11px;
  }

  .cat-btn .arrow {
    display: none;
  }

  .user-area {
    margin-left: auto;
    gap: 8px;
  }

  .primary-btn {
    padding: 7px 14px;
  }

  .main-content {
    padding: 14px 12px 24px;
  }
}
</style>