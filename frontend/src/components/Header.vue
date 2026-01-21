<template>
  <header class="header">
    <div class="container">
      <div class="header-content">
        <router-link to="/" class="logo">
          <h1>Магазин</h1>
        </router-link>
        <nav class="nav">
          <router-link to="/" class="nav-link">Главная</router-link>
          <router-link to="/products" class="nav-link">Каталог</router-link>
          <template v-for="category in parentCategories" :key="category.id">
            <div 
              v-if="getChildCategories(category.id).length > 0"
              class="nav-item-dropdown"
              @mouseenter="hoveredCategoryId = category.id"
              @mouseleave="hoveredCategoryId = null"
            >
              <router-link 
                :to="`/products?category=${category.id}`" 
                class="nav-link"
              >
                {{ category.name }}
                <span class="dropdown-arrow">▼</span>
              </router-link>
              <div 
                v-if="hoveredCategoryId === category.id"
                class="dropdown-menu"
              >
                <router-link
                  v-for="child in getChildCategories(category.id)"
                  :key="child.id"
                  :to="`/products?category=${child.id}`"
                  class="dropdown-item"
                  @click="hoveredCategoryId = null"
                >
                  {{ child.name }}
                </router-link>
              </div>
            </div>
            <router-link 
              v-else
              :key="`link-${category.id}`"
              :to="`/products?category=${category.id}`" 
              class="nav-link"
            >
              {{ category.name }}
            </router-link>
          </template>
        </nav>
        <div class="cart-icon">
          <router-link to="/cart" class="cart-link">
            <span class="cart-badge" v-if="cartStore.totalItems > 0">
              {{ cartStore.totalItems }}
            </span>
            🛒 Корзина
          </router-link>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useCartStore } from '../store/cart'
import { categoriesAPI } from '../services/api'

const cartStore = useCartStore()
const categories = ref([])
const loading = ref(false)
const hoveredCategoryId = ref(null)

const parentCategories = computed(() => {
  // Фильтруем только родительские категории (без parentId) и активные
  const filtered = categories.value.filter(cat => {
    const isParent = !cat.parentId || cat.parentId === null || cat.parentId === undefined
    const isActive = cat.isActive !== false
    return isParent && isActive
  })
  console.log('All categories:', categories.value)
  console.log('Parent categories:', filtered)
  return filtered
})

const getChildCategories = (parentId) => {
  // Получаем дочерние категории для родительской категории
  return categories.value.filter(cat => {
    return cat.parentId === parentId && (cat.isActive !== false)
  })
}

const fetchCategories = async () => {
  loading.value = true
  try {
    const response = await categoriesAPI.getAll()
    categories.value = response.data || []
    console.log('Categories loaded:', categories.value)
  } catch (error) {
    console.error('Error fetching categories:', error)
    console.error('Error details:', error.response?.data || error.message)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.header {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 1rem 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  text-decoration: none;
  color: #333;
}

.logo h1 {
  font-size: 1.5rem;
  font-weight: bold;
}

.nav {
  display: flex;
  gap: 2rem;
  flex-wrap: wrap;
  align-items: center;
}

.nav-link {
  text-decoration: none;
  color: #333;
  font-weight: 500;
  transition: color 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.nav-link:hover,
.nav-link.router-link-active {
  color: #007bff;
}

.dropdown-arrow {
  font-size: 0.7rem;
  transition: transform 0.3s ease;
}

.nav-item-dropdown {
  position: relative;
  display: inline-block;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  background: white;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  border-radius: 4px;
  min-width: 200px;
  padding: 0.5rem 0;
  margin-top: 0.5rem;
  z-index: 1000;
  animation: fadeInDown 0.2s ease;
}

.dropdown-item {
  display: block;
  padding: 0.75rem 1.5rem;
  text-decoration: none;
  color: #333;
  transition: background-color 0.2s ease;
}

.dropdown-item:hover {
  background-color: #f5f5f5;
  color: #007bff;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.cart-icon {
  position: relative;
}

.cart-link {
  text-decoration: none;
  color: #333;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.cart-badge {
  background-color: #007bff;
  color: white;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: bold;
}
</style>
