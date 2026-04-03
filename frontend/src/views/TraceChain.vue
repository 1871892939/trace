<template>
  <div class="chain-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">溯源链</h2>
        <p class="page-subtitle">查看批次完整溯源轨迹，从产地到风险评估全链路可视化</p>
      </div>
      <div class="batch-selector">
        <el-select
          v-model="selectedBatchId"
          filterable
          remote
          reserve-keyword
          placeholder="输入批次号或企业名称搜索"
          :remote-method="searchBatches"
          :loading="searchLoading"
          style="width: 320px"
          @change="handleBatchChange"
        >
          <el-option
            v-for="b in searchResults"
            :key="b.id"
            :label="b.batchNo"
            :value="b.id"
          >
            <div class="batch-option">
              <span class="batch-no">{{ b.batchNo }}</span>
              <span class="batch-meta">{{ b.origin }} / {{ b.enterprise }}</span>
            </div>
          </el-option>
        </el-select>
      </div>
    </div>

    <div v-if="!selectedBatchId" class="empty-state">
      <el-empty description="请在上方选择批次以查看溯源链" />
    </div>

    <template v-else-if="chainData">
      <!-- 批次概览卡片 -->
      <div class="batch-overview">
        <div class="overview-tag">
          <el-icon><Guide /></el-icon>
          批次信息
        </div>
        <div class="overview-cards">
          <div class="overview-item">
            <div class="item-label">批次编号</div>
            <div class="item-value">{{ chainData.batch.batchNo }}</div>
          </div>
          <div class="overview-item">
            <div class="item-label">产地</div>
            <div class="item-value">{{ chainData.batch.origin }}</div>
          </div>
          <div class="overview-item">
            <div class="item-label">企业</div>
            <div class="item-value">{{ chainData.batch.enterprise }}</div>
          </div>
          <div class="overview-item">
            <div class="item-label">生产日期</div>
            <div class="item-value">{{ chainData.batch.productionDate || '—' }}</div>
          </div>
          <div class="overview-item" v-if="chainData.risk">
            <div class="item-label">风险等级</div>
            <div class="item-value">
              <el-tag :type="riskTagType(chainData.risk.riskLevel)" effect="dark" size="small">
                {{ chainData.risk.riskLevel }}
              </el-tag>
            </div>
          </div>
          <div class="overview-item" v-if="chainData.risk">
            <div class="item-label">风险评分</div>
            <div class="item-value">
              <span class="score-badge" :class="riskScoreClass(chainData.risk.riskScore)">
                {{ chainData.risk.riskScore }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 溯源链时间线 -->
      <div class="chain-timeline">
        <!-- 检测节点 -->
        <div v-if="chainData.detection" class="timeline-node detection-node">
          <div class="node-icon detection-icon">
            <svg viewBox="0 0 24 24" fill="none"><path d="M9 3H5a2 2 0 00-2 2v4m6-6h10a2 2 0 012 2v4M9 3v18m0 0h10a2 2 0 002-2V9m-12 12H5a2 2 0 01-2-2V9m0 0h18" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </div>
          <div class="node-body">
            <div class="node-header">
              <span class="node-title">检测环节</span>
              <span class="node-time">{{ chainData.detection.testTime }}</span>
            </div>
            <div class="node-content">
              <div class="detect-grid">
                <div class="detect-item">
                  <span class="detect-label">农残</span>
                  <span class="detect-val">{{ chainData.detection.pesticide }} mg/kg</span>
                </div>
                <div class="detect-item">
                  <span class="detect-label">重金属</span>
                  <span class="detect-val">{{ chainData.detection.heavyMetal }} mg/kg</span>
                </div>
                <div class="detect-item">
                  <span class="detect-label">微生物</span>
                  <span class="detect-val">{{ chainData.detection.microbe }} CFU/g</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 物流节点 -->
        <div v-if="chainData.logistics && chainData.logistics.length > 0" class="timeline-node logistics-node">
          <div class="node-icon logistics-icon">
            <svg viewBox="0 0 24 24" fill="none"><path d="M8 9l3 3-3 3m5 0h3M5 20h14a2 2 0 002-2V6a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </div>
          <div class="node-body">
            <div class="node-header">
              <span class="node-title">物流轨迹</span>
              <span class="node-badge">共 {{ chainData.logistics.length }} 条记录</span>
            </div>
            <div class="logistics-scroll">
              <div v-for="(log, idx) in chainData.logistics" :key="log.id" class="logistics-item">
                <div class="log-index">{{ idx + 1 }}</div>
                <div class="log-info">
                  <div class="log-row">
                    <span class="log-label">GPS</span>
                    <span class="log-val">{{ log.gpsLng }}, {{ log.gpsLat }}</span>
                  </div>
                  <div class="log-row">
                    <span class="log-label">温度</span>
                    <span class="log-val" :class="{ 'temp-warn': isTempAnomaly(log.temperature) }">
                      {{ log.temperature }}°C
                    </span>
                  </div>
                  <div class="log-row">
                    <span class="log-label">湿度</span>
                    <span class="log-val" :class="{ 'hum-warn': isHumAnomaly(log.humidity) }">
                      {{ log.humidity }}%
                    </span>
                  </div>
                  <div class="log-time">{{ log.recordTime }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 风险评估节点 -->
        <div v-if="chainData.risk" class="timeline-node risk-node">
          <div class="node-icon risk-icon">
            <svg viewBox="0 0 24 24" fill="none"><path d="M12 9v4m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </div>
          <div class="node-body">
            <div class="node-header">
              <span class="node-title">风险评估</span>
              <el-tag :type="riskTagType(chainData.risk.riskLevel)" effect="dark" size="small">
                {{ chainData.risk.riskLevel }}
              </el-tag>
            </div>
            <div class="risk-detail">
              <div class="risk-score-display">
                <div class="score-ring" :class="riskScoreClass(chainData.risk.riskScore)">
                  <span class="score-number">{{ chainData.risk.riskScore }}</span>
                  <span class="score-unit">分</span>
                </div>
                <div class="score-desc">{{ riskScoreDesc(chainData.risk.riskScore) }}</div>
              </div>
              <div v-if="chainData.risk.factors" class="risk-factors">
                <div class="factors-label">风险因素明细</div>
                <pre class="factors-json">{{ chainData.risk.factors }}</pre>
              </div>
            </div>
          </div>
        </div>

        <!-- 预警节点 -->
        <div v-if="chainData.alerts && chainData.alerts.length > 0" class="timeline-node alert-node">
          <div class="node-icon alert-icon">
            <svg viewBox="0 0 24 24" fill="none"><path d="M15 17H5a2 2 0 01-2-2V5a2 2 0 012-2h14a2 2 0 012 2v10l-4-4zM12 9v4M12 17h.01" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </div>
          <div class="node-body">
            <div class="node-header">
              <span class="node-title">预警记录</span>
              <span class="node-badge">{{ chainData.alerts.length }} 条</span>
            </div>
            <div class="alert-list">
              <div v-for="alert in chainData.alerts" :key="alert.id" class="alert-item">
                <div class="alert-header">
                  <el-tag :type="alert.handled ? 'success' : 'danger'" size="small">
                    {{ alertTypeName(alert.alertType) }}
                  </el-tag>
                  <span class="alert-status">{{ alert.handled ? '已处理' : '未处理' }}</span>
                  <span class="alert-time">{{ alert.createTime }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 无任何记录 -->
        <div v-if="!chainData.detection && (!chainData.logistics || chainData.logistics.length === 0) && !chainData.risk && (!chainData.alerts || chainData.alerts.length === 0)" class="no-data-node">
          <el-empty description="该批次暂无任何溯源记录，请先在数据模拟中生成测试数据" />
        </div>
      </div>
    </template>

    <div v-else-if="loading" class="loading-area">
      <el-skeleton :rows="8" animated />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Guide } from '@element-plus/icons-vue'
import { getTraceChain, queryBatches } from '@/api/data'
import { ElMessage } from 'element-plus'

const route = useRoute()

const selectedBatchId = ref(null)
const chainData = ref(null)
const loading = ref(false)
const searchResults = ref([])
const searchLoading = ref(false)

const alertTypeMap = {
  TEMP: '温度异常',
  HUMIDITY: '湿度异常',
  PESTICIDE: '农残超标',
  HEAVY_METAL: '重金属超标',
  MICROBE: '微生物超标',
  COMPOSITE: '综合风险'
}

function alertTypeName(type) {
  return alertTypeMap[type] || type || '未知'
}

function riskTagType(level) {
  const map = { Low: 'success', Medium: 'warning', High: 'danger' }
  return map[level] || 'info'
}

function riskScoreClass(score) {
  if (score == null) return ''
  if (score <= 40) return 'score-low'
  if (score <= 70) return 'score-medium'
  return 'score-high'
}

function riskScoreDesc(score) {
  if (score == null) return ''
  if (score <= 40) return '低风险，食品符合安全标准'
  if (score <= 70) return '中风险，建议关注并复核检测结果'
  return '高风险，存在明显安全隐患，需立即处理'
}

function isTempAnomaly(temp) {
  if (temp == null) return false
  return temp > 8 || temp < 0
}

function isHumAnomaly(hum) {
  if (hum == null) return false
  return hum > 85 || hum < 40
}

async function searchBatches(query) {
  if (!query) {
    searchResults.value = []
    return
  }
  searchLoading.value = true
  try {
    const res = await queryBatches({ keyword: query })
    if (res.code === 200) {
      searchResults.value = (res.data || []).slice(0, 20)
    }
  } finally {
    searchLoading.value = false
  }
}

async function fetchChain() {
  if (!selectedBatchId.value) return
  loading.value = true
  chainData.value = null
  try {
    const res = await getTraceChain(selectedBatchId.value)
    if (res.code === 200) {
      chainData.value = res.data
    } else if (res.code === 404) {
      ElMessage.warning('批次不存在')
    }
  } finally {
    loading.value = false
  }
}

function handleBatchChange(id) {
  if (id) {
    fetchChain()
  } else {
    chainData.value = null
  }
}

onMounted(() => {
  const batchId = route.query.batchId
  if (batchId) {
    selectedBatchId.value = Number(batchId)
    fetchChain()
  }
})
</script>

<style scoped>
.chain-page {
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

.empty-state {
  background: #ffffff;
  border-radius: 12px;
  padding: 60px 20px;
  text-align: center;
}

.batch-overview {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.overview-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #1a3a6b;
  margin-bottom: 16px;
  letter-spacing: 1px;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
}

.overview-item {
  background: #f8fafc;
  border-radius: 8px;
  padding: 12px 14px;
}

.item-label {
  font-size: 11px;
  color: #909399;
  margin-bottom: 4px;
  letter-spacing: 0.5px;
}

.item-value {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.score-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 700;
}
.score-low  { background: #f0f9eb; color: #67c23a; }
.score-medium { background: #fdf6ec; color: #e6a23c; }
.score-high { background: #fef0f0; color: #f56c6c; }

.chain-timeline {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.timeline-node {
  display: flex;
  gap: 16px;
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.node-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.node-icon svg {
  width: 22px;
  height: 22px;
}
.detection-icon { background: #ecf5ff; color: #409eff; }
.logistics-icon { background: #f0f9eb; color: #67c23a; }
.risk-icon { background: #fef0f0; color: #f56c6c; }
.alert-icon { background: #fdf6ec; color: #e6a23c; }

.node-body {
  flex: 1;
  min-width: 0;
}

.node-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.node-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.node-time, .node-badge {
  font-size: 12px;
  color: #909399;
}

/* 检测 */
.detect-grid {
  display: flex;
  gap: 16px;
}

.detect-item {
  background: #f8fafc;
  border-radius: 8px;
  padding: 10px 14px;
  flex: 1;
}

.detect-label {
  display: block;
  font-size: 11px;
  color: #909399;
  margin-bottom: 4px;
}

.detect-val {
  font-size: 15px;
  font-weight: 600;
  color: #409eff;
}

/* 物流 */
.logistics-scroll {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 280px;
  overflow-y: auto;
}

.logistics-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.log-index {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #f0f9eb;
  color: #67c23a;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
}

.log-info {
  flex: 1;
  background: #f8fafc;
  border-radius: 8px;
  padding: 8px 12px;
}

.log-row {
  display: flex;
  gap: 8px;
  font-size: 13px;
  line-height: 1.8;
}

.log-label {
  color: #909399;
  min-width: 40px;
}

.log-val {
  color: #303133;
  font-weight: 500;
}

.temp-warn { color: #f56c6c !important; font-weight: 700; }
.hum-warn  { color: #e6a23c !important; font-weight: 700; }

.log-time {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 2px;
}

/* 风险 */
.risk-detail {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.risk-score-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.score-ring {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 4px solid;
}
.score-low .score-ring  { border-color: #67c23a; background: #f0f9eb; }
.score-medium .score-ring { border-color: #e6a23c; background: #fdf6ec; }
.score-high .score-ring { border-color: #f56c6c; background: #fef0f0; }

.score-number {
  font-size: 22px;
  font-weight: 800;
  line-height: 1;
}
.score-low .score-number  { color: #67c23a; }
.score-medium .score-number { color: #e6a23c; }
.score-high .score-number { color: #f56c6c; }

.score-unit {
  font-size: 11px;
  color: #909399;
}

.score-desc {
  font-size: 12px;
  color: #606266;
  text-align: center;
  max-width: 100px;
}

.risk-factors {
  flex: 1;
}

.factors-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.factors-json {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 12px;
  color: #606266;
  font-family: 'Courier New', monospace;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
}

/* 预警 */
.alert-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.alert-item {
  background: #fdf6ec;
  border-radius: 8px;
  padding: 10px 14px;
}

.alert-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.alert-status {
  font-size: 12px;
  color: #606266;
}

.alert-time {
  font-size: 11px;
  color: #c0c4cc;
  margin-left: auto;
}

.no-data-node {
  background: #ffffff;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
}

.loading-area {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
}

/* 批次搜索选项 */
.batch-option {
  display: flex;
  align-items: center;
  gap: 8px;
}
.batch-no {
  font-weight: 600;
  color: #303133;
}
.batch-meta {
  font-size: 12px;
  color: #909399;
}
</style>
