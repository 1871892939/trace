<template>
  <div class="alert-dashboard">
    <div class="page-header">
      <div>
        <h2 class="page-title">预警大盘</h2>
        <p class="page-subtitle">预警处理追踪与批次关联分析，实时掌握预警处置情况</p>
      </div>
      <el-button type="primary" :icon="Refresh" @click="fetchData" :loading="loading">刷新数据</el-button>
    </div>

    <div v-if="loading && !dashData" class="loading-area">
      <el-skeleton :rows="10" animated />
    </div>

    <template v-else-if="dashData">
      <!-- 处理率 + 处理时效统计 -->
      <div class="handle-stats">
        <div class="rate-card">
          <div class="rate-ring" :class="rateClass">
            <span class="rate-num">{{ handleRateNum }}</span>
            <span class="rate-pct">%</span>
          </div>
          <div class="rate-info">
            <div class="rate-title">处理率</div>
            <div class="rate-desc">
              <span class="ok">{{ dashData.handledCount }}</span>
              <span class="sep"> / </span>
              <span class="total">{{ dashData.totalCount }}</span>
              <span class="unit">条已处理</span>
            </div>
          </div>
        </div>

        <div class="time-card">
          <div class="time-title">处理时效</div>
          <div class="time-rows">
            <div class="time-row">
              <span class="time-val">{{ dashData.handleTimeStats?.todayHandleCount ?? 0 }}</span>
              <span class="time-lbl">今日处理</span>
            </div>
            <div class="time-row">
              <span class="time-val">{{ dashData.handleTimeStats?.weekHandleCount ?? 0 }}</span>
              <span class="time-lbl">本周处理</span>
            </div>
            <div class="time-row">
              <span class="time-val">{{ dashData.handleTimeStats?.monthHandleCount ?? 0 }}</span>
              <span class="time-lbl">本月处理</span>
            </div>
            <div class="time-row highlight">
              <span class="time-val">{{ dashData.handleTimeStats?.avgHandleHours ?? 0 }}h</span>
              <span class="time-lbl">平均处理时长</span>
            </div>
          </div>
        </div>

        <div class="stat-mini-cards">
          <div class="mini-card total-card">
            <div class="mini-num">{{ dashData.totalCount }}</div>
            <div class="mini-lbl">预警总数</div>
          </div>
          <div class="mini-card unhandled-card">
            <div class="mini-num">{{ dashData.unhandledCount }}</div>
            <div class="mini-lbl">待处理</div>
          </div>
          <div class="mini-card handled-card">
            <div class="mini-num">{{ dashData.handledCount }}</div>
            <div class="mini-lbl">已处理</div>
          </div>
        </div>
      </div>

      <!-- 预警等级分布 + 近7天趋势 -->
      <div class="mid-row">
        <!-- 预警等级分布 -->
        <div class="card">
          <div class="card-title">预警等级分布</div>
          <div class="level-bars">
            <div class="level-item urgent">
              <div class="level-header">
                <span class="level-name">紧急</span>
                <span class="level-count">{{ dashData.levelDistribution?.urgent ?? 0 }}</span>
              </div>
              <el-progress
                :percentage="levelPercent(dashData.levelDistribution?.urgent)"
                :stroke-width="10"
                :show-text="false"
                color="#f56c6c"
              />
            </div>
            <div class="level-item serious">
              <div class="level-header">
                <span class="level-name">重要</span>
                <span class="level-count">{{ dashData.levelDistribution?.serious ?? 0 }}</span>
              </div>
              <el-progress
                :percentage="levelPercent(dashData.levelDistribution?.serious)"
                :stroke-width="10"
                :show-text="false"
                color="#e6a23c"
              />
            </div>
            <div class="level-item normal">
              <div class="level-header">
                <span class="level-name">一般</span>
                <span class="level-count">{{ dashData.levelDistribution?.normal ?? 0 }}</span>
              </div>
              <el-progress
                :percentage="levelPercent(dashData.levelDistribution?.normal)"
                :stroke-width="10"
                :show-text="false"
                color="#67c23a"
              />
            </div>
          </div>
          <div class="level-tip">
            紧急：风险分≥0.8 &nbsp;|&nbsp; 重要：0.5≤风险分&lt;0.8 &nbsp;|&nbsp; 一般：风险分&lt;0.5
          </div>
        </div>

        <!-- 近7天趋势 -->
        <div class="card flex-1">
          <div class="card-title">近7天预警趋势</div>
          <div class="trend-chart">
            <div class="trend-bars">
              <div
                v-for="(val, idx) in dashData.weekTrend"
                :key="idx"
                class="trend-col"
              >
                <div class="bar-wrap">
                  <div
                    class="trend-bar"
                    :style="{ height: barHeight(val) + 'px' }"
                  >
                    <span class="bar-val">{{ val }}</span>
                  </div>
                </div>
                <span class="trend-label">{{ dashData.trendLabels?.[idx] || '' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- TOP批次 + 预警类型分布 -->
      <div class="mid-row">
        <!-- TOP 预警批次 -->
        <div class="card">
          <div class="card-title">TOP 预警批次</div>
          <div v-if="dashData.topAlertBatches?.length > 0" class="top-list">
            <div
              v-for="(b, idx) in dashData.topAlertBatches"
              :key="b.batchId"
              class="top-item"
              @click="goToChain(b.batchId)"
            >
              <div class="top-rank" :class="`rank-${idx + 1}`">{{ idx + 1 }}</div>
              <div class="top-info">
                <div class="top-batch-no">{{ b.batchNo || b.batchId }}</div>
                <div class="top-meta">{{ b.origin }} / {{ b.enterprise }}</div>
              </div>
              <div class="top-right">
                <span class="top-count">{{ b.alertCount }}条</span>
                <el-tag v-if="b.hasUnhandle" type="danger" size="small" effect="dark">未处理</el-tag>
                <el-tag v-else type="success" size="small">已清零</el-tag>
              </div>
            </div>
          </div>
          <div v-else class="empty-mini">暂无数据</div>
        </div>

        <!-- 预警类型分布 -->
        <div class="card">
          <div class="card-title">预警类型分布</div>
          <div class="type-list">
            <div v-for="(count, type) in dashData.typeDistribution" :key="type" class="type-row">
              <el-tag :type="typeTagType(type)" size="small" effect="dark">{{ alertTypeName(type) }}</el-tag>
              <span class="type-count">{{ count }}</span>
              <el-progress
                :percentage="typePercent(count)"
                :stroke-width="8"
                :show-text="false"
                :color="typeColor(type)"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 未处理预警列表 -->
      <div class="card">
        <div class="card-title">
          待处理预警
          <el-tag type="danger" effect="dark" size="small">{{ dashData.unhandledCount }}条</el-tag>
        </div>
        <div v-if="dashData.recentUnhandled?.length > 0" class="unhandled-list">
          <div
            v-for="alert in dashData.recentUnhandled"
            :key="alert.id"
            class="unhandled-item"
            @click="goToChain(alert.batchId)"
          >
            <div class="uh-left">
              <el-tag :type="alertTagType(alert.alertType)" size="small" effect="dark">
                {{ alertTypeName(alert.alertType) }}
              </el-tag>
              <span class="uh-batch">{{ alert.batchNo || alert.batchId }}</span>
              <span class="uh-meta">{{ alert.origin }} / {{ alert.enterprise }}</span>
            </div>
            <div class="uh-right">
              <span class="uh-time">{{ alert.createTime }}</span>
              <span class="uh-score">{{ alert.riskScore != null ? (alert.riskScore * 100).toFixed(0) + '分' : '' }}</span>
            </div>
          </div>
          <div class="view-more" @click="goToAlertList">查看全部预警列表 →</div>
        </div>
        <div v-else class="empty-mini">暂无未处理预警，系统运行良好</div>
      </div>
    </template>

    <el-empty v-else description="暂无预警数据，请先在数据模拟中生成测试数据" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import { getAlertDashboard } from '@/api/data'

const router = useRouter()
const loading = ref(false)
const dashData = ref(null)

const maxTrend = computed(() => {
  if (!dashData.value?.weekTrend?.length) return 1
  return Math.max(...dashData.value.weekTrend, 1)
})

const handleRateNum = computed(() => {
  if (!dashData.value?.totalCount) return 0
  return Math.round(dashData.value.handledCount * 100 / dashData.value.totalCount)
})

const rateClass = computed(() => {
  const r = handleRateNum.value
  if (r >= 80) return 'rate-high'
  if (r >= 50) return 'rate-mid'
  return 'rate-low'
})

function levelPercent(count) {
  if (!dashData.value?.totalCount) return 0
  return Math.round((count || 0) * 100 / dashData.value.totalCount)
}

function typePercent(count) {
  if (!dashData.value?.totalCount) return 0
  return Math.round((count || 0) * 100 / dashData.value.totalCount)
}

function barHeight(val) {
  return Math.max(Math.round((val / maxTrend.value) * 80), val > 0 ? 8 : 0)
}

function alertTypeName(type) {
  const m = {
    TEMP: '温度异常', HUMIDITY: '湿度异常', PESTICIDE: '农残超标',
    HEAVY_METAL: '重金属超标', MICROBE: '微生物超标', COMPOSITE: '综合风险'
  }
  return m[type] || type || '未知'
}

function typeTagType(type) {
  const m = {
    TEMP: 'danger', HUMIDITY: 'warning', PESTICIDE: 'danger',
    HEAVY_METAL: 'danger', MICROBE: 'danger', COMPOSITE: 'warning'
  }
  return m[type] || 'info'
}

function typeColor(type) {
  const m = {
    TEMP: '#f56c6c', HUMIDITY: '#e6a23c', PESTICIDE: '#f56c6c',
    HEAVY_METAL: '#f56c6c', MICROBE: '#f56c6c', COMPOSITE: '#e6a23c'
  }
  return m[type] || '#409eff'
}

function alertTagType(type) {
  const m = {
    TEMP: 'danger', HUMIDITY: 'warning', PESTICIDE: 'danger',
    HEAVY_METAL: 'danger', MICROBE: 'danger', COMPOSITE: 'warning'
  }
  return m[type] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getAlertDashboard()
    if (res.code === 200) dashData.value = res.data
  } finally {
    loading.value = false
  }
}

function goToChain(batchId) {
  router.push({ path: '/main/trace/chain', query: { batchId } })
}

function goToAlertList() {
  router.push({ path: '/main/alert/list' })
}

onMounted(fetchData)
</script>

<style scoped>
.alert-dashboard { display: flex; flex-direction: column; gap: 16px; }

.page-header {
  display: flex; align-items: center; justify-content: space-between;
}
.page-title { margin: 0 0 4px; font-size: 18px; font-weight: 600; color: #1a3a6b; }
.page-subtitle { margin: 0; font-size: 13px; color: #909399; }

/* 处理率 + 时效 */
.handle-stats {
  display: grid;
  grid-template-columns: auto 1fr 1fr;
  gap: 16px;
  align-items: stretch;
}

.rate-card {
  background: #ffffff; border-radius: 12px; padding: 20px 24px;
  display: flex; align-items: center; gap: 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
.rate-ring {
  width: 80px; height: 80px; border-radius: 50%;
  display: flex; align-items: baseline; justify-content: center;
  border: 6px solid; flex-shrink: 0;
}
.rate-high { border-color: #67c23a; background: #f0f9eb; }
.rate-mid  { border-color: #e6a23c; background: #fdf6ec; }
.rate-low  { border-color: #f56c6c; background: #fef0f0; }
.rate-num { font-size: 28px; font-weight: 800; line-height: 1; }
.rate-high .rate-num { color: #67c23a; }
.rate-mid  .rate-num { color: #e6a23c; }
.rate-low  .rate-num { color: #f56c6c; }
.rate-pct  { font-size: 16px; color: #909399; margin-left: 2px; }
.rate-title { font-size: 15px; font-weight: 600; color: #303133; margin-bottom: 6px; }
.rate-desc { font-size: 13px; color: #606266; }
.rate-desc .ok { color: #67c23a; font-weight: 700; }
.rate-desc .sep { color: #c0c4cc; }
.rate-desc .total { color: #303133; font-weight: 600; }
.rate-desc .unit { color: #909399; }

.time-card {
  background: #ffffff; border-radius: 12px; padding: 16px 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
.time-title { font-size: 14px; font-weight: 600; color: #303133; margin-bottom: 12px; }
.time-rows { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.time-row { text-align: center; }
.time-val { display: block; font-size: 20px; font-weight: 800; color: #1a3a6b; }
.time-lbl { font-size: 11px; color: #909399; }
.time-row.highlight .time-val { color: #e6a23c; }

.stat-mini-cards {
  display: flex; flex-direction: column; gap: 8px;
}
.mini-card {
  flex: 1; background: #ffffff; border-radius: 10px; padding: 12px 16px;
  display: flex; align-items: center; gap: 10px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
.mini-num { font-size: 22px; font-weight: 800; color: #1a3a6b; }
.mini-lbl { font-size: 13px; color: #909399; }
.total-card .mini-num { color: #1a3a6b; }
.unhandled-card .mini-num { color: #f56c6c; }
.handled-card .mini-num { color: #67c23a; }

/* 通用卡片 */
.card {
  background: #ffffff; border-radius: 12px; padding: 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
.card-title {
  font-size: 14px; font-weight: 600; color: #303133;
  margin-bottom: 16px; display: flex; align-items: center; gap: 8px;
}
.flex-1 { flex: 1; min-width: 0; }

.mid-row {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 16px;
}

/* 等级分布 */
.level-bars { display: flex; flex-direction: column; gap: 14px; }
.level-item {}
.level-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.level-name { font-size: 13px; color: #606266; font-weight: 500; }
.level-count { font-size: 14px; font-weight: 700; color: #303133; }
.level-tip { font-size: 11px; color: #c0c4cc; margin-top: 12px; }

/* 趋势图 */
.trend-chart { padding-top: 4px; }
.trend-bars { display: flex; align-items: flex-end; gap: 8px; height: 120px; }
.trend-col { flex: 1; display: flex; flex-direction: column; align-items: center; height: 100%; }
.bar-wrap { flex: 1; display: flex; align-items: flex-end; width: 100%; }
.trend-bar {
  width: 100%; background: linear-gradient(180deg, #409eff, #66b1ff);
  border-radius: 4px 4px 0 0; display: flex; justify-content: center;
  position: relative; min-height: 4px; transition: height 0.3s;
}
.bar-val {
  position: absolute; top: -18px; font-size: 11px; font-weight: 600; color: #409eff;
}
.trend-label { font-size: 11px; color: #909399; margin-top: 4px; }

/* TOP批次 */
.top-list { display: flex; flex-direction: column; gap: 8px; }
.top-item {
  display: flex; align-items: center; gap: 10px; cursor: pointer;
  background: #f8fafc; border-radius: 8px; padding: 8px 12px;
  transition: background 0.2s;
}
.top-item:hover { background: #ecf5ff; }
.top-rank {
  width: 20px; height: 20px; border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 700; flex-shrink: 0;
}
.rank-1 { background: #fef0f0; color: #f56c6c; }
.rank-2 { background: #fdf6ec; color: #e6a23c; }
.rank-3 { background: #f0f9eb; color: #67c23a; }
.rank-4, .rank-5 { background: #f0f4f8; color: #909399; }
.top-info { flex: 1; min-width: 0; }
.top-batch-no { font-size: 13px; font-weight: 600; color: #303133; }
.top-meta { font-size: 11px; color: #909399; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.top-right { display: flex; align-items: center; gap: 6px; flex-shrink: 0; }
.top-count { font-size: 12px; font-weight: 600; color: #f56c6c; }

/* 预警类型 */
.type-list { display: flex; flex-direction: column; gap: 12px; }
.type-row { display: grid; grid-template-columns: 100px 60px 1fr; align-items: center; gap: 10px; }
.type-count { font-size: 14px; font-weight: 700; color: #303133; }

/* 未处理预警 */
.unhandled-list { display: flex; flex-direction: column; gap: 8px; }
.unhandled-item {
  display: flex; align-items: center; justify-content: space-between;
  background: #fef0f0; border-radius: 8px; padding: 10px 14px;
  cursor: pointer; transition: background 0.2s;
}
.unhandled-item:hover { background: #fde2e2; }
.uh-left { display: flex; align-items: center; gap: 10px; }
.uh-batch { font-size: 13px; font-weight: 600; color: #303133; }
.uh-meta { font-size: 12px; color: #909399; }
.uh-right { display: flex; align-items: center; gap: 12px; flex-shrink: 0; }
.uh-time { font-size: 11px; color: #c0c4cc; }
.uh-score { font-size: 13px; font-weight: 700; color: #f56c6c; }
.view-more {
  text-align: center; font-size: 13px; color: #409eff; cursor: pointer;
  padding: 8px; border-radius: 8px; margin-top: 4px;
}
.view-more:hover { background: #ecf5ff; }

.empty-mini { text-align: center; color: #c0c4cc; font-size: 13px; padding: 24px 0; }
.loading-area { background: #ffffff; border-radius: 12px; padding: 24px; }
</style>
