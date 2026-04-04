<template>
  <div class="user-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">用户管理</h2>
        <p class="page-subtitle">管理系统用户账号，支持新增、编辑、禁用与删除操作</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog" :loading="loading">
        新增用户
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input
        v-model="filters.keyword"
        placeholder="搜索用户名"
        clearable
        style="width: 220px"
        @keyup.enter="fetchData"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-select v-model="filters.role" placeholder="全部角色" clearable style="width: 140px">
        <el-option label="管理员" value="admin" />
        <el-option label="监管员" value="supervisor" />
      </el-select>

      <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 130px">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>

      <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <!-- 统计概览 -->
    <div class="stat-bar">
      <div class="stat-item">
        <span class="stat-num">{{ tableData.length }}</span>
        <span class="stat-lbl">用户总数</span>
      </div>
      <div class="stat-item">
        <span class="stat-num">{{ adminCount }}</span>
        <span class="stat-lbl">管理员</span>
      </div>
      <div class="stat-item">
        <span class="stat-num">{{ enabledCount }}</span>
        <span class="stat-lbl">启用中</span>
      </div>
    </div>

    <!-- 用户表格 -->
    <div class="table-card">
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="username" label="用户名" min-width="160">
          <template #default="{ row }">
            <div class="user-cell">
              <div class="avatar" :class="row.role">{{ row.username.charAt(0).toUpperCase() }}</div>
              <span class="username-text">{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="角色" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'primary'" size="small" effect="dark">
              {{ row.roleName }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="170" />

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-divider direction="vertical" />
            <el-button
              v-if="row.status === 1"
              type="warning"
              link
              size="small"
              @click="toggleStatus(row, 0)"
            >
              禁用
            </el-button>
            <el-button
              v-else
              type="success"
              link
              size="small"
              @click="toggleStatus(row, 1)"
            >
              启用
            </el-button>
            <el-divider direction="vertical" />
            <el-button
              type="danger"
              link
              size="small"
              @click="handleDelete(row)"
              :disabled="row.username === currentUsername"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && tableData.length === 0" class="empty-tip">
        <el-empty description="暂无用户数据" />
      </div>
    </div>

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增用户' : '编辑用户'"
      width="460px"
      :close-on-click-modal="false"
    >
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="dialogForm.username"
            placeholder="3-20 个字符"
            :disabled="dialogMode === 'edit'"
            clearable
          />
        </el-form-item>

        <el-form-item :label="dialogMode === 'create' ? '密码' : '新密码'" prop="password">
          <el-input
            v-model="dialogForm.password"
            type="password"
            show-password
            :placeholder="dialogMode === 'create' ? '6-20 个字符' : '不修改请留空'"
            clearable
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword" v-if="dialogMode === 'create'">
          <el-input
            v-model="dialogForm.confirmPassword"
            type="password"
            show-password
            placeholder="再次输入密码"
            clearable
          />
        </el-form-item>

        <el-form-item label="角色" prop="role">
          <el-select v-model="dialogForm.role" style="width: 100%">
            <el-option label="管理员" value="admin" />
            <el-option label="监管员" value="supervisor" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dialogForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">确认保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const currentUsername = computed(() => userStore.username)

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const dialogFormRef = ref(null)

const filters = reactive({ keyword: '', role: '', status: null })

const dialogForm = reactive({ id: null, username: '', password: '', confirmPassword: '', role: 'supervisor', status: 1 })

const dialogRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== dialogForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const adminCount = computed(() => tableData.value.filter(r => r.role === 'admin').length)
const enabledCount = computed(() => tableData.value.filter(r => r.status === 1).length)

async function fetchData() {
  loading.value = true
  try {
    const params = {}
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.role) params.role = filters.role
    if (filters.status !== null && filters.status !== '') params.status = filters.status
    const res = await getUserList(params)
    if (res.code === 200) tableData.value = res.data || []
  } catch {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

function handleReset() {
  filters.keyword = ''
  filters.role = ''
  filters.status = null
  fetchData()
}

function resetDialogForm() {
  dialogForm.id = null
  dialogForm.username = ''
  dialogForm.password = ''
  dialogForm.confirmPassword = ''
  dialogForm.role = 'supervisor'
  dialogForm.status = 1
}

function openCreateDialog() {
  dialogMode.value = 'create'
  resetDialogForm()
  dialogVisible.value = true
  dialogFormRef.value?.clearValidate()
}

function openEditDialog(row) {
  dialogMode.value = 'edit'
  dialogForm.id = row.id
  dialogForm.username = row.username
  dialogForm.password = ''
  dialogForm.confirmPassword = ''
  dialogForm.role = row.role
  dialogForm.status = row.status
  dialogVisible.value = true
  dialogFormRef.value?.clearValidate()
}

async function handleSave() {
  const valid = await dialogFormRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (dialogMode.value === 'create') {
      const res = await createUser({
        username: dialogForm.username,
        password: dialogForm.password,
        role: dialogForm.role
      })
      if (res.code === 200) {
        ElMessage.success('创建成功')
        dialogVisible.value = false
        fetchData()
      } else {
        ElMessage.error(res.message || '创建失败')
      }
    } else {
      const req = { id: dialogForm.id, role: dialogForm.role, status: dialogForm.status }
      if (dialogForm.password) req.password = dialogForm.password
      const res = await updateUser(req)
      if (res.code === 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        fetchData()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row, status) {
  const action = status === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确认${action}用户「${row.username}」？`, `${action}用户`, {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  const res = await updateUser({ id: row.id, status })
  if (res.code === 200) {
    ElMessage.success(`${action}成功`)
    fetchData()
  } else {
    ElMessage.error(res.message || `${action}失败`)
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `删除后不可恢复，确认删除用户「${row.username}」？`,
      '删除用户',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'error' }
    )
  } catch {
    return
  }
  const res = await deleteUser(row.id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    fetchData()
  } else {
    ElMessage.error(res.message || '删除失败')
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.user-page { display: flex; flex-direction: column; gap: 16px; }

.page-header { display: flex; align-items: center; justify-content: space-between; }
.page-title { margin: 0 0 4px; font-size: 18px; font-weight: 600; color: #1a3a6b; }
.page-subtitle { margin: 0; font-size: 13px; color: #909399; }

.filter-bar {
  display: flex; align-items: center; gap: 12px; padding: 16px 20px;
  background: #ffffff; border-radius: 12px; box-shadow: 0 1px 4px rgba(0,0,0,.06);
}

.stat-bar { display: flex; gap: 16px; }
.stat-item {
  display: flex; align-items: baseline; gap: 6px;
  background: #ffffff; border-radius: 10px; padding: 10px 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
.stat-num { font-size: 22px; font-weight: 800; color: #1a3a6b; }
.stat-lbl { font-size: 13px; color: #909399; }

.table-card {
  background: #ffffff; border-radius: 12px; padding: 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}

.user-cell { display: flex; align-items: center; gap: 10px; }
.avatar {
  width: 32px; height: 32px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: 700; color: #ffffff;
  flex-shrink: 0;
}
.avatar.admin { background: linear-gradient(135deg, #f56c6c, #e64a19); }
.avatar.supervisor { background: linear-gradient(135deg, #409eff, #1a5a96); }
.username-text { font-weight: 500; color: #303133; }

.empty-tip { padding: 40px 0; }
</style>
