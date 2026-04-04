<template>
  <div class="login-root">

    <!-- 动态背景 -->
    <div class="bg-orbs">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
    </div>

    <!-- 主卡片 -->
    <div class="auth-card">
      <!-- 顶部 Logo 区 -->
      <div class="card-logo">
        <div class="logo-icon">
          <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M24 4L6 14V26C6 35.94 13.68 45.48 24 48C34.32 45.48 42 35.94 42 26V14L24 4Z"
                  fill="url(#shield-grad)" stroke="rgba(255,255,255,0.6)" stroke-width="2" stroke-linejoin="round"/>
            <path d="M17 24L22 29L31 20" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            <defs>
              <linearGradient id="shield-grad" x1="6" y1="4" x2="42" y2="48" gradientUnits="userSpaceOnUse">
                <stop stop-color="#409eff"/>
                <stop offset="1" stop-color="#67c23a"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <div class="logo-text">
          <h1 class="system-name">食品安全溯源系统</h1>
          <p class="system-sub">Food Safety Traceability Platform</p>
        </div>
      </div>

      <!-- 切换标签 -->
      <div class="tab-bar">
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'login' }"
          @click="switchTab('login')"
        >
          登录
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'register' }"
          @click="switchTab('register')"
        >
          注册
        </button>
        <div class="tab-indicator" :style="indicatorStyle"></div>
      </div>

      <!-- 登录表单 -->
      <transition name="form-fade" mode="out-in">
        <div v-if="activeTab === 'login'" key="login" class="form-wrap">
          <p class="form-hint">使用您的账号登录系统</p>

          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" size="large" @submit.prevent="handleLogin">
            <el-form-item prop="username">
              <div class="input-wrap">
                <el-icon class="input-icon"><User /></el-icon>
                <el-input
                  v-model="loginForm.username"
                  placeholder="请输入用户名"
                  clearable
                  autocomplete="username"
                />
              </div>
            </el-form-item>

            <el-form-item prop="password">
              <div class="input-wrap">
                <el-icon class="input-icon"><Lock /></el-icon>
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  show-password
                  autocomplete="current-password"
                  @keyup.enter="handleLogin"
                />
              </div>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="submit-btn"
                :loading="loading"
                @click="handleLogin"
              >
                <span v-if="!loading">登 录</span>
                <span v-else>登录中...</span>
              </el-button>
            </el-form-item>
          </el-form>

          <div class="quick-login">
            <p class="quick-label">快速登录</p>
            <div class="quick-accounts">
              <div class="quick-chip" @click="quickLogin('admin', '123456')">
                <span class="chip-role admin">管理员</span>
                <span class="chip-user">admin / 123456</span>
              </div>
              <div class="quick-chip" @click="quickLogin('supervisor01', '123456')">
                <span class="chip-role supervisor">监管员</span>
                <span class="chip-user">supervisor01 / 123456</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 注册表单 -->
        <div v-else key="register" class="form-wrap">
          <p class="form-hint">创建新账号，系统管理员审核后生效</p>

          <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" size="large" @submit.prevent="handleRegister">
            <el-form-item prop="username">
              <div class="input-wrap">
                <el-icon class="input-icon"><User /></el-icon>
                <el-input
                  v-model="registerForm.username"
                  placeholder="设置用户名（3-20 字符）"
                  clearable
                  autocomplete="username"
                />
              </div>
            </el-form-item>

            <el-form-item prop="password">
              <div class="input-wrap">
                <el-icon class="input-icon"><Lock /></el-icon>
                <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="设置密码（6-20 字符）"
                  show-password
                  autocomplete="new-password"
                />
              </div>
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <div class="input-wrap">
                <el-icon class="input-icon"><Lock /></el-icon>
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="再次确认密码"
                  show-password
                  autocomplete="new-password"
                />
              </div>
            </el-form-item>

            <el-form-item prop="role">
              <div class="input-wrap">
                <el-icon class="input-icon"><Grid /></el-icon>
                <el-select v-model="registerForm.role" placeholder="选择角色" style="width: 100%">
                  <el-option label="监管员" value="supervisor" />
                  <el-option label="管理员" value="admin" />
                </el-select>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="submit-btn"
                :loading="loading"
                @click="handleRegister"
              >
                <span v-if="!loading">注 册</span>
                <span v-else>注册中...</span>
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </transition>
    </div>

    <!-- 底部版权 -->
    <div class="footer-text">NCG Traceability System v1.0.0</div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Grid } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { register as registerApi } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const loading = ref(false)
const loginFormRef = ref(null)
const registerFormRef = ref(null)

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', password: '', confirmPassword: '', role: 'supervisor' })

const indicatorStyle = computed(() => ({
  transform: activeTab.value === 'login' ? 'translateX(0)' : 'translateX(100%)',
  width: '50%'
}))

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 个字符', trigger: 'blur' }
  ]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

function switchTab(tab) {
  if (tab === activeTab.value) return
  activeTab.value = tab
  loading.value = false
}

function quickLogin(username, password) {
  loginForm.username = username
  loginForm.password = password
  handleLogin()
}

