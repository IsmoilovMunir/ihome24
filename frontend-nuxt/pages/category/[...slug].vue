<script setup>
const apiBase = useApiBase()
const siteUrl = useSiteUrl()

const { data: categories } = await useAsyncData('catalog-categories', () =>
  $fetch('/api/categories', { baseURL: apiBase }),
)

const productsStore = useProductsStore()
productsStore.$patch({
  categories: categories.value ? toPlainSerializable(categories.value) : [],
  loading: false,
  error: null,
})

useSeoMeta({
  title: 'Каталог товаров - iHome24',
  description: 'Категории каталога iHome24: подбор товаров для дома и офиса.',
  robots: 'index, follow',
})

useHead({
  link: [{ rel: 'canonical', href: `${siteUrl}${useRoute().path}` }],
})
</script>

<template>
  <PagesProducts />
</template>
