<template>
  <header class="header-modern">
    <!-- Верхний слой: логотип и иконки -->
    <div class="header-top">
      <nav class="header-navbar">
        <ul class="header-nav-left">
          <!-- Можно добавить иконку слева, если нужно -->
        </ul>
        
        <div class="header-logo">
          <router-link to="/" class="header-logo-link">
            <span class="logo-text">iHome24</span>
          </router-link>
        </div>
        
        <ul class="header-nav-right">
          <!-- Корзина -->
          <li class="header-nav-item">
            <router-link to="/cart" class="header-nav-link relative">
              <svg
                class="header-icon"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"
                />
              </svg>
              <span
                v-if="cartStore.totalItems > 0"
                class="cart-badge"
              >
                {{ cartStore.totalItems }}
              </span>
            </router-link>
          </li>
          
          <!-- Поиск (можно добавить функционал позже) -->
          <li class="header-nav-item">
            <button class="header-nav-link" @click="toggleSearch">
              <svg
                class="header-icon"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                />
              </svg>
            </button>
          </li>
          
          <!-- Аккаунт -->
          <li class="header-nav-item">
            <div v-if="authStore.isAuthenticated" class="header-account">
              <router-link to="/profile" class="header-nav-link">
                <svg
                  class="header-icon"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                  />
                </svg>
              </router-link>
            </div>
            <router-link v-else to="/login" class="header-nav-link">
              <svg
                class="header-icon"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                />
              </svg>
            </router-link>
          </li>
        </ul>
      </nav>
    </div>
    
    <!-- Нижний слой: горизонтальное меню -->
    <div class="header-menu">
      <div class="horizontal-menu">
        <ul class="menu-list">
          <li class="menu-item">
            <router-link to="/" class="menu-link">Главная</router-link>
          </li>
          <li
            v-for="parentCategory in parentCategories"
            :key="parentCategory.id"
            class="menu-item menu-item-with-submenu"
            @mouseenter="showSubmenu(parentCategory.id)"
            @mouseleave="hideSubmenu"
          >
            <router-link
              :to="`/products?category=${parentCategory.id}`"
              :class="['menu-link', { 'menu-link-active': isCategoryActive(parentCategory.id) }]"
              exact-active-class=""
              @click="handleMainMenuClick"
            >
              {{ parentCategory.name }}
            </router-link>
          </li>
        </ul>
      </div>
    </div>
    
    <!-- Большое подменю с картинками -->
    <transition name="dropdown-menu">
      <div
        v-if="visibleSubmenu && getCurrentCategoryData()"
        class="dropdown-menu"
        @mouseenter="keepSubmenuVisible"
        @mouseleave="hideSubmenu"
      >
        <div class="dropdown-menu-wrap">
          <ul class="dropdown-menu-list">
            <li
              v-for="(group, groupIndex) in getCategoryGroups()"
              :key="groupIndex"
              class="dropdown-menu-item"
              :class="getItemWidthClass(groupIndex)"
            >
              <div class="dropdown-menu-submenu">
                <div
                  v-for="(submenu, submenuIndex) in group"
                  :key="submenuIndex"
                  class="double-submenu-submenu"
                >
                  <!-- Заголовок - категория уровня 2 -->
                  <div class="submenu-title">
                    <router-link
                      v-if="!submenu.hasLevel3"
                      :to="`/products?category=${submenu.level2Category.id}`"
                      :class="['submenu-title-link', { 'submenu-title-link-active': isCategoryActive(submenu.level2Category.id) }]"
                      exact-active-class=""
                      @click="handleSubmenuClick"
                    >
                      {{ submenu.title }}
                    </router-link>
                    <span 
                      v-else 
                      :class="['submenu-title-text', { 'submenu-title-text-active': hasActiveChild(submenu.level2Category.id) }]"
                    >
                      {{ submenu.title }}
                    </span>
                  </div>
                  <!-- Список категорий уровня 3 (если есть) -->
                  <div v-if="submenu.hasLevel3 && submenu.items.length > 0" class="submenu-menu-list">
                    <div
                      v-for="level3Category in submenu.items"
                      :key="level3Category.id"
                      class="submenu-item-wrapper"
                    >
                      <router-link
                        :to="`/products?category=${level3Category.id}`"
                        :class="['submenu-item-link', { 'submenu-item-link-active': isCategoryActive(level3Category.id) }]"
                        exact-active-class=""
                        @click="handleSubmenuClick"
                      >
                        {{ level3Category.name }}
                      </router-link>
                    </div>
                  </div>
                </div>
              </div>
            </li>
          </ul>
          <img
            v-if="getCategoryImage()"
            :src="getCategoryImage()"
            :alt="getCurrentCategoryData()?.name"
            :title="getCurrentCategoryData()?.name"
            class="dropdown-menu-img"
          />
        </div>
      </div>
    </transition>

    <!-- Оверлей поиска -->
    <Teleport to="body">
      <Transition name="search-overlay">
        <div
          v-if="searchOpen"
          class="search-overlay"
          @click.self="closeSearch"
        >
          <div class="search-overlay-panel">
            <div class="search-overlay-header">
              <input
                ref="searchInputRef"
                v-model="searchQuery"
                type="text"
                class="search-overlay-input"
                placeholder="Поиск по названию, категории, описанию..."
                autofocus
                @keydown.esc="closeSearch"
                @keydown.enter.prevent="goToSearchPage"
              />
              <button type="button" class="search-overlay-close" @click="closeSearch" aria-label="Закрыть">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
            <div class="search-overlay-results">
              <div v-if="searchQuery.trim().length < 2" class="search-overlay-hint">
                Введите минимум 2 символа для поиска
              </div>
              <div v-else-if="searchResultsProducts.length === 0 && searchResultsCategories.length === 0" class="search-overlay-empty">
                Ничего не найдено
              </div>
              <template v-else>
                <div class="search-overlay-all-link-wrap">
                  <router-link
                    :to="{ path: '/search', query: { q: searchQuery.trim() } }"
                    class="search-overlay-all-link"
                    @click="closeSearch"
                  >
                    Все результаты по запросу «{{ searchQuery.trim() }}» →
                  </router-link>
                </div>
                <div v-if="searchResultsCategories.length > 0" class="search-results-group">
                  <div class="search-results-group-title">Категории</div>
                  <router-link
                    v-for="cat in searchResultsCategories"
                    :key="'cat-' + cat.id"
                    :to="`/products?category=${cat.id}`"
                    class="search-result-item search-result-category"
                    @click="closeSearch"
                  >
                    {{ cat.name }}
                  </router-link>
                </div>
                <div v-if="searchResultsProducts.length > 0" class="search-results-group">
                  <div class="search-results-group-title">Товары</div>
                  <router-link
                    v-for="product in searchResultsProducts"
                    :key="'prod-' + product.id"
                    :to="`/products/${product.id}`"
                    class="search-result-item search-result-product"
                    @click="closeSearch"
                  >
                    <img
                      v-if="getProductSearchImageUrl(product)"
                      :src="getProductSearchImageUrl(product)"
                      :alt="product.name"
                      class="search-result-product-img"
                    />
                    <div class="search-result-product-info">
                      <span class="search-result-product-name">{{ product.name }}</span>
                      <span v-if="product.category?.name" class="search-result-product-category">{{ product.category.name }}</span>
                      <span v-if="product.price != null" class="search-result-product-price">{{ formatSearchPrice(product.price) }}</span>
                    </div>
                  </router-link>
                </div>
              </template>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </header>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { useAuthStore } from '../stores/auth'
