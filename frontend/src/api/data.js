import request from './request'

export function generateData(type, count, clean = true) {
  return request.post(`/simulation/generate?type=${type}&count=${count}&clean=${clean}`)
}

export function getOverview() {
  return request.get('/overview/dashboard')
}

export function queryBatches({ keyword, riskLevel, alertType } = {}) {
  const params = new URLSearchParams()
  if (keyword) params.append('keyword', keyword)
  if (riskLevel) params.append('riskLevel', riskLevel)
  if (alertType) params.append('alertType', alertType)
  return request.get(`/trace/batch/query?${params.toString()}`)
}

export function createBatch(data) {
  return request.post('/batch/create', data)
}

export function updateBatch(data) {
  return request.put('/batch/update', data)
}

export function deleteBatch(batchId) {
  return request.delete(`/batch/${batchId}`)
}

export function checkBatchNoExists(batchNo, excludeId = null) {
  const params = new URLSearchParams({ batchNo })
  if (excludeId != null) params.append('excludeId', excludeId)
  return request.get(`/batch/check-batch-no?${params.toString()}`)
}

export function getTraceChain(batchId) {
  return request.get(`/trace/chain/${batchId}`)
}

export function queryAlerts({ keyword, alertType, handled } = {}) {
  const params = new URLSearchParams()
  if (keyword) params.append('keyword', keyword)
  if (alertType) params.append('alertType', alertType)
  if (handled !== undefined && handled !== null) params.append('handled', handled)
  return request.get(`/alert/list?${params.toString()}`)
}

export function handleAlert(alertId) {
  return request.post(`/alert/handle/${alertId}`)
}

export function getAlertDashboard() {
  return request.get('/alert/dashboard')
}
