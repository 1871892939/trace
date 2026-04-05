<template>
  <div class="batch-entry-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">批次录入</h2>
        <p class="page-subtitle">完整录入批次信息，包括基础信息、检测数据与物流轨迹</p>
      </div>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="130px"
      class="batch-form"
    >
      <!-- 一、基础信息 -->
      <div class="section-card">
        <div class="section-title">
          <span class="section-dot green" />
          基础信息
        </div>
        <div class="section-body">
          <el-row :gutter="24">
            <el-col :span="8">
              <el-form-item label="批次编号" prop="batchNo">
                <el-input
                  v-model="form.batchNo"
                  placeholder="如 BATCH20260404001"
                  clearable
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="产地编码" prop="origin">
                <el-input
                  v-model="form.origin"
                  placeholder="如 440000"
                  clearable
                />
                <div class="field-tip">省份代码示例：北京 110000 / 广东 440000 / 浙江 330000</div>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="所属企业" prop="enterprise">
                <el-input
                  v-model="form.enterprise"
                  placeholder="请输入企业名称"
                  clearable
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="8">
              <el-form-item label="生产日期" prop="productionDate">
                <el-date-picker
                  v-model="form.productionDate"
                  type="date"
                  placeholder="选择生产日期"
                  style="width: 100%"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </div>

      <!-- 二、检测数据 -->
      <div class="section-card">
        <div class="section-title">
          <span class="section-dot blue" />
          检测数据
          <span class="section-optional"></span>
        </div>
        <div class="section-body">
          <el-row :gutter="24">
            <el-col :span="8">
              <el-form-item label="农残 (mg/kg)">
                <el-input-number
                  v-model="form.detection.pesticide"
                  :precision="4"
                  :step="0.01"
                  :min="0"
                  style="width: 100%"
                  placeholder="请输入农残值"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="重金属 (mg/kg)">
                <el-input-number
                  v-model="form.detection.heavyMetal"
                  :precision="4"
                  :step="0.01"
                  :min="0"
                  style="width: 100%"
                  placeholder="请输入重金属值"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="微生物 (CFU/g)">
                <el-input-number
                  v-model="form.detection.microbe"
                  :precision="2"
                  :step="1"
                  :min="0"
                  style="width: 100%"
                  placeholder="请输入微生物值"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="8">
              <el-form-item label="检测时间">
                <el-date-picker
                  v-model="form.detection.testTime"
                  type="datetime"
                  placeholder="选择检测时间"
                  style="width: 100%"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </div>

      <!-- 三、物流轨迹 -->
      <div class="section-card">
        <div class="section-title">
          <span class="section-dot orange" />
          物流轨迹
          <span class="section-optional"></span>
          <el-button type="primary" link size="small" :icon="Plus" @click="addLogisticsRow">
            添加一条
          </el-button>
        </div>
        <div class="section-body">
          <el-empty v-if="form.logistics.length === 0" description="暂无物流记录，点击上方「添加一条」录入" :image-size="60" />

          <div v-for="(row, idx) in form.logistics" :key="idx" class="logistics-row-card">
            <div class="logistics-row-header">
              <span class="row-index">{{ idx + 1 }}</span>
              <span class="row-label">物流节点 {{ idx + 1 }}</span>
              <el-button type="danger" link size="small" @click="removeLogisticsRow(idx)">
                删除
              </el-button>
            </div>

            <el-row :gutter="20">
              <el-col :span="6">
                <el-form-item label="经度" :prop="`logistics.${idx}.gpsLng`" :rules="logisticsRules.gpsLng">
                  <el-input-number
                    v-model="row.gpsLng"
                    :precision="6"
                    :step="0.000001"
                    :min="-180"
                    :max="180"
                    style="width: 100%"
                    placeholder="如 113.264385"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="纬度" :prop="`logistics.${idx}.gpsLat`" :rules="logisticsRules.gpsLat">
                  <el-input-number
                    v-model="row.gpsLat"
                    :precision="6"
                    :step="0.000001"
                    :min="-90"
                    :max="90"
                    style="width: 100%"
                    placeholder="如 23.129163"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="温度 (℃)" :prop="`logistics.${idx}.temperature`" :rules="logisticsRules.temperature">
                  <el-input-number
                    v-model="row.temperature"
                    :precision="2"
                    :step="0.1"
                    style="width: 100%"
                    placeholder="如 4.5"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="湿度 (%)" :prop="`logistics.${idx}.humidity`" :rules="logisticsRules.humidity">
                  <el-input-number
                    v-model="row.humidity"
                    :precision="2"
                    :step="0.1"
                    :min="0"
                    :max="100"
                    style="width: 100%"
                    placeholder="如 65.0"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row>
              <el-col :span="8">
                <el-form-item label="记录时间" :prop="`logistics.${idx}.recordTime`" :rules="logisticsRules.recordTime">
                  <el-date-picker
                    v-model="row.recordTime"
                    type="datetime"
                    placeholder="选择记录时间"
                    style="width: 100%"
                    format="YYYY-MM-DD HH:mm:ss"
                    value-format="YYYY-MM-DD HH:mm:ss"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </div>
      </div>

      <!-- 提交按钮 -->
      <div class="submit-bar">
        <el-button @click="handleReset">重置表单</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确认录入
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { createBatch, checkBatchNoExists } from '@/api/data'
import { useRouter } from 'vue-router'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)