async function handleLogin() {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login({ ...loginForm })
    ElMessage.success('登录成功')
    router.push('/overview')
  } catch (err) {
    ElMessage.error(err.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await registerApi({
      username: registerForm.username,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword,
      role: registerForm.role
    })
    if (res.code === 200) {
      ElMessage.success('注册成功，请使用新账号登录')
      activeTab.value = 'login'
      loginForm.username = registerForm.username
      loginForm.password = ''
      registerForm.password = ''
      registerForm.confirmPassword = ''
    } else {
      ElMessage.error(res.message || '注册失败')
    }
  } catch {
    ElMessage.error('注册请求失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* === 根容器 === */
.login-root {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #060e1e;
  position: relative;
  overflow: hidden;
}

/* === 动态光球背景 === */
.bg-orbs { position: absolute; inset: 0; pointer-events: none; }

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.18;
  animation: orbFloat 12s ease-in-out infinite;
}

.orb-1 {
  width: 500px; height: 500px;
  background: #409eff;
  top: -100px; left: -100px;
  animation-delay: 0s;
}

.orb-2 {
  width: 400px; height: 400px;
  background: #67c23a;
  bottom: -80px; right: -60px;
  animation-delay: -4s;
}

.orb-3 {
  width: 350px; height: 350px;
  background: #e6a23c;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -8s;
}

@keyframes orbFloat {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -20px) scale(1.05); }
  66% { transform: translate(-20px, 15px) scale(0.95); }
}

/* === 主卡片 === */
.auth-card {
  position: relative;
  z-index: 10;
  width: 440px;
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 24px;
  padding: 40px 44px;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.5), inset 0 1px 0 rgba(255,255,255,0.1);
}

/* === Logo 区 === */
.card-logo {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 32px;
  gap: 0;
}

.logo-icon svg {
  width: 56px;
  height: 56px;
  margin-bottom: 12px;
  filter: drop-shadow(0 4px 16px rgba(64, 158, 255, 0.4));
}

.logo-text { text-align: center; }

.system-name {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 3px;
}

.system-sub {
  margin: 4px 0 0;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
  letter-spacing: 1px;
}

/* === 切换标签 === */
.tab-bar {
  position: relative;
  display: flex;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 28px;
}

.tab-btn {
  flex: 1;
  padding: 8px 0;
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.5);
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: color 0.25s;
  position: relative;
  z-index: 1;
  border-radius: 9px;
}

.tab-btn.active {
  color: #ffffff;
  font-weight: 600;
}

.tab-indicator {
  position: absolute;
  top: 4px;
  left: 4px;
  height: calc(100% - 8px);
  background: linear-gradient(135deg, rgba(64,158,255,0.7), rgba(103,194,58,0.5));
  border-radius: 9px;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 0;
}

/* === 表单 === */
.form-wrap { width: 100%; }

.form-hint {
  text-align: center;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.45);
  margin: 0 0 24px;
}

.input-wrap {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
}

.input-icon {
  position: absolute;
  left: 14px;
  color: rgba(255, 255, 255, 0.4);
  z-index: 1;
  font-size: 16px;
  pointer-events: none;
}

:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.08) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  box-shadow: none !important;
  border-radius: 12px !important;
  padding-left: 40px !important;
  transition: border-color 0.2s, background 0.2s !important;
}

:deep(.el-input__wrapper:hover) {
  background: rgba(255, 255, 255, 0.12) !important;
  border-color: rgba(64, 158, 255, 0.5) !important;
}

:deep(.el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.12) !important;
  border-color: #409eff !important;
}

:deep(.el-input__inner) {
  color: rgba(255, 255, 255, 0.9) !important;
  font-size: 14px !important;
}

:deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.3) !important;
}

:deep(.el-select .el-input__wrapper) {
  padding-left: 40px !important;
}

:deep(.el-select__wrapper) {
  background: rgba(255, 255, 255, 0.08) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  box-shadow: none !important;
  color: rgba(255, 255, 255, 0.9) !important;
  border-radius: 12px !important;
  min-height: 40px !important;
}

:deep(.el-select__placeholder) {
  color: rgba(255, 255, 255, 0.3) !important;
}

:deep(.el-form-item) { margin-bottom: 18px; }
:deep(.el-form-item__error) { font-size: 12px; }

/* === 提交按钮 === */
.submit-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  letter-spacing: 6px;
  background: linear-gradient(135deg, #409eff, #67c23a) !important;
  border: none !important;
  border-radius: 12px !important;
  font-weight: 600;
  color: #ffffff !important;
  transition: opacity 0.2s, transform 0.15s !important;
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.35) !important;
}

.submit-btn:hover {
  opacity: 0.92;
  transform: translateY(-1px);
  box-shadow: 0 6px 28px rgba(64, 158, 255, 0.5) !important;
}

.submit-btn:active { transform: translateY(0); }

/* === 快速登录 === */
.quick-login {
  margin-top: 8px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.quick-label {
  text-align: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.3);
  margin: 0 0 12px;
}

.quick-accounts {
  display: flex;
  gap: 10px;
}

.quick-chip {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s;
}

.quick-chip:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(64, 158, 255, 0.4);
}

.chip-role {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 20px;
  letter-spacing: 0.5px;
}

.chip-role.admin {
  background: rgba(245, 108, 108, 0.2);
  color: #f56c6c;
  border: 1px solid rgba(245, 108, 108, 0.3);
}

.chip-role.supervisor {
  background: rgba(64, 158, 255, 0.2);
  color: #409eff;
  border: 1px solid rgba(64, 158, 255, 0.3);
}

.chip-user {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.45);
}

/* === 表单切换动画 === */
.form-fade-enter-active,
.form-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.form-fade-enter-from {
  opacity: 0;
  transform: translateX(12px);
}
.form-fade-leave-to {
  opacity: 0;
  transform: translateX(-12px);
}

/* === 底部 === */
.footer-text {
  position: fixed;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 11px;
  color: rgba(255, 255, 255, 0.2);
  letter-spacing: 1px;
  z-index: 10;
}
</style>
