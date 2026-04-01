<template>
  <div class="simulation-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">数据模拟</h2>
        <p class="page-subtitle">生成仿真批次数据，用于系统演示与功能验证</p>
      </div>
    </div>

    <!-- 操作卡片区 -->
    <div class="action-grid">
      <!-- 正常数据 -->
      <div class="action-card normal">
        <div class="card-icon normal-icon">
          <svg viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.5"/>
            <path d="M8 12L11 15L16 9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="card-info">
          <h3>正常数据</h3>
          <p>农残、重金属、微生物及物流指标均在安全阈值内的仿真批次</p>
        </div>
        <div class="card-actions">
          <el-button type="primary" size="large" @click="handleGenerate('normal', 1)" :loading="loading.normal1">
            模拟 1 条
          </el-button>
          <el-button type="primary" plain size="large" @click="handleGenerate('normal', 20)" :loading="loading.normal20">
            模拟 20 条
          </el-button>
        </div>
      </div>

      <!-- 异常数据 -->
      <div class="action-card anomaly">
        <div class="card-icon anomaly-icon">
          <svg viewBox="0 0 24 24" fill="none">
            <path d="M12 9V13M12 17H12.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="card-info">
          <h3>异常数据</h3>
          <p>模拟农残超标、重金属超标、微生物超标或物流温湿度异常等风险场景</p>
        </div>
        <div class="card-actions">
          <el-button type="danger" size="large" @click="handleGenerate('anomaly', 1)" :loading="loading.anomaly1">
            模拟 1 条
          </el-button>
          <el-button type="danger" plain size="large" @click="handleGenerate('anomaly', 20)" :loading="loading.anomaly20">
            模拟 20 条
          </el-button>
        </div>
      </div>
    </div>

    <!-- 结果展示 -->
    <transition name="slide-fade">
      <div v-if="lastResult" class="result-section">
        <div class="result-header">
          <h3>生成结果</h3>
          <el-tag :type="lastResult.alertCount > 0 ? 'danger' : 'success'" size="large">
            {{ lastResult.alertCount > 0 ? '含预警数据' : '纯正常数据' }}
          </el-tag>
        </div>

        <div class="result-stats">
          <div class="stat-card">
            <div class="stat-value">{{ lastResult.generated }}</div>
            <div class="stat-label">生成批次</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ lastResult.alertCount }}</div>
            <div class="stat-label">触发预警</div>
          </div>
          <div class="stat-card">
            <div class="stat-value" :style="{ color: lastResult.riskDistribution?.High > 0 ? '#f56c6c' : '#67c23a' }">
              {{ lastResult.riskDistribution?.High || 0 }}
            </div>
            <div class="stat-label">高风险</div>
          </div>
          <div class="stat-card">
            <div class="stat-value" style="color: #e6a23c">
              {{ lastResult.riskDistribution?.Medium || 0 }}
            </div>
            <div class="stat-label">中风险</div>
          </div>
          <div class="stat-card">
            <div class="stat-value" style="color: #67c23a">
              {{ lastResult.riskDistribution?.Low || 0 }}
            </div>
            <div class="stat-label">低风险</div>
          </div>
        </div>

        <!-- 批次编号列表 -->
        <div class="batch-list" v-if="lastResult.batchNos && lastResult.batchNos.length">
          <div class="batch-list-header">
            <span>批次编号</span>
          </div>
          <div class="batch-tags">
            <el-tag
              v-for="no in lastResult.batchNos"
              :key="no"
              type="info"
              effect="plain"
              size="small"
              class="batch-tag"
            >{{ no }}</el-tag>
          </div>
        </div>
      </div>
    </transition>

    <!-- 历史模拟记录（简化显示） -->
    <div class="history-section" v-if="history.length">
      <h3 class="section-title">本次会话历史</h3>
      <el-table :data="history" stripe style="width: 100%" size="small">
        <el-table-column prop="time" label="时间" width="160" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'normal' ? 'success' : 'danger'" size="small">
              {{ row.type === 'normal' ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="count" label="数量" width="80" />
        <el-table-column prop="message" label="结果" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { generateData } from '@/api/data'

const lastResult = ref(null)
const history = ref([])
const loading = reactive({
  normal1: false,
  normal20: false,
  anomaly1: false,
  anomaly20: false
})

async function handleGenerate(type, count) {
  const key = type === 'normal' ? (count === 1 ? 'normal1' : 'normal20') : (count === 1 ? 'anomaly1' : 'anomaly20')
  loading[key] = true

  try {
    const res = await generateData(type, count)
    if (res.code === 200) {
      lastResult.value = res.data
      history.value.unshift({
        time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
        type,
        count,
        message: res.data.message
      })
      if (history.value.length > 10) history.value.pop()
      ElMessage.success(res.message)
    } else {
      ElMessage.error(res.message || '生成失败')
    }
  } catch (e) {
    ElMessage.error('请求失败：' + (e.message || '未知错误'))
  } finally {
    loading[key] = false
  }
}
</script>

<style scoped>
.simulation-page {
  max-width: 1100px;
}

.page-header {
  margin-bottom: 32px;
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

.action-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 32px;
}

.action-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 28px;
  border: 1px solid #ebeef5;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.action-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
}

.action-card.normal::before {
  background: linear-gradient(90deg, #409eff, #67c23a);
}

.action-card.anomaly::before {
  background: linear-gradient(90deg, #f56c6c, #e6a23c);
}

.action-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.card-icon svg {
  width: 28px;
  height: 28px;
}

.normal-icon {
  background: linear-gradient(135deg, #e8f4ff, #d9f0d3);
  color: #409eff;
}

.anomaly-icon {
  background: linear-gradient(135deg, #fef0f0, #fdf6ec);
  color: #f56c6c;
}

.card-info h3 {
  margin: 0 0 8px;
  font-size: 17px;
  font-weight: 600;
  color: #303133;
}

.card-info p {
  margin: 0 0 20px;
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
}

.card-actions {
  display: flex;
  gap: 12px;
}

.card-actions .el-button {
  flex: 1;
}

/* 结果区 */
.result-section {
  background: #ffffff;
  border-radius: 16px;
  padding: 28px;
  border: 1px solid #ebeef5;
  margin-bottom: 32px;
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.result-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.result-stats {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #f8f9fb;
  border-radius: 12px;
  padding: 18px 12px;
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
  margin-bottom: 6px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.batch-list {
  border-top: 1px solid #f0f2f5;
  padding-top: 16px;
}

.batch-list-header {
  font-size: 13px;
  color: #909399;
  margin-bottom: 10px;
}

.batch-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.batch-tag {
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

/* 历史区 */
.history-section {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #ebeef5;
}

.section-title {
  margin: 0 0 16px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

/* 动画 */
.slide-fade-enter-active {
  transition: all 0.4s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.3s ease-in;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
