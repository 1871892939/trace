import request from './request'

export interface LoginData {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  role: string
  username: string
}

/**
 * 用户登录
 */
export function login(data: LoginData): Promise<any> {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data
  })
}

/**
 * 用户登出
 */
export function logout() {
  return request({
    url: '/api/auth/logout',
    method: 'post'
  })
}

/**
 * 刷新 Token
 */
export function refreshToken(token: string) {
  return request({
    url: '/api/auth/refresh',
    method: 'post',
    params: { token }
  })
}
