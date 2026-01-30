<template>
  <div class="products-page bg-[#3A3331]">
    <!-- Баннер категории (показывается для категорий уровня 2) -->
    <div v-if="bannerCategory" class="category-banner">
      <div
        v-if="bannerCategoryImageUrl"
        class="category-banner-image"
        :style="{ backgroundImage: `url(${bannerCategoryImageUrl})` }"
      ></div>
      <div class="category-banner-overlay"></div>
      
      <!-- Кнопка "Назад" с названием категории в верхнем углу баннера (только на мобильных) -->
      <div v-if="isMobile" class="category-banner-back-button">
        <button
          @click="goToParentCategory"
          class="mobile-back-button text-white hover:text-orange-500 transition-colors flex items-center justify-center"
        >
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <h1 class="mobile-category-title">
          {{ parentCategoryName || 'Каталог' }}
        </h1>
      </div>
      
      <div class="category-banner-content">
        <h1 class="category-banner-title">{{ bannerCategory.name }}</h1>
      </div>
    </div>

    <div :class="['container mx-auto', isMobile ? 'px-2 py-4' : 'px-4 py-8']">
      <div class="flex flex-col md:flex-row gap-8">
        <!-- Sidebar - показываем только если выбрана категория и не мобильное устройство -->
        <aside v-if="sidebarCategory && !isMobile" class="w-full md:w-64">
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
          <!-- Если не выбрана категория, показываем карточки категорий верхнего уровня -->
          <div v-if="!selectedCategory">
            <!-- Заголовок скрыт на мобильных -->
            <div v-if="!isMobile" class="mb-6">
              <h1 class="text-3xl font-bold text-white mb-2">Каталог</h1>
            </div>

            <div v-if="productsStore.loading" class="text-center py-12">
              <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
            </div>

            <div v-else-if="categoriesToDisplay.length === 0" class="text-center py-12 text-gray-400">
              Категории не найдены
            </div>

            <CatalogCategoryGrid
              v-else
              :categories="categoriesToDisplay"
              :special-categories-config="specialCategoriesConfig"
            />
          </div>

          <!-- Если выбрана категория, показываем дочерние категории или товары -->
          <div v-else>
            <!-- Кнопка "Назад" с названием категории для мобильных (когда нет баннера) -->
            <div v-if="isMobile && !bannerCategory" class="mb-4 flex items-center gap-3 w-full">
              <button
                @click="goToParentCategory"
                class="mobile-back-button text-white hover:text-orange-500 transition-colors flex items-center justify-center"
              >
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
                </svg>
              </button>
              <h1 class="mobile-category-title">
                {{ parentCategoryName || 'Каталог' }}
              </h1>
            </div>

            <!-- Если у выбранной категории есть дочерние категории, показываем их -->
            <div v-if="childCategoriesToShow.length > 0">
              <!-- Заголовок и кнопка "Назад" для десктопа -->
              <div v-if="!isMobile" class="mb-6">
                <!-- Кнопка "Назад" если есть родительская категория -->
                <button
                  v-if="selectedCategory.parentId"
                  @click="goToParentCategory"
                  class="mb-4 text-white hover:text-orange-500 transition-colors flex items-center gap-2"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
                  </svg>
                  Назад
                </button>
                <h1 class="text-3xl font-bold text-white mb-2">{{ selectedCategory.name }}</h1>
              </div>

              <div v-if="productsStore.loading" class="text-center py-12">
                <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
              </div>

              <div v-else-if="childCategoriesToShow.length === 0" class="text-center py-12 text-gray-400">
                Категории не найдены
              </div>

              <div v-else>
                <!-- Показываем дочерние категории (уровень 2 или 3) -->
                <CatalogCategoryGrid
                  :categories="childCategoriesToShow"
                  :special-categories-config="specialCategoriesConfig"
                />
                
                <!-- Когда на уровне 1 показываем категории уровня 3 — показываем товары категорий уровня 2 -->
                <div v-if="isMobile && selectedCategory && level2ProductsWhenOnLevel1.length > 0" class="mt-6">
                  <div class="grid grid-cols-2 gap-4">
                    <ProductCard
                      v-for="product in level2ProductsWhenOnLevel1"
                      :key="product.id"
                      :product="product"
                    />
                  </div>
                </div>
                
                <!-- Товары текущей выбранной категории (уровень 1, 2 или 3) -->
                <div v-if="isMobile && selectedCategory && selectedCategoryProducts.length > 0" class="mt-6">
                  <div class="grid grid-cols-2 gap-4">
                    <ProductCard
                      v-for="product in selectedCategoryProducts"
                      :key="product.id"
                      :product="product"
                    />
                  </div>
                </div>
              </div>
            </div>

            <!-- Если дочерних категорий нет, показываем товары -->
            <div v-else>
              <div v-if="productsStore.loading" class="text-center py-12">
                <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
              </div>

              <div v-else-if="productsStore.error" class="text-center py-12 text-red-400">
                Ошибка загрузки товаров: {{ productsStore.error }}
              </div>

              <div v-else-if="filteredProducts.length === 0" class="text-center py-12 text-gray-400">
                Товары не найдены
              </div>

              <div v-else class="grid grid-cols-2 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
                <ProductCard
                  v-for="product in filteredProducts"
                  :key="product.id"
                  :product="product"
                />
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useProductsStore } from '../stores/products'
import { fileApi } from '../services/api'
import ProductCard from '../components/ProductCard.vue'
import CatalogCategoryGrid from '../components/CatalogCategoryGrid.vue'

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

