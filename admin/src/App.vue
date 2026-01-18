<script setup>
import { onMounted } from 'vue'
import { useTheme } from 'vuetify'
import { useAbility } from '@casl/vue'
import ScrollToTop from '@core/components/ScrollToTop.vue'
import initCore from '@core/initCore'
import {
  initConfigStore,
  useConfigStore,
} from '@core/stores/config'
import { hexToRgb } from '@core/utils/colorConverter'

const { global } = useTheme()

// ℹ️ Sync current theme with initial loader theme
initCore()
initConfigStore()

const configStore = useConfigStore()
const ability = useAbility()

// Восстанавливаем ability rules из cookies при загрузке приложения
onMounted(() => {
  const userAbilityRules = useCookie('userAbilityRules').value
  const isLoggedIn = !!(useCookie('userData').value && useCookie('accessToken').value)
  
  if (isLoggedIn && userAbilityRules) {
    try {
      // Обновляем ability, если правила есть в cookies
      if (userAbilityRules && Array.isArray(userAbilityRules) && userAbilityRules.length > 0) {
        ability.update(userAbilityRules)
      } else if (userAbilityRules && typeof userAbilityRules === 'object') {
        // Если это объект, преобразуем в массив
        ability.update([userAbilityRules])
      }
    } catch (error) {
      console.warn('Error updating ability on mount:', error)
    }
  }
})
</script>

<template>
  <VLocaleProvider :rtl="configStore.isAppRTL">
    <!-- ℹ️ This is required to set the background color of active nav link based on currently active global theme's primary -->
    <VApp :style="`--v-global-theme-primary: ${hexToRgb(global.current.value.colors.primary)}`">
      <RouterView />
      <ScrollToTop />
    </VApp>
  </VLocaleProvider>
</template>
