import type { FetchOptions, FetchError } from 'ofetch'

/** SSR → Spring напрямую; CSR → относительный /api (или NUXT_PUBLIC_API_BASE). */
export function resolveApiBase(config: {
  apiBaseServer: string
  public: { apiBase: string }
}): string {
  if (import.meta.server) {
    return String(config.apiBaseServer || 'http://localhost:8080').replace(/\/$/, '')
  }
  const pub = String(config.public.apiBase || '').trim()
  if (pub) return pub.replace(/\/$/, '')
  return ''
}

/**
 * Base для URL файлов/картинок в HTML (src, og:image).
 * Никогда не использует apiBaseServer (http://backend:8080) — браузер его не откроет.
 */
export function resolvePublicApiBase(config: {
  public: { apiBase: string }
}): string {
  const pub = String(config.public.apiBase || '').trim()
  if (pub) return pub.replace(/\/$/, '')
  if (import.meta.client && typeof window !== 'undefined') {
    return window.location.origin
  }
  // SSR: относительные /api/files/... резолвятся от origin сайта (nginx → backend)
  return ''
}

/** Путь всегда начинается с /api */
export function normalizeApiPath(path: string): string {
  if (/^https?:\/\//i.test(path)) return path
  if (path.startsWith('/api')) return path
  return `/api${path.startsWith('/') ? path : `/${path}`}`
}

export function handleApiFetchError(error: unknown): never {
  const fetchError = error as FetchError
  const status =
    fetchError.response?.status
    ?? fetchError.statusCode
    ?? (fetchError as { status?: number }).status
    ?? 500

  if (status === 404) {
    throw createError({
      statusCode: 404,
      statusMessage: 'Ресурс не найден',
      message: fetchError.message || 'Ресурс не найден',
      fatal: true,
    })
  }

  if (status >= 500) {
    throw createError({
      statusCode: 500,
      statusMessage: 'Ошибка сервера',
      message: fetchError.message || 'Внутренняя ошибка сервера',
      fatal: true,
    })
  }

  throw error
}

export type ApiFetch = <T>(path: string, options?: FetchOptions) => Promise<T>

export function createApiFetch(getBaseURL: () => string): ApiFetch {
  return async function apiFetch<T>(path: string, options: FetchOptions = {}): Promise<T> {
    const baseURL = getBaseURL()
    const url = normalizeApiPath(path)

    const headers: Record<string, string> = {
      ...(options.headers as Record<string, string> | undefined),
    }

    if (import.meta.client && typeof localStorage !== 'undefined') {
      const token = localStorage.getItem('accessToken')
      if (token && !headers.Authorization) {
        headers.Authorization = `Bearer ${token}`
      }
    }

    try {
      return await $fetch<T>(url, {
        ...options,
        baseURL: baseURL || undefined,
        headers: Object.keys(headers).length ? headers : options.headers,
      })
    } catch (error) {
      handleApiFetchError(error)
    }
  }
}
