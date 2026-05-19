import type { FetchOptions } from 'ofetch'
import { createApiFetch, resolveApiBase, type ApiFetch } from '~/utils/apiFetch'

/**
 * Обёртка над $fetch для Spring API.
 * - SSR: `runtimeConfig.apiBaseServer` (http://localhost:8080 / http://backend:8080)
 * - CSR: относительный `/api` на текущем origin (через Vite/Nginx proxy)
 */
export function useApi() {
  const config = useRuntimeConfig()

  const getBaseURL = () =>
    resolveApiBase({
      apiBaseServer: String(config.apiBaseServer || ''),
      public: { apiBase: String(config.public.apiBase || '') },
    })

  const api: ApiFetch = createApiFetch(getBaseURL)

  return {
    /** GET/POST/… с авто-префиксом /api и обработкой 404/500 */
    api,
    getBaseURL,
    get: <T>(path: string, options?: FetchOptions) =>
      api<T>(path, { ...options, method: 'GET' }),
    post: <T>(path: string, body?: unknown, options?: FetchOptions) =>
      api<T>(path, { ...options, method: 'POST', body }),
    put: <T>(path: string, body?: unknown, options?: FetchOptions) =>
      api<T>(path, { ...options, method: 'PUT', body }),
    patch: <T>(path: string, body?: unknown, options?: FetchOptions) =>
      api<T>(path, { ...options, method: 'PATCH', body }),
    del: <T>(path: string, options?: FetchOptions) =>
      api<T>(path, { ...options, method: 'DELETE' }),
  }
}
