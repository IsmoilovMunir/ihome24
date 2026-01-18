<script setup>
const statistics = [
  {
    title: 'Продажи',
    stats: '0',
    icon: 'tabler-chart-pie-2',
    color: 'primary',
  },
  {
    title: 'Клиенты',
    stats: '0',
    icon: 'tabler-users',
    color: 'info',
  },
  {
    title: 'Товары',
    stats: '0',
    icon: 'tabler-shopping-cart',
    color: 'error',
  },
  {
    title: 'Доход',
    stats: '₽0',
    icon: 'tabler-currency-ruble',
    color: 'success',
  },
]
</script>

<template>
  <VCard title="Статистика">
    <template #append>
      <span class="text-sm text-disabled">Обновлено месяц назад</span>
    </template>

    <VCardText>
      <VRow>
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
