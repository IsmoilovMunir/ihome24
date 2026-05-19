<template>
  <div
    class="catalog-category-grid"
    :class="{ 'catalog-category-grid--child': nestedMobileLayout }"
  >
    <CatalogCategoryTile
      v-for="category in sortedCategories"
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
  /** Дочерние категории на мобильном: 2 колонки (≤424px), 3 колонки (425–768px) */
  nestedMobileLayout: {
    type: Boolean,
    default: false,
  },
})

/**
 * Как в CatalogCategoryTile: полоска из 3 mobile-фото только если не спецкатегория с картинками из конфига.
 * Порядок: сначала все плитки с одним кадром (и карусель из конфига), затем секции с 3 фото в ряд —
 * внутри групп сохраняем порядок из API/админки.
 */
const sortedCategories = computed(() => {
  const list = [...props.categories]
  if (list.length <= 1)
    return list

  const config = props.specialCategoriesConfig || {}

  const isTripleStripTile = (category) => {
    const cfg = config[category.id]
    if (cfg?.isSpecial && cfg?.images?.length)
      return false
    return !!(category.mobileImageUrl && category.mobileImageUrl2 && category.mobileImageUrl3)
  }

  return list
    .map((c, i) => ({ c, i }))
    .sort((a, b) => {
      const ta = isTripleStripTile(a.c) ? 1 : 0
      const tb = isTripleStripTile(b.c) ? 1 : 0
      if (ta !== tb)
        return ta - tb
      return a.i - b.i
    })
    .map(x => x.c)
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
/* Шире 320px: ячейки растут (minmax), секция внутри плитки тоже; gap ровно 10px */
.catalog-category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(134px, 1fr));
  column-gap: 10px;
  row-gap: 10px;
  padding: 10px 0;
  width: 100%;
  box-sizing: border-box;
}

/* Три фото: ширина как у двух обычных плиток (2 колонки + gap) — растёт с экраном */
.catalog-category-grid :deep(.catalog-category-tile--triple-strip) {
  grid-column: span 2;
  width: 100%;
  max-width: none;
  justify-self: stretch;
}

/* 320px и уже: фиксированные колонки 134px, плитка квадратная 134×134 (только корень каталога) */
@media (max-width: 320px) {
  .catalog-category-grid:not(.catalog-category-grid--child) {
    grid-template-columns: repeat(auto-fill, 134px);
    justify-content: center;
  }
}

/*
  Дочерние категории (мобильный): плотнее сетка.
  ≤424px — 2 плитки в ряд; с 425px до 768px — 3 в ряд.
*/
@media (max-width: 424px) {
  .catalog-category-grid--child {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (min-width: 425px) and (max-width: 768px) {
  .catalog-category-grid--child {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

/* В дочерней сетке плитки растягиваются по ячейке, без фиксированных 134px */
@media (max-width: 768px) {
  .catalog-category-grid--child :deep(.catalog-category-tile:not(.catalog-category-tile--triple-strip)) {
    align-items: stretch;
    width: 100%;
    min-width: 0;
    max-width: 100%;
  }

  .catalog-category-grid--child :deep(.catalog-category-tile:not(.catalog-category-tile--triple-strip) .catalog-category-tile-image-container) {
    width: 100%;
    max-width: 100%;
    align-self: stretch;
  }
}
</style>
