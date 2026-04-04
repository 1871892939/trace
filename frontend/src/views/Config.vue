<template>
  <div class="config-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">参数配置</h2>
        <p class="page-subtitle">系统算法与预警阈值的可配置参数，修改后实时生效</p>
      </div>
      <el-button type="primary" :icon="Refresh" @click="fetchData" :loading="loading">
        刷新
      </el-button>
    </div>

    <!-- 分组标签 -->
    <div class="group-tabs">
      <el-radio-group v-model="activeGroup" size="default">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="risk">风险评分</el-radio-button>
        <el-radio-button value="anomaly">异常检测</el-radio-button>
        <el-radio-button value="alert">预警阈值</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-bar">
      <div class="stat-item">
        <span class="stat-num">{{ tableData.length }}</span>
        <span class="stat-lbl">当前参数</span>
      </div>
      <div class="stat-item">
        <span class="stat-num">{{ editableCount }}</span>
        <span class="stat-lbl">可编辑</span>
      </div>
      <div class="stat-item readonly">
        <span class="stat-num">{{ readonlyCount }}</span>
        <span class="stat-lbl">只读</span>
      </div>
    </div>

    <!-- 参数表格 -->
    <div class="table-card">
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%" :row-class-name="tableRowClass">
        <el-table-column label="参数名称" min-width="200">
          <template #default="{ row }">
            <div class="param-name-cell">
              <span class="param-name">{{ row.paramName }}</span>
              <el-tag v-if="row.editable === 0" type="info" size="small" effect="plain">只读</el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="参数标识" min-width="200">
          <template #default="{ row }">
            <code class="param-key">{{ row.paramKey }}</code>
          </template>
        </el-table-column>

        <el-table-column label="参数值" min-width="220">
          <template #default="{ row }">
            <div v-if="row.editable === 1" class="editable-cell">
              <el-input
                v-model="row.editValue"
                size="small"
                style="width: 160px"
                @keyup.enter="saveRow(row)"
                @blur="maybeReset(row)"
                :ref="el => setInputRef(row.id, el)"
              />
              <el-button
                type="primary"
                link
                size="small"
                @click="saveRow(row)"
                :loading="savingId === row.id"
              >
                保存
              </el-button>
            </div>
            <span v-else class="readonly-value">{{ row.paramValue }}</span>
          </template>
        </el-table-column>

        <el-table-column label="单位" width="120" align="center">
          <template #default="{ row }">
            <span class="unit-text">{{ getUnit(row.paramKey) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="说明" min-width="260">
          <template #default="{ row }">
            <span class="desc-text">{{ row.description }}</span>
          </template>
        </el-table-column>

        <el-table-column label="更新时间" width="160" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ row.updateTime || '—' }}</span>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && tableData.length === 0" class="empty-tip">
        <el-empty description="该分组暂无参数" />
      </div>
    </div>

    <!-- 分组说明 -->
    <div class="info-card">
      <div class="info-title">
        <el-icon><InfoFilled /></el-icon>
        分组说明
      </div>
      <ul class="info-list">
        <li><strong>风险评分</strong>：风险评分算法的权重配置、国标限量阈值、风险等级边界值。修改后对新生效批次立即生效。</li>
        <li><strong>异常检测</strong>：3σ 统计异常检测的 σ 系数参数。仅供算法调试使用，修改前请确认影响范围。</li>
        <li><strong>预警阈值</strong>：告警级别划分的分数阈值、综合预警触发条件。</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { Refresh, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getConfigList, updateConfig } from '@/api/config'

const loading = ref(false)
const savingId = ref(null)
const activeGroup = ref('')
const tableData = ref([])
const inputRefs = reactive({})

const editableCount = computed(() => tableData.value.filter(r => r.editable === 1).length)
const readonlyCount = computed(() => tableData.value.filter(r => r.editable === 0).length)

