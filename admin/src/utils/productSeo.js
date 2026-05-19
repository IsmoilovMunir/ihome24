const cyrillicToLatin = {
  а: 'a', б: 'b', в: 'v', г: 'g', д: 'd', е: 'e', ё: 'yo', ж: 'zh', з: 'z',
  и: 'i', й: 'y', к: 'k', л: 'l', м: 'm', н: 'n', о: 'o', п: 'p', р: 'r',
  с: 's', т: 't', у: 'u', ф: 'f', х: 'kh', ц: 'ts', ч: 'ch', ш: 'sh', щ: 'shch',
  ъ: '', ы: 'y', ь: '', э: 'e', ю: 'yu', я: 'ya',
}

const CYRILLIC_PATTERN = /[\u0400-\u04FF]/

export const MAX_SLUG_LENGTH = 100
const SLUG_PATTERN = /^[a-z0-9]+(?:-[a-z0-9]+)*$/

export function containsCyrillic(text) {
  if (!text || typeof text !== 'string') return false
  return CYRILLIC_PATTERN.test(text)
}

export function slugifyProductName(name, maxLength = MAX_SLUG_LENGTH) {
  if (!name || typeof name !== 'string') return ''
  let transliterated = ''
  for (let i = 0; i < name.trim().length; i++) {
    const c = name[i]
    const lower = c.toLowerCase()
    if (cyrillicToLatin[lower] !== undefined) {
      transliterated += cyrillicToLatin[lower]
    } else {
      transliterated += c
    }
  }
  let slug = transliterated.toLowerCase().replace(/[^a-z0-9]+/g, '-')
  slug = slug.replace(/^-+|-+$/g, '')
  if (!slug) return ''
  return slug.length > maxLength ? slug.slice(0, maxLength).replace(/-+$/, '') : slug
}

export function isValidSlugFormat(slug) {
  if (!slug || typeof slug !== 'string') return false
  return slug.length <= MAX_SLUG_LENGTH && SLUG_PATTERN.test(slug)
}

export const META_TITLE_MAX = 120
export const META_TITLE_RECOMMENDED_MIN = 50
export const META_TITLE_RECOMMENDED_MAX = 65
export const META_TITLE_YELLOW_MAX = 80

export function defaultMetaTitle(productName) {
  const n = (productName || '').trim()
  return n ? `${n} — купить в ihome24.ru` : 'Купить в ihome24.ru'
}

/**
 * F-2: счётчик Meta Title — зелёный 50–65, жёлтый 65–80, красный >80.
 */
export function getMetaTitleLengthStatus(length, isManual = true) {
  if (!isManual) {
    return {
      color: 'info',
      message: 'Пусто — подставится автогенерация из названия товара',
    }
  }
  if (length > META_TITLE_YELLOW_MAX) {
    return {
      color: 'error',
      message: 'Более 80 символов — заголовок сильно обрежется в поиске',
    }
  }
  if (length > META_TITLE_RECOMMENDED_MAX) {
    return {
      color: 'warning',
      message: '65–80 символов — в выдаче обычно видна только часть заголовка',
    }
  }
  if (length >= META_TITLE_RECOMMENDED_MIN) {
    return {
      color: 'success',
      message: '50–65 символов — оптимально для Яндекса и Google',
    }
  }
  return {
    color: 'warning',
    message: `Менее ${META_TITLE_RECOMMENDED_MIN} символов — заголовок может выглядеть коротким`,
  }
}

export const META_DESCRIPTION_MAX = 300
export const META_DESCRIPTION_RECOMMENDED_MIN = 120
export const META_DESCRIPTION_RECOMMENDED_MAX = 160
export const META_DESCRIPTION_YELLOW_MAX = 200

export function formatPriceForMeta(price) {
  const p = Number(price)
  if (!Number.isFinite(p) || p < 0) return '0'
  return new Intl.NumberFormat('ru-RU', { maximumFractionDigits: 0 }).format(Math.round(p))
}

/** Автогенерация Meta Description по ТЗ */
export function defaultMetaDescription(productName, price) {
  const n = (productName || '').trim()
  if (!n) {
    return 'Доставка. Гарантия. Заказать на ihome24.ru.'
  }
  const priceStr = formatPriceForMeta(price)
  const text = `${n} по цене ${priceStr} ₽. Доставка. Гарантия. Заказать на ihome24.ru.`
  return text.length > META_DESCRIPTION_MAX ? text.slice(0, META_DESCRIPTION_MAX) : text
}

