<script setup>
import { useTheme } from 'vuetify'
import { prefixWithPlus } from '@core/utils/formatters'

const vuetifyTheme = useTheme()

const series = [{
  data: [
    2000,
    2000,
    4000,
    4000,
    3050,
    3050,
    2000,
    2000,
    3050,
    3050,
    4700,
    4700,
    2750,
    2750,
    5700,
    5700,
  ],
}]

const chartOptions = computed(() => {
  const currentTheme = vuetifyTheme.current.value.colors
  
  return {
    chart: {
      type: 'area',
      toolbar: false,
    },
    markers: { strokeColor: 'transparent' },
    dataLabels: { enabled: false },
    grid: {
      show: false,
      padding: {
        left: -10,
        right: -5,
        top: -40,
      },
    },
    stroke: {
      width: 3,
      curve: 'straight',
    },
    colors: [currentTheme.warning],
    fill: {
      type: 'gradient',
      gradient: {
        opacityFrom: 0.6,
        opacityTo: 0.15,
        stops: [
          0,
          95,
          100,
        ],
      },
    },
    xaxis: {
      labels: { show: false },
      axisBorder: { show: false },
      axisTicks: { show: false },
      lines: { show: false },
    },
    yaxis: {
      labels: { show: false },
      min: 1000,
      max: 6000,
      tickAmount: 5,
    },
    tooltip: { enabled: false },
  }
})

const projectStatus = [
  {
    title: 'Пожертвования',
    amount: '$756.26',
    lossProfit: -139.34,
  },
  {
    title: 'Подкасты',
    amount: '$2,207.03',
    lossProfit: +576.24,
  },
]

const moreList = [
  {
    title: 'Показать больше',
    value: 'View More',
  },
  {
    title: 'Удалить',
    value: 'Delete',
  },
]
</script>

<template>
  <VCard title="Статус проекта">
    <template #append>
      <div class="mt-n4 me-n2">
        <MoreBtn
          size="small"
          :menu-list="moreList"
        />
      </div>
    </template>

    <VCardText>
      <VList class="card-list mb-6">
        <VListItem>
          <VListItemTitle class="font-weight-medium">
            $4,3742
          </VListItemTitle>
          <template #prepend>
            <VAvatar
              color="warning"
              variant="tonal"
              rounded
              icon="tabler-currency-dollar"
            />
          </template>
          <VListItemSubtitle>
            Ваш доход
          </VListItemSubtitle>

          <template #append>
            <span class="text-success font-weight-medium">+10.2%</span>
          </template>
        </VListItem>
      </VList>

      <VueApexCharts
        :options="chartOptions"
        :series="series"
        height="208"
      />

      <VList class="card-list">
        <VListItem
          v-for="status in projectStatus"
          :key="status.title"
        >
          <VListItemTitle class="font-weight-medium">
            {{ status.title }}
          </VListItemTitle>
          <template #append>
            <span class="me-4 text-medium-emphasis">{{ status.amount }}</span>
            <span :class="status.lossProfit > 0 ? 'text-success' : 'text-error'">{{ prefixWithPlus(status.lossProfit) }}</span>
          </template>
        </VListItem>
      </VList>
    </VCardText>
  </VCard>
</template>

<style lang="scss" scoped>
.card-list {
  --v-card-list-gap: 16px;
}
</style>
