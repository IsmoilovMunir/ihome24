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
          <!-- Корзина (скрыто на мобильных — есть в нижнем меню) -->
          <li class="header-nav-item header-nav-item--hide-mobile">
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
          
          <!-- Аккаунт (скрыто на мобильных — есть в нижнем меню) -->
          <li class="header-nav-item header-nav-item--account header-nav-item--hide-mobile" ref="accountNavRef">
            <button
              type="button"
              class="header-nav-link"
              aria-haspopup="true"
              :aria-expanded="personalMenuStore.open"
              @click="personalMenuStore.toggle"
            >
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
            </button>
          </li>
        </ul>
      </nav>
    </div>

    <!-- Меню личного кабинета (Teleport — вне скрытого элемента, работает на мобильных) -->
    <Teleport to="body">
      <Transition name="personal-menu">
        <div
          v-if="personalMenuStore.open"
          class="personal-menu-wrapper"
          @click.self="personalMenuStore.closeMenu"
        >
          <div class="personal-menu personal-menu--shared" @click.stop>
            <button
              type="button"
              class="personal-menu__close-mobile"
              aria-label="Закрыть"
              @click="personalMenuStore.closeMenu"
            >
              Закрыть
            </button>
            <div class="personal-menu__items">
                  <!-- Город -->
                  <div class="personal-menu__location personal-menu__item">
                    <svg class="personal-menu__icon" width="24" height="24" fill="none" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                      <path d="M12 13.43a3.12 3.12 0 1 0 0-6.24 3.12 3.12 0 0 0 0 6.24z" fill="currentColor" stroke="currentColor" stroke-width="1.5"/>
                      <path d="M3.62 8.49c1.97-8.66 14.8-8.65 16.76.01 1.15 5.08-2.01 9.38-4.78 12.04a5.193 5.193 0 0 1-7.21 0c-2.76-2.66-5.92-6.97-4.77-12.05z" stroke="currentColor" stroke-width="1.5"/>
                    </svg>
                    <button
                      type="button"
                      class="personal-menu__location-name personal-menu__location-btn"
                      @click="openCityPicker"
                    >
                      {{ locationLoading ? 'Определение...' : (locationCity || 'Москва') }}
                    </button>
                  </div>

                  <!-- Кнопка входа (если не авторизован) -->
                  <router-link
                    v-if="!authStore.isAuthenticated"
                    to="/login"
                    class="personal-menu__auth-btn"
                    @click="personalMenuStore.closeMenu"
                  >
                    Вход или регистрация
                  </router-link>

                  <!-- Пункты меню (при клике проверяется авторизация) -->
                  <router-link to="/personal" class="personal-menu__item personal-menu__link" @click="handlePersonalMenuClick">
                    <svg class="personal-menu__icon" width="24" height="24" fill="none" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                      <path d="m9.268 3.603-4.94 3.85c-.825.642-1.495 2.008-1.495 3.044v6.792c0 2.127 1.733 3.869 3.86 3.869h10.614a3.871 3.871 0 0 0 3.86-3.86v-6.673c0-1.11-.743-2.53-1.65-3.162l-5.665-3.97c-1.284-.898-3.346-.852-4.584.11zM12 17.49v-2.75" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Личный кабинет
                  </router-link>
                  <router-link to="/personal/orders" class="personal-menu__item personal-menu__link" @click="handlePersonalMenuClick">
                    <svg class="personal-menu__icon" width="24" height="24" fill="none" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                      <path d="M9.5 22h6c4.02 0 4.74-1.61 4.95-3.57l.75-6C21.47 9.99 20.77 8 16.5 8h-8c-4.27 0-4.97 1.99-4.7 4.43l.75 6C4.76 20.39 5.48 22 9.5 22zM8 7.67V6.7c0-2.25 1.81-4.46 4.06-4.67A4.5 4.5 0 0 1 17 6.51v1.38" stroke="currentColor" stroke-width="1.5" stroke-miterlimit="10" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Заказы
                  </router-link>
                  <router-link to="/personal/favorites" class="personal-menu__item personal-menu__link" @click="handlePersonalMenuClick">
                    <svg class="personal-menu__icon" width="24" height="24" fill="none" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                      <path d="M12.62 20.81C12.28 20.93 11.72 20.93 11.38 20.81C8.48 19.82 2 15.69 2 8.68998C2 5.59998 4.49 3.09998 7.56 3.09998C9.38 3.09998 10.99 3.97998 12 5.33998C13.01 3.97998 14.63 3.09998 16.44 3.09998C19.51 3.09998 22 5.59998 22 8.68998C22 15.69 15.52 19.82 12.62 20.81Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Избранное
                  </router-link>
                  <router-link to="/personal/profile" class="personal-menu__item personal-menu__link" @click="handlePersonalMenuClick">
                    <svg class="personal-menu__icon" width="24" height="24" fill="none" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                      <path d="M12.5 12a5 5 0 1 0 0-10 5 5 0 0 0 0 10zM21.09 22c0-3.87-3.85-7-8.59-7s-8.59 3.13-8.59 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Личные данные
                  </router-link>

                  <!-- Где мой заказ? -->
                  <router-link to="/order-tracking" class="personal-menu__item personal-menu__link" @click="handlePersonalMenuClick">
                    <svg class="personal-menu__icon" width="24" height="24" fill="none" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 25 24">
                      <path d="M12.2366 19.5C16.3787 19.5 19.7366 16.1421 19.7366 12C19.7366 7.85786 16.3787 4.5 12.2366 4.5C8.09443 4.5 4.73657 7.85786 4.73657 12C4.73657 16.1421 8.09443 19.5 12.2366 19.5Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                      <path d="M12.2366 15C13.8935 15 15.2366 13.6569 15.2366 12C15.2366 10.3431 13.8935 9 12.2366 9C10.5797 9 9.23657 10.3431 9.23657 12C9.23657 13.6569 10.5797 15 12.2366 15Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                      <path d="M12.2366 4V2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                      <path d="M4.23657 12H2.23657" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                      <path d="M12.2366 20V22" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                      <path d="M20.2366 12H22.2366" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Где мой заказ?
                  </router-link>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
    
    <!-- Нижний слой: горизонтальное меню (скрыто на странице корзины) -->
    <div v-if="route.path !== '/cart'" class="header-menu">
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
    
    <!-- Большое подменю с картинками (скрыто на странице корзины) -->
    <transition name="dropdown-menu">
      <div
        v-if="route.path !== '/cart' && visibleSubmenu && getCurrentCategoryData()"
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

    <!-- Модальное окно выбора города -->
    <Teleport to="body">
      <Transition name="city-picker">
        <div
          v-if="cityPickerOpen"
          class="city-picker-overlay"
          @click.self="closeCityPicker"
        >
          <div class="city-picker-modal">
            <div class="city-picker__header">
              <h3 class="city-picker__title">Укажите ваш город</h3>
              <p class="city-picker__info">
                Правильный выбор города влияет на наличие и условия доставки товаров
              </p>
            </div>
            <div class="city-picker__input-wrap">
              <input
                ref="citySearchInputRef"
                v-model="citySearchQuery"
                type="text"
                class="city-picker__input"
                placeholder="Ваш город"
                @keydown.esc="closeCityPicker"
              />
              <svg class="city-picker__input-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M11.5833 19.5C15.9555 19.5 19.5 15.9556 19.5 11.5833C19.5 7.21108 15.9555 3.66666 11.5833 3.66666C7.21104 3.66666 3.66663 7.21108 3.66663 11.5833C3.66663 15.9556 7.21104 19.5 11.5833 19.5Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M20.5001 20.5L19 18.9997" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div v-if="locationCity" class="city-picker__chosen">
              <span class="city-picker__chosen-label">{{ locationCity }}</span>
              <svg width="21" height="21" viewBox="0 0 21 21" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M17.1094 6.5L10.0458 13.9295C9.32254 14.6902 8.13901 14.6902 7.41575 13.9295L4.10937 10.4518" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <ul class="city-picker__list">
              <li
                v-for="city in filteredCities"
                :key="city"
                class="city-picker__item"
              >
                <button
                  type="button"
                  class="city-picker__link"
                  :class="{ 'city-picker__link--chosen': city === locationCity }"
                  @click="selectCity(city)"
                >
                  {{ city }}
                </button>
              </li>
            </ul>
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
import { usePersonalMenuStore } from '../stores/personalMenu'
import { fileApi, geoApi } from '../services/api'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const authStore = useAuthStore()
const productsStore = useProductsStore()
const personalMenuStore = usePersonalMenuStore()

