<template>
  <router-link
    :to="categoryLink"
    :data-test="'catalog-category-tile'"
    :class="[
      'catalog-category-tile',
      { 'catalog-category-tile-special': isSpecial, 'catalog-category-tile--triple-strip': isMobileStrip },
    ]"
  >
    <!-- Секция превью: картинка + подпись внутри -->
    <div :class="['catalog-category-tile-image-container', { 'catalog-category-tile-image-multiple': showMultiImageLayout }]">
      <div
        :class="[
          'catalog-category-tile-media',
          { 'catalog-category-tile-media--strip': isMobileStrip },
        ]"
      >
        <!-- Спецкатегории: смена кадров -->
        <template v-if="specialCarouselActive">
          <img
            v-for="(img, index) in specialImageUrlsList"
            :key="index"
            :src="img"
            loading="lazy"
            decoding="async"
            :alt="category.name"
            data-test="catalog-category-tile-img"
            :class="['catalog-category-tile-image', 'catalog-category-tile-image-multi']"
          />
        </template>
        <!-- До 3 фото мобильной плитки из API — в ряд -->
        <div
          v-else-if="isMobileStrip"
          class="catalog-category-tile-strip"
        >
          <img
            v-for="(src, index) in mobileStripUrls"
            :key="index"
            :src="src"
            loading="lazy"
            decoding="async"
            :alt="category.name"
            data-test="catalog-category-tile-img"
            class="catalog-category-tile-strip-img"
          />
        </div>
        <img
          v-else-if="singleTileImageSrc"
          :src="singleTileImageSrc"
          :srcset="singleTileImageSrcSet || undefined"
          sizes="(max-width: 768px) 50vw, 33vw"
          :alt="category.name"
          loading="lazy"
          decoding="async"
          data-test="catalog-category-tile-img"
          class="catalog-category-tile-image"
        />
      </div>

      <div
        v-if="!isSpecial && category.name"
        data-test="catalog-category-tile-title"
        class="catalog-category-tile-title catalog-category-tile-title--in-section"
      >
        {{ category.name }}
      </div>

      <img
        v-if="isSpecial && logoUrl"
        :src="logoUrl"
        :alt="category.name"
        data-test="catalog-category-tile-title-placeholder"
        class="catalog-category-tile-logo catalog-category-tile-logo--in-section"
      />
    </div>
  </router-link>
</template>

<script setup>
import { computed } from 'vue'
import { fileApi } from '../services/api'
import { useProductsStore } from '../stores/products'
import { buildCategoryPath } from '../utils/categoryUrl'

const productsStore = useProductsStore()

const props = defineProps({
  category: {
    type: Object,
    required: true,
  },
  // Для специальных категорий с несколькими изображениями
  images: {
    type: Array,
    default: () => null,
  },
  // Для специальных категорий с SVG логотипом
  logoUrl: {
    type: String,
    default: null,
  },
  // Флаг для специального стиля (как у Bork Home, Healthterior и т.д.)
  isSpecial: {
    type: Boolean,
    default: false,
  },
})

const specialImageUrlsList = computed(() => {
  if (!props.images?.length)
    return []
  return props.images.map(img => {
    if (typeof img === 'string' && (img.startsWith('http://') || img.startsWith('https://')))
      return img
    return fileApi.getImageUrlBySize(img, 'medium')
  })
})

/** Спецкатегория: анимация из нескольких картинок из конфига */
const specialCarouselActive = computed(() => props.isSpecial && props.images && props.images.length > 1)

/** Ровно 3 фото mobileImageUrl* — полоска в плитке (как в админке) */
const mobileStripUrls = computed(() => {
  if (props.isSpecial && props.images?.length)
    return []
  const paths = [
    props.category.mobileImageUrl,
    props.category.mobileImageUrl2,
    props.category.mobileImageUrl3,
  ].filter(Boolean)
  if (paths.length === 3)
    return paths.map(p => fileApi.getImageUrlBySize(p, 'small'))
  return []
})