import { useProductsStore } from '../stores/products'
import { fileApi } from '../services/api'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const authStore = useAuthStore()
const productsStore = useProductsStore()

const visibleSubmenu = ref(null)
const submenuTimeout = ref(null)

const searchOpen = ref(false)
const searchQuery = ref('')
const searchInputRef = ref(null)

const categories = computed(() => productsStore.categories)

// Проверка, активна ли категория
const isCategoryActive = (categoryId) => {
  const currentCategoryId = route.query.category
  if (!currentCategoryId) return false
  return String(categoryId) === String(currentCategoryId)
}

// Проверка, активна ли любая из дочерних категорий уровня 3
const hasActiveChild = (level2CategoryId) => {
  const currentCategoryId = route.query.category
  if (!currentCategoryId) return false
  const level3Categories = getChildCategories(level2CategoryId)
  return level3Categories.some(cat => String(cat.id) === String(currentCategoryId))
}

// Родительские категории (без parentId)
const parentCategories = computed(() => {
  return categories.value.filter(cat => !cat.parentId)
})

// Получить дочерние категории для родительской
const getChildCategories = (parentId) => {
  return categories.value.filter(cat => cat.parentId === parentId)
}

// Получить данные текущей категории
const getCurrentCategoryData = () => {
  if (!visibleSubmenu.value) return null
  return categories.value.find(cat => cat.id === visibleSubmenu.value)
}

