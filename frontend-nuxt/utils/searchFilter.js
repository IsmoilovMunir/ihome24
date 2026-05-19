export function normalizeSearchQuery(q) {
  return (q || '').toLowerCase().trim()
}

export function matchesSearchQuery(text, searchQ) {
  if (!text || !searchQ) return false
  return normalizeSearchQuery(String(text)).includes(normalizeSearchQuery(searchQ))
}

export function filterProductsByQuery(products, query) {
  const q = normalizeSearchQuery(query)
  if (q.length < 1) return []
  return (products || []).filter((p) => {
    if (matchesSearchQuery(p.name, q)) return true
    if (matchesSearchQuery(p.description, q)) return true
    if (p.category?.name && matchesSearchQuery(p.category.name, q)) return true
    if (p.sku && matchesSearchQuery(p.sku, q)) return true
    if (Array.isArray(p.benefits) && p.benefits.some(b => matchesSearchQuery(b, q))) return true
    if (Array.isArray(p.characteristics)) {
      if (p.characteristics.some(c => matchesSearchQuery(c.name, q) || matchesSearchQuery(c.value, q))) {
        return true
      }
    }
    return false
  })
}

export function filterCategoriesByQuery(categories, query) {
  const q = normalizeSearchQuery(query)
  if (q.length < 1) return []
  return (categories || []).filter(c => c.name && matchesSearchQuery(c.name, q))
}
