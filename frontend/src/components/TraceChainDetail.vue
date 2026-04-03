<template>
  <div class="chain-detail">
    <div v-if="loading" class="loading">
      <el-skeleton :rows="6" animated />
    </div>
    <div v-else-if="!chainData" class="empty">
      <el-empty description="暂无溯源数据" />
    </div>
    <template v-else>
      <!-- 批次信息 -->
      <div class="section">
        <div class="section-title">批次信息</div>
        <div class="info-grid">
          <div class="info-item"><span class="lbl">批次号</span><span class="val">{{ chainData.batch.batchNo }}</span></div>
          <div class="info-item"><span class="lbl">产地</span><span class="val">{{ chainData.batch.origin }}</span></div>
          <div class="info-item"><span class="lbl">企业</span><span class="val">{{ chainData.batch.enterprise }}</span></div>
          <div class="info-item"><span class="lbl">生产日期</span><span class="val">{{ chainData.batch.productionDate || '—' }}</span></div>
        </div>
      </div>

      <!-- 检测数据 -->
      <div v-if="chainData.detection" class="section">
        <div class="section-title">检测数据</div>
        <div class="detect-grid">
          <div class="detect-card">
            <div class="detect-lbl">农残</div>
            <div class="detect-val">{{ chainData.detection.pesticide }} <span class="unit">mg/kg</span></div>
          </div>
          <div class="detect-card">
            <div class="detect-lbl">重金属</div>
            <div class="detect-val">{{ chainData.detection.heavyMetal }} <span class="unit">mg/kg</span></div>
          </div>
          <div class="detect-card">
            <div class="detect-lbl">微生物</div>
            <div class="detect-val">{{ chainData.detection.microbe }} <span class="unit">CFU/g</span></div>
          </div>
        </div>
        <div class="meta-row">检测时间：{{ chainData.detection.testTime }}</div>
      </div>

      <!-- 物流轨迹 -->
      <div v-if="chainData.logistics && chainData.logistics.length > 0" class="section">
        <div class="section-title">物流轨迹 <span class="count-tag">{{ chainData.logistics.length }}条</span></div>
        <div class="logistics-list">
          <div v-for="(log, idx) in chainData.logistics" :key="log.id" class="log-item">
            <div class="log-idx">{{ idx + 1 }}</div>
            <div class="log-body">
              <div class="log-row">
                <span class="lbl">GPS</span>
                <span class="val">{{ log.gpsLng }}, {{ log.gpsLat }}</span>
              </div>
              <div class="log-row">
                <span class="lbl">温度</span>
                <span class="val" :class="{ warn: isTempAnomaly(log.temperature) }">{{ log.temperature }}°C</span>
              </div>
              <div class="log-row">
                <span class="lbl">湿度</span>
                <span class="val" :class="{ warn: isHumAnomaly(log.humidity) }">{{ log.humidity }}%</span>
              </div>
              <div class="meta-row">{{ log.recordTime }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 风险评估 -->
      <div v-if="chainData.risk" class="section">
        <div class="section-title">风险评估</div>
        <div class="risk-row">
          <el-tag :type="riskTagType(chainData.risk.riskLevel)" effect="dark" size="small">
            {{ chainData.risk.riskLevel }}
          </el-tag>
          <span class="risk-score" :class="riskScoreClass(chainData.risk.riskScore)">
            {{ chainData.risk.riskScore }} 分
          </span>
        </div>
        <div v-if="chainData.risk.factors" class="factors-box">
          <pre class="factors-pre">{{ chainData.risk.factors }}</pre>
        </div>
      </div>

      <!-- 预警记录 -->
      <div v-if="chainData.alerts && chainData.alerts.length > 0" class="section">
        <div class="section-title">预警记录 <span class="count-tag">{{ chainData.alerts.length }}条</span></div>
        <div class="alert-list">
          <div v-for="alert in chainData.alerts" :key="alert.id" class="alert-item">
            <el-tag :type="alert.handled ? 'success' : 'danger'" size="small">
              {{ alertTypeName(alert.alertType) }}
            </el-tag>
            <span class="alert-handled">{{ alert.handled ? '已处理' : '未处理' }}</span>
            <span class="meta-row" style="margin-left: auto">{{ alert.createTime }}</span>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTraceChain } from '@/api/data'
