<template>
  <div class="container mx-auto px-4 py-8 bg-[#3A3331]">
    <div v-if="productsStore.loading" class="text-center py-12">
      <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
    </div>

    <div v-else-if="productsStore.error" class="text-center py-12 text-red-400">
      Ошибка загрузки товара: {{ productsStore.error }}
    </div>

    <div v-else-if="product" class="grid grid-cols-1 lg:grid-cols-2 gap-8">
      <!-- Images -->
      <div>
        <div class="bg-[#26211E] rounded-lg overflow-hidden mb-4 flex items-center justify-center h-96">
          <img
            v-if="mainImageUrl"
            :src="mainImageUrl"
            :alt="product.name"
            class="w-[97%] h-[97%] object-contain"
          />
          <div
            v-else
            class="w-full h-96 bg-[#26211E] flex items-center justify-center"
          >
            <svg
              class="w-24 h-24 text-gray-300"
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
        </div>
        <div v-if="product.images && product.images.length > 1" class="grid grid-cols-4 gap-2">
          <button
            v-for="(image, index) in product.images"
            :key="index"
            @click="selectedImageIndex = index"
            class="bg-[#26211E] rounded overflow-hidden hover:opacity-75 transition-opacity"
            :class="{ 'ring-2 ring-primary-600': selectedImageIndex === index }"
          >
            <img
              :src="getImageUrl(image.imageUrl || image.url)"
              :alt="`${product.name} - изображение ${index + 1}`"
              class="w-full h-20 object-cover"
            />
          </button>
        </div>
      </div>

      <!-- Product Info -->
      <div>
        <h1 class="text-3xl font-bold text-white mb-4">{{ product.name }}</h1>
        
        <div class="flex items-center space-x-4 mb-4">
          <span class="text-3xl font-bold text-primary-600">
            {{ formatPrice(product.price) }}
          </span>
          <span
            v-if="product.oldPrice && product.oldPrice > product.price"
            class="text-xl text-gray-500 line-through"
          >
            {{ formatPrice(product.oldPrice) }}
          </span>
        </div>

        <div class="mb-6">
          <p v-if="product.description" class="text-gray-200 mb-4">
            {{ product.description }}
          </p>
          
          <div v-if="product.benefits && product.benefits.length > 0" class="mb-4">
            <h3 class="font-semibold mb-2">Преимущества:</h3>
            <ul class="list-disc list-inside space-y-1 text-gray-200">
              <li v-for="benefit in product.benefits" :key="benefit">
                {{ benefit }}
              </li>
            </ul>
          </div>

          <div v-if="product.characteristics && product.characteristics.length > 0" class="mb-4">
            <h3 class="font-semibold mb-2">Характеристики:</h3>
            <dl class="grid grid-cols-2 gap-2 text-sm">
              <template v-for="char in product.characteristics" :key="char.id">
                <dt class="font-medium text-gray-200">{{ char.name }}:</dt>
                <dd class="text-gray-300">{{ char.value }}</dd>
              </template>
            </dl>
          </div>

          <div class="mb-4">
            <p class="text-sm text-gray-300">
              <span class="font-semibold">Артикул:</span> {{ product.sku || 'Не указан' }}
            </p>
            <p v-if="product.brand" class="text-sm text-gray-300">
              <span class="font-semibold">Бренд:</span> {{ product.brand }}
            </p>
            <p class="text-sm text-gray-300">
              <span class="font-semibold">Наличие:</span>
              <span :class="isAvailable ? 'text-green-600' : 'text-red-600'">
                {{ isAvailable ? 'В наличии' : 'Нет в наличии' }}
              </span>
            </p>
          </div>
        </div>

        <div class="flex items-center space-x-4 mb-6">
          <div class="flex items-center border rounded-md">
            <button
              @click="decreaseQuantity"
              class="px-4 py-2 text-gray-700 hover:bg-gray-100"
            >
              -
            </button>
            <input
              v-model.number="quantity"
              type="number"
              min="1"
              :max="maxQuantity"
              class="w-16 text-center border-x"
            />
            <button
              @click="increaseQuantity"
              class="px-4 py-2 text-gray-700 hover:bg-gray-100"
            >
              +
            </button>
          </div>
        </div>

        <button
          @click="addToCart"
          :disabled="!isAvailable"
          class="w-full bg-primary-600 text-white py-3 rounded-md hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors font-semibold text-lg"
        >
          {{ isAvailable ? 'Добавить в корзину' : 'Нет в наличии' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useProductsStore } from '../stores/products'
import { useCartStore } from '../stores/cart'
import { fileApi } from '../services/api'

const route = useRoute()
const router = useRouter()
const productsStore = useProductsStore()
const cartStore = useCartStore()

const quantity = ref(1)
const selectedImageIndex = ref(0)

const product = computed(() => productsStore.selectedProduct)

const getImageUrl = (url) => {
  if (!url) return null
  const result = fileApi.getFileUrl(url)
  console.log('ProductDetail getImageUrl:', url, '→', result)
  return result
}

const mainImageUrl = computed(() => {
  if (!product.value) return null
  
  // Проверяем images массив (в ProductImageResponse поле называется imageUrl, не url!)
  if (product.value.images && product.value.images.length > 0) {
    const img = product.value.images[selectedImageIndex.value]
    // Проверяем разные возможные поля (imageUrl - правильное поле из ProductImageResponse)
    const imgUrl = img.imageUrl || img.url || (typeof img === 'string' ? img : null)
    if (imgUrl) {
      return getImageUrl(imgUrl)
    }
  }
  
  // Проверяем imageUrl
  if (product.value.imageUrl) {
    return getImageUrl(product.value.imageUrl)
  }
  
  console.log('ProductDetail: No image found for product', product.value)
  return null
})

const isAvailable = computed(() => {
  return product.value?.isActive && 
         (product.value?.stockQuantity === null || product.value?.stockQuantity > 0)
})

const maxQuantity = computed(() => {
  if (!product.value?.stockQuantity) return 999
  return product.value.stockQuantity
})

const formatPrice = (price) => {
  if (!price) return '0 ₽'
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB',
    minimumFractionDigits: 0,
  }).format(price)
}

const increaseQuantity = () => {
  if (quantity.value < maxQuantity.value) {
    quantity.value++
  }
}

const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

const addToCart = () => {
  if (isAvailable.value && product.value) {
    cartStore.addToCart(product.value, quantity.value)
    router.push('/cart')
  }
}

onMounted(async () => {
  const productId = Number(route.params.id)
  try {
    await productsStore.fetchProductById(productId)
  } catch (error) {
    router.push('/products')
  }
})
</script>