const visibleSubmenu = ref(null)
const submenuTimeout = ref(null)

const accountNavRef = ref(null)

const locationCity = ref('')
const locationLoading = ref(true)

const cityPickerOpen = ref(false)
const citySearchQuery = ref('')
const citySearchInputRef = ref(null)

const CITIES = [
  'Белгород', 'Брянск', 'Владимир', 'Вологда', 'Екатеринбург', 'Казань', 'Калуга',
  'Краснодар', 'Москва', 'Нижний Новгород', 'Новомосковск', 'Новосибирск', 'Ногинск',
  'Обнинск', 'Орёл', 'Ростов-на-Дону', 'Санкт-Петербург', 'Сергиев Посад', 'Сочи', 'Тула'
]

/** Маппинг EN→RU для городов от ipapi.co */
const CITY_EN_RU = {
  Belgorod: 'Белгород', Bryansk: 'Брянск', Vladimir: 'Владимир', Vologda: 'Вологда',
  Yekaterinburg: 'Екатеринбург', Kazan: 'Казань', Kaluga: 'Калуга', Krasnodar: 'Краснодар',
  Moscow: 'Москва', 'Nizhny Novgorod': 'Нижний Новгород', Novosibirsk: 'Новосибирск',
  Noginsk: 'Ногинск', Obninsk: 'Обнинск', Oryol: 'Орёл', 'Rostov-on-Don': 'Ростов-на-Дону',
  'Saint Petersburg': 'Санкт-Петербург', 'St Petersburg': 'Санкт-Петербург',
  'Sergiev Posad': 'Сергиев Посад', Sochi: 'Сочи', Tula: 'Тула'
}

