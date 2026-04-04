<template>
  <div class="batch-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">批次查询</h2>
        <p class="page-subtitle">多条件检索溯源批次，查看风险与预警状态</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="goToEntry">新增批次</el-button>
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
        <el-table-column label="修改时间" width="160" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ row.updateTime || row.createTime || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作人" width="100" align="center">
          <template #default="{ row }">
            <span class="operator-text">{{ row.operator || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="goToChain(row)">
              溯源链
            </el-button>
            <el-divider direction="vertical" />
            <el-button type="warning" link size="small" @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-divider direction="vertical" />
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && tableData.length === 0" class="empty-tip">
        <el-empty description="未找到符合条件的批次记录" />
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑批次"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="批次编号">
          <el-input :value="editForm.batchNo" disabled />
        </el-form-item>
        <el-form-item label="产地编码" prop="origin">
          <el-input v-model="editForm.origin" placeholder="如 440000" clearable />
        </el-form-item>
        <el-form-item label="所属企业" prop="enterprise">
          <el-input v-model="editForm.enterprise" placeholder="请输入企业名称" clearable />
        </el-form-item>
        <el-form-item label="生产日期" prop="productionDate">
          <el-date-picker
            v-model="editForm.productionDate"
            type="date"
            placeholder="选择生产日期"
            style="width: 100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit" :loading="saving">确认保存</el-button>
      </template>
    </el-dialog>

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
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { queryBatches, updateBatch, deleteBatch } from '@/api/data'
import TraceChainDetail from '@/components/TraceChainDetail.vue'

const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const chainDrawerVisible = ref(false)
const selectedBatchId = ref(null)
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editForm = reactive({
  id: null,
  batchNo: '',
  origin: '',
  enterprise: '',
  productionDate: ''
})

const filters = reactive({
  keyword: '',
  riskLevel: '',
  alertType: ''
})

const editRules = {
  origin: [{ required: true, message: '请输入产地编码', trigger: 'blur' }],
  enterprise: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  productionDate: [{ required: true, message: '请选择生产日期', trigger: 'change' }]
}

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

function goToChain(row) {
  router.push({ path: '/main/trace/chain', query: { batchId: row.id } })
}

function goToEntry() {
  router.push('/main/batch/entry')
}

function openEditDialog(row) {
  editForm.id = row.id
  editForm.batchNo = row.batchNo
  editForm.origin = row.origin
  editForm.enterprise = row.enterprise
  editForm.productionDate = row.productionDate
  editDialogVisible.value = true
  editFormRef.value?.clearValidate()
}

async function handleSaveEdit() {
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const res = await updateBatch({
      id: editForm.id,
      origin: editForm.origin.trim(),
      enterprise: editForm.enterprise.trim(),
      productionDate: editForm.productionDate
    })
    if (res.code === 200) {
      ElMessage.success('修改成功')
      editDialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch {
    ElMessage.error('修改失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `删除批次「${row.batchNo}」将同时清除该批次已处理的预警和风险评估记录，确认删除？`,
      '删除批次',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'error' }
    )
  } catch {
    return
  }

  const res = await deleteBatch(row.id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    fetchData()
  } else {
    ElMessage.error(res.message || '删除失败')
  }
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

.time-text,
.operator-text {
  font-size: 12px;
  color: #909399;
}

.empty-tip {
  padding: 40px 0;
}
</style>
