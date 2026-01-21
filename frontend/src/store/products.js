import { defineStore } from 'pinia'
import { ref } from 'vue'
import { productsAPI } from '../services/api'

export const useProductsStore = defineStore('products', () => {
  const products = ref([])
  const product = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // Загрузить все товары
  const fetchProducts = async () => {
    loading.value = true
    error.value = null
    try {
      const response = await productsAPI.getAll()
      
      // Проверяем формат ответа и извлекаем массив продуктов
      let productsData = []
      if (Array.isArray(response.data)) {
        productsData = response.data
      } else if (Array.isArray(response)) {
        productsData = response
      } else if (response && response.data && Array.isArray(response.data)) {
        productsData = response.data
      } else {
        console.warn('Unexpected API response format:', response)
        productsData = []
      }
      
      // Преобразуем данные от бэкенда в формат фронтенда
      products.value = productsData.map(product => {
        // Преобразуем images в gallery (массив URL)
        const gallery = product.images && product.images.length > 0 
          ? product.images.map(img => img.imageUrl).filter(url => url)
          : (product.imageUrl ? [product.imageUrl] : [])
        
        return {
          id: product.id,
          name: product.name,
          description: product.description || '',
          price: product.price ? parseFloat(product.price) : 0,
          imageUrl: gallery.length > 0 ? gallery[0] : (product.imageUrl || ''),
          sku: product.sku || '',
          stockQuantity: product.stockQuantity || 0,
          isActive: product.isActive !== false, // По умолчанию активен
          category: product.category?.name || '',
          gallery: gallery,
        }
      })
    } catch (err) {
      error.value = err.response?.data?.message || err.message || 'Ошибка при загрузке товаров'
      console.error('Error fetching products:', err)
    } finally {
      loading.value = false
    }
  }

  // Загрузить товар по ID
  const fetchProductById = async (id) => {
    loading.value = true
    error.value = null
    try {
      const response = await productsAPI.getById(id)
      const data = response.data
      console.log('Raw API response:', response)
      console.log('Product data:', data)
      console.log('Images array:', data.images)
      
      // Преобразуем данные от бэкенда в формат фронтенда
      // Преобразуем images в gallery (массив URL)
      let gallery = []
      if (data.images && Array.isArray(data.images) && data.images.length > 0) {
        // Сортируем по sortOrder
        const sortedImages = [...data.images].sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
        gallery = sortedImages.map(img => img.imageUrl || img).filter(url => url && url.trim())
      }
      
      // Если галерея пустая, но есть imageUrl, добавляем его
      if (gallery.length === 0 && data.imageUrl) {
        gallery = [data.imageUrl]
      }
      
      product.value = {
        id: data.id,
        name: data.name,
        description: data.description || '',
        price: data.price ? parseFloat(data.price) : 0,
        oldPrice: data.oldPrice ? parseFloat(data.oldPrice) : null,
        imageUrl: gallery.length > 0 ? gallery[0] : (data.imageUrl || ''),
        sku: data.sku || '',
        stockQuantity: data.stockQuantity || 0,
        isActive: data.isActive !== false,
        category: data.category?.name || '',
        gallery: gallery, // Галерея изображений
        characteristics: data.characteristics || [], // Характеристики (если есть)
        createdAt: data.createdAt,
        updatedAt: data.updatedAt,
      }
      
      console.log('Товар загружен:', product.value)
      console.log('Галерея изображений:', gallery)
    } catch (err) {
      error.value = err.response?.data?.message || err.message || 'Ошибка при загрузке товара'
      console.error('Error fetching product:', err)
    } finally {
      loading.value = false
    }
  }

  // Поиск товаров
  const searchProducts = async (query) => {
    loading.value = true
    error.value = null
    try {
      const response = await productsAPI.search(query)
      
      // Проверяем формат ответа
      let productsData = []
      if (Array.isArray(response.data)) {
        productsData = response.data
      } else if (Array.isArray(response)) {
        productsData = response
      } else {
        productsData = []
      }
      
      // Преобразуем данные от бэкенда в формат фронтенда
      products.value = productsData.map(product => ({
        id: product.id,
        name: product.name,
        description: product.description || '',
        price: product.price ? parseFloat(product.price) : 0,
        imageUrl: product.imageUrl || '',
        sku: product.sku || '',
        stockQuantity: product.stockQuantity || 0,
        isActive: product.isActive !== false,
        category: product.category?.name || '',
      }))
    } catch (err) {
      error.value = err.response?.data?.message || err.message || 'Ошибка при поиске товаров'
      console.error('Error searching products:', err)
    } finally {
      loading.value = false
    }
  }

  return {
    products,
    product,
    loading,
    error,
    fetchProducts,
    fetchProductById,
    searchProducts
  }
})
