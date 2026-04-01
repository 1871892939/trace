import request from './request'
import type { DataEntryRequest } from '@/types/data'

/**
 * 录入数据
 */
export function entryData(data: DataEntryRequest) {
  return request({
    url: '/api/data/entry',
    method: 'post',
    data
  })
}

/**
 * 获取批次列表
 */
export function getBatches(params?: { page?: number; size?: number }) {
  return request({
    url: '/api/data/batches',
    method: 'get',
    params
  })
}

/**
 * 获取检测数据统计
 */
export function getDetectionStats() {
  return request({
    url: '/api/data/detection/stats',
    method: 'get'
  })
}

/**
 * 获取物流数据统计
 */
export function getLogisticsStats() {
  return request({
    url: '/api/data/logistics/stats',
    method: 'get'
  })
}
