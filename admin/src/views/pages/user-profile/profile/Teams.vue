<script setup>
const props = defineProps({
  teamsData: {
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
  <VCard title="Команды">
    <template #append>
      <div>
        <MoreBtn :menu-list="moreList" />
      </div>
    </template>

    <VCardText>
      <VList class="card-list">
        <VListItem
          v-for="data in props.teamsData"
          :key="data.title"
        >
          <template #prepend>
            <VAvatar
              size="38"
              :image="data.avatar"
            />
          </template>

          <VListItemTitle class="font-weight-medium">
            {{ data.title }}
          </VListItemTitle>
          <VListItemSubtitle>{{ data.members }} Участников</VListItemSubtitle>

          <template #append>
            <VChip
              label
              :color="data.ChipColor"
              size="small"
              class="font-weight-medium"
            >
              {{ data.chipText }}
            </VChip>
          </template>
        </VListItem>

        <VListItem>
          <VListItemTitle class="pt-2 text-center">
            <RouterLink :to="{ name: 'pages-user-profile-tab', params: { tab: 'teams' } }">
              <p class="mb-0">
                Просмотреть все команды
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
