<script setup>
const transitions = [
  {
    avatarIcon: 'tabler-wallet',
    avatarColor: 'primary',
    title: 'Кошелек',
    subtitle: 'Starbucks',
    stats: '-$75',
    profit: false,
  },
  {
    avatarIcon: 'tabler-building-bank',
    avatarColor: 'success',
    title: 'Банковский перевод',
    subtitle: 'Пополнение счета',
    stats: '+$480',
    profit: true,
  },
  {
    avatarIcon: 'tabler-brand-paypal',
    avatarColor: 'error',
    title: 'PayPal',
    subtitle: 'Оплата клиента',
    stats: '+$268',
    profit: true,
  },
  {
    avatarIcon: 'tabler-credit-card',
    avatarColor: 'secondary',
    title: 'Master Card',
    subtitle: 'Заказан iPhone 13',
    stats: '-$699',
    profit: false,
  },
  {
    avatarIcon: 'tabler-currency-dollar',
    avatarColor: 'info',
    title: 'Банковские транзакции',
    subtitle: 'Возврат',
    stats: '+$98',
    profit: true,
  },
  {
    avatarIcon: 'tabler-brand-paypal',
    avatarColor: 'error',
    title: 'PayPal',
    subtitle: 'Оплата клиента',
    stats: '+$126',
    profit: true,
  },
  {
    avatarIcon: 'tabler-building-bank',
    avatarColor: 'success',
    title: 'Банковский перевод',
    subtitle: 'Оплата аренды офиса',
    stats: '-$1290',
    profit: false,
  },
]

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
    subtitle="Всего 58 транзакций за этот месяц"
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
          :key="transition.title"
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
