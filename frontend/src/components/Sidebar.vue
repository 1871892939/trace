<template>
  <div class="sidebar">
    <div class="sidebar-header">
      <div class="logo-area">
        <svg class="logo-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 2L3 7V12C3 16.55 6.84 20.74 12 22C17.16 20.74 21 16.55 21 12V7L12 2Z" fill="rgba(255,255,255,0.15)" stroke="rgba(255,255,255,0.8)" stroke-width="1.5" stroke-linejoin="round"/>
          <path d="M9 12L11 14L15 10" stroke="white" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span class="logo-text">溯源系统</span>
      </div>
    </div>

    <el-menu
      :default-active="activeMenu"
      class="sidebar-menu"
      background-color="transparent"
      text-color="rgba(255,255,255,0.65)"
      active-text-color="#ffffff"
      :router="true"
    >
      <el-menu-item index="/dashboard">
        <el-icon><DataBoard /></el-icon>
        <template #title>首页概览</template>
      </el-menu-item>

      <el-menu-item index="/overview">
        <el-icon><TrendCharts /></el-icon>
        <template #title>大盘监控</template>
      </el-menu-item>

      <el-menu-item index="/simulation">
        <el-icon><Cpu /></el-icon>
        <template #title>数据模拟</template>
      </el-menu-item>

      <el-sub-menu index="trace">
        <template #title>
          <el-icon><Guide /></el-icon>
          <span>溯源管理</span>
        </template>
        <el-menu-item index="/trace/batch">批次查询</el-menu-item>
        <el-menu-item index="/trace/chain">溯源链</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="alert">
        <template #title>
          <el-icon><Bell /></el-icon>
          <span>预警中心</span>
        </template>
        <el-menu-item index="/alert/list">预警列表</el-menu-item>
        <el-menu-item index="/alert/handle">预警处理</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="system">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </template>
        <el-menu-item index="/system/user">用户管理</el-menu-item>
        <el-menu-item index="/system/config">参数配置</el-menu-item>
      </el-sub-menu>
    </el-menu>

    <div class="sidebar-footer">
      <div class="version-info">v1.0.0</div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  DataBoard,
  TrendCharts,
  Cpu,
  Guide,
  Bell,
  Setting
} from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.sidebar {
  width: 220px;
  height: 100vh;
  background: linear-gradient(180deg, #1a3a6b 0%, #0f2347 100%);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.03'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  pointer-events: none;
}

.sidebar-header {
  padding: 20px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  position: relative;
  z-index: 1;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
}

.logo-text {
  font-size: 15px;
  font-weight: 600;
  color: #ffffff;
  letter-spacing: 3px;
  white-space: nowrap;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px 0;
  position: relative;
  z-index: 1;
}

.sidebar-menu::-webkit-scrollbar {
  width: 4px;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 2px;
}

.sidebar-menu::-webkit-scrollbar-track {
  background: transparent;
}

:deep(.el-menu) {
  border: none;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
  margin: 2px 10px;
  padding-left: 16px !important;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.2s ease;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.08) !important;
  color: #ffffff !important;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.35) 0%, rgba(103, 194, 58, 0.2) 100%) !important;
  color: #ffffff !important;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

:deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: linear-gradient(180deg, #409eff, #67c23a);
  border-radius: 0 2px 2px 0;
}

:deep(.el-sub-menu .el-menu-item) {
  height: 38px;
  line-height: 38px;
  font-size: 13px;
  padding-left: 48px !important;
  margin: 1px 10px;
}

:deep(.el-sub-menu__title) {
  padding-left: 16px !important;
}

:deep(.el-sub-menu .el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.06) !important;
}

:deep(.el-sub-menu .el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.25) 0%, rgba(103, 194, 58, 0.15) 100%) !important;
}

:deep(.el-icon) {
  font-size: 16px;
  margin-right: 10px;
}

:deep(.el-sub-menu .el-icon) {
  margin-right: 10px;
}

:deep(.el-sub-menu__icon-arrow) {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  position: relative;
  z-index: 1;
}

.version-info {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.25);
  text-align: center;
  letter-spacing: 1px;
}
</style>
