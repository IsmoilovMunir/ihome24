import { createApiServices } from '~/utils/createApi'
import { resolveApiBase, resolvePublicApiBase } from '~/utils/apiFetch'

export default defineNuxtPlugin(() => {
  const config = useRuntimeConfig()
  const apiBaseURL = resolveApiBase({
    apiBaseServer: String(config.apiBaseServer || ''),
    public: { apiBase: String(config.public.apiBase || '') },
  })
  const fileBaseURL = resolvePublicApiBase({
    public: { apiBase: String(config.public.apiBase || '') },
  })
  const services = createApiServices(apiBaseURL, fileBaseURL)
  return { provide: services }
})
