import { buildSitemapEntries } from '../../utils/sitemapUrls'

/**
 * Источник URL для @nuxtjs/sitemap — данные из Spring API.
 * GET /api/__sitemap__/urls
 */
export default defineEventHandler(async () => {
  const config = useRuntimeConfig()
  const apiBase = String(config.apiBaseServer || 'http://localhost:8080').replace(/\/$/, '')

  try {
    const [products, categories] = await Promise.all([
      $fetch<unknown[]>(`${apiBase}/api/products`, { timeout: 120_000 }),
      $fetch<unknown[]>(`${apiBase}/api/categories`, { timeout: 60_000 }),
    ])

    return buildSitemapEntries(
      (products || []) as Parameters<typeof buildSitemapEntries>[0],
      (categories || []) as Parameters<typeof buildSitemapEntries>[1],
    )
  } catch (error) {
    console.error('[sitemap] Failed to fetch catalog from Spring API:', error)
    throw createError({
      statusCode: 503,
      statusMessage: 'Sitemap source unavailable',
    })
  }
})
