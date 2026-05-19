<script setup>
import { SITE_DEFAULT_DESCRIPTION, SITE_DEFAULT_TITLE } from '~/utils/siteSeo'

const { api } = useApi()

const { data: categories } = await useAsyncData('catalog-categories', () =>
  api('/categories'),
)

const productsStore = useProductsStore()
productsStore.$patch({
  categories: categories.value ? toPlainSerializable(categories.value) : [],
  loading: false,
  error: null,
})

useSeoMeta({
  title: SITE_DEFAULT_TITLE,
  description: SITE_DEFAULT_DESCRIPTION,
  robots: 'index, follow',
})

// canonical — layouts/default.vue (без page/sort)
</script>

<template>
  <PagesProducts />
</template>