// Определение мобильного устройства
const isMobile = ref(false)

const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

// Получить категории верхнего уровня (без parentId) для отображения карточками
const topLevelCategories = computed(() => {
  return productsStore.categories.filter(cat => !cat.parentId && cat.imageUrl)
})

// Получить все категории для отображения
// При переходе на /products без выбранной категории всегда показываем только категории верхнего уровня
const categoriesToDisplay = computed(() => {
  // Всегда показываем только категории верхнего уровня, если категория не выбрана
  return topLevelCategories.value
})

// Получить дочерние категории для выбранной категории
// Показываем дочерние категории только если выбрана категория верхнего уровня (без parentId)
// На мобильных: если у категорий уровня 2 есть подкатегории уровня 3, показываем уровень 3 (пропуская уровень 2)
// На десктопе: показываем товары (не показываем карточки подкатегорий)
const childCategoriesToShow = computed(() => {
  if (!selectedCategory.value) return []
  
  // На десктопе не показываем карточки подкатегорий, сразу показываем товары
  if (!isMobile.value) {
    return []
  }
  
  // На мобильных показываем дочерние категории только для категорий верхнего уровня
  if (!selectedCategory.value.parentId) {
    // Получаем прямые дочерние категории выбранной категории (уровень 2)
    const level2Categories = productsStore.categories.filter(
      cat => cat.parentId === selectedCategory.value.id && cat.imageUrl
    )
    
    // Проверяем, есть ли у категорий уровня 2 дочерние категории уровня 3
    const level3Categories = []
    let hasLevel3Categories = false
    
    level2Categories.forEach(level2Cat => {
      const children = productsStore.categories.filter(
        cat => cat.parentId === level2Cat.id && cat.imageUrl
      )
      if (children.length > 0) {
        hasLevel3Categories = true
        level3Categories.push(...children)
      }
    })
    
    // Если есть категории уровня 3, показываем их (пропуская уровень 2)
    if (hasLevel3Categories && level3Categories.length > 0) {
      return level3Categories
    }
    
    // Если нет категорий уровня 3, показываем категории уровня 2
    return level2Categories
  }
  
  // На мобильных для категории уровня 2 показываем категории уровня 3
  if (isMobile.value && selectedCategory.value.parentId) {
    const parent = productsStore.categories.find(c => c.id === selectedCategory.value.parentId)
    // Если родитель без parentId — это категория уровня 2
    if (parent && !parent.parentId) {
      const level3Categories = productsStore.categories.filter(
        cat => cat.parentId === selectedCategory.value.id && cat.imageUrl
      )
      return level3Categories
    }
  }
  
  return []
})

// Товары текущей выбранной категории (для показа вместе с дочерними категориями на 1, 2 и 3 уровне)
const selectedCategoryProducts = computed(() => {
  if (!selectedCategory.value || !isMobile.value) return []
  
  // Товары, которые принадлежат именно выбранной категории (не дочерним)
  return productsStore.products.filter(p => {
    if (!p.category || !p.category.id) return false
    return p.category.id === selectedCategory.value.id
  })
})

