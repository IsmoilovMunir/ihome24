import { defineStore } from 'pinia'

export const usePersonalMenuStore = defineStore('personalMenu', {
  state: () => ({
    open: false,
  }),
  actions: {
    toggle() {
      this.open = !this.open
    },
    openMenu() {
      this.open = true
    },
    closeMenu() {
      this.open = false
    },
  },
})
