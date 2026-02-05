<template>
  <div class="min-h-full bg-[#2E2826]">
  <div class="container mx-auto px-4 py-8">
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

    <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Left column: 3 sections -->
      <form class="lg:col-span-2 space-y-6" @submit.prevent="submitOrder">
        <!-- Section 1: Контактные данные -->
        <div class="rounded-2xl shadow-md p-6" style="background-color: #3A3331;">
          <h2 class="text-xl font-semibold mb-4 text-white">Контактные данные</h2>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-300 mb-1">ФИО *</label>
              <input
                v-model="form.fullName"
                type="text"
                required
                class="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:ring-2 focus:ring-[#F47327] focus:border-transparent"
                placeholder="Иванов Иван Иванович"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-300 mb-1">Email *</label>
              <input
                v-model="form.email"
                type="email"
                required
                class="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:ring-2 focus:ring-[#F47327] focus:border-transparent"
                placeholder="example@mail.ru"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-300 mb-1">Телефон *</label>
              <input
                v-model="form.phone"
                type="tel"
                required
                class="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:ring-2 focus:ring-[#F47327] focus:border-transparent"
                placeholder="+7 (999) 123-45-67"
              />
            </div>
          </div>
        </div>

        <!-- Section 2: Способ доставки -->
        <div id="checkout-delivery-method" data-test="checkout-delivery-method" class="rounded-2xl shadow-md p-6" style="background-color: #3A3331;">
          <h2 class="text-xl font-semibold mb-4 text-white">Способ доставки</h2>
          <div class="flex gap-3 mb-4">
            <button
              type="button"
              data-test="checkout-delivery-method-delivery-tab"
              :class="['flex-1 flex items-center justify-center gap-2 py-3 px-4 rounded-xl border-2 transition-colors', deliveryMethod === 'delivery' ? 'border-[#F47327] bg-[#F47327]/20 text-white' : 'border-white/20 text-gray-400 hover:border-white/40']"
              @click="deliveryMethod = 'delivery'"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4" />
              </svg>
              <span data-test="checkout-delivery-method-delivery-method-name">Доставка</span>
            </button>
            <button
              type="button"
              data-test="checkout-delivery-method-delivery-tab"
              :class="['flex-1 flex items-center justify-center gap-2 py-3 px-4 rounded-xl border-2 transition-colors', deliveryMethod === 'pickup' ? 'border-[#F47327] bg-[#F47327]/20 text-white' : 'border-white/20 text-gray-400 hover:border-white/40']"
              @click="deliveryMethod = 'pickup'"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <span data-test="checkout-delivery-method-delivery-method-name">Самовывоз</span>
            </button>
          </div>
          <div v-if="deliveryMethod === 'delivery'" data-test="checkout-delivery-method-select-delivery-cta" class="flex items-center justify-between p-4 rounded-xl bg-white/5 border border-white/10">
            <div>
              <span data-test="checkout-delivery-method-select-delivery-cta-subtitle" class="text-gray-300">Куда доставить заказ?</span>
            </div>
            <button type="button" data-test="checkout-cta-button" class="px-4 py-2 rounded-xl bg-[#3A3331] border-2 border-[#F47327] text-white font-semibold hover:bg-[#F47327]/50 transition-colors">
              Указать
            </button>
          </div>
          <div v-if="deliveryMethod === 'delivery'" class="mt-3">
            <label class="block text-sm font-medium text-gray-300 mb-1">Адрес доставки *</label>
            <textarea
              v-model="form.address"
              :required="deliveryMethod === 'delivery'"
              rows="2"
              class="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:ring-2 focus:ring-[#F47327] focus:border-transparent"
              placeholder="Город, улица, дом, квартира"
            ></textarea>
          </div>
          <div v-if="deliveryMethod === 'pickup'" class="mt-3">
            <label class="block text-sm font-medium text-gray-300 mb-1">Адрес пункта самовывоза *</label>
            <input
              v-model="form.pickupAddress"
              required
              type="text"
              class="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:ring-2 focus:ring-[#F47327] focus:border-transparent"
              placeholder="Адрес пункта выдачи"
            />
          </div>
        </div>

        <!-- Section 3: Способ оплаты -->
        <div data-test="checkout-payment-method" class="rounded-2xl shadow-md p-6" style="background-color: #3A3331;">
          <h2 class="text-xl font-semibold mb-4 text-white">Способ оплаты</h2>
          <div class="grid grid-cols-2 sm:grid-cols-3 gap-3">
            <button
              v-for="pm in paymentMethods"
              :key="pm.id"
              type="button"
              data-test="checkout-payment-method-card"
              :class="['flex flex-col items-center gap-2 p-4 rounded-xl border-2 transition-colors text-left', paymentMethod === pm.id ? 'border-[#F47327] bg-[#F47327]/20' : 'border-white/20 text-gray-400 hover:border-white/40']"
              @click="paymentMethod = pm.id"
            >
              <span data-test="checkout-payment-method-card-name" class="text-white font-medium text-sm">{{ pm.name }}</span>
              <span v-if="pm.desc" data-test="checkout-payment-method-card-description" class="text-gray-400 text-xs">{{ pm.desc }}</span>
            </button>
          </div>
          <div class="mt-4">
            <label class="block text-sm font-medium text-gray-300 mb-1">Комментарий к заказу *</label>
            <textarea
              v-model="form.comment"
              required
              rows="2"
              class="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:ring-2 focus:ring-[#F47327] focus:border-transparent"
              placeholder="Дополнительная информация"
            ></textarea>
          </div>
        </div>

        <div v-if="error" class="bg-red-500/20 border border-red-400/50 text-red-300 px-4 py-3 rounded-xl">
          {{ error }}
        </div>

        <button
            type="submit"
            :disabled="loading"
            class="w-full bg-[#3A3331] text-white py-3 rounded-2xl border-2 border-[#F47327] hover:bg-[#F47327]/50 active:bg-[#F47327]/50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors font-semibold"
          >
            {{ loading ? 'Оформление...' : 'Оформить заказ' }}
          </button>
      </form>

      <!-- Order Summary -->
      <div class="lg:col-span-1">
        <div class="rounded-2xl shadow-md p-6 sticky top-20" style="background-color: #3A3331;">
          <h2 class="text-xl font-semibold mb-4 text-white">Ваш заказ</h2>
          <div class="space-y-3 mb-4">
            <div
              v-for="item in cartStore.items"
              :key="item.product.id"
              class="flex gap-3 items-center"
            >
              <router-link :to="`/products/${item.product.id}`" class="flex-shrink-0 w-14 h-14 rounded overflow-hidden">
                <img
                  v-if="getImageUrl(item.product)"
                  :src="getImageUrl(item.product)"
                  :alt="item.product.name"
                  class="w-full h-full object-cover"
                />
                <div
                  v-else
                  class="w-full h-full bg-[#26211E] flex items-center justify-center"
                >
                  <svg class="w-7 h-7 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
              </router-link>
              <div class="flex-1 min-w-0 flex flex-col gap-0.5">
                <span class="text-white text-sm font-medium truncate">
                  {{ item.product.name }}
                </span>
                <span class="text-gray-400 text-xs">
                  {{ item.quantity }} шт. · {{ formatPrice(item.product.price * item.quantity) }}
                </span>
              </div>
            </div>
          </div>
          <div class="border-t border-white/20 pt-4">
            <div class="flex justify-between text-xl font-bold text-white">
              <span>Итого:</span>
              <span>{{ formatPrice(cartStore.totalPrice) }}</span>
            </div>
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
import { fileApi } from '../services/api'

