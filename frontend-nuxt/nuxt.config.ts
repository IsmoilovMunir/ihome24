// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-05-01',
  devtools: { enabled: true },
  ssr: true,

  modules: ['@pinia/nuxt', '@nuxtjs/tailwindcss'],

  css: ['~/assets/css/style.css'],

  runtimeConfig: {
    /** SSR → Spring API (Docker: http://backend:8080) */
    apiBaseServer: process.env.NUXT_API_BASE_SERVER || 'http://localhost:8080',
    public: {
      /** Browser API base; empty = same origin (/api via nginx) */
      apiBase: process.env.NUXT_PUBLIC_API_BASE || '',
      siteUrl: process.env.NUXT_PUBLIC_SITE_URL || 'https://ihome24.ru',
    },
  },

  routeRules: {
    '/': { prerender: false },
    '/products/**': { ssr: true },
    '/category/**': { ssr: true },
    '/support/**': { ssr: true },
    '/order-tracking': { ssr: true },
    '/services': { ssr: true },
    '/cart': { ssr: false },
    '/checkout': { ssr: false },
    '/login': { ssr: false },
    '/register': { ssr: false },
    '/search': { ssr: false },
    '/personal/**': { ssr: false },
  },

  /** Browser /api in dev hits Vite, not Nitro — proxy like legacy frontend/vite.config.js */
  vite: {
    server: {
      proxy: {
        '/api': {
          target: process.env.NUXT_API_BASE_SERVER || 'http://localhost:8080',
          changeOrigin: true,
        },
      },
    },
  },

  nitro: {
    devProxy: {
      '/api': {
        target: process.env.NUXT_API_BASE_SERVER || 'http://localhost:8080',
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
