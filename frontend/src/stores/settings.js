import { defineStore } from 'pinia'
import { settingsApi } from '../services/api'

/**
 * Уровни цен (розница / мелкий опт / крупный опт).
 * @typedef {{ minQty: number, maxQty: number | null, discountPercent: number, label: string }} PriceTier
 */

export const useSettingsStore = defineStore('settings', {
  state: () => ({
    /** @type {PriceTier[]} */
    priceTiers: [],
    priceTiersLoaded: false,
  }),

  getters: {
    /** Уровни для расчёта (если с сервера пусто — подставляем по умолчанию) */
    tiersForCalculation(state) {
      if (state.priceTiers?.length) return state.priceTiers
      return [
        { minQty: 1, maxQty: 10, discountPercent: 0, label: 'Розничная' },
        { minQty: 11, maxQty: 100, discountPercent: 10, label: 'Мелкий опт' },
        { minQty: 101, maxQty: null, discountPercent: 15, label: 'Крупный опт' },
      ]
    },
    /**
     * Цена за единицу с учётом объёмной скидки (без использования других геттеров — только state).
     */
    unitPriceForQuantity(state) {
      const defaultTiers = [
        { minQty: 1, maxQty: 10, discountPercent: 0, label: 'Розничная' },
        { minQty: 11, maxQty: 100, discountPercent: 10, label: 'Мелкий опт' },
        { minQty: 101, maxQty: null, discountPercent: 15, label: 'Крупный опт' },
      ]
      const tiers = state.priceTiers?.length ? state.priceTiers : defaultTiers
      return (displayPrice, quantity) => {
        if (!displayPrice || displayPrice <= 0) return 0
        if (!tiers.length) return displayPrice
        const q = Math.max(0, Math.floor(quantity))
        for (const tier of tiers) {
          const min = tier.minQty ?? 0
          const max = tier.maxQty
          if (q >= min && (max == null || q <= max)) {
            const discount = (tier.discountPercent ?? 0) / 100
            return Math.round(displayPrice * (1 - discount) * 100) / 100
          }
        }
        return displayPrice
      }
    },
  },

  actions: {
    async fetchPriceTiers() {
      if (this.priceTiersLoaded) return
      try {
        const { data } = await settingsApi.getPriceTiers()
        this.priceTiers = data?.tiers ?? []
        this.priceTiersLoaded = true
      } catch (err) {
        console.warn('Price tiers load failed:', err)
        this.priceTiers = []
        this.priceTiersLoaded = true
      }
    },
  },
})