// Получить картинку категории
const getCategoryImage = () => {
  const category = getCurrentCategoryData()
  if (!category || !category.imageUrl) return null
  return fileApi.getFileUrl(category.imageUrl)
}

// Группировать дочерние категории для отображения в колонках
// Поддержка 3 уровней: уровень 1 -> уровень 2 -> уровень 3
// Структура как в vue-bork: категории уровня 2 с заголовками, под ними категории уровня 3
const getCategoryGroups = () => {
  if (!visibleSubmenu.value) return []
  
  const level2Categories = getChildCategories(visibleSubmenu.value) // Категории уровня 2
  if (level2Categories.length === 0) return []
  
  // Группируем категории уровня 2 с их дочерними категориями уровня 3
  // Каждая колонка может содержать несколько групп (категория уровня 2 + её дочерние уровня 3)
  const groups = []
  const itemsPerColumn = 12 // Максимум групп в колонке
  let currentGroup = []
  
  level2Categories.forEach((level2Cat) => {
    const level3Categories = getChildCategories(level2Cat.id) // Категории уровня 3
    
    // Всегда создаем группу с заголовком уровня 2
    // Если есть категории уровня 3 - они будут подпунктами
    // Если нет - категория уровня 2 будет кликабельной
    currentGroup.push({
      title: level2Cat.name, // Заголовок - категория уровня 2
      items: level3Categories.length > 0 ? level3Categories : [], // Подпункты - категории уровня 3
      level2Category: level2Cat, // Ссылка на категорию уровня 2
      hasLevel3: level3Categories.length > 0 // Есть ли дочерние уровня 3
    })
    
    // Если группа заполнена, добавляем её в groups и создаем новую
    if (currentGroup.length >= itemsPerColumn) {
      groups.push(currentGroup)
      currentGroup = []
    }
  })
  
  // Добавляем оставшиеся элементы
  if (currentGroup.length > 0) {
    groups.push(currentGroup)
  }
  
  return groups
}

// Получить класс ширины для элемента меню
const getItemWidthClass = (index) => {
  const groups = getCategoryGroups()
  if (groups.length === 1) return 'dropdown-menu-item-width-260'
  if (groups.length === 2) return 'dropdown-menu-item-width-260'
  return 'dropdown-menu-item-width-260'
}

const showSubmenu = (categoryId) => {
  // Очищаем таймер, если он есть
  if (submenuTimeout.value) {
    clearTimeout(submenuTimeout.value)
    submenuTimeout.value = null
  }
  
  if (getChildCategories(categoryId).length > 0) {
    visibleSubmenu.value = categoryId
  }
}

const keepSubmenuVisible = () => {
  // Очищаем таймер при наведении на подменю
  if (submenuTimeout.value) {
    clearTimeout(submenuTimeout.value)
    submenuTimeout.value = null
  }
}

const hideSubmenu = () => {
  // Добавляем задержку перед закрытием, чтобы пользователь успел перейти к подменю
  submenuTimeout.value = setTimeout(() => {
    visibleSubmenu.value = null
    submenuTimeout.value = null
  }, 300) // 300ms задержка для плавного перехода
}

const handleMainMenuClick = () => {
  // При клике на пункт основного меню закрываем подменю
  visibleSubmenu.value = null
  if (submenuTimeout.value) {
    clearTimeout(submenuTimeout.value)
    submenuTimeout.value = null
  }
}

const handleSubmenuClick = () => {
  // При клике на дочернюю категорию закрываем подменю
  visibleSubmenu.value = null
  if (submenuTimeout.value) {
    clearTimeout(submenuTimeout.value)
    submenuTimeout.value = null
  }
}

