<script setup>
/**
 * Без демо-карт, адресов и диалогов шаблона — только то, что приходит из API пользователя.
 */
defineProps({
  userData: {
    type: Object,
    required: true,
  },
})
</script>

<template>
  <VRow>
    <VCol cols="12">
      <VCard title="Оплата и план">
        <VCardText>
          <p class="text-body-2 text-medium-emphasis mb-6">
            Подписки и привязанные карты в этом разделе не реализованы. Ниже отображаются поля профиля из API (если заполнены).
          </p>

          <VRow>
            <VCol
              cols="12"
              md="6"
            >
              <AppTextField
                :model-value="userData.currentPlan || '—'"
                label="План"
                readonly
                density="comfortable"
              />
            </VCol>
            <VCol
              cols="12"
              md="6"
            >
              <AppTextField
                :model-value="userData.billing || '—'"
                label="Оплата"
                readonly
                density="comfortable"
              />
            </VCol>
          </VRow>
        </VCardText>
      </VCard>
    </VCol>
  </VRow>
</template>
