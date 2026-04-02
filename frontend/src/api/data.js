import request from './request'

export function generateData(type, count, clean = true) {
  return request.post(`/simulation/generate?type=${type}&count=${count}&clean=${clean}`)
}

export function getOverview() {
  return request.get('/overview/dashboard')
}
