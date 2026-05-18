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
  description: 'Каталог товаров iHome24: товары для дома и офиса с актуальными ценами и наличием.',
  robots: 'index, follow',
})

useHead({
  link: [{ rel: 'canonical', href: `${siteUrl}/products` }],
})
</script>

<template>
  <PagesProducts />
</template>
