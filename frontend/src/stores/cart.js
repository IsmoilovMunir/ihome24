import { defineStore } from 'pinia'
import { cartApi } from '../services/api'
import { useSettingsStore } from './settings'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: JSON.parse(localStorage.getItem('cart') || '[]'),
  }),

  getters: {
    totalItems: (state) => {
      return state.items.reduce((sum, item) => sum + item.quantity, 0)
    },
    totalPrice: (state) => {
      const settingsStore = useSettingsStore()
      const total = state.items.reduce((sum, item) => {
        const price = item.priceBase ?? item.product?.price ?? 0
        const qty = item.quantity ?? 0
        const unitPrice = settingsStore.unitPriceForQuantity(price, qty)
        return sum + unitPrice * qty
      }, 0)
      return Math.round(total * 100) / 100
    },
    isEmpty: (state) => state.items.length === 0,
  },

  actions: {
    addToCart(product, quantity = 1, variant = null) {
      const variantKey =
        variant?.variantId ||
        variant?.sku ||
        variant?.attributes?.size ||
        variant?.attributes?.color ||
        null
      const variantLabel =
        variant?.attributes?.size ||
        variant?.attributes?.color ||
        null
      const priceBase = (variant && variant.price && typeof variant.price.base !== 'undefined')
        ? Number(variant.price.base)
        : (product?.price ?? 0)
      // Остаток варианта: только из variant.stock.quantity, без подстановки общего остатка товара
      const variantStockQuantity =
        (variant && typeof variant.stock?.quantity === 'number')
          ? variant.stock.quantity
          : null

      const existingItem = this.items.find(item =>
        item.product.id === product.id &&
        (item.variantKey ?? null) === (variantKey ?? null),
      )
      
      if (existingItem) {
        existingItem.quantity += quantity
        existingItem.priceBase = priceBase
        existingItem.variantLabel = variantLabel
        existingItem.variantStockQuantity = variantStockQuantity
      } else {
        this.items.push({
          product,
          quantity,
          variantKey,
          variantLabel,
          priceBase,
          variantStockQuantity,
        })
      }
      
      this.saveCart()
    },

    removeFromCart(productId, variantKey = null) {
      this.items = this.items.filter(item =>
        !(item.product.id === productId && (item.variantKey ?? null) === (variantKey ?? null)),
      )
      this.saveCart()
    },

    updateQuantity(productId, quantity, variantKey = null) {
      const item = this.items.find(item =>
        item.product.id === productId && (item.variantKey ?? null) === (variantKey ?? null),
      )
      if (item) {
        if (quantity <= 0) {
          this.removeFromCart(productId, variantKey)
        } else {
          const maxQty =
            (typeof item.variantStockQuantity === 'number'
              ? item.variantStockQuantity
              : (item.product?.stockQuantity ?? 999999))
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
        const serverItems = data.validItems || []

        // Карта по productId из ответа сервера (последняя версия товара)
        const byProductId = new Map()
        for (const item of serverItems) {
          const id = item.product?.id
          if (id != null) {
            byProductId.set(id, item)
          }
        }

        const newItems = []

        // Проходим по нашим текущим позициям и обновляем только данные товара,
        // сохраняя variantKey / variantLabel и количество как есть.
        for (const old of this.items) {
          const productId = old.product?.id
          if (productId == null) continue

          const serverItem = byProductId.get(productId)
          // Если сервер говорит, что товар недействителен/удалён — пропускаем его
          if (!serverItem || !serverItem.product) continue

          newItems.push({
            product: serverItem.product,
            quantity: old.quantity,
            variantKey: old.variantKey ?? null,
            variantLabel: old.variantLabel ?? null,
            priceBase: old.priceBase ?? (serverItem.product?.price ?? old.product?.price ?? 0),
            variantStockQuantity: typeof old.variantStockQuantity === 'number'
              ? old.variantStockQuantity
              : (typeof serverItem.product?.stockQuantity === 'number'
                  ? serverItem.product.stockQuantity
                  : (typeof old.product?.stockQuantity === 'number' ? old.product.stockQuantity : null)),
          })
        }

        this.items = newItems
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
