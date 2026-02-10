<template>
  <div class="min-h-full bg-[#2E2826]">
    <div class="container mx-auto px-3 md:px-4 py-4 md:py-8">
    <div v-if="cartStore.isEmpty" class="flex flex-col items-center justify-center py-24 text-center">
      <div data-test="basket-empty-title" class="text-2xl font-bold text-white mb-2">
        Ваша корзина пуста
      </div>
      <div data-test="basket-empty-subtitle" class="text-gray-400 mb-8">
        Выберите превосходную технику и аксессуары
      </div>
      <div class="flex flex-wrap gap-4 justify-center">
        <router-link
          to="/"
          data-test="basket-empty-button"
          class="inline-block bg-[#3A3331] text-white px-6 py-3 rounded-2xl border-2 border-[#F47327] hover:bg-[#F47327]/50 active:bg-[#F47327]/50 transition-colors font-semibold"
        >
          Перейти на главную
        </router-link>
        <router-link
          to="/login"
          data-test="basket-empty-button"
          class="inline-block bg-[#3A3331] text-white px-6 py-3 rounded-2xl border-2 border-[#F47327] hover:bg-[#F47327]/50 active:bg-[#F47327]/50 transition-colors font-semibold"
        >
          Войти
        </router-link>
      </div>
    </div>

    <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-4 md:gap-8">
      <!-- Cart Items -->
      <div class="lg:col-span-2">
        <div class="rounded-2xl shadow-md p-4 md:p-6" style="background-color: #3A3331;">
          <div class="space-y-3 md:space-y-4">
            <div
              v-for="item in cartStore.items"
              :key="item.product.id"
              class="flex flex-col sm:flex-row sm:items-center gap-3 sm:gap-4 border-b border-white/20 pb-3 md:pb-4"
            >
              <router-link :to="`/products/${item.product.id}`" class="flex-shrink-0 flex items-center gap-3 sm:block">
                <img
                  v-if="getImageUrl(item.product)"
                  :src="getImageUrl(item.product)"
                  :alt="item.product.name"
                  class="w-16 h-16 sm:w-20 sm:h-20 md:w-24 md:h-24 object-cover rounded flex-shrink-0"
                />
                <div
                  v-else
                  class="w-16 h-16 sm:w-20 sm:h-20 md:w-24 md:h-24 bg-[#26211E] rounded flex items-center justify-center flex-shrink-0"
                >
                  <svg
                    class="w-8 h-8 sm:w-10 sm:h-10 md:w-12 md:h-12 text-gray-300"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                    />
                  </svg>
                </div>
                <div class="flex-1 min-w-0 sm:hidden">
                  <router-link
                    :to="`/products/${item.product.id}`"
                    class="text-base font-semibold text-white hover:text-[#C56129] transition-colors line-clamp-2"
                  >
                    {{ item.product.name }}
                  </router-link>
                  <p class="text-gray-400 text-sm mt-0.5">
                    {{ formatPrice(item.product.price) }}
                  </p>
                </div>
              </router-link>

              <div class="flex-1 min-w-0 hidden sm:block">
                <router-link
                  :to="`/products/${item.product.id}`"
                  class="text-base md:text-lg font-semibold text-white hover:text-[#C56129] transition-colors"
                >
                  {{ item.product.name }}
                </router-link>
                <p class="text-gray-400 text-sm mt-1">
                  {{ formatPrice(item.product.price) }}
                </p>
              </div>

              <div class="flex items-center justify-between sm:justify-end gap-2 flex-wrap">
                <div class="flex items-center space-x-2">
                  <button
                    @click="cartStore.updateQuantity(item.product.id, item.quantity - 1)"
                    class="px-2 py-1 md:px-3 md:py-1 border border-white/30 rounded text-white hover:bg-white/10 text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    -
                  </button>
                  <span class="w-8 md:w-12 text-center text-white text-sm md:text-base">{{ item.quantity }}</span>
                  <button
                    @click="cartStore.updateQuantity(item.product.id, item.quantity + 1)"
                    :disabled="item.quantity >= (item.product?.stockQuantity ?? 999999)"
                    class="px-2 py-1 md:px-3 md:py-1 border border-white/30 rounded text-white hover:bg-white/10 text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    +
                  </button>
                </div>
                <div class="flex items-center gap-2">
                  <p class="text-base md:text-lg font-semibold text-white">
                    {{ formatPrice(item.product.price * item.quantity) }}
                  </p>
                  <button
                    @click="cartStore.removeFromCart(item.product.id)"
                    class="text-gray-400 hover:text-gray-300 text-xs md:text-sm"
                  >
                    Удалить
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Order Summary -->
      <div class="lg:col-span-1">
        <div class="rounded-2xl shadow-md p-4 md:p-6 sticky top-20" style="background-color: #3A3331;">
          <h2 class="text-xl font-semibold mb-4 text-white">Итого</h2>
          <div class="space-y-2 mb-4">
            <div class="flex justify-between text-gray-400">
              <span>Товаров:</span>
              <span>{{ cartStore.totalItems }} шт.</span>
            </div>
            <div class="flex justify-between text-xl font-bold text-white pt-4 border-t border-white/20">
              <span>Сумма:</span>
              <span>{{ formatPrice(cartStore.totalPrice) }}</span>
            </div>
          </div>
          <router-link
            to="/checkout"
            data-test="basket-order-to-checkout"
            class="block w-full bg-[#3A3331] text-white py-3 rounded-2xl border-2 border-[#F47327] hover:bg-[#F47327]/50 active:bg-[#F47327]/50 transition-colors text-center font-semibold"
          >
            Оформить заказ
          </router-link>
          <button
            @click="cartStore.clearCart()"
            class="w-full mt-2 text-gray-400 hover:text-gray-300 text-sm"
          >
            Очистить корзину
          </button>
        </div>
      </div>
    </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useCartStore } from '../stores/cart'
import { fileApi } from '../services/api'

const cartStore = useCartStore()

onMounted(() => {
  cartStore.validateCart()
})

const getImageUrl = (product) => {
  if (product.imageUrl) {
    return fileApi.getFileUrl(product.imageUrl)
  }
  if (product.images && product.images.length > 0) {
    const img = product.images[0]
    const imgUrl = img.imageUrl || img.url || (typeof img === 'string' ? img : null)
    if (imgUrl) {
      return fileApi.getFileUrl(imgUrl)
    }
  }
  return null
}

const formatPrice = (price) => {
  if (!price) return '0 ₽'
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB',
    minimumFractionDigits: 0,
  }).format(price)
}
</script>
