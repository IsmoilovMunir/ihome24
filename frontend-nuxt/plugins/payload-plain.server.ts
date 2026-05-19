import { toPlainSerializable } from '~/utils/serializable'

/**
 * SSR payload должен содержать только обычные JSON-объекты.
 * Иначе devalue + pinia shouldHydrate падают на Object.create(null) (defu, $fetch, …).
 */
export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.hook('app:rendered', () => {
    const payload = nuxtApp.payload as Record<string, unknown>
    for (const key of Object.keys(payload)) {
      const plain = toPlainSerializable(payload[key])
      if (plain !== undefined) {
        payload[key] = plain
      }
    }
  })
})
