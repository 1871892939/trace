<template>
  <div class="batch-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">批次查询</h2>
        <p class="page-subtitle">多条件检索溯源批次，查看风险与预警状态</p>
      </div>
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

      <el-select v-model="filters.riskLevel" placeholder="风险等级" clearable style="width: 140px">
        <el-option label="低风险 Low" value="Low" />
        <el-option label="中风险 Medium" value="Medium" />
        <el-option label="高风险 High" value="High" />
      </el-select>

      <el-select v-model="filters.alertType" placeholder="预警类型" clearable style="width: 160px">
        <el-option label="温度异常" value="TEMP" />
        <el-option label="湿度异常" value="HUMIDITY" />
        <el-option label="农残超标" value="PESTICIDE" />
        <el-option label="重金属超标" value="HEAVY_METAL" />
        <el-option label="微生物超标" value="MICROBE" />
        <el-option label="综合风险" value="COMPOSITE" />
      </el-select>

      <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <!-- 数据表格 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
        @row-click="handleViewChain"
      >
        <el-table-column prop="batchNo" label="批次号" min-width="160" />
        <el-table-column prop="origin" label="产地" min-width="120" />
        <el-table-column prop="enterprise" label="企业" min-width="140" />
        <el-table-column prop="productionDate" label="生产日期" width="120" />
        <el-table-column label="风险等级" width="110">
          <template #default="{ row }">
            <el-tag
              v-if="row.riskLevel && row.riskLevel !== 'Unknown'"
              :type="riskLevelType(row.riskLevel)"
              effect="dark"
              size="small"
            >
              {{ row.riskLevel }}
            </el-tag>
            <el-tag v-else type="info" size="small">无记录</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="风险评分" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.riskScore != null" class="risk-score">{{ row.riskScore }}</span>
            <span v-else class="no-data">—</span>
          </template>
        </el-table-column>
        <el-table-column label="预警状态" width="110" align="center">
          <template #default="{ row }">
            <template v-if="row.hasAlert">
              <el-tag :type="row.handled ? 'success' : 'danger'" size="small">
                {{ row.handled ? '已处理' : '未处理' }}
              </el-tag>
            </template>
            <template v-else>
              <el-tag type="success" size="small" plain>正常</el-tag>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="预警类型" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.alertType" class="alert-type">{{ alertTypeName(row.alertType) }}</span>
            <span v-else class="no-data">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click.stop="goToChain(row)">
              查看溯源链
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && tableData.length === 0" class="empty-tip">
        <el-empty description="未找到符合条件的批次记录" />
      </div>
    </div>

    <!-- 溯源链抽屉 -->
    <el-drawer
      v-model="chainDrawerVisible"
      title="溯源链详情"
      size="600px"
      direction="rtl"
      :before-close="() => { chainDrawerVisible = false }"
    >
      <TraceChainDetail v-if="chainDrawerVisible" :batch-id="selectedBatchId" />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Refresh } from '@element-plus/icons-vue'
import { queryBatches } from '@/api/data'
import TraceChainDetail from '@/components/TraceChainDetail.vue'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const chainDrawerVisible = ref(false)
const selectedBatchId = ref(null)

const filters = reactive({
  keyword: '',
  riskLevel: '',
  alertType: ''
})

const alertTypeMap = {
  TEMP: '温度异常',
  HUMIDITY: '湿度异常',
  PESTICIDE: '农残超标',
  HEAVY_METAL: '重金属超标',
  MICROBE: '微生物超标',
  COMPOSITE: '综合风险'
}

function alertTypeName(type) {
  return alertTypeMap[type] || type
}

function riskLevelType(level) {
  const map = { Low: 'success', Medium: 'warning', High: 'danger' }
  return map[level] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await queryBatches({
      keyword: filters.keyword || undefined,
      riskLevel: filters.riskLevel || undefined,
      alertType: filters.alertType || undefined
    })
    if (res.code === 200) {
      tableData.value = res.data || []
    }
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  fetchData()
}

function handleReset() {
  filters.keyword = ''
  filters.riskLevel = ''
  filters.alertType = ''
  fetchData()
}

function handleViewChain(row) {
  selectedBatchId.value = row.id
  chainDrawerVisible.value = true
}

function goToChain(row) {
  router.push({ path: '/main/trace/chain', query: { batchId: row.id } })
}

fetchData()
</script>

<style scoped>
.batch-page {
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

.table-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.risk-score {
  font-weight: 600;
  font-size: 14px;
}

.alert-type {
  font-size: 13px;
  color: #606266;
}

.no-data {
  color: #c0c4cc;
}

.empty-tip {
  padding: 40px 0;
}
</style>
