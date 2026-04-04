<template>
  <div class="operation-log-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">操作日志</h2>
        <p class="page-subtitle">审计系统增删改操作记录，便于追溯与安全审查</p>
      </div>
      <el-button :icon="Refresh" @click="fetchData" :loading="loading">
        刷新
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input
        v-model="filters.keyword"
        placeholder="搜索用户名 / 描述 / 模块"
        clearable
        style="width: 220px"
        @keyup.enter="fetchData"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-select v-model="filters.operationType" placeholder="操作类型" clearable style="width: 120px">
        <el-option label="新增" value="CREATE" />
        <el-option label="修改" value="UPDATE" />
        <el-option label="删除" value="DELETE" />
      </el-select>

      <el-select v-model="filters.module" placeholder="所属模块" clearable style="width: 140px">
        <el-option label="批次管理" value="批次管理" />
        <el-option label="用户管理" value="用户管理" />
        <el-option label="参数配置" value="参数配置" />
        <el-option label="预警管理" value="预警管理" />
        <el-option label="其他" value="其他" />
      </el-select>

      <el-select v-model="filters.status" placeholder="操作结果" clearable style="width: 120px">
        <el-option label="成功" value="SUCCESS" />
        <el-option label="失败" value="FAIL" />
      </el-select>

      <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <!-- 统计概览 -->
    <div class="stat-bar">
      <div class="stat-item">
        <span class="stat-num">{{ tableData.length }}</span>
        <span class="stat-lbl">当前筛选</span>
      </div>
      <div class="stat-item success">
        <span class="stat-num">{{ successCount }}</span>
        <span class="stat-lbl">条成功</span>
      </div>
      <div class="stat-item fail">
        <span class="stat-num">{{ failCount }}</span>
        <span class="stat-lbl">条失败</span>
      </div>
    </div>

    <!-- 日志表格 -->
    <div class="table-card">
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column label="操作时间" width="170" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ row.operateTime }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作用户" min-width="130">
          <template #default="{ row }">
            <div class="user-cell">
              <div class="avatar" :class="row.role">{{ (row.username || '?').charAt(0).toUpperCase() }}</div>
              <div class="user-info">
                <span class="username-text">{{ row.username || '—' }}</span>
                <el-tag :type="row.role === 'admin' ? 'danger' : 'primary'" size="small" effect="plain">
                  {{ row.roleName || row.role }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="opTypeTag(row.operationType)" size="small" effect="dark">
              {{ row.operationTypeName || row.operationType }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="所属模块" width="110" align="center">
          <template #default="{ row }">
            <span class="module-text">{{ row.module }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作描述" min-width="200">
          <template #default="{ row }">
            <span class="desc-text">{{ row.description }}</span>
          </template>
        </el-table-column>

        <el-table-column label="请求路径" min-width="160">
          <template #default="{ row }">
            <code class="url-code">{{ row.requestUrl }}</code>
          </template>
        </el-table-column>

        <el-table-column label="IP 地址" width="130" align="center">
          <template #default="{ row }">
            <span class="ip-text">{{ row.ipAddress || '—' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="结果" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" size="small" effect="plain">
              {{ row.statusName || row.status }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作人" width="100" align="center">
          <template #default="{ row }">
            <span class="operator-text">{{ row.operator || row.username || '—' }}</span>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && tableData.length === 0" class="empty-tip">
        <el-empty description="暂无操作日志记录" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getOperationLogList } from '@/api/operation-log'

const loading = ref(false)
const tableData = ref([])

const filters = reactive({
  keyword: '',
  operationType: '',
  module: '',
  status: ''
})

const successCount = computed(() => tableData.value.filter(r => r.status === 'SUCCESS').length)
const failCount = computed(() => tableData.value.filter(r => r.status === 'FAIL').length)

function opTypeTag(type) {
  const map = { CREATE: 'success', UPDATE: 'warning', DELETE: 'danger' }
  return map[type] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const params = {}
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.operationType) params.operationType = filters.operationType
    if (filters.module) params.module = filters.module
    if (filters.status) params.status = filters.status
    const res = await getOperationLogList(params)
    if (res.code === 200) {
      tableData.value = res.data || []
    }
  } catch {
    ElMessage.error('加载日志失败')
  } finally {
    loading.value = false
  }
}

function handleReset() {
  filters.keyword = ''
  filters.operationType = ''
  filters.module = ''
  filters.status = ''
  fetchData()
}

fetchData()
</script>

<style scoped>
.operation-log-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 600;
  color: #1a3a6b;
}

.page-subtitle {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.stat-bar {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: baseline;
  gap: 6px;
  background: #ffffff;
  border-radius: 10px;
  padding: 10px 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.stat-num {
  font-size: 22px;
  font-weight: 800;
  color: #1a3a6b;
}

.stat-lbl {
  font-size: 13px;
  color: #909399;
}

.stat-item.success .stat-num { color: #67c23a; }
.stat-item.fail .stat-num { color: #f56c6c; }

.table-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.time-text {
  font-size: 13px;
  color: #606266;
  font-family: 'Courier New', monospace;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  color: #ffffff;
  flex-shrink: 0;
}

.avatar.admin { background: linear-gradient(135deg, #f56c6c, #e64a19); }
.avatar.supervisor { background: linear-gradient(135deg, #409eff, #1a5a96); }
.avatar.unknown { background: linear-gradient(135deg, #909399, #606266); }

.user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.username-text {
  font-weight: 500;
  color: #303133;
  font-size: 13px;
}

.module-text {
  font-size: 13px;
  color: #606266;
}

.desc-text {
  font-size: 13px;
  color: #303133;
}

.url-code {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  color: #409eff;
}

.ip-text {
  font-size: 12px;
  color: #909399;
  font-family: 'Courier New', monospace;
}

.operator-text {
  font-size: 13px;
  color: #606266;
}

.empty-tip {
  padding: 40px 0;
}
</style>
