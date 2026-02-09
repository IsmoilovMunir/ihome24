<script setup>
import {
  useDisplay,
  useTheme,
} from 'vuetify'
import { hexToRgb } from '@layouts/utils'

const vuetifyTheme = useTheme()
const display = useDisplay()
const dashboardData = inject('dashboardData')

const totalLeads = computed(() => {
  const products = dashboardData?.value?.popularProducts ?? []
  return products.reduce((s, p) => s + (p.totalQuantity || 0), 0)
})


const chartLabels = computed(() => {
  const products = dashboardData?.value?.popularProducts ?? []
  return products.length > 0 ? products.map(p => p.productName) : ['Нет данных']
})

const chartSeries = computed(() => {
  const products = dashboardData?.value?.popularProducts ?? []
  return products.length > 0 ? products.map(p => p.totalQuantity) : [0]
})

const chartOptions = computed(() => {
  const currentTheme = vuetifyTheme.current.value.colors
  const variableTheme = vuetifyTheme.current.value.variables
  const labelSuccessColor = `rgba(${ hexToRgb(currentTheme.success) },0.2)`
  const headingColor = `rgba(${ hexToRgb(currentTheme['on-background']) },${ variableTheme['high-emphasis-opacity'] })`

  const chartColors = {
    donut: {
      series1: currentTheme.success,
      series2: '#28c76fb3',
      series3: '#28c76f80',
      series4: labelSuccessColor,
    },
  }
  
  return {
    chart: {
      parentHeightOffset: 0,
      type: 'donut',
    },
    labels: chartLabels.value,
    colors: [
      chartColors.donut.series1,
      chartColors.donut.series2,
      chartColors.donut.series3,
      chartColors.donut.series4,
    ].slice(0, Math.max(1, chartLabels.value.length)),
    stroke: { width: 0 },
    dataLabels: {
      enabled: false,
      formatter(val) {
        return `${ Number.parseInt(val) }%`
      },
    },
    legend: { show: false },
    tooltip: { theme: false },
    grid: {
      padding: {
        top: 0,
        bottom: -10,
        right: -20,
        left: -20,
      },
    },
    states: { hover: { filter: { type: 'none' } } },
    plotOptions: {
      pie: {
        donut: {
          size: '70%',
          labels: {
            show: true,
            value: {
              fontSize: '1.375rem',
              fontFamily: 'Public Sans',
              color: headingColor,
              fontWeight: 600,
              offsetY: -15,
              formatter(val) {
                return `${ Number.parseInt(val) }%`
              },
            },
            name: {
              offsetY: 20,
              fontFamily: 'Public Sans',
            },
            total: {
              show: true,
              showAlways: true,
              color: currentTheme.success,
              fontSize: '.8125rem',
              label: 'Всего',
              fontFamily: 'Public Sans',
              formatter(_w, opts) {
                return opts.globals.seriesTotals.reduce((a, b) => a + b, 0)
              },
            },
          },
        },
      },
    },
    responsive: [
      {
        breakpoint: display.thresholds.value.lg,
        options: {
          chart: {
            width: 100,
            height: 90,
          },
        },
      },
      {
        breakpoint: 420,
        options: {
          chart: {
            width: 80,
            height: 80,
          },
        },
      },
    ],
  }
})
</script>

<template>
  <VCard>
    <VCardText class="d-flex justify-space-between overflow-hidden">
      <div class="d-flex flex-column">
        <div class="mb-auto">
          <h5 class="text-h5 text-no-wrap">
            Сгенерированные лиды
          </h5>
          <div class="text-base">
            Ежемесячный отчет
          </div>
        </div>

        <div>
          <h3 class="text-h3">
            {{ totalLeads }}
          </h3>
          <div>
            <VIcon
              icon="tabler-chevron-up"
              color="success"
              class="me-1"
            />
            <span class="text-success font-weight-medium">0% </span>
          </div>
        </div>
      </div>
      <div class="overflow-hidden flex-shrink-0">
        <VueApexCharts
          :options="chartOptions"
          :series="chartSeries"
          :height="90"
          :width="90"
        />
      </div>
    </VCardText>
  </VCard>
</template>