function newLogisticsRow() {
  return { gpsLng: null, gpsLat: null, temperature: null, humidity: null, recordTime: '' }
}

const form = reactive({
  batchNo: '',
  origin: '',
  enterprise: '',
  productionDate: '',
  detection: { pesticide: null, heavyMetal: null, microbe: null, testTime: '' },
  logistics: []
})

const validateBatchNo = async (rule, value, callback) => {
  if (!value || !value.trim()) {
    callback(new Error('请输入批次编号'))
    return
  }
  if (value.length > 50) {
    callback(new Error('批次编号不超过 50 个字符'))
    return
  }
  try {
    const res = await checkBatchNoExists(value.trim())
    if (res.code === 200 && res.data?.exists) {
      callback(new Error('该批次编号已存在'))
    } else {
      callback()
    }
  } catch {
    callback()
  }
}

const rules = {
  batchNo: [
    { required: true, message: '请输入批次编号', trigger: 'blur' },
    { validator: validateBatchNo, trigger: 'blur' }
  ],
  origin: [{ required: true, message: '请输入产地编码', trigger: 'blur' }],
  enterprise: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  productionDate: [{ required: true, message: '请选择生产日期', trigger: 'change' }]
}

const logisticsRules = {
  gpsLng: [
    { required: true, message: '请输入经度', trigger: 'blur' },
    { type: 'number', min: -180, max: 180, message: '经度范围 -180 ~ 180', trigger: 'blur' }
  ],
  gpsLat: [
    { required: true, message: '请输入纬度', trigger: 'blur' },
    { type: 'number', min: -90, max: 90, message: '纬度范围 -90 ~ 90', trigger: 'blur' }
  ],
  temperature: [{ required: true, message: '请输入温度', trigger: 'blur' }],
  humidity: [{ required: true, message: '请输入湿度', trigger: 'blur' }],
  recordTime: [{ required: true, message: '请选择记录时间', trigger: 'change' }]
}

function addLogisticsRow() {
  form.logistics.push(newLogisticsRow())
}

function removeLogisticsRow(idx) {
  form.logistics.splice(idx, 1)
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = {
      batchNo: form.batchNo.trim(),
      origin: form.origin.trim(),
      enterprise: form.enterprise.trim(),
      productionDate: form.productionDate,
      detection: {
        pesticide: form.detection.pesticide != null ? String(form.detection.pesticide) : '',
        heavyMetal: form.detection.heavyMetal != null ? String(form.detection.heavyMetal) : '',
        microbe: form.detection.microbe != null ? String(form.detection.microbe) : '',
        testTime: form.detection.testTime || ''
      },
      logistics: form.logistics.map(row => ({
        gpsLng: row.gpsLng != null ? String(row.gpsLng) : '',
        gpsLat: row.gpsLat != null ? String(row.gpsLat) : '',
        temperature: row.temperature != null ? String(row.temperature) : '',
        humidity: row.humidity != null ? String(row.humidity) : '',
        recordTime: row.recordTime || ''
      }))
    }
    const res = await createBatch(payload)
    if (res.code === 200) {
      ElMessage.success('录入成功')
      handleReset()
      router.push('/main/batch/query')
    } else {
      ElMessage.error(res.message || '录入失败')
    }
  } catch {
    ElMessage.error('录入失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

function handleReset() {
  formRef.value?.resetFields()
  form.detection = { pesticide: null, heavyMetal: null, microbe: null, testTime: '' }
  form.logistics = []
}
</script>

<style scoped>
.batch-entry-page {
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

.batch-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.section-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 20px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.section-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.section-dot.green  { background: #67c23a; }
.section-dot.blue   { background: #409eff; }
.section-dot.orange { background: #e6a23c; }

.section-optional {
  font-size: 12px;
  font-weight: 400;
  color: #909399;
  margin-left: 2px;
}

.section-body {
  padding: 20px;
}

.field-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}

.logistics-row-card {
  background: #f8fafc;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 14px 16px;
  margin-bottom: 14px;
}

.logistics-row-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.row-index {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #409eff;
  color: #ffffff;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.row-label {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  flex: 1;
}

.submit-bar {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