const isMobileStrip = computed(() => mobileStripUrls.value.length === 3)

const singleTileImageSrc = computed(() => {
  if (specialCarouselActive.value)
    return null
  if (isMobileStrip.value)
    return null
  if (props.isSpecial && props.images?.length === 1)
    return specialImageUrlsList.value[0]
  const mobilePaths = [
    props.category.mobileImageUrl,
    props.category.mobileImageUrl2,
    props.category.mobileImageUrl3,
  ].filter(Boolean)
  if (mobilePaths.length >= 1 && mobilePaths.length < 3)
    return fileApi.getImageUrlBySize(mobilePaths[0], 'small')
  if (props.category.imageUrl)
    return fileApi.getImageUrlBySize(props.category.imageUrl, 'small')
  return null
})

const singleTileImageSrcSet = computed(() => {
  if (specialCarouselActive.value || isMobileStrip.value) return null
  const mobilePaths = [
    props.category.mobileImageUrl,
    props.category.mobileImageUrl2,
    props.category.mobileImageUrl3,
  ].filter(Boolean)
  if (mobilePaths.length >= 1 && mobilePaths.length < 3) {
    return fileApi.getImageSrcSet(mobilePaths[0])
  }
  if (props.category.imageUrl) return fileApi.getImageSrcSet(props.category.imageUrl)
  return null
})

const showMultiImageLayout = computed(() => specialCarouselActive.value || isMobileStrip.value)

const categoryLink = computed(() => {
  if (props.category.id)
    return buildCategoryPath(props.category, productsStore.categories)
  return '/products'
})
</script>

<style scoped>
.catalog-category-tile {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  box-sizing: border-box;
  width: 100%;
  min-width: 0;
  max-width: 100%;
  -webkit-text-size-adjust: 100%;
  text-size-adjust: 100%;
  cursor: pointer;
  transition: transform 0.3s ease, opacity 0.3s ease;
  text-decoration: none;
  color: inherit;
}

.catalog-category-tile:hover {
  transform: translateY(-4px);
  opacity: 0.9;
}

/* Три фото: на всю ширину 2 ячеек сетки (как две секции рядом), масштабируется с колонками */
.catalog-category-tile.catalog-category-tile--triple-strip {
  width: 100%;
  max-width: 100%;
  min-width: 0;
}

.catalog-category-tile.catalog-category-tile--triple-strip .catalog-category-tile-image-container {
  width: 100%;
  max-width: 100%;
}

/*
  Шире 320px: высота секции 134px, ширина на всю ячейку; фото и подпись по центру внутри.
  320px и уже: квадрат 134×134.
*/
.catalog-category-tile-image-container {
  position: relative;
  box-sizing: border-box;
  align-self: stretch;
  width: 100%;
  height: 134px;
  min-height: 134px;
  max-height: 134px;
  flex-shrink: 0;
  overflow: hidden;
  background: #2E2826;
  /* Фиксированный радиус (как ~20% от 134px), чтобы при широкой секции углы не «плыли», как у процента */
  border-radius: 27px;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  padding: 8px 6px 10px;
}

@media (max-width: 320px) {
  .catalog-category-tile:not(.catalog-category-tile--triple-strip) {
    align-items: center;
    width: 134px;
    min-width: 134px;
    max-width: 134px;
  }

  .catalog-category-tile:not(.catalog-category-tile--triple-strip) .catalog-category-tile-image-container {
    width: 134px;
    align-self: center;
  }
}

.catalog-category-tile-media {
  position: relative;
  flex: 1 1 auto;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  width: 100%;
  min-height: 0;
}

.catalog-category-tile-media--strip {
  align-items: center;
  justify-content: center;
}

.catalog-category-tile-strip {
  display: flex;
  flex-direction: row;
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  height: 92px;
  margin: -4px auto 0;
  gap: 4px;
  align-items: stretch;
}

