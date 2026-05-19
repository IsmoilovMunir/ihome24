import type { FetchResponse } from 'ofetch'
import { normalizeApiPath, resolveApiBase } from '~/utils/apiFetch'

export type ProductSlugFetchResult =
  | { kind: 'redirect', canonicalPath: string }
  | { kind: 'product', product: Record<string, unknown> }
  | { kind: 'not_found' }

function apiLocationToCanonicalPath(location: string | null): string | null {
  if (!location) return null
  try {
    const pathname = location.startsWith('http')
      ? new URL(location).pathname
      : location.split('?')[0]
    const match = pathname.match(/\/api\/products\/(.+)$/)
    if (match?.[1]) {
      return `/product/${decodeURIComponent(match[1])}`
    }
  } catch {
    /* ignore */
  }
  return null
}

/**
 * N-1: загрузка товара по slug/id с учётом 301 из таблицы редиректов (без auto-follow).
 */
export async function fetchProductBySlug(
  identifier: string,
  config: { apiBaseServer: string, public: { apiBase: string } },
): Promise<ProductSlugFetchResult> {
  const path = normalizeApiPath(`/products/${encodeURIComponent(identifier)}`)
  const baseURL = resolveApiBase(config)

  let response: FetchResponse<Record<string, unknown>>
  try {
    response = await $fetch.raw<Record<string, unknown>>(path, {
      baseURL: baseURL || undefined,
      redirect: 'manual',
    })
  } catch (error: unknown) {
    const fetchError = error as { response?: { status?: number }, statusCode?: number }
    const status = fetchError.response?.status ?? fetchError.statusCode
    if (status === 404) {
      return { kind: 'not_found' }
    }
    throw error
  }

  const { status } = response
  const data = response._data

  if (status === 301 || status === 302) {
    const canonicalPath =
      (typeof data?.canonicalPath === 'string' && data.canonicalPath)
      || apiLocationToCanonicalPath(response.headers.get('location'))
    if (canonicalPath) {
      return { kind: 'redirect', canonicalPath }
    }
    return { kind: 'not_found' }
  }

  if (status === 404 || !data) {
    return { kind: 'not_found' }
  }

  if (data.redirect === true && typeof data.canonicalPath === 'string') {
    return { kind: 'redirect', canonicalPath: data.canonicalPath }
  }

  const product = (data.product ?? data) as Record<string, unknown>
  if (product?.id != null) {
    return { kind: 'product', product }
  }

  return { kind: 'not_found' }
}
