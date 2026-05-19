import type { RouteLocationNormalizedLoaded } from 'vue-router'

/** Query-параметры, не попадающие в canonical (пагинация, сортировка, UTM). */
const STRIP_QUERY_KEYS = new Set([
  'page',
  'sort',
  'order',
  'utm_source',
  'utm_medium',
  'utm_campaign',
  'utm_term',
  'utm_content',
  'ysclid',
  'gclid',
  'fbclid',
])

function shouldStripQueryKey(key: string): boolean {
  const lower = key.toLowerCase()
  if (STRIP_QUERY_KEYS.has(lower)) return true
  if (lower.startsWith('utm_')) return true
  return false
}

/**
 * Путь + query для canonical: без page, sort, order и UTM.
 * ?page=2 → тот же path без page; ?sort=price → path без query.
 */
export function buildCanonicalPath(route: RouteLocationNormalizedLoaded): string {
  const path = route.path || '/'
  const query = route.query || {}
  const params = new URLSearchParams()

  for (const [key, raw] of Object.entries(query)) {
    if (shouldStripQueryKey(key)) continue
    const value = Array.isArray(raw) ? raw[0] : raw
    if (value == null || value === '') continue
    params.set(key, String(value))
  }

  const qs = params.toString()
  return qs ? `${path}?${qs}` : path
}

export function buildCanonicalHref(
  route: RouteLocationNormalizedLoaded,
  siteUrl: string,
): string {
  const base = siteUrl.replace(/\/$/, '')
  return `${base}${buildCanonicalPath(route)}`
}