const normalizeCity = (city) => (city && CITY_EN_RU[city]) || city

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

const filteredCities = computed(() => {
  const q = citySearchQuery.value.trim().toLowerCase()
  if (!q) return CITIES
  return CITIES.filter((c) => c.toLowerCase().includes(q))
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
  if (e.key === 'Escape' && personalMenuStore.open) personalMenuStore.closeMenu()
  if (e.key === 'Escape' && cityPickerOpen.value) closeCityPicker()
}

const openCityPicker = async () => {
  cityPickerOpen.value = true
  citySearchQuery.value = ''
  await nextTick()
  citySearchInputRef.value?.focus()
}

const closeCityPicker = () => {
  cityPickerOpen.value = false
  citySearchQuery.value = ''
}

/** При клике на пункт меню: если не авторизован — редирект на /login */
const handlePersonalMenuClick = (e) => {
  if (!authStore.isAuthenticated) {
    e.preventDefault()
    router.push('/login')
  }
  personalMenuStore.closeMenu()
}

const selectCity = (city) => {
  locationCity.value = city
  localStorage.setItem('ihome24_location_city', city)
  closeCityPicker()
}

const handleClickOutside = (e) => {
  if (!personalMenuStore.open) return
  const target = e.target
  if (accountNavRef.value?.contains(target)) return
  if (target?.closest('.mobile-bottom-menu')) return
  if (target?.closest('.personal-menu-wrapper')) return
  personalMenuStore.closeMenu()
}

/** JSONP для ip-api.com (обходит CORS, работает без бэкенда на localhost) */
const fetchLocationViaJsonp = () => new Promise((resolve) => {
  let resolved = false
  const callbackName = `__ihome24GeoCallback_${Date.now()}`
  const cleanup = (result) => {
    if (resolved) return
    resolved = true
    clearTimeout(timeout)
    delete window[callbackName]
    script.remove()
    resolve(result)
  }
  const timeout = setTimeout(() => cleanup(null), 8000)
  window[callbackName] = (data) => cleanup(data?.status === 'success' ? data.city : null)
  const script = document.createElement('script')
  script.src = `http://ip-api.com/json/?lang=ru&fields=status,city&callback=${callbackName}`
  script.onerror = () => cleanup(null)
  document.body.appendChild(script)
})

const fetchLocation = async () => {
  const cached = localStorage.getItem('ihome24_location_city')
  if (cached) {
    locationCity.value = cached
    locationLoading.value = false
    return
  }
  let city = null

  // 1. ipapi.co (HTTPS, CORS) — часто работает с localhost
  try {
    const res = await fetch('https://ipapi.co/json/')
    const data = await res.json()
    city = normalizeCity(data?.city) || data?.city
  } catch {
    //
  }

  // 2. JSONP ip-api.com (HTTP localhost, обходит CORS)
  if (!city && typeof window !== 'undefined' && window.location?.protocol === 'http:') {
    city = await fetchLocationViaJsonp()
  }

  // 3. Бэкенд — через прокси (localhost:3000) или напрямую
  if (!city) {
    try {
      const res = await fetch('/api/geo/location')
      const data = await res.json()
      city = data?.city
    } catch {
      try {
        const res = await geoApi.getLocation()
        city = res.data?.city
      } catch {
        //
      }
    }
  }

  locationCity.value = city || 'Москва'
  if (city) {
    localStorage.setItem('ihome24_location_city', city)
  }
  locationLoading.value = false
}

onMounted(async () => {
  await productsStore.fetchCategories()
  fetchLocation()
  window.addEventListener('keydown', onKeydown)
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown)
  document.removeEventListener('click', handleClickOutside)
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
  position: relative;
  z-index: 10002; /* Выше header-menu, чтобы личное меню не перекрывалось */
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

/* Скрыть на мобильных (есть в нижнем меню) */
@media screen and (max-width: 768px) {
  .header-nav-item--hide-mobile {
    display: none;
  }
}

/* Выпадающее меню личного кабинета */
.header-nav-item--account {
  position: relative;
}

.personal-menu-wrapper {
  position: fixed;
  inset: 0;
  z-index: 10003;
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  padding: 60px 16px 16px;
}

@media screen and (max-width: 768px) {
  .personal-menu-wrapper {
    align-items: flex-end;
    padding: 0;
    background: rgba(0, 0, 0, 0.5);
  }
}

.personal-menu {
  position: absolute;
  top: 0;
  right: 0;
  min-width: 320px;
  max-width: 360px;
  max-height: 85vh;
  overflow-y: auto;
  background: #2E2826;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4), 0 0 0 1px rgba(255, 255, 255, 0.06);
  padding: 12px 0;
}

.personal-menu__close-mobile {
  display: none;
}

@media screen and (max-width: 768px) {
  .personal-menu__close-mobile {
    display: block;
    width: 100%;
    padding: 12px 20px;
    background: none;
    border: none;
    color: #F47427;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    text-align: center;
  }
  .personal-menu.personal-menu--shared {
    position: relative;
    top: auto;
    right: auto;
    min-width: 100%;
    max-width: 100%;
    max-height: 85vh;
    border-radius: 16px 16px 0 0;
    padding: 0 0 40px;
  }
}

.personal-menu__items {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.personal-menu__item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  font-family: "helvetica", sans-serif;
  text-decoration: none;
  transition: background 0.2s, color 0.2s;
}

.personal-menu__item:hover {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}

.personal-menu__location {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 4px;
  padding-bottom: 12px;
}

.personal-menu__icon {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  color: rgba(255, 255, 255, 0.8);
}

.personal-menu__location-name {
  flex: 1;
  font-weight: 500;
}

.personal-menu__location-btn {
  background: none;
  border: none;
  color: inherit;
  font: inherit;
  cursor: pointer;
  padding: 0;
  text-align: left;
  transition: color 0.2s;
}

.personal-menu__location-btn:hover {
  color: #F47427;
}

.personal-menu__auth-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  margin: 8px 0 12px;
  padding: 12px 20px;
  text-align: center;
  background: #F47427;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  font-family: "helvetica", sans-serif;
  text-decoration: none;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s, transform 0.1s;
}

.personal-menu__auth-btn:hover {
  background: #e66a1f;
}

.personal-menu__link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  font-family: "helvetica", sans-serif;
  text-decoration: none;
  transition: background 0.2s, color 0.2s;
}

