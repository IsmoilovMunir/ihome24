export function useDashboard() {
  const stats = ref(null)
  const fullData = ref(null)
  const isLoading = ref(true)
  const error = ref(null)

  const formatRevenue = val => {
    if (val == null) return '₽0'
    return new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', minimumFractionDigits: 0 }).format(val)
  }

  const fetchStats = async () => {
    try {
      const data = await $api('/apps/ecommerce/dashboard/stats')
      stats.value = data
      return data
    } catch (e) {
      console.error('Ошибка загрузки статистики:', e)
      error.value = e
      return null
    }
  }

  const fetchFull = async () => {
    isLoading.value = true
    error.value = null
    try {
      const data = await $api('/apps/ecommerce/dashboard/full')
      fullData.value = data
      stats.value = data?.stats ?? null
      return data
    } catch (e) {
      console.error('Ошибка загрузки дашборда:', e)
      error.value = e
      return null
    } finally {
      isLoading.value = false
    }
  }

  return {
    stats,
    fullData,
    isLoading,
    error,
    formatRevenue,
    fetchStats,
    fetchFull,
  }
}
