import { buildCategoryPath, getCategoryChain } from '~/utils/categoryUrl'

type CategoryLike = {
  id: number
  name: string
  description?: string | null
  slug?: string | null
}

/** SEO meta и BreadcrumbList для страницы категории. */
export function useCategoryCatalogSeo(
  category: Ref<CategoryLike | null | undefined>,
  categories: Ref<CategoryLike[] | undefined>,
) {
  const siteUrl = useSiteUrl()
  const route = useRoute()

  const canonicalPath = computed(() => {
    const cat = category.value
    const list = categories.value ?? []
    if (!cat?.id) return route.path
    return buildCategoryPath(cat, list)
  })

  const pageTitle = computed(() => {
    const name = category.value?.name
    return name ? `${name} - iHome24` : 'Каталог - iHome24'
  })

  const pageDescription = computed(() => {
    const cat = category.value
    if (!cat) return 'Каталог товаров iHome24.'
    if (cat.description?.trim()) return cat.description.trim()
    return `Купить ${cat.name} в интернет-магазине iHome24. Актуальные цены и доставка.`
  })

  useSeoMeta({
    title: pageTitle,
    description: pageDescription,
    ogTitle: pageTitle,
    ogDescription: pageDescription,
    robots: 'index, follow',
  })

  useHead({
    link: computed(() => [
      { rel: 'canonical', href: `${siteUrl}${canonicalPath.value}`, key: 'canonical' },
    ]),
    script: computed(() => {
      const cat = category.value
      const list = categories.value ?? []
      if (!cat?.name) return []

      const chain = getCategoryChain(cat, list)
      const items = [
        {
          '@type': 'ListItem',
          position: 1,
          name: 'Главная',
          item: `${siteUrl}/`,
        },
        ...chain.map((c, index) => ({
          '@type': 'ListItem',
          position: index + 2,
          name: c.name,
          item: `${siteUrl}${buildCategoryPath(c, list)}`,
        })),
      ]

      return [
        {
          type: 'application/ld+json',
          key: 'jsonld-breadcrumb',
          innerHTML: JSON.stringify({
            '@context': 'https://schema.org',
            '@type': 'BreadcrumbList',
            itemListElement: items,
          }),
        },
      ]
    }),
  })
}
