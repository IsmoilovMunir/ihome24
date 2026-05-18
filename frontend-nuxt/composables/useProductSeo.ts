import { categoryPathSegment, getCategoryChain } from '~/utils/categoryUrl'
import { productPath } from '~/utils/productUrl'

function formatPriceRub(price: unknown) {
  const n = Number(price)
  if (!Number.isFinite(n) || n < 0) return '0'
  return new Intl.NumberFormat('ru-RU', { maximumFractionDigits: 0 }).format(Math.round(n))
}

function pruneJsonLd(value: unknown): unknown {
  if (Array.isArray(value)) {
    const items = value.map(pruneJsonLd).filter(v => v !== undefined && v !== null)
    return items.length ? items : undefined
  }
  if (value && typeof value === 'object') {
    const out: Record<string, unknown> = {}
    for (const [key, val] of Object.entries(value)) {
      const cleaned = pruneJsonLd(val)
      if (cleaned !== undefined && cleaned !== null) out[key] = cleaned
    }
    return Object.keys(out).length ? out : undefined
  }
  return value === undefined || value === null ? undefined : value
}

export function useProductSeo(options: {
  product: Record<string, unknown>
  route: { path: string }
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
  const name = (options.displayName || product.name || '').toString().trim()
  const price = options.offerPrice ?? (product.price as number)
  const title = name ? `${name} — купить в ihome24.ru` : 'Купить в ihome24.ru'
  const description = name
    ? `${name} по цене ${formatPriceRub(price)} руб. Доставка. Гарантия.`.slice(0, 160)
    : 'Доставка. Гарантия.'
  const canonical = `${siteUrl}${options.route.path}`

  let image = options.imageUrl || null
  if (!image && product.imageUrl) {
    image = $fileApi.getImageUrlLarge(product.imageUrl as string)
      || $fileApi.getFileUrl(product.imageUrl as string)
  }
  const images = image ? [image] : []

  const inStock = options.inStock ?? ((product.stockQuantity as number) ?? 0) > 0
  const sku = options.variantSku || (product.sku as string) || undefined
  const brandName = (product.brand as string) || 'iHome24'

  const productSchema = pruneJsonLd({
    '@context': 'https://schema.org',
    '@type': 'Product',
    '@id': `${canonical}#product`,
    name,
    description,
    sku,
    mpn: sku,
    image: images.length ? images : undefined,
    brand: { '@type': 'Brand', name: brandName },
    offers: {
      '@type': 'Offer',
      url: canonical,
      priceCurrency: 'RUB',
      price: price != null ? Number(price).toFixed(2) : undefined,
      availability: inStock ? 'https://schema.org/InStock' : 'https://schema.org/OutOfStock',
      itemCondition: 'https://schema.org/NewCondition',
    },
  })

  const breadcrumbElements: Array<Record<string, unknown>> = [
    { '@type': 'ListItem', position: 1, name: 'Главная', item: `${siteUrl}/` },
    { '@type': 'ListItem', position: 2, name: 'Каталог', item: `${siteUrl}/products` },
  ]
  let position = 3
  const category = product.category as Record<string, unknown> | undefined
  const categories = options.categories || []
  if (category?.id && categories.length) {
    const chain = getCategoryChain(category as never, categories as never[])
    let pathPrefix = '/category'
    for (const cat of chain) {
      pathPrefix += `/${encodeURIComponent(categoryPathSegment(cat as never))}`
      breadcrumbElements.push({
        '@type': 'ListItem',
        position: position++,
        name: (cat as { name: string }).name,
        item: `${siteUrl}${pathPrefix}`,
      })
    }
  }
  breadcrumbElements.push({
    '@type': 'ListItem',
    position,
    name,
    item: canonical,
  })

  const breadcrumbSchema = pruneJsonLd({
    '@context': 'https://schema.org',
    '@type': 'BreadcrumbList',
    itemListElement: breadcrumbElements,
  })

  useSeoMeta({
    title,
    description,
    robots: 'index, follow',
    ogType: 'product',
    ogSiteName: 'iHome24',
    ogTitle: title,
    ogDescription: description,
    ogUrl: canonical,
    ogImage: image || undefined,
    twitterCard: image ? 'summary_large_image' : 'summary',
    twitterTitle: title,
    twitterDescription: description,
    twitterImage: image || undefined,
  })

  useHead({
    link: [{ rel: 'canonical', href: canonical }],
    script: [
      { type: 'application/ld+json', key: 'jsonld-product', innerHTML: JSON.stringify(productSchema) },
      { type: 'application/ld+json', key: 'jsonld-breadcrumbs', innerHTML: JSON.stringify(breadcrumbSchema) },
    ],
  })
}
