<template>
  <div class="operation-log-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">操作日志</h2>
        <p class="page-subtitle">批次操作审计记录，支持按类型与关键字检索</p>
      </div>
      <el-button :icon="Refresh" @click="fetchData" :loading="loading">
        刷新
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input
        v-model="filters.keyword"
        placeholder="搜索批次号 / 操作描述"
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

      <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <!-- 统计概览 -->
    <div class="stat-bar">
      <div class="stat-item">
        <span class="stat-num">{{ tableData.length }}</span>
        <span class="stat-lbl">条记录</span>
      </div>
    </div>

    <!-- 日志表格 -->
    <div class="table-card">
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column label="批次编号" min-width="170">
          <template #default="{ row }">
            <span v-if="row.batchNo" class="batch-no">{{ row.batchNo }}</span>
            <span v-else class="no-data">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作时间" width="170" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ row.operateTime }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作用户" min-width="120">
          <template #default="{ row }">
            <div class="user-cell">
              <div class="avatar" :class="row.role || 'unknown'">
                {{ (row.username || '?').charAt(0).toUpperCase() }}
              </div>
              <span class="username-text">{{ row.username || '—' }}</span>
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

        <el-table-column label="操作描述" min-width="200">
          <template #default="{ row }">
            <span class="desc-text">{{ row.description }}</span>
          </template>
        </el-table-column>



        <el-table-column label="批次新增时间" width="160" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ row.batchCreateTime || '—' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="批次修改时间" width="160" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ row.batchUpdateTime || '—' }}</span>
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
import { ref, reactive } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getOperationLogList } from '@/api/operation-log'

const loading = ref(false)
const tableData = ref([])

const filters = reactive({
  keyword: '',
  operationType: ''
})

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
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: #ffffff;
  flex-shrink: 0;
}

.avatar.admin    { background: linear-gradient(135deg, #f56c6c, #e64a19); }
.avatar.supervisor { background: linear-gradient(135deg, #409eff, #1a5a96); }
.avatar.unknown  { background: linear-gradient(135deg, #909399, #606266); }
.avatar.system   { background: linear-gradient(135deg, #e6a23c, #8d6600); }

.username-text {
  font-weight: 500;
  color: #303133;
  font-size: 13px;
}

.desc-text {
  font-size: 13px;
  color: #303133;
}

.batch-no {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #409eff;
  font-weight: 500;
}

.no-data {
  color: #c0c4cc;
  font-size: 13px;
}

.empty-tip {
  padding: 40px 0;
}
</style>
