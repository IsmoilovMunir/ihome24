<script setup>
import { computed } from 'vue'
import { getCategoryAndDescendantIds } from '~/utils/categoryUrl'
import ProductCard from '~/components/ProductCard.vue'

const PER_PAGE = 24

const props = defineProps({
  category: { type: Object, required: true },
  categories: { type: Array, default: () => [] },
  products: { type: Array, default: () => [] },
  page: { type: Number, default: 1 },
})

const route = useRoute()
const { $fileApi } = useNuxtApp()

const categoryIds = computed(() =>
  getCategoryAndDescendantIds(props.category.id, props.categories),
)

const filteredProducts = computed(() =>
  props.products.filter(p => p.category?.id && categoryIds.value.includes(p.category.id)),
)

const totalPages = computed(() =>
  Math.max(1, Math.ceil(filteredProducts.value.length / PER_PAGE)),
)

const currentPage = computed(() => {
  const p = props.page
  if (!Number.isFinite(p) || p < 1) return 1
  return Math.min(Math.floor(p), totalPages.value)
})

const paginatedProducts = computed(() => {
  const start = (currentPage.value - 1) * PER_PAGE
  return filteredProducts.value.slice(start, start + PER_PAGE)
})

const bannerImageUrl = computed(() => {
  const url =
    props.category.bannerImageUrl
    || props.category.imageUrl
    || props.category.collectionImageUrl
  if (!url) return null
  return $fileApi.getImageUrlLarge(url) || $fileApi.getFileUrl(url)
})

function pageLink(pageNum) {
  const query = { ...route.query }
  if (pageNum <= 1) delete query.page
  else query.page = String(pageNum)
  return { path: route.path, query }
}
</script>

<template>
  <div class="catalog-category-page bg-[#3A3331] min-h-screen">
    <section v-if="category" class="category-banner">
      <div
        v-if="bannerImageUrl"
        class="category-banner-image"
        :style="{ backgroundImage: `url(${bannerImageUrl})` }"
      />
      <div class="category-banner-overlay" />
      <div class="category-banner-content container mx-auto px-4">
        <h1 class="category-banner-title">{{ category.name }}</h1>
        <p v-if="category.description" class="category-banner-description">
          {{ category.description }}
        </p>
      </div>
    </section>

    <div class="container mx-auto px-4 py-8">
      <p class="text-gray-300 text-sm mb-6">
        {{ filteredProducts.length }}
        {{ filteredProducts.length === 1 ? 'товар' : filteredProducts.length < 5 ? 'товара' : 'товаров' }}
      </p>

      <div v-if="paginatedProducts.length === 0" class="text-center py-12 text-gray-400">
        В этой категории пока нет товаров
      </div>

      <div
        v-else
        class="grid grid-cols-2 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 sm:gap-6"
      >
        <ProductCard
          v-for="product in paginatedProducts"
          :key="product.id"
          :product="product"
        />
      </div>

      <nav
        v-if="totalPages > 1"
        class="mt-10 flex flex-wrap items-center justify-center gap-2"
        aria-label="Пагинация"
      >
        <NuxtLink
          v-if="currentPage > 1"
          :to="pageLink(currentPage - 1)"
          class="catalog-page-btn"
          rel="prev"
        >
          Назад
        </NuxtLink>
        <NuxtLink
          v-for="n in totalPages"
          :key="n"
          :to="pageLink(n)"
          class="catalog-page-btn"
          :class="{ 'catalog-page-btn-active': n === currentPage }"
          :aria-current="n === currentPage ? 'page' : undefined"
        >
          {{ n }}
        </NuxtLink>
        <NuxtLink
          v-if="currentPage < totalPages"
          :to="pageLink(currentPage + 1)"
          class="catalog-page-btn"
          rel="next"
        >
          Вперёд
        </NuxtLink>
      </nav>
    </div>
  </div>
</template>

<style scoped>
.category-banner {
  position: relative;
  min-height: 200px;
  display: flex;
  align-items: flex-end;
  overflow: hidden;
}

.category-banner-image {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
}

.category-banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(58, 51, 49, 0.95), rgba(58, 51, 49, 0.4));
}

.category-banner-content {
  position: relative;
  z-index: 1;
  padding: 2rem 1rem 1.5rem;
}

.category-banner-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: #fff;
  margin-bottom: 0.5rem;
}

.category-banner-description {
  color: rgba(255, 255, 255, 0.85);
  max-width: 42rem;
}

.catalog-page-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 2.5rem;
  padding: 0.5rem 0.75rem;
  border-radius: 9999px;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 0.875rem;
  transition: background 0.2s;
}

.catalog-page-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.catalog-page-btn-active {
  background: #fff;
  color: #3a3331;
  font-weight: 600;
}
</style>
