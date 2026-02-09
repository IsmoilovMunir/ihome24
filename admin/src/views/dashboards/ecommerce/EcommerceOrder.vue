<script setup>
const currentTab = ref(0)
const dashboardData = inject('dashboardData')

const tabsData = [
  { key: 'PENDING', label: 'Новые' },
  { key: 'IN_PROCESSING', label: 'В подготовке' },
  { key: 'IN_DELIVERY', label: 'Доставка' },
]

const ordersByStatus = computed(() => dashboardData?.value?.ordersByStatus ?? {})
const ordersByStatusCount = computed(() => dashboardData?.value?.ordersByStatusCount ?? {})

const inDeliveryCount = computed(() => ordersByStatusCount.value?.IN_DELIVERY ?? 0)

const currentOrders = computed(() => {
  const key = tabsData[currentTab.value]?.key
  return ordersByStatus.value[key] ?? []
})

const formatRevenue = val => {
  if (val == null) return '₽0'
  return new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', minimumFractionDigits: 0 }).format(val)
}

</script>

<template>
  <VCard class="country-order-card">
    <VCardItem
      title="Заказы по статусу"
      :subtitle="`${inDeliveryCount} доставок в процессе`"
      class="pb-4"
    >
      <template #append>
        <MoreBtn size="small" />
      </template>
    </VCardItem>

    <VTabs
      v-model="currentTab"
      grow
      class="disable-tab-transition"
    >
      <VTab
        v-for="(tab, index) in tabsData"
        :key="tab.key"
      >
        {{ tab.label }}
      </VTab>
    </VTabs>

    <VCardText>
      <VWindow v-model="currentTab">
        <VWindowItem
          v-for="tab in tabsData"
          :key="tab.key"
        >
          <div
            v-if="currentOrders.length === 0"
            class="text-center text-disabled py-8"
          >
            Нет заказов
          </div>
          <VList
            v-else
            class="card-list"
          >
            <VListItem
              v-for="order in currentOrders"
              :key="order.id"
              :to="{ name: 'apps-ecommerce-order-details-id', params: { id: order.id } }"
              class="cursor-pointer"
            >
              <VListItemTitle class="font-weight-medium">
                #{{ order.orderNumber }} — {{ order.customer }}
              </VListItemTitle>
              <VListItemSubtitle>
                {{ formatRevenue(order.spent) }}
              </VListItemSubtitle>
            </VListItem>
          </VList>
        </VWindowItem>
      </VWindow>
    </VCardText>
  </VCard>
</template>

<style lang="scss" scoped>
.card-list {
  --v-card-list-gap: 16px;
}

.country-order-card {
  .v-timeline .v-timeline-divider__dot .v-timeline-divider__inner-dot {
    box-shadow: none !important;
  }
}
</style>
