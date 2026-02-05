<template>
  <footer class="text-white mt-auto border-t border-white/20 md:pb-0" :class="[isProductDetail ? 'pb-40' : 'pb-20', isCart || isCheckout ? 'bg-[#2E2826]' : 'bg-[#3A3331]']">
    <div class="container mx-auto px-4 py-8">
      <div class="border-t border-white/10 mt-8 pt-8 text-center text-gray-400">
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
