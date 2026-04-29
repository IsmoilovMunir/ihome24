import axios from 'axios'

const resolveApiBaseUrl = () => {
  const configuredBase = (import.meta.env.VITE_API_BASE_URL || '').trim()
  const fallbackBase = 'http://localhost:8080'
  const rawBase = configuredBase || fallbackBase

  if (typeof window === 'undefined') {
    return rawBase
  }

  const currentHost = window.location.hostname
  const isCurrentHostLocal = ['localhost', '127.0.0.1', '::1'].includes(currentHost)
  const isConfiguredLocalhost = /^https?:\/\/(localhost|127\.0\.0\.1|\[::1\])(?::\d+)?$/i.test(rawBase)

  // Защита от ошибочного прод-конфига: если сайт открыт не с localhost,
  // но API_BASE указывает на localhost, используем текущий origin.
  if (!isCurrentHostLocal && isConfiguredLocalhost) {
    return window.location.origin
  }

  return rawBase
}

const API_BASE_URL = resolveApiBaseUrl()

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Добавляем токен к запросам если он есть
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  // FormData: не задавать Content-Type — браузер сам добавит boundary
  if (config.data instanceof FormData) {
    delete config.headers['Content-Type']
  }
  return config
})

// Обработка ошибок
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('userData')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export const productApi = {
  getAll: () => api.get('/api/products'),
  getById: (id) => api.get(`/api/products/${id}`),
}

export const categoryApi = {
  getAll: () => api.get('/api/categories'),
  getById: (id) => api.get(`/api/categories/${id}`),
}

export const authApi = {
  login: (credentials) => api.post('/api/auth/login', credentials),
  register: (userData) => api.post('/api/publicapi/auth/register', userData),
  logout: () => api.post('/api/auth/logout'),
  getMe: () => api.get('/api/auth/me'),
  updateMe: (userData) => api.put('/api/auth/me', userData),
  sendSmsCode: (phone) => api.post('/api/auth/send-sms-code', { phone }),
  verifySmsCode: (phone, code) => api.post('/api/auth/verify-sms-code', { phone, code }),
  completeRegistration: (data) => api.post('/api/auth/complete-registration', data),
  uploadAvatar: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    // FormData: interceptor уберёт Content-Type, браузер сам подставит boundary
    return api.post('/api/auth/me/avatar', formData)
  },
}

export const orderApi = {
  create: (orderData) => api.post('/api/apps/ecommerce/orders', orderData),
  list: (params) => api.get('/api/apps/ecommerce/orders', { params }),
  /** Список заказов текущего пользователя (по токену). Только свои заказы. */
  listMy: (params) => api.get('/api/apps/ecommerce/orders/me', { params }),
  getById: (idOrOrderNumber) => api.get(`/api/apps/ecommerce/orders/${idOrOrderNumber}`),
}

export const geoApi = {
  getLocation: () => api.get('/api/geo/location'),
}

/** Уровни цен (розница / мелкий опт / крупный опт) для расчёта цены по количеству */
export const settingsApi = {
  getPriceTiers: () => api.get('/api/settings/price-tiers'),
}

export const cartApi = {
  /** Валидирует корзину: проверяет существование товаров, возвращает актуальные данные и список удалённых */
  validate: (items) => api.post('/api/cart/validate', {
    items: items
      .filter(item => (item.product?.id ?? item.productId) != null)
      .map(item => ({
        productId: item.product?.id ?? item.productId,
        quantity: item.quantity,
      })),
  }),
}

export const fileApi = {
  getFileUrl: (filePath) => {
    if (!filePath || filePath.trim() === '') return null
    // Нормализация старых URL аватаров вида /api/avatars/:2
    if (filePath.startsWith('http://') || filePath.startsWith('https://')) {
      // Убираем лишние двоеточия перед ID: /api/avatars/:2 -> /api/avatars/2
      return filePath.replace(/(\/api\/avatars\/):+(\d+)/, '$1$2')
    }

    // Локальный аватар (fallback при недоступности MinIO)
    if (filePath.startsWith('local:')) {
      let userId = filePath.substring(5)
      // Защита от некорректных значений вида "local::2" -> ":2"
      if (typeof userId === 'string') {
        userId = userId.replace(/^:+/, '')
      }
      if (!userId) return null
      return `${API_BASE_URL}/api/avatars/${userId}`
    }

    // Если путь уже начинается с /api/files/, убираем этот префикс
    if (filePath.startsWith('/api/files/')) {
      filePath = filePath.substring('/api/files/'.length)
    }

    // Если путь начинается с api/files/, убираем этот префикс
    if (filePath.startsWith('api/files/')) {
      filePath = filePath.substring('api/files/'.length)
    }

    // Убираем начальный слеш если есть
    if (filePath.startsWith('/')) {
      filePath = filePath.substring(1)
    }

    return `${API_BASE_URL}/api/files/${filePath}`
  },
  /** URL варианта large (1200px) — для прогрессивной загрузки главного фото */
  getImageUrlLarge: (filePath) => {
    if (!filePath || filePath.trim() === '') return null
    const base = fileApi.getFileUrl(filePath)
    if (!base) return null
    return base.replace(/\/medium\//, '/large/').replace(/\/small\//, '/large/')
  },
  /** URL изображения нужного размера (small | medium | large | original). */
  getImageUrlBySize: (filePath, size = 'medium') => {
    if (!filePath || filePath.trim() === '') return null
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
  /** Srcset из доступных размеров (small/medium/large) для responsive image. */
  getImageSrcSet: (filePath) => {
    if (!filePath || filePath.trim() === '') return null
    const small = fileApi.getImageUrlBySize(filePath, 'small')
    const medium = fileApi.getImageUrlBySize(filePath, 'medium')
    const large = fileApi.getImageUrlBySize(filePath, 'large')
    return `${small} 300w, ${medium} 600w, ${large} 1200w`
  },
  /** URL оригинала — максимальное качество (если доступен) */
  getImageUrlOriginal: (filePath) => {
    if (!filePath || filePath.trim() === '') return null
    const base = fileApi.getFileUrl(filePath)
    if (!base) return null
    return base.replace(/\/medium\//, '/original/').replace(/\/small\//, '/original/').replace(/\/large\//, '/original/')
  },
}

export default api
