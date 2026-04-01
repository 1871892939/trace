import request from './request'

export function generateData(type, count) {
  return request.post(`/simulation/generate?type=${type}&count=${count}`)
}

export function getOverview() {
  return request.get('/overview/dashboard')
}
