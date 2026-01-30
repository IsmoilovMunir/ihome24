<template>
  <div>
    <!-- Hero Banner -->
    <section class="home-hero-banner">
      <div class="home-hero-banner-bg" aria-hidden="true"></div>
      <div class="home-hero-banner-overlay"></div>
      <div class="home-hero-banner-content">
        <div class="container mx-auto px-4 text-center relative z-10">
          <h1 class="home-hero-banner-title">Добро пожаловать в iHome24</h1>
          <p class="home-hero-banner-subtitle">Качественные товары для дома и офиса</p>
          <router-link
            to="/products"
            class="home-hero-banner-cta"
          >
            Перейти в каталог
          </router-link>
        </div>
      </div>
    </section>

    <!-- Popular Products and Collections -->
    <section class="py-12 bg-[#3A3331]">
      <div class="container mx-auto px-4">
        <h2 class="text-3xl font-bold text-white mb-8">
          {{ productsStore.featuredProducts.length > 0 ? 'Хиты продаж' : 'Популярные товары' }}
        </h2>
        <div v-if="productsStore.loading" class="text-center py-12">
          <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
        </div>
        <div v-else-if="productsStore.error" class="text-center py-12 text-red-400">
          Ошибка загрузки товаров: {{ productsStore.error }}
        </div>
        <div v-else-if="featuredProducts.length === 0 && categories.length === 0" class="text-center py-12 text-gray-400">
          Товары не найдены
        </div>
        <div v-else>
          <!-- Desktop Grid: только товары, если нет категорий с картинкой -->
          <div
            v-if="categories.length === 0"
            class="products-collections-grid desktop-grid desktop-grid-products-only"
          >
            <div
              v-for="product in featuredProducts.slice(0, 40)"
              :key="product.id"
              class="product-item"
            >
              <ProductCard :product="product" />
            </div>
            <div
              v-for="product in featuredProducts.slice(40)"
              :key="product.id"
              class="product-item"
            >
              <ProductCard :product="product" />
            </div>
          </div>
          <!-- Desktop Grid: коллекции + товары -->
          <div
            v-else
            class="products-collections-grid desktop-grid"
          >
            <template v-for="cycle in 5" :key="cycle">
              <!-- Индексы для текущего цикла -->
              <template v-if="(cycle - 1) * 2 < categories.length">
                <!-- РЯД 1: Коллекция слева на первом ряду цикла (колонки 1-2) -->
                <div
                  :class="`collection-item collection-cycle-${cycle} collection-left`"
                  :style="getCollectionLeftStyle(cycle)"
                >
                  <CollectionCard :collection="categories[(cycle - 1) * 2]" />
                </div>
                
                <!-- РЯД 1: 2 товара справа на первом ряду цикла (колонки 3-4) -->
                <template v-for="idx in [0, 1]" :key="`cycle-${cycle}-product-${idx}`">
                  <div
                    v-if="(cycle - 1) * 8 + idx < featuredProducts.length"
                    :class="`product-item product-cycle-${cycle}`"
                    :style="getProductRightStyle(cycle, idx)"
                  >
                    <ProductCard :product="featuredProducts[(cycle - 1) * 8 + idx]" />
                  </div>
                </template>
                
                <!-- РЯД 2: 2 товара слева на втором ряду цикла (колонки 1-2) -->
                <template v-for="idx in [0, 1]" :key="`cycle-${cycle}-product-left-${idx}`">
                  <div
                    v-if="(cycle - 1) * 8 + 2 + idx < featuredProducts.length"
                    :class="`product-item product-cycle-${cycle}-left`"
                    :style="getProductLeftStyle(cycle, idx)"
                  >
                    <ProductCard :product="featuredProducts[(cycle - 1) * 8 + 2 + idx]" />
                  </div>
                </template>
                
                <!-- РЯД 2: Коллекция справа на втором ряду цикла (колонки 3-4) -->
                <div
                  v-if="(cycle - 1) * 2 + 1 < categories.length"
                  :class="`collection-item collection-cycle-${cycle} collection-right`"
                  :style="getCollectionRightStyle(cycle)"
                >
                  <CollectionCard :collection="categories[(cycle - 1) * 2 + 1]" />
                </div>
                
                <!-- РЯД 3: 4 товара в один ряд после цикла -->
                <template v-for="idx in [0, 1, 2, 3]" :key="`cycle-${cycle}-product-row3-${idx}`">
                  <div
                    v-if="(cycle - 1) * 8 + 4 + idx < featuredProducts.length"
                    :class="`product-item product-cycle-${cycle}-row3`"
                    :style="getProductRow3Style(cycle, idx)"
                  >
                    <ProductCard :product="featuredProducts[(cycle - 1) * 8 + 4 + idx]" />
                  </div>
                </template>
              </template>
            </template>
            
            <!-- Остальные товары после всех циклов -->
            <div
              v-for="product in featuredProducts.slice(40)"
              :key="product.id"
              class="product-item"
            >
              <ProductCard :product="product" />
            </div>
          </div>
          
          <!-- Mobile Layout: повторяющийся паттерн -->
          <div class="mobile-layout">
            <template v-for="cycle in 5" :key="cycle">
              <!-- Коллекция 1 -->
              <div
                v-if="(cycle - 1) * 2 < categories.length"
                class="mobile-collection"
              >
                <CollectionCard :collection="categories[(cycle - 1) * 2]" />
              </div>
              
              <!-- 4 товара после коллекции 1 -->
              <div class="mobile-products-grid">
                <template v-for="idx in [0, 1, 2, 3]" :key="`mobile-cycle-${cycle}-product-1-${idx}`">
                  <div
                    v-if="(cycle - 1) * 8 + idx < featuredProducts.length"
                    class="mobile-product-item"
                  >
                    <ProductCard :product="featuredProducts[(cycle - 1) * 8 + idx]" />
                  </div>
                </template>
              </div>
              
              <!-- Коллекция 2 (опционально) -->
              <div
                v-if="(cycle - 1) * 2 + 1 < categories.length"
                class="mobile-collection"
              >
                <CollectionCard :collection="categories[(cycle - 1) * 2 + 1]" />
              </div>
              
              <!-- 4 товара после коллекции 2 -->
              <div class="mobile-products-grid">
                <template v-for="idx in [0, 1, 2, 3]" :key="`mobile-cycle-${cycle}-product-2-${idx}`">
                  <div
                    v-if="(cycle - 1) * 8 + 4 + idx < featuredProducts.length"
                    class="mobile-product-item"
                  >
                    <ProductCard :product="featuredProducts[(cycle - 1) * 8 + 4 + idx]" />
                  </div>
                </template>
              </div>
            </template>
            
            <!-- Остальные товары после всех циклов -->
            <div v-if="featuredProducts.length > 40" class="mobile-products-grid">
              <div
                v-for="product in featuredProducts.slice(40)"
                :key="product.id"
                class="mobile-product-item"
              >
                <ProductCard :product="product" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useProductsStore } from '../stores/products'
