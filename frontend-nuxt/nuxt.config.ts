// https://nuxt.com/docs/api/configuration/nuxt-config
const siteUrl = process.env.NUXT_PUBLIC_SITE_URL || 'https://ihome24.ru'
const apiServer = process.env.NUXT_API_BASE_SERVER || 'http://localhost:8080'

export default defineNuxtConfig({
  compatibilityDate: '2025-05-01',
  devtools: { enabled: true },
  ssr: true,

  modules: [
    '@pinia/nuxt',
    '@nuxtjs/tailwindcss',
    '@nuxtjs/sitemap',
    '@nuxtjs/robots',
    'nuxt-schema-org',
  ],

  site: {
    url: siteUrl,
    name: 'iHome24',
    defaultLocale: 'ru',
  },

  robots: {
    disallow: [
      '/api/',
      '/admin/',
      '/cart',
      '/checkout',
      '/account',
      '/personal/',
      '/login/',
      '/register/',
      '/search/',
    ],
    sitemap: `${siteUrl}/sitemap.xml`,
  },

  schemaOrg: {
    identity: {
      type: 'Organization',
      name: 'iHome24',
      url: siteUrl,
      logo: `${siteUrl}/photos/logo.svg`,
    },
  },

  /** Sitemap: товары и категории из Spring API (server/api/__sitemap__/urls). */
  sitemap: {
    sources: ['/api/__sitemap__/urls'],
    excludeAppSources: true,
    autoLastmod: true,
    cacheMaxAgeSeconds: 21_600,
    xsl: false,
    discoverImages: false,
  },

  css: ['~/assets/css/style.css'],

  runtimeConfig: {
    /** SSR → Spring API (Docker: http://backend:8080) */
    apiBaseServer: apiServer,
    public: {
      /** Browser API base (NUXT_PUBLIC_API_BASE); пусто = same origin /api */
      apiBase: process.env.NUXT_PUBLIC_API_BASE || '',
      siteUrl,
    },
  },

  routeRules: {
    '/': { prerender: false },
    '/product/**': { ssr: true },
    '/products/**': { ssr: true },
    '/category/**': { ssr: true },
    '/catalog/**': { ssr: true },
    '/support/**': { ssr: true },
    '/order-tracking': { ssr: true },
    '/services': { ssr: true },
    '/optovym-klientam': { ssr: true },
    '/cart': { ssr: false },
    '/checkout': { ssr: false },
    '/login': { ssr: false },
    '/register': { ssr: false },
    '/search': { ssr: true },
    '/account': { ssr: true },
    '/account/**': { ssr: true },
    '/personal/**': { ssr: false },
  },

  /** Browser /api in dev hits Vite; SSR — nitro devProxy */
  vite: {
    server: {
      proxy: {
        '/api': {
          target: apiServer,
          changeOrigin: true,
        },
      },
    },
  },

  nitro: {
    devProxy: {
      '/api': {
        target: apiServer,
        changeOrigin: true,
      },
    },
  },

  app: {
    head: {
      htmlAttrs: { lang: 'ru' },
      charset: 'utf-8',
      viewport: 'width=device-width, initial-scale=1',
      link: [
        { rel: 'icon', type: 'image/svg+xml', href: '/photos/fiveicon.svg' },
        { rel: 'preconnect', href: 'https://images.unsplash.com', crossorigin: '' },
      ],
      meta: [
        { name: 'yandex-verification', content: 'af8d96cb29abf22a' },
      ],
    },
  },
})
