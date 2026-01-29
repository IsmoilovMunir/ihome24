<template>
  <div class="container mx-auto px-4 py-8 bg-[#3A3331]">
    <h1 class="text-3xl font-bold text-white mb-8">Оформление заказа</h1>

    <div v-if="cartStore.isEmpty" class="text-center py-12">
      <p class="text-xl text-gray-300 mb-4">Ваша корзина пуста</p>
      <router-link
        to="/products"
        class="inline-block bg-primary-600 text-white px-6 py-3 rounded-md hover:bg-primary-700 transition-colors"
      >
        Перейти в каталог
      </router-link>
    </div>

    <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- Order Form -->
      <div class="lg:col-span-2">
        <div class="bg-white rounded-lg shadow-md p-6">
          <h2 class="text-xl font-semibold mb-6">Данные для доставки</h2>
          
          <form @submit.prevent="submitOrder" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                ФИО *
              </label>
              <input
                v-model="form.fullName"
                type="text"
                required
                class="w-full px-4 py-2 border rounded-md focus:ring-2 focus:ring-primary-600 focus:border-transparent"
                placeholder="Иванов Иван Иванович"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Email *
              </label>
              <input
                v-model="form.email"
                type="email"
                required
                class="w-full px-4 py-2 border rounded-md focus:ring-2 focus:ring-primary-600 focus:border-transparent"
                placeholder="example@mail.ru"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Телефон *
              </label>
              <input
                v-model="form.phone"
                type="tel"
                required
                class="w-full px-4 py-2 border rounded-md focus:ring-2 focus:ring-primary-600 focus:border-transparent"
                placeholder="+7 (999) 123-45-67"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Адрес доставки *
              </label>
              <textarea
                v-model="form.address"
                required
                rows="3"
                class="w-full px-4 py-2 border rounded-md focus:ring-2 focus:ring-primary-600 focus:border-transparent"
                placeholder="Город, улица, дом, квартира"
              ></textarea>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Комментарий к заказу
              </label>
              <textarea
                v-model="form.comment"
                rows="3"
                class="w-full px-4 py-2 border rounded-md focus:ring-2 focus:ring-primary-600 focus:border-transparent"
                placeholder="Дополнительная информация"
              ></textarea>
            </div>

            <div v-if="error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
              {{ error }}
            </div>

            <button
              type="submit"
              :disabled="loading"
              class="w-full bg-primary-600 text-white py-3 rounded-md hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors font-semibold"
            >
              {{ loading ? 'Оформление...' : 'Оформить заказ' }}
            </button>
          </form>
        </div>
      </div>

      <!-- Order Summary -->
      <div class="lg:col-span-1">
        <div class="bg-white rounded-lg shadow-md p-6 sticky top-20">
          <h2 class="text-xl font-semibold mb-4">Ваш заказ</h2>
          <div class="space-y-2 mb-4">
            <div
              v-for="item in cartStore.items"
              :key="item.product.id"
              class="flex justify-between text-sm"
            >
              <span class="text-gray-600">
                {{ item.product.name }} × {{ item.quantity }}
              </span>
              <span class="font-medium">
                {{ formatPrice(item.product.price * item.quantity) }}
              </span>
            </div>
          </div>
          <div class="border-t pt-4">
            <div class="flex justify-between text-xl font-bold text-gray-900">
              <span>Итого:</span>
              <span>{{ formatPrice(cartStore.totalPrice) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const cartStore = useCartStore()
const authStore = useAuthStore()

const loading = ref(false)
const error = ref(null)

const form = ref({
  fullName: authStore.user?.fullName || '',
  email: authStore.user?.email || '',
  phone: '',
  address: '',
  comment: '',
})

const formatPrice = (price) => {
  if (!price) return '0 ₽'
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB',
    minimumFractionDigits: 0,
  }).format(price)
}

const submitOrder = async () => {
  loading.value = true
  error.value = null

  try {
    // Здесь должна быть интеграция с API для создания заказа
    // Пока что просто очищаем корзину и показываем сообщение
    await new Promise(resolve => setTimeout(resolve, 1000)) // Имитация запроса
    
    alert('Заказ успешно оформлен! Номер заказа будет отправлен на ваш email.')
    cartStore.clearCart()
    router.push('/')
  } catch (err) {
    error.value = 'Ошибка при оформлении заказа. Попробуйте еще раз.'
    console.error('Order error:', err)
  } finally {
    loading.value = false
  }
}
</script>
