import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

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
}

export const orderApi = {
  create: (orderData) => api.post('/api/apps/ecommerce/orders', orderData),
}

export const geoApi = {
  getLocation: () => api.get('/api/geo/location'),
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
    if (filePath.startsWith('http://') || filePath.startsWith('https://')) return filePath
    
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
  /** URL оригинала — максимальное качество (если доступен) */
  getImageUrlOriginal: (filePath) => {
    if (!filePath || filePath.trim() === '') return null
    const base = fileApi.getFileUrl(filePath)
    if (!base) return null
    return base.replace(/\/medium\//, '/original/').replace(/\/small\//, '/original/').replace(/\/large\//, '/original/')
  },
}

export default api