const toggleSearch = async () => {
  searchOpen.value = true
  searchQuery.value = ''
  if (productsStore.products.length === 0) {
    await productsStore.fetchProducts()
  }
  await nextTick()
  searchInputRef.value?.focus()
}

const closeSearch = () => {
  searchOpen.value = false
  searchQuery.value = ''
}

const goToSearchPage = () => {
  const q = searchQuery.value.trim()
  if (q) {
    closeSearch()
    router.push({ path: '/search', query: { q } })
  }
}

const normalizeQuery = (q) => (q || '').toLowerCase().trim()

const matchesQuery = (text) => {
  if (!text) return false
  return normalizeQuery(String(text)).includes(normalizeQuery(searchQuery.value))
}

const searchResultsProducts = computed(() => {
  const q = normalizeQuery(searchQuery.value)
  if (q.length < 2) return []
  const products = productsStore.products || []
  return products.filter((p) => {
    if (matchesQuery(p.name)) return true
    if (matchesQuery(p.description)) return true
    if (p.category?.name && matchesQuery(p.category.name)) return true
    if (p.sku && matchesQuery(p.sku)) return true
    if (Array.isArray(p.benefits)) {
      if (p.benefits.some((b) => matchesQuery(b))) return true
    }
    if (Array.isArray(p.characteristics)) {
      if (p.characteristics.some((c) => matchesQuery(c.name) || matchesQuery(c.value))) return true
    }
    return false
  })
})

const searchResultsCategories = computed(() => {
  const q = normalizeQuery(searchQuery.value)
  if (q.length < 2) return []
  const cats = productsStore.categories || []
  return cats.filter((c) => c.name && matchesQuery(c.name))
})

const getProductSearchImageUrl = (product) => {
  if (product?.imageUrl) return fileApi.getFileUrl(product.imageUrl)
  if (product?.images?.[0]?.imageUrl) return fileApi.getFileUrl(product.images[0].imageUrl)
  if (product?.images?.[0]?.url) return fileApi.getFileUrl(product.images[0].url)
  return null
}

const formatSearchPrice = (price) => {
  if (price == null) return ''
  return new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', maximumFractionDigits: 0 }).format(price)
}

const onKeydown = (e) => {
  if (e.key === 'Escape' && searchOpen.value) closeSearch()
}

onMounted(async () => {
  await productsStore.fetchCategories()
  window.addEventListener('keydown', onKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown)
})
</script>

<style scoped>
.header-modern {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 10001; /* Выше подменю */
  display: flex;
  flex-flow: column nowrap;
  width: 100%;
  color: #fff;
  background: rgba(46, 40, 38, 0.4);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
}

.header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
  width: 100%;
  min-height: 60px;
  padding: 6px 100px;
  background: rgba(46, 40, 38, 0.4);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
}

@media screen and (max-width: 768px) {
  .header-top {
    height: auto;
    padding: 6px 20px;
  }
}

.header-navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.header-nav-left,
.header-nav-right {
  display: flex;
  align-items: center;
  list-style: none;
  margin: 0;
  padding: 0;
  gap: 20px;
}

.header-logo {
  flex-grow: 1;
  max-width: 637px;
  margin: 0 auto;
  text-align: center;
}

@media screen and (max-width: 768px) {
  .header-logo {
    margin-right: 6px;
    text-align: left;
  }
}

.header-logo-link {
  display: inline-block;
  color: #fff;
  text-decoration: none;
}

.logo-text {
  font-size: 24px;
  font-weight: bold;
  font-family: "helvetica", sans-serif;
}

.header-nav-link {
  display: flex;
  align-items: center;
  color: #fff;
  text-decoration: none;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  transition: opacity 0.3s;
}

.header-nav-link:hover {
  opacity: 0.7;
}

.header-icon {
  display: inline-block;
  width: 24px;
  height: 24px;
}

.cart-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #F47427;
  color: #fff;
  font-size: 11px;
  font-weight: bold;
  border-radius: 50%;
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-menu {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
  z-index: 10001; /* Выше подменю, чтобы быть видимым */
  background: rgba(46, 40, 38, 0.4);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
}

@media screen and (max-width: 768px) {
  .header-menu {
    display: none;
  }
}

.horizontal-menu {
  display: flex;
  justify-content: center;
  padding: 0 20px;
}

