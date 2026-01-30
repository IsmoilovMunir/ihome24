<template>
  <div class="products-page bg-[#3A3331]">
    <!-- Баннер категории (показывается для всех категорий, использует родительскую категорию верхнего уровня) -->
    <div v-if="topLevelCategory" class="category-banner">
      <div
        v-if="topLevelCategoryImageUrl"
        class="category-banner-image"
        :style="{ backgroundImage: `url(${topLevelCategoryImageUrl})` }"
      ></div>
      <div class="category-banner-overlay"></div>
      <div class="category-banner-content">
        <h1 class="category-banner-title">{{ topLevelCategory.name }}</h1>
      </div>
    </div>

    <div class="container mx-auto px-4 py-8">
      <div class="flex flex-col md:flex-row gap-8">
        <!-- Sidebar - показываем только если выбрана категория -->
        <aside v-if="sidebarCategory" class="w-full md:w-64">
          <div class="category-sidebar sticky top-[110px]">
            <h3 class="category-sidebar-title">{{ sidebarCategory.name }}</h3>
            <ul class="category-sidebar-list">
              <!-- Кнопка "Показать все" вместо родительской категории уровня 2 -->
              <li class="category-sidebar-item">
                <button
                  @click="showAllProducts"
                  :class="['category-sidebar-button', { 'category-sidebar-button-active': !selectedCategoryId || isCategoryActive(sidebarCategory.id) }]"
                >
                  Показать все
                </button>
              </li>
              <!-- Категории уровня 2 с раскрывающимися дочерними категориями уровня 3 -->
              <li
                v-for="level2Category in level2CategoriesWithChildren"
                :key="level2Category.id"
                class="category-sidebar-item category-sidebar-item-expandable"
              >
                <!-- Если у категории уровня 2 есть дочерние уровня 3 - делаем раскрывающейся -->
                <div v-if="level2Category.hasChildren" class="category-sidebar-expandable">
                  <button
                    @click="toggleCategory(level2Category.id)"
                    class="category-sidebar-link category-sidebar-link-expandable category-sidebar-link-level2"
                  >
                    <span>{{ level2Category.name }}</span>
                    <svg
                      :class="['category-sidebar-arrow', { 'category-sidebar-arrow-expanded': isCategoryExpanded(level2Category.id) }]"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                    </svg>
                  </button>
                  <!-- Дочерние категории уровня 3 (показываются при раскрытии) -->
                  <ul
                    v-if="isCategoryExpanded(level2Category.id)"
                    class="category-sidebar-sublist"
                  >
                    <li
                      v-for="level3Category in level2Category.children"
                      :key="level3Category.id"
                      class="category-sidebar-subitem"
                    >
                      <router-link
                        :to="`/products?category=${level3Category.id}`"
                        :class="['category-sidebar-link category-sidebar-sublink', { 'category-sidebar-link-active': isCategoryActive(level3Category.id) }]"
                      >
                        {{ level3Category.name }}
                      </router-link>
                    </li>
                  </ul>
                </div>
                <!-- Если у категории уровня 2 нет дочерних - обычная ссылка -->
                <router-link
                  v-else
                  :to="`/products?category=${level2Category.id}`"
                  :class="['category-sidebar-link', { 'category-sidebar-link-active': isCategoryActive(level2Category.id) }]"
                >
                  {{ level2Category.name }}
                </router-link>
              </li>
            </ul>
          </div>
        </aside>

        <!-- Products Grid -->
        <main class="flex-1 products-section">
          <div v-if="!selectedCategory" class="mb-6">
            <h1 class="text-3xl font-bold text-white mb-2">Каталог товаров</h1>
          </div>

          <div v-if="productsStore.loading" class="text-center py-12">
          <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
        </div>

        <div v-else-if="productsStore.error" class="text-center py-12 text-red-400">
          Ошибка загрузки товаров: {{ productsStore.error }}
        </div>

        <div v-else-if="filteredProducts.length === 0" class="text-center py-12 text-gray-400">
          Товары не найдены
        </div>

          <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            <ProductCard
              v-for="product in filteredProducts"
              :key="product.id"
              :product="product"
            />
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useProductsStore } from '../stores/products'
import { fileApi } from '../services/api'
import ProductCard from '../components/ProductCard.vue'

const route = useRoute()
const router = useRouter()
const productsStore = useProductsStore()

const selectedCategoryId = computed(() => {
  const categoryId = route.query.category
  return categoryId ? Number(categoryId) : null
})

const selectedCategory = computed(() => {
  if (!selectedCategoryId.value) return null
  return productsStore.categories.find(c => c.id === selectedCategoryId.value)
})

