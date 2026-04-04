import request from './request'

export function getConfigList(group) {
  const params = group ? `?group=${group}` : ''
  return request.get(`/config/list${params}`)
}

export function getConfigGroups() {
  return request.get('/config/groups')
}

export function updateConfig(id, paramValue) {
  return request.post('/config/update', { id, paramValue })
}
