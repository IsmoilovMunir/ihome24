<template>
  <div class="container mx-auto px-4 py-8 bg-[#3A3331] product-detail-page">
    <div v-if="productsStore.loading" class="text-center py-12">
      <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
    </div>

    <div v-else-if="productsStore.error" class="text-center py-12 text-red-400">
      Ошибка загрузки товара: {{ productsStore.error }}
    </div>

    <div v-else-if="product" class="grid grid-cols-1 lg:grid-cols-2 gap-8">
      <!-- Images -->
      <div>
        <div
          class="bg-[#26211E] rounded-lg overflow-hidden mb-4 flex items-center justify-center h-96 relative cursor-pointer"
          @click="openFullscreenGallery"
        >
          <img
            v-if="mainImageUrl"
            :src="mainImageUrl"
            :alt="product.name"
            class="w-[97%] h-[97%] object-contain pointer-events-none"
          />
          <div
            v-else
            class="w-full h-96 bg-[#26211E] flex items-center justify-center pointer-events-none"
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
          <!-- Стрелки переключения фото (только если больше одного изображения) -->
          <template v-if="product.images && product.images.length > 1">
            <button
              type="button"
              @click.stop="goToPrevImage"
              class="absolute left-2 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-black/50 text-white flex items-center justify-center transition-colors z-10 hover:text-[#C56129] active:text-[#C56129]"
              aria-label="Предыдущее фото"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            <button
              type="button"
              @click.stop="goToNextImage"
              class="absolute right-2 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-black/50 text-white flex items-center justify-center transition-colors z-10 hover:text-[#C56129] active:text-[#C56129]"
              aria-label="Следующее фото"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </button>
          </template>
        </div>
        <div v-if="product.images && product.images.length > 1" class="grid grid-cols-4 gap-2">
          <button
            v-for="(image, index) in product.images"
            :key="index"
            @click="selectedImageIndex = index"
            class="bg-[#26211E] rounded overflow-hidden hover:opacity-75 transition-opacity aspect-square w-full flex items-center justify-center"
            :class="{ 'ring-2 ring-[#F37021]': selectedImageIndex === index }"
          >
            <img
              :src="getImageUrl(image.imageUrl || image.url)"
              :alt="`${product.name} - изображение ${index + 1}`"
              class="w-[90%] h-[90%] object-cover"
            />
          </button>
        </div>
      </div>

      <!-- Product Info -->
      <div>
        <h1 class="text-3xl text-white mb-4 product-detail-title">{{ product.name }}</h1>
        
        <div class="flex items-center space-x-4 mb-4">
          <span
            class="text-2xl font-normal"
            style="color: #9E9390; font-family: helvetica, sans-serif;"
          >
            {{ formatPrice(totalPrice) }}
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
              <span class="font-semibold">Наличие: </span>
              <span style="color: #C56129;"> {{ isAvailable ? 'В наличии' : 'Нет в наличии' }}</span>
            </p>
          </div>
        </div>

        <div class="flex items-center space-x-4 mb-6">
          <div class="flex items-center gap-1 rounded border px-1" style="border-color: #9D9390;">
            <button
              type="button"
              @click="decreaseQuantity"
              class="w-9 h-9 flex items-center justify-center rounded transition-opacity hover:opacity-80"
              style="color: #9D9390;"
              aria-label="Уменьшить"
            >
              −
            </button>
            <input
              v-model.number="quantity"
              type="number"
              min="1"
              :max="maxQuantity"
              class="w-12 text-center bg-transparent border-none text-lg focus:outline-none focus:ring-0"
              style="color: #9D9390; font-family: helvetica, sans-serif;"
            />
            <button
              type="button"
              @click="increaseQuantity"
              class="w-9 h-9 flex items-center justify-center rounded transition-opacity hover:opacity-80"
              style="color: #9D9390;"
              aria-label="Увеличить"
            >
              +
            </button>
          </div>
        </div>

        <button
          @click="addToCart"
          :disabled="!isAvailable"
          class="w-full py-3 rounded-md text-white font-semibold text-lg border-2 transition-colors duration-1000 bg-[#3A3331] border-[#C56129] hover:bg-[#C56129] disabled:bg-gray-300 disabled:border-gray-400 disabled:cursor-not-allowed"
        >
          {{ isAvailable ? 'Добавить в корзину' : 'Нет в наличии' }}
        </button>
      </div>
    </div>

    <!-- Полноэкранный просмотр фото -->
    <Teleport to="body">
      <div
        v-if="showFullscreenGallery && product"
        class="fixed inset-0 z-[11000] flex flex-col"
        style="background-color: #3A3331;"
        @click.self="closeFullscreenGallery"
        role="dialog"
        aria-modal="true"
        aria-label="Просмотр изображения"
      >
        <button
          type="button"
          @click="closeFullscreenGallery"
          class="absolute top-6 right-6 z-20 w-12 h-12 rounded-full text-white flex items-center justify-center transition-colors border-2 border-white/50 hover:text-[#C56129] active:text-[#C56129] hover:border-[#C56129] active:border-[#C56129]"
          style="background-color: #26211E;"
          aria-label="Закрыть (выход из полноэкранного режима)"
          title="Закрыть"
        >
          <span class="text-2xl font-light leading-none" aria-hidden="true">×</span>
        </button>

        <div
          class="flex-1 flex items-center justify-center min-h-0 p-4 rounded-lg m-4 overflow-hidden cursor-grab active:cursor-grabbing"
          style="background-color: #26211E;"
          @wheel.prevent="onFullscreenWheel"
          @mousedown="onFullscreenPanStart"
          @touchstart.prevent="onFullscreenPanStart"
        >
          <div
            v-if="mainImageUrl"
            class="w-full h-full flex items-center justify-center origin-center select-none touch-none"
            :style="{
              transform: `translate(${fullscreenPan.x}px, ${fullscreenPan.y}px) scale(${fullscreenZoom})`,
              transition: isPanning ? 'none' : 'transform 0.15s ease-out',
            }"
          >
            <img
              :src="mainImageUrl"
              :alt="product.name"
              class="max-w-full max-h-full object-contain select-none pointer-events-none"
              draggable="false"
            />
          </div>
        </div>

        <div class="absolute top-6 right-20 z-20 flex items-center gap-1 rounded border border-white/30 p-1" style="background-color: #26211E;">
          <button
            type="button"
            @click.stop="fullscreenZoomOut"
            class="w-9 h-9 rounded text-white flex items-center justify-center transition-colors hover:text-[#C56129] disabled:opacity-50"
            :disabled="fullscreenZoom <= ZOOM_MIN"
            aria-label="Уменьшить"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4" />
            </svg>
          </button>
          <span class="text-white text-sm min-w-[2.5rem] text-center" style="color: #9E9390;">{{ Math.round(fullscreenZoom * 100) }}%</span>
          <button
            type="button"
            @click.stop="fullscreenZoomIn"
            class="w-9 h-9 rounded text-white flex items-center justify-center transition-colors hover:text-[#C56129] disabled:opacity-50"
            :disabled="fullscreenZoom >= ZOOM_MAX"
            aria-label="Увеличить"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
          </button>
        </div>

        <template v-if="product.images && product.images.length > 1">
          <div class="absolute left-2 top-1/2 -translate-y-1/2 z-20">
            <button
              type="button"
              @click.stop="goToPrevImage"
              class="w-10 h-10 rounded-full text-white flex items-center justify-center transition-colors border border-white/30 hover:text-[#C56129] hover:border-[#C56129] active:text-[#C56129] active:border-[#C56129]"
              style="background-color: #26211E;"
              aria-label="Предыдущее фото"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
              </svg>
            </button>
          </div>
          <div class="absolute right-2 top-1/2 -translate-y-1/2 z-20">
            <button
              type="button"
              @click.stop="goToNextImage"
              class="w-10 h-10 rounded-full text-white flex items-center justify-center transition-colors border border-white/30 hover:text-[#C56129] hover:border-[#C56129] active:text-[#C56129] active:border-[#C56129]"
              style="background-color: #26211E;"
              aria-label="Следующее фото"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </button>
          </div>

          <div class="flex justify-center gap-2 pb-6 pt-4 px-4 overflow-x-auto rounded-t-lg" style="background-color: #26211E;">
            <button
              v-for="(image, index) in product.images"
              :key="index"
              type="button"
              @click.stop="selectedImageIndex = index"
              class="flex-shrink-0 w-16 h-16 rounded overflow-hidden border-2 transition-colors hover:opacity-90 flex items-center justify-center"
              :class="selectedImageIndex === index ? 'border-[#F37021]' : 'border-white/20 opacity-80'"
              style="background-color: #26211E;"
            >
              <img
                :src="getImageUrl(image.imageUrl || image.url)"
                :alt="`${product.name} - изображение ${index + 1}`"
                class="w-[90%] h-[90%] object-cover"
              />
            </button>
          </div>
        </template>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
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
const showFullscreenGallery = ref(false)
const fullscreenZoom = ref(1)
const fullscreenPan = ref({ x: 0, y: 0 })
const isPanning = ref(false)
let panStart = { clientX: 0, clientY: 0, x: 0, y: 0 }

