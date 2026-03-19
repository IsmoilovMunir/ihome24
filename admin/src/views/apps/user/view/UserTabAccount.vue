<script setup>
/**
 * Вкладка «Аккаунт»: только данные из API (без демо-проектов / таймлайна / шаблонных счетов).
 */
const props = defineProps({
  userData: {
    type: Object,
    required: true,
  },
})

const formatDate = value => {
  if (!value)
    return '—'
  try {
    const d = new Date(value)
    if (Number.isNaN(d.getTime()))
      return String(value)
    return d.toLocaleString('ru-RU')
  } catch {
    return String(value)
  }
}
</script>

<template>
  <VRow>
    <VCol cols="12">
      <VCard title="Данные пользователя">
        <VCardText>
          <p class="text-body-2 text-medium-emphasis mb-6">
            Информация загружается с сервера. Редактирование профиля — через панель слева или вкладку «Безопасность» (пароль).
          </p>

          <VRow>
            <VCol
              cols="12"
              md="6"
            >
              <AppTextField
                :model-value="props.userData.fullName || '—'"
                label="Полное имя"
                readonly
                density="comfortable"
              />
            </VCol>
            <VCol
              cols="12"
              md="6"
            >
              <AppTextField
                :model-value="props.userData.username || '—'"
                label="Имя пользователя"
                readonly
                density="comfortable"
              />
            </VCol>
            <VCol
              cols="12"
              md="6"
            >
              <AppTextField
                :model-value="props.userData.email || '—'"
                label="Email"
                readonly
                density="comfortable"
              />
            </VCol>
            <VCol
              cols="12"
              md="6"
            >
              <AppTextField
                :model-value="props.userData.phone || '—'"
                label="Телефон"
                readonly
                density="comfortable"
              />
            </VCol>
            <VCol
              cols="12"
              md="6"
            >
              <AppTextField
                :model-value="props.userData.role || '—'"
                label="Роль"
                readonly
                density="comfortable"
              />
            </VCol>
            <VCol
              cols="12"
              md="6"
            >
              <AppTextField
                :model-value="props.userData.status || '—'"
                label="Статус"
                readonly
                density="comfortable"
              />
            </VCol>
            <VCol
              cols="12"
              md="6"
            >
              <AppTextField
                :model-value="props.userData.currentPlan || '—'"
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
                :model-value="props.userData.billing || '—'"
                label="Оплата"
                readonly
                density="comfortable"
              />
            </VCol>
            <VCol
              cols="12"
              md="6"
            >
              <AppTextField
                :model-value="formatDate(props.userData.createdAt)"
                label="Создан"
                readonly
                density="comfortable"
              />
            </VCol>
            <VCol
              cols="12"
              md="6"
            >
              <AppTextField
                :model-value="formatDate(props.userData.updatedAt)"
                label="Обновлён"
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
