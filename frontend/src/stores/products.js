import { defineStore } from 'pinia'
import { productApi, categoryApi } from '../services/api'

export const useProductsStore = defineStore('products', {
  state: () => ({
    products: [],
    categories: [],
    loading: false,
    error: null,
    selectedProduct: null,
  }),

  getters: {
    featuredProducts: (state) => state.products.filter(p => p.isFeatured),
    productsByCategory: (state) => (categoryId) => {
      return state.products.filter(p => p.category?.id === categoryId)
    },
  },

  actions: {
    async fetchProducts() {
      this.loading = true
      this.error = null
      try {
        const response = await productApi.getAll()
        this.products = response.data
      } catch (error) {
        this.error = error.message
        console.error('Error fetching products:', error)
      } finally {
        this.loading = false
      }
    },

    async fetchProductById(id) {
      this.loading = true
      this.error = null
      try {
        const response = await productApi.getById(id)
        this.selectedProduct = response.data
        return response.data
      } catch (error) {
        this.error = error.message
        console.error('Error fetching product:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchCategories() {
      try {
        const response = await categoryApi.getAll()
        this.categories = response.data
      } catch (error) {
        console.error('Error fetching categories:', error)
      }
    },
  },
})
