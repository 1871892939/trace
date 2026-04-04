import request from './request'

export function getUserList({ keyword, role, status } = {}) {
  const params = new URLSearchParams()
  if (keyword) params.append('keyword', keyword)
  if (role) params.append('role', role)
  if (status !== undefined && status !== null) params.append('status', status)
  return request.get(`/user/list?${params.toString()}`)
}

export function createUser(data) {
  return request.post('/user/create', data)
}

export function updateUser(data) {
  return request.post('/user/update', data)
}

export function deleteUser(id) {
  return request.delete(`/user/${id}`)
}
