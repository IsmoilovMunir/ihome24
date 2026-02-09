<script setup>
const dashboardData = inject('dashboardData')

const popularProducts = computed(() => {
  const list = dashboardData?.value?.popularProducts ?? []
  return list.map(p => ({
    id: p.productId,
    title: p.productName,
    subtitle: `${p.totalQuantity} продаж`,
    stats: p.totalQuantity,
    avatarImg: p.imageUrl || null,
  }))
})

const totalVisitors = computed(() => {
  const list = dashboardData?.value?.popularProducts ?? []
  return list.reduce((sum, p) => sum + (p.totalQuantity || 0), 0)
})

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
    title="Популярные товары"
    :subtitle="`Всего ${totalVisitors} продаж`"
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
          v-for="product in popularProducts"
          :key="product.title"
        >
          <template #prepend>
            <VAvatar
              size="46"
              rounded
              class="me-1"
              :image="product.avatarImg"
              :icon="product.avatarImg ? undefined : 'tabler-package'"
            />
          </template>

          <VListItemTitle class="font-weight-medium me-4">
            {{ product.title }}
          </VListItemTitle>
          <VListItemSubtitle class="me-4">
            {{ product.subtitle }}
          </VListItemSubtitle>

          <template #append>
            <div class="d-flex align-center">
              <span class="text-body-1">{{ product.stats }}</span>
            </div>
          </template>
        </VListItem>
      </VList>
    </VCardText>
  </VCard>
</template>

<style lang="scss" scoped>
.card-list {
  --v-card-list-gap: 1.25rem;
}
</style>
