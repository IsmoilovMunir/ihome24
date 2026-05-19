/**
 * Таблица транслитерации (как в SlugService на бэкенде).
 */
const cyrillicToLatin = {
  а: 'a', б: 'b', в: 'v', г: 'g', д: 'd', е: 'e', ё: 'yo', ж: 'zh', з: 'z',
  и: 'i', й: 'y', к: 'k', л: 'l', м: 'm', н: 'n', о: 'o', п: 'p', р: 'r',
  с: 's', т: 't', у: 'u', ф: 'f', х: 'kh', ц: 'ts', ч: 'ch', ш: 'sh', щ: 'shch',
  ъ: '', ы: 'y', ь: '', э: 'e', ю: 'yu', я: 'ya',
}

const MAX_SLUG_LENGTH = 100
const SLUG_PATTERN = /^[a-z0-9]+(?:-[a-z0-9]+)*$/

/**
 * Формирует slug из названия (для превью в админке; канонический slug — с бэкенда).
 */
export function slugify(name) {
  if (!name || typeof name !== 'string') return ''
  let transliterated = ''
  const s = name.trim()
  for (let i = 0; i < s.length; i++) {
    const c = s[i]
    const lower = c.toLowerCase()
    if (cyrillicToLatin[lower] !== undefined) {
      transliterated += cyrillicToLatin[lower]
    } else {
      transliterated += c
    }
  }
  let slug = transliterated.toLowerCase().replace(/[^a-z0-9]+/g, '-')
  slug = slug.replace(/^-+|-+$/g, '')
  if (!slug) return ''
  return slug.length > MAX_SLUG_LENGTH
    ? slug.slice(0, MAX_SLUG_LENGTH).replace(/-+$/, '')
    : slug
}

export function isValidSlugFormat(slug) {
  if (!slug || typeof slug !== 'string') return false
  return slug.length <= MAX_SLUG_LENGTH && SLUG_PATTERN.test(slug)
}

/**
 * Канонический slug товара для URL.
 * @param {{ seo?: { slug?: string, pathSegment?: string } }} product
 */
export function productSeoSlug(product) {
  const fromApi = product?.slug?.trim()
    || product?.seo?.slug?.trim()
    || product?.seo?.pathSegment?.trim()
  if (fromApi) return fromApi
  return slugify(product?.name || '') || (product?.id ? `product-${product.id}` : '')
}

/**
 * Канонический путь: /product/kukhonnyi-nozh-chef-20sm
 */
export function productSeoPath(product) {
  const slug = productSeoSlug(product)
  if (slug) return `/product/${slug}`
  if (product?.id != null) return `/products/${product.id}`
  return '/products'
}

export function productPath(product) {
  return productSeoPath(product)
}

export function isProductSlugCanonical(product, slugParam) {
  if (!product || slugParam == null) return false
  const actual = String(slugParam).trim().toLowerCase()
  if (actual === String(product.id)) return true
  const expected = productSeoSlug(product).toLowerCase()
  return actual === expected
}

/** @deprecated Предпочтительно GET /api/products/{slug} */
export function parseProductIdFromRoute(idOrSlug) {
  if (idOrSlug == null || idOrSlug === '') return null
  const s = String(idOrSlug).trim()
  const numPart = s.includes('-') ? s.slice(0, s.indexOf('-')) : s
  if (!/^\d+$/.test(numPart)) return null
  const id = parseInt(numPart, 10)
  return Number.isNaN(id) ? null : id
}

export const parseProductIdFromSlug = parseProductIdFromRoute
