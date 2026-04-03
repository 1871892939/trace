<template>
  <div class="login-container">
    <div class="login-card">
      <div class="card-header">
        <h2 class="title">食品安全溯源系统</h2>
        <p class="subtitle">Food Safety Traceability Platform</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            size="large"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            prefix-icon="Lock"
            show-password
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="tips">
        <p>测试账号：supervisor01 / 123456</p>
        <p>管理员账号：admin / 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 个字符', trigger: 'blur' }
  ]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login({ ...form })
    ElMessage.success('登录成功')
    router.push('/overview')
  } catch (err) {
    ElMessage.error(err.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0a2e5c 0%, #1a5a96 50%, #0a3d7a 100%);
}

.login-card {
  width: 420px;
  padding: 40px 48px;
  background: rgba(255, 255, 255, 0.97);
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.card-header {
  text-align: center;
  margin-bottom: 36px;
}

.title {
  font-size: 22px;
  font-weight: 600;
  color: #1a3a6b;
  margin: 0 0 8px;
  letter-spacing: 2px;
}

.subtitle {
  font-size: 12px;
  color: #8a9ab5;
  margin: 0;
  letter-spacing: 1px;
}

.login-form {
  margin-bottom: 24px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #1a5a96, #0a3d7a);
  border: none;
}

.login-btn:hover {
  background: linear-gradient(135deg, #1e6bb5, #0d4a8e);
}

.tips {
  padding: 12px 16px;
  background: #f0f4fa;
  border-radius: 6px;
  border-left: 3px solid #1a5a96;
}

.tips p {
  margin: 0;
  font-size: 12px;
  color: #5a6a7a;
  line-height: 1.8;
}
</style>