import ProductCard from '../components/ProductCard.vue'
import CollectionCard from '../components/CollectionCard.vue'

const productsStore = useProductsStore()

// Функции для вычисления стилей grid позиций
const getCollectionLeftStyle = (cycle) => {
  // Каждый цикл занимает 3 ряда
  const row = (cycle - 1) * 3 + 1
  return {
    gridColumn: '1 / 3',
    gridRow: row.toString()
  }
}

const getProductRightStyle = (cycle, idx) => {
  // Первый ряд цикла
  const row = (cycle - 1) * 3 + 1
  const col = idx === 0 ? 3 : 4
  return {
    gridColumn: col.toString(),
    gridRow: row.toString()
  }
}

const getProductLeftStyle = (cycle, idx) => {
  // Второй ряд цикла
  const row = (cycle - 1) * 3 + 2
  const col = idx === 0 ? 1 : 2
  return {
    gridColumn: col.toString(),
    gridRow: row.toString()
  }
}

const getCollectionRightStyle = (cycle) => {
  // Второй ряд цикла
  const row = (cycle - 1) * 3 + 2
  return {
    gridColumn: '3 / 5',
    gridRow: row.toString()
  }
}

const getProductRow3Style = (cycle, idx) => {
  // Третий ряд цикла - 4 товара в ряд
  const row = (cycle - 1) * 3 + 3
  const col = idx + 1
  return {
    gridColumn: col.toString(),
    gridRow: row.toString()
  }
}

