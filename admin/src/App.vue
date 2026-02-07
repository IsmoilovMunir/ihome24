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
import { useBackendReady } from '@/composables/useBackendReady'
import { VNodeRenderer } from '@layouts/components/VNodeRenderer'
import { themeConfig } from '@themeConfig'

const { global } = useTheme()

// ℹ️ Sync current theme with initial loader theme
initCore()
initConfigStore()

const configStore = useConfigStore()
const ability = useAbility()
const { isReady: isBackendReady, errorMessage: backendError } = useBackendReady()

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
      <!-- Ожидание backend: показываем overlay до первого успешного ответа от сервера -->
      <div
        v-if="!isBackendReady"
        class="backend-wait-overlay"
      >
        <div class="backend-wait-content">
          <div class="backend-wait-logo">
            <VNodeRenderer :nodes="themeConfig.app.logo" />
          </div>
          <h1 class="backend-wait-title">
            {{ themeConfig.app.title }}
          </h1>
          <VProgressCircular
            indeterminate
            color="primary"
            size="48"
            width="3"
            class="backend-wait-spinner"
          />
        </div>
      </div>
      <template v-else>
        <RouterView />
        <ScrollToTop />
        <VSnackbar
          v-if="backendError"
          :model-value="!!backendError"
          color="warning"
          location="bottom"
          :timeout="8000"
        >
          {{ backendError }}
        </VSnackbar>
      </template>
    </VApp>
  </VLocaleProvider>
</template>

<style scoped>
.backend-wait-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgb(var(--v-theme-surface));
  z-index: 9999;
}
.backend-wait-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  padding: 2rem;
}
.backend-wait-logo {
  line-height: 0;
  color: rgb(var(--v-global-theme-primary));
}
.backend-wait-logo :deep(svg) {
  width: 48px;
  height: 48px;
}
.backend-wait-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: rgb(var(--v-theme-on-surface));
  margin: 0;
}
.backend-wait-spinner {
  margin-top: 0.5rem;
}
</style>
