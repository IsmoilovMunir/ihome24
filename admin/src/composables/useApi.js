import { createFetch } from '@vueuse/core'
import { destr } from 'destr'

const resolveAdminApiBaseUrl = () => {
  const configured = (import.meta.env.VITE_API_BASE_URL || '').trim()
  const fallback = '/api'
  const rawBase = configured || fallback

  if (typeof window === 'undefined') return rawBase

  const host = window.location.hostname
  const isCurrentHostLocal = ['localhost', '127.0.0.1', '::1'].includes(host)
  const isConfiguredLocalhost = /^https?:\/\/(localhost|127\.0\.0\.1|\[::1\])(?::\d+)?(\/.*)?$/i.test(rawBase)

  if (!isCurrentHostLocal && isConfiguredLocalhost) {
    return `${window.location.origin}/api`
  }

  if (/^https?:\/\//i.test(rawBase) && !rawBase.endsWith('/api')) {
    return `${rawBase.replace(/\/+$/, '')}/api`
  }

  return rawBase
}

export const useApi = createFetch({
  baseUrl: resolveAdminApiBaseUrl(),
  fetchOptions: {
    headers: {
      Accept: 'application/json',
    },
  },
  options: {
    refetch: true,
    async beforeFetch({ options }) {
      const accessToken = useCookie('accessToken').value
      if (accessToken) {
        options.headers = {
          ...options.headers,
          Authorization: `Bearer ${accessToken}`,
        }
      }
      
      return { options }
    },
    afterFetch(ctx) {
      const { data, response } = ctx

      // Parse data if it's JSON
      let parsedData = null
      try {
        parsedData = destr(data)
      }
      catch (error) {
        console.error(error)
      }
      
      return { data: parsedData, response }
    },
  },
})
