<template>
  <div class="product-tile rounded-lg overflow-hidden relative">
    <router-link :to="`/products/${product.id}`">
      <div class="product-card-image-container w-full h-[410px] relative flex items-center justify-center overflow-hidden group rounded-xl" style="background-color: var(--product-tile-background);">
        <img
          v-if="imageUrl"
          :src="imageUrl"
          :alt="product.name"
          class="w-[90%] h-[90%] object-contain transition-transform duration-300 ease-in-out group-hover:scale-[1.20]"
        />
        <div
          v-else
          class="w-full h-full flex items-center justify-center"
          style="background-color: var(--product-tile-background);"
        >
          <svg
            class="w-16 h-16 text-gray-300"
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
        
        <!-- Кнопка "В корзину" в правом верхнем углу -->
        <button
          @click.stop="addToCart"
          :disabled="!isAvailable"
          class="absolute top-2 right-2 bg-[#F37021] text-white px-3 py-1.5 rounded-md hover:bg-[#E0651D] disabled:bg-gray-500 disabled:cursor-not-allowed transition-colors text-sm font-semibold z-10"
        >
          {{ isAvailable ? 'В корзину' : 'Нет' }}
        </button>
        
        <!-- Бейдж "Хит продаж" -->
        <div
          v-if="product.isFeatured"
          class="absolute top-2 left-2 bg-primary-600 text-white px-2 py-1 rounded text-xs font-semibold z-10"
        >
          Хит продаж
        </div>
      </div>
    </router-link>
    
    <!-- Текст внизу картинки -->
    <div class="px-0 py-0 mt-4" style="background-color: var(--siteBg);">
      <router-link :to="`/products/${product.id}`">
        <h3 class="text-sm mb-0 hover:opacity-90 transition-opacity pl-0 product-name" style="color: var(--product-tile-title-color);">
          {{ product.name }}
        </h3>
      </router-link>
      
      <div class="flex items-center justify-between px-0">
        <div class="flex items-center space-x-2">
          <span class="text-sm product-price" style="color: var(--product-tile-price-color);">
            {{ formatPrice(product.price) }}
          </span>
          <span
            v-if="product.oldPrice && product.oldPrice > product.price"
            class="text-sm line-through opacity-70 product-price"
            style="color: var(--product-tile-price-color);"
          >
            {{ formatPrice(product.oldPrice) }}
          </span>
        </div>
        <span
          v-if="product.stockQuantity !== null && product.stockQuantity <= 10"
          class="text-xs font-semibold"
          style="color: var(--product-tile-title-color);"
        >
          Осталось: {{ product.stockQuantity }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useCartStore } from '../stores/cart'
import { fileApi } from '../services/api'

const props = defineProps({
  product: {
    type: Object,
    required: true,
  },
})

const cartStore = useCartStore()

const imageUrl = computed(() => {
  // Проверяем imageUrl
  if (props.product.imageUrl) {
    const url = fileApi.getFileUrl(props.product.imageUrl)
    console.log('ProductCard imageUrl:', props.product.imageUrl, '→', url)
    return url
  }
  
  // Проверяем images массив (в ProductImageResponse поле называется imageUrl, не url!)
  if (props.product.images && props.product.images.length > 0) {
    const img = props.product.images[0]
    // Проверяем разные возможные поля (imageUrl - правильное поле из ProductImageResponse)
    const imgUrl = img.imageUrl || img.url || (typeof img === 'string' ? img : null)
    if (imgUrl) {
      const url = fileApi.getFileUrl(imgUrl)
      console.log('ProductCard images[0]:', img, '→', imgUrl, '→', url)
      return url
    }
  }
  
  console.log('ProductCard: No image found for product', props.product.id, props.product)
  return null
})

const isAvailable = computed(() => {
  return props.product.isActive && 
         (props.product.stockQuantity === null || props.product.stockQuantity > 0)
})

const formatPrice = (price) => {
  if (!price) return '0 ₽'
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB',
    minimumFractionDigits: 0,
  }).format(price)
}

const addToCart = () => {
  if (isAvailable.value) {
    cartStore.addToCart(props.product, 1)
  }
}
</script>

<style scoped>
.product-name,
.product-price {
  font-family: "helvetica", sans-serif;
  font-weight: 400;
}

@media (max-width: 640px) {
  .product-card-image-container {
    height: 266px !important; /* 410px - 15% - 15% - 10% = 266px */
  }
}
</style>
