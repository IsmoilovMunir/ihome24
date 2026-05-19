import { buildCategoryPath } from '~/utils/categoryUrl'
import { productSeoPath } from '~/utils/productUrl'

type CategoryDto = {
  id: number
  slug?: string | null
  name?: string | null
  parentId?: number | null
  isActive?: boolean | null
  updatedAt?: string | null
}

type ProductDto = {
  id: number
  name?: string | null
  isActive?: boolean | null
  updatedAt?: string | null
  seo?: { slug?: string | null, pathSegment?: string | null } | null
}

export type SitemapUrlEntry = {
  loc: string
  lastmod?: string
  changefreq?: string
  priority?: number
}

function toLastmod(value: string | null | undefined): string | undefined {
  if (!value) return undefined
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return undefined
  return d.toISOString()
}

/** Статические страницы витрины (как в Spring SitemapService). */
function staticEntries(): SitemapUrlEntry[] {
  return [
    { loc: '/', changefreq: 'daily', priority: 1 },
    { loc: '/products', changefreq: 'daily', priority: 0.9 },
    { loc: '/support/contacts', changefreq: 'monthly', priority: 0.6 },
    { loc: '/support/oferta', changefreq: 'monthly', priority: 0.4 },
    { loc: '/order-tracking', changefreq: 'weekly', priority: 0.5 },
    { loc: '/services', changefreq: 'monthly', priority: 0.4 },
    { loc: '/optovym-klientam', changefreq: 'monthly', priority: 0.85 },
  ]
}

export function buildSitemapEntries(
  products: ProductDto[],
  categories: CategoryDto[],
): SitemapUrlEntry[] {
  const entries: SitemapUrlEntry[] = [...staticEntries()]

  const activeCategories = (categories || []).filter(c => c.isActive !== false)
  for (const category of activeCategories) {
    const path = buildCategoryPath(category, activeCategories)
    if (!path || path === '/products') continue
    entries.push({
      loc: path,
      lastmod: toLastmod(category.updatedAt),
      changefreq: 'daily',
      priority: 0.9,
    })
  }

  const activeProducts = (products || []).filter(p => p.id != null && p.isActive !== false)
  for (const product of activeProducts) {
    entries.push({
      loc: productSeoPath(product),
      lastmod: toLastmod(product.updatedAt),
      changefreq: 'weekly',
      priority: 0.8,
    })
  }

  return entries
}
