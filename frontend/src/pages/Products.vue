<template>
  <div class="container mx-auto px-4 py-8 bg-[#3A3331]">
    <div class="flex flex-col md:flex-row gap-8">
      <!-- Sidebar -->
      <aside class="w-full md:w-64">
        <div class="bg-white rounded-lg shadow-md p-6 sticky top-20">
          <h3 class="text-lg font-semibold mb-4 text-gray-900">Категории</h3>
          <ul class="space-y-2">
            <li>
              <router-link
                to="/products"
                class="block text-gray-700 hover:text-primary-600 transition-colors"
                :class="{ 'text-primary-600 font-semibold': !selectedCategoryId }"
              >
                Все товары
              </router-link>
            </li>
            <li
              v-for="category in productsStore.categories"
              :key="category.id"
            >
              <router-link
                :to="`/products?category=${category.id}`"
                class="block text-gray-700 hover:text-primary-600 transition-colors"
                :class="{ 'text-primary-600 font-semibold': selectedCategoryId === category.id }"
              >
                {{ category.name }}
              </router-link>
            </li>
          </ul>
        </div>
      </aside>

      <!-- Products Grid -->
      <main class="flex-1">
        <div class="mb-6">
          <h1 class="text-3xl font-bold text-white mb-2">Каталог товаров</h1>
          <p v-if="selectedCategory" class="text-gray-300">
            Категория: {{ selectedCategory.name }}
          </p>
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
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useProductsStore } from '../stores/products'
import ProductCard from '../components/ProductCard.vue'

const route = useRoute()
const productsStore = useProductsStore()

const selectedCategoryId = computed(() => {
  const categoryId = route.query.category
  return categoryId ? Number(categoryId) : null
})

const selectedCategory = computed(() => {
  if (!selectedCategoryId.value) return null
  return productsStore.categories.find(c => c.id === selectedCategoryId.value)
})

const filteredProducts = computed(() => {
  if (!selectedCategoryId.value) {
    return productsStore.products
  }
  return productsStore.productsByCategory(selectedCategoryId.value)
})

onMounted(async () => {
  await productsStore.fetchProducts()
  await productsStore.fetchCategories()
})
</script>
