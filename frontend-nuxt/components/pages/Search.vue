<template>
  <div class="search-page bg-[#3A3331] min-h-screen pt-[60px] md:pt-[110px] pb-8">
    <div class="container mx-auto px-4 py-8">
      <h1 class="text-3xl font-bold text-white mb-6">Поиск</h1>

      <form class="mb-8" @submit.prevent="submitSearch">
        <div class="flex gap-3">
          <input
            v-model="inputQuery"
            type="text"
            class="search-page-input"
            placeholder="Поиск по названию, категории, описанию..."
            autofocus
          />
          <button type="submit" class="search-page-submit">Найти</button>
        </div>
      </form>

      <div v-if="!query" class="text-gray-400">
        Введите запрос в поле выше. Результаты появятся на этой странице по адресу /search/?q=ваш_запрос
      </div>

      <template v-else>
        <div v-if="productsStore.loading" class="text-center py-12">
          <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-[#F47427]"></div>
        </div>

        <template v-else>
          <div v-if="searchResultsCategories.length === 0 && searchResultsProducts.length === 0" class="text-gray-400 py-12">
            По запросу «{{ query }}» ничего не найдено.
          </div>

          <template v-else>
            <section v-if="searchResultsCategories.length > 0" class="mb-10">
              <h2 class="text-xl font-semibold text-white mb-4">Категории</h2>
              <div class="flex flex-wrap gap-3">
                <NuxtLink
                  v-for="cat in searchResultsCategories"
                  :key="'cat-' + cat.id"
                  :to="categoryHref(cat)"
                  class="search-page-category-link"
                >
                  {{ cat.name }}
                </NuxtLink>
              </div>
            </section>

            <section v-if="searchResultsProducts.length > 0">
              <h2 class="text-xl font-semibold text-white mb-4">Товары ({{ searchResultsProducts.length }})</h2>
              <div class="grid grid-cols-2 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 sm:gap-6">
                <ProductCard
                  v-for="product in searchResultsProducts"
                  :key="'prod-' + product.id"
                  :product="product"
                />
              </div>
            </section>
          </template>
        </template>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useProductsStore } from '~/stores/products'
import { buildCategoryPath } from '~/utils/categoryUrl'
import {
  filterCategoriesByQuery,
  filterProductsByQuery,
} from '~/utils/searchFilter'
import ProductCard from '~/components/ProductCard.vue'

const props = defineProps({
  /** Каталог уже загружен на странице (SSR) */
  catalogPrefetched: { type: Boolean, default: false },
})

const route = useRoute()
const router = useRouter()
const productsStore = useProductsStore()

const categoryHref = cat => buildCategoryPath(cat, productsStore.categories)

const query = computed(() => (route.query.q || '').trim())
const inputQuery = ref('')

watch(
  () => route.query.q,
  (q) => {
    inputQuery.value = q || ''
  },
  { immediate: true }
)

const searchResultsProducts = computed(() =>
  filterProductsByQuery(productsStore.products, query.value),
)

const searchResultsCategories = computed(() =>
  filterCategoriesByQuery(productsStore.categories, query.value),
)

function submitSearch() {
  const q = inputQuery.value.trim()
  if (q) {
    router.replace({ path: '/search', query: { q } })
  }
}

onMounted(async () => {
  inputQuery.value = route.query.q || ''
  if (!props.catalogPrefetched) {
    const { refreshCatalog } = useCatalogRefresh()
    await refreshCatalog()
  }
})
</script>

<style scoped>
.search-page-input {
  flex: 1;
  max-width: 400px;
  padding: 12px 16px;
  font-size: 16px;
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  outline: none;
  font-family: inherit;
}

.search-page-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.search-page-input:focus {
  border-color: #F47427;
}

.search-page-submit {
  padding: 12px 24px;
  font-size: 16px;
  font-weight: 500;
  color: #fff;
  background: #F47427;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.search-page-submit:hover {
  background: #E0651D;
}

.search-page-category-link {
  display: inline-block;
  padding: 10px 16px;
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  text-decoration: none;
  transition: background 0.2s;
}

.search-page-category-link:hover {
  background: rgba(244, 116, 39, 0.3);
}
</style>
