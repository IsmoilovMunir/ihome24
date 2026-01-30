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
  </header>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { useAuthStore } from '../stores/auth'
import { useProductsStore } from '../stores/products'
import { fileApi } from '../services/api'

const route = useRoute()
const cartStore = useCartStore()
const authStore = useAuthStore()
const productsStore = useProductsStore()

const visibleSubmenu = ref(null)
const submenuTimeout = ref(null)

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

const toggleSearch = () => {
  // TODO: Реализовать поиск
  console.log('Search clicked')
}

onMounted(async () => {
  await productsStore.fetchCategories()
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
  background: rgba(46, 40, 38, 0.7);
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
  background: rgba(46, 40, 38, 0.7);
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
  background: rgba(46, 40, 38, 0.7);
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
</style>
