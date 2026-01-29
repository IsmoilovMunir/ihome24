<template>
  <div class="container mx-auto px-4 py-8 bg-[#3A3331]">
    <h1 class="text-3xl font-bold text-white mb-8">Корзина</h1>

    <div v-if="cartStore.isEmpty" class="text-center py-12">
      <svg
        class="mx-auto h-24 w-24 text-gray-400 mb-4"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"
        />
      </svg>
      <p class="text-xl text-gray-300 mb-4">Ваша корзина пуста</p>
      <router-link
        to="/products"
        class="inline-block bg-primary-600 text-white px-6 py-3 rounded-md hover:bg-primary-700 transition-colors"
      >
        Перейти в каталог
      </router-link>
    </div>

    <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- Cart Items -->
      <div class="lg:col-span-2">
        <div class="bg-white rounded-lg shadow-md p-6">
          <div class="space-y-4">
            <div
              v-for="item in cartStore.items"
              :key="item.product.id"
              class="flex items-center space-x-4 border-b pb-4"
            >
              <router-link :to="`/products/${item.product.id}`" class="flex-shrink-0">
                <img
                  v-if="getImageUrl(item.product)"
                  :src="getImageUrl(item.product)"
                  :alt="item.product.name"
                  class="w-24 h-24 object-cover rounded"
                />
                <div
                  v-else
                  class="w-24 h-24 bg-[#26211E] rounded flex items-center justify-center"
                >
                  <svg
                    class="w-12 h-12 text-gray-300"
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
              </router-link>

              <div class="flex-1">
                <router-link
                  :to="`/products/${item.product.id}`"
                  class="text-lg font-semibold text-gray-900 hover:text-primary-600 transition-colors"
                >
                  {{ item.product.name }}
                </router-link>
                <p class="text-gray-600 text-sm mt-1">
                  {{ formatPrice(item.product.price) }}
                </p>
              </div>

              <div class="flex items-center space-x-2">
                <button
                  @click="cartStore.updateQuantity(item.product.id, item.quantity - 1)"
                  class="px-3 py-1 border rounded hover:bg-gray-100"
                >
                  -
                </button>
                <span class="w-12 text-center">{{ item.quantity }}</span>
                <button
                  @click="cartStore.updateQuantity(item.product.id, item.quantity + 1)"
                  class="px-3 py-1 border rounded hover:bg-gray-100"
                >
                  +
                </button>
              </div>

              <div class="text-right">
                <p class="text-lg font-semibold text-gray-900">
                  {{ formatPrice(item.product.price * item.quantity) }}
                </p>
                <button
                  @click="cartStore.removeFromCart(item.product.id)"
                  class="text-red-600 hover:text-red-800 text-sm mt-1"
                >
                  Удалить
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Order Summary -->
      <div class="lg:col-span-1">
        <div class="bg-white rounded-lg shadow-md p-6 sticky top-20">
          <h2 class="text-xl font-semibold mb-4">Итого</h2>
          <div class="space-y-2 mb-4">
            <div class="flex justify-between text-gray-600">
              <span>Товаров:</span>
              <span>{{ cartStore.totalItems }} шт.</span>
            </div>
            <div class="flex justify-between text-xl font-bold text-gray-900 pt-4 border-t">
              <span>Сумма:</span>
              <span>{{ formatPrice(cartStore.totalPrice) }}</span>
            </div>
          </div>
          <router-link
            to="/checkout"
            class="block w-full bg-primary-600 text-white py-3 rounded-md hover:bg-primary-700 transition-colors text-center font-semibold"
          >
            Оформить заказ
          </router-link>
          <button
            @click="cartStore.clearCart()"
            class="w-full mt-2 text-gray-600 hover:text-gray-800 text-sm"
          >
            Очистить корзину
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useCartStore } from '../stores/cart'
import { fileApi } from '../services/api'

const cartStore = useCartStore()

const getImageUrl = (product) => {
  if (product.imageUrl) {
    return fileApi.getFileUrl(product.imageUrl)
  }
  if (product.images && product.images.length > 0) {
    const img = product.images[0]
    // В ProductImageResponse поле называется imageUrl, не url!
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
