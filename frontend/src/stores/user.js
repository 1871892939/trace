import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    username: localStorage.getItem('username') || '',
    role: localStorage.getItem('role') || ''
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.role === 'admin'
  },

  actions: {
    async login(loginForm) {
      const res = await loginApi(loginForm)
      if (res.code === 200) {
        const { token, role, username } = res.data
        this.token = token
        this.username = username
        this.role = role
        localStorage.setItem('token', token)
        localStorage.setItem('username', username)
        localStorage.setItem('role', role)
        return true
      } else {
        throw new Error(res.message || '登录失败')
      }
    },

    async logout() {
      try {
        await logoutApi()
      } catch (e) {
        // ignore
      }
      this.token = ''
      this.username = ''
      this.role = ''
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
    }
  }
})
