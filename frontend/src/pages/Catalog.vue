<template>
  <div class="catalog-page bg-[#3A3331] min-h-screen">
    <div class="container mx-auto px-4 py-8">
      <div class="mb-6">
        <h1 class="text-3xl font-bold text-white mb-2">Каталог</h1>
      </div>

      <div v-if="productsStore.loading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>

      <div v-else-if="topLevelCategories.length === 0" class="text-center py-12 text-gray-400">
        Категории не найдены
      </div>

      <CatalogCategoryGrid
        v-else
        :categories="topLevelCategories"
        :special-categories-config="specialCategoriesConfig"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useProductsStore } from '../stores/products'
import CatalogCategoryGrid from '../components/CatalogCategoryGrid.vue'
import { fileApi } from '../services/api'

const productsStore = useProductsStore()

const topLevelCategories = computed(() => {
  return productsStore.categories.filter(cat => !cat.parentId && cat.imageUrl)
})

// Конфигурация для специальных категорий (с несколькими изображениями и логотипами)
// Пример конфигурации - можно настроить под ваши категории
const specialCategoriesConfig = computed(() => {
  const config = {}
  
  // Пример: если у категории есть специальный идентификатор или имя
  topLevelCategories.value.forEach(category => {
    // Здесь можно добавить логику определения специальных категорий
    // Например, по имени или ID категории
    if (category.name?.toLowerCase().includes('bork home') || 
        category.name?.toLowerCase().includes('healthterior') ||
        category.name?.toLowerCase().includes('interior') ||
        category.name?.toLowerCase().includes('limited')) {
      config[category.id] = {
        isSpecial: true,
        // Пример: несколько изображений для анимации
        images: category.imageUrl ? [
          fileApi.getFileUrl(category.imageUrl),
          fileApi.getFileUrl(category.imageUrl), // В реальности это будут разные изображения
          fileApi.getFileUrl(category.imageUrl)
        ] : null,
        // Пример: URL логотипа (можно добавить в модель категории)
        logoUrl: null // category.logoUrl ? fileApi.getFileUrl(category.logoUrl) : null
      }
    }
  })
  
  return config
})

onMounted(async () => {
  await productsStore.fetchCategories()
})
</script>

<style scoped>
.catalog-page {
  padding-top: 60px;
}

@media (min-width: 768px) {
  .catalog-page {
    padding-top: 110px;
  }
}
</style>
