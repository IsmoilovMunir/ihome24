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
      <!-- Количество +/− в корзине, если товар уже добавлен -->
      <div
        v-if="isInCart"
        class="bg-[#F37021] rounded-md flex items-center z-20 overflow-hidden"
      >
        <button
          type="button"
          @click.stop="decreaseQuantity"
          class="w-8 h-8 flex items-center justify-center text-white hover:bg-[#E0651D] transition-colors text-lg"
        >
          −
        </button>
        <button
          type="button"
          @click.stop="goToCart"
          class="px-2 min-w-[2rem] h-8 flex items-center justify-center text-white text-sm font-semibold hover:bg-[#E0651D] transition-colors"
        >
          {{ cartQuantity }}
        </button>
        <button
          type="button"
          @click.stop="increaseQuantity"
          class="w-8 h-8 flex items-center justify-center text-white hover:bg-[#E0651D] transition-colors text-lg disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="cartQuantity >= maxQuantity"
        >
          +
        </button>
      </div>
      
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
        <span
          v-if="product.stockQuantity !== null && product.stockQuantity > 0 && product.stockQuantity <= 10"
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

const isInCart = computed(() => {
  return cartStore.items.some(item => item.product.id === props.product.id)
})

const cartQuantity = computed(() => {
  const item = cartStore.items.find(item => item.product.id === props.product.id)
  return item?.quantity ?? 0
})

const maxQuantity = computed(() => {
  if (props.product.stockQuantity == null) return 999
  return props.product.stockQuantity
})

const decreaseQuantity = () => {
  if (cartQuantity.value > 1) {
    cartStore.updateQuantity(props.product.id, cartQuantity.value - 1)
  } else {
    cartStore.removeFromCart(props.product.id)
  }
}

const increaseQuantity = () => {
  if (cartQuantity.value < maxQuantity.value) {
    cartStore.updateQuantity(props.product.id, cartQuantity.value + 1)
  }
}

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
