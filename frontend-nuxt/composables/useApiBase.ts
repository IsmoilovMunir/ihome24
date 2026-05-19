import { resolveApiBase } from '~/utils/apiFetch'

/** @deprecated Предпочтительно useApi() / $apiFetch */
export function useApiBase() {
  const config = useRuntimeConfig()
  const base = resolveApiBase({
    apiBaseServer: String(config.apiBaseServer || ''),
    public: { apiBase: String(config.public.apiBase || '') },
  })
  if (base) return base
  if (import.meta.client && typeof window !== 'undefined') {
    return window.location.origin
  }
  return 'http://localhost:8080'
}

export function useSiteUrl() {
  const config = useRuntimeConfig()
  return String(config.public.siteUrl || 'https://ihome24.ru').replace(/\/$/, '')
}