.menu-list {
  display: flex;
  align-items: center;
  list-style: none;
  margin: 0;
  padding: 0;
  gap: 30px;
}

@media screen and (max-width: 768px) {
  .menu-list {
    flex-wrap: wrap;
    gap: 15px;
    justify-content: flex-start;
  }
}

.menu-link {
  display: block;
  padding: 15px 0;
  color: #fff;
  font-size: 14px;
  font-family: "helvetica", sans-serif;
  text-decoration: none;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  transition: opacity 0.3s;
  position: relative;
  font-weight: normal;
  z-index: 10002; /* Выше подменю */
}

.menu-link:hover {
  opacity: 1;
}

.menu-link:hover::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: #F47427;
}

.menu-link-active {
  opacity: 1;
}

.menu-link-active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: #F47427;
}

/* Подменю */
.menu-item-with-submenu {
  position: relative;
}

/* Невидимая область для плавного перехода к подменю */
.menu-item-with-submenu::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 0;
  right: 0;
  height: 10px;
  background: transparent;
  z-index: 10001;
}

.submenu {
  position: absolute;
  top: 100%;
  left: 0;
  background: #2E2826;
  min-width: 200px;
  list-style: none;
  margin: 0;
  padding: 0;
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
  transition: opacity 0.3s ease, transform 0.3s ease, visibility 0.3s;
  z-index: 10000;
  border-top: 2px solid #F47427;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
}

.submenu-visible {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.submenu-item {
  margin: 0;
  padding: 0;
}

.submenu-link {
  display: block;
  padding: 12px 20px;
  color: #fff;
  font-size: 14px;
  font-family: "helvetica", sans-serif;
  text-decoration: none;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  transition: background-color 0.3s, color 0.3s;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.submenu-link:hover {
  background-color: rgba(244, 116, 39, 0.1);
  color: #F47427;
}

.submenu-link.router-link-active {
  background-color: rgba(244, 116, 39, 0.2);
  color: #F47427;
}

.submenu-item:last-child .submenu-link {
  border-bottom: none;
}

/* Большое подменю с картинками */
.dropdown-menu {
  position: fixed;
  top: 110px; /* Начинается сразу после header (60px top + ~50px menu) */
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  width: 100%;
  min-height: 649px;
  background: #2E2826;
  z-index: 10000; /* Ниже header-menu, чтобы меню было видно */
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
  pointer-events: auto;
}

.dropdown-menu-wrap {
  display: flex;
  justify-content: center;
  width: 1280px;
  max-width: 100%;
  padding: 24px 20px 45px;
  margin: 0 auto;
}

.dropdown-menu-list {
  display: flex;
  margin: 0;
  padding: 0;
  list-style: none;
}

.dropdown-menu-list > li:not(:last-child) {
  margin-right: 80px;
}

.dropdown-menu-item {
  color: #fff;
  list-style-type: none;
}

.dropdown-menu-item-width-260 {
  width: 260px;
}

.dropdown-menu-item-width-250 {
  width: 250px;
}

.dropdown-menu-item-width-520 {
  width: 520px;
}

@media screen and (min-width: 1600px) {
  .dropdown-menu-item-width-520 {
    width: 580px;
  }
}

.dropdown-menu-submenu {
  display: flex;
  flex-direction: column;
}

.double-submenu-submenu {
  margin-bottom: 15px;
}

.double-submenu-submenu:last-child {
  margin-bottom: 0;
}

.submenu-title {
  font-size: 14px;
  font-family: "helvetica", sans-serif;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  font-weight: 500;
  line-height: 1.2;
}

/* Заголовок серым, если есть дочерние уровня 3 */
.submenu-title-text {
  display: block;
  color: #9d9390; /* Серый цвет по умолчанию */
  font-size: 14px;
  font-family: "helvetica", sans-serif;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  font-weight: 500;
  transition: color 0.3s;
}

.submenu-title-text-active {
  color: #fff; /* Белый цвет, если выбрана дочерняя категория */
}

.submenu-title-link {
  display: block;
  color: #fff; /* Белый цвет по умолчанию (категория уровня 2 без дочерних) */
  font-size: 14px;
  font-family: "helvetica", sans-serif;
  text-decoration: none;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  font-weight: 500;
  transition: color 0.3s;
  position: relative;
}

.submenu-title-link:hover {
  color: #fff; /* Остается белым при наведении */
}

.submenu-title-link:hover::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background: #F47427;
}

.submenu-title-link-active {
  color: #fff; /* Белый цвет для выбранной категории */
}

.submenu-title-link-active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background: #F47427;
}

