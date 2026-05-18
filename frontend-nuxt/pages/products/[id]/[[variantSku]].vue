<script setup>
import { parseProductIdFromRoute } from '~/utils/productUrl'

const route = useRoute()
const apiBase = useApiBase()
const productId = parseProductIdFromRoute(route.params.id)

if (productId == null) {
  throw createError({ statusCode: 404, statusMessage: 'Товар не найден' })
}

const [{ data: product, error }, { data: categories }] = await Promise.all([
  useAsyncData(`product-${productId}`, () =>
    $fetch(`/api/products/${productId}`, { baseURL: apiBase }),
  ),
  useAsyncData('categories-all', () => $fetch('/api/categories', { baseURL: apiBase })),
])

if (error.value || !product.value) {
  throw createError({ statusCode: 404, statusMessage: 'Товар не найден' })
}

const productsStore = useProductsStore()
const plainProduct = toPlainSerializable(product.value)
productsStore.selectedProduct = plainProduct
if (categories.value) productsStore.categories = toPlainSerializable(categories.value)

const variantSku = route.params.variantSku || null
const displayName = product.value.name
const offerPrice = product.value.price
const { $fileApi } = useNuxtApp()
const imageUrl = product.value.imageUrl
  ? ($fileApi.getImageUrlLarge(product.value.imageUrl)
    || $fileApi.getFileUrl(product.value.imageUrl))
  : null
const inStock = (product.value.stockQuantity ?? 0) > 0

useProductSeo({
  product: plainProduct,
  route: { path: route.path },
  categories: categories.value || [],
  variantSku,
  displayName,
  imageUrl,
  inStock,
  offerPrice,
})
</script>

<template>
  <PagesProductDetail :ssr-product="plainProduct" />
</template>