.personal-menu__link:hover {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}

.personal-menu__link:hover .personal-menu__icon {
  color: #F47427;
}

.personal-menu__link.router-link-active {
  background: rgba(244, 116, 39, 0.1);
  color: #F47427;
}

.personal-menu__link.router-link-active .personal-menu__icon {
  color: #F47427;
}

.personal-menu__badge {
  margin-left: auto;
  padding: 4px 10px;
  background: rgba(59, 130, 246, 0.2);
  color: #60a5fa;
  font-size: 12px;
  font-weight: 600;
  border-radius: 8px;
}

/* Анимация personal menu */
.personal-menu-enter-active,
.personal-menu-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.personal-menu-enter-from,
.personal-menu-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media screen and (max-width: 768px) {
  .personal-menu-enter-active .personal-menu,
  .personal-menu-leave-active .personal-menu {
    transition: transform 0.3s ease;
  }
  .personal-menu-enter-from .personal-menu,
  .personal-menu-leave-to .personal-menu {
    transform: translateY(100%);
  }
}

.header-menu {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
  z-index: 10001; /* Ниже header-top, чтобы личное меню было поверх */
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
  font-size: 12px;
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

/* City picker modal */
.city-picker-overlay {
  position: fixed;
  inset: 0;
  z-index: 10004;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 80px 16px 24px;
  overflow-y: auto;
}

.city-picker-modal {
  width: 100%;
  max-width: 480px;
  background: #2E2826;
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
  padding: 24px;
}

.city-picker__header {
  margin-bottom: 20px;
}

.city-picker__title {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  font-family: "helvetica", sans-serif;
}

.city-picker__info {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.4;
}

.city-picker__input-wrap {
  position: relative;
  margin-bottom: 16px;
}

.city-picker__input {
  width: 100%;
  padding: 12px 16px 12px 44px;
  font-size: 16px;
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  outline: none;
  font-family: inherit;
}

.city-picker__input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.city-picker__input:focus {
  border-color: #F47427;
  background: rgba(255, 255, 255, 0.1);
}

.city-picker__input-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: rgba(255, 255, 255, 0.5);
  pointer-events: none;
}

.city-picker__chosen {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  margin-bottom: 12px;
  background: rgba(244, 116, 39, 0.15);
  border-radius: 8px;
  color: #F47427;
}

.city-picker__chosen-label {
  font-weight: 500;
}

.city-picker__list {
  list-style: none;
  margin: 0;
  padding: 0;
  max-height: 280px;
  overflow-y: auto;
}

.city-picker__item {
  margin: 0;
}

.city-picker__link {
  display: block;
  width: 100%;
  padding: 12px 16px;
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.9);
  font-size: 15px;
  font-family: inherit;
  text-align: left;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  border-radius: 8px;
}

.city-picker__link:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.city-picker__link--chosen {
  color: #F47427;
  font-weight: 500;
}

.city-picker-enter-active,
.city-picker-leave-active {
  transition: opacity 0.2s ease;
}

.city-picker-enter-from,
.city-picker-leave-to {
  opacity: 0;
}
</style>