const unitMap = {
  'risk.low.threshold': '分',
  'risk.high.threshold': '分',
  'risk.weight.detection': '%',
  'risk.weight.pesticide': '%',
  'risk.weight.heavy_metal': '%',
  'risk.weight.microbe': '%',
  'risk.weight.temp': '%',
  'risk.weight.humidity': '%',
  'limit.pesticide': 'mg/kg',
  'limit.heavy_metal': 'mg/kg',
  'limit.microbe': 'CFU/g',
  'limit.temp.min': '℃',
  'limit.temp.max': '℃',
  'limit.humidity.min': '%',
  'limit.humidity.max': '%',
  'anomaly.sigma.warning': 'σ',
  'anomaly.sigma.critical': 'σ',
  'alert.score.urgent': '分',
  'alert.score.serious': '分',
  'alert.composite.threshold': '分'
}

function getUnit(key) {
  return unitMap[key] || '—'
}

function tableRowClass({ row }) {
  return row.editable === 0 ? 'readonly-row' : ''
}

function setInputRef(id, el) {
  if (el) inputRefs[id] = el
}

async function fetchData() {
  loading.value = true
  try {
    const group = activeGroup.value || undefined
    const res = await getConfigList(group)
    if (res.code === 200) {
      tableData.value = (res.data || []).map(p => ({ ...p, editValue: p.paramValue }))
    }
  } catch (e) {
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

function maybeReset(row) {
  nextTick(() => {
    if (!inputRefs[row.id]?.$el?.contains(document.activeElement)) return
  })
  setTimeout(() => {
    if (inputRefs[row.id] && document.activeElement !== inputRefs[row.id].$el?.querySelector('input')) {
      row.editValue = row.paramValue
    }
  }, 200)
}

async function saveRow(row) {
  if (row.editValue === row.paramValue) return
  savingId.value = row.id
  try {
    const res = await updateConfig(row.id, row.editValue)
    if (res.code === 200) {
      ElMessage.success(`「${row.paramName}」更新成功`)
      row.paramValue = row.editValue
    } else {
      ElMessage.error(res.message || '更新失败')
      row.editValue = row.paramValue
    }
  } catch {
    ElMessage.error('更新失败')
    row.editValue = row.paramValue
  } finally {
    savingId.value = null
  }
}

watch(activeGroup, () => fetchData())

fetchData()
</script>

<style scoped>
.config-page { display: flex; flex-direction: column; gap: 16px; }

.page-header {
  display: flex; align-items: center; justify-content: space-between;
}
.page-title { margin: 0 0 4px; font-size: 18px; font-weight: 600; color: #1a3a6b; }
.page-subtitle { margin: 0; font-size: 13px; color: #909399; }

.group-tabs {
  background: #ffffff;
  padding: 14px 20px;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}

.stat-bar { display: flex; gap: 16px; }
.stat-item {
  display: flex; align-items: baseline; gap: 6px;
  background: #ffffff; border-radius: 10px; padding: 10px 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
.stat-num { font-size: 22px; font-weight: 800; color: #1a3a6b; }
.stat-lbl { font-size: 13px; color: #909399; }
.stat-item.readonly .stat-num { color: #909399; }

.table-card {
  background: #ffffff; border-radius: 12px; padding: 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}

.param-name-cell { display: flex; align-items: center; gap: 8px; }
.param-name { font-weight: 500; color: #303133; }

.param-key {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  color: #606266;
}

.editable-cell { display: flex; align-items: center; gap: 8px; }

.readonly-value { color: #909399; font-size: 13px; }

.unit-text { color: #c0c4cc; font-size: 12px; }

.desc-text { color: #606266; font-size: 13px; }

.time-text { color: #909399; font-size: 12px; }

:deep(.readonly-row) {
  background-color: #fafafa;
}

.empty-tip { padding: 40px 0; }

.info-card {
  background: #f0f7ff;
  border: 1px solid #d9ecff;
  border-radius: 12px;
  padding: 16px 20px;
}

.info-title {
  display: flex; align-items: center; gap: 6px;
  font-size: 14px; font-weight: 600; color: #1a3a6b;
  margin-bottom: 10px;
}

.info-list {
  margin: 0; padding-left: 20px;
  display: flex; flex-direction: column; gap: 6px;
  font-size: 13px; color: #606266; line-height: 1.6;
}
</style>
