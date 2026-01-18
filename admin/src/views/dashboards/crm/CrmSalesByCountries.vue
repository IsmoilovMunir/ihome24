<script setup>
import usFlag from '@images/icons/countries/us.png'

const salesByCountries = [
  {
    avatarImg: usFlag,
    stats: '$8,567k',
    subtitle: 'Россия',
    profitLoss: 25.8,
  },
  {
    avatarImg: usFlag,
    stats: '$2,415k',
    subtitle: 'Россия',
    profitLoss: -6.2,
  },
  {
    avatarImg: usFlag,
    stats: '$865k',
    subtitle: 'Россия',
    profitLoss: 12.4,
  },
  {
    avatarImg: usFlag,
    stats: '$745k',
    subtitle: 'Россия',
    profitLoss: -11.9,
  },
  {
    avatarImg: usFlag,
    stats: '$45',
    subtitle: 'Россия',
    profitLoss: 16.2,
  },
  {
    avatarImg: usFlag,
    stats: '$12k',
    subtitle: 'Россия',
    profitLoss: 14.8,
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
    title="Продажи по странам"
    subtitle="Ежемесячный обзор продаж"
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
          v-for="country in salesByCountries"
          :key="country.stats"
        >
          <template #prepend>
            <VAvatar
              size="34"
              color="secondary"
              variant="tonal"
              class="me-1"
              :image="country.avatarImg"
            />
          </template>

          <VListItemTitle class="font-weight-medium">
            {{ country.stats }}
          </VListItemTitle>
          <VListItemSubtitle>
            {{ country.subtitle }}
          </VListItemSubtitle>

          <template #append>
            <div :class="`d-flex align-center font-weight-medium ${country.profitLoss > 0 ? 'text-success' : 'text-error'}`">
              <VIcon
                :icon="country.profitLoss > 0 ? 'tabler-chevron-up' : 'tabler-chevron-down'"
                size="20"
                class="me-1"
              />
              <div class="font-weight-medium">
                {{ Math.abs(country.profitLoss) }}%
              </div>
            </div>
          </template>
        </VListItem>
      </VList>
    </VCardText>
  </VCard>
</template>

<style lang="scss" scoped>
.card-list {
  --v-card-list-gap: 1rem;
}
</style>
