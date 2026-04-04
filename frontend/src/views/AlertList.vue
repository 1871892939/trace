<template>
  <div class="alert-list-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">预警列表</h2>
        <p class="page-subtitle">查看所有预警记录，支持处理预警操作</p>
      </div>
      <el-button type="primary" :icon="Refresh" @click="fetchData" :loading="loading">
        刷新
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input
        v-model="filters.keyword"
        placeholder="搜索批次号 / 产地 / 企业"
        clearable
        style="width: 240px"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-select v-model="filters.alertType" placeholder="预警类型" clearable style="width: 160px">
        <el-option label="温度异常" value="TEMP" />
        <el-option label="湿度异常" value="HUMIDITY" />
        <el-option label="农残超标" value="PESTICIDE" />
        <el-option label="重金属超标" value="HEAVY_METAL" />
        <el-option label="微生物超标" value="MICROBE" />
        <el-option label="综合风险" value="COMPOSITE" />
      </el-select>

      <el-select v-model="filters.handled" placeholder="处理状态" clearable style="width: 140px">
        <el-option label="已处理" :value="true" />
        <el-option label="未处理" :value="false" />
      </el-select>

      <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <!-- 统计概览 -->
    <div class="stat-bar">
      <div class="stat-item">
        <span class="stat-num">{{ tableData.length }}</span>
        <span class="stat-lbl">当前筛选结果</span>
      </div>
      <div class="stat-item unhandled">
        <span class="stat-num">{{ unhandledCount }}</span>
        <span class="stat-lbl">条未处理</span>
      </div>
    </div>

    <!-- 表格 -->
    <div class="table-card">
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="batchNo" label="批次号" min-width="160">
          <template #default="{ row }">
            <span class="batch-link" @click="goToChain(row)">{{ row.batchNo || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="origin" label="产地" min-width="120" />
        <el-table-column prop="enterprise" label="企业" min-width="140" />
        <el-table-column label="预警类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="alertTagType(row.alertType)" size="small" effect="dark">
              {{ alertTypeName(row.alertType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="风险分" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.riskScore != null" class="risk-score">{{ (row.riskScore * 100).toFixed(0) }}</span>
            <span v-else class="no-data">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="预警时间" width="160" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.handled ? 'success' : 'danger'" size="small">
              {{ row.handled ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="!row.handled"
              type="warning"
              link
              size="small"
              @click="handleSingle(row)"
            >
              处理
            </el-button>
            <span v-else class="handled-text">—</span>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && tableData.length === 0" class="empty-tip">
        <el-empty description="暂无预警记录" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { queryAlerts, handleAlert } from '@/api/data'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const chainDrawerVisible = ref(false)
const selectedBatchId = ref(null)

const filters = reactive({
  keyword: '',
  alertType: '',
  handled: null
})

const unhandledCount = computed(() => tableData.value.filter(r => !r.handled).length)

const alertTypeMap = {
  TEMP: '温度异常', HUMIDITY: '湿度异常', PESTICIDE: '农残超标',
  HEAVY_METAL: '重金属超标', MICROBE: '微生物超标', COMPOSITE: '综合风险'
}

const alertTagMap = {
  TEMP: 'danger', HUMIDITY: 'warning', PESTICIDE: 'danger',
  HEAVY_METAL: 'danger', MICROBE: 'danger', COMPOSITE: 'warning'
}

function alertTypeName(type) { return alertTypeMap[type] || type || '未知' }
function alertTagType(type) { return alertTagMap[type] || 'info' }

async function fetchData() {
  loading.value = true
  try {
    const params = {}
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.alertType) params.alertType = filters.alertType
    if (filters.handled !== null) params.handled = filters.handled
    const res = await queryAlerts(params)
    if (res.code === 200) tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

function handleSearch() { fetchData() }
function handleReset() {
  filters.keyword = ''
  filters.alertType = ''
  filters.handled = null
  fetchData()
}

async function handleSingle(row) {
  try {
    await ElMessageBox.confirm(
      `确认处理预警「${alertTypeName(row.alertType)}」（批次：${row.batchNo || row.batchId}）？`,
      '处理预警',
      { confirmButtonText: '确认处理', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  const res = await handleAlert(row.id)
  if (res.code === 200) {
    ElMessage.success('处理成功')
    fetchData()
  } else {
    ElMessage.error(res.message || '处理失败')
  }
}

function goToChain(row) {
  router.push({ path: '/main/trace/chain', query: { batchId: row.batchId } })
}

fetchData()
</script>

<style scoped>
.alert-list-page { display: flex; flex-direction: column; gap: 16px; }

.page-header {
  display: flex; align-items: center; justify-content: space-between;
}
.page-title { margin: 0 0 4px; font-size: 18px; font-weight: 600; color: #1a3a6b; }
.page-subtitle { margin: 0; font-size: 13px; color: #909399; }

.filter-bar {
  display: flex; align-items: center; gap: 12px; padding: 16px 20px;
  background: #ffffff; border-radius: 12px; box-shadow: 0 1px 4px rgba(0,0,0,.06);
}

.stat-bar {
  display: flex; gap: 16px;
}
.stat-item {
  display: flex; align-items: baseline; gap: 6px;
  background: #ffffff; border-radius: 10px; padding: 10px 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
.stat-num { font-size: 22px; font-weight: 800; color: #1a3a6b; }
.stat-lbl { font-size: 13px; color: #909399; }
.stat-item.unhandled .stat-num { color: #f56c6c; }

.table-card {
  background: #ffffff; border-radius: 12px; padding: 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}

.batch-link {
  color: #409eff; cursor: pointer; font-weight: 500;
}
.batch-link:hover { text-decoration: underline; }

.risk-score { font-weight: 700; font-size: 14px; }
.no-data { color: #c0c4cc; }
.handled-text { color: #c0c4cc; }

.empty-tip { padding: 40px 0; }
</style>
