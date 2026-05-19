/**
 * Глобальный $apiFetch с теми же правилами base URL и обработкой 404/500, что и useApi().
 */
export default defineNuxtPlugin(() => {
  const { api, getBaseURL } = useApi()

  return {
    provide: {
      apiFetch: api,
      apiBase: getBaseURL,
    },
  }
})