// Получить родительскую категорию верхнего уровня для баннера
// Если выбрана дочерняя категория, находим её родительскую категорию верхнего уровня
const topLevelCategory = computed(() => {
  if (!selectedCategory.value) return null
  
  let currentCategory = selectedCategory.value
  
  // Поднимаемся вверх по иерархии до родительской категории верхнего уровня
  while (currentCategory && currentCategory.parentId) {
    const parent = productsStore.categories.find(c => c.id === currentCategory.parentId)
    if (!parent) break
    currentCategory = parent
  }
  
  // Если это родительская категория верхнего уровня (без parentId)
  if (!currentCategory.parentId) {
    return currentCategory
  }
  
  return null
})

// Получить категорию для отображения в боковом меню
// Всегда показываем родительскую категорию верхнего уровня (уровень 1)
const sidebarCategory = computed(() => {
  if (!selectedCategory.value) return null
  
  // Используем topLevelCategory, чтобы всегда показывать родительскую категорию верхнего уровня
  return topLevelCategory.value
})

// Получить дочерние категории для категории в боковом меню
const childCategories = computed(() => {
  if (!sidebarCategory.value) return []
  return productsStore.categories.filter(cat => cat.parentId === sidebarCategory.value.id)
})

// Состояние раскрытых категорий уровня 2
const expandedCategories = ref(new Set())

// Переключить раскрытие категории уровня 2
const toggleCategory = (categoryId) => {
  if (expandedCategories.value.has(categoryId)) {
    expandedCategories.value.delete(categoryId)
  } else {
    expandedCategories.value.add(categoryId)
  }
}

// Проверить, раскрыта ли категория
const isCategoryExpanded = (categoryId) => {
  return expandedCategories.value.has(categoryId)
}

// Получить категории уровня 2 с их дочерними категориями уровня 3
const level2CategoriesWithChildren = computed(() => {
  if (!sidebarCategory.value) return []
  
  // Получаем все категории уровня 2, которые являются дочерними для sidebarCategory
  let level2Categories = productsStore.categories.filter(cat => cat.parentId === sidebarCategory.value.id)
  
  // Если выбрана категория уровня 3, нужно убедиться, что её родительская категория уровня 2 тоже включена
  if (selectedCategory.value && selectedCategory.value.parentId) {
    const parentLevel2 = productsStore.categories.find(c => c.id === selectedCategory.value.parentId)
    if (parentLevel2 && parentLevel2.parentId === sidebarCategory.value.id) {
      // Проверяем, есть ли уже эта категория в списке
      const alreadyIncluded = level2Categories.some(cat => cat.id === parentLevel2.id)
      if (!alreadyIncluded) {
        level2Categories = [parentLevel2, ...level2Categories]
      }
    }
  }
  
  return level2Categories.map(level2Cat => {
    const level3Categories = productsStore.categories.filter(cat => cat.parentId === level2Cat.id)
    return {
      ...level2Cat,
      children: level3Categories,
      hasChildren: level3Categories.length > 0
    }
  })
})

// Получить изображение родительской категории верхнего уровня для баннера
const topLevelCategoryImageUrl = computed(() => {
  if (!topLevelCategory.value || !topLevelCategory.value.imageUrl) return null
  return fileApi.getFileUrl(topLevelCategory.value.imageUrl)
})

// Получить все дочерние категории рекурсивно (включая уровень 3 и глубже)
// Если выбрана родительская категория верхнего уровня - показываем все её дочерние
// Если выбрана подкатегория - показываем только её и её дочерние
const getAllChildCategoryIds = (categoryId) => {
  const selectedCat = productsStore.categories.find(c => c.id === categoryId)
  if (!selectedCat) return [categoryId]
  
  const childIds = [categoryId] // Включаем саму категорию
  
  // Если это родительская категория верхнего уровня (без parentId)
  // Показываем все её дочерние категории
  if (!selectedCat.parentId) {
    const directChildren = productsStore.categories.filter(cat => cat.parentId === categoryId)
    
    directChildren.forEach(child => {
      childIds.push(child.id)
      // Рекурсивно получаем дочерние категории уровня 3
      const level3Children = productsStore.categories.filter(cat => cat.parentId === child.id)
      level3Children.forEach(level3Child => {
        childIds.push(level3Child.id)
      })
    })
  } else {
    // Если это подкатегория (уровень 2 или 3), показываем только её и её дочерние
    const directChildren = productsStore.categories.filter(cat => cat.parentId === categoryId)
    directChildren.forEach(child => {
      childIds.push(child.id)
    })
  }
  
  return childIds
}

// Проверка, активна ли категория
const isCategoryActive = (categoryId) => {
  return selectedCategoryId.value === categoryId
}

