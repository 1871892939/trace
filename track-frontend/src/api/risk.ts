import request from './request'

/**
 * 风险评估请求参数
 */
export interface RiskEvaluateData {
  batchId: number
  pesticide: number      // 农残值
  heavyMetal: number     // 重金属值
  microbe: number        // 微生物值
  temperature: number    // 温度
  humidity: number       // 湿度
  gpsLng: number         // GPS 经度
  gpsLat: number         // GPS 纬度
}

/**
 * 实时风险评估
 */
export function evaluateRisk(data: RiskEvaluateData) {
  return request({
    url: '/api/risk/evaluate',
    method: 'post',
    data
  })
}

/**
 * 获取最新预警列表
 */
export function getLatestAlerts(params?: { page?: number; size?: number }) {
  return request({
    url: '/api/risk/alert/latest',
    method: 'get',
    params
  })
}

/**
 * 手动更新统计数据
 */
export function updateStatistics() {
  return request({
    url: '/api/risk/stats/update',
    method: 'post'
  })
}
