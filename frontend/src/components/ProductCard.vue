<template>
  <div class="product-tile rounded-lg overflow-hidden relative">
    <router-link :to="`/products/${product.id}`">
      <div
        class="product-card-image-container protect-image w-full h-[410px] relative flex items-center justify-center overflow-hidden group rounded-xl"
        style="background-color: var(--product-tile-background);"
        @contextmenu.prevent
      >
        <img
          v-if="imageUrl"
          :src="imageUrl"
          :alt="product.name"
          class="w-full h-full md:w-[99%] md:h-[99%] object-contain transition-transform duration-300 ease-in-out group-hover:scale-[1.20]"
          draggable="false"
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
        
        <!-- Бейдж "Хит продаж" -->
        <div
          v-if="product.isFeatured"
          class="absolute top-2 left-2 bg-primary-600 text-white px-2 py-1 rounded text-xs font-semibold z-10"
        >
          Хит продаж
        </div>
      </div>
    </router-link>
    
    <!-- Кнопка "В корзину" или иконка корзины (вне router-link, чтобы не открывать товар) -->
    <div class="absolute top-2 right-2 z-20">
      <!-- Иконка корзины, если товар уже в корзине -->
      <button
        v-if="isInCart"
        @click.stop="goToCart"
        class="bg-[#F37021] text-white p-2.5 rounded-md hover:bg-[#E0651D] transition-colors z-20 flex items-center justify-center"
        title="Товар в корзине. Перейти в корзину"
      >
        <svg
          class="w-5 h-5"
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
      </button>
      
      <!-- Кнопка "В корзину", если товара нет в корзине -->
      <button
        v-else
        @click.stop="addToCart"
        :disabled="!isAvailable"
        class="bg-[#F37021] text-white px-3 py-1.5 rounded-md hover:bg-[#E0651D] disabled:bg-gray-500 disabled:cursor-not-allowed transition-colors text-sm font-semibold z-20"
      >
        {{ isAvailable ? 'В корзину' : 'Нет' }}
      </button>
    </div>
    
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
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { fileApi } from '../services/api'

const props = defineProps({
  product: {
    type: Object,
    required: true,
  },
})

const router = useRouter()
const cartStore = useCartStore()

const imageUrl = computed(() => {
  // Проверяем imageUrl
  if (props.product.imageUrl) {
    return fileApi.getFileUrl(props.product.imageUrl)
  }
  
  // Проверяем images массив (в ProductImageResponse поле называется imageUrl, не url!)
  if (props.product.images && props.product.images.length > 0) {
    const img = props.product.images[0]
    // Проверяем разные возможные поля (imageUrl - правильное поле из ProductImageResponse)
    const imgUrl = img.imageUrl || img.url || (typeof img === 'string' ? img : null)
    if (imgUrl) return fileApi.getFileUrl(imgUrl)
  }
  return null
})

const isAvailable = computed(() => {
  return props.product.isActive && 
         (props.product.stockQuantity === null || props.product.stockQuantity > 0)
})

const isInCart = computed(() => {
  return cartStore.items.some(item => item.product.id === props.product.id)
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

const goToCart = () => {
  router.push('/cart')
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
