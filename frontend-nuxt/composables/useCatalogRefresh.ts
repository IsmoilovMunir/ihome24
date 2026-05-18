/** Обновить каталог с API (как старый frontend), без спиннера если данные уже на экране. */
export function useCatalogRefresh() {
  const productsStore = useProductsStore()

  async function refreshCatalog() {
    const bgProducts = productsStore.products.length > 0
    const bgCategories = productsStore.categories.length > 0
    await Promise.all([
      productsStore.fetchProducts({ background: bgProducts }),
      productsStore.fetchCategories({ background: bgCategories }),
    ])
  }

  return { refreshCatalog }
}
