/**
 * N-2: 301 при устаревшем slug до SSR-рендера страницы товара.
 * GET /api/products/redirect/{slug} → newSlug | null | 404
 */
const PRODUCT_PATH_RE = /^\/product\/([^/]+)(?:\/([^/]+))?\/?$/

export default defineEventHandler(async (event) => {
  const url = getRequestURL(event)
  const pathname = url.pathname

  if (
    pathname.startsWith('/api')
    || pathname.startsWith('/_nuxt')
    || pathname.startsWith('/__')
    || pathname.includes('.')
  ) {
    return
  }

  const match = pathname.match(PRODUCT_PATH_RE)
  if (!match) {
    return
  }

  const slug = decodeURIComponent(match[1])
  const variantSegment = match[2] ? decodeURIComponent(match[2]) : null

  const config = useRuntimeConfig()
  const apiBase = String(config.apiBaseServer || 'http://localhost:8080').replace(/\/$/, '')

  let data: { newSlug?: string | null } | null = null
  try {
    data = await $fetch<{ newSlug?: string | null }>(
      `${apiBase}/api/products/redirect/${encodeURIComponent(slug)}`,
    )
  } catch (error: unknown) {
    const fetchError = error as { response?: { status?: number }, statusCode?: number }
    const status = fetchError.response?.status ?? fetchError.statusCode
    if (status === 404) {
      throw createError({
        statusCode: 404,
        statusMessage: 'Товар не найден',
        message: 'Товар не найден',
      })
    }
    throw error
  }

  const newSlug = data?.newSlug?.trim()
  if (!newSlug) {
    return
  }

  let targetPath = `/product/${newSlug}`
  if (variantSegment) {
    targetPath += `/${variantSegment}`
  }
  const target = `${targetPath}${url.search}`

  return sendRedirect(event, target, 301)
})
