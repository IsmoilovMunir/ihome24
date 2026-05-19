import {
  isProductSlugCanonical,
  productSeoPath,
} from '~/utils/productUrl'
import { fetchProductBySlug } from '~/utils/fetchProductBySlug'
import { toPlainSerializable } from '~/utils/serializable'

type SlugResolvePayload = {
  product?: Record<string, unknown> | null
  redirect?: boolean
  canonicalPath?: string
  categories?: unknown[]
}

/**
 * SSR-загрузка карточки товара для /product/:slug (N-1).
 * — useAsyncData + Spring API
 * — 301 при устаревшем slug (таблица редиректов)
 * — 404 если не найден
 * — useProductSeo: meta, canonical, Schema.org Product
 */
export async function useProductPage() {
  const nuxtApp = useNuxtApp()
  const route = useRoute()
  const config = useRuntimeConfig()
  const { api } = useApi()
  const { $fileApi } = useNuxtApp()
  const productsStore = useProductsStore()

  const slug = computed(() => String(route.params.slug || '').trim())
  const variantSku = computed(() => {
    const v = route.params.variantSku
    if (v == null || v === '') return null
    return String(v)
  })

  if (!slug.value) {
    throw createError({
      statusCode: 404,
      statusMessage: 'Товар не найден',
      fatal: true,
    })
  }

  const { data: payload } = await useAsyncData(
    () => `product-page-${slug.value}-${variantSku.value || ''}`,
    async (): Promise<SlugResolvePayload | null> => {
      const result = await fetchProductBySlug(slug.value, {
        apiBaseServer: String(config.apiBaseServer || ''),
        public: { apiBase: String(config.public.apiBase || '') },
      })

      if (result.kind === 'not_found') {
        return null
      }

      if (result.kind === 'redirect') {
        return { redirect: true, canonicalPath: result.canonicalPath }
      }

      const product = result.product
      if (!product?.id) {
        return null
      }

      if (!isProductSlugCanonical(product, slug.value)) {
        return { redirect: true, canonicalPath: productSeoPath(product) }
      }

      let categories: unknown[] = []
      try {
        categories = toPlainSerializable(await api('/categories')) ?? []
      } catch {
        categories = []
      }

      return {
        product: toPlainSerializable(product),
        categories,
      }
    },
    { watch: [slug, variantSku] },
  )

  if (payload.value?.redirect && payload.value.canonicalPath) {
    const path = variantSku.value
      ? `${payload.value.canonicalPath}/${variantSku.value}`
      : payload.value.canonicalPath
    await navigateTo({ path, query: route.query }, { redirectCode: 301 })
    return null
  }

  if (!payload.value?.product) {
    throw createError({
      statusCode: 404,
      statusMessage: 'Товар не найден',
      fatal: true,
    })
  }

  const plainProduct = payload.value.product
  productsStore.selectedProduct = plainProduct
  productsStore.categories = payload.value.categories ?? []

  const displayName = plainProduct.name as string
  const offerPrice = plainProduct.price as number
  const imageUrl = plainProduct.imageUrl
    ? ($fileApi.getImageUrlLarge(plainProduct.imageUrl as string)
      || $fileApi.getFileUrl(plainProduct.imageUrl as string))
    : null
  const inStock = ((plainProduct.stockQuantity as number) ?? 0) > 0
  const canonicalPath = productSeoPath(plainProduct)

  nuxtApp.runWithContext(() => {
    useProductSeo({
      product: plainProduct,
      canonicalPath,
      categories: payload.value!.categories as Array<Record<string, unknown>>,
      variantSku: variantSku.value,
      displayName,
      imageUrl,
      inStock,
      offerPrice,
    })
  })

  return {
    plainProduct,
    variantSku,
    canonicalPath,
    categories: payload.value.categories,
  }
}
