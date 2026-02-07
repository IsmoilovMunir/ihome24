import { defineStore } from 'pinia'
import { cartApi } from '../services/api'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: JSON.parse(localStorage.getItem('cart') || '[]'),
  }),

  getters: {
    totalItems: (state) => {
      return state.items.reduce((sum, item) => sum + item.quantity, 0)
    },
    totalPrice: (state) => {
      return state.items.reduce((sum, item) => {
        return sum + (item.product.price * item.quantity)
      }, 0)
    },
    isEmpty: (state) => state.items.length === 0,
  },

  actions: {
    addToCart(product, quantity = 1) {
      const existingItem = this.items.find(item => item.product.id === product.id)
      
      if (existingItem) {
        existingItem.quantity += quantity
      } else {
        this.items.push({
          product,
          quantity,
        })
      }
      
      this.saveCart()
    },

    removeFromCart(productId) {
      this.items = this.items.filter(item => item.product.id !== productId)
      this.saveCart()
    },

    updateQuantity(productId, quantity) {
      const item = this.items.find(item => item.product.id === productId)
      if (item) {
        if (quantity <= 0) {
          this.removeFromCart(productId)
        } else {
          const maxQty = item.product?.stockQuantity ?? 999999
          item.quantity = Math.min(quantity, maxQty)
          this.saveCart()
        }
      }
    },

    clearCart() {
      this.items = []
      this.saveCart()
    },

    /**
     * Валидирует корзину с бэкендом: убирает удалённые/неактивные товары,
     * обновляет цены и данные. Вызывать при открытии страницы корзины.
     */
    async validateCart() {
      if (this.items.length === 0) return
      try {
        const { data } = await cartApi.validate(this.items)
        this.items = (data.validItems || []).map(item => ({
          product: item.product,
          quantity: item.quantity,
        }))
        this.saveCart()
      } catch (err) {
        console.warn('Cart validation failed:', err)
      }
    },

    saveCart() {
      localStorage.setItem('cart', JSON.stringify(this.items))
    },
  },
})
