<template>
  <div class="catalog-category-grid">
    <CatalogCategoryTile
      v-for="category in categories"
      :key="category.id"
      :category="category"
      :images="getCategoryImages(category)"
      :logo-url="getCategoryLogo(category)"
      :is-special="isSpecialCategory(category)"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import CatalogCategoryTile from './CatalogCategoryTile.vue'

const props = defineProps({
  categories: {
    type: Array,
    required: true,
  },
  // Опциональная конфигурация для специальных категорий
  specialCategoriesConfig: {
    type: Object,
    default: () => ({}),
  },
})

// Определяем, является ли категория специальной (с несколькими изображениями и логотипом)
const isSpecialCategory = (category) => {
  return props.specialCategoriesConfig[category.id]?.isSpecial || false
}

// Получаем изображения для категории
const getCategoryImages = (category) => {
  const config = props.specialCategoriesConfig[category.id]
  if (config && config.images) {
    return config.images
  }
  return null
}

// Получаем логотип для категории
const getCategoryLogo = (category) => {
  const config = props.specialCategoriesConfig[category.id]
  if (config && config.logoUrl) {
    return config.logoUrl
  }
  return null
}
</script>

<style scoped>
.catalog-category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 24px;
  padding: 24px 0;
}

/* Планшеты */
@media (max-width: 1024px) {
  .catalog-category-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
  }
}

/* Мобильные устройства */
@media (max-width: 768px) {
  .catalog-category-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    padding: 16px 0;
    width: 100%;
  }
}

/* Маленькие мобильные устройства */
@media (max-width: 480px) {
  .catalog-category-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
    padding: 12px 0;
    width: 100%;
  }
}

/* Очень маленькие экраны */
@media (max-width: 360px) {
  .catalog-category-grid {
    gap: 6px;
    padding: 10px 0;
  }
}
</style>
