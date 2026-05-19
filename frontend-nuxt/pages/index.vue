<script setup>
/**
 * Главная страница (SSR).
 * Данные загружаются на сервере — в view-source видны h1, товары и meta.
 */
const { api } = useApi()

const { data: home } = await useAsyncData('home', async () => {
  const [products, categories] = await Promise.all([
    api('/products', { timeout: 60_000 }),
    api('/categories', { timeout: 60_000 }),
  ])
  return {
    products: toPlainSerializable(products) ?? [],
    categories: toPlainSerializable(categories) ?? [],
  }
})

const productsStore = useProductsStore()
productsStore.$patch({
  products: home.value?.products ?? [],
  categories: home.value?.categories ?? [],
  loading: false,
  error: null,
})

useHomeSeo()

/** Фоновое обновление каталога после гидратации (без спиннера). */
onMounted(() => {
  const { refreshCatalog } = useCatalogRefresh()
  refreshCatalog()
})
</script>

<template>
  <PagesHome />
</template>
