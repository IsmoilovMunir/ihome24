<template>
  <div class="container mx-auto px-4 py-8 pb-40 lg:pb-8 lg:px-0 bg-[#3A3331] product-detail-page">
    <div v-if="productsStore.loading" class="text-center py-12">
      <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
    </div>

    <div v-else-if="productsStore.error" class="text-center py-12 text-red-400">
      Ошибка загрузки товара: {{ productsStore.error }}
    </div>

    <div v-else-if="product" class="grid grid-cols-1 lg:grid-cols-4 gap-8">
      <!-- Images -->
      <div class="lg:col-span-3">
        <div
          ref="mainImageContainerRef"
          class="bg-[#26211E] rounded-lg overflow-hidden mb-4 flex items-center justify-center h-[768px] relative cursor-pointer"
          style="touch-action: pan-y;"
          @click="onMainImageClick"
          @touchstart="onMainImageTouchStart"
          @touchmove="onMainImageTouchMove"
          @touchend="onMainImageTouchEnd"
        >
          <div
            v-if="mainImageUrl"
            class="main-image-zoom-wrapper flex items-center justify-center w-full h-full"
            :style="mainImageTransformStyle"
          >
            <Transition name="photo-fade" mode="out-in">
              <img
                :key="selectedImageIndex"
                :src="mainImageUrl"
                :srcset="mainImageSrcSet || undefined"
                sizes="(max-width: 1024px) 100vw, 70vw"
                :alt="product.name"
                loading="eager"
                fetchpriority="high"
                decoding="async"
                width="1280"
                height="1280"
                class="max-w-[97%] max-h-[97%] w-auto h-auto object-contain select-none"
                draggable="false"
              />
            </Transition>
          </div>
          <div
            v-else
            class="w-full h-[768px] bg-[#26211E] flex items-center justify-center pointer-events-none"
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

          <!-- Пагинация изображений поверх фото (центр снизу) -->
          <div
            v-if="product.images && product.images.length > 1"
            id="swiper-pagination"
            class="swiper-pagination swiper-pagination-clickable swiper-pagination-bullets swiper-pagination-horizontal absolute left-1/2 bottom-4 -translate-x-1/2 z-10 flex items-center justify-center gap-2"
            style="pointer-events: auto;"
          >
            <span
              v-for="(image, index) in product.images"
              :key="index"
              class="swiper-pagination-bullet w-5 h-1.5 rounded-sm bg-white/30 cursor-pointer transition-colors hover:bg-[#C56129]"
              :class="{ 'swiper-pagination-bullet-active': selectedImageIndex === index }"
              :style="selectedImageIndex === index ? { backgroundColor: '#C56129' } : undefined"
              role="button"
              tabindex="0"
              :aria-label="`Изображение ${index + 1}`"
              @click.stop="selectedImageIndex = index"
              @keydown.enter.stop="selectedImageIndex = index"
            />
          </div>
        </div>

        <!-- Мобильный блок цены под фото -->
        <div
          class="lg:hidden mt-4 p-4 rounded-lg flex flex-col gap-4"
          style="background-color: #26211E;"
        >
          <h1 class="text-lg text-white font-bold product-detail-title">{{ displayName }}</h1>
          <div class="flex flex-wrap items-baseline gap-x-3 gap-y-1">
            <span
              class="text-2xl font-normal"
              style="color: #9E9390; font-family: helvetica, sans-serif;"
            >
              {{ formatPrice(unitPriceCurrent) }}/шт
            </span>
            <span
              v-if="product.oldPrice && product.oldPrice > product.price"
              class="text-xl text-gray-500 line-through"
            >
              {{ formatPrice(product.oldPrice) }}
            </span>
          </div>
          <!-- Переключатель вариантов (размеров/атрибутов) для мобильной версии — показываем только если вариантов больше одного -->
          <div
            v-if="variants.length > 1"
            class="mt-2 flex flex-wrap items-center gap-2"
          >
            <span class="text-xs text-gray-400">
              Размер / вариант:
            </span>
            <button
              v-for="(v, idx) in variants"
              :key="v.variantId || v.sku || idx"
              type="button"
              class="px-2 py-1 rounded border text-xs"
              :class="idx === selectedVariantIndex
                ? 'bg-[#C56129] border-[#C56129] text-white'
                : 'border-white/30 text-gray-200 hover:border-[#C56129] hover:text-[#C56129]'"
              @click="selectVariant(idx)"
            >
              {{ v.attributes?.size || v.attributes?.color || v.sku || `Вариант ${idx + 1}` }}
            </button>
          </div>
          <div
            v-if="(priceTiersDisplay || []).length"
            class="mt-3 pt-3 border-t border-white/10"
          >
            <p class="text-xs text-gray-500 mb-1.5" style="font-family: helvetica, sans-serif;">
              Чем больше штук — тем выгоднее цена за штуку:
            </p>
            <ul class="text-sm text-gray-300 space-y-1" style="font-family: helvetica, sans-serif;">
              <li
                v-for="(tier, i) in priceTiersDisplay"
                :key="i"
              >
                {{ tier.range }} — {{ tier.label }}: {{ formatPrice(tier.unitPrice) }}/шт
              </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- Product Info -->
      <div class="lg:col-span-1 lg:flex lg:flex-col lg:justify-center lg:min-h-[768px]">
        <h1 class="hidden lg:block text-3xl text-white mb-4 product-detail-title">{{ displayName }}</h1>
        
        <div class="hidden lg:block mb-4">
          <div class="flex flex-wrap items-baseline gap-x-3 gap-y-1">
            <span
              class="text-2xl font-normal"
              style="color: #9E9390; font-family: helvetica, sans-serif;"
            >
              {{ formatPrice(unitPriceCurrent) }}/шт
            </span>
            <span
              v-if="product.oldPrice && product.oldPrice > product.price"
              class="text-xl text-gray-500 line-through"
            >
              {{ formatPrice(product.oldPrice) }}
            </span>
          </div>
          <!-- Переключатель вариантов (размеров/атрибутов) — показываем только если вариантов больше одного -->
          <div
            v-if="variants.length > 1"
            class="mt-3 flex flex-wrap items-center gap-2"
          >
            <span class="text-xs text-gray-400">
              Размер / вариант:
            </span>
            <button
              v-for="(v, idx) in variants"
              :key="v.variantId || v.sku || idx"
              type="button"
              class="px-2 py-1 rounded border text-xs"
              :class="idx === selectedVariantIndex
                ? 'bg-[#C56129] border-[#C56129] text-white'
                : 'border-white/30 text-gray-200 hover:border-[#C56129] hover:text-[#C56129]'"
              @click="selectVariant(idx)"
            >
              {{ v.attributes?.size || v.attributes?.color || v.sku || `Вариант ${idx + 1}` }}
            </button>
          </div>
          <div
            v-if="(priceTiersDisplay || []).length"
            class="mt-3 pt-3 border-t border-white/10"
          >
            <p class="text-xs text-gray-500 mb-1.5" style="font-family: helvetica, sans-serif;">
              Чем больше штук — тем выгоднее цена за штуку:
            </p>
            <ul class="text-sm text-gray-300 space-y-1" style="font-family: helvetica, sans-serif;">
              <li
                v-for="(tier, i) in priceTiersDisplay"
                :key="i"
              >
                {{ tier.range }} — {{ tier.label }}: {{ formatPrice(tier.unitPrice) }}/шт
              </li>
            </ul>
          </div>
        </div>

        <!-- Кнопка: одна целая «Добавить» или разделённая «В корзину» + количество -->
        <div
          v-if="isAvailable"
          class="hidden lg:flex rounded-md overflow-hidden border-2"
          style="border-color: #C56129;"
        >
          <button
            v-if="!isInCart"
            type="button"
            @click="addToCart"
            class="flex-1 py-3 text-white font-semibold text-lg transition-colors duration-1000 bg-[#3A3331] hover:bg-[#C56129]"
          >
            Добавить в корзину
          </button>
          <template v-else>
            <button
              type="button"
              @click="router.push('/cart')"
              class="flex-1 py-3 text-white font-semibold text-lg transition-colors bg-[#C56129] hover:bg-[#d97235]"
            >
              В корзину
            </button>
            <div
              class="flex items-center gap-0 border-l px-2"
              style="border-color: #C56129; background-color: #3A3331;"
            >
              <button
                type="button"
                @click="decreaseCartQuantity"
                class="w-10 h-10 flex items-center justify-center text-white hover:bg-[#C56129]/20 transition-colors"
              >
                −
              </button>
              <input
                :value="cartQuantity"
                type="number"
                min="1"
                :max="maxQuantity"
                class="w-10 text-center text-white font-semibold bg-transparent focus:outline-none"
                @input="onCartQuantityInput"
              >
              <button
                type="button"
                @click="increaseCartQuantity"
                class="w-10 h-10 flex items-center justify-center text-white hover:bg-[#C56129]/20 transition-colors"
              >
                +
              </button>
            </div>
          </template>
        </div>
        <button
          v-else
          disabled
          class="hidden lg:block w-full py-3 rounded-md text-white font-semibold text-lg border-2 bg-gray-300 border-gray-400 cursor-not-allowed"
        >
          Нет в наличии
        </button>
      </div>

      <!-- Detailed info (desktop full width) -->
      <div class="lg:col-span-4">
        <div class="mb-6">
          <p v-if="product.description" class="text-gray-200 mb-4 font-medium" style="text-transform: lowercase;">
            {{ product.description }}
          </p>
          
          <div v-if="product.benefits && product.benefits.length > 0" class="mb-4 p-4 rounded-lg" style="background-color: #26211E;">
            <h3 class="mb-3 text-white text-lg" style="font-weight: 700; font-family: Arial, 'Helvetica Neue', sans-serif;">Преимущества</h3>
            <ul class="space-y-3">
              <li
                v-for="benefit in product.benefits"
                :key="benefit"
                class="flex gap-3 text-gray-200 text-sm leading-relaxed"
              >
                <span class="flex-shrink-0 w-5 h-5 rounded-full flex items-center justify-center mt-0.5" style="background-color: #C56129;">
                  <svg class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7" />
                  </svg>
                </span>
                <span>
                  <template v-if="benefit.includes(':')">
                    <span style="font-weight: 700; font-family: Arial, 'Helvetica Neue', sans-serif;">{{ benefit.split(':')[0] }}: </span>
                    <span class="font-normal" style="text-transform: lowercase;">{{ benefit.split(':').slice(1).join(':').trim() }}</span>
                  </template>
                  <template v-else>
                    {{ benefit }}
                  </template>
                </span>
              </li>
            </ul>
          </div>

          <div v-if="product.characteristics && product.characteristics.length > 0" class="mb-4">
            <h3 class="mb-3 text-white text-lg" style="font-weight: 700; font-family: Arial, 'Helvetica Neue', sans-serif;">Характеристики</h3>
            <div class="overflow-x-auto">
              <table class="w-full text-sm">
                <tbody>
                  <tr
                    v-for="(char, index) in product.characteristics"
                    :key="char.id || index"
                    class="border-b border-white/10"
                  >
                    <td class="py-2 pr-4 text-gray-200 lowercase">{{ char.key || '—' }}</td>
                    <td class="py-2 pr-4 text-gray-200 lowercase">{{ char.name || '—' }}</td>
                    <td class="py-2 text-gray-200 lowercase">{{ char.value || '—' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="mb-4 p-4 rounded-lg space-y-3" style="background-color: #26211E;">
            <div class="flex justify-between items-center py-2 border-b border-white/10">
              <span class="text-gray-400 text-sm font-medium capitalize">Артикул</span>
              <div class="flex items-center gap-2">
                <button
                  v-if="selectedVariant?.sku || product.sku"
                  type="button"
                  @click="copyArticle(selectedVariant?.sku || product.sku)"
                  class="p-1.5 rounded hover:bg-white/10 transition-colors text-gray-400 hover:text-[#C56129]"
                  :title="articleCopied ? 'Скопировано!' : 'Копировать артикул'"
                >
                  <svg v-if="!articleCopied" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                  </svg>
                  <svg v-else class="w-4 h-4 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                </button>
                <span class="text-gray-200 text-sm capitalize">
                  {{ selectedVariant?.sku || product.sku || 'Не указан' }}
                </span>
              </div>
            </div>
            <div v-if="product.brand" class="flex justify-between items-center py-2 border-b border-white/10">
              <span class="text-gray-400 text-sm font-medium capitalize">Бренд</span>
              <span class="text-gray-200 text-sm capitalize">{{ product.brand }}</span>
            </div>
            <div v-if="product.quantityPerPackage != null" class="flex justify-between items-center py-2 border-b border-white/10">
              <span class="text-gray-400 text-sm font-medium capitalize">Количество в упаковке</span>
              <span class="text-gray-200 text-sm capitalize">{{ product.quantityPerPackage }} шт.</span>
            </div>
            <div class="flex justify-between items-center py-2">
              <span class="text-gray-400 text-sm font-medium capitalize">Наличие</span>
              <span class="text-sm capitalize" :style="{ color: isAvailable ? '#C56129' : '#9d9390' }">
                {{ isAvailable ? `В наличии: ${maxQuantity} шт.` : 'Нет в наличии' }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Прикреплённая кнопка внизу экрана (мобильный) — над нижним меню -->
    <div
      v-if="product && !productsStore.loading && !productsStore.error"
      class="lg:hidden fixed left-0 right-0 z-[9999] p-4 pb-2"
      style="bottom: 75px; background-color: #3A3331; box-shadow: 0 -4px 12px rgba(0,0,0,0.3);"
    >
      <div
        v-if="isAvailable"
        class="container mx-auto flex rounded-md overflow-hidden border-2"
        style="border-color: #C56129;"
      >
        <button
          v-if="!isInCart"
          type="button"
          @click="addToCart"
          class="flex-1 py-3 text-white font-semibold text-lg transition-colors duration-1000 bg-[#3A3331] hover:bg-[#C56129]"
        >
          Добавить в корзину
        </button>
        <template v-else>
          <button
            type="button"
            @click="router.push('/cart')"
            class="flex-1 py-3 text-white font-semibold text-lg transition-colors bg-[#C56129] hover:bg-[#d97235]"
          >
            В корзину
          </button>
          <div
            class="flex items-center gap-0 border-l px-2"
            style="border-color: #C56129; background-color: #3A3331;"
          >
            <button
              type="button"
              @click="decreaseCartQuantity"
              class="w-10 h-10 flex items-center justify-center text-white hover:bg-[#C56129]/20 transition-colors"
            >
              −
            </button>
            <input
              :value="cartQuantity"
              type="number"
              min="1"
              :max="maxQuantity"
              class="w-10 text-center text-white font-semibold bg-transparent focus:outline-none"
              @input="onCartQuantityInput"
            >
            <button
              type="button"
              @click="increaseCartQuantity"
              class="w-10 h-10 flex items-center justify-center text-white hover:bg-[#C56129]/20 transition-colors"
            >
              +
            </button>
          </div>
        </template>
      </div>
      <button
        v-else
        disabled
        class="w-full py-3 rounded-md text-white font-semibold text-lg border-2 bg-gray-300 border-gray-400 cursor-not-allowed"
      >
        Нет в наличии
      </button>
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
          style="background-color: #26211E; touch-action: none;"
          @wheel.prevent="onFullscreenWheel"
          @mousedown="onFullscreenPanStart"
          @touchstart="onFullscreenTouchStart"
        >
          <div
            v-if="mainImageUrl"
            class="w-full h-full flex items-center justify-center origin-center select-none"
            :style="{
              transform: `translate(${fullscreenPan.x}px, ${fullscreenPan.y}px) scale(${fullscreenZoom})`,
              transition: (isPanning || isPinching) ? 'none' : 'transform 0.15s ease-out',
            }"
          >
            <img
              :src="mainImageUrl"
              :srcset="mainImageSrcSet || undefined"
              sizes="100vw"
              :alt="product.name"
              loading="lazy"
              decoding="async"
              width="1280"
              height="1280"
              class="max-w-full max-h-full object-contain select-none pointer-events-none"
              draggable="false"
            />
          </div>
        </div>

        <div class="hidden sm:flex absolute top-6 right-20 z-20 items-center gap-1 rounded border border-white/30 p-1" style="background-color: #26211E;">
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
                loading="lazy"
                decoding="async"
                :alt="`${product.name} - изображение ${index + 1}`"
                width="64"
                height="64"
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
import { useSettingsStore } from '../stores/settings'
import { fileApi } from '../services/api'
import { parseProductIdFromRoute, productPath } from '../utils/productUrl'

const route = useRoute()
const router = useRouter()
const productsStore = useProductsStore()
const cartStore = useCartStore()
const settingsStore = useSettingsStore()

const quantity = ref(1)
const selectedImageIndex = ref(0)
const mainImageContainerRef = ref(null)
const showFullscreenGallery = ref(false)

// Pinch-to-zoom для основного изображения (мобильный)
const mainImageZoom = ref(1)
const mainImagePan = ref({ x: 0, y: 0 })
let pinchStart = { distance: 0, zoom: 0, centerX: 0, centerY: 0 }
let panStartMain = { x: 0, y: 0, panX: 0, panY: 0 }
let mainImageHadGesture = false
const fullscreenZoom = ref(1)
const fullscreenPan = ref({ x: 0, y: 0 })
const isPanning = ref(false)
const isPinching = ref(false)
let panStart = { clientX: 0, clientY: 0, x: 0, y: 0 }
let fullscreenPinchStart = { distance: 0, zoom: 0 }

const ZOOM_MIN = 0.5
const ZOOM_MAX = 4
const ZOOM_STEP = 0.25

const product = computed(() => productsStore.selectedProduct)

// Варианты товара и выбранный вариант
const variants = computed(() => product.value?.variants ?? [])
const selectedVariantIndex = ref(0)
const selectedVariant = computed(() => variants.value[selectedVariantIndex.value] ?? null)
const mainImageTransformStyle = computed(() => ({
  transform: `translate(${mainImagePan.value.x}px, ${mainImagePan.value.y}px) scale(${mainImageZoom.value})`,
}))

const displayName = computed(() => {
  const base = product.value?.name || ''
  const label = selectedVariant.value?.attributes?.size
    || selectedVariant.value?.attributes?.color
    || ''
  return label ? `${base} ${label}` : base
})

const SEO_SITE_URL = (import.meta.env.VITE_SITE_URL || 'https://ihome24.ru').replace(/\/$/, '')

const upsertMeta = (name, content) => {
  if (!content) return
  let tag = document.head.querySelector(`meta[name="${name}"]`)
  if (!tag) {
    tag = document.createElement('meta')
    tag.setAttribute('name', name)
    document.head.appendChild(tag)
  }
  tag.setAttribute('content', content)
}

const upsertPropertyMeta = (property, content) => {
  if (!content) return
  let tag = document.head.querySelector(`meta[property="${property}"]`)
  if (!tag) {
    tag = document.createElement('meta')
    tag.setAttribute('property', property)
    document.head.appendChild(tag)
  }
  tag.setAttribute('content', content)
}

const setCanonical = (href) => {
  if (!href) return
  let link = document.head.querySelector('link[rel="canonical"]')
  if (!link) {
    link = document.createElement('link')
    link.setAttribute('rel', 'canonical')
    document.head.appendChild(link)
  }
  link.setAttribute('href', href)
}

const upsertJsonLdScript = (id, data) => {
  let script = document.head.querySelector(`script[data-seo-jsonld="${id}"]`)
  if (!script) {
    script = document.createElement('script')
    script.type = 'application/ld+json'
    script.setAttribute('data-seo-jsonld', id)
    document.head.appendChild(script)
  }
  script.textContent = JSON.stringify(data)
}

const updateProductSeo = () => {
  if (!product.value) return
  const title = `${displayName.value || product.value.name} - купить в iHome24`
  const raw = product.value.description || `Цена, характеристики и наличие товара ${displayName.value || product.value.name} в iHome24.`
  const description = String(raw).replace(/\s+/g, ' ').trim().slice(0, 160)
  const canonical = `${SEO_SITE_URL}${route.path}`
  const image = mainImageUrlLarge.value || mainImageUrlFast.value || ''

  document.title = title
  upsertMeta('description', description)
  upsertMeta('robots', 'index, follow')
  upsertMeta('twitter:title', title)
  upsertMeta('twitter:description', description)
  upsertMeta('twitter:card', image ? 'summary_large_image' : 'summary')
  if (image) upsertMeta('twitter:image', image)
  setCanonical(canonical)

  upsertPropertyMeta('og:type', 'product')
  upsertPropertyMeta('og:site_name', 'iHome24')
  upsertPropertyMeta('og:title', title)
  upsertPropertyMeta('og:description', description)
  upsertPropertyMeta('og:url', canonical)
  if (image) upsertPropertyMeta('og:image', image)

  const offerPrice = selectedVariant.value?.price?.base ?? product.value.price
  const inStock = isAvailable.value
  const sku = selectedVariant.value?.sku || product.value.sku || undefined
  const brandName = product.value.brand || 'iHome24'

  upsertJsonLdScript('product', {
    '@context': 'https://schema.org',
    '@type': 'Product',
    name: displayName.value || product.value.name,
    description,
    sku,
    image: image ? [image] : undefined,
    brand: {
      '@type': 'Brand',
      name: brandName,
    },
    offers: {
      '@type': 'Offer',
      url: canonical,
      priceCurrency: 'RUB',
      price: offerPrice != null ? Number(offerPrice) : undefined,
      availability: inStock
        ? 'https://schema.org/InStock'
        : 'https://schema.org/OutOfStock',
      itemCondition: 'https://schema.org/NewCondition',
    },
  })

  upsertJsonLdScript('breadcrumbs', {
    '@context': 'https://schema.org',
    '@type': 'BreadcrumbList',
    itemListElement: [
      {
        '@type': 'ListItem',
        position: 1,
        name: 'Главная',
        item: `${SEO_SITE_URL}/`,
      },
      {
        '@type': 'ListItem',
        position: 2,
        name: 'Каталог',
        item: `${SEO_SITE_URL}/products`,
      },
      {
        '@type': 'ListItem',
        position: 3,
        name: displayName.value || product.value.name,
        item: canonical,
      },
    ],
  })
}

const selectVariant = (index) => {
  selectedVariantIndex.value = index

  const sku = selectedVariant.value?.sku
  const basePath = productPath(product.value)

  if (sku) {
    router.replace({ path: `${basePath}/${sku}`, query: route.query })
  } else {
    router.replace({ path: basePath, query: route.query })
  }
}

const getTouchDistance = (t1, t2) => {
  return Math.hypot(t2.clientX - t1.clientX, t2.clientY - t1.clientY)
}

const getTouchCenter = (t1, t2) => ({
  x: (t1.clientX + t2.clientX) / 2,
  y: (t1.clientY + t2.clientY) / 2,
})

const onMainImageTouchStart = (e) => {
  mainImageHadGesture = false
  if (e.touches.length === 2) {
    pinchStart = {
      distance: getTouchDistance(e.touches[0], e.touches[1]),
      zoom: mainImageZoom.value,
      centerX: getTouchCenter(e.touches[0], e.touches[1]).x,
      centerY: getTouchCenter(e.touches[0], e.touches[1]).y,
    }
  } else if (e.touches.length === 1 && mainImageZoom.value > 1) {
    panStartMain = {
      x: e.touches[0].clientX,
      y: e.touches[0].clientY,
      panX: mainImagePan.value.x,
      panY: mainImagePan.value.y,
    }
  }
}

const onMainImageTouchMove = (e) => {
  if (e.touches.length === 2) {
    if (e.cancelable) e.preventDefault()
    mainImageHadGesture = true
    const distance = getTouchDistance(e.touches[0], e.touches[1])
    const scale = distance / pinchStart.distance
    mainImageZoom.value = Math.min(4, Math.max(0.5, pinchStart.zoom * scale))
  } else if (e.touches.length === 1 && mainImageZoom.value > 1) {
    if (e.cancelable) e.preventDefault()
    mainImageHadGesture = true
    mainImagePan.value = {
      x: panStartMain.panX + (e.touches[0].clientX - panStartMain.x),
      y: panStartMain.panY + (e.touches[0].clientY - panStartMain.y),
    }
  }
}

const onMainImageTouchEnd = () => {
  pinchStart = { distance: 0, zoom: 0, centerX: 0, centerY: 0 }
}

const onMainImageClick = (e) => {
  if (mainImageHadGesture) {
    mainImageHadGesture = false
    return
  }
  openFullscreenGallery()
}

const resetMainImageZoom = () => {
  mainImageZoom.value = 1
  mainImagePan.value = { x: 0, y: 0 }
}

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

const onFullscreenTouchStart = (e) => {
  document.removeEventListener('touchmove', onFullscreenTouchMove)
  document.removeEventListener('touchend', onFullscreenTouchEnd)
  document.removeEventListener('touchcancel', onFullscreenTouchEnd)
  if (e.touches.length === 2) {
    isPinching.value = true
    isPanning.value = false
    fullscreenPinchStart = {
      distance: getTouchDistance(e.touches[0], e.touches[1]),
      zoom: fullscreenZoom.value,
    }
    document.addEventListener('touchmove', onFullscreenTouchMove, { passive: false })
    document.addEventListener('touchend', onFullscreenTouchEnd)
    document.addEventListener('touchcancel', onFullscreenTouchEnd)
  } else if (e.touches.length === 1) {
    isPanning.value = true
    isPinching.value = false
    panStart = {
      clientX: e.touches[0].clientX,
      clientY: e.touches[0].clientY,
      x: fullscreenPan.value.x,
      y: fullscreenPan.value.y,
    }
    document.addEventListener('touchmove', onFullscreenTouchMove, { passive: false })
    document.addEventListener('touchend', onFullscreenTouchEnd)
    document.addEventListener('touchcancel', onFullscreenTouchEnd)
  }
}

const onFullscreenTouchMove = (e) => {
  if (e.touches.length === 2 && isPinching.value) {
    e.preventDefault()
    const distance = getTouchDistance(e.touches[0], e.touches[1])
    const scale = distance / fullscreenPinchStart.distance
    fullscreenZoom.value = Math.min(ZOOM_MAX, Math.max(ZOOM_MIN, fullscreenPinchStart.zoom * scale))
  } else if (e.touches.length === 1 && isPanning.value) {
    e.preventDefault()
    fullscreenPan.value = {
      x: panStart.x + (e.touches[0].clientX - panStart.clientX),
      y: panStart.y + (e.touches[0].clientY - panStart.clientY),
    }
  }
}

const onFullscreenTouchEnd = (e) => {
  if (e.touches.length < 2) isPinching.value = false
  if (e.touches.length < 1) {
    isPanning.value = false
    document.removeEventListener('touchmove', onFullscreenTouchMove)
    document.removeEventListener('touchend', onFullscreenTouchEnd)
    document.removeEventListener('touchcancel', onFullscreenTouchEnd)
  }
}

const onFullscreenPanStart = (e) => {
  if (e.touches) return
  const clientX = e.clientX
  const clientY = e.clientY
  isPanning.value = true
  panStart = { clientX, clientY, x: fullscreenPan.value.x, y: fullscreenPan.value.y }
  document.addEventListener('mousemove', onFullscreenPanMove)
  document.addEventListener('mouseup', onFullscreenPanEnd)
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
  return fileApi.getFileUrl(url)
}

/** Прогрессивная загрузка: сначала medium (быстро), затем large (качественно) */
const displayedMainImageUrl = ref('')
const mainImageUrlFast = computed(() => {
  if (!product.value) return null
  if (product.value.images?.length > 0) {
    const img = product.value.images[selectedImageIndex.value]
    const imgUrl = img?.imageUrl || img?.url || (typeof img === 'string' ? img : null)
    if (imgUrl) return getImageUrl(imgUrl)
  }
  if (product.value.imageUrl) return getImageUrl(product.value.imageUrl)
  return null
})
const mainImageUrlLarge = computed(() => {
  if (!product.value) return null
  if (product.value.images?.length > 0) {
    const img = product.value.images[selectedImageIndex.value]
    const imgUrl = img?.imageUrl || img?.url || (typeof img === 'string' ? img : null)
    if (imgUrl) return fileApi.getImageUrlLarge(imgUrl)
  }
  if (product.value.imageUrl) return fileApi.getImageUrlLarge(product.value.imageUrl)
  return null
})
const mainImageUrlOriginal = computed(() => {
  if (!product.value) return null
  if (product.value.images?.length > 0) {
    const img = product.value.images[selectedImageIndex.value]
    const imgUrl = img?.imageUrl || img?.url || (typeof img === 'string' ? img : null)
    if (imgUrl) return fileApi.getImageUrlOriginal(imgUrl)
  }
  if (product.value.imageUrl) return fileApi.getImageUrlOriginal(product.value.imageUrl)
  return null
})

watch(
  () => [mainImageUrlFast.value, mainImageUrlLarge.value, mainImageUrlOriginal.value, selectedImageIndex.value],
  () => {
    const fast = mainImageUrlFast.value
    const large = mainImageUrlLarge.value
    const original = mainImageUrlOriginal.value
    displayedMainImageUrl.value = fast || ''
    if (!fast) return
    const idx = selectedImageIndex.value
    const tryLarge = large && large !== fast
    const tryOriginal = original && original !== fast && original !== large
    if (tryLarge) {
      const imgLarge = new Image()
      imgLarge.onload = () => {
        if (selectedImageIndex.value !== idx) return
        displayedMainImageUrl.value = large
        if (tryOriginal) {
          const imgOrig = new Image()
          imgOrig.onload = () => {
            if (selectedImageIndex.value === idx) displayedMainImageUrl.value = original
          }
          imgOrig.onerror = () => {}
          imgOrig.src = original
        }
      }
      imgLarge.onerror = () => {
        if (tryOriginal && selectedImageIndex.value === idx) {
          const imgOrig = new Image()
          imgOrig.onload = () => {
            if (selectedImageIndex.value === idx) displayedMainImageUrl.value = original
          }
          imgOrig.onerror = () => {}
          imgOrig.src = original
        }
      }
      imgLarge.src = large
    } else if (tryOriginal) {
      const imgOrig = new Image()
      imgOrig.onload = () => {
        if (selectedImageIndex.value === idx) displayedMainImageUrl.value = original
      }
      imgOrig.onerror = () => {}
      imgOrig.src = original
    }
  },
  { immediate: true }
)

const mainImageUrl = computed(() => displayedMainImageUrl.value || mainImageUrlFast.value)
const mainImageSrcSet = computed(() => {
  if (!product.value) return null
  let filePath = null
  if (product.value.images?.length > 0) {
    const img = product.value.images[selectedImageIndex.value]
    filePath = img?.imageUrl || img?.url || (typeof img === 'string' ? img : null)
  } else if (product.value.imageUrl) {
    filePath = product.value.imageUrl
  }
  if (!filePath) return null
  return fileApi.getImageSrcSet(filePath)
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

const currentVariantKey = computed(() => {
  return (
    selectedVariant.value?.variantId ||
    selectedVariant.value?.sku ||
    selectedVariant.value?.attributes?.size ||
    selectedVariant.value?.attributes?.color ||
    null
  )
})

const isInCart = computed(() => {
  if (!product.value?.id) return false
  return cartStore.items.some(item =>
    item.product.id === product.value.id &&
    (item.variantKey ?? null) === (currentVariantKey.value ?? null),
  )
})

const cartQuantity = computed(() => {
  if (!product.value?.id) return 0
  const item = cartStore.items.find(item =>
    item.product.id === product.value.id &&
    (item.variantKey ?? null) === (currentVariantKey.value ?? null),
  )
  return item?.quantity ?? 0
})

// Анимации изменения количества в корзине на странице товара
const cartQuantityAnimations = {}

const onCartQuantityInput = (event) => {
  if (!product.value?.id) return

  const raw = event.target.value
  let value = parseInt(raw, 10)

  if (Number.isNaN(value)) {
    value = 1
  }
  if (value < 1) value = 1

  const hasStockLimit = typeof maxQuantity.value === 'number'
  const max = hasStockLimit ? maxQuantity.value : 999999

  // Если нет ограничения по складу или значение в пределах склада — просто устанавливаем
  if (!hasStockLimit || value <= max) {
    cartStore.updateQuantity(product.value.id, value, currentVariantKey.value)
    return
  }

  const productId = product.value.id

  // Останавливаем предыдущую анимацию для этого товара, если была
  if (cartQuantityAnimations[productId]) {
    cancelAnimationFrame(cartQuantityAnimations[productId])
    delete cartQuantityAnimations[productId]
  }

  // Анимация от введённого значения до максимума за фиксированное время (1 секунда)
  const start = value
  const end = max
  const duration = 1000 // мс
  const startTime = performance.now()

  const animate = (time) => {
    const elapsed = time - startTime
    const t = Math.min(elapsed / duration, 1) // 0..1

    const current = Math.round(start + (end - start) * t)

    cartStore.updateQuantity(productId, current, currentVariantKey.value)
    event.target.value = String(current)

    if (t >= 1) {
      delete cartQuantityAnimations[productId]
      return
    }

    cartQuantityAnimations[productId] = requestAnimationFrame(animate)
  }

  cartQuantityAnimations[productId] = requestAnimationFrame(animate)
}

const decreaseCartQuantity = () => {
  if (product.value?.id && cartQuantity.value > 1) {
    cartStore.updateQuantity(product.value.id, cartQuantity.value - 1, currentVariantKey.value)
  } else if (product.value?.id && cartQuantity.value === 1) {
    cartStore.removeFromCart(product.value.id, currentVariantKey.value)
  }
}

const increaseCartQuantity = () => {
  if (product.value?.id && cartQuantity.value < maxQuantity.value) {
    cartStore.updateQuantity(product.value.id, cartQuantity.value + 1, currentVariantKey.value)
  }
}

const isAvailable = computed(() => {
  if (!product.value?.isActive) return false

  const variantQty = selectedVariant.value?.stock?.quantity
  if (typeof variantQty === 'number')
    return variantQty > 0

  // Фолбэк на общее количество товара
  return product.value?.stockQuantity === null || product.value?.stockQuantity > 0
})

const maxQuantity = computed(() => {
  // Приоритет: количество на складе у варианта, затем общее количество товара
  const variantQty = selectedVariant.value?.stock?.quantity
  if (typeof variantQty === 'number' && !Number.isNaN(variantQty)) return variantQty

  if (product.value?.stockQuantity != null) return product.value.stockQuantity

  return 999
})

// Базовая цена: сначала из выбранного варианта, если есть, иначе из товара
const basePriceForVariant = computed(() => {
  if (selectedVariant.value?.price?.base != null)
    return Number(selectedVariant.value.price.base)

  return product.value?.price ?? 0
})

/** Цена за 1 шт. по текущей категории в зависимости от выбранного количества (реакция на quantity и на загрузку tiers) */
const unitPriceCurrent = computed(() => {
  if (!basePriceForVariant.value) return 0
  const q = quantity.value || 1
  return settingsStore.unitPriceForQuantity(basePriceForVariant.value, q)
})

const totalPrice = computed(() => {
  const unit = unitPriceCurrent.value
  const q = quantity.value || 1
  return Math.round(unit * q * 100) / 100
})

/** Уровни цен с диапазоном, названием и ценой за штуку — чтобы было понятно покупателю */
const priceTiersDisplay = computed(() => {
  const tiers = settingsStore.priceTiers
  const basePrice = basePriceForVariant.value
  if (!tiers?.length || basePrice == null) return []
  return tiers.map((t) => {
    const min = t.minQty ?? 0
    const max = t.maxQty
    const label = t.label || '—'
    const range = max != null ? `${min}–${max} шт.` : `от ${min} шт.`
    const unitPrice = settingsStore.unitPriceForQuantity(basePrice, min)
    return { range, label, unitPrice }
  })
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
    cartStore.addToCart(product.value, quantity.value, selectedVariant.value)
  }
}

const articleCopied = ref(false)
const copyArticle = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    articleCopied.value = true
    setTimeout(() => { articleCopied.value = false }, 2000)
  } catch (err) {
    console.error('Ошибка копирования:', err)
  }
}

watch(showFullscreenGallery, (isOpen) => {
  if (isOpen) {
    document.addEventListener('keydown', onFullscreenKeydown)
  } else {
    document.removeEventListener('keydown', onFullscreenKeydown)
  }
})

watch(selectedImageIndex, () => {
  resetMainImageZoom()
})

watch(
  () => [product.value?.id, displayName.value, route.path],
  () => {
    updateProductSeo()
  },
)


onMounted(async () => {
  window.scrollTo(0, 0)
  document.documentElement.scrollTop = 0
  document.body.scrollTop = 0
  await settingsStore.fetchPriceTiers()
  const productId = parseProductIdFromRoute(route.params.id)
  if (productId == null) {
    router.push('/products')
    return
  }
  try {
    await productsStore.fetchProductById(productId)
    const p = productsStore.selectedProduct
    if (p) {
      const canonicalBase = productPath(p)
      const currentSku = route.params.variantSku

      // Выставляем вариант по SKU из URL, если он есть
      if (currentSku && Array.isArray(p.variants) && p.variants.length > 0) {
        const idx = p.variants.findIndex(v => v.sku === currentSku)
        if (idx !== -1) {
          selectedVariantIndex.value = idx
        }
      }

      // Формируем канонический путь с учётом slug и SKU
      const sku = selectedVariant.value?.sku
      const canonical = sku ? `${canonicalBase}/${sku}` : canonicalBase

      if (route.path !== canonical) {
        router.replace({ path: canonical, query: route.query })
      }
      updateProductSeo()
    }
  } catch (error) {
    router.push('/products')
  }
})

onUnmounted(() => {
  document.removeEventListener('keydown', onFullscreenKeydown)
  document.removeEventListener('mousemove', onFullscreenPanMove)
  document.removeEventListener('mouseup', onFullscreenPanEnd)
  document.removeEventListener('touchmove', onFullscreenTouchMove)
  document.removeEventListener('touchend', onFullscreenTouchEnd)
  document.removeEventListener('touchcancel', onFullscreenTouchEnd)
})
</script>

<style scoped>
/* Все шрифты на странице товара явно прописаны */
.product-detail-page,
.product-detail-page h1:not(.product-detail-title),
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

.product-detail-page h1.product-detail-title {
  font-family: "bork", sans-serif !important;
  text-transform: none;
  font-size: 1.6em;
}

/* Галерея миниатюр — одна строка, незаметный горизонтальный скролл */
.product-images-gallery {
  scrollbar-width: thin;
  scrollbar-color: rgba(158, 147, 144, 0.4) transparent;
}
.product-images-gallery::-webkit-scrollbar {
  height: 4px;
}
.product-images-gallery::-webkit-scrollbar-track {
  background: transparent;
}
.product-images-gallery::-webkit-scrollbar-thumb {
  background: rgba(158, 147, 144, 0.4);
  border-radius: 2px;
}
.product-images-gallery::-webkit-scrollbar-thumb:hover {
  background: rgba(158, 147, 144, 0.6);
}

/* Убираем стрелки у input[type=number] (количество) */
input[type='number']::-webkit-outer-spin-button,
input[type='number']::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

input[type='number'] {
  -moz-appearance: textfield;
  appearance: textfield;
}

/* Плавная смена основного изображения */
.photo-fade-enter-active,
.photo-fade-leave-active {
  transition: opacity 0.6s ease;
}
.photo-fade-enter-from,
.photo-fade-leave-to {
  opacity: 0;
}
</style>
