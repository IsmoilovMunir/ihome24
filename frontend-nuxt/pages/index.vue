<script setup>
const apiBase = useApiBase()
const siteUrl = useSiteUrl()

const fetchOpts = { baseURL: apiBase, timeout: 60_000 }

const [{ data: products }, { data: categories }] = await Promise.all([
  useAsyncData('home-products', () => $fetch('/api/products', fetchOpts)),
  useAsyncData('home-categories', () => $fetch('/api/categories', fetchOpts)),
])

const productsStore = useProductsStore()
productsStore.$patch({
  products: products.value ? toPlainSerializable(products.value) : [],
  categories: categories.value ? toPlainSerializable(categories.value) : [],
  loading: false,
  error: null,
})

// После добавления товара в админке — подтянуть свежий каталог на клиенте
onMounted(() => {
  const { refreshCatalog } = useCatalogRefresh()
  refreshCatalog()
})

useSeoMeta({
  title: 'iHome24 - Интернет-магазин товаров для дома',
  description: 'iHome24 - интернет-магазин товаров для дома и офиса. Каталог, выгодные цены, доставка.',
  robots: 'index, follow',
})

useHead({
  link: [{ rel: 'canonical', href: `${siteUrl}/` }],
  script: [
    {
      type: 'application/ld+json',
      key: 'jsonld-website',
      innerHTML: JSON.stringify({
        '@context': 'https://schema.org',
        '@type': 'WebSite',
        name: 'iHome24',
        url: `${siteUrl}/`,
        potentialAction: {
          '@type': 'SearchAction',
          target: `${siteUrl}/search?q={search_term_string}`,
          'query-input': 'required name=search_term_string',
        },
      }),
    },
    {
      type: 'application/ld+json',
      key: 'jsonld-org',
      innerHTML: JSON.stringify({
        '@context': 'https://schema.org',
        '@type': 'Organization',
        name: 'iHome24',
        url: `${siteUrl}/`,
        logo: `${siteUrl}/photos/logo.svg`,
        email: 'info@ihome24.ru',
        telephone: '+79809416666',
      }),
    },
  ],
})
</script>

<template>
  <PagesHome />
</template>