const ZOOM_MIN = 0.5
const ZOOM_MAX = 4
const ZOOM_STEP = 0.25

const product = computed(() => productsStore.selectedProduct)

const openFullscreenGallery = () => {
  fullscreenZoom.value = 1
  fullscreenPan.value = { x: 0, y: 0 }
  showFullscreenGallery.value = true
}

const closeFullscreenGallery = () => {
  showFullscreenGallery.value = false
  fullscreenZoom.value = 1
  fullscreenPan.value = { x: 0, y: 0 }
}

const onFullscreenPanStart = (e) => {
  const clientX = e.touches ? e.touches[0].clientX : e.clientX
  const clientY = e.touches ? e.touches[0].clientY : e.clientY
  isPanning.value = true
  panStart = { clientX, clientY, x: fullscreenPan.value.x, y: fullscreenPan.value.y }
  document.addEventListener('mousemove', onFullscreenPanMove)
  document.addEventListener('mouseup', onFullscreenPanEnd)
  document.addEventListener('touchmove', onFullscreenPanMove, { passive: false })
  document.addEventListener('touchend', onFullscreenPanEnd)
  document.addEventListener('touchcancel', onFullscreenPanEnd)
}

const onFullscreenPanMove = (e) => {
  if (!isPanning.value) return
  const clientX = e.touches?.length ? e.touches[0].clientX : e.clientX
  const clientY = e.touches?.length ? e.touches[0].clientY : e.clientY
  if (e.cancelable) e.preventDefault()
  fullscreenPan.value = {
    x: panStart.x + (clientX - panStart.clientX),
    y: panStart.y + (clientY - panStart.clientY),
  }
}

