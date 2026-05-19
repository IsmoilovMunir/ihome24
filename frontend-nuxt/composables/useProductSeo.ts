import { buildCategoryPath, getCategoryChain } from '~/utils/categoryUrl'
import { productSeoPath, productSeoSlug } from '~/utils/productUrl'

function formatPriceRub(price: unknown) {
  const n = Number(price)
  if (!Number.isFinite(n) || n < 0) return '0'
  return new Intl.NumberFormat('ru-RU', { maximumFractionDigits: 0 }).format(Math.round(n))
}

const META_DESCRIPTION_MAX = 300

function buildDefaultMetaDescription(name: string, price: unknown) {
  const n = name.trim()
  if (!n) return 'Доставка. Гарантия. Заказать на ihome24.ru.'
  const text = `${n} по цене ${formatPriceRub(price)} ₽. Доставка. Гарантия. Заказать на ihome24.ru.`
  return text.length > META_DESCRIPTION_MAX ? text.slice(0, META_DESCRIPTION_MAX) : text
}

export function useProductSeo(options: {
  product: Record<string, unknown>
  /** Канонический путь без домена, напр. /product/kukhonnyi-nozh */
  canonicalPath?: string
  route?: { path: string }
  categories?: Array<Record<string, unknown>>
  variantSku?: string | null
  displayName?: string
  imageUrl?: string | null
  inStock?: boolean
  offerPrice?: number | null
}) {
  const siteUrl = useSiteUrl()
  const { $fileApi } = useNuxtApp()
  const product = options.product
  const seo = product.seo as {
    metaTitle?: string
    metaDescription?: string
    ogImage?: string
  } | undefined
  const name = (options.displayName || product.name || '').toString().trim()
  const price = options.offerPrice ?? (product.price as number)
  const title = (product.metaTitle as string | undefined)?.trim()
    || seo?.metaTitle?.trim()
    || (name ? `${name} — купить в ihome24.ru` : 'Купить в ihome24.ru')
  const metaDescription = (product.metaDescription as string | undefined)?.trim()
    || seo?.metaDescription?.trim()
    || buildDefaultMetaDescription(name, price)
  const schemaDescription =
    (typeof product.description === 'string' && product.description.trim())
    || metaDescription

  const slug = productSeoSlug(product as { id: number, name?: string, seo?: { slug?: string } })
  const path =
    options.canonicalPath
    || options.route?.path
    || productSeoPath(product as { id: number, name?: string })
  const canonical = `${siteUrl}${path}`

  const seoOgImage = (product.ogImage as string | undefined)?.trim()
    || seo?.ogImage?.trim()
  let image = options.imageUrl || null
  if (!image && seoOgImage) {
    image = $fileApi.getImageUrlLarge(seoOgImage as string)
      || $fileApi.getFileUrl(seoOgImage as string)
  }
  if (!image && product.imageUrl) {
    image = $fileApi.getImageUrlLarge(product.imageUrl as string)
      || $fileApi.getFileUrl(product.imageUrl as string)
  }
  const images = image ? [image] : []

  const inStock = options.inStock ?? ((product.stockQuantity as number) ?? 0) > 0
  const sku = options.variantSku || (product.sku as string) || undefined
  const brandName = (product.brand as string) || 'iHome24'

  const ratingValue = Number(
    product.ratingAverage ?? product.averageRating ?? product.rating,
  )
  const reviewCount = Number(
    product.reviewCount ?? product.reviewsCount ?? product.ratingCount,
  )

  const breadcrumbItems: Array<{
    name: string
    item: string
  }> = [
    { name: 'Главная', item: `${siteUrl}/` },
    { name: 'Каталог', item: `${siteUrl}/products` },
  ]

  const category = product.category as Record<string, unknown> | undefined
  const categories = options.categories || []
  if (category?.id && categories.length) {
    const chain = getCategoryChain(category as never, categories as never[])
    for (const cat of chain) {
      breadcrumbItems.push({
        name: (cat as { name: string }).name,
        item: `${siteUrl}${buildCategoryPath(cat as never, categories as never[])}`,
      })
    }
  }
  breadcrumbItems.push({
    name,
    item: canonical,
  })

  useSeoMeta({
    title,
    description: metaDescription,
    robots: 'index, follow',
    ogType: 'product',
    ogSiteName: 'iHome24',
    ogTitle: title,
    ogDescription: metaDescription,
    ogUrl: canonical,
    ogImage: image || undefined,
    twitterCard: image ? 'summary_large_image' : 'summary',
    twitterTitle: title,
    twitterDescription: metaDescription,
    twitterImage: image || undefined,
  })

  useHead({
    link: [{ rel: 'canonical', href: canonical, key: 'canonical-product' }],
  })

  const productSchema: Record<string, unknown> = {
    name,
    description: schemaDescription,
    sku,
    mpn: sku,
    image: images.length ? images : undefined,
    brand: brandName,
    offers: {
      price: price != null ? Number(price) : undefined,
      priceCurrency: 'RUB',
      availability: inStock ? 'InStock' : 'OutOfStock',
      url: canonical,
    },
  }

  if (Number.isFinite(ratingValue) && Number.isFinite(reviewCount) && reviewCount > 0) {
    productSchema.aggregateRating = {
      ratingValue,
      reviewCount,
      bestRating: 5,
      worstRating: 1,
    }
  }

  useSchemaOrg([
    defineProduct(productSchema),
    {
      '@type': 'BreadcrumbList',
      itemListElement: breadcrumbItems.map((crumb, index) => ({
        '@type': 'ListItem',
        position: index + 1,
        name: crumb.name,
        item: crumb.item,
      })),
    },
  ])

  return { title, metaDescription, canonical, slug }
}
