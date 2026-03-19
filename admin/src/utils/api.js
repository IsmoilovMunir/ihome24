import { ofetch } from 'ofetch'

export const $api = ofetch.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  // Prevent UI "hangs" when backend/proxy doesn't respond.
  // Per-request timeout can still override this default.
  timeout: 15_000,
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
  async onResponseError({ response }) {
    if (response?.status === 401 && typeof window !== 'undefined') {
      useCookie('userData').value = null
      useCookie('accessToken').value = null
      window.localStorage.removeItem('adminLastActivity')

      const currentPath = window.location.pathname + window.location.search
      const loginUrl = `/login?reason=unauthorized&to=${encodeURIComponent(currentPath)}`

      if (!window.location.pathname.startsWith('/login')) {
        window.location.replace(loginUrl)
      }
    }
  },
})
