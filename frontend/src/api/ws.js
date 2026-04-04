/**
 * WebSocket 服务 — 建立到后端 /ws/data-change 的长连接，
 * 接收 OVERVIEW_UPDATE / ALERT_NEW / BATCH_NEW 等推送消息，
 * 并将事件派发给各订阅者。
 *
 * 使用方式：
 *   import { wsService } from './ws'
 *   wsService.on('OVERVIEW_UPDATE', (data) => { ... })
 *   wsService.on('ALERT_NEW', (data) => { ... })
 */

const WS_URL = `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.host}/ws/data-change`

const reconnectDelay = [1000, 2000, 5000, 10000, 30000]
let reconnectIndex = 0
let reconnectTimer = null
let manualClose = false

const listeners = {}

function on(type, handler) {
  if (!listeners[type]) listeners[type] = []
  listeners[type].push(handler)
}

function off(type, handler) {
  if (!listeners[type]) return
  listeners[type] = listeners[type].filter(h => h !== handler)
}

function emit(type, data) {
  if (!listeners[type]) return
  listeners[type].forEach(h => {
    try { h(data) } catch (e) { console.error('[WS] handler error:', e) }
  })
}

let ws = null

function connect() {
  if (ws && (ws.readyState === WebSocket.CONNECTING || ws.readyState === WebSocket.OPEN)) {
    return
  }

  ws = new WebSocket(WS_URL)

  ws.onopen = () => {
    console.info('[WS] 连接已建立')
    reconnectIndex = 0
    manualClose = false
  }

  ws.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data)
      const type = msg.type
      const data = msg.data
      if (type && data !== undefined) {
        emit(type, data)
      }
    } catch (e) {
      console.warn('[WS] 消息解析失败：', event.data, e)
    }
  }

  ws.onerror = (error) => {
    console.warn('[WS] 连接异常：', error)
  }

  ws.onclose = (event) => {
    console.info('[WS] 连接关闭，code:', event.code)
    ws = null
    if (!manualClose) {
      scheduleReconnect()
    }
  }
}

function disconnect() {
  manualClose = true
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  if (ws) {
    ws.close()
    ws = null
  }
}

function scheduleReconnect() {
  if (reconnectIndex >= reconnectDelay.length) reconnectIndex = reconnectDelay.length - 1
  const delay = reconnectDelay[reconnectIndex]
  reconnectIndex++
  console.info(`[WS] ${delay / 1000}s 后尝试重连...`)
  reconnectTimer = setTimeout(() => {
    if (!manualClose) connect()
  }, delay)
}

function getStatus() {
  if (!ws) return 'DISCONNECTED'
  switch (ws.readyState) {
    case WebSocket.CONNECTING: return 'CONNECTING'
    case WebSocket.OPEN: return 'OPEN'
    case WebSocket.CLOSING: return 'CLOSING'
    case WebSocket.CLOSED: return 'CLOSED'
    default: return 'UNKNOWN'
  }
}

export const wsService = { on, off, connect, disconnect, getStatus }
