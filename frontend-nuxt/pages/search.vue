<script setup>
import {
  filterCategoriesByQuery,
  filterProductsByQuery,
} from '~/utils/searchFilter'

/** Поиск с SSR: параметр ?q= */
const route = useRoute()
const { api } = useApi()

const searchQuery = computed(() => {
  const raw = route.query.q
  const value = Array.isArray(raw) ? raw[0] : raw
  return String(value || '').trim()
})

const { data: catalog } = await useAsyncData(
  () => `search-${searchQuery.value || 'empty'}`,
  async () => {
    const [products, categories] = await Promise.all([
      api('/products', { timeout: 60_000 }),
      api('/categories'),
    ])
    return {
      products: toPlainSerializable(products) ?? [],
      categories: toPlainSerializable(categories) ?? [],
    }
  },
  { watch: [searchQuery] },
)

const productsStore = useProductsStore()
productsStore.$patch({
  products: catalog.value?.products ?? [],
  categories: catalog.value?.categories ?? [],
  loading: false,
  error: null,
})

const productHits = computed(() =>
  filterProductsByQuery(catalog.value?.products ?? [], searchQuery.value),
)
const categoryHits = computed(() =>
  filterCategoriesByQuery(catalog.value?.categories ?? [], searchQuery.value),
)

useSeoMeta({
  title: computed(() =>
    searchQuery.value
      ? `Поиск: ${searchQuery.value} - iHome24`
      : 'Поиск - iHome24',
  ),
  description: computed(() => {
    if (!searchQuery.value) {
      return 'Поиск по каталогу интернет-магазина iHome24.'
    }
    const total = productHits.value.length + categoryHits.value.length
    return `Результаты поиска «${searchQuery.value}» в iHome24: найдено ${total} позиций.`
  }),
  robots: 'noindex, nofollow',
})

const siteUrl = useSiteUrl()
useHead({
  link: computed(() => [
    {
      rel: 'canonical',
      key: 'canonical',
      href: searchQuery.value
        ? `${siteUrl}/search?q=${encodeURIComponent(searchQuery.value)}`
        : `${siteUrl}/search`,
    },
  ]),
})
</script>

<template>
  <PagesSearch :catalog-prefetched="true" />
</template>
