<template>
  <div class="overview-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">大盘概览</h2>
        <p class="page-subtitle">多维度聚合数据可视化，实时监控溯源系统运行状态</p>
      </div>
      <el-button type="primary" :icon="Refresh" @click="fetchData" :loading="loading">
        刷新数据
      </el-button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading && !overviewData" class="loading-area">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 无数据提示 -->
    <el-empty v-else-if="!overviewData && !loading" description="暂无数据，请先在数据模拟中生成测试数据" />

    <template v-else-if="overviewData">
      <!-- 顶部统计卡片 -->
      <div class="stat-cards">
        <div class="stat-card">
          <div class="stat-icon batch-icon">
            <svg viewBox="0 0 24 24" fill="none"><rect x="3" y="3" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/></svg>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ overviewData.totalBatches }}</div>
            <div class="stat-label">批次总数</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon alert-icon">
            <svg viewBox="0 0 24 24" fill="none"><path d="M15 17H5a2 2 0 01-2-2V5a2 2 0 012-2h14a2 2 0 012 2v10l-4-4zM12 9v4M12 17h.01" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </div>
          <div class="stat-body">
            <div class="stat-value" style="color: #e6a23c">{{ overviewData.totalAlerts }}</div>
            <div class="stat-label">预警总数</div>
          </div>
        </div>

        <div class="stat-card" :class="{ 'has-alert': overviewData.unhandledAlerts > 0 }">
          <div class="stat-icon unhandled-icon" :class="{ pulse: overviewData.unhandledAlerts > 0 }">
            <svg viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.5"/><path d="M12 7v5l3 3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
          </div>
          <div class="stat-body">
            <div class="stat-value" :style="{ color: overviewData.unhandledAlerts > 0 ? '#f56c6c' : '#67c23a' }">
              {{ overviewData.unhandledAlerts }}
            </div>
            <div class="stat-label">待处理预警</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon temp-icon">
            <svg viewBox="0 0 24 24" fill="none"><path d="M12 9V3M12 9a4 4 0 100 8 4 4 0 000-8zM12 21v-4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ overviewData.avgTemperature }}°C</div>
            <div class="stat-label">平均温度</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon hum-icon">
            <svg viewBox="0 0 24 24" fill="none"><path d="M12 2.69l5.66 5.66a8 8 0 11-11.31 0L12 2.69z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/></svg>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ overviewData.avgHumidity }}%</div>
            <div class="stat-label">平均湿度</div>
          </div>
        </div>
      </div>

      <!-- 图表行 1 -->
      <div class="chart-row">
        <!-- 风险等级分布 -->
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">风险等级分布</span>
          </div>
          <div class="chart-body">
            <v-chart class="echart" :option="riskPieOption" autoresize />
          </div>
        </div>

        <!-- 风险评分分布 -->
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">风险评分分布</span>
          </div>
          <div class="chart-body">
            <v-chart class="echart" :option="riskBarOption" autoresize />
          </div>
        </div>

        <!-- 预警类型分布 -->
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">预警类型分布</span>
          </div>
          <div class="chart-body">
            <v-chart class="echart" :option="alertTypeOption" autoresize />
          </div>
        </div>
      </div>

      <!-- 图表行 2 -->
      <div class="chart-row-2">
        <!-- 批次新增趋势 -->
        <div class="chart-card wide">
          <div class="chart-header">
            <span class="chart-title">近 30 天批次新增趋势</span>
          </div>
          <div class="chart-body">
            <v-chart class="echart" :option="batchTrendOption" autoresize />
          </div>
        </div>
      </div>

      <!-- 图表行 3 -->
      <div class="chart-row-2">
        <!-- 预警趋势 -->
        <div class="chart-card wide">
          <div class="chart-header">
            <span class="chart-title">近 30 天预警趋势</span>
          </div>
          <div class="chart-body">
            <v-chart class="echart" :option="alertTrendOption" autoresize />
          </div>
        </div>
      </div>

      <!-- 批次列表（溯源详情） -->
      <div class="batch-table-section">
        <div class="section-header">
          <span class="chart-title">溯源批次详情</span>
          <span class="batch-count">共 {{ overviewData.batchDetails?.length || 0 }} 条记录</span>
        </div>
        <el-table :data="overviewData.batchDetails" stripe style="width: 100%" size="small">
          <el-table-column prop="batchNo" label="批次编号" min-width="160">
            <template #default="{ row }">
              <span class="batch-no">{{ row.batchNo }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="origin" label="产地" width="100" />
          <el-table-column prop="enterprise" label="企业" min-width="120" show-overflow-tooltip />
          <el-table-column prop="productionDate" label="生产日期" width="120" />
          <el-table-column prop="riskScore" label="风险评分" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                :type="row.riskScore > 70 ? 'danger' : row.riskScore > 40 ? 'warning' : 'success'"
                size="small"
                effect="dark"
              >{{ row.riskScore }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="riskLevel" label="风险等级" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                :type="row.riskLevel === 'High' ? 'danger' : row.riskLevel === 'Medium' ? 'warning' : 'success'"
                size="small"
              >{{ row.riskLevel === 'High' ? '高风险' : row.riskLevel === 'Medium' ? '中风险' : '低风险' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="hasAlert" label="预警状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.hasAlert" type="danger" size="small" effect="dark">
                {{ alertTypeLabel(row.alertType) }}
              </el-tag>
              <el-tag v-else type="info" size="small">正常</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import VChart, { THEME_KEY } from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import { ElMessage } from 'element-plus'
import { getOverview } from '@/api/data'
import { wsService } from '@/api/ws'

use([CanvasRenderer, PieChart, BarChart, LineChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const loading = ref(false)
const overviewData = ref(null)

async function fetchData() {
  loading.value = true
  try {
    const res = await getOverview()
    if (res.code === 200) {
      overviewData.value = res.data
    } else {
      ElMessage.error(res.message || '获取数据失败')
    }
  } catch (e) {
    ElMessage.error('请求失败：' + (e.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
  wsService.connect()
  wsService.on('OVERVIEW_UPDATE', (data) => {
    overviewData.value = data
  })
})

onUnmounted(() => {
  wsService.off('OVERVIEW_UPDATE')
  wsService.disconnect()
})

function alertTypeLabel(type) {
  const map = {
    'TEMP': '温度异常',
    'HUMIDITY': '湿度异常',
    'PESTICIDE': '农残超标',
    'HEAVY_METAL': '重金属超标',
    'MICROBE': '微生物超标',
    'COMPOSITE': '综合风险'
  }
  return map[type] || type || '未知'
}

// ==================== 图表配置 ====================

const riskPieOption = computed(() => {
  const d = overviewData.value?.riskDistribution || {}
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c} 批次 ({d}%)' },
    legend: { bottom: 10, textStyle: { color: '#606266', fontSize: 12 } },
    color: ['#67c23a', '#e6a23c', '#f56c6c'],
    series: [{
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%', fontSize: 12, color: '#606266' },
      emphasis: { label: { fontSize: 14, fontWeight: 'bold' } },
      data: [
        { value: d.Low || 0, name: '低风险' },
        { value: d.Medium || 0, name: '中风险' },
        { value: d.High || 0, name: '高风险' }
      ]
    }]
  }
})

const riskBarOption = computed(() => {
  const hist = overviewData.value?.riskScoreHistogram || [0, 0, 0, 0, 0]
  const labels = ['0-20', '20-40', '40-60', '60-80', '80-100']
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { top: 20, right: 20, bottom: 30, left: 50 },
    xAxis: { type: 'category', data: labels, axisLabel: { color: '#909399', fontSize: 11 }, axisLine: { lineStyle: { color: '#e4e7ed' } } },
    yAxis: { type: 'value', axisLabel: { color: '#909399', fontSize: 11 }, splitLine: { lineStyle: { color: '#f0f2f5' } } },
    series: [{
      type: 'bar',
      data: hist,
      itemStyle: {
        color: (params) => {
          const colors = ['#67c23a', '#8cc63f', '#e6a23c', '#f78913', '#f56c6c']
          return colors[params.dataIndex]
        },
        borderRadius: [4, 4, 0, 0]
      },
      barMaxWidth: 36
    }]
  }
})

const alertTypeOption = computed(() => {
  const d = overviewData.value?.alertTypeDistribution || {}
  const types = ['TEMP', 'HUMIDITY', 'PESTICIDE', 'HEAVY_METAL', 'MICROBE', 'COMPOSITE']
  const labels = ['温度', '湿度', '农残', '重金属', '微生物', '综合']
  const values = types.map(t => d[t] || 0)
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { top: 20, right: 20, bottom: 30, left: 50 },
    xAxis: { type: 'category', data: labels, axisLabel: { color: '#909399', fontSize: 11 }, axisLine: { lineStyle: { color: '#e4e7ed' } } },
    yAxis: { type: 'value', axisLabel: { color: '#909399', fontSize: 11 }, splitLine: { lineStyle: { color: '#f0f2f5' } } },
    series: [{
      type: 'bar',
      data: values,
      itemStyle: {
        color: (params) => {
          const colors = ['#409eff', '#36cfc9', '#f56c6c', '#e6a23c', '#909399', '#7c3aed']
          return colors[params.dataIndex]
        },
        borderRadius: [4, 4, 0, 0]
      },
      barMaxWidth: 40
    }]
  }
})

const batchTrendOption = computed(() => {
  const labels = overviewData.value?.trendLabels || []
  const data = overviewData.value?.batchTrend || []
  return {
    tooltip: { trigger: 'axis' },
    grid: { top: 20, right: 30, bottom: 30, left: 50 },
    xAxis: { type: 'category', data: labels, axisLabel: { color: '#909399', fontSize: 10, rotate: 30 }, axisLine: { lineStyle: { color: '#e4e7ed' } }, splitLine: { show: false } },
    yAxis: { type: 'value', axisLabel: { color: '#909399', fontSize: 11 }, splitLine: { lineStyle: { color: '#f0f2f5' } } },
    series: [{
      type: 'line',
      data,
      smooth: true,
      symbol: 'circle',
      symbolSize: 5,
      lineStyle: { color: '#409eff', width: 2.5 },
      itemStyle: { color: '#409eff' },
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(64, 158, 255, 0.25)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.02)' }
          ]
        }
      }
    }]
  }
})

