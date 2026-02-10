<script setup>
const dashboardData = inject('dashboardData')

const formatRevenue = val => {
  if (val == null) return '₽0'
  return new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', minimumFractionDigits: 0 }).format(val)
}

const formatDate = d => {
  if (!d) return ''
  return new Date(d).toLocaleDateString('ru-RU', { day: 'numeric', month: 'short', year: 'numeric' })
}

const transitions = computed(() => {
  const list = dashboardData?.value?.recentTransactions ?? []
  return list.map(t => ({
    id: t.orderId,
    title: `Заказ #${t.orderNumber}`,
    subtitle: `${t.customer} · ${formatDate(t.date)}`,
    stats: formatRevenue(t.amount),
    profit: t.isIncome,
    avatarColor: 'primary',
    avatarIcon: 'tabler-shopping-cart',
  }))
})

const totalTransactions = computed(() => transitions.value.length)

const moreList = [
  {
    title: 'Обновить',
    value: 'refresh',
  },
  {
    title: 'Скачать',
    value: 'Download',
  },
  {
    title: 'Показать все',
    value: 'View All',
  },
]
</script>

<template>
  <VCard
    title="Транзакции"
    :subtitle="`Всего ${totalTransactions} транзакций`"
  >
    <template #append>
      <div class="mt-n4 me-n2">
        <MoreBtn
          size="small"
          :menu-list="moreList"
        />
      </div>
    </template>

    <VCardText>
      <VList class="card-list">
        <VListItem
          v-for="transition in transitions"
          :key="transition.id"
          :to="{ name: 'apps-ecommerce-order-details-id', params: { id: transition.id } }"
          class="cursor-pointer"
        >
          <template #prepend>
            <VAvatar
              size="34"
              :color="transition.avatarColor"
              variant="tonal"
              class="me-1"
              rounded
            >
              <VIcon
                :icon="transition.avatarIcon"
                size="22"
              />
            </VAvatar>
          </template>

          <VListItemTitle>
            {{ transition.title }}
          </VListItemTitle>
          <VListItemSubtitle>
            {{ transition.subtitle }}
          </VListItemSubtitle>

          <template #append>
            <div class="d-flex align-center">
              <span :class="`${transition.profit ? 'text-success' : 'text-error'} font-weight-medium`">{{ transition.stats }}</span>
            </div>
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
