<script setup>
const currentTab = ref('Новые')

const tabsData = [
  'Новые',
  'В подготовке',
  'Доставка',
]
</script>

<template>
  <VCard class="country-order-card">
    <VCardItem
      title="Заказы по странам"
      subtitle="0 доставок в процессе"
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
        :key="index"
      >
        {{ tab }}
      </VTab>
    </VTabs>

    <VCardText>
      <VWindow v-model="currentTab">
        <VWindowItem>
          <div class="text-center text-disabled py-8">
            Нет заказов
          </div>
        </VWindowItem>

        <VWindowItem>
          <div class="text-center text-disabled py-8">
            Нет заказов
          </div>
        </VWindowItem>

        <VWindowItem>
          <div class="text-center text-disabled py-8">
            Нет заказов
          </div>
        </VWindowItem>
      </VWindow>
    </VCardText>
  </VCard>
</template>

<style lang="scss">
.country-order-card {
  .v-timeline .v-timeline-divider__dot .v-timeline-divider__inner-dot {
    box-shadow: none !important;
  }
}
</style>
