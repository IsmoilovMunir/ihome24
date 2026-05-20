import { resolveApiBase, resolvePublicApiBase } from '~/utils/apiFetch'

/** Base для $fileApi (картинки в HTML). Не использует Docker-internal backend:8080. */
export function useFileApiBase() {
  const config = useRuntimeConfig()
  return resolvePublicApiBase({
    public: { apiBase: String(config.public.apiBase || '') },
  })
}

/** @deprecated Предпочтительно useApi() / $apiFetch; для файлов — useFileApiBase() */
export function useApiBase() {
  return useFileApiBase()
}

export function useSiteUrl() {
  const config = useRuntimeConfig()
  return String(config.public.siteUrl || 'https://ihome24.ru').replace(/\/$/, '')
}
