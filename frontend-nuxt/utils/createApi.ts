import axios, { type AxiosInstance } from 'axios'

export function createApiClient(baseURL: string): AxiosInstance {
  const api = axios.create({
    baseURL,
    timeout: 60_000,
    headers: { 'Content-Type': 'application/json' },
  })

  if (import.meta.client) {
    api.interceptors.request.use((config) => {
      const token = localStorage.getItem('accessToken')
      if (token) config.headers.Authorization = `Bearer ${token}`
      if (config.data instanceof FormData) delete config.headers['Content-Type']
      return config
    })
    api.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          const path = window.location.pathname || ''
          const url = String(error.config?.url || '')
          const isStorePublicApi = /\/api\/(cart|checkout|products|categories|settings|geo)(\/|$)/.test(url)
          const isStorePage = /^\/(cart|checkout|products|category|search)(\/|$)/.test(path)
          if (!isStorePublicApi && !isStorePage) {
            localStorage.removeItem('accessToken')
            localStorage.removeItem('userData')
            window.location.href = '/login'
          }
        }
        return Promise.reject(error)
      },
    )
  }

  return api
}

export function createApiServices(baseURL: string) {
  const api = createApiClient(baseURL)

  const fileApi = {
    getFileUrl: (filePath: string | null | undefined) => {
      if (!filePath || filePath.trim() === '') return null
      if (filePath.startsWith('http://') || filePath.startsWith('https://')) {
        return filePath.replace(/(\/api\/avatars\/):+(\d+)/, '$1$2')
      }
      if (filePath.startsWith('local:')) {
        let userId = filePath.substring(5).replace(/^:+/, '')
        if (!userId) return null
        return `${baseURL}/api/avatars/${userId}`
      }
      let path = filePath
      if (path.startsWith('/api/files/')) path = path.substring('/api/files/'.length)
      if (path.startsWith('api/files/')) path = path.substring('api/files/'.length)
      if (path.startsWith('/')) path = path.substring(1)
      return `${baseURL}/api/files/${path}`
    },
    getImageUrlLarge: (filePath: string) => {
      const base = fileApi.getFileUrl(filePath)
      if (!base) return null
      return base.replace(/\/medium\//, '/large/').replace(/\/small\//, '/large/')
    },
    getImageUrlBySize: (filePath: string, size = 'medium') => {
      const base = fileApi.getFileUrl(filePath)
      if (!base) return null
      const target = String(size).toLowerCase()
      if (!['small', 'medium', 'large', 'original'].includes(target)) return base
      return base
        .replace(/\/small\//, `/${target}/`)
        .replace(/\/medium\//, `/${target}/`)
        .replace(/\/large\//, `/${target}/`)
        .replace(/\/original\//, `/${target}/`)
    },
    getImageSrcSet: (filePath: string) => {
      const small = fileApi.getImageUrlBySize(filePath, 'small')
      const medium = fileApi.getImageUrlBySize(filePath, 'medium')
      const large = fileApi.getImageUrlBySize(filePath, 'large')
      return `${small} 300w, ${medium} 600w, ${large} 1200w`
    },
    getImageUrlOriginal: (filePath: string) => {
      const base = fileApi.getFileUrl(filePath)
      if (!base) return null
      return base.replace(/\/medium\//, '/original/').replace(/\/small\//, '/original/').replace(/\/large\//, '/original/')
    },
  }

  return {
    api,
    fileApi,
    productApi: {
      getAll: () => api.get('/api/products'),
      getById: (id: number | string) => api.get(`/api/products/${id}`),
    },
    categoryApi: {
      getAll: () => api.get('/api/categories'),
      getById: (id: number | string) => api.get(`/api/categories/${id}`),
    },
    authApi: {
      login: (credentials: unknown) => api.post('/api/auth/login', credentials),
      register: (userData: unknown) => api.post('/api/publicapi/auth/register', userData),
      logout: () => api.post('/api/auth/logout'),
      getMe: () => api.get('/api/auth/me'),
      updateMe: (userData: unknown) => api.put('/api/auth/me', userData),
      sendSmsCode: (phone: string) => api.post('/api/auth/send-sms-code', { phone }),
      verifySmsCode: (phone: string, code: string) => api.post('/api/auth/verify-sms-code', { phone, code }),
      completeRegistration: (data: unknown) => api.post('/api/auth/complete-registration', data),
      uploadAvatar: (file: File) => {
        const formData = new FormData()
        formData.append('file', file)
        return api.post('/api/auth/me/avatar', formData)
      },
    },
    orderApi: {
      create: (orderData: unknown) => api.post('/api/apps/ecommerce/orders', orderData),
      list: (params: unknown) => api.get('/api/apps/ecommerce/orders', { params }),
      listMy: (params: unknown) => api.get('/api/apps/ecommerce/orders/me', { params }),
      getById: (idOrOrderNumber: string | number) => api.get(`/api/apps/ecommerce/orders/${idOrOrderNumber}`),
    },
    geoApi: {
      getLocation: () => api.get('/api/geo/location'),
    },
    settingsApi: {
      getPriceTiers: () => api.get('/api/settings/price-tiers'),
      getPaymentDetails: () => api.get('/api/settings/payment-details'),
    },
    checkoutApi: {
      saveLead: (
        data: {
          fullName: string
          email: string
          phone: string
          step?: string
          items?: Array<{ productId: number; quantity: number }>
        },
        issueToken = false,
      ) =>
        api.post('/api/checkout/lead', data, { params: issueToken ? { issueToken: true } : {} }),
    },
    cartApi: {
      validate: (items: unknown[]) => api.post('/api/cart/validate', {
        items: (items as Array<{ product?: { id?: number }; productId?: number; quantity: number }>)
          .filter(item => (item.product?.id ?? item.productId) != null)
          .map(item => ({
            productId: item.product?.id ?? item.productId,
            quantity: item.quantity,
          })),
      }),
    },
    companyApi: {
      suggest: (q: string, count = 8) =>
        api.get('/api/publicapi/company/suggest', { params: { q, count } }),
      findByInn: (inn: string) =>
        api.get('/api/publicapi/company/party', { params: { inn } }),
    },
    wholesaleApi: {
      submitLead: (data: {
        name: string
        phone: string
        inn?: string
        message?: string
        website?: string
      }) => api.post('/api/publicapi/wholesale/lead', data),
    },
  }
}

export type ApiServices = ReturnType<typeof createApiServices>
