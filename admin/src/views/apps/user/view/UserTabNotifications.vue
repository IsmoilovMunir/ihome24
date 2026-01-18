<script setup>
const notifications = ref([
  {
    type: 'Новое для вас',
    email: true,
    browser: false,
    app: false,
  },
  {
    type: 'Активность аккаунта',
    email: false,
    browser: true,
    app: true,
  },
  {
    type: 'Новый браузер использован для входа',
    email: true,
    browser: true,
    app: true,
  },
  {
    type: 'Новое устройство подключено',
    email: false,
    browser: true,
    app: false,
  },
])
</script>

<template>
  <VCard
    class="user-tab-notification"
    title="Уведомления"
    subtitle="Вы будете получать уведомления для выбранных ниже элементов."
  >
    <VCardText class="px-0">
      <VDivider />
      <VTable class="text-no-wrap">
        <thead>
          <tr>
            <th scope="col">
              ТИП
            </th>
            <th scope="col">
              EMAIL
            </th>
            <th scope="col">
              БРАУЗЕР
            </th>
            <th scope="col">
              ПРИЛОЖЕНИЕ
            </th>
          </tr>
        </thead>

        <tbody>
          <tr
            v-for="notification in notifications"
            :key="notification.type"
          >
            <td class="text-high-emphasis">
              {{ notification.type }}
            </td>
            <td>
              <VCheckbox v-model="notification.email" />
            </td>
            <td>
              <VCheckbox v-model="notification.browser" />
            </td>
            <td>
              <VCheckbox v-model="notification.app" />
            </td>
          </tr>
        </tbody>
      </VTable>
      <VDivider />
    </VCardText>

    <VCardText class="d-flex flex-wrap gap-4">
      <VBtn>Сохранить изменения</VBtn>
      <VBtn
        color="secondary"
        variant="tonal"
      >
        Отменить
      </VBtn>
    </VCardText>
  </VCard>
</template>
