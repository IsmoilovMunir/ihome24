import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json'
  }
})

// Интерцептор для обработки ошибок
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error.response?.data || error.message)
    return Promise.reject(error)
  }
)

// API для товаров
export const productsAPI = {
  // Получить все товары
  getAll: async () => {
    const response = await api.get('/products')
    // Бэкенд возвращает массив напрямую, а не в data
    return { data: Array.isArray(response.data) ? response.data : response.data || [] }
  },
  
  // Получить товар по ID
  getById: async (id) => {
    const response = await api.get(`/products/${id}`)
    return { data: response.data }
  },
  
  // Поиск товаров (пока используем фильтрацию на клиенте)
  search: async (query) => {
    const response = await api.get('/products')
    const products = Array.isArray(response.data) ? response.data : response.data || []
    // Фильтруем товары на клиенте по названию и описанию
    const filtered = products.filter(product => 
      product.name?.toLowerCase().includes(query.toLowerCase()) ||
      product.description?.toLowerCase().includes(query.toLowerCase())
    )
    return { data: filtered }
  }
}

// API для категорий
export const categoriesAPI = {
  // Получить все категории
  getAll: async () => {
    const response = await api.get('/categories')
    return { data: Array.isArray(response.data) ? response.data : response.data || [] }
  },
  
  // Получить категорию по ID
  getById: async (id) => {
    const response = await api.get(`/categories/${id}`)
    return { data: response.data }
  }
}

// API для заказов
export const ordersAPI = {
  // Создать заказ
  create: (orderData) => api.post('/orders', orderData),
  
  // Получить заказ по ID
  getById: (id) => api.get(`/orders/${id}`)
}

export default api