const alertTrendOption = computed(() => {
  const labels = overviewData.value?.trendLabels || []
  const data = overviewData.value?.alertTrend || []
  return {
    tooltip: { trigger: 'axis' },
    grid: { top: 20, right: 30, bottom: 30, left: 50 },
    xAxis: { type: 'category', data: labels, axisLabel: { color: '#909399', fontSize: 10, rotate: 30 }, axisLine: { lineStyle: { color: '#e4e7ed' } }, splitLine: { show: false } },
    yAxis: { type: 'value', axisLabel: { color: '#909399', fontSize: 11 }, splitLine: { lineStyle: { color: '#f0f2f5' } } },
    series: [{
      type: 'line',
      data,
      smooth: true,
      symbol: 'circle',
      symbolSize: 5,
      lineStyle: { color: '#f56c6c', width: 2.5 },
      itemStyle: { color: '#f56c6c' },
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(245, 108, 108, 0.25)' },
            { offset: 1, color: 'rgba(245, 108, 108, 0.02)' }
          ]
        }
      }
    }]
  }
})
</script>

<style scoped>
.overview-page {
  max-width: 1400px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}

.page-title {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 600;
  color: #1a3a6b;
}

.page-subtitle {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.loading-area {
  background: #ffffff;
  border-radius: 16px;
  padding: 28px;
}

/* 统计卡片 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #ffffff;
  border-radius: 14px;
  padding: 20px;
  border: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  gap: 14px;
  transition: all 0.2s ease;
}

.stat-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transform: translateY(-1px);
}

.stat-card.has-alert {
  border-color: rgba(245, 108, 108, 0.3);
  background: linear-gradient(135deg, #fff, #fef9f9);
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon svg {
  width: 22px;
  height: 22px;
}

.batch-icon { background: linear-gradient(135deg, #e8f4ff, #d9ecff); color: #409eff; }
.alert-icon { background: linear-gradient(135deg, #fef6ec, #fdf2e6); color: #e6a23c; }
.unhandled-icon { background: linear-gradient(135deg, #fef0f0, #fde8e8); color: #f56c6c; }
.temp-icon { background: linear-gradient(135deg, #f0f5ff, #e6ecff); color: #7c3aed; }
.hum-icon { background: linear-gradient(135deg, #f0fff4, #e6fffa); color: #36cfc9; }

.stat-body {
  min-width: 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

/* 图表通用 */
.chart-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.chart-row-2 {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.chart-card {
  background: #ffffff;
  border-radius: 14px;
  border: 1px solid #ebeef5;
  overflow: hidden;
  transition: all 0.2s ease;
}

.chart-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.chart-card.wide {
  /* full width handled by grid */
}

.chart-header {
  padding: 18px 20px 12px;
  border-bottom: 1px solid #f0f2f5;
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.chart-body {
  padding: 12px 12px 16px;
}

.echart {
  width: 100%;
  height: 220px;
}

/* 批次表格 */
.batch-table-section {
  background: #ffffff;
  border-radius: 14px;
  border: 1px solid #ebeef5;
  overflow: hidden;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px 14px;
  border-bottom: 1px solid #f0f2f5;
}

.batch-count {
  font-size: 12px;
  color: #909399;
}

.batch-no {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #409eff;
  font-weight: 500;
}

/* 动画 */
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.pulse {
  animation: pulse 2s ease-in-out infinite;
}
</style>
