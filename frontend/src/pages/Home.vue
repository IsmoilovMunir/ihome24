<template>
  <div class="home">
    <div class="container">
      <section class="hero">
        <h1>Добро пожаловать в наш магазин!</h1>
        <p>Лучшие товары по выгодным ценам</p>
        <router-link to="/products" class="btn btn-primary">
          Перейти в каталог
        </router-link>
      </section>

      <section class="featured-products" v-if="products.length > 0">
        <h2>Популярные товары</h2>
        <div class="grid grid-3">
          <ProductCard 
            v-for="product in featuredProducts" 
            :key="product.id" 
            :product="product"
            @click="goToProduct(product.id)"
          />
        </div>
      </section>

      <div v-if="loading" class="loading">
        Загрузка товаров...
      </div>

      <div v-if="error" class="error">
        {{ error }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useProductsStore } from '../store/products'
import ProductCard from '../components/ProductCard.vue'

const router = useRouter()
const productsStore = useProductsStore()

const { products, loading, error } = productsStore

const featuredProducts = computed(() => {
  return products.slice(0, 6)
})

const goToProduct = (id) => {
  router.push(`/products/${id}`)
}

onMounted(() => {
  productsStore.fetchProducts()
})
</script>

<style scoped>
.home {
  padding: 2rem 0;
}

.hero {
  text-align: center;
  padding: 4rem 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 8px;
  margin-bottom: 3rem;
}

.hero h1 {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.hero p {
  font-size: 1.25rem;
  margin-bottom: 2rem;
}

.featured-products {
  margin-top: 3rem;
}

.featured-products h2 {
  font-size: 2rem;
  margin-bottom: 2rem;
  text-align: center;
}
</style>
