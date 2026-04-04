import request from './request'

export function getOperationLogList({ keyword, operationType, module, status } = {}) {
  const params = new URLSearchParams()
  if (keyword) params.append('keyword', keyword)
  if (operationType) params.append('operationType', operationType)
  if (module) params.append('module', module)
  if (status) params.append('status', status)
  return request.get(`/operation-log/list?${params.toString()}`)
}
