<script setup>
import { SITE_DEFAULT_DESCRIPTION, SITE_DEFAULT_TITLE } from '~/utils/siteSeo'

const { api } = useApi()
const siteUrl = useSiteUrl()

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
  ogTitle: SITE_DEFAULT_TITLE,
  ogDescription: SITE_DEFAULT_DESCRIPTION,
  robots: 'index, follow',
})

useHead({
  link: [{ rel: 'canonical', href: `${siteUrl}/products`, key: 'canonical' }],
})
</script>

<template>
  <PagesProducts />
</template>
