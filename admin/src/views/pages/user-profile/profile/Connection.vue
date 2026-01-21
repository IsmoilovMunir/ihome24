<script setup>
const props = defineProps({
  connectionsData: {
    type: Array,
    required: true,
  },
})

const moreList = [
  {
    title: 'Поделиться связями',
    value: 'Share connections',
  },
  {
    title: 'Предложить правки',
    value: 'Suggest edits',
  },
  {
    title: 'Сообщить об ошибке',
    value: 'Report Bug',
  },
]
</script>

<template>
  <VCard title="Связи">
    <template #append>
      <div>
        <MoreBtn :menu-list="moreList" />
      </div>
    </template>

    <VCardText>
      <VList class="card-list">
        <VListItem
          v-for="data in props.connectionsData"
          :key="data.name"
        >
          <template #prepend>
            <VAvatar
              size="38"
              :image="data.avatar"
            />
          </template>

          <VListItemTitle class="font-weight-medium">
            {{ data.name }}
          </VListItemTitle>
          <VListItemSubtitle>{{ data.connections }} Связей</VListItemSubtitle>

          <template #append>
            <VBtn
              icon
              size="38"
              class="rounded"
              :variant="data.isFriend ? 'elevated' : 'tonal' "
            >
              <VIcon
                size="22"
                :icon="data.isFriend ? 'tabler-user-x' : 'tabler-user-check'"
              />
            </VBtn>
          </template>
        </VListItem>

        <VListItem>
          <VListItemTitle class="pt-2 text-center">
            <RouterLink :to="{ name: 'pages-user-profile-tab', params: { tab: 'connections' } }">
              <p class="mb-0">
                Просмотреть все связи
              </p>
            </RouterLink>
          </VListItemTitle>
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
