<template>
  <div class="dashboard">
    <div class="header">
      <div class="header-left">
        <h2>食品安全溯源系统</h2>
      </div>
      <div class="header-right">
        <span class="username">{{ userStore.username }}</span>
        <el-tag :type="userStore.isAdmin ? 'danger' : 'success'" size="small">
          {{ userStore.isAdmin ? '管理员' : '监管员' }}
        </el-tag>
        <el-button type="danger" size="small" plain @click="handleLogout">
          退出登录
        </el-button>
      </div>
    </div>
    <div class="content">
      <el-empty description="仪表盘开发中，后续迭代完善" />
    </div>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()

async function handleLogout() {
  await userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: #f0f2f5;
}

.header {
  height: 60px;
  background: #1a3a6b;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.header h2 {
  margin: 0;
  color: #fff;
  font-size: 18px;
  font-weight: 500;
  letter-spacing: 2px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.username {
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
}

.content {
  padding: 32px;
}
</style>
