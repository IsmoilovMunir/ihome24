<script setup>
const notifications = ref([
  {
    type: 'Новое для вас',
    email: true,
    browser: true,
    app: true,
  },
  {
    type: 'Активность аккаунта',
    email: true,
    browser: true,
    app: true,
  },
  {
    type: 'Использован новый браузер для входа',
    email: true,
    browser: true,
    app: false,
  },
  {
    type: 'Подключено новое устройство',
    email: true,
    browser: false,
    app: false,
  },
])
</script>

<template>
  <VCard class="user-tab-notification">
    <VCardItem>
      <VCardTitle class="mb-1">
        Уведомления
      </VCardTitle>
      <VCardSubtitle class="text-body-1 text-wrap">
        Вы будете получать уведомления для выбранных ниже элементов.
      </VCardSubtitle>
    </VCardItem>
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
            v-for="(notification, index) in notifications"
            :key="notification.type"
            :class="index % 2 === 0 ? 'table-colored-raw' : ''"
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