.catalog-category-tile-strip-img {
  flex: 1 1 0;
  min-width: 0;
  height: 100%;
  object-fit: contain;
  object-position: center;
  border-radius: 8px;
}

.catalog-category-tile-image-multiple .catalog-category-tile-media {
  min-height: 92px;
}

.catalog-category-tile-image {
  display: block;
  flex: 0 0 auto;
  height: 88px;
  max-height: 88px;
  width: auto;
  max-width: 100%;
  object-fit: contain;
  border-radius: 27px;
  transition: opacity 0.3s ease;
}

.catalog-category-tile-image-multi {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  height: 88px;
  max-height: 88px;
  width: auto;
  max-width: 100%;
  object-fit: contain;
  border-radius: 27px;
  opacity: 0;
  animation: fadeInOut 6s infinite;
}

.catalog-category-tile-image-multi:nth-child(1) {
  animation-delay: 0s;
  opacity: 1;
}

.catalog-category-tile-image-multi:nth-child(2) {
  animation-delay: 2s;
}

.catalog-category-tile-image-multi:nth-child(3) {
  animation-delay: 4s;
}

@keyframes fadeInOut {
  0%, 33% {
    opacity: 1;
  }
  34%, 100% {
    opacity: 0;
  }
}

.catalog-category-tile-title {
  font-size: 16px;
  font-weight: 500;
  color: #fff;
  font-family: "helvetica", sans-serif;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  line-height: 1.4;
}

.catalog-category-tile-title--in-section {
  flex-shrink: 0;
  margin-top: auto;
  padding-top: 0;
  transform: translateY(-10px);
  text-align: center;
  width: 100%;
  font-size: 15px;
  line-height: 1.25;
  letter-spacing: 0.02em;
  text-transform: lowercase;
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2;
}

.catalog-category-tile-title--in-section::first-letter {
  text-transform: uppercase;
}

.catalog-category-tile-logo {
  max-width: 120px;
  height: auto;
  object-fit: contain;
}

.catalog-category-tile-logo--in-section {
  flex-shrink: 0;
  margin-top: auto;
  padding-top: 6px;
  max-width: 100%;
  max-height: 36px;
  width: auto;
  object-fit: contain;
  align-self: center;
}

/* Специальный стиль для категорий типа Bork Home, Healthterior и т.д. */
.catalog-category-tile-special .catalog-category-tile-image-container {
  background: transparent;
}

/* Планшеты */
@media (max-width: 1024px) {
  .catalog-category-tile-title {
    font-size: 15px;
  }

  .catalog-category-tile-title--in-section {
    font-size: 14px;
  }
  
  .catalog-category-tile-logo {
    max-width: 110px;
  }
}

/* Мобильные устройства */
@media (max-width: 768px) {
  .catalog-category-tile {
    -webkit-tap-highlight-color: transparent;
  }
  
  .catalog-category-tile-title {
    font-size: 13px;
    letter-spacing: 0.02em;
    word-wrap: break-word;
    overflow-wrap: break-word;
  }

  .catalog-category-tile-title--in-section {
    font-size: 13px;
    letter-spacing: 0.02em;
    padding: 0 2px;
    transform: translateY(-8px);
  }
  
  .catalog-category-tile-logo {
    max-width: 90px;
  }

  .catalog-category-tile-logo--in-section {
    max-height: 32px;
  }
  
  .catalog-category-tile:hover {
    transform: none; /* Убираем hover эффект на мобильных */
  }
  
  .catalog-category-tile:active {
    opacity: 0.7;
  }
}

/* Маленькие мобильные устройства */
@media (max-width: 480px) {
  .catalog-category-tile-title {
    font-size: 12px;
    letter-spacing: 0.01em;
  }

  .catalog-category-tile-title--in-section {
    font-size: 12px;
  }
  
  .catalog-category-tile-logo {
    max-width: 80px;
  }

  .catalog-category-tile-logo--in-section {
    max-height: 28px;
  }
}
</style>