const onFullscreenPanEnd = () => {
  isPanning.value = false
  document.removeEventListener('mousemove', onFullscreenPanMove)
  document.removeEventListener('mouseup', onFullscreenPanEnd)
  document.removeEventListener('touchmove', onFullscreenPanMove)
  document.removeEventListener('touchend', onFullscreenPanEnd)
  document.removeEventListener('touchcancel', onFullscreenPanEnd)
}

const fullscreenZoomIn = () => {
  fullscreenZoom.value = Math.min(ZOOM_MAX, fullscreenZoom.value + ZOOM_STEP)
}

const fullscreenZoomOut = () => {
  fullscreenZoom.value = Math.max(ZOOM_MIN, fullscreenZoom.value - ZOOM_STEP)
}

const onFullscreenWheel = (e) => {
  e.preventDefault()
  if (e.deltaY < 0) fullscreenZoomIn()
  else fullscreenZoomOut()
}

const onFullscreenKeydown = (e) => {
  if (e.key === 'Escape') {
    closeFullscreenGallery()
    return
  }
  if (e.key === 'ArrowLeft') {
    goToPrevImage()
    return
  }
  if (e.key === 'ArrowRight') {
    goToNextImage()
    return
  }
}

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

const goToPrevImage = () => {
  if (!product.value?.images?.length) return
  const n = product.value.images.length
  selectedImageIndex.value = selectedImageIndex.value === 0 ? n - 1 : selectedImageIndex.value - 1
  if (showFullscreenGallery.value) fullscreenPan.value = { x: 0, y: 0 }
}

const goToNextImage = () => {
  if (!product.value?.images?.length) return
  const n = product.value.images.length
  selectedImageIndex.value = selectedImageIndex.value === n - 1 ? 0 : selectedImageIndex.value + 1
  if (showFullscreenGallery.value) fullscreenPan.value = { x: 0, y: 0 }
}

const isAvailable = computed(() => {
  return product.value?.isActive && 
         (product.value?.stockQuantity === null || product.value?.stockQuantity > 0)
})

const maxQuantity = computed(() => {
  if (!product.value?.stockQuantity) return 999
  return product.value.stockQuantity
})

const totalPrice = computed(() => {
  if (!product.value?.price) return 0
  return (product.value.price * (quantity.value || 1))
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

watch(showFullscreenGallery, (isOpen) => {
  if (isOpen) {
    document.addEventListener('keydown', onFullscreenKeydown)
  } else {
    document.removeEventListener('keydown', onFullscreenKeydown)
  }
})

onMounted(async () => {
  window.scrollTo(0, 0)
  document.documentElement.scrollTop = 0
  document.body.scrollTop = 0
  const productId = Number(route.params.id)
  try {
    await productsStore.fetchProductById(productId)
  } catch (error) {
    router.push('/products')
  }
})

onUnmounted(() => {
  document.removeEventListener('keydown', onFullscreenKeydown)
  document.removeEventListener('mousemove', onFullscreenPanMove)
  document.removeEventListener('mouseup', onFullscreenPanEnd)
  document.removeEventListener('touchmove', onFullscreenPanMove)
  document.removeEventListener('touchend', onFullscreenPanEnd)
  document.removeEventListener('touchcancel', onFullscreenPanEnd)
})
</script>

<style scoped>
/* Все шрифты на странице товара явно прописаны */
.product-detail-page,
.product-detail-page h1,
.product-detail-page h2,
.product-detail-page h3,
.product-detail-page p,
.product-detail-page span,
.product-detail-page div,
.product-detail-page ul,
.product-detail-page li,
.product-detail-page dt,
.product-detail-page dd,
.product-detail-page label,
.product-detail-page button,
.product-detail-page a,
.product-detail-page input {
  font-family: helvetica, sans-serif;
  font-weight: 400;
  text-transform: uppercase;
}

.product-detail-title {
  font-family: helvetica, sans-serif;
  font-weight: 400;
}
</style>
