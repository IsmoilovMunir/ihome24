import { defineStore } from 'pinia'
import { productApi, categoryApi } from '~/utils/api'
import { toPlainSerializable } from '~/utils/serializable'

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
    /**
     * @param {{ background?: boolean }} opts
     * background=true — обновить без спиннера, если товары уже показаны (после SSR)
     */
    async fetchProducts({ background = false } = {}) {
      const silent = background && this.products.length > 0
      if (!silent) {
        this.loading = true
        this.error = null
      }
      try {
        const response = await productApi.getAll()
        const list = Array.isArray(response) ? response : response?.data
        this.products = toPlainSerializable(list) || []
        if (silent) this.error = null
      } catch (error) {
        const message = error?.message || 'Не удалось загрузить товары'
        if (!silent) this.error = message
        console.error('Error fetching products:', error)
      } finally {
        if (!silent) this.loading = false
      }
    },

    async fetchProductById(id) {
      this.loading = true
      this.error = null
      try {
        const response = await productApi.getById(id)
        this.selectedProduct = toPlainSerializable(response.data)
        return this.selectedProduct
      } catch (error) {
        this.error = error.message
        console.error('Error fetching product:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchCategories({ background = false } = {}) {
      if (background && this.categories.length > 0) {
        try {
          const response = await categoryApi.getAll()
          this.categories = toPlainSerializable(response.data) || []
        } catch (error) {
          console.error('Error fetching categories:', error)
        }
        return
      }
      if (!background && this.categories.length > 0) return
      try {
        const response = await categoryApi.getAll()
        this.categories = toPlainSerializable(response.data) || []
      } catch (error) {
        console.error('Error fetching categories:', error)
      }
    },
  },
})
