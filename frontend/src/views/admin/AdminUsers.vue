<template>
  <div class="admin-users-page">
    <div class="page-head">
      <h2>全平台用户</h2>
      <p>查看平台所有注册用户，共 {{ users.length }} 人</p>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="users" stripe>
        <el-table-column prop="id" label="用户ID" width="90" />
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="nickname" label="昵称" min-width="140" />
        <el-table-column label="角色" width="130">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'warning' : 'success'">
              {{ row.role === 'ADMIN' ? '平台管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { adminApi } from '../../api'

const users = ref([])
const loading = ref(false)

async function loadUsers() {
  loading.value = true
  try {
    users.value = await adminApi.users()
  } finally {
    loading.value = false
  }
}

onMounted(loadUsers)
</script>

<style scoped>
.page-head {
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
</style>

