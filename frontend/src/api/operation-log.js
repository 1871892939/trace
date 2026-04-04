import request from './request'

export function getOperationLogList({ keyword, operationType } = {}) {
  const params = new URLSearchParams()
  if (keyword) params.append('keyword', keyword)
  if (operationType) params.append('operationType', operationType)
  return request.get(`/operation-log/list?${params.toString()}`)
}