.submenu-menu-list {
  display: flex;
  flex-direction: column;
}

.submenu-item-wrapper {
  margin-bottom: 0;
}

.submenu-item-link {
  display: block;
  color: #fff; /* Белый цвет по умолчанию (категория уровня 3) */
  font-size: 14px;
  font-family: "helvetica", sans-serif;
  text-decoration: none;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding: 4px 0;
  transition: color 0.3s;
  cursor: pointer;
  pointer-events: auto;
  position: relative;
  font-weight: 500; /* Такой же как у заголовков */
  line-height: 1.5;
}

.submenu-item-link:hover {
  color: #fff; /* Остается белым при наведении */
}

.submenu-item-link:hover::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: #F47427;
}

.submenu-item-link-active {
  color: #fff; /* Белый цвет для выбранной категории */
}

.submenu-item-link-active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: #F47427;
}

.dropdown-menu-img {
  width: 500px;
  height: 445px;
  margin-left: 80px;
  object-fit: cover;
  object-position: center;
  border-radius: 10px;
}

@media screen and (max-width: 1280px) {
  .dropdown-menu-wrap {
    flex-direction: column;
    align-items: center;
  }
  
  .dropdown-menu-img {
    margin-left: 0;
    margin-top: 30px;
    width: 100%;
    max-width: 500px;
  }
}

/* Анимация появления подменю */
.dropdown-menu-enter-active,
.dropdown-menu-leave-active {
  transition: transform 200ms ease-out, opacity 200ms ease-out;
}

.dropdown-menu-enter-from {
  transform: translateY(-20px);
  opacity: 0;
}

.dropdown-menu-leave-to {
  opacity: 0;
}

/* Оверлей поиска (Teleport to body — стили без scoped для глобального оверлея) */
</style>

<style>
.search-overlay {
  position: fixed;
  inset: 0;
  z-index: 10002;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 80px 16px 24px;
  overflow-y: auto;
}

.search-overlay-panel {
  width: 100%;
  max-width: 560px;
  background: #2E2826;
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
  overflow: hidden;
}

.search-overlay-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.search-overlay-input {
  flex: 1;
  padding: 12px 16px;
  font-size: 16px;
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  outline: none;
  font-family: inherit;
}

.search-overlay-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.search-overlay-input:focus {
  border-color: #F47427;
  background: rgba(255, 255, 255, 0.1);
}

.search-overlay-close {
  padding: 8px;
  color: #fff;
  background: transparent;
  border: none;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.2s;
}

.search-overlay-close:hover {
  background: rgba(255, 255, 255, 0.1);
}

.search-overlay-results {
  max-height: 60vh;
  overflow-y: auto;
  padding: 12px;
}

.search-overlay-hint,
.search-overlay-empty {
  padding: 24px;
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

.search-overlay-all-link-wrap {
  padding: 12px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 8px;
}

.search-overlay-all-link {
  display: inline-block;
  color: #F47427;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: opacity 0.2s;
}

.search-overlay-all-link:hover {
  opacity: 0.9;
}

.search-results-group-title {
  padding: 8px 12px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: rgba(255, 255, 255, 0.6);
}

.search-result-item {
  display: block;
  padding: 10px 12px;
  border-radius: 8px;
  color: #fff;
  text-decoration: none;
  transition: background 0.2s;
}

.search-result-item:hover {
  background: rgba(255, 255, 255, 0.08);
}

.search-result-category {
  font-size: 15px;
}

.search-result-product {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-result-product-img {
  width: 48px;
  height: 48px;
  object-fit: contain;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 6px;
  flex-shrink: 0;
}

.search-result-product-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.search-result-product-name {
  font-weight: 500;
  font-size: 15px;
}

.search-result-product-category {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.search-result-product-price {
  font-size: 13px;
  color: #F47427;
}

.search-overlay-enter-active,
.search-overlay-leave-active {
  transition: opacity 0.2s ease;
}

.search-overlay-enter-from,
.search-overlay-leave-to {
  opacity: 0;
}
</style>
