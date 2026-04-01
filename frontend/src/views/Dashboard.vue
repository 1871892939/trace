<template>
  <div class="main-layout">
    <Sidebar />
    <div class="main-area">
      <div class="topbar">
        <div class="topbar-left">
          <h2 class="system-title">食品安全溯源系统</h2>
        </div>
        <div class="topbar-right">
          <div class="user-info">
            <div class="avatar">{{ userStore.username?.charAt(0)?.toUpperCase() || 'U' }}</div>
            <span class="username">{{ userStore.username }}</span>
            <el-tag :type="userStore.isAdmin ? 'danger' : 'success'" size="small" effect="dark">
              {{ userStore.isAdmin ? '管理员' : '监管员' }}
            </el-tag>
          </div>
          <el-button type="danger" size="default" plain round @click="handleLogout">
            <el-icon style="margin-right: 4px"><SwitchButton /></el-icon>
            退出
          </el-button>
        </div>
      </div>
      <div class="page-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { SwitchButton } from '@element-plus/icons-vue'
import Sidebar from '@/components/Sidebar.vue'

const userStore = useUserStore()
const router = useRouter()

async function handleLogout() {
  await userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.main-layout {
  display: flex;
  min-height: 100vh;
  background: #f0f4f8;
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.topbar {
  height: 60px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  z-index: 10;
  position: sticky;
  top: 0;
}

.topbar-left {
  display: flex;
  align-items: center;
}

.system-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a3a6b;
  letter-spacing: 2px;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1a3a6b, #409eff);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0;
}

.username {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.page-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
