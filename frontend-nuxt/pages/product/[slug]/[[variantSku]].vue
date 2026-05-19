<script setup>
/**
 * N-1: SSR-карточка товара /product/:slug
 * — данные и meta из Spring API (useProductPage → useProductSeo)
 * — canonical https://ihome24.ru/product/{slug}
 * — 301 при редиректе из product_slug_redirects
 */
const result = await useProductPage()
const plainProduct = computed(() => result?.plainProduct ?? null)
</script>

<template>
  <PagesProductDetail
    v-if="plainProduct"
    :ssr-product="plainProduct"
  />
</template>
