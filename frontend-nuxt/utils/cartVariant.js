export function normalizeProductId(id) {
  if (id == null) return null
  const n = Number(id)
  return Number.isNaN(n) ? id : n
}

export function getVariantKey(variant) {
  if (!variant) return null
  return (
    variant.variantId ||
    variant.sku ||
    variant.attributes?.size ||
    variant.attributes?.color ||
    null
  )
}

export function getVariantLabel(variant) {
  if (!variant) return null
  return variant.attributes?.size || variant.attributes?.color || null
}

export function getDefaultVariant(product) {
  const variants = product?.variants
  if (!variants?.length) return null
  return variants[0]
}

/** Вариант и ключ для корзины: без явного варианта — первый вариант товара (как на карточке товара). */
export function resolveCartVariant(product, variant = null) {
  const resolved = variant ?? getDefaultVariant(product)
  return {
    variant: resolved,
    variantKey: getVariantKey(resolved),
    variantLabel: getVariantLabel(resolved),
  }
}

/** Сливает строки с одним productId + variantKey (в т.ч. legacy без variantKey). */
export function deduplicateCartItems(items) {
  const map = new Map()

  for (const raw of items) {
    const product = raw.product
    const productId = normalizeProductId(product?.id)
    if (productId == null) continue

    let variantKey = raw.variantKey ?? null
    let variantLabel = raw.variantLabel ?? null
    let priceBase = raw.priceBase
    let variantStockQuantity = raw.variantStockQuantity

    const defaultVariant = getDefaultVariant(product)
    if (variantKey == null && defaultVariant) {
      variantKey = getVariantKey(defaultVariant)
      variantLabel = variantLabel ?? getVariantLabel(defaultVariant)
      if (priceBase == null && defaultVariant.price?.base != null) {
        priceBase = Number(defaultVariant.price.base)
      }
      if (typeof variantStockQuantity !== 'number' && typeof defaultVariant.stock?.quantity === 'number') {
        variantStockQuantity = defaultVariant.stock.quantity
      }
    }

    const mapKey = `${productId}::${variantKey ?? ''}`
    const existing = map.get(mapKey)

    if (existing) {
      existing.quantity += raw.quantity ?? 0
      if (priceBase != null) existing.priceBase = priceBase
      if (variantLabel) existing.variantLabel = variantLabel
      if (typeof variantStockQuantity === 'number') {
        existing.variantStockQuantity = variantStockQuantity
      }
    } else {
      map.set(mapKey, {
        product: { ...product, id: productId },
        quantity: raw.quantity ?? 0,
        variantKey,
        variantLabel,
        priceBase: priceBase ?? product?.price ?? 0,
        variantStockQuantity,
      })
    }
  }

  return [...map.values()]
}
