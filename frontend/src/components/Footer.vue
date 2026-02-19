<template>
  <footer class="text-white mt-auto border-t border-white/20 md:pb-0" :class="[isProductDetail ? 'pb-40' : 'pb-20', isCart || isCheckout ? 'bg-[#2E2826]' : 'bg-[#3A3331]']">
    <div class="container mx-auto px-4 py-8">
      <div class="flex flex-col items-center gap-4 sm:flex-row sm:justify-center sm:gap-8 pb-6">
        <router-link to="/support/contacts" class="text-gray-400 hover:text-white transition-colors text-sm">
          Контакты
        </router-link>
        <router-link to="/support/oferta" class="text-gray-400 hover:text-white transition-colors text-sm">
          Договор оферты
        </router-link>
      </div>
      <div class="border-t border-white/10 pt-8 text-center text-gray-400">
        <p>&copy; 2025 iHome24. Все права защищены.</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isProductDetail = computed(() => route.path.startsWith('/products/') && route.params.id)
const isCart = computed(() => route.path === '/cart')
const isCheckout = computed(() => route.path === '/checkout')
</script>