const router = useRouter()
const cartStore = useCartStore()
const authStore = useAuthStore()

const loading = ref(false)
const error = ref(null)
const deliveryMethod = ref('delivery')
const paymentMethod = ref('cash')

const paymentMethods = [
  { id: 'cash', name: 'При получении', desc: 'наличными или картой' },
]

const form = ref({
  fullName: authStore.user?.fullName || '',
  email: authStore.user?.email || '',
  phone: '',
  address: '',
  pickupAddress: '',
  comment: '',
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

const validateForm = () => {
  const f = form.value
  if (!f.fullName?.trim()) {
    error.value = 'Заполните ФИО'
    return false
  }
  if (!f.email?.trim()) {
    error.value = 'Заполните Email'
    return false
  }
  if (!f.phone?.trim()) {
    error.value = 'Заполните телефон'
    return false
  }
  if (deliveryMethod.value === 'delivery' && !f.address?.trim()) {
    error.value = 'Заполните адрес доставки'
    return false
  }
  if (deliveryMethod.value === 'pickup' && !f.pickupAddress?.trim()) {
    error.value = 'Заполните адрес пункта самовывоза'
    return false
  }
  if (!f.comment?.trim()) {
    error.value = 'Заполните комментарий к заказу'
    return false
  }
  error.value = null
  return true
}

const submitOrder = async () => {
  if (!validateForm()) return

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