const featuredProducts = computed(() => {
  const featured = productsStore.featuredProducts
  if (featured.length > 0) {
    // Для 5 циклов нужно минимум 40 товаров (8 товаров на цикл: 4 в цикле + 4 после)
    return featured.slice(0, 40)
  }
  // Если нет featured продуктов, показываем первые 40 обычных
  return productsStore.products.slice(0, 40)
})
const categories = computed(() => productsStore.categories.filter(cat => cat.imageUrl).slice(0, 10))

onMounted(async () => {
  window.scrollTo(0, 0)
  document.documentElement.scrollTop = 0
  document.body.scrollTop = 0
  await productsStore.fetchProducts()
  await productsStore.fetchCategories()
})
</script>

<style scoped>
/* Hero Banner */
.home-hero-banner {
  position: relative;
  min-height: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.home-hero-banner-bg {
  position: absolute;
  inset: 0;
  background-image: url('https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=1600');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.home-hero-banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(58, 51, 49, 0.85) 0%, rgba(46, 40, 38, 0.75) 50%, rgba(244, 116, 39, 0.25) 100%);
  z-index: 1;
}

.home-hero-banner-content {
  position: relative;
  z-index: 2;
  width: 100%;
  padding: 60px 24px;
}

.home-hero-banner-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #fff;
  margin-bottom: 1rem;
  font-family: "helvetica", sans-serif;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
}

.home-hero-banner-subtitle {
  font-size: 1.25rem;
  color: rgba(255, 255, 255, 0.95);
  margin-bottom: 2rem;
  max-width: 480px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.5;
}

.home-hero-banner-cta {
  display: inline-block;
  padding: 14px 32px;
  font-size: 1rem;
  font-weight: 600;
  color: #2E2826;
  background: #fff;
  border-radius: 8px;
  text-decoration: none;
  transition: transform 0.2s, box-shadow 0.2s;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.home-hero-banner-cta:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.25);
}

@media (min-width: 768px) {
  .home-hero-banner {
    min-height: 500px;
  }
  .home-hero-banner-title {
    font-size: 3rem;
  }
  .home-hero-banner-subtitle {
    font-size: 1.35rem;
  }
}

@media (max-width: 640px) {
  .home-hero-banner {
    min-height: 360px;
  }
  .home-hero-banner-title {
    font-size: 1.75rem;
  }
  .home-hero-banner-subtitle {
    font-size: 1rem;
  }
}

/* Desktop Grid */
.products-collections-grid.desktop-grid,
.products-collections-grid.desktop-grid-products-only {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  grid-auto-rows: min-content;
}

.products-collections-grid.desktop-grid .collection-item {
  grid-column: span 2;
}

.products-collections-grid.desktop-grid .product-item,
.products-collections-grid.desktop-grid-products-only .product-item {
  grid-column: span 1;
}

/* Общие стили для коллекций и товаров в циклах */
.products-collections-grid.desktop-grid .collection-item {
  grid-column: span 2;
}

/* Mobile Layout - скрыт на desktop */
.mobile-layout {
  display: none;
}

/* Mobile Products Grid */
.mobile-products-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.mobile-collection {
  width: 100%;
  margin-bottom: 24px;
}

.mobile-product-item {
  width: 100%;
}

@media (max-width: 1024px) {
  .products-collections-grid.desktop-grid,
  .products-collections-grid.desktop-grid-products-only {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .products-collections-grid.desktop-grid .collection-item {
    grid-column: span 2;
  }
  
  .products-collections-grid.desktop-grid .product-item,
  .products-collections-grid.desktop-grid-products-only .product-item {
    grid-column: span 1;
  }
}

@media (max-width: 640px) {
  /* Скрываем desktop grid на мобильных */
  .products-collections-grid.desktop-grid,
  .products-collections-grid.desktop-grid-products-only {
    display: none !important;
  }
  
  /* Показываем mobile layout */
  .mobile-layout {
    display: block !important;
  }
  
  .mobile-collection {
    margin-bottom: 24px;
  }
  
  .mobile-products-grid {
    gap: 12px;
  }
}
</style>
