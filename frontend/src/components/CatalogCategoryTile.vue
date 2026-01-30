<template>
  <router-link
    :to="categoryLink"
    :data-test="'catalog-category-tile'"
    :class="['catalog-category-tile', { 'catalog-category-tile-special': isSpecial }]"
  >
    <!-- Image Container -->
    <div :class="['catalog-category-tile-image-container', { 'catalog-category-tile-image-multiple': hasMultipleImages }]">
      <!-- Single Image -->
      <img
        v-if="!hasMultipleImages && imageUrl"
        :src="imageUrl"
        :alt="category.name"
        data-test="catalog-category-tile-img"
        class="catalog-category-tile-image"
      />
      
      <!-- Multiple Images (for animation/carousel) -->
      <template v-if="hasMultipleImages">
        <img
          v-for="(img, index) in imageUrls"
          :key="index"
          :src="img"
          :alt="category.name"
          data-test="catalog-category-tile-img"
          :class="['catalog-category-tile-image', 'catalog-category-tile-image-multi']"
        />
      </template>
    </div>
    
    <!-- Title Container -->
    <div :class="['catalog-category-tile-title-container', { 'catalog-category-tile-title-special': isSpecial }]">
      <!-- Text Title -->
      <div
        v-if="!isSpecial && category.name"
        data-test="catalog-category-tile-title"
        class="catalog-category-tile-title"
      >
        {{ category.name }}
      </div>
      
      <!-- SVG Logo (for special categories) -->
      <img
        v-if="isSpecial && logoUrl"
        :src="logoUrl"
        :alt="category.name"
        data-test="catalog-category-tile-title-placeholder"
        class="catalog-category-tile-logo"
      />
    </div>
  </router-link>
</template>

<script setup>
import { computed } from 'vue'
import { fileApi } from '../services/api'

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

const imageUrl = computed(() => {
  if (props.images && props.images.length > 0) {
    return props.images[0]
  }
  if (props.category.imageUrl) {
    return fileApi.getFileUrl(props.category.imageUrl)
  }
  return null
})

const imageUrls = computed(() => {
  if (props.images && props.images.length > 0) {
    return props.images.map(img => {
      if (typeof img === 'string' && (img.startsWith('http://') || img.startsWith('https://'))) {
        return img
      }
      return fileApi.getFileUrl(img)
    })
  }
  return []
})

const hasMultipleImages = computed(() => {
  return props.isSpecial && props.images && props.images.length > 1
})

const categoryLink = computed(() => {
  if (props.category.id) {
    return `/products?category=${props.category.id}`
  }
  return '/products'
})
</script>

<style scoped>
.catalog-category-tile {
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: transform 0.3s ease, opacity 0.3s ease;
  text-decoration: none;
  color: inherit;
}

.catalog-category-tile:hover {
  transform: translateY(-4px);
  opacity: 0.9;
}

.catalog-category-tile-image-container {
  position: relative;
  width: 100%;
  overflow: hidden;
  background: #2E2826;
  border-radius: 8px;
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.catalog-category-tile-image-multiple {
  display: flex;
  gap: 0;
  position: relative;
}

.catalog-category-tile-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: opacity 0.3s ease;
}

.catalog-category-tile-image-multi {
  position: absolute;
  top: 0;
  left: 0;
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

.catalog-category-tile-title-container {
  margin-top: 12px;
  text-align: center;
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

.catalog-category-tile-title-special {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 16px;
}

.catalog-category-tile-logo {
  max-width: 120px;
  height: auto;
  object-fit: contain;
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
  
  .catalog-category-tile-logo {
    max-width: 110px;
  }
  
  .catalog-category-tile-title-container {
    margin-top: 10px;
  }
}

/* Мобильные устройства */
@media (max-width: 768px) {
  .catalog-category-tile {
    width: 100%;
    -webkit-tap-highlight-color: transparent;
  }
  
  .catalog-category-tile-image-container {
    border-radius: 6px;
    min-height: 0;
  }
  
  .catalog-category-tile-title {
    font-size: 13px;
    letter-spacing: 0.02em;
    word-wrap: break-word;
    overflow-wrap: break-word;
  }
  
  .catalog-category-tile-logo {
    max-width: 90px;
  }
  
  .catalog-category-tile-title-container {
    margin-top: 8px;
    padding: 0 4px;
  }
  
  .catalog-category-tile-title-special {
    margin-top: 12px;
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
  
  .catalog-category-tile-logo {
    max-width: 80px;
  }
  
  .catalog-category-tile-title-container {
    margin-top: 6px;
  }
  
  .catalog-category-tile-title-special {
    margin-top: 10px;
  }
  
  .catalog-category-tile-image-container {
    border-radius: 4px;
  }
}
</style>
