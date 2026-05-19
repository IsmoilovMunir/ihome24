import { slugify } from './productUrl.js'

/**
 * Сегмент пути для категории: slug из API или ЧПУ из названия.
 * @param {{ slug?: string, name?: string }} category
 * @returns {string}
 */
export function categoryPathSegment(category) {
  if (!category) return 'category'
  const raw = (category.slug && String(category.slug).trim()) || slugify(category.name || '')
  return raw || `id-${category.id}`
}

/**
 * Цепочка от корня до категории (включительно).
 * @param {{ id: number, parentId?: number|null }} category
 * @param {Array} categories
 * @returns {Array}
 */
export function getCategoryChain(category, categories) {
  if (!category?.id || !categories?.length) return []
  const chain = []
  let c = category
  const guard = new Set()
  while (c && !guard.has(c.id)) {
    guard.add(c.id)
    chain.unshift(c)
    if (!c.parentId) break
    c = categories.find(x => x.id === c.parentId)
  }
  return chain
}

/**
 * ЧПУ каталога: /category/{slug_уровня1}/{slug_уровня2}/…
 * @param {{ id: number, parentId?: number|null, slug?: string, name?: string }} category
 * @param {Array} categories — полный список из store
 * @returns {string}
 */
export function buildCategoryPath(category, categories) {
  if (!category?.id) return '/products'
  const chain = getCategoryChain(category, categories)
  if (!chain.length) return '/products'
  const segments = chain.map(cat => encodeURIComponent(categoryPathSegment(cat)))
  return `/category/${segments.join('/')}`
}

function segmentMatchesCategory(category, segment) {
  if (!segment || !category) return false
  const want = String(segment).toLowerCase()
  const s = categoryPathSegment(category).toLowerCase()
  return s === want
}

/**
 * Разбор /category/a/b/c → id категории или null.
 * @param {string[]} segments — уже декодированные сегменты пути
 * @param {Array} categories
 * @returns {number|null}
 */
export function resolveCategoryIdFromSegments(segments, categories) {
  if (!segments?.length || !categories?.length) return null
  let current = categories.find(
    c => !c.parentId && segmentMatchesCategory(c, segments[0]),
  )
  if (!current) return null
  if (segments.length === 1) return current.id
  for (let i = 1; i < segments.length; i++) {
    const next = categories.find(
      c => c.parentId === current.id && segmentMatchesCategory(c, segments[i]),
    )
    if (!next) return null
    current = next
  }
  return current.id
}

/**
 * Текущий выбранный id категории из маршрута: query ?category= или путь /category/…
 * @param {import('vue-router').RouteLocationNormalizedLoaded} route
 * @param {Array} categories
 * @returns {number|null}
 */
/**
 * Найти категорию по одному сегменту slug (маршрут /catalog/:slug).
 * @param {string} slug
 * @param {Array} categories
 * @returns {object|null}
 */
export function findCategoryBySlug(slug, categories) {
  if (!slug || !categories?.length) return null
  let decoded = String(slug)
  try {
    decoded = decodeURIComponent(decoded)
  } catch {
    /* keep raw */
  }
  const want = decoded.toLowerCase()

  const active = categories.filter(c => c.isActive !== false)

  const byApiSlug = active.find(
    c => c.slug && String(c.slug).trim().toLowerCase() === want,
  )
  if (byApiSlug) return byApiSlug

  const matches = active.filter(
    c => categoryPathSegment(c).toLowerCase() === want,
  )
  if (matches.length === 0) return null
  if (matches.length === 1) return matches[0]

  return matches.sort((a, b) => {
    const depthA = getCategoryChain(a, categories).length
    const depthB = getCategoryChain(b, categories).length
    return depthA - depthB
  })[0]
}

/** Id категории и всех её потомков (для фильтра товаров). */
export function getCategoryAndDescendantIds(categoryId, categories) {
  if (!categoryId || !categories?.length) return []
  const ids = new Set([categoryId])
  let changed = true
  while (changed) {
    changed = false
    for (const c of categories) {
      if (c.parentId && ids.has(c.parentId) && !ids.has(c.id)) {
        ids.add(c.id)
        changed = true
      }
    }
  }
  return [...ids]
}

export function resolveCategoryIdFromRoute(route, categories) {
  if (typeof route.path === 'string' && route.path.startsWith('/category/')) {
    if (!categories?.length) return null
    const raw = route.path.slice('/category/'.length).split('/').filter(Boolean)
    const segments = raw.map(s => {
      try {
        return decodeURIComponent(s)
      } catch {
        return s
      }
    })
    return resolveCategoryIdFromSegments(segments, categories)
  }
  const q = route.query?.category
  if (q !== undefined && q !== null && q !== '') {
    const n = Number(q)
    return Number.isNaN(n) ? null : n
  }
  return null
}
