<script setup>
import { useDashboard } from '@/composables/useDashboard'

const { formatRevenue } = useDashboard()
const dashboardData = inject('dashboardData')

const statistics = computed(() => {
  const d = dashboardData?.value?.stats
  return [
    { title: 'Продажи', stats: String(d?.sales ?? 0), icon: 'tabler-chart-pie-2', color: 'primary' },
    { title: 'Клиенты', stats: String(d?.customers ?? 0), icon: 'tabler-users', color: 'info' },
    { title: 'Товары', stats: String(d?.products ?? 0), icon: 'tabler-shopping-cart', color: 'error' },
    { title: 'Доход', stats: formatRevenue(d?.revenue), icon: 'tabler-currency-ruble', color: 'success' },
  ]
})

const isLoading = computed(() => !dashboardData?.value)
const lastUpdated = ref(null)
watch(dashboardData, d => {
  if (d?.value) lastUpdated.value = new Date()
}, { immediate: true })
</script>

<template>
  <VCard
    title="Статистика"
    :loading="isLoading"
  >
    <template #append>
      <span class="text-sm text-disabled">
        {{ lastUpdated ? `Обновлено ${lastUpdated.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })}` : 'Загрузка...' }}
      </span>
    </template>

    <VCardText>
      <div v-if="isLoading" class="d-flex justify-center py-8">
        <VProgressCircular indeterminate />
      </div>
      <VRow v-else>
        <VCol
          v-for="item in statistics"
          :key="item.title"
          cols="6"
          md="3"
        >
          <div class="d-flex align-center gap-4 mt-md-9 mt-0">
            <VAvatar
              :color="item.color"
              variant="tonal"
              rounded
              size="40"
            >
              <VIcon :icon="item.icon" />
            </VAvatar>

            <div class="d-flex flex-column">
              <h5 class="text-h5">
                {{ item.stats }}
              </h5>
              <div class="text-sm">
                {{ item.title }}
              </div>
            </div>
          </div>
        </VCol>
      </VRow>
    </VCardText>
  </VCard>
</template>