// Товары категорий уровня 2 (когда на уровне 1 показываем категории уровня 3 — уровень 2 пропущен, но его товары показываем)
const level2ProductsWhenOnLevel1 = computed(() => {
  if (!selectedCategory.value || !isMobile.value) return []
  
  // Только когда выбрана категория уровня 1
  if (selectedCategory.value.parentId) return []
  
  // Все категории уровня 2 под этой категорией уровня 1
  const level2Categories = productsStore.categories.filter(
    cat => cat.parentId === selectedCategory.value.id
  )
  const level2Ids = level2Categories.map(c => c.id)
  
  if (level2Ids.length === 0) return []
  
  // Товары, принадлежащие любой из категорий уровня 2
  return productsStore.products.filter(p => {
    if (!p.category || !p.category.id) return false
    return level2Ids.includes(p.category.id)
  })
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

// Получить категорию для баннера
// Десктоп: баннер с категорией уровня 1 при любом выбранном уровне (1, 2 или 3)
// Мобильные: баннер только когда выбрана категория уровня 1
const bannerCategory = computed(() => {
  if (!selectedCategory.value) return null
  
  if (isMobile.value) {
    // На мобильных — баннер только для категории уровня 1
    if (!selectedCategory.value.parentId) {
      return selectedCategory.value
    }
    return null
  }
  
  // На десктопе — всегда показываем баннер категории уровня 1 при любой выбранной категории
  return topLevelCategory.value
})

// Получить изображение категории для баннера
const bannerCategoryImageUrl = computed(() => {
  if (!bannerCategory.value || !bannerCategory.value.imageUrl) return null
  return fileApi.getFileUrl(bannerCategory.value.imageUrl)
})

// Получить название родительской категории для отображения рядом со стрелкой "Назад"
// Показываем название категории на уровень назад
// Название категории рядом с кнопкой "Назад" — показываем категорию, куда ведёт "Назад"
// На мобильных с уровня 3 возвращаемся на уровень 1, поэтому показываем название уровня 1
const parentCategoryName = computed(() => {
  if (!selectedCategory.value) {
    return 'Каталог'
  }
  
  // На мобильных с уровня 3 показываем название уровня 1 (куда ведёт "Назад")
  if (isMobile.value && selectedCategory.value.parentId) {
    const parent = productsStore.categories.find(c => c.id === selectedCategory.value.parentId)
    if (parent && parent.parentId) {
      // Мы на уровне 3 — "Назад" ведёт на уровень 1
      const level1 = productsStore.categories.find(c => c.id === parent.parentId)
      if (level1 && level1.name) return level1.name
    }
    if (parent && parent.name) {
      // Мы на уровне 2 — показываем название уровня 1
      return parent.name
    }
  }
  
  // Десктоп или уровень 1: показываем родительскую категорию или "Каталог"
  if (selectedCategory.value.parentId) {
    const parent = productsStore.categories.find(c => c.id === selectedCategory.value.parentId)
    if (parent && parent.name) return parent.name
  }
  
  return 'Каталог'
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

// Конфигурация для специальных категорий (с несколькими изображениями и логотипами)
const specialCategoriesConfig = computed(() => {
  const config = {}
  
  // Определяем, какие категории использовать для конфигурации
  const categoriesForConfig = selectedCategory.value && childCategoriesToShow.value.length > 0
    ? childCategoriesToShow.value
    : categoriesToDisplay.value
  
  // Применяем конфигурацию ко всем категориям для отображения
  categoriesForConfig.forEach(category => {
    // Пример: если у категории есть специальный идентификатор или имя
    // Можно добавить логику определения специальных категорий
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

// Переход к родительской категории
const goToParentCategory = () => {
  if (!selectedCategory.value) {
    router.push('/products')
    return
  }
  
  if (!selectedCategory.value.parentId) {
    // Уже на уровне 1 — возврат в каталог
    router.push('/products')
    return
  }
  
  // На мобильных: с уровня 3 возвращаемся на уровень 1 (уровень 2 пропущен в навигации)
  if (isMobile.value) {
    const parent = productsStore.categories.find(c => c.id === selectedCategory.value.parentId)
    if (parent && parent.parentId) {
      // Текущая категория — уровень 3, родитель — уровень 2; переходим на уровень 1
      router.push(`/products?category=${parent.parentId}`)
      return
    }
  }
  
  router.push(`/products?category=${selectedCategory.value.parentId}`)
}

onMounted(async () => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  await productsStore.fetchProducts()
  await productsStore.fetchCategories()
})
</script>

<style scoped>
.products-page {
  min-height: 100vh;
}

/* Кнопка "Назад" для мобильных */
.mobile-back-button {
  padding: 10px;
  border-radius: 8px;
  background: rgba(46, 40, 38, 0.9);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
  flex-shrink: 0;
}

.mobile-back-button:active {
  background: rgba(46, 40, 38, 1);
  transform: scale(0.96);
}

.mobile-category-title {
  font-family: "helvetica", sans-serif !important;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  font-weight: 500;
  font-size: 14px !important;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
  display: block !important;
  color: #ffffff !important;
  line-height: 1.4;
  margin: 0;
  padding: 0;
  visibility: visible !important;
  opacity: 1 !important;
}

/* Мобильные стили для контейнера */
@media (max-width: 768px) {
  .products-page .container {
    padding-left: 8px;
    padding-right: 8px;
    padding-top: 16px;
    padding-bottom: 16px;
  }
  
  .products-page .products-section {
    width: 100%;
    padding: 0;
  }
  
  /* Убираем отступы у секции с карточками на мобильных */
  .products-page .products-section > div {
    margin: 0;
  }
}

@media (max-width: 480px) {
  .products-page .container {
    padding-left: 8px;
    padding-right: 8px;
  }
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

.category-banner-back-button {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 3;
  display: flex;
  align-items: center;
  gap: 12px;
  width: calc(100% - 32px);
  max-width: calc(100% - 32px);
}

.category-banner-back-button .mobile-category-title {
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
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
