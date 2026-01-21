<template>
  <div class="products-page">
    <div class="container">
      <div class="page-header">
        <h1>Каталог товаров</h1>
        <div class="search-box">
          <input 
            type="text" 
            v-model="searchQuery" 
            placeholder="Поиск товаров..."
            @input="handleSearch"
            class="search-input"
          />
        </div>
      </div>

      <div v-if="loading" class="loading">
        Загрузка товаров...
      </div>

      <div v-if="error" class="error">
        {{ error }}
      </div>

      <div v-if="!loading && !error && products.length === 0" class="no-products">
        <p>Товары не найдены</p>
      </div>

      <div v-if="!loading && products.length > 0" class="products-grid grid grid-3">
        <ProductCard 
          v-for="product in products" 
          :key="product.id" 
          :product="product"
          @click="goToProduct(product.id)"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useProductsStore } from '../store/products'
import ProductCard from '../components/ProductCard.vue'

const router = useRouter()
const productsStore = useProductsStore()

const { products, loading, error } = productsStore
const searchQuery = ref('')

const goToProduct = (id) => {
  router.push(`/products/${id}`)
}

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    productsStore.searchProducts(searchQuery.value)
  } else {
    productsStore.fetchProducts()
  }
}

onMounted(() => {
  productsStore.fetchProducts()
})
</script>

<style scoped>
.products-page {
  padding: 2rem 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.page-header h1 {
  font-size: 2rem;
}

.search-box {
  flex: 1;
  max-width: 400px;
}

.search-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.no-products {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.products-grid {
  margin-top: 2rem;
}
</style>
