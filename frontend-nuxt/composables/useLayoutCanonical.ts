import { buildCanonicalHref } from '~/utils/canonicalUrl'

/**
 * Canonical для layout: текущий URL без page / sort / order / UTM.
 * Страницы с собственным canonical должны задавать link с key: 'canonical'.
 */
export function useLayoutCanonical() {
  const route = useRoute()
  const siteUrl = useSiteUrl()

  const href = computed(() => buildCanonicalHref(route, siteUrl))

  useHead({
    link: computed(() => [
      { rel: 'canonical', href: href.value, key: 'canonical' },
    ]),
  })
}
