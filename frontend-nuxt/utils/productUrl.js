/**
 * Транслитерация кириллицы в латиницу для ЧПУ (как на Яндекс.Маркете, MTS и др.)
 */
const cyrillicToLatin = {
  а: 'a', б: 'b', в: 'v', г: 'g', д: 'd', е: 'e', ё: 'e', ж: 'zh', з: 'z',
  и: 'i', й: 'y', к: 'k', л: 'l', м: 'm', н: 'n', о: 'o', п: 'p', р: 'r',
  с: 's', т: 't', у: 'u', ф: 'f', х: 'h', ц: 'ts', ч: 'ch', ш: 'sh', щ: 'sch',
  ъ: '', ы: 'y', ь: '', э: 'e', ю: 'yu', я: 'ya',
}

/**
 * Формирует slug из названия товара для URL (латиница, дефисы, без спецсимволов).
 * @param {string} name - название товара
 * @param {number} maxLength - макс. длина slug (по умолчанию 80)
 * @returns {string}
 */
export function slugify(name) {
  if (!name || typeof name !== 'string') return ''
  let s = name.toLowerCase().trim()
  let out = ''
  for (let i = 0; i < s.length; i++) {
    const c = s[i]
    if (cyrillicToLatin[c] !== undefined) {
      out += cyrillicToLatin[c]
    } else if (/[a-z0-9]/.test(c)) {
      out += c
    } else if (c === ' ' || c === '-' || c === '_') {
      if (out.length > 0 && out[out.length - 1] !== '-') out += '-'
    }
  }
  return out.replace(/-+/g, '-').replace(/^-|-$/g, '').slice(0, 80) || 'product'
}

/**
 * Путь к странице товара с ЧПУ: /products/1-iphone-17-256gb
 * @param {{ id: number, name?: string }} product - товар (id обязателен, name для slug)
 * @returns {string}
 */
export function productPath(product) {
  if (!product?.id) return '/products'
  const slug = slugify(product.name || '')
  return slug ? `/products/${product.id}-${slug}` : `/products/${product.id}`
}

/**
 * Из параметра маршрута (id или id-slug) извлекает числовой id товара.
 * @param {string} idOrSlug - значение route.params.id, например "1" или "1-iphone-17-256gb"
 * @returns {number|null}
 */
export function parseProductIdFromRoute(idOrSlug) {
  if (idOrSlug == null || idOrSlug === '') return null
  const s = String(idOrSlug).trim()
  const numPart = s.includes('-') ? s.slice(0, s.indexOf('-')) : s
  const id = parseInt(numPart, 10)
  return Number.isNaN(id) ? null : id
}