const filteredProducts = computed(() => {
  if (!selectedCategoryId.value) {
    return productsStore.products
  }
  
  // Получаем все дочерние категории (включая саму выбранную)
  const categoryIds = getAllChildCategoryIds(selectedCategoryId.value)
  
  // Фильтруем товары, которые принадлежат любой из этих категорий
  return productsStore.products.filter(p => {
    if (!p.category || !p.category.id) return false
    return categoryIds.includes(p.category.id)
  })
})

const scrollToProducts = () => {
  // Прокручиваем к товарам
  const productsSection = document.querySelector('.products-section')
  if (productsSection) {
    productsSection.scrollIntoView({ behavior: 'smooth', block: 'start' })
  } else {
    // Если секция не найдена, прокручиваем к контейнеру с товарами
    window.scrollTo({ top: 400, behavior: 'smooth' })
  }
}

const showAllProducts = () => {
  // Переходим на страницу с родительской категорией (показываем все товары)
  router.push(`/products?category=${sidebarCategory.value.id}`)
  scrollToProducts()
}

onMounted(async () => {
  await productsStore.fetchProducts()
  await productsStore.fetchCategories()
})
</script>

<style scoped>
.products-page {
  min-height: 100vh;
}

/* Баннер категории */
.category-banner {
  position: relative;
  width: 100%;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #2E2826;
}

.category-banner-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.category-banner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1;
}

.category-banner-content {
  position: relative;
  z-index: 2;
  text-align: center;
}

.category-banner-title {
  font-size: 48px;
  font-weight: 500;
  font-family: "helvetica", sans-serif;
  color: #fff;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.category-banner-button {
  font-size: 18px;
  font-weight: 500;
  font-family: "helvetica", sans-serif;
  color: #fff;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  background: #F47427;
  border: none;
  padding: 16px 40px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s, transform 0.2s;
}

.category-banner-button:hover {
  background: #E0651D;
  transform: translateY(-2px);
}

.category-banner-button:active {
  transform: translateY(0);
}

@media screen and (max-width: 768px) {
  .category-banner {
    height: 300px;
  }
  
  .category-banner-title {
    font-size: 32px;
  }
}

/* Боковое меню категорий */
.category-sidebar {
  background: rgba(46, 40, 38, 0.7);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-radius: 12px;
  padding: 24px;
}

.category-sidebar-title {
  font-size: 18px;
  font-weight: 500;
  font-family: "helvetica", sans-serif;
  color: #fff;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.category-sidebar-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.category-sidebar-item {
  margin-bottom: 8px;
}

.category-sidebar-item:last-child {
  margin-bottom: 0;
}

.category-sidebar-link {
  display: block;
  color: #9d9390;
  font-size: 14px;
  font-family: "helvetica", sans-serif;
  text-decoration: none;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding: 8px 0;
  transition: color 0.3s;
  position: relative;
  font-weight: 500;
}

.category-sidebar-link:hover {
  color: #fff;
}

.category-sidebar-link:hover::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: #F47427;
}

.category-sidebar-link-active {
  color: #fff;
}

.category-sidebar-link-active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: #F47427;
}

.category-sidebar-button {
  display: block;
  width: 100%;
  color: #9d9390;
  font-size: 14px;
  font-family: "helvetica", sans-serif;
  text-decoration: none;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding: 8px 0;
  transition: color 0.3s;
  position: relative;
  font-weight: 500;
  background: none;
  border: none;
  cursor: pointer;
  text-align: left;
}

.category-sidebar-button:hover {
  color: #fff;
}

.category-sidebar-button:hover::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: #F47427;
}

.category-sidebar-button-active {
  color: #fff;
}

.category-sidebar-button-active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: #F47427;
}

/* Раскрывающиеся категории */
.category-sidebar-item-expandable {
  margin-bottom: 0;
}

.category-sidebar-expandable {
  width: 100%;
}

.category-sidebar-link-expandable {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.category-sidebar-arrow {
  width: 16px;
  height: 16px;
  transition: transform 0.3s ease;
  flex-shrink: 0;
  margin-left: 8px;
}

.category-sidebar-arrow-expanded {
  transform: rotate(180deg);
}

.category-sidebar-sublist {
  list-style: none;
  margin: 0;
  padding: 0;
  padding-left: 20px;
  margin-top: 4px;
}

.category-sidebar-subitem {
  margin-bottom: 4px;
}

.category-sidebar-subitem:last-child {
  margin-bottom: 0;
}

.category-sidebar-sublink {
  padding-left: 0;
  font-size: 13px;
}

/* Категория уровня 2 всегда серая, никогда не становится белой */
.category-sidebar-link-level2 {
  color: #9d9390 !important;
}

.category-sidebar-link-level2:hover {
  color: #9d9390 !important;
}

.category-sidebar-link-level2:hover::after {
  display: none; /* Убираем желтую линию при наведении */
}
</style>
