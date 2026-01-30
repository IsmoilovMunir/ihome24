import { defineStore } from 'pinia'
import { authApi } from '../services/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: JSON.parse(localStorage.getItem('userData') || 'null'),
    accessToken: localStorage.getItem('accessToken') || null,
    loading: false,
    error: null,
  }),

  getters: {
    isAuthenticated: (state) => !!state.accessToken && !!state.user,
  },

  actions: {
    async login(credentials) {
      this.loading = true
      this.error = null
      try {
        const response = await authApi.login(credentials)
        const { accessToken, userData } = response.data
        
        this.accessToken = accessToken
        this.user = userData
        
        localStorage.setItem('accessToken', accessToken)
        localStorage.setItem('userData', JSON.stringify(userData))
        
        return { success: true }
      } catch (error) {
        this.error = error.response?.data?.errors || { message: error.message }
        return { success: false, error: this.error }
      } finally {
        this.loading = false
      }
    },

    async register(userData) {
      this.loading = true
      this.error = null
      try {
        await authApi.register(userData)
        return { success: true }
      } catch (error) {
        this.error = error.response?.data?.errors || { message: error.message }
        return { success: false, error: this.error }
      } finally {
        this.loading = false
      }
    },

    async logout() {
      try {
        await authApi.logout()
      } catch (error) {
        console.error('Logout error:', error)
      } finally {
        this.accessToken = null
        this.user = null
        localStorage.removeItem('accessToken')
        localStorage.removeItem('userData')
      }
    },

    async fetchUser() {
      try {
        const response = await authApi.getMe()
        this.user = response.data
        localStorage.setItem('userData', JSON.stringify(response.data))
      } catch (error) {
        console.error('Error fetching user:', error)
        this.logout()
      }
    },
  },
})
