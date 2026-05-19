import { createApiServices } from '~/utils/createApi'

export default defineNuxtPlugin(() => {
  const baseURL = useApiBase()
  const services = createApiServices(baseURL)
  return { provide: services }
})