/**
 * F-2: счётчик Meta Description — зелёный 120–160, жёлтый 160–200, красный >200.
 */
export function getMetaDescriptionLengthStatus(length, isManual = true) {
  if (!isManual) {
    return {
      color: 'info',
      message: 'Пусто — подставится автогенерация из названия и цены',
    }
  }
  if (length > META_DESCRIPTION_YELLOW_MAX) {
    return {
      color: 'error',
      message: 'Более 200 символов — описание сильно обрежется в сниппете',
    }
  }
  if (length > META_DESCRIPTION_RECOMMENDED_MAX) {
    return {
      color: 'warning',
      message: '160–200 символов — в выдаче может показаться не полностью',
    }
  }
  if (length >= META_DESCRIPTION_RECOMMENDED_MIN) {
    return {
      color: 'success',
      message: '120–160 символов — оптимально для сниппета',
    }
  }
  return {
    color: 'warning',
    message: `Менее ${META_DESCRIPTION_RECOMMENDED_MIN} символов — описание может выглядеть коротким`,
  }
}

/** Итоговый URL карточки на витрине (F-2). */
export function buildProductPageUrl(slugPart) {
  const part = (slugPart || '').trim()
  if (!part) return 'ihome24.ru/product/…'
  return `ihome24.ru/product/${part}`
}

/**
 * Статус SEO для колонки в списке товаров (F-4).
 * @returns {'filled' | 'autogen' | 'empty'}
 */
export function getProductSeoListStatus(product) {
  const seo = product?.seo || product
  const slug = (seo?.storedSlug ?? product?.storedSlug ?? '').trim()
  const title = (seo?.storedMetaTitle ?? product?.storedMetaTitle ?? '').trim()
  const desc = (seo?.storedMetaDescription ?? product?.storedMetaDescription ?? '').trim()

  const hasSlug = slug.length > 0 && !slug.startsWith('product-')
  const hasTitle = title.length > 0
  const hasDesc = desc.length > 0

  if (hasSlug && hasTitle && hasDesc) {
    return 'filled'
  }
  if (!hasSlug && !hasTitle && !hasDesc) {
    return 'empty'
  }
  return 'autogen'
}

export const SEO_LIST_STATUS_META = {
  filled: {
    icon: 'tabler-circle-check',
    color: 'success',
    label: 'SEO заполнено',
  },
  autogen: {
    icon: 'tabler-alert-triangle',
    color: 'warning',
    label: 'Частично или автогенерация',
  },
  empty: {
    icon: 'tabler-circle-x',
    color: 'error',
    label: 'SEO не заполнено',
  },
}

export function buildProductPreviewUrl(slugPart) {
  const part = (slugPart || '').trim()
  if (!part) return 'https://ihome24.ru/products'
  return `https://ihome24.ru/product/${part}`
}

/** Видимая длина в сниппете Яндекса (F-3). */
export const YANDEX_SNIPPET_TITLE_DESKTOP = 65
export const YANDEX_SNIPPET_TITLE_MOBILE = 55
export const YANDEX_SNIPPET_DESC_DESKTOP = 160
export const YANDEX_SNIPPET_DESC_MOBILE = 120

/**
 * Обрезка текста с многоточием как в выдаче Яндекса.
 */
export function truncateSerpText(text, maxLength) {
  if (!text || typeof text !== 'string') return ''
  const s = text.trim()
  if (s.length <= maxLength) return s
  const cut = s.slice(0, maxLength).trimEnd()
  const lastSpace = cut.lastIndexOf(' ')
  const trimmed = lastSpace > maxLength * 0.6 ? cut.slice(0, lastSpace) : cut
  return `${trimmed}…`
}

/** Подсказки для полей SEO (F-2). */
export const SEO_FIELD_HINTS = {
  slug: 'Часть адреса страницы товара в латинице. Уникальный slug помогает поисковикам и людям понять ссылку. Пустое поле заполнится из названия при сохранении.',
  metaTitle: 'Заголовок вкладки браузера и синяя строка в результатах Яндекса и Google. Влияет на кликабельность (CTR).',
  metaDescription: 'Краткое описание под заголовком в поиске. Не влияет напрямую на ранжирование, но помогает получить больше переходов.',
}
