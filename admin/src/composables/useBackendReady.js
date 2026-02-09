import { ref, onMounted } from 'vue'

/**
 * Ожидает готовности backend перед отображением приложения.
 * Опрашивает /actuator/health каждые 2 сек до успешного ответа или таймаута (30 сек).
 * Не блокирует навсегда — после таймаута приложение всё равно загрузится.
 */
export function useBackendReady() {
  const isReady = ref(false)
  const errorMessage = ref(null)

  const checkBackend = async () => {
    const origin = typeof window !== 'undefined' ? window.location.origin : ''
    const urls = ['/actuator/health', '/api/categories']
    for (const path of urls) {
      try {
        const res = await fetch(`${origin}${path}`, { method: 'GET' })
        if (res.ok) {
          isReady.value = true
          errorMessage.value = null
          return true
        }
      } catch (_e) {
        // Сеть недоступна, ECONNREFUSED и т.д. — пробуем следующий URL
      }
    }
    return false
  }

  onMounted(async () => {
    const maxAttempts = 15
    const delayMs = 2000

    for (let i = 0; i < maxAttempts; i++) {
      if (await checkBackend())
        return

      if (i < maxAttempts - 1) {
        await new Promise(r => setTimeout(r, delayMs))
      }
    }

    // После таймаута всё равно показываем приложение
    isReady.value = true
    errorMessage.value = 'Сервер не отвечает. Убедитесь, что backend запущен на localhost:8080'
  })

  return { isReady, errorMessage }
}
