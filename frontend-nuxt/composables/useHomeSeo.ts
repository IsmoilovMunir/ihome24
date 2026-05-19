import { SITE_DEFAULT_DESCRIPTION, SITE_DEFAULT_TITLE } from '~/utils/siteSeo'

/** SEO и Schema.org для главной страницы (SSR в <head>). */
export function useHomeSeo() {
  const siteUrl = useSiteUrl()

  useSeoMeta({
    title: SITE_DEFAULT_TITLE,
    description: SITE_DEFAULT_DESCRIPTION,
    ogTitle: SITE_DEFAULT_TITLE,
    ogDescription: SITE_DEFAULT_DESCRIPTION,
    ogType: 'website',
    robots: 'index, follow',
  })

  useHead({
    link: [{ rel: 'canonical', href: `${siteUrl}/`, key: 'canonical' }],
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
            target: {
              '@type': 'EntryPoint',
              urlTemplate: `${siteUrl}/search?q={search_term_string}`,
            },
            'query-input': 'required name=search_term_string',
          },
        }),
      },
      {
        type: 'application/ld+json',
        key: 'jsonld-organization',
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
}