import { ElMessage } from 'element-plus'

const props = defineProps({
  batchId: {
    type: Number,
    required: true
  }
})

const chainData = ref(null)
const loading = ref(false)

const alertTypeMap = {
  TEMP: '温度异常', HUMIDITY: '湿度异常', PESTICIDE: '农残超标',
  HEAVY_METAL: '重金属超标', MICROBE: '微生物超标', COMPOSITE: '综合风险'
}
function alertTypeName(type) { return alertTypeMap[type] || type || '未知' }
function riskTagType(level) {
  return ({ Low: 'success', Medium: 'warning', High: 'danger' })[level] || 'info'
}
function riskScoreClass(score) {
  if (score == null) return ''
  if (score <= 40) return 'score-low'
  if (score <= 70) return 'score-medium'
  return 'score-high'
}
function isTempAnomaly(temp) { return temp != null && (temp > 8 || temp < 0) }
function isHumAnomaly(hum) { return hum != null && (hum > 85 || hum < 40) }

async function fetchData() {
  loading.value = true
  try {
    const res = await getTraceChain(props.batchId)
    if (res.code === 200) chainData.value = res.data
    else if (res.code === 404) ElMessage.warning('批次不存在')
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.chain-detail { padding: 0 4px; }
.loading, .empty { padding: 24px 0; }
.section { margin-bottom: 20px; }
.section-title {
  font-size: 13px;
  font-weight: 600;
  color: #1a3a6b;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  letter-spacing: 1px;
}
.count-tag {
  font-size: 11px;
  font-weight: 400;
  color: #909399;
  background: #f0f4f8;
  padding: 1px 8px;
  border-radius: 10px;
}
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.info-item { background: #f8fafc; border-radius: 8px; padding: 8px 12px; }
.lbl { display: block; font-size: 11px; color: #909399; margin-bottom: 2px; }
.val { font-size: 13px; font-weight: 600; color: #303133; }
.detect-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin-bottom: 8px; }
.detect-card { background: #ecf5ff; border-radius: 8px; padding: 8px 12px; text-align: center; }
.detect-lbl { font-size: 11px; color: #409eff; margin-bottom: 4px; }
.detect-val { font-size: 14px; font-weight: 700; color: #303133; }
.unit { font-size: 11px; font-weight: 400; color: #909399; }
.meta-row { font-size: 11px; color: #c0c4cc; margin-top: 4px; }
.logistics-list { display: flex; flex-direction: column; gap: 8px; }
.log-item { display: flex; gap: 10px; }
.log-idx {
  width: 22px; height: 22px; border-radius: 50%;
  background: #f0f9eb; color: #67c23a; font-size: 11px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0; margin-top: 2px;
}
.log-body { flex: 1; background: #f8fafc; border-radius: 8px; padding: 8px 12px; }
.log-row { display: flex; gap: 8px; font-size: 12px; line-height: 1.8; }
.warn { color: #f56c6c !important; font-weight: 700; }
.risk-row { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.risk-score { font-size: 18px; font-weight: 800; }
.score-low { color: #67c23a; }
.score-medium { color: #e6a23c; }
.score-high { color: #f56c6c; }
.factors-box { background: #f5f7fa; border-radius: 8px; padding: 10px 12px; }
.factors-pre { font-size: 12px; color: #606266; font-family: 'Courier New', monospace; white-space: pre-wrap; word-break: break-all; margin: 0; }
.alert-list { display: flex; flex-direction: column; gap: 6px; }
.alert-item { background: #fdf6ec; border-radius: 8px; padding: 8px 12px; display: flex; align-items: center; gap: 8px; }
.alert-handled { font-size: 12px; color: #606266; }
</style>
