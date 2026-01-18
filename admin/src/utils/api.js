import { ofetch } from 'ofetch'

export const $api = ofetch.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: {
    'Content-Type': 'application/json',
  },
  async onRequest({ options }) {
    const accessToken = useCookie('accessToken').value
    if (accessToken) {
      options.headers = options.headers || {}
      if (options.headers instanceof Headers) {
        options.headers.set('Authorization', `Bearer ${accessToken}`)
      } else {
        options.headers['Authorization'] = `Bearer ${accessToken}`
      }
    }
    // Убеждаемся, что Content-Type установлен правильно для всех POST/PUT запросов с body
    if (options.method && ['POST', 'PUT', 'PATCH'].includes(options.method.toUpperCase())) {
      if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
        if (options.headers instanceof Headers) {
          options.headers.set('Content-Type', 'application/json')
        } else {
          options.headers = options.headers || {}
          options.headers['Content-Type'] = 'application/json'
        }
      }
    }
  },
})
