<script setup>
/**
 * Legacy URL /products/:id → 301 на канонический /product/:slug
 */
import { parseProductIdFromRoute, productSeoPath } from '~/utils/productUrl'

const route = useRoute()
const { api } = useApi()

const productId = parseProductIdFromRoute(route.params.id)
if (productId == null) {
  throw createError({ statusCode: 404, statusMessage: 'Товар не найден' })
}

let resolved
try {
  resolved = await api(`/products/${productId}`)
} catch {
  throw createError({ statusCode: 404, statusMessage: 'Товар не найден' })
}

const product = resolved?.product ?? resolved
if (!product?.id) {
  throw createError({ statusCode: 404, statusMessage: 'Товар не найден' })
}

const target = resolved?.canonicalPath || productSeoPath(product)
const variantSku = route.params.variantSku
const path = variantSku ? `${target}/${variantSku}` : target

await navigateTo({ path, query: route.query }, { redirectCode: 301 })
</script>

<template>
  <div />
</template>
