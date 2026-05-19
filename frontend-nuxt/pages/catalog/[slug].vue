<script setup>
import {
  findCategoryBySlug,
  getCategoryAndDescendantIds,
} from '~/utils/categoryUrl'

const PER_PAGE = 24

const route = useRoute()
const { api } = useApi()

const slug = computed(() => String(route.params.slug || ''))

const currentPage = computed(() => {
  const raw = route.query.page
  const p = Number(Array.isArray(raw) ? raw[0] : raw)
  if (!Number.isFinite(p) || p < 1) return 1
  return Math.floor(p)
})

const { data: catalog } = await useAsyncData(
  () => `catalog-${slug.value}`,
  async () => {
    const [categories, products] = await Promise.all([
      api('/categories'),
      api('/products', { timeout: 60_000 }),
    ])
    const list = toPlainSerializable(categories) ?? []
    const category = findCategoryBySlug(slug.value, list)

    if (!category) {
      throw createError({
        statusCode: 404,
        statusMessage: 'Категория не найдена',
      })
    }

    return {
      category,
      categories: list,
      products: toPlainSerializable(products) ?? [],
    }
  },
  { watch: [slug] },
)

if (!catalog.value?.category) {
  throw createError({
    statusCode: 404,
    statusMessage: 'Категория не найдена',
  })
}

const categoryRef = computed(() => catalog.value?.category ?? null)
const categoriesRef = computed(() => catalog.value?.categories ?? [])

useCategoryCatalogSeo(categoryRef, categoriesRef)

const productsStore = useProductsStore()
productsStore.$patch({
  categories: catalog.value.categories,
  products: catalog.value.products,
  loading: false,
  error: null,
})

watch(
  [catalog, currentPage],
  () => {
    const data = catalog.value
    if (!data) return
    const ids = getCategoryAndDescendantIds(data.category.id, data.categories)
    const total = data.products.filter(
      p => p.category?.id && ids.includes(p.category.id),
    ).length
    const maxPage = Math.max(1, Math.ceil(total / PER_PAGE))
    if (currentPage.value > maxPage) {
      navigateTo({
        path: route.path,
        query: maxPage > 1 ? { page: String(maxPage) } : {},
        replace: true,
      })
    }
  },
  { immediate: true },
)
</script>

<template>
  <PagesCatalogCategory
    v-if="catalog?.category"
    :category="catalog.category"
    :categories="catalog.categories"
    :products="catalog.products"
    :page="currentPage"
  />
</template>
