<template>
  <div class="batch-entry-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">批次录入</h2>
        <p class="page-subtitle">手动录入溯源批次基本信息，数据模拟仍用于生成检测与物流数据</p>
      </div>
    </div>

    <div class="form-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="batch-form"
      >
        <el-form-item label="批次编号" prop="batchNo">
          <el-input
            v-model="form.batchNo"
            placeholder="请输入批次编号，如 BATCH20260404001"
            clearable
            style="width: 320px"
          />
        </el-form-item>

        <el-form-item label="产地编码" prop="origin">
          <el-input
            v-model="form.origin"
            placeholder="请输入产地编码（省份代码），如 440000"
            clearable
            style="width: 320px"
          />
          <div class="form-tip">省份代码示例：北京 110000 / 广东 440000 / 浙江 330000</div>
        </el-form-item>

        <el-form-item label="所属企业" prop="enterprise">
          <el-input
            v-model="form.enterprise"
            placeholder="请输入企业名称"
            clearable
            style="width: 320px"
          />
        </el-form-item>

        <el-form-item label="生产日期" prop="productionDate">
          <el-date-picker
            v-model="form.productionDate"
            type="date"
            placeholder="选择生产日期"
            style="width: 200px"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            确认录入
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="tips-card">
      <div class="tips-title">
        <el-icon><InfoFilled /></el-icon>
        录入说明
      </div>
      <ul class="tips-list">
        <li>批次编号请保持唯一，重复录入会报错；可使用日期格式如 <code>BATCH20260404001</code></li>
        <li>产地编码为数字，可参考国家标准 GB/T 2260（6 位省份代码）</li>
        <li>基础批次录入后，请前往「数据模拟」生成检测与物流数据，再在「预警中心」查看分析结果</li>
        <li>批次录入后如需修改，可在「批次查询」页面编辑；删除批次将同时清除该批次的已处理预警和风险评估记录</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { createBatch, checkBatchNoExists } from '@/api/data'
import { useRouter } from 'vue-router'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  batchNo: '',
  origin: '',
  enterprise: '',
  productionDate: ''
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
  origin: [
    { required: true, message: '请输入产地编码', trigger: 'blur' }
  ],
  enterprise: [
    { required: true, message: '请输入企业名称', trigger: 'blur' }
  ],
  productionDate: [
    { required: true, message: '请选择生产日期', trigger: 'change' }
  ]
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const res = await createBatch({
      batchNo: form.batchNo.trim(),
      origin: form.origin.trim(),
      enterprise: form.enterprise.trim(),
      productionDate: form.productionDate
    })
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

.form-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 32px 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.batch-form {
  max-width: 600px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.tips-card {
  background: #f0f7ff;
  border: 1px solid #d9ecff;
  border-radius: 12px;
  padding: 16px 20px;
}

.tips-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #1a3a6b;
  margin-bottom: 10px;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.tips-list code {
  font-family: 'Courier New', monospace;
  background: #e8f0fe;
  padding: 1px 5px;
  border-radius: 3px;
  font-size: 12px;
  color: #1a3a6b;
}
</style>
