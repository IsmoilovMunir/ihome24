/** Базовый URL API: на сервере — backend, в браузере — public или origin */
export function useApiBase() {
  const config = useRuntimeConfig()
  if (import.meta.server) {
    return String(config.apiBaseServer || 'http://localhost:8080').replace(/\/$/, '')
  }
  const pub = String(config.public.apiBase || '').trim()
  if (pub) return pub.replace(/\/$/, '')
  if (import.meta.client && typeof window !== 'undefined') {
    return window.location.origin
  }
  return 'http://localhost:8080'
}

export function useSiteUrl() {
  const config = useRuntimeConfig()
  return String(config.public.siteUrl || 'https://ihome24.ru').replace(/\/$/, '')
}
