<template>
  <div>
    <!-- Hero Section -->
    <section class="bg-gradient-to-r from-primary-600 to-primary-800 text-white py-20">
      <div class="container mx-auto px-4 text-center">
        <h1 class="text-4xl md:text-5xl font-bold mb-4">Добро пожаловать в iHome24</h1>
        <p class="text-xl mb-8">Качественные товары для дома и офиса</p>
        <router-link
          to="/products"
          class="inline-block bg-white text-primary-600 px-8 py-3 rounded-md font-semibold hover:bg-gray-100 transition-colors"
        >
          Перейти в каталог
        </router-link>
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
          <!-- Desktop Grid -->
          <div class="products-collections-grid desktop-grid">
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
  await productsStore.fetchProducts()
  await productsStore.fetchCategories()
})
</script>

<style scoped>
/* Desktop Grid */
.products-collections-grid.desktop-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  grid-auto-rows: min-content;
}

.products-collections-grid.desktop-grid .collection-item {
  grid-column: span 2;
}

.products-collections-grid.desktop-grid .product-item {
  grid-column: span 1;
}

/* Общие стили для коллекций и товаров в циклах */
.products-collections-grid.desktop-grid .collection-item {
  grid-column: span 2;
}

.products-collections-grid.desktop-grid .product-item {
  grid-column: span 1;
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
  .products-collections-grid.desktop-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .products-collections-grid.desktop-grid .collection-item {
    grid-column: span 2;
  }
  
  /* На планшете коллекции занимают всю ширину, товары по 2 в ряд */
  .products-collections-grid.desktop-grid .product-item {
    grid-column: span 1;
  }
}

@media (max-width: 640px) {
  /* Скрываем desktop grid на мобильных */
  .products-collections-grid.desktop-grid {
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
